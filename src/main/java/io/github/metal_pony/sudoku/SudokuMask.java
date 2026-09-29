package io.github.metal_pony.sudoku;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.metal_pony.sudoku.Constants.*;
import io.github.metal_pony.sudoku.util.ArraysUtil;
import io.github.metal_pony.sudoku.util.Counting;
import io.github.metal_pony.sudoku.util.StringsUtil;

/**
 * A fixed-sized (81) bitset used to represent a set of cells of a sudoku board.
 *
 * SudokuMask is well-suited to represent unavoidable sets. Because of the bitwise
 * implementation, provides performant operations for hitting-set searches, namely
 * via `intersects(other)`.
 */
public class SudokuMask implements Comparable<SudokuMask>, Comparator<SudokuMask> {
    /** Number of bytes used to store SudokuMask data.*/
    public static final int NUM_BYTES = 11;

    /** Cache of individual cell masks.*/
    public static final SudokuMask[] CELL_MASKS = new SudokuMask[SPACES];
    /** Cache of row masks.*/
    public static final SudokuMask[] ROW_MASKS = new SudokuMask[SPACES];
    /** Cache of column masks.*/
    public static final SudokuMask[] COL_MASKS = new SudokuMask[SPACES];
    /** Cache of region masks.*/
    public static final SudokuMask[] REGION_MASKS = new SudokuMask[SPACES];
    static {
        for (int i = 0; i < DIGITS; i++) {
            ROW_MASKS[i] = new SudokuMask();
            COL_MASKS[i] = new SudokuMask();
            REGION_MASKS[i] = new SudokuMask();
        }
        for (int ci = 0; ci < SPACES; ci++) {
            CELL_MASKS[ci] = new SudokuMask();
            CELL_MASKS[ci].setBit(ci);
            ROW_MASKS[Sudoku.cellRow(ci)].setBit(ci);
            COL_MASKS[Sudoku.cellCol(ci)].setBit(ci);
            REGION_MASKS[Sudoku.cellRegion(ci)].setBit(ci);
        }
    }

    /**
     * Exception thrown when a data structure was used but is not the proper length.
     */
    public static final class LengthException extends RuntimeException {
        /**
         * Creates a new LengthException with the default message.
         */
        LengthException() {
            super("Invalid length");
        }
    }

    /**
     * Exception thrown when a value was used outside an intended range.
     */
    public static final class RangeException extends RuntimeException {
        /**
         * Creates a new RangeException with the default message.
         */
        RangeException() {
            super("Out of range");
        }
        /**
         * Creates a new RangeException with the given value interpolated
         * into a default message.
         * @param val Integer value to interpolate.
         */
        RangeException(int val) {
            super(String.format("Out of range: %d", val));
        }
    }

    /**
     * Returns a new SudokuMask with all bits set.
     * @return A new, full SudokuMask.
     */
    public static SudokuMask full() {
        SudokuMask mask = new SudokuMask();
        mask.bitsSet = SPACES;
        mask.bytes[0] = (byte)1;
        for (int i = 1; i < NUM_BYTES; i++) {
            mask.bytes[i] = (byte)0xFF;
        }
        return mask;
    }

    /**
     * Returns a new SudokuMask with the given number of bits set at random.
     * @param bitCount Number of bits to set.
     * @return A new SudokuMask with bits randomly set.
     * @throws RangeException If bitCount is negative or greater than 81.
     */
    public static SudokuMask random(int bitCount) {
        if (bitCount < 0 || bitCount > SPACES) throw new RangeException(bitCount);
        if (bitCount <= 0) return new SudokuMask();
        if (bitCount >= SPACES) return full();
        SudokuMask mask = new SudokuMask();
        int[] arr = ArraysUtil.shuffle(ArraysUtil.range(SPACES));
        for (int i = 0; i < bitCount; i++) {
            mask.setBit(arr[i]);
        }
        return mask;
    }

    static final long BITS_1_MASK = 0x01FFFFL;

    byte[] bytes;
    int bitsSet;

    /**
     * Creates a new SudokuMask from the given sudoku string.
     * Non-digit and '0' characters translate to unset bits.
     * @param sudokuStr String data used for initialization.
     * @throws LengthException If the string length is not 81.
     */
    public SudokuMask(String sudokuStr) {
        this(sudokuStr.toCharArray());
    }

    /**
     * Creates a new SudokuMask from the given values.
     * Non-digit and '0' characters translate to unset bits.
     * @param vals Character data used for initialization.
     * @throws LengthException If the array length is not 81.
     */
    public SudokuMask(char[] vals) {
        this();
        setFromCharArr(vals);
    }

    /**
     * Creates a new blank SudokuMask.
     */
    public SudokuMask() {
        this.bytes = new byte[NUM_BYTES];
        this.bitsSet = 0;
    }

    /**
     * Creates a new SudokuMask as a copy of the one given.
     * @param other The SudokuMask to copy.
     */
    public SudokuMask(SudokuMask other) {
        this();
        System.arraycopy(other.bytes, 0, bytes, 0, 11);
        this.bitsSet = other.bitsSet;
    }

    /**
     * Creates a new SudokuMask using the bytes from the given BigInteger.
     * If <code>big</code> has more than 11 bytes, an error is thrown.
     * <code>big</code> with fewer than 11 bytes is valid, indicating that
     * the remaining bits are unset.
     * @param big BigInteger used to map the mask bits.
     */
    public SudokuMask(BigInteger big) {
        this();
        byte[] bigBytes = big.toByteArray();
        final int len = bigBytes.length;
        if (len > NUM_BYTES) {
            throw new IllegalArgumentException(
                String.format("bigint too large (%d bytes)", len)
            );
        }
        if (len == NUM_BYTES) bigBytes[0] &= (byte)1;
        int offset = NUM_BYTES - len;
        for (int i = 0; i < len; i++) {
            bytes[i + offset] = bigBytes[i];
            bitsSet += Integer.bitCount(Byte.toUnsignedInt(bigBytes[i]));
        }
    }

    /**
     * Maps the characters in the given array to this mask.
     * Nonzero digit characters will be mapped to set bits; all other characters
     * will be mapped to bits unset.
     * @param arr Character array to map data from.
     */
    private void setFromCharArr(char[] arr) {
        if (arr == null || arr.length != SPACES) throw new LengthException();
        for (int i = 0; i < SPACES; i++) {
            if (arr[i] > '0' && arr[i] <= '9') {
                this.bitsSet++;
                int bsi = (i + 7) / Byte.SIZE;
                int bi = (i + 7) % Byte.SIZE;
                this.bytes[bsi] |= (byte)(128 >>> bi);
            }
        }
    }

    /**
     * Builds and returns a byte array representation of this mask.
     * @return Byte array representing the mask.
     */
    public byte[] toByteArray() {
        byte[] result = new byte[NUM_BYTES];
        System.arraycopy(this.bytes, 0, result, 0, NUM_BYTES);
        return result;
    }

    /**
     * Gets a BigInteger representation of this SudokuMask.
     * @return New BigInteger containing this mask's data.
     */
    public BigInteger toBigInt() {
        return new BigInteger(1, toByteArray());
    }

    /**
     * Gets the number of bits set in the mask.
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
        if (bit < 0 || bit >= SPACES) throw new RangeException(bit);
        int i = (bit + 7) / Byte.SIZE;
        int rsh = (bit + 7) % Byte.SIZE;
        return (bytes[i] & (128 >>> rsh)) > 0;
    }

    /**
     * Sets the bit at the given index.
     * @param bit Index of the bit to set. Aka sudoku cell index.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask setBit(int bit) {
        if (bit < 0 || bit >= SPACES) throw new RangeException(bit);
        if (!testBit(bit)) {
            bitsSet++;
            int i = (bit + 7) / Byte.SIZE;
            int rsh = (bit + 7) % Byte.SIZE;
            bytes[i] |= (byte)(128 >>> rsh);
        }
        return this;
    }

    /**
     * Behaves like a bitwise OR. Any bits set in the given mask will be set in this one.
     * @param other The other mask to combine into this one.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask add(SudokuMask other) {
        bitsSet = 0;
        for (int i = 0; i < NUM_BYTES; i++) {
            bytes[i] |= other.bytes[i];
            bitsSet += Integer.bitCount(Byte.toUnsignedInt(bytes[i]));
        }
        return this;
    }

    /**
     * Any bits set in the given mask will be unset in this one.
     * @param other The other mask to combine into this one.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask subtract(SudokuMask other) {
        bitsSet = 0;
        for (int i = 0; i < NUM_BYTES; i++) {
            bytes[i] &= (byte)~other.bytes[i];
            bitsSet += Integer.bitCount(Byte.toUnsignedInt(bytes[i]));
        }
        return this;
    }

    /**
     * Unsets the bit at the given index.
     * @param bit Index of the bit to unset. Aka sudoku cell index.
     * @return This SudokuMask for convenience.
     */
    public SudokuMask unsetBit(int bit) {
        if (bit < 0 || bit >= SPACES) throw new RangeException(bit);
        if (testBit(bit)) {
            bitsSet--;
            int i = (bit + 7) / Byte.SIZE;
            int rsh = (bit + 7) % Byte.SIZE;
            bytes[i] ^= (byte)(128 >>> rsh);
        }
        return this;
    }

    /**
     * Flips the bit at the given index.
     * @param bit Index of the bit to flip. Aka sudoku cell index.
     * @return  This SudokuMask for convenience.
     */
    public SudokuMask flipBit(int bit) {
        if (bit < 0 || bit >= SPACES) throw new RangeException(bit);
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
        for (int i = 0; i < NUM_BYTES; i++) {
            bytes[i] = (byte)~bytes[i];
        }
        bytes[0] &= (byte)1;
        bitsSet = SPACES - bitsSet;
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
        if (bitsSet == 0 || other.bitsSet == 0) return false;
        for (int i = 0; i < NUM_BYTES; i++) {
            if ((bytes[i] & other.bytes[i]) > 0) return true;
        }
        return false;
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
        for (int i = 0; i < NUM_BYTES; i++) {
            if ((bytes[i] & other.bytes[i]) != other.bytes[i]) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return StringsUtil.padLeft(new BigInteger(1, bytes).toString(2), SPACES, '0');
    }

    /**
     * Gets the string representing this mask with 0s replaced by dots '.'.
     * @return String representation of this mask.
     */
    public String toStringDots() {
        return toString().replaceAll("0", ".");
    }

    /**
     * Applies the mask to the given sudoku grid string.
     * @param sudokuConfigStr 81-length string representing sudoku grid.
     * @return A new string containing the input's characters at the positions where
     * this mask has set bits. Everywhere else will be '.'.
     * @throws IllegalArgumentException If the input string is not the proper length.
     */
    public String applyTo(String sudokuConfigStr) {
        if (sudokuConfigStr.length() != SPACES) {
            throw new IllegalArgumentException("input string must be length 81");
        }
        StringBuilder strb = new StringBuilder();
        for (int i = 0; i < SPACES; i++) {
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
        for (int bit = 0, i = 0; bit < SPACES; bit++) {
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
        for (int bit = 0, i = 0; bit < SPACES; bit++) {
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
        if (bitsSet != _obj.bitsSet) return false;
        return Arrays.equals(bytes, _obj.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public int compareTo(SudokuMask o) {
        return Arrays.compareUnsigned(bytes, o.bytes);
    }

    @Override
    public int compare(SudokuMask o1, SudokuMask o2) {
        return o1.compareTo(o2);
    }

    private static List<SudokuMask> primeSieve = new ArrayList<>(){{
        SudokuMask[] rowMasks = new SudokuMask[DIGITS];
        SudokuMask[] colMasks = new SudokuMask[DIGITS];

        for (int i = 0; i < DIGITS; i++) {
            rowMasks[i] = new SudokuMask();
            colMasks[i] = new SudokuMask();
        }

        for (int ci = 0; ci < SPACES; ci++) {
            rowMasks[Sudoku.cellRow(ci)].setBit(ci);
            colMasks[Sudoku.cellCol(ci)].setBit(ci);
        }

        for (int k = 0; k < 3; k++) {
            add(new SudokuMask(rowMasks[3*k]).add(rowMasks[3*k + 1]));
            add(new SudokuMask(rowMasks[3*k]).add(rowMasks[3*k + 2]));
            add(new SudokuMask(rowMasks[3*k + 1]).add(rowMasks[3*k + 2]));

            add(new SudokuMask(colMasks[3*k]).add(colMasks[3*k + 1]));
            add(new SudokuMask(colMasks[3*k]).add(colMasks[3*k + 2]));
            add(new SudokuMask(colMasks[3*k + 1]).add(colMasks[3*k + 2]));
        }
    }};

    /**
     * Checks whether the given mask has at least one bit overlapping
     * with each mask in the prime sieve.
     * @param mask Mask to check.
     * @return True if the mask satisfies the prime sieve; otherwise false.
     */
    public static boolean satisfiesPrimeSieve(SudokuMask mask) {
        for (SudokuMask item : primeSieve) {
            if (!item.intersects(mask)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given mask has at least one bit overlapping
     * with each mask in the prime sieve. If a sieve mask is not satisfied,
     * returns the index of that mask. If all are satisfied, returns -1.
     * @param mask Mask to check.
     * @return -1 if the prime sieve is satisfied; otherwise the index
     * of the non-overlapping item.
     */
    public static int satisfiesPrimeSieveInt(SudokuMask mask) {
		for (int i = 0; i < primeSieve.size(); i++) {
			if (!primeSieve.get(i).intersects(mask)) {
				return i;
			}
		}
        return -1;
    }

    /**
     * Generates a random palindrome with the given number of bits set.
     * @param bitCount Total number of bits to be set in the palindrome.
     * @return A randomly generated palindrome mask.
     */
    public SudokuMask randomPalindrome(int bitCount) {
        if (bitCount < 0 || bitCount > SPACES) throw new RangeException(bitCount);
        int k = bitCount / 2;
        long nck = Counting.NChooseKLong(40, k);
        palindrome(bitCount, ThreadLocalRandom.current().nextLong(nck));
        return this;
    }

    /**
     * Replaces this mask with a generated palindrome.
     * @param bitCount Total number of bits to be set in the palindrome.
     * @param r Combinatorial index of the palindrome to generate; Must be &lt; (40 choose bitCount/2).
     */
    public void palindrome(int bitCount, long r) {
        if (bitCount < 0 || bitCount > SPACES) throw new RangeException(bitCount);

        bitsSet = 0;
        Arrays.fill(bytes, (byte)0);
        if (bitCount == 0) {
            return;
        } else if (bitCount == SPACES) {
            Arrays.fill(bytes, (byte)0xFF);
            bytes[0] = (byte)1;
            bitsSet = SPACES;
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
                setBit(bit);
                setBit(SPACES - bit - 1);
				_k--;
			} else {
				r -= _nck;
			}
		}

        if (bitCount % 2 == 1) setBit(40);
    }

    /**
     * Builds and returns a multiline string represeting this mask.
     * @return Multiline string of the mask.
     */
    public String toMedString() {
        StringBuilder strb = new StringBuilder();
        String lineSep = System.lineSeparator();
        for (int i = 0; i < SPACES; i++) {
            strb.append(testBit(i) ? '#' : '.');

            // Print pipe between region columns
            if ((((i+1)%3) == 0) && (((i+1)%9) != 0)) {
                strb.append(" | ");
            } else {
                strb.append(' ');
            }

            if (((i+1)%9) == 0) {
                strb.append(lineSep);
                if (i < 80) {
                    // Border between region rows
                    if (((((i+1)/9)%3) == 0) && (((i/9)%8) != 0)) {
                        strb.append("------+-------+------");
                        strb.append(lineSep);
                    }
                }
            }
        }

        return strb.toString();
    }

    /**
     * Counts and returns the number of cells in each area (row, column, region).
     * <ul>
     * <li><code>result[0-8]</code> correspond to the sudoku board's rows</li>
     * <li><code>result[9-17]</code> for the column counts</li>
     * <li><code>result[18-26]</code> for the region counts</li>
     * </ul>
     * @return Array containing cell counts for each area.
     */
    public int[] areaBitCounts() {
        int[] counts = new int[9 * 3];
        for (int ci = 0; ci < 81; ci++) {
            if (testBit(ci)) {
                counts[Sudoku.cellRow(ci)]++;
                counts[9 + Sudoku.cellCol(ci)]++;
                counts[18 + Sudoku.cellRegion(ci)]++;
            }
        }
        return counts;
    }
}
