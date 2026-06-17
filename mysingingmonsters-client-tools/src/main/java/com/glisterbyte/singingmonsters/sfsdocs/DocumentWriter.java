package com.glisterbyte.singingmonsters.sfsdocs;

import com.github.therapi.runtimejavadoc.*;
import com.glisterbyte.singingmonsters.sfsmapping.*;
import com.glisterbyte.singingmonsters.sfsmodels.*;
import com.glisterbyte.singingmonsters.sfsmodels.events.GetFriendsResponse;
import com.glisterbyte.singingmonsters.sfsmodels.requests.PlayerRequest;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentWriter {

    private final DocumentMaster master;
    private final Document doc;

    private static final CommentFormatter formatter = new CommentFormatter();

    private static final Map<Class<?>, Class<?>> primitiveBoxMap = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );

    public DocumentWriter(Document doc) {
        this.master = doc.master;
        this.doc = doc;
    }

    private static String getCustomComment(ClassJavadoc classJavadoc, String tag) {
        for (OtherJavadoc other : classJavadoc.getOther()) {
            if (other.getName().equals(tag)) {
                return formatter.format(other.getComment()).replace("\\n", "\n");
            }
        }
        return null;
    }

    private static String format(String s, Object... args) {
        return MessageFormatter.arrayFormat(s, args).getMessage();
    }

    public static String mono(String s) {
        return "`" + s + "`";
    }

    public static String makeTitle(Class<?> targetClass) {
        String title = targetClass.getSimpleName();
        for (int i = 1; i < title.length(); i++) {
            if (Character.isLowerCase(title.charAt(i))) continue;
            title = title.substring(0, i) + " " + title.substring(i);
            i++;
        }
        return title;
    }

    public static String makeCommandTitle(Class<?> targetClass) {
        String title = makeTitle(targetClass);
        if (title.endsWith(" Request")) {
            title = title.substring(0, title.length() - 8);
        }
        if (title.startsWith("Db ")) {
            title = "Query " + title.substring(3) + " Database";
        }
        return title;
    }

    public static String makeDataTile(Class<?> targetClass) {
        String title = makeTitle(targetClass);
        if (title.startsWith("Sfs ")) title = title.substring(4);
        return title;
    }

    private String formatType(Class<?> type, Type... generics) {

        if (type.isPrimitive()) type = primitiveBoxMap.get(type);

        if (SfsModel.class.isAssignableFrom(type)) {

            Document dataDoc = master.getDocument(type);

            //noinspection unchecked
            master.addDataReference((Class<? extends SfsModel>)type, doc);

            return "[" + dataDoc.getTitle() + "](/" + dataDoc.getRelativePath() + ")";

        }
        else if (List.class.isAssignableFrom(type)) {
            try {
                return "array\\<" + formatType(Class.forName(generics[0].getTypeName())) + "\\>";
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            return type.getSimpleName().toLowerCase();
        }

    }

    private String formatType(Field field) {
        Type generic = field.getGenericType();
        List<Type> generics = new ArrayList<>();
        if (generic instanceof ParameterizedType parameterizedType) {
            generics.addAll(List.of(parameterizedType.getActualTypeArguments()));
        }
        return formatType(field.getType(), generics.toArray(new Type[0]));
    }

    private Table makeFieldsTable(List<Field> fields) {

        Table table = new Table("Key", "Type", "Description");

        table.setColumnWidths(20, 20, 60);

        for (Field field : fields) {

            String key = field.isAnnotationPresent(SfsKey.class)
                    ? field.getAnnotation(SfsKey.class).value()
                    : field.getName();

            String val;
            if (field.isAnnotationPresent(SfsJsonArray.class)) val = "string";
            else val = formatType(field);
            if (field.isAnnotationPresent(SfsOptional.class)) val += "?";

            String desc = "";
            if (field.isAnnotationPresent(SfsJsonArray.class)) {
                desc += "Serialized JSON to be deserialized as " + formatType(field) + ". ";
            }
            desc += formatter.format(RuntimeJavadoc.getJavadoc(field).getComment())
                    .replace("\n", "").strip();

            table.addRow(key, val, desc);

        }

        return table;

    }

    private void documentFields(Class<?> targetClass) {

        Table table = makeFieldsTable(
                Arrays.stream(targetClass.getFields())
                        .filter(f -> !f.isAnnotationPresent(SfsObjectField.class))
                        .filter(f -> f.getDeclaringClass() != SfsResultResponse.class)
                        .filter(f -> f.getDeclaringClass() != SfsEventModel.class)
                        .toList()
        );

        boolean isEntryArray = targetClass.isAnnotationPresent(SfsEntryArray.class);
        boolean isResultResponse = SfsResultResponse.class.isAssignableFrom(targetClass);

        if (isEntryArray) {
            doc.add("This data is always provided as an [entry array](/topics/EntryArray).");
        }

        if (isResultResponse) {
            doc.add(
                    "Though not listed here, the [standard response parameters]"
                            + "(/topics/StandardResponseParameters) are included."
            );
        }

        if (table.isEmpty()) {
            if (isResultResponse) doc.add("There are no other parameters.");
            else doc.add("There are no parameters.");
        }
        else {
            doc.add(table);
        }

        String afterFields = getCustomComment(RuntimeJavadoc.getJavadoc(targetClass), "afterFields");
        if (afterFields != null) doc.add(afterFields);

    }

    private void documentRequestOrResponse(Class<?> targetClass, boolean uniqueCommand) {
        if (!uniqueCommand) {
            documentFields(targetClass);
        }
        else {
            doc.newSubSection("Command");
            doc.add(mono(master.getCommand(targetClass)));
            doc.newSection("Fields");
            documentFields(targetClass);
            doc.popSection();
        }
    }

    public void documentCommand(
            Class<? extends SfsRequestModel> requestClass,
            @Nullable Class<? extends SfsEventModel> responseClass
    ) {

        String requestCommand = master.getCommand(requestClass);
        assert requestCommand != null;
        String responseCommand = responseClass == null ? null : master.getCommand(responseClass);
        boolean sameCommand = requestCommand.equals(responseCommand);

        ClassJavadoc requestJavadoc = RuntimeJavadoc.getJavadoc(requestClass);

        doc.setTitle(makeCommandTitle(requestClass));

        doc.editTitleSection();
        doc.add(requestJavadoc.getComment());

        if (sameCommand) {
            doc.newSection("Command");
            doc.add(mono(requestCommand));
        }

        doc.newSection("Request");
        documentRequestOrResponse(requestClass, !sameCommand);

        String additionalRequests = getCustomComment(requestJavadoc, "additionalRequest");
        if (additionalRequests != null) {
            doc.newSubSection("Typical Additional Requests");
            doc.add(additionalRequests);
            doc.popSection();
        }

        if (responseClass != null) {

            doc.newSection("Response");

            if (DbResponse.class.isAssignableFrom(responseClass)) {
                doc.add("See [Database Queries](/topics/DatabaseQueries) for important information about the response.");
            }

            documentRequestOrResponse(responseClass, !sameCommand);

            if (requestClass == PlayerRequest.class) addExtraFriendDocs();

            ClassJavadoc responseJavadoc = RuntimeJavadoc.getJavadoc(responseClass);
            String additionalResponses = getCustomComment(responseJavadoc, "additionalResponse");
            if (additionalResponses != null) {
                doc.newSubSection("Additional Responses");
                doc.add(additionalResponses);
                doc.popSection();
            }

        }

    }

    public void documentData(Class<? extends SfsModel> dataClass) {

        ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(dataClass);

        doc.editTitleSection();
        doc.add(classJavadoc.getComment());

        doc.newSection("Fields");
        documentFields(dataClass);

        List<Document> refDocs = master.getDataReferencingDocs(dataClass);
        if (!refDocs.isEmpty()) {
            doc.newSection("Referenced By");
            for (Document refDoc : refDocs) {
                doc.add(format("* [{}](/{})", refDoc.getTitle(), refDoc.getRelativePath()));
            }
        }

    }

    public void documentEvent(Class<? extends SfsEventModel> eventClass) {

        ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(eventClass);

        doc.setTitle(makeCommandTitle(eventClass));

        doc.editTitleSection();
        doc.add(classJavadoc.getComment());

        doc.newSection("Fields");
        documentFields(eventClass);

    }

    private void addExtraFriendDocs() {
        doc.add(format(
                "An extra response with command {} is also sent with the following fields.",
                master.getCommand(GetFriendsResponse.class)
        ));
        documentFields(GetFriendsResponse.class);
    }

}