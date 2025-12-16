package io.github.metal_pony.sudoku.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestStringsUtil {

    @Test
    void padLeft_whenNull_throws() {
        assertThrows(NullPointerException.class, () -> {
            StringsUtil.padLeft(null, 0, '0');
        });
    }

    @Test
    void padLeft() {
        assertEquals("", StringsUtil.padLeft("", 0, '0'));
        assertEquals("a", StringsUtil.padLeft("a", 0, 'x'));
        assertEquals("a", StringsUtil.padLeft("a", 1, 'x'));
        assertEquals("xa", StringsUtil.padLeft("a", 2, 'x'));
        assertEquals("0", StringsUtil.padLeft("", 1, '0'));
        assertEquals("             bananas", StringsUtil.padLeft("bananas", 20, ' '));
        assertEquals("\\\\\\\\meow", StringsUtil.padLeft("meow", 8, '\\'));
        assertEquals("xxxxxxxxpow", StringsUtil.padLeft("pow", 11, 'x'));
    }
}
