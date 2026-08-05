package io.github.metal_pony.sudoku.util;

/**
 * Additional String utilities.
 */
public class StringsUtil {
    /**
     * Prepends the given string with the fillChar to meet a length requirement.
     * @param str String to prepend.
     * @param length Desired string length.
     * @param fillChar Character used to pad.
     * @return A new string with the desired length, padded with the given fill character;
     * if str length was already >= desired length, returns str.
     */
    public static String padLeft(String str, int length, char fillChar) {
        return (
            (length - str.length() > 0) ?
            Character.toString(fillChar).repeat(length - str.length()) + str :
            str
        );
    }

    /**
     * Reverses the given string.
     * @param str The string to reverse.
     * @return A new string of the reversed content.
     */
    public static String reverse(String str) {
        return new String(ArraysUtil.reverse(str.toCharArray()));
    }
}
