package io.github.metal_pony.sudoku;

import io.github.metal_pony.sudoku.util.Counting;

/**
 * Contains pre-generated sudoku data for fast lookup.
 */
public final class Constants {
    private Constants() {}

    /** Square root of the number of subdivided areas on a standard sudoku board.*/
    public static final int RANK = 3;
    /** Number of digits in standard 9x9 sudoku.*/
    public static final int DIGITS = 9;
    /** Number of spaces or cells on a standard sudoku board.*/
    public static final int SPACES = 81;
    /** Represents the combination of all candidates for a cell (0x1ff).*/
    public static final int ALL = 511;
    /** Minumum number of clues required for a valid sudoku puzzle.*/
    public static final int MIN_CLUES = 17;

    /** Maps digits (as the index) to a 9-bit encoded values.*/
    public static final int[] ENCODER = {0,1,2,4,8,16,32,64,128,256};

    /**
     * Maps the 9-bit encoded values (as the index) to the associated digit.
     * NOTE: Values that represent more than one digit are mapped to 0.
     * Powers of 2 map to digits 1 through 9.
     */
    public static final int[] DECODER = new int[1<<DIGITS];
    static {
        for (int d = 1; d <= DIGITS; d++) DECODER[1 << (d - 1)] = d;
    }

    /** Maps cell indices to respective rows.*/
    public static final int[] CELL_ROWS = {
        0, 0 ,0,  0, 0 ,0,  0, 0, 0,
        1, 1 ,1,  1, 1 ,1,  1, 1, 1,
        2, 2 ,2,  2, 2 ,2,  2, 2, 2,

        3, 3 ,3,  3, 3 ,3,  3, 3, 3,
        4, 4 ,4,  4, 4 ,4,  4, 4, 4,
        5, 5 ,5,  5, 5 ,5,  5, 5, 5,

        6, 6 ,6,  6, 6 ,6,  6, 6, 6,
        7, 7 ,7,  7, 7 ,7,  7, 7, 7,
        8, 8 ,8,  8, 8 ,8,  8, 8, 8
    };

    /** Maps cell indices to respective columns.*/
    public static final int[] CELL_COLS = {
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
    public static final int[] CELL_REGIONS = {
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
    public static final int[][] ROW_INDICES = {
        { 0,  1,  2,  3,  4,  5,  6,  7,  8},
        { 9, 10, 11, 12, 13, 14, 15, 16, 17},
        {18, 19, 20, 21, 22, 23, 24, 25, 26},
        {27, 28, 29, 30, 31, 32, 33, 34, 35},
        {36, 37, 38, 39, 40, 41, 42, 43, 44},
        {45, 46, 47, 48, 49, 50, 51, 52, 53},
        {54, 55, 56, 57, 58, 59, 60, 61, 62},
        {63, 64, 65, 66, 67, 68, 69, 70, 71},
        {72, 73, 74, 75, 76, 77, 78, 79, 80}
    };

    /** Maps column indices to an array of cell indices (cells in the given column).*/
    public static final int[][] COL_INDICES = {
        { 0,  9, 18, 27, 36, 45, 54, 63, 72},
        { 1, 10, 19, 28, 37, 46, 55, 64, 73},
        { 2, 11, 20, 29, 38, 47, 56, 65, 74},
        { 3, 12, 21, 30, 39, 48, 57, 66, 75},
        { 4, 13, 22, 31, 40, 49, 58, 67, 76},
        { 5, 14, 23, 32, 41, 50, 59, 68, 77},
        { 6, 15, 24, 33, 42, 51, 60, 69, 78},
        { 7, 16, 25, 34, 43, 52, 61, 70, 79},
        { 8, 17, 26, 35, 44, 53, 62, 71, 80}
    };

    /** Maps region indices to an array of cell indices (cells in the given region).*/
    public static final int[][] REGION_INDICES = {
        { 0,  1,  2,  9, 10, 11, 18, 19, 20},
        { 3,  4,  5, 12, 13, 14, 21, 22, 23},
        { 6,  7,  8, 15, 16, 17, 24, 25, 26},
        {27, 28, 29, 36, 37, 38, 45, 46, 47},
        {30, 31, 32, 39, 40, 41, 48, 49, 50},
        {33, 34, 35, 42, 43, 44, 51, 52, 53},
        {54, 55, 56, 63, 64, 65, 72, 73, 74},
        {57, 58, 59, 66, 67, 68, 75, 76, 77},
        {60, 61, 62, 69, 70, 71, 78, 79, 80}
    };

    /** Maps band indices to an array of cell indices (cells in the given band).*/
    public static final int[][] BAND_INDICES = {
        { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26},
        {27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53},
        {54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80}
    };

    /**
     * Maps row indices withing a band to an array of cell indices (cells in the given band's row).
     * <br><br><code>BAND_ROW_INDICES[band 0-2][row 0-2] = [... cell indices]</code>
     */
    public static final int[][][] BAND_ROW_INDICES = {
        {
            { 0, 1, 2, 3, 4, 5, 6, 7, 8},
            { 9,10,11,12,13,14,15,16,17},
            {18,19,20,21,22,23,24,25,26}
        },
        {
            {27,28,29,30,31,32,33,34,35},
            {36,37,38,39,40,41,42,43,44},
            {45,46,47,48,49,50,51,52,53}
        },
        {
            {54,55,56,57,58,59,60,61,62},
            {63,64,65,66,67,68,69,70,71},
            {72,73,74,75,76,77,78,79,80}
        }
    };

    /** Maps stack indices to an array of cell indices (cells in the given stack).*/
    public static final int[][] STACK_INDICES = {
        {0,9,18,27,36,45,54,63,72,1,10,19,28,37,46,55,64,73,2,11,20,29,38,47,56,65,74},
        {3,12,21,30,39,48,57,66,75,4,13,22,31,40,49,58,67,76,5,14,23,32,41,50,59,68,77},
        {6,15,24,33,42,51,60,69,78,7,16,25,34,43,52,61,70,79,8,17,26,35,44,53,62,71,80}
    };

    /**
     * Maps column indices withing a stack to an array of cell indices (cells in the given stack's column).
     * <br><br><code>STACK_COL_INDICES[stack 0-2][col 0-2] = [... cell indices]</code>
     */
    public static final int[][][] STACK_COL_INDICES = {
        {
            {0,9,18,27,36,45,54,63,72},
            {1,10,19,28,37,46,55,64,73},
            {2,11,20,29,38,47,56,65,74}
        },
        {
            {3,12,21,30,39,48,57,66,75},
            {4,13,22,31,40,49,58,67,76},
            {5,14,23,32,41,50,59,68,77}
        },
        {
            {6,15,24,33,42,51,60,69,78},
            {7,16,25,34,43,52,61,70,79},
            {8,17,26,35,44,53,62,71,80}
        }
    };

    /** Maps cells indices to the other cell indices within the same row.*/
    public static final int[][] ROW_NEIGHBORS = new int[SPACES][DIGITS - 1];
    /** Maps cells indices to the other cell indices within the same column.*/
    public static final int[][] COL_NEIGHBORS = new int[SPACES][DIGITS - 1];
    /** Maps cells indices to the other cell indices within the same region.*/
    public static final int[][] REGION_NEIGHBORS = new int[SPACES][DIGITS - 1];
    /** Maps cells indices to all other cell indices within the same row, column, and region.*/
    public static final int[][] CELL_NEIGHBORS = new int[SPACES][3*(DIGITS-1) - (DIGITS-1)/2];

    /** Maps encoded values to the array of individual digits it represents.*/
    public static final int CANDIDATES[][] = new int[1<<DIGITS][];
    /** Maps encoded values to the array of individual digits (encoded) it represents.*/
    public static final int[][] CANDIDATES_ENC = new int[CANDIDATES.length][];
    /** Maps the candidate numbers to string representations (mashes the candidate digits together).*/
    public static final String[] CANDIDATES_STRS = new String[1<<DIGITS];

    /** Maps indices [0, 511] to its bit count.*/
    public static final int[] BIT_COUNT_MAP = new int[1<<DIGITS];
    /** Digit combinations indexed by bit count (aka digit count).*/
    public static final int[][] DIGIT_COMBOS_MAP = new int[DIGITS + 1][];

    static {
        for (int encoded = 0; encoded < CANDIDATES.length; encoded++) {
            CANDIDATES[encoded] = new int[Integer.bitCount(encoded)];
            CANDIDATES_ENC[encoded] = new int[Integer.bitCount(encoded)];
            int _val = encoded;
            int i = 0;
            int j = 0;
            int digit = 1;
            String str = "";
            while (_val > 0) {
                if ((_val & 1) > 0) {
                    CANDIDATES[encoded][i++] = digit;
                    CANDIDATES_ENC[encoded][j++] = ENCODER[digit];
                    str += Integer.toString(digit);
                }
                _val >>= 1;
                digit++;
            }
            CANDIDATES_STRS[encoded] = str;
        }

        for (int nDigits = 0; nDigits < DIGIT_COMBOS_MAP.length; nDigits++) {
            DIGIT_COMBOS_MAP[nDigits] = new int[Counting.nChooseK(DIGITS, nDigits).intValueExact()];
        }
        int[] combosCount = new int[DIGITS + 1];
        for (int i = 0; i < BIT_COUNT_MAP.length; i++) {
            int bits = Integer.bitCount(i);
            BIT_COUNT_MAP[i] = bits;
            DIGIT_COMBOS_MAP[bits][combosCount[bits]++] = i;
        }

        for (int i = 0; i < SPACES; i++) {
            int row = CELL_ROWS[i];
            int col = CELL_COLS[i];
            int region = CELL_REGIONS[i];

            int ri = 0;
            int coli = 0;
            int regi = 0;
            int ni = 0;

            for (int j = 0; j < SPACES; j++) {
                if (i == j) continue;
                int jrow = CELL_ROWS[j];
                int jcol = CELL_COLS[j];
                int jregion = CELL_REGIONS[j];

                if (jrow == row) ROW_NEIGHBORS[i][ri++] = j;
                if (jcol == col) COL_NEIGHBORS[i][coli++] = j;
                if (jregion == region) REGION_NEIGHBORS[i][regi++] = j;
                if (jrow == row || jcol == col || jregion == region) CELL_NEIGHBORS[i][ni++] = j;
            }
        }
    }
}
