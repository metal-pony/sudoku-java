package io.github.metal_pony.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.metal_pony.sudoku.util.ArraysUtil;
import io.github.metal_pony.sudoku.util.Counting;

/**
 * A Sudoku instance manages the state of a standard 9x9 sudoku board,
 * and provides methods for validation, manipulation, and finding solutions.
 * There are also static utilities for generation of full sudoku and puzzles.
 *
 * <br><br>Want to contribute?
 * <br><br>GitHub Repo: <a href="https://github.com/metal-pony/sudoku-java">sudoku-java</a>
 *
 * @author Jeff Gibson, github.com/metal-pony
 */
public class Sudoku {
    public static final int RANK = 3;
    /** Number of digits in standard 9x9 sudoku.*/
    public static final int DIGITS = 9; // rank^2
    /** Number of spaces or cells on a standard sudoku board.*/
    public static final int SPACES = 81; // rank^2^2
    /** Represents the combination of all candidates for a cell (0x1ff).*/
    public static final int ALL = 511; // 2^rank^2 - 1
    /** Minumum number of clues required for a valid sudoku puzzle.*/
    public static final int MIN_CLUES = 17; // rank^2 * 2 - 1

    /*
     * Masks for working with 27-bit constraints values.
     */
    /** Leftmost 9 bits indicate which digits have been used within a row.*/
    static final int ROW_MASK = ALL << (DIGITS * 2);
    /** Middle 9 bits indicate which digits have been used within a column.*/
    static final int COL_MASK = ALL << DIGITS;
    /** Rightmost 9 bits indicate which digits have been used within a region.*/
    static final int REGION_MASK = ALL;
    /**
     * Combination of row, column, region constraints masks (0x7ffffff).
     * When a cell's constraints equals this value, then that cell's row,
     * column, and region are valid and full. If all constraints are this value,
     * then the grid is full and valid (solved).
     */
    static final int FULL_CONSTRAINTS = ROW_MASK | COL_MASK | REGION_MASK;

    /** Maps digits (as the index) to a 9-bit encoded values.*/
    static final int[] ENCODER = new int[] { 0, 1, 2, 4, 8, 16, 32, 64, 128, 256 };
    /**
     * Maps the 9-bit encoded values (as the index) to the associated digit.
     * NOTE: Values that represent more than one digit are mapped to 0.
     * Powers of 2 map to digits 1 through 9.
     */
    static final int[] DECODER = new int[1<<DIGITS];
    static {
        for (int d = 1; d <= DIGITS; d++) DECODER[1 << (d - 1)] = d;
    }

    /** Maps encoded values to the array of individual digits it represents.*/
    static final int[][] CANDIDATES = new int[1<<DIGITS][];

    /** Maps encoded values to the array of individual digits (encoded) it represents.*/
    static final int[][] CANDIDATES_ENC = new int[CANDIDATES.length][];
    static {
        for (int encoded = 0; encoded < CANDIDATES.length; encoded++) {
            CANDIDATES[encoded] = new int[Integer.bitCount(encoded)];
            CANDIDATES_ENC[encoded] = new int[Integer.bitCount(encoded)];
            int _val = encoded;
            int i = 0;
            int j = 0;
            int digit = 1;
            while (_val > 0) {
                if ((_val & 1) > 0) {
                    CANDIDATES[encoded][i++] = digit;
                    CANDIDATES_ENC[encoded][j++] = ENCODER[digit];
                }
                _val >>= 1;
                digit++;
            }
        }
    }

    /** Maps indices [0, 511] to its bit count.*/
    static final int[] BIT_COUNT_MAP = new int[1<<DIGITS];

    /** Digit combinations indexed by bit count (aka digit count).*/
    static final int[][] DIGIT_COMBOS_MAP = new int[DIGITS + 1][];
    static {
        for (int nDigits = 0; nDigits < DIGIT_COMBOS_MAP.length; nDigits++) {
            DIGIT_COMBOS_MAP[nDigits] = new int[Counting.nChooseK(DIGITS, nDigits).intValueExact()];
        }
        int[] combosCount = new int[DIGITS + 1];
        for (int i = 0; i < BIT_COUNT_MAP.length; i++) {
            int bits = Integer.bitCount(i);
            BIT_COUNT_MAP[i] = bits;
            DIGIT_COMBOS_MAP[bits][combosCount[bits]++] = i;
        }
    }

    /** Gets the 9-bit encoded value of the given digit.*/
    public static int encode(int digit) {
        return ENCODER[digit];
    }

    /**
     * Gets the digit associated with the given 9-bit encoded value.
     * If the encoded value represents more than one digit, returns 0.
     */
    public static int decode(int encoded) {
        return DECODER[encoded];
    }

    /**
     * Gets whether the encoded value represents a single digit.
     * @param encoded 9-bit encoded value.
     * @return True if the value represents a single digit; otherwise false.
     */
    public static boolean isDigit(int encoded) {
        return DECODER[encoded] > 0;
    }

    /** Returns the row for the given cell by index.*/
    public static int cellRow(int cellIndex) {
        // return cellIndex / DIGITS;
        return CELL_ROWS[cellIndex];
    }

    /** Returns the column for the given cell by index.*/
    public static int cellCol(int cellIndex) {
        // return cellIndex % DIGITS;
        return CELL_COLS[cellIndex];
    }

    /** Returns the region for the given cell by index.*/
    public static int cellRegion(int cellIndex) {
        // int regionRow = cellIndex / (RANK * DIGITS);
        // int regionCol = (cellIndex % DIGITS) / RANK;
        // return (regionRow * RANK) + regionCol;
        return CELL_REGIONS[cellIndex];
    }

    /** Maps cell indices to respective rows.*/
    private static final int[] CELL_ROWS = new int[]{
        0, 0, 0,  0, 0, 0,  0, 0, 0,
        1, 1, 1,  1, 1, 1,  1, 1, 1,
        2, 2, 2,  2, 2, 2,  2, 2, 2,

        3, 3, 3,  3, 3, 3,  3, 3, 3,
        4, 4, 4,  4, 4, 4,  4, 4, 4,
        5, 5, 5,  5, 5, 5,  5, 5, 5,

        6, 6, 6,  6, 6, 6,  6, 6, 6,
        7, 7, 7,  7, 7, 7,  7, 7, 7,
        8, 8, 8,  8, 8, 8,  8, 8, 8
    };
    /** Maps cell indices to respective columns.*/
    private static final int[] CELL_COLS = new int[]{
        0, 1, 2,  3, 4, 5,  6, 7, 8,
        0, 1, 2,  3, 4, 5,  6, 7, 8,
        0, 1, 2,  3, 4, 5,  6, 7, 8,

        0, 1, 2,  3, 4, 5,  6, 7, 8,
        0, 1, 2,  3, 4, 5,  6, 7, 8,
        0, 1, 2,  3, 4, 5,  6, 7, 8,

        0, 1, 2,  3, 4, 5,  6, 7, 8,
        0, 1, 2,  3, 4, 5,  6, 7, 8,
        0, 1, 2,  3, 4, 5,  6, 7, 8
    };
    /** Maps cell indices to respective regions.*/
    private static final int[] CELL_REGIONS = new int[]{
        0, 0, 0,  1, 1, 1,  2, 2, 2,
        0, 0, 0,  1, 1, 1,  2, 2, 2,
        0, 0, 0,  1, 1, 1,  2, 2, 2,

        3, 3, 3,  4, 4, 4,  5, 5, 5,
        3, 3, 3,  4, 4, 4,  5, 5, 5,
        3, 3, 3,  4, 4, 4,  5, 5, 5,

        6, 6, 6,  7, 7, 7,  8, 8, 8,
        6, 6, 6,  7, 7, 7,  8, 8, 8,
        6, 6, 6,  7, 7, 7,  8, 8, 8
    };
    /** Maps row indices to an array of cell indices (cells in the given row).*/
    static final int[][] ROW_INDICES = new int[DIGITS][DIGITS];
    /** Maps column indices to an array of cell indices (cells in the given column).*/
    static final int[][] COL_INDICES = new int[DIGITS][DIGITS];
    /** Maps region indices to an array of cell indices (cells in the given region).*/
    static final int[][] REGION_INDICES = new int[DIGITS][DIGITS];
    /** Maps band indices to an array of cell indices (cells in the given band).*/
    static final int[][] BAND_INDICES = new int[3][3*DIGITS];
    /** Maps stack indices to an array of cell indices (cells in the given stack).*/
    static final int[][] STACK_INDICES = new int[3][3*DIGITS];
    /**
     * Maps row indices withing a band to an array of cell indices (cells in the given band's row).
     * <br><br><code>BAND_ROW_INDICES[band 0-2][row 0-2] = [... cell indices]</code>
     */
    static final int[][][] BAND_ROW_INDICES = new int[3][3][DIGITS];
    /**
     * Maps column indices withing a stack to an array of cell indices (cells in the given stack's column).
     * <br><br><code>STACK_COL_INDICES[stack 0-2][col 0-2] = [... cell indices]</code>
     */
    static final int[][][] STACK_COL_INDICES = new int[3][3][DIGITS];
    static {
        int[] rowi = new int[DIGITS];
        int[] coli = new int[DIGITS];
        int[] regi = new int[DIGITS];
        for (int i = 0; i < SPACES; i++) {
            int row = cellRow(i);
            int col = cellCol(i);
            int region = cellRegion(i);

            ROW_INDICES[row][rowi[row]++] = i;
            COL_INDICES[col][coli[col]++] = i;
            REGION_INDICES[region][regi[region]++] = i;

            int band = row / RANK;
            int rowInBand = row % RANK;
            int stack = col / RANK;
            int colInStack = col % RANK;
            int indexInBand = i % (DIGITS * RANK);
            int indexInStack = (row * RANK) + colInStack;
            BAND_INDICES[band][indexInBand] = i;
            STACK_INDICES[stack][indexInStack] = i;
            BAND_ROW_INDICES[band][rowInBand][col] = i;
            STACK_COL_INDICES[stack][colInStack][row] = i;
        }
    }
    /** Maps cells indices to the other cell indices within the same row.*/
    static int[][] ROW_NEIGHBORS = new int[SPACES][DIGITS - 1];
    /** Maps cells indices to the other cell indices within the same column.*/
    static int[][] COL_NEIGHBORS = new int[SPACES][DIGITS - 1];
    /** Maps cells indices to the other cell indices within the same region.*/
    static int[][] REGION_NEIGHBORS = new int[SPACES][DIGITS - 1];
    /** Maps cells indices to all other cell indices within the same row, column, and region.*/
    static int[][] CELL_NEIGHBORS = new int[SPACES][3*(DIGITS-1) - (DIGITS-1)/2];
    static {
        for (int i = 0; i < SPACES; i++) {
            int row = cellRow(i);
            int col = cellCol(i);
            int region = cellRegion(i);

            int ri = 0;
            int coli = 0;
            int regi = 0;
            int ni = 0;

            for (int j = 0; j < SPACES; j++) {
                if (i == j) continue;
                int jrow = cellRow(j);
                int jcol = cellCol(j);
                int jregion = cellRegion(j);

                if (jrow == row) {
                    ROW_NEIGHBORS[i][ri++] = j;
                }
                if (jcol == col) {
                    COL_NEIGHBORS[i][coli++] = j;
                }
                if (jregion == region) {
                    REGION_NEIGHBORS[i][regi++] = j;
                }
                if (jrow == row || jcol == col || jregion == region) {
                    CELL_NEIGHBORS[i][ni++] = j;
                }
            }
        }
    }

    /**
     * Checks whether the area of the given sudoku board is valid.
     * @param digits Sudoku board digits.
     * @param areaIndices Indices making up the row, column, or region.
     * @return True if the area is valid, i.e., contains no repeated digits; otherwise false.
     */
    private static boolean isAreaValid(int[] digits, int[] areaIndices) {
        if (digits.length != SPACES) return false;
        int digitsSeen = 0;
        for (int i = 0; i < DIGITS; i++) {
            int digit = digits[areaIndices[i]];
            if (digit < 0 || digit > DIGITS) return false;
            if (digit > 0) {
                int digitMask = 1 << (digit - 1);
                if ((digitMask & digitsSeen) > 0) return false;
                digitsSeen |= digitMask;
            }
        }
        return true;
    }

    /**
     * Checks whether the area of the given sudoku board is full with digits.
     * Does not check whether there are repeated digits.
     * @param digits Sudoku board digits.
     * @param areaIndices Indices making up the row, column, or region.
     * @return True if the area is full; otherwise false.
     */
    private static boolean isAreaFull(int[] digits, int[] areaIndices) {
        if (digits.length != SPACES) return false;
        for (int i = 0; i < DIGITS; i++) {
            int digit = digits[areaIndices[i]];
            if (digit <= 0 || digit > DIGITS) return false;
        }
        return true;
    }

    /**
     * Checks whether the given row is valid.
     * @param digits Sudoku board digits.
     * @param rowIndex Row to check.
     * @return True if the row is valid, i.e., contains no repeated digits; otherwise false.
     */
    public static boolean isRowValid(int[] digits, int rowIndex) {
        return isAreaValid(digits, ROW_INDICES[rowIndex]);
    }

    /**
     * Checks whether the given column is valid.
     * @param digits Sudoku board digits.
     * @param colIndex Column to check.
     * @return True if the column is valid, i.e., contains no repeated digits; otherwise false.
     */
    public static boolean isColValid(int[] digits, int colIndex) {
        return isAreaValid(digits, COL_INDICES[colIndex]);
    }

    /**
     * Checks whether the given region is valid.
     * @param digits Sudoku board digits.
     * @param regionIndex Region to check.
     * @return True if the region is valid, i.e., contains no repeated digits; otherwise false.
     */
    public static boolean isRegionValid(int[] digits, int regionIndex) {
        return isAreaValid(digits, REGION_INDICES[regionIndex]);
    }

    /**
     * Checks whether the given row is full of digits.
     * Does not check whether there are repeated digits.
     * @param digits Sudoku board digits.
     * @param rowIndex Row to check.
     * @return True if the row is full; otherwise false.
     */
    public static boolean isRowFull(int[] digits, int rowIndex) {
        return isAreaFull(digits, ROW_INDICES[rowIndex]);
    }

    /**
     * Checks whether the given column is full of digits.
     * Does not check whether there are repeated digits.
     * @param digits Sudoku board digits.
     * @param colIndex Column to check.
     * @return True if the column is full; otherwise false.
     */
    public static boolean isColFull(int[] digits, int colIndex) {
        return isAreaFull(digits, COL_INDICES[colIndex]);
    }

    /**
     * Checks whether the given region is full of digits.
     * Does not check whether there are repeated digits.
     * @param digits Sudoku board digits.
     * @param regionIndex Region to check.
     * @return True if the region is full; otherwise false.
     */
    public static boolean isRegionFull(int[] digits, int regionIndex) {
        return isAreaFull(digits, REGION_INDICES[regionIndex]);
    }

    /**
     * Checks whether the given sudoku board is valid.
     * @param digits Sudoku board digits.
     * @return True if the sudoku board is valid, i.e., contains no repeated digits
     * in any row, column, or region; otherwise false.
     */
    public static boolean isValid(int[] digits) {
        if (digits.length != SPACES) return false;

        int[] rowValidity = new int[DIGITS];
        int[] colValidity = new int[DIGITS];
        int[] regionValidity = new int[DIGITS];

        for (int ci = 0; ci < SPACES; ci++) {
            int digit = digits[ci];
            if (digit < 0 || digit > DIGITS) return false;
            if (digit == 0) continue;

            int row = CELL_ROWS[ci];
            int col = CELL_COLS[ci];
            int region = CELL_REGIONS[ci];
            int digitMask = 1 << (digit - 1);
            if (
                (digitMask & rowValidity[row]) > 0 ||
                (digitMask & colValidity[col]) > 0 ||
                (digitMask & regionValidity[region]) > 0
            ) {
                return false;
            }
            rowValidity[row] |= digitMask;
            colValidity[col] |= digitMask;
            regionValidity[region] |= digitMask;
        }

        return true;
    }

    /**
     * Checks whether the given sudoku board is full.
     * Does not check whether there are repeated digits.
     * @param digits Sudoku board digits.
     * @return True if the sudoku board is full of digits; otherwise false.
     */
    public static boolean isFull(int[] digits) {
        if (digits.length != SPACES) return false;
        for (int ci = 0; ci < DIGITS; ci++) {
            if (digits[ci] <= 0 || digits[ci] > DIGITS) return false;
        }
        return true;
    }

    /**
     * Checks whether the given sudoku board is solved (i.e., full and valid).
     * @param digits Sudoku board digits.
     * @return True if the sudoku board is solved; otherwise false.
     */
    public static boolean isSolved(int[] digits) {
        if (digits.length != SPACES) return false;

        int[] rowValidity = new int[DIGITS];
        int[] colValidity = new int[DIGITS];
        int[] regionValidity = new int[DIGITS];

        for (int ci = 0; ci < SPACES; ci++) {
            int digit = digits[ci];
            if (digit <= 0 || digit > DIGITS) return false;

            int row = CELL_ROWS[ci];
            int col = CELL_COLS[ci];
            int region = CELL_REGIONS[ci];
            int digitMask = 1 << (digit - 1);
            if (
                (digitMask & rowValidity[row]) > 0 ||
                (digitMask & colValidity[col]) > 0 ||
                (digitMask & regionValidity[region]) > 0
            ) {
                return false;
            }
            rowValidity[row] |= digitMask;
            colValidity[col] |= digitMask;
            regionValidity[region] |= digitMask;
        }

        return true;
    }

    /**
     * Rotates a given square matrix array 90 degrees clockwise.
     * @param arr The NxN matrix to rotate, as a single array.
     * @param n Length of one of the sides.
     * @return The mutated array.
     * @throws IllegalArgumentException if the array length is not n^2.
     */
    public static int[] rotate90(int[] arr, int n) {
        if (arr == null) throw new NullPointerException();
        if (n < 0) throw new IllegalArgumentException("n must be nonnegative");
        if (arr.length != n * n) throw new IllegalArgumentException("arr length not n square");
        for (int layer = 0; layer < n / 2; layer++) {
            int first = layer;
            int last = n - 1 - layer;
            for (int i = first; i < last; i++) {
                int offset = i - first;
                int top = arr[first * n + i];
                arr[first * n + i] = arr[(last - offset) * n + first];
                arr[(last - offset) * n + first] = arr[last * n + (last - offset)];
                arr[last * n + (last - offset)] = arr[i * n + last];
                arr[i * n + last] = top;
            }
        }
        return arr;
    }

    /**
     * Reflects a (rows x N) matrix over the horizontal axis.
     * @param arr The matrix to reflect.
     * @param rows The number of rows in the matrix.
     * @return The mutated array.
     * @throws IllegalArgumentException if (array length / rows) is not a whole number.
     */
    public static int[] reflectOverHorizontal(int[] arr, int rows) {
        if (arr == null) throw new NullPointerException();
        if (rows <= 0) throw new IllegalArgumentException("rows must be positive");
        if (arr.length % rows != 0) throw new IllegalArgumentException("array length must be divisible by number of rows");
        int cols = arr.length / rows;
        for (int r = 0; r < (rows / 2); r++) {
            for (int c = 0; c < cols; c++) {
                int ai = r * cols + c;
                int bi = (rows - r - 1) * cols + c;
                arr[ai] ^= arr[bi];
                arr[bi] ^= arr[ai];
                arr[ai] ^= arr[bi];
            }
        }
        return arr;
    }

    /**
     * Reflects a (rows x N) matrix over the vertical axis.
     * @param arr The matrix to reflect.
     * @param rows The number of rows in the matrix.
     * @return The mutated array.
     * @throws IllegalArgumentException if (array length / rows) is not a whole number.
     */
    public static int[] reflectOverVertical(int[] arr, int rows) {
        if (arr == null) throw new NullPointerException();
        if (rows <= 0) throw new IllegalArgumentException("rows must be positive");
        if (arr.length % rows != 0) throw new IllegalArgumentException("array length must be divisible by number of rows");
        int cols = arr.length / rows;
        for (int c = 0; c < (cols / 2); c++) {
            for (int r = 0; r < rows; r++) {
                int ai = r * cols + c;
                int bi = r * cols + (cols - c - 1);
                arr[ai] ^= arr[bi];
                arr[bi] ^= arr[ai];
                arr[ai] ^= arr[bi];
            }
        }
        return arr;
    }

    /**
     * Reflects a given square matrix over the diagonal (line from bottomleft - topright).
     * @param arr The NxN matrix to reflect.
     * @param n Length of one of the sides.
     * @return The mutated array.
     * @throws IllegalArgumentException if the array length is not n^2.
     */
    public static int[] reflectOverDiagonal(int[] arr, int n) {
        if (arr == null) throw new NullPointerException();
        if (n < 0) throw new IllegalArgumentException("n must be nonnegative");
        if (arr.length != n * n) throw new IllegalArgumentException("arr length not n square");
        reflectOverVertical(arr, n);
        rotate90(arr, n);
        return arr;
    }

    /**
     * Reflects a given square matrix over the anti-diagonal (line from topleft - bottomright).
     * @param arr The NxN matrix to reflect.
     * @param n Length of one of the sides.
     * @return The mutated array.
     * @throws IllegalArgumentException if the array length is not n^2.
     */
    public static int[] reflectOverAntiDiagonal(int[] arr, int n) {
        rotate90(arr, n);
        reflectOverVertical(arr, n);
        return arr;
    }

    /**
     * Swaps the specified bands of the sudoku array.
     * @param arr Sudoku board array.
     * @param bandIndexA Band index.
     * @param bandIndexB Band index, different from A.
     * @return The mutated sudoku board array.
     */
    public static int[] swapBands(int[] arr, int bandIndexA, int bandIndexB) {
        if (bandIndexA == bandIndexB) return arr;
        if (bandIndexA < 0 || bandIndexB < 0 || bandIndexA > 2 || bandIndexB > 2)
            throw new IllegalArgumentException("swapBands error, specified band(s) out of bounds");
        for (int i = 0; i < 27; i++) {
            int ai = BAND_INDICES[bandIndexA][i];
            int bi = BAND_INDICES[bandIndexB][i];

            arr[ai] ^= arr[bi];
            arr[bi] ^= arr[ai];
            arr[ai] ^= arr[bi];
        }
        return arr;
    }

    /**
     * Swaps the specified rows within a band of the sudoku array.
     * @param arr Sudoku board array.
     * @param bandIndex Band index.
     * @param rowA Row index.
     * @param rowB Row index, different from A.
     * @return The mutated sudoku board array.
     */
    public static int[] swapBandRows(int[] arr, int bandIndex, int rowA, int rowB) {
        if (rowA == rowB) return arr;
        if (bandIndex < 0 || bandIndex > 2 || rowA < 0 || rowB < 0 || rowA > 2 || rowB > 2)
            throw new IllegalArgumentException("swapBandRows error, specified band or row(s) out of bounds");
        for (int i = 0; i < DIGITS; i++) {
            int ii = BAND_ROW_INDICES[bandIndex][rowA][i];
            int jj = BAND_ROW_INDICES[bandIndex][rowB][i];

            arr[ii] ^= arr[jj];
            arr[jj] ^= arr[ii];
            arr[ii] ^= arr[jj];
        }
        return arr;
    }

    /**
     * Swaps the specified stacks of the sudoku array.
     * @param arr Sudoku board array.
     * @param stackIndexA Stack index.
     * @param stackIndexB Stack index, different from A.
     * @return The mutated sudoku board array.
     */
    public static int[] swapStacks(int[] arr, int stackIndexA, int stackIndexB) {
        if (stackIndexA == stackIndexB) return arr;
        if (stackIndexA < 0 || stackIndexB < 0 || stackIndexA > 2 || stackIndexB > 2)
            throw new IllegalArgumentException("swapStacks error, specified stack(s) out of bounds");
        for (int i = 0; i < 27; i++) {
            int ai = STACK_INDICES[stackIndexA][i];
            int bi = STACK_INDICES[stackIndexB][i];

            arr[ai] ^= arr[bi];
            arr[bi] ^= arr[ai];
            arr[ai] ^= arr[bi];
        }
        return arr;
    }

    /**
     * Swaps the specified columns within a stack of the sudoku array.
     * @param arr Sudoku board array.
     * @param stackIndex Stack index.
     * @param colA Column index.
     * @param colB Column index, different from A.
     * @return The mutated sudoku board array.
     */
    public static int[] swapStackCols(int[] arr, int stackIndex, int colA, int colB) {
        if (colA == colB) return arr;
        if (stackIndex < 0 || colA < 0 || colB < 0 || stackIndex > 2 || colA > 2 || colB > 2)
            throw new IllegalArgumentException("swapStackCols error, specified stack or col(s) out of bounds");
        for (int i = 0; i < Sudoku.DIGITS; i++) {
            int ii = STACK_COL_INDICES[stackIndex][colA][i];
            int jj = STACK_COL_INDICES[stackIndex][colB][i];

            arr[ii] ^= arr[jj];
            arr[jj] ^= arr[ii];
            arr[ii] ^= arr[jj];
        }
        return arr;
    }

    /**
     * Rearranges the given sudoku board digits such that the top row
     * reads 1 through 9 sequentially.
     * @param digits Sudoku board digits.
     * @return The mutated sudoku board digits.
     * @throws IllegalArgumentException if the board is not full.
     * TODO The board shouldn't have to be full to normalize.
     */
    public static int[] normalize(int[] digits) {
        if (digits.length != SPACES) {
            throw new IllegalArgumentException("digits length must be 81");
        }
        // Top row must be filled in and valid.
        if (!isRowFull(digits, 0) || !isRowValid(digits, 0)) {
            throw new IllegalArgumentException("top row must be full and valid");
        }
        // All in array must be proper digits
        for (int ci = 0; ci < SPACES; ci++) {
            if (digits[ci] < 0 || digits[ci] > DIGITS) {
                throw new IllegalArgumentException("bad value in sudoku matrix");
            }
        }

        for (int tarDigit = 1; tarDigit <= DIGITS; tarDigit++) {
            int curDigit = digits[tarDigit - 1];
            if (curDigit != tarDigit) {
                for (int ci = 0; ci < SPACES; ci++) {
                    if (digits[ci] == curDigit) {
                        digits[ci] = tarDigit;
                    } else if (digits[ci] == tarDigit) {
                        digits[ci] = curDigit;
                    }
                }
            }
        }

        return digits;
    }

    public static boolean isCandidatePair(int n) {
        return (n > 0) && (n < ALL) && (Integer.bitCount(n) == 2);
    }

    /**
     * Checks that the given string is valid to be used to initialize a Sudoku instance.
     * (i.e. is proper length and contains digits, '.', or '-' chars).
     *
     * NOTE: This does NOT check if the grid is a valid sudoku.
     * For that, check <code>sudoku.solutionsFlag() == 1</code>.
     * @param gridStr
     * @return True if the string can be used to instantiate a Sudoku instance; otherwise false.
     */
    public static boolean isValidStr(String gridStr) {
        return conformGridStr(gridStr) != null;
    }

    /**
     * Checks that the given sudoku string is of proper length.
     * Expands dashes '-' into 9 empty cells.
     * Converts non-digit characters to zeroes.
     * If this returns <code>null</code>, then the string shouldn't be used
     * in Sudoku constructor.
     * @param gridStr String representing a sudoku board.
     * @return The same string, or sudoku equivalent, if the original was valid;
     * or <code>null</code> if the string was malformed or an improper length.
     */
    private static String conformGridStr(String gridStr) {
        // Check for NULL and fail fast if length is bad.
        if (gridStr == null || gridStr.length() > SPACES) return null;
        // Expand '-' with 9 '0', and replace nonzero chars with '0'
        gridStr = gridStr.replaceAll("-", "0".repeat(DIGITS)).replaceAll("[^1-9]", "0");
        // Check for proper length
        return (gridStr.length() == SPACES) ? gridStr : null;
    }

    /**
     * Builds a multiline string representation of the given sudoku board,
     * including spacing and region borders.
     * @param digits Sudoku board digits.
     * @return Multiline string representation of the board.
     */
    public static String toFullString(int[] digits) {
        StringBuilder strb = new StringBuilder("  ");
        String lineSep = System.lineSeparator();
        for (int i = 0; i < SPACES; i++) {
            if (digits[i] > 0) {
                strb.append(digits[i]);
            } else {
                strb.append('.');
            }

            // Print pipe between region columns
            if ((((i+1)%3) == 0) && (((i+1)%9) != 0)) {
                strb.append(" | ");
            } else {
                strb.append("   ");
            }

            if (((i+1)%9) == 0) {
                strb.append(lineSep);

                if (i < 80) {
                    // Border between region rows
                    if (((((i+1)/9)%3) == 0) && (((i/9)%8) != 0)) {
                        strb.append(" -----------+-----------+------------");
                    } else {
                        strb.append("            |           |            ");
                    }
                    strb.append(lineSep);
                    strb.append("  ");
                }
            }
        }

        return strb.toString();
    }

    /**
     * Builds a multiline string representation of the given sudoku board,
     * including region borders.
     * @param digits Sudoku board digits.
     * @return Multiline string representation of the board.
     */
    public static String toMedString(int[] digits) {
        StringBuilder strb = new StringBuilder();
        String lineSep = System.lineSeparator();
        for (int i = 0; i < SPACES; i++) {
            if (digits[i] > 0) {
                strb.append(digits[i]);
            } else {
                strb.append('.');
            }

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

    /** Sudoku board array.*/
    int[] digits;

    /**
     * Sudoku board array, as encoded candidates per cell.
     * <br><br>Example: <code>candidates[55] = 0b110110001</code>.
     * Cell 55 has possibilities <code>9, 8, 6, 5, 1</code>.
     * <br><br>
     * <code>0</code> indicates that the cell cannot be any digit (the puzzle may be invalid).
     */
    int[] candidates;

    /**
     * A matrix which tracks digits that have been used in each row, column, and region
     * - encoded as 27-bit values. Index by row, column, or region.
     * <br><br>
     * The encoding works as follows:
     * <br><br>
     * <code>[9 bits for the row digits][9 bits for the column][9 bits for the region]</code>
     * <br><br>
     * Some bit manipulation is required to access the values for any given area.
     * <br><br>
     * e.g., To retrieve the digits used by the bottom row (8): <code>(constraints[8] >> 18) & ALL</code>.
     * This gives a 9-bit encdoded value where bits set correlate to digits used thus far in the given row.
     * <br><br>See <code>cellConstraints(cellIndex)</code> for usage.
     */
    int[] constraints;

    /** A count of the empty cells on the board.*/
    int numEmptyCells = SPACES;

    /** Tracks whether the board is currently valid.*/
    boolean isValid = true;

    // TODO Implement isSolved cache
    // This should be cached true when isSolved is called, and invalidated whenever a value is changed
    /** Tracks whether the board is solved.*/
    boolean isSolved = false;

    /**
     * Creates a new, empty Sudoku instance.
     */
    public Sudoku() {
        this.digits = new int[SPACES];
        this.candidates = new int[SPACES];
        Arrays.fill(this.candidates, ALL);
        this.constraints = new int[DIGITS];
    }

    /**
     * Creates a new Sudoku instance with state copied from <code>other</code>.
     * @param other Another sudoku instance.
     */
    public Sudoku(Sudoku other) {
        this();
        copyFrom(other);
    }

    /**
     * Creates a new Sudoku instance with values supplied from a string.
     * The string may contain dashes/minuses, '-', which will be expanded into 9 empty cells.
     * Periods are often used for empty cells, but any non-digit character will translate
     * to an empty cell.
     * @param gridStr A string representation of a sudoku board.
     * @throw IllegalArgumentException if the string is malformed or of improper length.
     */
    public Sudoku(String gridStr) {
        this();

        gridStr = conformGridStr(gridStr);
        if (gridStr == null) {
            throw new IllegalArgumentException("Malformed sudoku grid string");
        }

        for (int i = 0; i < SPACES; i++) {
            int digit = gridStr.charAt(i) - '0';
            if (digit > 0) {
                setDigit(i, digit);
            }
        }
    }

    /**
     * Creates a new Sudoku instance, copying the supplied digits.
     * Only digit will be copied; other values will be empty cells.
     * @param digits Sudoku board digits.
     * @throws IllegalArgumentException if digits length is not exactly 81.
     */
    public Sudoku(int[] digits) {
        this();
        copyFrom(digits);
    }

    /** Helper that converts byte[] representation of sudoku board into int[].*/
    private static int[] fromBytes(byte[] bytes) {
        if (bytes.length != 41) throw new IllegalArgumentException("bytes length must be 41");
        int[] _digits = new int[SPACES];
        for (int bi = 0; bi < 40; bi++) {
            _digits[2*bi] = (int)( (bytes[bi] >>> 4) & 0xf );
            _digits[2*bi + 1] = (int)( bytes[bi] & 0xf );
        }
        _digits[80] = (int)( (bytes[40] >>> 4) & 0xf );
        return _digits;
    }

    /**
     * Creates a new Sudoku instance from a given byte array.
     * The array must be a specific length (41), as each byte will cover 2 cells.
     * @param bytes Byte array encoding of sudoku digits.
     * @throws IllegalArgumentException if the byte array length is not 41.
     */
    public Sudoku(byte[] bytes) {
        this(fromBytes(bytes));
    }

    /**
     * Resets the instance to an empty board.
     */
    public void reset() {
        this.numEmptyCells = SPACES;
        this.isSolved = false;
        this.isValid = true;
        Arrays.fill(this.candidates, ALL);
        Arrays.fill(this.digits, 0);
        Arrays.fill(this.constraints, 0);
    }

    /**
     * Overwrites the state of this instance with that of the one given.
     * @param other Another Sudoku instance.
     */
    public void copyFrom(Sudoku other) {
        this.numEmptyCells = other.numEmptyCells;
        this.isSolved = other.isSolved;
        this.isValid = other.isValid;
        System.arraycopy(other.digits, 0, this.digits, 0, SPACES);
        System.arraycopy(other.candidates, 0, this.candidates, 0, SPACES);
        System.arraycopy(other.constraints, 0, this.constraints, 0, DIGITS);
    }

    /**
     * Overwrites the state of this instance with the given sudoku array.
     * @param digits Sudoku board digits.
     */
    public void copyFrom(int[] digits) {
        if (digits.length != SPACES) {
            throw new IllegalArgumentException("digits array has bad length");
        }

        this.numEmptyCells = SPACES;
        this.isSolved = false;
        this.isValid = true;

        Arrays.fill(this.candidates, ALL);
        Arrays.fill(this.digits, 0);
        Arrays.fill(this.constraints, 0);

        for (int ci = 0; ci < SPACES; ci++) {
            int d = digits[ci];
            if (d > 0 && d <= DIGITS) {
                setDigit(ci, d);
            }
        }
    }

    /**
     * Get a digit.
     * @param cellIndex
     * @return Digit of the specified cell.
     */
    public int getDigit(int cellIndex) {
        return digits[cellIndex];
    }

    /**
     * Gets a copy of the board digits.
     * @return A new array containing the board digits.
     */
    public int[] toArray() {
        int[] arr = new int[SPACES];
        System.arraycopy(digits, 0, arr, 0, SPACES);
        return arr;
    }

    /**
     * Get an array of candidate digits for a cell.
     * @param cellIndex
     * @return A new array containing candidates for the specified cell.
     */
    public int[] getCellCandidates(int cellIndex) {
        return ArraysUtil.copy(CANDIDATES[candidates[cellIndex]]);
    }

    /**
     * Gets a copy of the board as encoded board values.
     * @return A new array containing each cell's encoded value.
     */
    public int[] getCandidatesEncoded() {
        int[] arr = new int[SPACES];
        System.arraycopy(candidates, 0, arr, 0, SPACES);
        return arr;
    }

    /**
     * Set the value of a cell.
     * TODO Keep isValid, isSolved, in sync.
     * @param cellIndex
     * @param digit
     */
    public void setDigit(int cellIndex, int digit) {
        int prevDigit = this.digits[cellIndex];
        if (prevDigit == digit) return;

        digits[cellIndex] = digit;
        candidates[cellIndex] = ENCODER[digit];

        // Digit removed (or replaced)
        if (prevDigit > 0) {
            numEmptyCells++;
            removeConstraint(cellIndex, prevDigit);
        }
        // Digit added (or replaced)
        if (digit > 0) {
            numEmptyCells--;
            addConstraint(cellIndex, digit);
        }
    }

    /**
     * Clears the value of a cell.
     * @param cellIndex
     */
    public void clearCell(int cellIndex) {
        setDigit(cellIndex, 0);
    }

    /**
     * Helper that adds a value to the constraints map.
     * @param cellIndex
     * @param digit
     */
    void addConstraint(int cellIndex, int digit) {
        int dMask = ENCODER[digit];
        constraints[CELL_ROWS[cellIndex]] |= dMask << (DIGITS*2);
        constraints[CELL_COLS[cellIndex]] |= dMask << DIGITS;
        constraints[CELL_REGIONS[cellIndex]] |= dMask;
    }

    /**
     * Helper that removes a value from the constraints map.
     * @param cellIndex
     * @param digit
     */
    void removeConstraint(int cellIndex, int digit) {
        int dMask = ENCODER[digit];
        constraints[CELL_ROWS[cellIndex]] &= ~(dMask << (DIGITS*2));
        constraints[CELL_COLS[cellIndex]] &= ~(dMask << DIGITS);
        constraints[CELL_REGIONS[cellIndex]] &= ~dMask;
    }

    /**
     * Gets the constraints for the specified cell, indicating which digits are
     * present in the cell's row, column, and region.
     * @param cellIndex
     * @return An 9-bit encoded board value indicating which digits the cell cannot be.
     */
    public int cellConstraints(int cellIndex) {
        return (
            (constraints[CELL_ROWS[cellIndex]] >> (DIGITS*2)) |
            (constraints[CELL_COLS[cellIndex]] >> (DIGITS)) |
            constraints[CELL_REGIONS[cellIndex]]
        ) & ALL;
    }

    /**
     * Creates a new Sudoku instance and fills 3 non-conflicting regions randomly.
     * Ideal for crafting random solutions, therefore referred as a solution "seed".
     * @return A new partially-filled sudoku instance.
     */
    public static Sudoku configSeed() {
        Sudoku seed = new Sudoku();
        int[] _digitsArr = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        ArraysUtil.shuffle(_digitsArr);
        for (int i = 0; i < DIGITS; i++) seed.setDigit(REGION_INDICES[0][i], _digitsArr[i]);
        ArraysUtil.shuffle(_digitsArr);
        for (int i = 0; i < DIGITS; i++) seed.setDigit(REGION_INDICES[4][i], _digitsArr[i]);
        ArraysUtil.shuffle(_digitsArr);
        for (int i = 0; i < DIGITS; i++) seed.setDigit(REGION_INDICES[8][i], _digitsArr[i]);
        return seed;
    }

    /**
     * Resets all empty cells to full candidates, and rebuilds constraints.
     * Used prior to searching for solutions to ensure the puzzle is in a good state.
     */
    void resetCandidatesAndValidity() {
        isValid = true;
        Arrays.fill(constraints, 0);
        for (int ci = 0; ci < SPACES; ci++) {
            candidates[ci] = ALL;
            if (digits[ci] > 0) {
                candidates[ci] = ENCODER[digits[ci]];
                if ((cellConstraints(ci) & candidates[ci]) > 0) {
                    isValid = false;
                }
                addConstraint(ci, digits[ci]);
            }
        }
    }

    /** Gets whether the sudoku board is full.*/
    public boolean isFull() {
        return this.numEmptyCells == 0;
    }

    /** Gets whether the sudoku board is empty.*/
    public boolean isEmpty() {
        return numEmptyCells == SPACES;
    }

    /** Gets the number of empty cells.*/
    public int numEmptyCells() {
        return numEmptyCells;
    }

    /** Gets the number of filled-in cells.*/
    public int numClues() {
        return SPACES - numEmptyCells;
    }

    /** Gets whether the sudoku board is solved (full and valid).*/
    public boolean isSolved() {
        for (int c : constraints) {
            if (c != FULL_CONSTRAINTS) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets whether the sudoku board is valid, i.e., there are no rows, columns,
     * or regions with repeated or conflicting digits.
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * Attempts to fill in the board values by constraint propagation and
     * other basic sudoku techniques.
     * May or may not complete the board.
     */
    public void reduce() {
        for (int i = 0; i < SPACES; i++) reduceCell(i);
    }

    /**
     * Attempts to reduce a specific cell by applying constraints,
     * checking for a naked or hidden single, and other basic sudoku logic.
     * <br><br>If the cell is already solved, does nothing.
     * <br><br>If the cell reduces to 0 candidates, marks the instance as invalid.
     * <br><br>If the cell's candidates are successfully reduced or the cell becomes
     * solved, recursively calls <code>reduceCell()</code> for all neighboring cells.
     * @param ci Cell index.
     */
    void reduceCell(int ci) {
        if (digits[ci] > 0) return;

        int originalCandidates = candidates[ci];
        // If candidate reduces to 0, then the board is invalid.
        candidates[ci] &= ~cellConstraints(ci);

        if (candidates[ci] <= 0) {
            isValid = false;
            setDigit(ci, 0);
            return;
        }

        // If by applying the constraints, the number of candidates is reduced to 1,
        // then the cell is solved.
        if (isDigit(candidates[ci])) {
            setDigit(ci, DECODER[candidates[ci]]);
        } else {
            int uniqueCandidate = getUniqueCandidate(ci);
            if (uniqueCandidate > 0) {
                setDigit(ci, DECODER[uniqueCandidate]);
            } else {
                // If cell[ci] is not a double, then this next part can be skipped.
                // if (isCandidatePair(reducedCandidates)) {
                //     // For each area,
                //     //    Look for candidate pairs
                //     //    If found,
                //     //      Remove the pair of digits from candidates in the area (except the pair of cells)
                //     int ciRow = CELL_ROWS[ci];
                //     int ciCol = CELL_COLS[ci];
                //     int ciRegion = CELL_REGIONS[ci];
                //     for (int col = 0; col < DIGITS; col++) {
                //         // Look at row neighbors for a potential pair with ci
                //         if (col == ciCol) continue; // Skip ci
                //         int rowNi = DIGITS*ciRow + col;
                //         if (decode(candidates[rowNi]) > 0) continue;
                //         if (candidates[rowNi] == candidates[ci]) {
                //             // Found pair (ci, gi)
                //             // TODO Maintain a collection of 'seen pairs' and if we've seen this pair, skip processing.
                //             // console.log(`Found pairs within row, value [ ${reducedCandidates.toString(2)} ] at (${ci}, ${rowNi})`);

                //             for (int ei = 0; ei < DIGITS; ei++) {
                //                 int ki = DIGITS*ciRow + ei;
                //                 if (ci == ki || rowNi == ki || digits[ki] > 0) continue;
                //                 int _before = candidates[ki];
                //                 int _after = (_before & ~reducedCandidates);
                //                 if (isDigit(_after)) {
                //                     setDigit(ki, decode(_after));
                //                     // console.log(`DIGIT resolved after reducing PAIR [${ki}] ${_before.toString(2)} -> ${decode(_after)}`);
                //                 } else {
                //                     candidates[ki] = _after;
                //                 }
                //                 if (_after < _before) {
                //                     for (int ni : CELL_NEIGHBORS[ki]) {
                //                         if (DECODER[candidates[ni]] == 0) reduceCell(ni);
                //                     }
                //                 }
                //             }
                //         }
                //     }
                // }
            }
        }

        if (candidates[ci] < originalCandidates) {
            for (int n : CELL_NEIGHBORS[ci]) {
                if (digits[n] == 0) {
                    reduceCell(n);
                }
            }
        }
    }

    /**
     * Helper method for performing a pass of constraint propagation on the entire board,
     * similar to <code>reduce()</code>, but only constraint propagation -- not
     * checking any other techniques.
     *
     * Not currently used.
     */
    private void constraintProp() {
        for (int i = 0; i < SPACES; i++) {
            _constraintProp(i);
        }
    }

    /**
     * Helper to propagate constraints to a specific cell.
     * Similar to <code>reduceCell()</code>.
     * @param ci Cell index.
     */
    private void _constraintProp(int ci) {
        if (digits[ci] > 0) {
            return;
        }

        int originalCandidates = candidates[ci];
        // If candidate constraints reduces to 0, then the board is likely invalid.
        candidates[ci] &= ~cellConstraints(ci);
        if (candidates[ci] <= 0) {
            isValid = false;
            setDigit(ci, 0);
            return;
        }

        // If by applying the constraints, the number of candidates is reduced to 1,
        // then the cell is solved.
        if (isDigit(candidates[ci])) {
            setDigit(ci, DECODER[candidates[ci]]);
        }

        if (candidates[ci] < originalCandidates) {
            for (int n : CELL_NEIGHBORS[ci]) {
                if (digits[n] == 0) {
                    _constraintProp(n);
                }
            }
        }
    }

    /**
     * Hidden single checker.
     *
     * Checks if the specified cell contains a candidate that is unique
     * within its row, column, or region.
     * @param cellIndex
     * @return The unique candidate value (encoded); or 0 if none.
     */
    int getUniqueCandidate(int cellIndex) {
        for (int candidate : CANDIDATES_ENC[candidates[cellIndex]]) {
            boolean unique = true;
            for (int neighborIndex : ROW_NEIGHBORS[cellIndex]) {
                if ((candidates[neighborIndex] & candidate) > 0) {
                    unique = false;
                    break;
                }
            }
            if (unique) return candidate;

            unique = true;
            for (int neighborIndex : COL_NEIGHBORS[cellIndex]) {
                if ((candidates[neighborIndex] & candidate) > 0) {
                    unique = false;
                    break;
                }
            }
            if (unique) return candidate;

            unique = true;
            for (int neighborIndex : REGION_NEIGHBORS[cellIndex]) {
                if ((candidates[neighborIndex] & candidate) > 0) {
                    unique = false;
                    break;
                }
            }
            if (unique) return candidate;
        }

        return 0;
    }

    /**
     * Generates a puzzle with the given number of clues.
     * Not recommended to attempt puzzle generation with less than 21 clues.
     * @return A new Sudoku puzzle instance.
     * @throws IllegalArgumentException if numClues is less than the minimum 17.
     */
    public static Sudoku generatePuzzle(int numClues) {
        return generatePuzzle(generateConfig(), numClues, null, 0, 0L);
    }

    /**
     * Generates a puzzle within the given parameters, using this instance as the solution grid.
     *
     * @param numClues
     * @param sieve
     * @param difficulty
     * @param timeoutMs
     * @return
     */
    public Sudoku generatePuzzle(
        int numClues,
        SudokuSieve sieve,
        int difficulty,
        long timeoutMs
    ) {
        return Sudoku.generatePuzzle(this, numClues, sieve, difficulty, timeoutMs);
    }

    /**
     * Generates a puzzle.
     * If numClues is less than the minimum 17, returns null.
     * @param grid (Optional) The solution. If provided, must be full and valid.
     * @param numClues Number of clues.
     * @param sieve A list of SudokuMask to use as a sieve of unavoidable sets.
     * @param difficulty 0: ignore, 1: easy, 2: medium, 3: hard.
     * @param timeoutMs Amount of system time(ms) to spend generating. 0 for no limit.
     * @return A new Sudoku puzzle instance; or null if the time limit is exceeded.
     * @throws IllegalArgumentException numClues out of bounds;
     * or sieve is given but grid is not;
     * or grid is given but not solved;
     * or difficulty out of bounds.
     */
    public static Sudoku generatePuzzle(
        Sudoku grid,
        int numClues,
        SudokuSieve sieve,
        int difficulty,
        long timeoutMs
    ) {
        if (numClues < MIN_CLUES || numClues > SPACES)
            throw new IllegalArgumentException("Invalid number of clues");
        if (sieve != null && grid == null)
            throw new IllegalArgumentException("Sieve provided without grid");
        if (grid == null)
            grid = configSeed().solution();
        if (!grid.isSolved())
            throw new IllegalArgumentException("Solution grid is invalid");
        if (numClues >= SPACES)
            return grid;
        if (difficulty < 0 || difficulty > 3)
            throw new IllegalArgumentException(String.format("Invalid difficulty (%d); expected 0 <= difficulty <= 4", difficulty));
        if (sieve == null)
            sieve = new SudokuSieve(grid);

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        long start = System.currentTimeMillis();
        // const FULLMASK = (1n << BigInt(SPACES)) - 1n;
        // SudokuMask FULLMASK = SudokuMask.full();
        int maskFails = 0;
        int puzzleCheckFails = 0;
        int putBacks = 0;
        SudokuMask mask = SudokuMask.full();
        List<Integer> remaining = ArraysUtil.rangeList(SPACES);
        ArrayList<Integer> removed = new ArrayList<>();
        int[] indices = ArraysUtil.shuffle(ArraysUtil.range(SPACES));
        int choices = 0;

        while (remaining.size() > numClues) {
            int startChoices = remaining.size();
            ArraysUtil.shuffle(remaining);
            for (int i = 0; i < remaining.size() && remaining.size() > numClues; i++) {
                int choice = remaining.get(i);
                // mask &= ~cellMask(choice);
                mask.unsetBit(choice);

                boolean satisfies = sieve.doesMaskSatisfy(mask);

                // If not, or if there are multiple solutions,
                // put the cell back and try the next
                if (!satisfies) {
                    maskFails++;
                    // mask |= cellMask(choice);
                    mask.setBit(choice);

                    // Once in awhile, check the time
                    if (timeoutMs > 0L && maskFails == 100) {
                        if ((System.currentTimeMillis() - start) > timeoutMs) {
                            return null;
                        }
                        maskFails -= 100;
                    }

                    continue;
                }

                if (grid.filter(mask).solutionsFlag() != 1) {
                    puzzleCheckFails++;
                    if (puzzleCheckFails == 100 && sieve.size() < 100) {
                        sieve.seedThreaded(sieve.fullPrintCombos(2));
                    } else if (puzzleCheckFails == 2000 && sieve.size() < 1000) {
                        sieve.seedThreaded(sieve.fullPrintCombos(3));
                    } else if (puzzleCheckFails > 10_000 && sieve.size() < 10_000) {
                        sieve.addFromPuzzleMask(mask);
                    }

                    mask.setBit(choice);
                    continue;
                }

                removed.add(choice);
                remaining.remove(i);
                i--;
            }

            // If no cells were chosen
            // - Put some cells back and try again
            if (
                (
                    remaining.size() == numClues &&
                    difficulty > 0 &&
                    grid.filter(mask).solutionsFlag() == 1 //&&
                    // grid.filter(mask).difficulty() != difficulty
                ) || remaining.size() == startChoices
            ) {
                int numToPutBack = 3 + rand.nextInt(3);
                ArraysUtil.shuffle(removed);
                for (int i = 0; i < numToPutBack; i++) {
                    int cell = removed.remove(removed.size() - 1);
                    remaining.add(cell);
                    mask.setBit(cell);
                    if (removed.size() == 0)
                        break;
                }
                putBacks++;
            }
        }

        return grid.filter(mask);
    }

    /**
     * Helper class used in the search algorithms.
     */
    private static class SudokuNode {
        Sudoku sudoku;
        int index = -1;
        int values = -1;
        SudokuNode(Sudoku sudoku) {
            this.sudoku = sudoku;
            sudoku.reduce();
            index = sudoku.pickEmptyCell();
            if (index != -1) {
                values = sudoku.candidates[index];
            }
        }
        SudokuNode next() {
            // If this node's sudoku had no emptycells, then `index` and `values`
            // would have never been set, and both would still be -1
            if (values <= 0 || !sudoku.isValid) return null;

            Sudoku s = new Sudoku(sudoku);
            int[] candidateDigits = CANDIDATES[values];
            int randomCandidateDigit = candidateDigits[ThreadLocalRandom.current().nextInt(candidateDigits.length)];
            s.setDigit(index, randomCandidateDigit);
            values &= ~(ENCODER[randomCandidateDigit]);
            return new SudokuNode(s);
        }
        boolean hasNext() {
            return (values > 0 && sudoku.isValid) ? true : false;
        }
    }

    /**
     * Helper class used in searchForSolutions3.
     * Created during solution search rewrite to reduce memory footprint.
     * (Did it work? - I don't remember!)
     */
    private static class ANode {
        Sudoku snapshot = new Sudoku();
        int emptyCi = -1;
        int emptyCandidates = -1;

        ANode() {}

        void set(Sudoku sudoku) {
            snapshot.copyFrom(sudoku);
            emptyCi = sudoku.pickEmptyCell();
            emptyCandidates = -1;
            if (!sudoku.isValid) return;
            emptyCandidates = (snapshot.isValid && emptyCi > -1) ? snapshot.candidates[emptyCi] : -1;
        }

        boolean loadNext(Sudoku sudoku) {
            if (emptyCandidates <= 0) {
                return false;
            }

            do {
                sudoku.copyFrom(snapshot);
                int[] candidateDigits = CANDIDATES[emptyCandidates];

                int randomCandidateDigit = candidateDigits[ThreadLocalRandom.current().nextInt(candidateDigits.length)];
                // int randomCandidateDigit = candidateDigits[candidateDigits.length - 1];

                sudoku.setDigit(emptyCi, randomCandidateDigit);
                emptyCandidates -= ENCODER[randomCandidateDigit];

                // sudoku.reduce();
                for (int ni : CELL_NEIGHBORS[emptyCi]) {
                    if (sudoku.digits[ni] == 0) sudoku.reduceCell(ni);
                }

            } while (emptyCandidates > 0 && !sudoku.isValid);

            return sudoku.isValid;
        }
    }

    /**
     * Initiates an exhaustive search for solutions. <code>solutionCallback</code> will be
     * invoked with solutions as they are found. The callback should return <code>true</code>
     * to continue the search, or <code>false</code> to end it.
     * TODO rename
     * @param solutionCallback A function which takes a solution to act upon, and returns
     * whether or not to continue the search.
     */
    public void searchForSolutions3(Function<Sudoku,Boolean> solutionCallback) {
        Sudoku puzz = new Sudoku(this);
        puzz.resetCandidatesAndValidity();
        puzz.reduce();

        // If we can stop early then GREAT!
        if (!puzz.isValid) return;
        if (puzz.isSolved()) {
            solutionCallback.apply(puzz);
            return;
        }

        ANode[] stack = new ANode[puzz.numEmptyCells];
        for (int i = 0; i < stack.length; i++) stack[i] = new ANode();
        stack[0].set(puzz);
        stack[0].loadNext(puzz);
        int curStackIndex = 0;

        while (curStackIndex > -1) {
            // NOTE: puzz state is modified by ANodes -- it will be kept in sync with the stack top.
            if (!puzz.isValid) {
                // While top does NOT have a valid alternative, POP off the stack.
                while (curStackIndex > -1 && !stack[curStackIndex].loadNext(puzz)) curStackIndex--;
            } else if (puzz.isSolved()) {
                // Solution found, send to callback and possibly halt.
                if (!solutionCallback.apply(new Sudoku(puzz))) break;
                // While top does NOT have a valid alternative, POP off the stack.
                while (curStackIndex > -1 && !stack[curStackIndex].loadNext(puzz)) curStackIndex--;
            } else {
                // Valid but not solved, PUSH to the stack.
                curStackIndex++;
                // Copy puzz state into node, then find/load the next valid state.
                stack[curStackIndex].set(puzz);
                stack[curStackIndex].loadNext(puzz);
            }
        }
    }

    /**
     * Initiates an exhaustive search for solutions and returns the total.
     * This may take awhile if the puzzle is sparse.
     * @return Number of solutions to this sudoku.
     */
    public long countSolutions() {
        Sudoku root = new Sudoku(this);
        root.resetCandidatesAndValidity();

        if (!root.isValid) return 0;

        long count = 0L;
        Stack<SudokuNode> stack = new Stack<>();
        stack.push(new SudokuNode(root));

        while (!stack.isEmpty()) {
            SudokuNode top = stack.peek();
            Sudoku sudoku = top.sudoku;

            if (sudoku.isSolved()) {
                stack.pop();
                count++;
            } else if (top.hasNext()) {
                stack.push(top.next());
            } else {
                stack.pop();
            }
        }

        return count;
    }

    /**
     * Initiates an exhaustive asynchronous solutions search using the given number of threads.
     * Continues until all solutions are found, or until the specified amount of time has elapsed.
     * <code>solutionCallback</code> will be called asynchronously with solutions as they are found.
     * @param solutionCallback Invoked with solutions as they are found. Multiple threads may
     * execute the function asynchronously, so like, plan ahead for that.
     * @param numThreads Number of threads to utilize. Not recommended to use more than
     * the system's max.
     * @param timeoutMs The total (system clock) time to wait for execution.
     * If 0 or negative, defaults to 1 hour.
     * @return True if the search is completely exhausted; otherwise false (timeout or interruption).
     */
    public boolean searchForSolutionsAsync(
        Consumer<Sudoku> solutionCallback,
        int numThreads,
        long timeoutMs
    ) {
        if (numThreads < 1) throw new IllegalArgumentException("numThreads must be positive");
        if (timeoutMs < 0L) timeoutMs = TimeUnit.HOURS.toMillis(1L);

        Sudoku root = new Sudoku(this);
        root.resetCandidatesAndValidity();

        Queue<SudokuNode> q = new LinkedList<>();
        q.offer(new SudokuNode(root));

        final int MAX_QUEUE_SIZE = numThreads * numThreads;
        while (!q.isEmpty() && q.size() < MAX_QUEUE_SIZE) {
            SudokuNode node = q.poll();

            if (node.sudoku.isSolved()) {
                solutionCallback.accept(node.sudoku);
                continue;
            }

            SudokuNode next;
            while ((next = node.next()) != null) q.offer(next);
        }

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            numThreads, numThreads,
            1L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
        );

        for (SudokuNode node : q) {
            pool.submit(() -> {
                node.sudoku.searchForSolutions3(solution -> {
                    solutionCallback.accept(solution);
                    return true;
                });
            });
        }

        pool.shutdown();
        try {
            return pool.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            pool.shutdownNow();
        }
        return false;
    }

    /**
     * Initiates an exhaustive asynchronous solutions search using the given number of threads.
     * Continues until all solutions are found, or until a preset 1 hour time limit has elapsed.
     * <code>solutionCallback</code> will be called asynchronously with solutions as they are found.
     * @param callback Invoked with solutions as they are found. Multiple threads may
     * execute the function asynchronously, so like, plan ahead for that.
     * @param numThreads Number of threads to utilize. Not recommended to use more than system max.
     * @return True if the search is completely exhausted; otherwise false (timeout or interruption).
     */
    public boolean searchForSolutionsAsync(Consumer<Sudoku> callback, int numThreads) {
        return searchForSolutionsAsync(callback, numThreads, TimeUnit.HOURS.toMillis(1L));
    }

    /**
     * Initiates an exhaustive asynchronous solutions search using the maximum number of threads
     * allowed by the system.
     * Continues until all solutions are found, or until a preset 1 hour time limit has elapsed.
     * <code>solutionCallback</code> will be called asynchronously with solutions as they are found.
     * @param callback Invoked with solutions as they are found. Multiple threads may
     * execute the function asynchronously, so like, plan ahead for that.
     * @return True if the search is completely exhausted; otherwise false (timeout or interruption).
     */
    public boolean searchForSolutionsAsync(Consumer<Sudoku> callback) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        return searchForSolutionsAsync(callback, numThreads, TimeUnit.HOURS.toMillis(1L));
    }

    /**
     * Gets an iterator of all solutions, generated sequentially on-demand.
     * @return A Sudoku solution iterator.
     */
    public Iterable<Sudoku> solutions() {
        return new SolutionIterator(this);
    }

    /**
     * A helper class that generates a sequence of solutions for a given sudoku.
     */
    public static class SolutionIterator implements Iterator<Sudoku>, Iterable<Sudoku> {
        Sudoku root;
        Sudoku next;
        Stack<SudokuNode> stack = new Stack<>();

        public SolutionIterator(Sudoku root) {
            this.root = new Sudoku(root);
            this.stack = new Stack<>();
            this.root.resetCandidatesAndValidity();
            this.stack.push(new SudokuNode(this.root));
            findNext();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Sudoku next() {
            Sudoku result = next;
            findNext();
            return result;
        }

        private void findNext() {
            next = null;
            while (!stack.isEmpty()) {
                SudokuNode node = stack.peek();
                if (node.sudoku.isSolved()) {
                    next = node.sudoku;
                    stack.pop();
                    return;
                } else if (node.hasNext()) {
                    stack.push(node.next());
                } else {
                    stack.pop();
                }
            }
        }

        @Override
        public Iterator<Sudoku> iterator() {
            return this;
        }
    }

    /**
     * Initiates an exhaustive search for solutions, using the given number of threads.
     * This may take awhile if the puzzle is sparse. Preconfigured time limit of 1 day.
     * @param numThreads Number of threads to utilize. Not recommended to use more than system max.
     * @return Number of solutions found.
     */
    public long countSolutionsAsync(int numThreads) {
        if (numThreads < 1) throw new IllegalArgumentException("numThreads must be positive");

        AtomicLong count = new AtomicLong();

        Sudoku root = new Sudoku(this);
        // Ensure candidates and constraints are in good order for the search
        root.resetCandidatesAndValidity();

        int maxSplitSize = numThreads * numThreads;
        Queue<SudokuNode> queue = new LinkedList<>();
        queue.offer(new SudokuNode(root));
        while (!queue.isEmpty() && queue.size() < maxSplitSize) {
            SudokuNode node = queue.poll();

            if (node.sudoku.isSolved()) {
                count.incrementAndGet();
                continue;
            }

            while (node.hasNext()) queue.offer(node.next());
        }

        if (queue.isEmpty()) return count.get();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            numThreads, numThreads,
            1L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
        );

        while (!queue.isEmpty()) {
            SudokuNode node = queue.poll();
            pool.submit(() -> {
                long localCount = node.sudoku.countSolutions();
                count.addAndGet(localCount);
            });
        }

        pool.shutdown();
        try {
            pool.awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return count.get();
    }

    // TODO countSolutionsAsync helpers similar to searchForSolutionsAsync.

    /**
     * Generates a random solution.
     * @return A new, solved sudoku instance.
     */
    public static Sudoku generateConfig() {
        return configSeed().solution();
    }

    /**
     * Generate a specified amount of random sudoku solutions.
     * @param amount Number of solution to generate.
     * @param list List where the solutions will be added.
     * @return The given List.
     */
    public static List<Sudoku> generateConfigs(int amount, List<Sudoku> list) {
        for (int n = 0; n < amount; n++) {
            list.add(generateConfig());
        }
        return list;
    }

    /**
     * Generate a specified amount of random sudoku solutions.
     * @param amount Number of solutions to generate.
     * @return A new List containing the generated solutions.
     */
    public static List<Sudoku> generateConfigs(int amount) {
        return generateConfigs(amount, new ArrayList<>(amount));
    }

    /**
     * Initiates a search for solutions and returns the first one.
     * @return The sudoku solution; or null if no solutions were found.
     */
    public Sudoku solution() {
        AtomicReference<Sudoku> result = new AtomicReference<>();
        searchForSolutions3(solution -> {
            result.set(solution);
            return false;
        });
        return result.get();
    }

    /**
     * Initiates a search for all solutions, and returns a List of results.
     * @return A new List containing all solutions.
     */
    public List<Sudoku> getAllSolutions() {
        return getAllSolutions(new ArrayList<>());
    }

    /**
     * Initiates a search for all solutions, collecting them in the given List.
     * @param list List where the solutions will be added.
     * @return The given List.
     */
    public List<Sudoku> getAllSolutions(List<Sudoku> list) {
        searchForSolutions3(solution -> {
            list.add(solution);
            return true;
        });
        return list;
    }
    // TODO does it need to be a List, or can we just use Collection.add?

    /**
     * Initiates a search for solutions, collecting them in the given List.
     * @param amount Maximum number of solutions to collect.
     * @param list List where the solutions will be added.
     * @return The given List.
     */
    public List<Sudoku> getSolutions(int amount, List<Sudoku> list) {
        searchForSolutions3(solution -> {
            list.add(solution);
            return list.size() < amount;
        });
        return list;
    }

    // TODO getAllSolutionsAsync helpers

    /**
     * Attempts to solve this sudoku, replacing the current state with the solution.
     * If there are multiple solutions, uses the first found.
     * @return True if the sudoku has been solved; otherwise false (no solution).
     */
    public boolean solve() {
        AtomicReference<Sudoku> solution = new AtomicReference<>();
        searchForSolutions3(_solution -> {
            solution.set(_solution);
            return false;
        });

        Sudoku _solution = solution.get();
        if (_solution != null) {
            this.numEmptyCells = _solution.numEmptyCells;
            this.isValid = _solution.isValid;
            System.arraycopy(_solution.digits, 0, this.digits, 0, SPACES);
            System.arraycopy(_solution.candidates, 0, this.candidates, 0, SPACES);
            System.arraycopy(_solution.constraints, 0, this.constraints, 0, DIGITS);
            return true;
        }

        return false;
    }

    /**
     * Checks whether the additive branches of this puzzle solve uniquely.
     * If a puzzle is thought to branch additively by independently testing each
     * candidate in each empty cell, then this method checks that each of those
     * branches results in a puzzle that has a single, or no, solution.
     * <br><br>If any branch has multiple solutions, then the check fails.
     * <br><br>If the puzzle contains any cells that have no candidates,
     * i.e. an invalid puzzle, then the check fails.
     * <br><br>This method is useful for detecting unavoidable sets.
     * @return True if the additive branches of this puzzle are valid sudoku.
     */
    public boolean doBranchesSolveUniquely() {
        for (int ci = 0; ci < SPACES; ci++) {
            int originalVal = candidates[ci];
            // If there are no candidates for a cell, fail.
            if (originalVal == 0) return false;
            if (digits[ci] == 0) {
                // Number of branches for cell[ci] that have a single solution.
                int count = 0;
                for (int candidateDigit : CANDIDATES[originalVal]) {
                    setDigit(ci, candidateDigit); // mutates constraints
                    int flag = solutionsFlag();
                    setDigit(ci, 0); // undo the constraints mutation
                    candidates[ci] = originalVal;
                    // If a branch has multiple solutions, fail.
                    if (flag == 2) return false;
                    if (flag == 1) count++;
                }
                // For each empty cell, there must be at least one branch
                // that has a single solution.
                // If there were no branches with solutions (all were invalid),
                // then the puzzle was probably invalid to begin with.
                if (count < 1) return false;
            }
        }
        return true;
    }

    /**
     * Builds a new SudokuMask where bits are set for each digit on this sudoku.
     * @return A new SudokuMask.
     */
    public SudokuMask getMask() {
        if (isFull()) return SudokuMask.full();
        if (isEmpty()) return new SudokuMask();
        SudokuMask result = new SudokuMask();
        for (int ci = 0; ci < SPACES; ci++) {
            if (digits[ci] > 0) {
                result.setBit(ci);
            }
        }
        return result;
    }

    /**
     * Creates a SudokuMask with bits set for each digit included in the given
     * <code>encoded</code> value.
     * @param encoded 9-bit encoded value representing multiple digits.
     * @return A new SudokuMask with bits set for select digits on the board.
     */
    public SudokuMask maskForDigits(int encoded) {
        SudokuMask result = new SudokuMask();
        for (int ci = 0; ci < SPACES; ci++) {
            if ((candidates[ci] & encoded) > 0) {
                result = result.setBit(ci);
            }
        }
        return result;
    }

    /**
     * Helper that takes a sieve and builds a fingerprint hash.
     * <br><br>If level 2 is specified, then sieve items with odd numbers of cells are omitted
     * (because there won't be any), and the string is compressed accordingly.
     * <br><br>Example, <code>c:b:8:8:5:c::1a</code> (Whereas uncompressed would read <code>c::b::8::8::5::c::::1a</code>).
     * <br><br>Otherwise, <code>level</code> has no other effect.
     * @param level When <code>2</code>, indicates the hash should be compressed.
     * @param sieve A populated SudokuSieve.
     * @return A hash string.
     */
    private static String fpFromSieve(int level, SudokuSieve sieve) {
        // Track the maximum number of cells used by any unavoidable set
        // int minNumCells = SPACES;
        int maxNumCells = 0;
        int[] itemCountByNumCells = new int[SPACES];
        for (SudokuMask ua : sieve.items()) {
            int numCells = ua.bitCount();
            itemCountByNumCells[numCells]++;
            // if (numCells < minNumCells) minNumCells = numCells;
            if (numCells > maxNumCells) maxNumCells = numCells;
        }

        ArrayList<String> itemsList = new ArrayList<>();
        // An item (unavoidable set) includes a minimum of 4 cells
        for (int numCells = 4; numCells <= maxNumCells; numCells++) {
            // In level 2, there can be no UAs using an odd number of cells,
            // because each cell must have at least one complement.
            // Skipping odd numbers avoids "::", keeping the fingerprint short.
            if (level == 2 && (numCells & 1) == 1) {
                continue;
            }

            int count = itemCountByNumCells[numCells];
            itemsList.add((count > 0) ? Integer.toString(count, 16) : "");
        }

        return String.join(":", itemsList);
    }

    /**
     * Helper to build a fingerprint hash using the digit-combos strategy with a given level.
     * The digit-combos strategy collects sieve items by removing every combination of {level}
     * digits from the board, and checking all solutions for unavoidable sets (items) to add.
     * This means the minimum level is 2 (to remove every combination of 2 digits),
     * and that each subsequent level is a significant jump in processing power.
     * Not recommended above level 4.
     * @param level The level of detail (and processing power) used to process and build
     * the fingerprint. Every combination of {level} digits will be removed from the board and
     * solutions collected and checked for unavoidable sets to add to a sieve.
     * @return A hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    private String dc(int level) {
        SudokuSieve sieve = new SudokuSieve(toArray());
        sieve.seed(sieve.digitCombos(level));
        return fpFromSieve(level, sieve);
    }

    /**
     * Computes this solution's fingerprint by the digit-combos strategy (level 2).
     * @return The hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String dc2() { return dc(2); }

    /**
     * Computes this solution's fingerprint by the digit-combos strategy (level 3).
     * @return The hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String dc3() { return dc(3); }

    /**
     * Computes this solution's fingerprint by the digit-combos strategy (level 4).
     * @return The hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String dc4() { return dc(4); }

    /**
     * Computes this solution's fingerprint by the full-print strategy (level 2).
     * @return The hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String fp2() { return fp(2); }

    /**
     * Computes this solution's fingerprint by the full-print strategy (level 3).
     * @return The hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String fp3() { return fp(3); }

    /**
     * Computes this solution's fingerprint by the full-print strategy (level 4).
     * @return The hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String fp4() { return fp(4); }

    /**
     * Helper to build a fingerprint hash using the full-print strategy with a given level.
     * <br><br>The full-print strategy extends digit-combos, in that it collects sieve items by
     * removing every combination of {level} digits from the board and checking solutions,
     * and in addition also tries every combination of areas removed from the board, i.e.
     * every combination of {level} rows, columns, and regions are removed and solutions
     * checked for unavoidable sets.
     * <br><br>This collects more sieve items and increases the uniqueness of the fingerprint, as
     * opposed to checking only digit-combos, but adds slightly more processing overhead. And the
     * fingerprint is still guaranteed to remain the same regardless of how the board is transformed.
     * <br><br>Not recommended above level 4.
     * @param level The level of detail (and processing power) used to process and build
     * the fingerprint. Every combination of {level} digits and areas will be removed from the board and
     * solutions collected and checked for unavoidable sets to add to a sieve.
     * @return A hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String fp(int level) { return fp(level, 1); }

    /**
     * Helper to build a fingerprint hash using the full-print strategy with a given level,
     * using the specified number of threads.
     * <br><br>See {@link Sudoku#fp(int level)} for more info on full-print strategy.
     * <br><br>Not recommended above level 4.
     * @param level The level of detail (and processing power) used to process and build
     * the fingerprint. Every combination of {level} digits and areas will be removed from the board and
     * solutions collected and checked for unavoidable sets to add to a sieve.
     * @param numThreads Number of threads to use.
     * @return A hash string.
     * @throws IllegalArgumentException if this grid is not solved.
     */
    public String fp(int level, int numThreads) {
        SudokuSieve sieve = new SudokuSieve(toArray());
        if (numThreads == 1) {
            sieve.seed(sieve.fullPrintCombos(level));
        } else {
            sieve.seedThreaded(sieve.fullPrintCombos(level), numThreads);
        }
        return fpFromSieve(level, sieve);
    }

    /***********************************************
     * TRANSFORMATIONS
     *
     * The following transformations are symmetry-preserving,
     * i.e. they do not change the number of solutions.
     * Transformed grids will retain the same fingerprint.
     *
     * After any transformation, 'constraints' may be out of sync
     * and require reseting via 'resetCandidatesAndValidity()'.
     ***********************************************/

    /**
     * Swaps all of digit {a} on the board with digit {b}.
     * Does nothing if digits are the same, or it either are out of bounds.
     * TODO Ensure constraints are kept in sync.
     * @param a Digit to swap {b} with.
     * @param b Digit to swap {a} with.
     */
    public void swapDigits(int a, int b) {
        if (a == b) return;
        if (a <= 0 || a > 9 || b <= 0 || b > 9) return;
        for (int i = 0; i < SPACES; i++) {
            int d = digits[i];
            if (d == a) {
                setDigit(i, b);
            } else if (d == b) {
                setDigit(i, a);
            }
        }
    }

    /**
     * Rearranges the board digits such that the top row is sequential, 1 through 9.
     * If the grid is not solved, or does not have a unique solution,
     * then this has no effect.
     * TODO Ensure constraints are kept in sync.
     * @return This sudoku.
     */
    public Sudoku normalize() {
        if (!isValid) return this;
        if (isSolved()) {
            copyFrom(normalize(ArraysUtil.copy(this.digits)));
        } else {
            if (solutionsFlag() != 1) return this;
            int[] solutionDigits = normalize(solution().digits);
            for (int i = 0; i < SPACES; i++) {
                if (digits[i] == 0) solutionDigits[i] = 0;
            }
            copyFrom(solutionDigits);
        }
        return this;
    }

    /**
     * Rotates the board clockwise the given number of turns.
     * TODO Ensure constraints are kept in sync.
     * @param turns Number of times to rotate the board.
     * @return This sudoku.
     */
    public Sudoku rotate(int turns) {
        // Normalize turns to be within [0, 3]
        turns = ((turns % 4) + 4) % 4;
        for (int t = 0; t < turns; t++) {
            rotate90(candidates, DIGITS);
            rotate90(digits, DIGITS);
        }
        return this;
    }

    /**
     * Reflects the board values over the horizontal.
     * TODO Ensure constraints are kept in sync.
     * @return This sudoku.
     */
    public Sudoku reflectHorizontal() {
        reflectOverHorizontal(candidates, DIGITS);
        reflectOverHorizontal(digits, DIGITS);
        return this;
    }

    /**
     * Reflects the board values over the vertical.
     * TODO Ensure constraints are kept in sync.
     * @return This sudoku.
     */
    public Sudoku reflectVertical() {
        reflectOverVertical(candidates, DIGITS);
        reflectOverVertical(digits, DIGITS);
        return this;
    }

    /**
     * Reflects the board values over the diagonal.
     * TODO Ensure constraints are kept in sync.
     * @return This sudoku.
     */
    public Sudoku reflectDiagonal() {
        reflectOverDiagonal(candidates, DIGITS);
        reflectOverDiagonal(digits, DIGITS);
        return this;
    }

    /**
     * Reflects the board values over the antidiagonal.
     * TODO Ensure constraints are kept in sync.
     * @return This sudoku.
     */
    public Sudoku reflectAntiDiagonal() {
        reflectOverAntiDiagonal(candidates, DIGITS);
        reflectOverAntiDiagonal(digits, DIGITS);
        return this;
    }

    /**
     * Swaps the specified bands.
     * TODO Ensure constraints are kept in sync.
     * @param bandIndexA Band index (0, 1, or 2).
     * @param bandIndexB Band index, different than A.
     * @return This sudoku.
     */
    public Sudoku swapBands(int bandIndexA, int bandIndexB) {
        swapBands(candidates, bandIndexA, bandIndexB);
        swapBands(digits, bandIndexA, bandIndexB);
        return this;
    }

    /**
     * Swaps the specified rows within a band.
     * TODO Ensure constraints are kept in sync.
     * @param bandIndex Band index (0, 1, or 2).
     * @param rowA Row index (0, 1, or 2).
     * @param rowB Row index , different than A.
     * @return This sudoku.
     */
    public Sudoku swapBandRows(int bandIndex, int rowA, int rowB) {
        swapBandRows(candidates, bandIndex, rowA, rowB);
        swapBandRows(digits, bandIndex, rowA, rowB);
        return this;
    }

    /**
     * Swaps the specified stacks.
     * TODO Ensure constraints are kept in sync.
     * @param stackIndexA Stack index (0, 1, or 2).
     * @param stackIndexB Stack index, different than A.
     * @return This sudoku.
     */
    public Sudoku swapStacks(int stackIndexA, int stackIndexB) {
        swapStacks(candidates, stackIndexA, stackIndexB);
        swapStacks(digits, stackIndexA, stackIndexB);
        return this;
    }

    /**
     * Swaps the specified columns within a stack.
     * TODO Ensure constraints are kept in sync.
     * @param stackIndex Stack index (0, 1, or 2).
     * @param colA Column index (0, 1, or 2).
     * @param colB Column index , different than A.
     * @return This sudoku.
     */
    public Sudoku swapStackCols(int stackIndex, int colA, int colB) {
        swapStackCols(candidates, stackIndex, colA, colB);
        swapStackCols(digits, stackIndex, colA, colB);
        return this;
    }

    /**
     * Scrambles the sudoku via symmetry-preserving transformations. Afterwards, the
     * board will look completely different, but will still be essentially the same.
     * TODO Ensure constraints are kept in sync.
     * @return
     */
    public Sudoku scramble() {
        rotate(ThreadLocalRandom.current().nextInt(4));

        randomizeDigits();

        List<Runnable> transforms = new ArrayList<>(){{
            add(() -> rotate(1));
            add(() -> swapBands(0, 1));
            add(() -> swapBands(0, 2));
            add(() -> swapBands(1, 2));
            add(() -> swapBandRows(0, 0, 1));
            add(() -> swapBandRows(0, 0, 2));
            add(() -> swapBandRows(0, 1, 2));
            add(() -> swapBandRows(1, 0, 1));
            add(() -> swapBandRows(1, 0, 2));
            add(() -> swapBandRows(1, 1, 2));
            add(() -> swapBandRows(2, 0, 1));
            add(() -> swapBandRows(2, 0, 2));
            add(() -> swapBandRows(2, 1, 2));
            add(() -> rotate(2));
            add(() -> swapStacks(0, 1));
            add(() -> swapStacks(0, 2));
            add(() -> swapStacks(1, 2));
            add(() -> swapStackCols(0, 0, 1));
            add(() -> swapStackCols(0, 0, 2));
            add(() -> swapStackCols(0, 1, 2));
            add(() -> swapStackCols(1, 0, 1));
            add(() -> swapStackCols(1, 0, 2));
            add(() -> swapStackCols(1, 1, 2));
            add(() -> swapStackCols(2, 0, 1));
            add(() -> swapStackCols(2, 0, 2));
            add(() -> swapStackCols(2, 1, 2));
            add(() -> rotate(3));

            // These can be achieved through combinations of the above transforms.
            // add(() -> reflectHorizontal());
            // add(() -> reflectVertical());
            // add(() -> reflectDiagonal());
            // add(() -> reflectAntiDiagonal());
        }};

        final int nTransforms = transforms.size();
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int t = 0; t < 137; t++) {
            transforms.get(rand.nextInt(nTransforms)).run();
        }

        return this;
    }

    /**
     * Randomly swaps combinations of digits on the board.
     * Afterwards, the grid will be essentially the same, just with different numbers.
     * TODO Ensure constraints are kept in sync.
     * @return This sudoku.
     */
    public Sudoku randomizeDigits() {
        int[] digits = new int[9];
        for (int d = 1; d <= 9; d++) {
            digits[d - 1] = d;
        }
        ArraysUtil.shuffle(digits);
        for (int d = 1; d <= 9; d++) {
            swapDigits(d, digits[d - 1]);
        }
        return this;
    }

    // End transformations

    /**
     * Filters this grid with the given mask.
     * @param mask A mask indicating which digits to keep in the result.
     * @return A new Sudoku with filtered board values.
     */
    public Sudoku filter(SudokuMask mask) {
        Sudoku result = new Sudoku();
        for (int ci = 0; ci < SPACES; ci++) {
            if (mask.testBit(ci) && digits[ci] > 0) {
                result.setDigit(ci, digits[ci]);
            }
        }
        return result;
    }

    /**
     * Gets a string representation of this grid filtered through the given mask.
     * @param mask A mask indicating which digits to keep in the result.
     * @return A new Sudoku with filtered board values.
     */
    public String filterStr(SudokuMask mask) {
        char[] strb = new char[SPACES];
        for (int ci = 0; ci < SPACES; ci++) {
            strb[ci] = (mask.testBit(ci) && digits[ci] > 0) ? (char)('0' + digits[ci]) : '.';
        }
        return new String(strb);
    }

    /**
     * Gets a mask indicating differences between this sudoku and the one given.
     * @param other Another sudoku.
     * @return A new SudokuMask where bits set indicate a difference between boards.
     */
    public SudokuMask diffMask(Sudoku other) {
        SudokuMask result = new SudokuMask();
        for (int i = 0; i < SPACES; i++) {
            if (digits[i] != other.digits[i]) {
               result.setBit(i);
            }
        }
        return result;
    }

    /**
     * Gets a flag indicating the grid's number of solutions.
     * <ul>
     * <li>0 -> No solutions</li>
     * <li>1 -> Single solution</li>
     * <li>2 -> Multiple solutions</li>
     * </ul>
     * NOTE: This method includes the overhead of solving the grid, but fails fast
     * as soon as 2 solutions are found.
     */
    public int solutionsFlag() {
        if (!isValid) return 0;
        if (numEmptyCells > SPACES - MIN_CLUES) return 2;

        AtomicInteger count = new AtomicInteger();
        searchForSolutions3(_s -> (count.incrementAndGet() < 2));
        return count.get();
    }

    /**
     * Finds and returns the index of an empty cell, or -1 if no empty cells exist.
     * Prioritizes empty cells with the fewest number of candidates. If multiple cells
     * have the fewest number of candidates, chooses one of them at random.
     * @return Index of an empty cell, or -1 if no empty cells exist.
     */
    int pickEmptyCell() {
        return pickEmptyCell(0, SPACES);
    }

    /**
     * Finds and returns the index of an empty cell within a given cell range,
     * or -1 if no empty cells exist.
     * Prioritizes empty cells with the fewest number of candidates. If multiple cells
     * have the fewest number of candidates, chooses one of them at random.
     * @param startIndex Starting cell index of the range to check (inclusive).
     * @param endIndex Ending cell index of the range to check (exclusive).
     * @return Index of an empty cell, or -1 if no empty cells exist.
     */
    // Hoisting this list up actually performs slightly slower, and I'm not sure why...
    // private List<Integer> _minimums = new ArrayList<>();
    public int pickEmptyCell(int startIndex, int endIndex) {
        if (numEmptyCells == 0) return -1;
        if (numEmptyCells == SPACES) return ThreadLocalRandom.current().nextInt(SPACES);

        int min = DIGITS + 1;
        // TODO Compare performance when using int[81] instead of ArrayList
        List<Integer> _minimums = new ArrayList<>();
        for (int ci = startIndex; ci < endIndex; ci++) {
            if (digits[ci] == 0) {
                int numCandidates = BIT_COUNT_MAP[candidates[ci]];
                // This actually seems to run slightly slower...
                // if (numCandidates == 2) {
                //     return ci;
                // }
                if (numCandidates < min) {
                    min = numCandidates;
                    _minimums.clear();
                    _minimums.add(ci);
                } else if (numCandidates == min) {
                    _minimums.add(ci);
                }
            }
        }

        return (!_minimums.isEmpty()) ? _minimums.get(ThreadLocalRandom.current().nextInt(_minimums.size())) : -1;
        // return _minimums.get(RandomGenerator.getDefault().nextInt(_minimums.size()));
    }

    /**
     * Builds a string 81 character length representation of this sudoku.
     * Empty cells will be indicated by a dot (<code>.</code>).
     */
    @Override
    public String toString() {
        char[] chars = new char[SPACES];
        for (int i = 0; i < SPACES; i++) {
            chars[i] = (digits[i] > 0) ? (char)('0' + digits[i]) : '.';
        }
        return new String(chars);
    }

    /**
     * Builds a multiline string representation of this sudoku,
     * including spacing and region borders.
     * @return Multiline string representation of the board.
     */
    public String toFullString() {
        return toFullString(digits);
    }

    /**
     * Builds a multiline string representation of this sudoku, including region borders.
     * @return Multiline string representation of the board.
     */
    public String toMedString() {
        return toMedString(digits);
    }

    /**
     * Builds a byte array representation of this sudoku.
     * For use with <code>new Sudoku(bytesArr)</code>.
     * @return A 41-length byte array containing this sudoku's digit information.
     */
    public byte[] toByteArray() {
        if (numEmptyCells == SPACES) return new byte[41];

        int len = 41;
        byte[] result = new byte[len];
        for (int i = 0; i < len - 1; i++) {
            result[i] = (byte)( ((digits[i*2] & 0xf) << 4) + (digits[i*2 + 1] & 0xf) );
        }
        result[40] = (byte)( ((digits[80] & 0xf) << 4) + 0xf );

        return result;
    }
}
