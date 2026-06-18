package com.smartfoxserver.v2.entities.data;

public enum SFSDataType {
   NULL(0),
   BOOL(1),
   BYTE(2),
   SHORT(3),
   INT(4),
   LONG(5),
   FLOAT(6),
   DOUBLE(7),
   UTF_STRING(8),
   BOOL_ARRAY(9),
   BYTE_ARRAY(10),
   SHORT_ARRAY(11),
   INT_ARRAY(12),
   LONG_ARRAY(13),
   FLOAT_ARRAY(14),
   DOUBLE_ARRAY(15),
   UTF_STRING_ARRAY(16),
   SFS_ARRAY(17),
   SFS_OBJECT(18),
   CLASS(19),
   TEXT(20);

   private int typeID;

   private SFSDataType(int typeID) {
      this.typeID = typeID;
   }

   public static SFSDataType fromTypeId(int typeId) {
      SFSDataType[] var4;
      for(SFSDataType item : var4 = values()) {
         if (item.getTypeID() == typeId) {
            return item;
         }
      }

      throw new IllegalArgumentException("Unknown typeId for SFSDataType");
   }

   public static SFSDataType fromClass(Class clazz) {
      return null;
   }

   public int getTypeID() {
      return this.typeID;
   }

   public static String getCompactName(SFSDataType type) {
      return switch (type) {
          case NULL -> "nil";
          case BOOL -> "b";
          case BYTE -> "u8";
          case SHORT -> "i16";
          case INT -> "i32";
          case LONG -> "i64";
          case FLOAT -> "f";
          case DOUBLE -> "d";
          case UTF_STRING -> "s";
          case BOOL_ARRAY -> getCompactName(BOOL) + "[]";
          case BYTE_ARRAY -> getCompactName(BYTE) + "[]";
          case SHORT_ARRAY -> getCompactName(SHORT) + "[]";
          case INT_ARRAY -> getCompactName(INT) + "[]";
          case LONG_ARRAY -> getCompactName(LONG) + "[]";
          case FLOAT_ARRAY -> getCompactName(FLOAT) + "[]";
          case DOUBLE_ARRAY -> getCompactName(DOUBLE) + "[]";
          case UTF_STRING_ARRAY -> getCompactName(UTF_STRING) + "[]";
          case SFS_ARRAY -> "[?]";
          case SFS_OBJECT -> "obj";
          case CLASS -> "cls";
          case TEXT -> "txt";
      };
   }

}
