package com.smartfoxserver.v2.entities.data;

import java.util.ArrayList;
import java.util.List;

public class SFSDataWrapper {

   private SFSDataType typeId;
   private Object object;

   public SFSDataWrapper(SFSDataType typeId, Object object) {
      this.typeId = typeId;
      this.object = object;
   }

   public SFSDataType getTypeId() {
      return this.typeId;
   }

   public Object getObject() {
      return this.object;
   }

   @Override
   public String toString() {
      return "SFSDataWrapper(" + object.getClass().getName() + ", " + object + ")";
   }

   public String toCompactString() {

      if (typeId == SFSDataType.NULL) return SFSDataType.getCompactName(SFSDataType.NULL);

      StringBuilder builder = new StringBuilder();

      if (typeId != SFSDataType.SFS_OBJECT) {
         builder.append(SFSDataType.getCompactName(typeId));
         builder.append(':');
      }

      switch (typeId) {
         case UTF_STRING -> builder.append('"').append(object).append('"');
         case BOOL_ARRAY, BYTE_ARRAY, SHORT_ARRAY, INT_ARRAY, LONG_ARRAY,
              FLOAT_ARRAY, DOUBLE_ARRAY, UTF_STRING_ARRAY -> {
            List<?> list = (List<?>)object;
            builder.append('[')
                    .append(String.join(",", list.stream().map(
                            item -> {
                               if (typeId == SFSDataType.UTF_STRING_ARRAY) return "\"" + item + "\"";
                               else return item.toString();
                            }
                    ).toList()))
                    .append(']');
         }
         case SFS_ARRAY -> {
            try {
               List<SFSDataWrapper> list = new ArrayList<>();
               for (SFSDataWrapper element : (SFSArray)object) list.add(element);
               builder.append('[')
                       .append(String.join(",", list.stream().map(SFSDataWrapper::toCompactString).toList()))
                       .append(']');
            }
            catch (ClassCastException ex) {
               builder.append(object).append(ex);
            }
         }
         case SFS_OBJECT -> builder.append(((SFSObject)object).getCompactDump());
         default -> builder.append(object);
      }

      return builder.toString();

   }

}
