package com.glisterbyte.singingmonsters.common;

import okio.ByteString;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static int countOccurrences(String str, char target) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    public static String format(String s, Object... args) {
        return MessageFormatter.arrayFormat(s, args).getMessage();
    }

    public static String formatFriendCode(String friendCode) {
        friendCode = friendCode.toUpperCase();
        if (!friendCode.contains("-")) {
            List<String> friendCodeParts = new ArrayList<>();
            for (int i = 0; i < friendCode.length(); i += 4) {
                friendCodeParts.add(
                        friendCode.substring(
                                i,
                                Math.min(i + 4, friendCode.length())
                        )
                );
            }
            friendCode = String.join("-", friendCodeParts).toUpperCase();
        }
        return friendCode;
    }

    public static String formatByteStringDump(ByteString bytes) {

        final int rowLength = 16;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < bytes.size(); i += rowLength) {

            builder.append(String.format("%08X  ", i));

            for (int j = 0; j < rowLength; j++) {
                if (i + j < bytes.size()) {
                    builder.append(String.format("%02X ", bytes.getByte(i + j)));
                } else {
                    builder.append("   ");
                }
                if (j == 7) builder.append(" ");
            }

            builder.append(" ");

            for (int j = 0; j < rowLength && i + j < bytes.size(); j++) {
                byte b = bytes.getByte(i + j);
                builder.append(b >= 32 && b < 127 ? (char) b : '.');
            }

            builder.append("\n");
        }

        return builder.toString();

    }

}