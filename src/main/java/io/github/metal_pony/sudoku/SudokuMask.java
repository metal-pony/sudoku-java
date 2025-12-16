package io.github.metal_pony.sudoku;

import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import io.github.metal_pony.sudoku.util.ArraysUtil;
import io.github.metal_pony.sudoku.util.Counting;
import io.github.metal_pony.sudoku.util.StringsUtil;

/**
 * Represents a sudoku mask containing 81 bits.
 *
 * The mask is intended to be applied to a sudoku grid to create a
 * partially-filled grid or puzzle, through <code>sudokuGrid.filter(mask)</code>.
 * The bits set in the mask are associated with cells to carry over from the grid
 * to the resulting puzzle.
 */
public class SudokuMask implements Comparable<SudokuMask>, Comparator<SudokuMask> {
    static final int N = 81;
    static final SudokuMask[] CELL_MASKS = new SudokuMask[N];
    static {
        for (int ci = 0; ci < N; ci++) {
            CELL_MASKS[ci] = new SudokuMask();
            CELL_MASKS[ci].setBit(ci);
        }
    }

    public static final class LengthException extends RuntimeException {
        LengthException() {
            super("Invalid length");
        }
    }

    public static final class RangeException extends RuntimeException {
        RangeException() {
            super("Out of range");
        }
        RangeException(int val) {
            super(String.format("Out of range: %d", val));
        }
    }

    /**
     * Returns a new SudokuMask with all bits set.
     */
    public static SudokuMask full() {
        SudokuMask mask = new SudokuMask();
        mask.bitsSet = N;
        mask.bits[1] = 0x1FFFFL;
        mask.bits[0] = 0xFFFFFFFFFFFFFFFFL;
        return mask;
    }

    /**
     * Returns a new SudokuMask with the given number of bits set at random.
     * @param bitCount Number of bits to set.
     * @throws RangeException If bitCount is negative or greater than 81.
     */
    public static SudokuMask random(int bitCount) {
        if (bitCount < 0 || bitCount > N) throw new RangeException(bitCount);
        if (bitCount <= 0) return new SudokuMask();
        if (bitCount >= N) return full();
        SudokuMask mask = new SudokuMask();
        int[] arr = ArraysUtil.shuffle(range(new int[N]));
        for (int i = 0; i < bitCount; i++) {
            mask.setBit(arr[i]);
        }
        return mask;
    }

    private static int[] range(int[] arr) {
        for (int i = 0; i < arr.length; i++) arr[i] = i;
        return arr;
    }

    // I apologize to myself for future me's confusion.
    //
    // cell indices   0  1  2 .. 15 16  17 18 .. 40 .. 78 79 80
    // bits[1]      [16 15 14 ..  1  0]
    // bits[0]                         [63 62 .. 40 ..  2  1  0]
    long[] bits;
    int bitsSet;

    /**
     * Creates a new SudokuMask from the given sudoku string.
     * Non-digit and '0' characters translate to unset bits.
     * @param sudokuStr
     * @throws LengthException If the string length is not 81.
     */
    public SudokuMask(String sudokuStr) {
        this(sudokuStr.toCharArray());
    }

    /**
     * Creates a new SudokuMask from the given values.
     * Non-digit and '0' characters translate to unset bits.
     * @param vals
     * @throws LengthException If the array length is not 81.
     */
    public SudokuMask(char[] vals) {
        if (vals == null || vals.length != N) throw new LengthException();
        this.bits = new long[]{0L, 0L};
        this.bitsSet = 0;
        setFromCharArr(vals);
    }

    /**
     * Creates a new SudokuMask where all bits are unset.
     */
    public SudokuMask() {
        this.bits = new long[]{0L, 0L};
        this.bitsSet = 0;
    }

    /**
     * Creates a new SudokuMask as a copy of the one given.
     * @param other The SudokuMask to copy.
     */
    public SudokuMask(SudokuMask other) {
        this.bits = new long[]{other.bits[0], other.bits[1]};
        this.bitsSet = other.bitsSet;
    }

    private void setFromCharArr(char[] arr) {
        for (int i = 0; i < N; i++) {
            if (arr[i] > '0' && arr[i] <= '9') {
                this.bitsSet++;
                int bsi = i > 16 ? 0 : 1;
                int bi = (80 - i) % 64;
                this.bits[bsi] |= (1L<<bi);
            }
        }
    }

    /**
     * @return The number of bits set.
     */
    public int bitCount() {
        return bitsSet;
    }

    /**
     * Gets whether the given bit is set in the mask.
     * @param bit Index of the bit to check. Aka sudoku cell index.
     * @return True if the bit associated with the sudoku cell is set; otherwise false.
     */
    public boolean testBit(int bit) {
        if (bit < 0 || bit >= N) throw new RangeException(bit);
        long bsi = bit > 16 ? bits[0] : bits[1];
        int bi = (80 - bit) % 64;
        return ((bsi >>> bi) & 1L) == 1L;
    }

    /**
     * Sets the bit at the given index.
     * @param bit Index of the bit to set. Aka sudoku cell index.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask setBit(int bit) {
        if (bit < 0 || bit >= N) throw new RangeException(bit);
        if (!testBit(bit)) {
            bitsSet++;
            int bsi = (bit > 16) ? 0 : 1;
            int bi = (80 - bit) % 64;
            bits[bsi] |= (1L<<bi);
        }
        return this;
    }

    /**
     * Behaves like a bitwise OR. Any bits set in the given mask will be set in this one.
     * @param other The other mask to combine into this one.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask add(SudokuMask other) {
        for (int b = 0; b < N; b++) {
            if (other.testBit(b) && !testBit(b)) {
                bitsSet++;
                int bsi = (b > 16) ? 0 : 1;
                int bi = (80 - b) % 64;
                bits[bsi] |= (1L<<bi);
            }
        }
        return this;
    }

    /**
     * Unsets the bit at the given index.
     * @param bit Index of the bit to unset. Aka sudoku cell index.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask unsetBit(int bit) {
        if (bit < 0 || bit >= N) throw new RangeException(bit);
        if (testBit(bit)) {
            bitsSet--;
            int bsi = (bit > 16) ? 0 : 1;
            int bi = (80 - bit) % 64;
            bits[bsi] ^= (1L<<bi);
        }
        return this;
    }

    /**
     * Flips the bit at the given index.
     * @param bit Index of the bit to flip. Aka sudoku cell index.
     * @return  This SudokuMask for convenience.
     */
    public SudokuMask flipBit(int bit) {
        if (bit < 0 || bit >= N) throw new RangeException(bit);
        if (testBit(bit)) {
            unsetBit(bit);
        } else {
            setBit(bit);
        }
        return this;
    }

    /**
     * Flips all bits.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask flip() {
        bits[1] = ((~bits[1]) & 0x1FFFFL);
        bits[0] = ~bits[0];
        bitsSet = N - bitsSet;
        return this;
    }

    // caveat: false if either are empty
    /**
     * Checks whether this mask and the given mask have any set bits in common.
     *
     * If either have no bits set, this returns false.
     * @param other The other SudokuMask to compare bits.
     * @return True if this and `other` have any set bits in common; otherwise false.
     */
    public boolean intersects(SudokuMask other) {
        if (other == null) return false;
        return ((bits[0] & other.bits[0]) | (bits[1] & other.bits[1])) != 0L;
    }

    /**
     * Checks whether this mask has all the set bits of the given mask.
     *
     * If either have no bits set, this returns false.
     * @param other The other SudokuMask to compare bits.
     * @return True if this has all the set bits of `other`; otherwise false.
     */
    public boolean hasBitsSet(SudokuMask other) {
        if (other == null) return false;
        if (bitsSet == 0 || other.bitsSet == 0) return false;
        return (
            (bits[0] & other.bits[0]) == other.bits[0] &&
            (bits[1] & other.bits[1]) == other.bits[1]
        );
    }

    @Override
    public String toString() {
        return (
            StringsUtil.padLeft(Long.toBinaryString(bits[1]), 17, '0') +
            StringsUtil.padLeft(Long.toBinaryString(bits[0]), 64, '0')
        );
    }

    /**
     * Gets the string representing this mask with 0s replaced by dots '.'.
     */
    public String toStringDots() {
        return toString().replaceAll("0", ".");
    }

    /**
     * A hexadecimal representation of this mask.
     */
    public String toHexString() {
        String first = Long.toHexString(bits[1]);
        boolean usePad = !("0".equals(first));
        return String.format(
            "%s%s",
            "0".equals(first) ? "" : first,
            (usePad ?
                StringsUtil.padLeft(Long.toHexString(bits[0]), 16, '0') :
                Long.toHexString(bits[0]))
        );
    }

    /**
     * Parses a hexadecimal mask string into a SudokuMask.
     * The string should not contain the '0x' prefix.
     * Only the first 21 characters of the hex string will be used.
     * @param maskHexStr Hexadecimal mask string.
     * @returns A new SudokuMask.
     * @throws RangeException If the resulting mask string represents bits
     * outside of the mask space.
     */
    public static SudokuMask parseHexString(String maskHexStr) {
        // Ensure the input is 21 characters.
        maskHexStr = StringsUtil.padLeft(maskHexStr, 21, '0').substring(0, 21);
        SudokuMask mask = new SudokuMask();
        long bits0 = Long.parseUnsignedLong(maskHexStr.substring(maskHexStr.length() - 16), 16);
        long bits1 = Long.parseUnsignedLong(maskHexStr.substring(0, maskHexStr.length() - 16), 16);
        int bit = N - 64 - 1;
        while (bits1 != 0L) {
            if ((bits1 & 1L) == 1L) {
                // error if mask str was too big
                mask.setBit(bit);
            }
            bits1 >>>= 1;
            bit--;
        }
        bit = N - 1;
        while (bits0 != 0L) {
            if ((bits0 & 1L) == 1L) {
                mask.setBit(bit);
            }
            bits0 >>>= 1;
            bit--;
        }
        return mask;
    }

    /**
     * Applies the mask to the given sudoku grid string.
     * @param sudokuConfigStr 81-length string representing sudoku grid.
     * @return A new string containing the input's characters at the positions where
     * this mask has set bits. Everywhere else will be '.'.
     * @throws IllegalArgumentException If the input string is not the proper length.
     */
    public String applyTo(String sudokuConfigStr) {
        if (sudokuConfigStr.length() != N) {
            throw new IllegalArgumentException("input string must be length 81");
        }
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < N; i++) {
            strb.append(testBit(i) ? sudokuConfigStr.charAt(i) : '.');
        }
        return strb.toString();
    }

    /**
     * Converts this mask to an array of indices where the bits are set.
     * @return An array of indices corresponding to the set bits in this mask.
     */
    public int[] toIndices() {
        int[] result = new int[bitsSet];
        for (int bit = 0, i = 0; bit < N; bit++) {
            if (testBit(bit)) {
                result[i++] = bit;
            }
        }
        return result;
    }

    /**
     * Splits this mask into an array of SudokuMask components, each containing a single bit set.
     * @return An array of SudokuMask components, each with one bit set.
     */
    public SudokuMask[] split() {
        SudokuMask[] components = new SudokuMask[bitsSet];
        for (int bit = 0, i = 0; bit < N; bit++) {
            if (testBit(bit)) {
                components[i++] = new SudokuMask().setBit(bit);
            }
        }
        return components;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SudokuMask)) return false;
        if (this == obj) return true;
        SudokuMask _obj = (SudokuMask) obj;
        return (bits[0] == _obj.bits[0] && bits[1] == _obj.bits[1]);
    }

    @Override
    public int hashCode() {
        long key = bits[0];
        key = (key ^ (key >>> 32)) * 0x111111111111111L;
        key = (key ^ (key >>> 32)) * 0x111111111111111L;
        key = (key ^ (key >>> 32));
        long key1 = bits[1] * 0x111111111111111L;
        return ((int)key) ^ ((int)key1);
    }

    @Override
    public int compareTo(SudokuMask o) {
        int compare = Long.compareUnsigned(bits[1], o.bits[1]);
        if (compare == 0) {
            return Long.compareUnsigned(bits[0], o.bits[0]);
        }
        return compare;
    }

    @Override
    public int compare(SudokuMask o1, SudokuMask o2) {
        return o1.compareTo(o2);
    }

    public void randomPalindrome(int bitCount) {
        if (bitCount < 0 || bitCount > N) throw new RangeException(bitCount);
        int k = bitCount / 2;
        long nck = Counting.NChooseKLong(40, k);
        long r = ThreadLocalRandom.current().nextLong(nck);
        palindrome(bitCount, r);
    }

    public void palindrome(int bitCount, long r) {
        if (bitCount < 0 || bitCount > N) throw new RangeException(bitCount);

        bitsSet = bitCount;
        bits[0] = 0L;
        bits[1] = 0L;
        if (bitCount == 0) {
            return;
        } else if (bitCount == N) {
            bits[1] = 0x1FFFFL;
            bits[0] = 0xFFFFFFFFFFFFFFFFL;
            return;
        }

        int n = 40;
        int k = bitCount / 2;
        long nck = Counting.NChooseKLong(40, k);

        if (r >= nck) throw new IllegalArgumentException(String.format("r too large. Max %d", nck));

        int bit = 0;
		for (int _n = n - 1, _k = k - 1; _k >= 0; _n--, bit++) {
			long _nck = Counting.NChooseKLong(_n, _k);
			if (r < _nck) {
                bits[0] |= 1L << bit;
                if (bit > 16) {
                    bits[0] |= 1L << (80 - bit);
                } else {
                    bits[1] |= 1L << (16 - bit);
                }
				_k--;
			} else {
				r -= _nck;
			}
		}

        if (bitCount % 2 == 1) bits[0] |= (1L << 40);
    }
}
