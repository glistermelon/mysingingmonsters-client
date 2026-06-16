package com.glisterbyte.singingmonsters.sfsmapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glisterbyte.singingmonsters.common.StringUtil;
import com.glisterbyte.singingmonsters.sfsmapping.exceptions.*;
import com.glisterbyte.singingmonsters.sfsmodels.BlankJsonList;
import com.glisterbyte.singingmonsters.sfsmodels.SfsCorrelatedResultResponse;
import com.glisterbyte.singingmonsters.sfsmodels.SfsModel;
import com.glisterbyte.singingmonsters.sfsmodels.SfsResultResponse;
import com.smartfoxserver.v2.entities.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SfsMapper {

    private static final Logger logger = LoggerFactory.getLogger(SfsMapper.class);

    private static String getFieldKey(Field field) {
        return field.isAnnotationPresent(SfsKey.class)
                ? field.getAnnotation(SfsKey.class).value()
                : field.getName();
    }

    private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<>();

    static {
        primitiveDefaults.put(boolean.class, false);
        primitiveDefaults.put(byte.class, (byte)0);
        primitiveDefaults.put(short.class, (short)0);
        primitiveDefaults.put(int.class, 0);
        primitiveDefaults.put(long.class, 0L);
        primitiveDefaults.put(float.class, 0.0f);
        primitiveDefaults.put(double.class, 0.0d);
        primitiveDefaults.put(char.class, (char)0);
    }

    private static final Set<Class<?>> wrapperTypes = Set.of(
            Boolean.class, Byte.class, Short.class, Character.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class
    );

    public static boolean isBoxedType(Class<?> type) {
        return wrapperTypes.contains(type);
    }

    @SuppressWarnings("rawtypes")
    private static ArrayList<?> createRawArrayList() {
        return new ArrayList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addToUnknownList(List<?> list, Object object) {
        ((List)list).add(object);
    }

    private static <T> T mapWrapperToClass(Class<T> type, @Nullable Class<?> listElementType, SFSDataWrapper wrapper)
            throws MapFromSfsException {

        Object sourceObject = wrapper.getObject();

        if (sourceObject == null) throw new MapFromSfsException("Cannot map null to " + type.getName());

        String sourceObjectStr = sourceObject.getClass().getName() + " " + sourceObject;

        if (type.isPrimitive() || isBoxedType(type)) {
            try {
                //noinspection unchecked
                return (T)sourceObject;
            }
            catch (ClassCastException ex) {
                throw new MapFromSfsException(StringUtil.format(
                        "Cannot cast {} to {}",
                        sourceObjectStr, type.getName()
                ));
            }
        }

        if (type == String.class) {
            if (sourceObject instanceof String sourceString) {
                //noinspection unchecked
                return (T)sourceString;
            }
            else {
                throw new MapFromSfsException(StringUtil.format("Cannot cast {} to String", sourceObjectStr));
            }
        }

        if (List.class.isAssignableFrom(type)) {

            if (listElementType == null) {
                throw new MapFromSfsException(StringUtil.format(
                        "List element type is null (source object is {})", sourceObjectStr
                ));
            }

            List<?> mappedList;
            try {
                mappedList = createRawArrayList();
            }
            catch (Throwable ex) {
                throw new MapFromSfsException(
                        StringUtil.format(
                                "Failed to instantiate {} <{}> (source object is {})",
                                type.getName(), listElementType.getName(), sourceObjectStr
                        ),
                        ex
                );
            }

            if (List.class.isAssignableFrom(sourceObject.getClass())) {

                List<?> sourceList = (List<?>)sourceObject;
                for (Object sourceElement : sourceList) {
                    if (listElementType.isInstance(sourceElement)) {
                        addToUnknownList(mappedList, sourceElement);
                    }
                    else {
                        throw new MapFromSfsException(StringUtil.format(
                                "Source element {} does not match list element type {} (source object is {})",
                                sourceElement, listElementType.getName(), sourceObjectStr
                        ));
                    }
                }

            }
            else {

                if (!(sourceObject instanceof ISFSArray sfsArray)) {
                    throw new MapFromSfsException(StringUtil.format(
                            "Expected an SFSArray to map to {} <{}>, but got {}",
                            type.getName(), listElementType.getName(), sourceObjectStr
                    ));
                }

                for (SFSDataWrapper elementWrapper : sfsArray) {
                    addToUnknownList(
                            mappedList,
                            mapWrapperToClass(listElementType, null, elementWrapper)
                    );
                }

            }

            //noinspection unchecked
            return (T)mappedList;

        }

        if (sourceObject instanceof ISFSObject sfsObject) {
            return mapSFSObjectToClass(type, sfsObject);
        }

        throw new MapFromSfsException(StringUtil.format(
                "Don't know how to map {} to {} <{}>",
                sourceObjectStr, type.getName(), listElementType
        ));

    }

    public static <T> T mapSFSObjectToClass(Class<T> tClass, ISFSObject sfsObject) throws MapFromSfsException {

        T mappedObj;
        try {
            var constructor = tClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            mappedObj = constructor.newInstance();
        }
        catch (
                NoSuchMethodException
                | InvocationTargetException
                | java.lang.InstantiationException
                | IllegalAccessException ex
        ) {
            throw new MapFromSfsException("Failed to instantiate mapped object", ex);
        }

        // For failed requests, anything that can be mapped will be, but the rest will be ignored without throwing
        boolean allFieldsOptional = false;
        if (mappedObj instanceof SfsResultResponse) {
            Boolean success = sfsObject.getBool("success");
            if (success == null) throw new MissingKeyException("success", boolean.class, sfsObject);
            allFieldsOptional = !success;
        }

        try {

            for (Field field : getFields(tClass)) {

                field.setAccessible(true);

                if (field.isAnnotationPresent(SfsMapperIgnore.class)) continue;

                if (field.isAnnotationPresent(SfsObjectField.class)) {
                    field.set(mappedObj, sfsObject);
                    continue;
                }

                Class<?> fieldType = field.getType();
                String key = getFieldKey(field);
                SFSDataWrapper sourceWrapper = sfsObject.get(key);
                Object sourceObject = Optional.ofNullable(sourceWrapper)
                        .map(SFSDataWrapper::getObject).orElse(null);

                Class<?> listElementType = Optional.ofNullable(field.getAnnotation(SfsArrayElementType.class))
                        .map(SfsArrayElementType::value).orElse(null);

                if (sourceObject == null) {

                    if (field.isAnnotationPresent(SfsOptional.class) || allFieldsOptional) {
                        if (fieldType.isPrimitive()) {
                            try {
                                field.set(mappedObj, primitiveDefaults.get(fieldType));
                            }
                            catch (IllegalArgumentException ex) {
                                throw new MapFromSfsException("Failed to set primitive field", ex);
                            }
                        }
                        else field.set(mappedObj, null);
                    }
                    else {
                        throw new MissingKeyException(key, fieldType, sfsObject);
                    }

                }
                else if (field.isAnnotationPresent(SfsJsonObject.class)) {

                    if (!(sourceObject instanceof String jsonStr)) throw new MissingKeyException(key, String.class, sfsObject);
                    var mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    try {
                        field.set(mappedObj, mapper.readValue(jsonStr, field.getType()));
                    }
                    catch (JsonProcessingException ex) {
                        throw new DeserializeJsonException(jsonStr, ex);
                    }

                }
                else if (fieldType.isAnnotationPresent(SfsEntryArray.class)) {

                    if (!(sourceObject instanceof ISFSArray sfsArray)) {
                        throw new MissingKeyException(key, ISFSArray.class, sfsObject);
                    }

                    SFSObject mappedSfsObject = new SFSObject();
                    for (SFSDataWrapper elementWrapper : sfsArray) {
                        Object element = elementWrapper.getObject();
                        if (!(element instanceof ISFSObject elementSfsObject)) {
                            throw new MapFromSfsException(StringUtil.format(
                                    "SFSArray annotated with SfsPropertyArray has non-SFSObject element {} {}",
                                    element.getClass().getName(), element
                            ));
                        }
                        var entry = elementSfsObject.iterator().next();
                        mappedSfsObject.put(entry.getKey(), entry.getValue());
                    }

                    if (
                            field.isAnnotationPresent(SfsOptional.class)
                            && getFields(fieldType).stream()
                                    .map(SfsMapper::getFieldKey).noneMatch(mappedSfsObject::containsKey)
                    ) {
                        field.set(mappedObj, null);
                    }
                    else {
                        field.set(mappedObj, mapSFSObjectToClass(fieldType, mappedSfsObject));
                    }

                }
                else if (field.isAnnotationPresent(SfsJsonArray.class)) {

                    if (!(sourceObject instanceof String jsonStr)) {
                        throw new MissingKeyException(key, String.class, sfsObject);
                    }

                    if (jsonStr.isBlank()) {
                        field.set(mappedObj, new BlankJsonList<>());
                        continue;
                    }

                    if (listElementType == null) {
                        throw new MapFromSfsException(StringUtil.format(
                                "Field {}::{} needs to be annotated with SfsArrayElementType",
                                tClass.getName(), fieldType.getName()
                        ));
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    var listType = mapper.getTypeFactory().constructCollectionType(List.class, listElementType);
                    try {
                        field.set(mappedObj, mapper.readValue(jsonStr, listType));
                    }
                    catch (JsonProcessingException ex) {
                        throw new DeserializeJsonException(jsonStr, ex);
                    }

                }
                else {

                    field.set(mappedObj, mapWrapperToClass(fieldType, listElementType, sourceWrapper));

                }

            }

        }
        catch (IllegalAccessException ex) {
            throw new MapFromSfsException(ex);
        }
        catch (MapFromSfsException ex) {
            throw new MapFromSfsException(sfsObject, ex);
        }

        return mappedObj;

    }

    private static SfsModel castToModel(Object object) throws MapToSfsException {
        if (!SfsModel.class.isAssignableFrom(object.getClass())) {
            throw new MapToSfsException(StringUtil.format(
                    "Cannot cast {} {} to SfsModel",
                    object.getClass().getName(), object
            ));
        }
        return (SfsModel)object;
    }

    public static <T extends SfsModel> SFSObject mapToSFSObject(T obj) throws MapToSfsException  {

        SFSObject sfsObject = new SFSObject();

        for (Field field : getFields(obj.getClass())) {

            if (
                    field.isAnnotationPresent(SfsMapperIgnore.class)
                    || field.isAnnotationPresent(SfsObjectField.class)
            ) continue;

            try {

                field.setAccessible(true);
                Object value = field.get(obj);
                if (value == null) continue;

                String key = field.isAnnotationPresent(SfsKey.class)
                        ? field.getAnnotation(SfsKey.class).value()
                        : field.getName();

                var type = field.getType();

                if (type == int.class || type == Integer.class) {
                    sfsObject.putInt(key, (Integer)value);
                } else if (type == long.class || type == Long.class) {
                    sfsObject.putLong(key, (Long)value);
                } else if (type == short.class || type == Short.class) {
                    sfsObject.putShort(key, (Short)value);
                } else if (type == double.class || type == Double.class) {
                    sfsObject.putDouble(key, (Double)value);
                } else if (type == float.class || type == Float.class) {
                    sfsObject.putFloat(key, (Float)value);
                } else if (type == boolean.class || type == Boolean.class) {
                    sfsObject.putBool(key, (Boolean)value);
                } else if (type == String.class) {
                    sfsObject.putUtfString(key, (String)value);
                }
                else if (List.class.isAssignableFrom(type)) {
                    List<?> list = (List<?>)value;
                    Class<?> elementType;
                    if (field.isAnnotationPresent(SfsArrayElementType.class)) {
                        elementType = field.getAnnotation(SfsArrayElementType.class).value();
                    }
                    else {
                        logger.warn("""
                                No element type notation present for field '{}';
                                attempting to guess the type.""", field.getName());
                        elementType = list.isEmpty() ? null : list.getFirst().getClass();
                    }
                    if (elementType == Integer.class) {
                        @SuppressWarnings("unchecked")
                        List<Integer> intList = (List<Integer>)value;
                        sfsObject.putIntArray(key, intList);
                    } else if (elementType == Long.class) {
                        @SuppressWarnings("unchecked")
                        List<Long> longList = (List<Long>)value;
                        sfsObject.putLongArray(key, longList);
                    } else if (elementType == Short.class) {
                        @SuppressWarnings("unchecked")
                        List<Short> shortList = (List<Short>)value;
                        sfsObject.putShortArray(key, shortList);
                    } else if (elementType == Double.class) {
                        @SuppressWarnings("unchecked")
                        List<Double> doubleList = (List<Double>)value;
                        sfsObject.putDoubleArray(key, doubleList);
                    } else if (elementType == Float.class) {
                        @SuppressWarnings("unchecked")
                        List<Float> floatList = (List<Float>)value;
                        sfsObject.putFloatArray(key, floatList);
                    } else if (elementType == Boolean.class) {
                        @SuppressWarnings("unchecked")
                        List<Boolean> boolList = (List<Boolean>)value;
                        sfsObject.putBoolArray(key, boolList);
                    } else if (elementType == String.class) {
                        @SuppressWarnings("unchecked")
                        List<String> stringList = (List<String>) value;
                        sfsObject.putUtfStringArray(key, stringList);
                    } else if (elementType != null && SFSObject.class.isAssignableFrom(elementType)) {
                        SFSArray sfsArray = new SFSArray();
                        for (Object element : list) {
                            sfsArray.addSFSObject((SFSObject)element);
                        }
                        sfsObject.putSFSArray(key, sfsArray);
                    } else {
                        SFSArray sfsArray = new SFSArray();
                        for (Object element : list) {
                            sfsArray.addSFSObject(mapToSFSObject(castToModel(element)));
                        }
                        sfsObject.putSFSArray(key, sfsArray);
                    }
                }
                else {
                    sfsObject.putSFSObject(key, mapToSFSObject(castToModel(value)));
                }

            }
            catch (IllegalAccessException ex) {
                throw new MapToSfsException(ex);
            }
        }

        return sfsObject;

    }

    private static List<Field> getFields(Class<?> classType) {
        List<Field> fields = new ArrayList<>();
        while (classType != null) {
            fields.addAll(0, List.of(classType.getDeclaredFields()));
            classType = classType.getSuperclass();
        }
        return fields;
    }

}
