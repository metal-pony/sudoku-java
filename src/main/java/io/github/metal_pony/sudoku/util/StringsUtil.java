package io.github.metal_pony.sudoku.util;

public class StringsUtil {
    public static String padLeft(String str, int length, char fillChar) {
        return (length - str.length() > 0) ?
            Character.toString(fillChar).repeat(length - str.length()) + str :
            str;
    }
}
