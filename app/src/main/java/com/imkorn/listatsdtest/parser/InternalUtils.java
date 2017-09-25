package com.imkorn.listatsdtest.parser;

import java.util.Locale;

/**
 * Created by imkorn on 22.09.17.
 */

public class InternalUtils {
    public static void assertTagName(String name) {
        if (name != null &&
            !name.isEmpty() &&
            !Character.isDigit(name.charAt(0)) &&
                withoutSpaces(name)) {
            return;
        }


        throw new IllegalArgumentException(String.format(Locale.US,
                                                         "Wrong tag name [%s]",
                                                         name));
    }

    private static boolean withoutSpaces(String name) {
        for (int index = 0; index < name.length(); index++) {
            final char character = name.charAt(index);
            if (character == ' ' ||
                character == '\n'||
                character == '\t' ) {
                return false;
            }
        }
        return true;
    }
}
