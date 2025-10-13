package io.github.metal_pony.sudoku;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.metal_pony.sudoku.util.ArraysUtil;
import io.github.metal_pony.sudoku.util.Counting;

import io.github.metal_pony.sudoku.GeneratedPuzzles;
import io.github.metal_pony.sudoku.PuzzleEntry;
import io.github.metal_pony.sudoku.Sudoku;
import io.github.metal_pony.sudoku.SudokuSieve;

public class TestSudoku {

    final Class<NullPointerException> nullErr = NullPointerException.class;
    final Class<IllegalArgumentException> argErr = IllegalArgumentException.class;

    @Nested
    class Static {
        void cellRow() {}
        void cellCol() {}
        void cellRegion() {}

        void isRowFull() {}
        void cisClFull() {}
        void isRegionFull() {}
        void isFull() {}

        void isRowValid() {}
        void isColValid() {}
        void isRegionValid() {}
        void isValid() {}

        void isSolved() {}

        void isValidStr() {}

        @Test
        void rotate90() {
            // When arr is null, throws NullPointerException
            assertThrows(nullErr, () -> { Sudoku.rotate90(null, 1); });

            // When arr is empty, does nothing
            int[] arr = new int[0];
            int[] actual = Sudoku.rotate90(arr, 0);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, actual));

            // When arr is not square, throws IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.rotate90(new int[2], 1); });
            assertThrows(argErr, () -> { Sudoku.rotate90(new int[3], 1); });
            assertThrows(argErr, () -> { Sudoku.rotate90(new int[5], 2); });
            assertThrows(argErr, () -> { Sudoku.rotate90(new int[99], 9); });

            // Otherwise, rotates array as expected
            arr = new int[]{1, 2, 3, 4};
            actual = Sudoku.rotate90(arr, 2);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{3, 1, 4, 2}));

            arr = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
            actual = Sudoku.rotate90(arr, 3);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{7, 4, 1, 8, 5, 2, 9, 6, 3}));
        }

        @Test
        void reflectOverHorizontal() {
            // When arr is null, throws NullPointerException
            assertThrows(nullErr, () -> { Sudoku.reflectOverHorizontal(null, 0); });

            // When rows is 0 or negative, throw IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverHorizontal(new int[1], 0); });
            assertThrows(argErr, () -> { Sudoku.reflectOverHorizontal(new int[1], -1); });

            // When arr is not divisible by rows, throws IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverHorizontal(new int[3], 2); });
            assertThrows(argErr, () -> { Sudoku.reflectOverHorizontal(new int[4], 3); });
            assertThrows(argErr, () -> { Sudoku.reflectOverHorizontal(new int[9], 4); });
            assertThrows(argErr, () -> { Sudoku.reflectOverHorizontal(new int[99], 10); });


            // When rows < 2, does nothing
            int[] arr = new int[]{1,2,3,4,5};
            int[] actual = Sudoku.reflectOverHorizontal(arr, 1);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(new int[]{1,2,3,4,5}, actual));

            // Otherwise, reflects array as expected
            arr = new int[]{
                1, 2,
                3, 4
            };
            actual = Sudoku.reflectOverHorizontal(arr, 2);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                3, 4,
                1, 2
            }));

            arr = new int[]{
                1, 2,
                3, 4,
                5, 6,
                7, 8,
                9, 10
            };
            actual = Sudoku.reflectOverHorizontal(arr, 5);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                9, 10,
                7, 8,
                5, 6,
                3, 4,
                1, 2
            }));
        }

        @Test
        void reflectOverVertical() {
            // When arr is null, throws NullPointerException
            assertThrows(nullErr, () -> { Sudoku.reflectOverVertical(null, 0); });

            // When rows is 0, throw IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverVertical(new int[1], 0); });
            assertThrows(argErr, () -> { Sudoku.reflectOverVertical(new int[1], -1); });

            // When arr is not divisible by rows, throws IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverVertical(new int[3], 2); });
            assertThrows(argErr, () -> { Sudoku.reflectOverVertical(new int[4], 3); });
            assertThrows(argErr, () -> { Sudoku.reflectOverVertical(new int[9], 4); });
            assertThrows(argErr, () -> { Sudoku.reflectOverVertical(new int[99], 10); });

            // When cols (arr.length / rows) < 2, does nothing
            int[] arr = new int[]{1,2,3,4,5};
            int[] actual = Sudoku.reflectOverVertical(arr, 5);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(new int[]{1,2,3,4,5}, actual));

            // Otherwise, reflects array as expected
            arr = new int[]{
                1, 2,
                3, 4
            };
            actual = Sudoku.reflectOverVertical(arr, 2);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                2, 1,
                4, 3
            }));

            arr = new int[]{
                1, 2,
                3, 4,
                5, 6,
                7, 8,
                9, 10
            };
            actual = Sudoku.reflectOverVertical(arr, 5);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                2, 1,
                4, 3,
                6, 5,
                8, 7,
                10, 9
            }));
        }

        @Test
        void reflectOverDiagonal() {
            // When arr is null, throws NullPointerException
            assertThrows(nullErr, () -> { Sudoku.reflectOverDiagonal(null, 1); });

            // When arr is empty, throws IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[1], 0); });
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[1], -1); });

            // When arr is not square, throws IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[2], 1); });
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[3], 2); });
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[5], 2); });
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[99], 10); });

            // Otherwise, rotates array as expected
            int[] arr = new int[]{
                1, 2,
                3, 4
            };
            int[] actual = Sudoku.reflectOverDiagonal(arr, 2);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                4, 2,
                3, 1
            }));

            arr = new int[]{
                1,  2,  3,  4,
                5,  6,  7,  8,
                9, 10, 11, 12,
                13, 14, 15, 16
            };
            actual = Sudoku.reflectOverDiagonal(arr, 4);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                16, 12,  8,  4,
                15, 11,  7,  3,
                14, 10,  6,  2,
                13,  9,  5,  1
            }));
        }

        @Test
        void reflectOverAntiDiagonal() {
            // When arr is null, throws NullPointerException
            assertThrows(nullErr, () -> { Sudoku.reflectOverAntiDiagonal(null, 1); });

            // When arr is empty, throws IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[1], 0); });
            assertThrows(argErr, () -> { Sudoku.reflectOverDiagonal(new int[1], -1); });

            // When arr is not square, throws IllegalArgumentException
            assertThrows(argErr, () -> { Sudoku.reflectOverAntiDiagonal(new int[2], 2); });
            assertThrows(argErr, () -> { Sudoku.reflectOverAntiDiagonal(new int[3], 3); });
            assertThrows(argErr, () -> { Sudoku.reflectOverAntiDiagonal(new int[5], 5); });
            assertThrows(argErr, () -> { Sudoku.reflectOverAntiDiagonal(new int[99], 99); });

            // Otherwise, rotates array as expected
            int[] arr = new int[]{
                1, 2,
                3, 4
            };
            int[] actual = Sudoku.reflectOverAntiDiagonal(arr, 2);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                1, 3,
                2, 4
            }));

            arr = new int[]{
                1,  2,  3,  4,
                5,  6,  7,  8,
                9, 10, 11, 12,
                13, 14, 15, 16
            };
            actual = Sudoku.reflectOverAntiDiagonal(arr, 4);
            assertTrue(arr == actual);
            assertTrue(Arrays.equals(arr, new int[]{
                1,  5,  9, 13,
                2,  6, 10, 14,
                3,  7, 11, 15,
                4,  8, 12, 16
            }));
        }

        @Test
        void generatePuzzle_whenSieveHasItems_butGridNull_throws() {
            assertThrows(IllegalArgumentException.class, () -> {
                Sudoku.generatePuzzle(null, 27, new SudokuSieve(Sudoku.generateConfig()), 0, 0L, true);
            });
        }

        @Test
        void generatePuzzle_whenNumCluesLessThanMin_returnsNull() {
            for (int clues = 16; clues > -100; clues--) {
                assertNull(
                    Sudoku.generatePuzzle(null, clues, null, 0, 0L, true)
                );
            }
        }

        @Test
        void generatePuzzles_whenNumCluesMoreThanSpaces_returnsFullGrid() {
            for (int clues = 82; clues < 100; clues++) {
                Sudoku p = Sudoku.generatePuzzle(null, clues, null, 0, 0L, true);
                assertTrue(p.isFull());
                assertEquals(0, p.numEmptyCells());
            }
        }

        @Test
        void generatePuzzles_whenGridInvalid_throws() {
            assertThrows(IllegalArgumentException.class, () -> {
                Sudoku.generatePuzzle(
                    new Sudoku("999999999648937152957182364435279618296813475781645293364798521812564937579321846"),
                    27, null, 0, 0L, true
                );
            });
        }

        @Test
        void generatePuzzles_whenDifficultyOutOfRange_throws() {
            int[] invalidDiffs = new int[]{-10, -5, -3, -2, -1, 5, 6, 7, 8, 9, 10};
            for (int invalidDiff : invalidDiffs) {
                final int _invalidDiff = invalidDiff;
                assertThrows(IllegalArgumentException.class, () -> {
                    Sudoku.generatePuzzle(null, 27, null, _invalidDiff, 0L, true);
                });
            }
        }

        @Test
        void generatePuzzles_stressTest() {
            for (int clues = 81; clues >= 24; clues--) {
                for (int t = 0; t < 10; t++) {
                    Sudoku p = Sudoku.generatePuzzle(clues);
                    int[] board = p.getBoard();

                    // Validity
                    assertTrue(Sudoku.isValid(board));

                    // Has expected number of clues
                    int digitCount = 0;
                    for (int boardValue : board) {
                        if (boardValue > 0) {
                            digitCount++;
                        }
                    }
                    assertEquals(clues, digitCount);

                    // Has single solution
                    assertEquals(1, p.solutionsFlag());
                }
            }
        }
    }

    private final String configFixtureStr = "218574639573896124469123578721459386354681792986237415147962853695318247832745961";
    private Sudoku configFixture;
    private SudokuSieve configFixtureSieve;

    // NOTE: level 4 fingerprints currently take several seconds to generate,
    //  so they are disabled for CI checks.
    private String[][] configFixtureFingerprints = new String[][]{
        {"dc2", "9:9:7:4:2:3::16"},
        {"dc3", "9::f::f:1:11:5:f:5:f:c:f:9:3d:e:16:25:21:8:8"},
        // {"dc4", "9::f::18:6:3b:16:43:2b:75:63:9c:a8:14a:139:1b6:29c:352:3cc:49b:460:4a4:350:251:14e:6a:21:1"},
        {"ac2", "9:6:4:9:1:4::6"},
        {"ac3", "9::f::11:5:2a:8:2c:3:2e:8:12:b:30:6:11:7:1"},
        // {"ac4", "9::f::18:6:4f:1e:7e:43:d2:75:f5:d0:138:ff:18f:11c:15e:10a:f4:b7:72:3d:17:a:4:1"},
        {"fp2", "9:f:b:d:3:7::1c"},
        {"fp3", "9::f::18:6:3b:d:3b:8:3d:14:21:14:6d:14:27:2c:22:8:8"},
        // {"fp4", "9::f::18:6:53:23:9b:65:13b:d8:191:178:282:238:345:3b8:4b0:4d6:58f:517:516:38d:268:158:6e:22:1"},
    };

    private final String puzzleFixtureStr = "1.......945...71..9.7..23...3.2.9....9....57..8.......3.......1..26.5....79......";
    private Sudoku puzzleFixture;
    private String[] puzzleSolutions;

    private HashMap<String,Integer> PUZZLESTRS_TO_NUM_SOLUTIONS = new HashMap<>() {{
        put("...45.7...5........4......3.8...3.1.9..241..85.69...3.2..3...7.3...7..........3..", 1463);
        put("....5..89..8...16......1..2..76.3..............1..5..45...6..73.......4..74..89.1", 2361);
        // put("..3.5.7.9..7..8.4...8............8.6.8...54.2...8..........932.3.42..6......3.1..", 25339);
        put("12..5..8..7.3.9........7..6...56..9.....4.8......92..1....2...8.6.1.......8...6.5", 996);
        put("1..45...96...1......7...1..3......5.9....531.......6...9.16.......3.4.6.2...7...1", 5076);
        put(".2..56...8..3..56........3..1.2...........64.....9..239.........81.2....26..314..", 3171);
        put(".2.4.6..99...........79213.........1..9...3.........5.3.8....72...5......65.29..4", 4004);
        // put("....5..89....3.......2...........9..2......75..9.8.6.2.51...8.6....9...1.92..1.57", 7535);
        put("....5.7..56...8.4...9.7..61...6.....65...94.8..4....2.4.....836.3...7............", 1509);
        put("....56...76....52..95.2...3.......7.2.78...455...9.1...3.....5...8...3.......5...", 2132);
        put("..3.....9.7....65...9.71.345.1..78..9.43.2......54.......9..3............4.1.....", 322);
        // put("...4..7......1...6.........3.....8..7.584......8.3..6.5...7....43...51.897.1...3.", 27462);
        put("1.......9.......4...4...2....2.....8..92..4.14.8....9..365...1.8.....5.6..56.8...", 5338);
        put("...45.................8..1..1...4...63......8..8...195...7..8.1.5..9.3.48.16...5.", 1589);
        // put(".....6..9........2.84.97....1...23...9..85..62...61.4.3...2........38..4.....4...", 8244);
        // put("..3..6........8..69.67..1..5.....96.8.9.....7.67....1....8.....4.8...6......94..3", 25661);
        put("..3.5.7.....2......4891..6.812.3.........5....9..8...........252.5.....1.795.....", 448);
        put("1..4..7.9......3...75.8.6143........8.43...6......4...2..1....6..8.........9.5..2", 3383);
        // put("...4..7.9...7.8....681...4.....1.9....6......931.4.....8.2.4...2...6..7....3...9.", 7506);
        put(".2.......9.6.175..........34.....961.....5....7.9.4.......42...237.8...5....3..2.", 243);
    }};

    private String[] invalidPuzzles = new String[]{
        // Invalid because of digit clashing (col 7: 3...5.3.1)
        "1.2....3..9..1.........3..1.15...4...2.1...5.8.....1.6..1....3......1...2......1.",
        // Invalid because cell 0 has no candidates
        ".123456789...1...........1.1...........1...........1....1...........1...........1"
    };

    @BeforeEach
    void before() {
        configFixture = new Sudoku(configFixtureStr);
        configFixtureSieve = new SudokuSieve(configFixture.getBoard());
        puzzleFixture = new Sudoku(puzzleFixtureStr);
        puzzleSolutions = copyPuzzleFixtureSolutions();
        Arrays.sort(puzzleSolutions);
    }

    @Test
    void testThing() {
        populateSieveForAllDigitCombos(2);

        SudokuMask[] expectedItems = new SudokuMask[] {
            new SudokuMask("001000001000000000001000001000000000000000000000000000000000000000000000000000000"),
            new SudokuMask("000000000000000000000000000100001000000000000100001000000000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000000000000000000001100000001100000000000"),
            new SudokuMask("101000000000000000000000000000000000000000000000000000000000000000000000101000000"),
            new SudokuMask("000000000000000000000000000000000000000011000000000000000000000000011000000000000"),
            new SudokuMask("000000000000000000000000000100000100100000100000000000000000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000101000000000000000000000000101000000000"),
            new SudokuMask("000000000000000000000000000101000000000000000000000000101000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000000000000000100010000100010000000000000"),
            new SudokuMask("000000000000000000011000000000000000000000000101000000000000000110000000000000000"),
            new SudokuMask("000000000000000000000000000000000000100010000010010000000000000000000000110000000"),
            new SudokuMask("011000000000000000000000000001000010000000000010000010000000000000000000000000000"),
            new SudokuMask("000010010000000000000001010000000000000000000000011000000000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000000000101000001001000000000000001100000"),
            new SudokuMask("000000000000000000000000000001000001000000000001000010000000000000000000000000011"),
            new SudokuMask("000000000100000001100000100000000000000000000000000101000000000000000000000000000"),
            new SudokuMask("000101000000000000000000000000110000000000000000000000000000000000000000000011000"),
            new SudokuMask("000000000000000000000000000000000000011000000000000000010000010001000010000000000"),
            new SudokuMask("001100000100100000000000000000000000000000000000000000000000000001001000100001000"),
            new SudokuMask("000000000000100100000100001000000000000000000000000000100000100000000000100000001"),
            new SudokuMask("000000000011000000000000000000000000000000000000000000001000001000100001010100000"),
            new SudokuMask("100010000010000010000010010110000000000000000000000000000000000000000000000000000"),
            new SudokuMask("000000000000000000110000000000000000000000000000000000010010000100000010000010010"),
            new SudokuMask("010000100000001100010100000000000000000101000000000000000000000000000000000000000"),
            new SudokuMask("000000000000000101000000000000000000000000000000000110000000000000010010000010001"),
            new SudokuMask("000000000000000000000000101000010010010010000010000001000000110000000000000000000"),
            new SudokuMask("000000000000100010000010001010000010000010001010100000000000000000000000000000000"),
            new SudokuMask("000001100000001001000000000000100001001100000001000100000000000000000000000000000"),
            new SudokuMask("010001000000000000100100000001100000001001000000000000110000000000000000000000000"),
            new SudokuMask("000000101000011000000000000000001001000100010000000000000110000000000000000000110"),
            new SudokuMask("001000010001100000000001001000000110000000000000000000000000101000101000000000000"),
            new SudokuMask("000000000000110000000000000000001010000010010110000000000100100010001000100000100"),
            new SudokuMask("000010001010010000001000010000000000000000110000000000001100000010000001000100100"),
            new SudokuMask("010010000010000100000100010000000000000001100000001010000000000000010001000100001"),
            new SudokuMask("000100001100010000001000100000011000010000010100000001000100010011000000000001100"),
            new SudokuMask("000001001000010001101000000000101000001000010100000100010100000010000010000010100"),
            new SudokuMask("000000011001010000001001000000001100100000010100010000000100001010100000010000100"),
            new SudokuMask("100000001000010010001010000010001000000000011100100000000101000010000100001000100"),
            new SudokuMask("010000001000010100001100000001001000000001010100000010100100000010010000000000101"),
            new SudokuMask("001010000010100000000000011100000010000010100010001000001000100000001001100100000"),
            new SudokuMask("001000100000101000010000001000000011000110000011000000000010100100001000100000010"),
            new SudokuMask("001001000000100001100000001000100010001010000010000100010000100000001010100010000"),
            new SudokuMask("000010100010001000010000010100000001000100100001001000001010000100000001000100010"),
            new SudokuMask("000110000110000000000000110100010000010000100000001001001000010001000001000101000"),
            new SudokuMask("000011000010000001100000010100100000001000100000001100011000000000000011000110000"),
            new SudokuMask("000100100100001000010000100000010001010100000001000001000010010101000000000001010"),
            new SudokuMask("000000110001001000010001000000000101100100000001010000000010001100100000010000010"),
            new SudokuMask("100000100000001010010010000010000001000100001001100000000011000100000100001000010"),
            new SudokuMask("000100010101000000000001100000010100110000000000010001000000011001100000010001000"),
            new SudokuMask("100100000100000010000010100010010000010000001000100001000001010001000100001001000"),
            new SudokuMask("010100000100000100000100100001010000010001000000000011100000010001010000000001001"),
            new SudokuMask("000001010001000001100001000000100100101000000000010100010000001000100010010010000"),
            new SudokuMask("100001000000000011100010000010100000001000001000100100010001000000000110001010000"),
            new SudokuMask("100000010001000010000011000010000100100000001000110000000001001000100100011000000"),
            new SudokuMask("010000010001000100000101000001000100100001000000010010100000001000110000010000001"),
            new SudokuMask("110000000000000110000110000011000000000001001000100010100001000000010100001000001"),
        };

        SudokuMask[] actualItems = new SudokuMask[configFixtureSieve.size()];
        int i = 0;
        for (SudokuMask item : configFixtureSieve.items()) {
            actualItems[i++] = item;
        }
        Arrays.sort(actualItems);
        Arrays.sort(expectedItems);
        assertArrayEquals(expectedItems, actualItems);
    }

    @Test
    void puzzleByteBreakdownAndRehydration() {
        PuzzleEntry[] sudoku17 = PuzzleEntry.all17();
        int i = 0;
        for (PuzzleEntry pEntry : sudoku17) {
            Sudoku p1 = pEntry.puzzle();

            byte[] bytes = p1.toBytes();
            Sudoku p2 = new Sudoku(bytes);

            assertEquals(p1.toString(), p2.toString());
            i++;
        }
        // System.out.printf("Read %d 17-clues puzzles, and tested byte representations on each.\n", sudoku17.length);
        assertEquals(sudoku17.length, i);
    }

    @Test
    void fingerprints() {
        for (String[] algo : configFixtureFingerprints) {
            String name = algo[0];
            String expectedPrint = algo[1];
            String actualFp;
            try {
                actualFp = (String)Sudoku.class.getMethod(name, new Class<?>[0]).invoke(configFixture, new Object[0]);
                // Output for debugging
                // boolean matches = expectedPrint.equals(actualFp);
                // System.out.printf("CHECKING %s(): %s ", name, actualFp);
                // System.out.println(matches ? "✅" : "❌ " + expectedPrint);
                assertEquals(actualFp, expectedPrint);
            } catch (
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException |
                NoSuchMethodException |
                SecurityException e
            ) {
                e.printStackTrace();
                fail(String.format("failed to run fingerprint algorithm \"%s\", listed in test class", name));
            }
        }
    }

    // @Test
    // NOTE: This takes a little while, so it's disabled for CI checks.
    void fingerprint_afterShuffling_isAlwaysTheSame() {
        int numShuffles = 100;
        Sudoku grid = Sudoku.generateConfig();
        String expectedLv2 = grid.fp2();
        String expectedLv3 = grid.fp3();

        int count = 0;
        int slice = numShuffles / 80;
        System.out.printf("[%s]\n[", "=".repeat(80));
        for (int t = 0; t < numShuffles; t++) {
            if (++count == slice) {
                System.out.print('.');
                count -= slice;
            }

            grid.scramble();
            assertEquals(expectedLv2, grid.fp2());
            assertEquals(expectedLv3, grid.fp3());
        }
        System.out.println("]");
    }

    @Test
    void test_searchForSolutions3_withKnownInvalidPuzzles_findsNoSolutions() {
        for (String invalidStr : invalidPuzzles) {
            Sudoku p = new Sudoku(invalidStr);
            ArrayList<Sudoku> solutionSet = new ArrayList<>();
            long timeStart = System.currentTimeMillis();
            p.searchForSolutions3(s -> {
                solutionSet.add(s);
                return true;
            });
            long timeEnd = System.currentTimeMillis();
            assertEquals(0, solutionSet.size());
            assertTrue(
                (timeEnd - timeStart) < 1000L,
                String.format("Expected no solutions to be found within 1s. Took %dms", (timeEnd - timeStart))
            );

            assertNull(p.solution());

            assertEquals(0, p.solutionsFlag());
        }
    }

    @Test
    void test_solutionIterator_withKnownInvalidPuzzles_findsNoSolutions() {
        // Invalid puzzles, no solutions
        for (String invalidStr : invalidPuzzles) {
            Sudoku p = new Sudoku(invalidStr);
            Set<String> solutionSet = new HashSet<>();
            int countSolutions = 0;
            long timeStart = System.currentTimeMillis();
            for (Sudoku solution : p.solutions()) {
                countSolutions++;
                solutionSet.add(solution.toString());
            }
            long timeEnd = System.currentTimeMillis();
            assertEquals(0, solutionSet.size());
            assertEquals(0, countSolutions);
            assertTrue(
                (timeEnd - timeStart) < 1000L,
                String.format("Expected no solutions to be found within 1s. Took %dms", (timeEnd - timeStart))
            );
        }
    }

    @Test
    void test_searchForSolutions3_withKnownValidPuzzle_findsSolution() {
        Sudoku p = new Sudoku("...8.1..........435............7.8........1...2..3....6......75..34........2..6..");
        ArrayList<String> solutionSet = new ArrayList<>();
        p.searchForSolutions3(s -> {
            // System.out.println(s.toString());
            solutionSet.add(s.toString());
            return true;
        });
        assertEquals(1, solutionSet.size());
        assertEquals(
            "237841569186795243594326718315674892469582137728139456642918375853467921971253684",
            solutionSet.get(0)
        );

        for (Entry<String,Integer> entry : PUZZLESTRS_TO_NUM_SOLUTIONS.entrySet()) {
            p = new Sudoku(entry.getKey());
            int expectedNumSolutions = entry.getValue();
            solutionSet.clear();
            p.searchForSolutions3(s -> solutionSet.add(s.toString()));
            assertEquals(expectedNumSolutions, solutionSet.size());
        }
    }

    @Test
    void test_solutionIterator_withKnownValidPuzzle_findsSolution() {
        // 17-clue puzzle, single solution
        Set<String> solutionSet = new HashSet<>();
        int countSolutions = 0;
        Sudoku puzzle = new Sudoku("...8.1..........435............7.8........1...2..3....6......75..34........2..6..");
        String knownSolution = "237841569186795243594326718315674892469582137728139456642918375853467921971253684";
        for (Sudoku solution : puzzle.solutions()) {
            countSolutions++;
            solutionSet.add(solution.toString());
            assertEquals(knownSolution, solution.toString());
        }
        assertEquals(1, solutionSet.size());
        assertEquals(1, countSolutions);
    }

    @Test
    void test_solutionIterator_withKnownValidPuzzles_findsSolution() {
        // Pre-generated puzzles from file, single solutions.
        Set<String> solutionSet = new HashSet<>();
        for (String puzzleStr : GeneratedPuzzles.PUZZLES_24_1000) {
            Sudoku puzzle = new Sudoku(puzzleStr);
            int countSolutions = 0;
            solutionSet.clear();
            for (Sudoku solution : puzzle.solutions()) {
                countSolutions++;
                solutionSet.add(solution.toString());
            }
            assertEquals(1, solutionSet.size());
            assertEquals(1, countSolutions);
        }
    }

    @Test
    void test_solutionIterator_withKnownValidPuzzles_findsAllSolution() {
        // Puzzles with multiple solutions
        Set<String> solutionSet = new HashSet<>();
        for (Entry<String,Integer> entry : PUZZLESTRS_TO_NUM_SOLUTIONS.entrySet()) {
            int countSolutions = 0;
            solutionSet.clear();
            int expectedNumSolutions = entry.getValue();
            Sudoku p = new Sudoku(entry.getKey());
            for (Sudoku solution : p.solutions()) {
                countSolutions++;
                solutionSet.add(solution.toString());
            }
            assertEquals(expectedNumSolutions, solutionSet.size());
            assertEquals(expectedNumSolutions, countSolutions);
        }
    }

    @Test
    void test_searchForSolutionsAsync_withKnownValidPuzzle_findsSolution() {
        Sudoku p = new Sudoku("...8.1..........435............7.8........1...2..3....6......75..34........2..6..");
        List<Sudoku> solutionSet = Collections.synchronizedList(new ArrayList<>());
        p.searchForSolutionsAsync(solutionSet::add, 4, 1000L);
        assertEquals(1, solutionSet.size());
        assertEquals(
            "237841569186795243594326718315674892469582137728139456642918375853467921971253684",
            solutionSet.get(0).toString()
        );
    }

    @Test
    void test_searchForSolutionsAsync_findsCorrectNumberOfSolutions() {
        for (Entry<String,Integer> entry : PUZZLESTRS_TO_NUM_SOLUTIONS.entrySet()) {
            Sudoku puzzle = new Sudoku(entry.getKey());
            int expectedNumSolutions = entry.getValue();
            Vector<Sudoku> solutionSet = new Vector<>();
            puzzle.searchForSolutionsAsync(solutionSet::add, 64, 1000L);
            assertEquals(expectedNumSolutions, solutionSet.size());
        }
    }

    @Test
    void searchForSolutions3() {
        HashSet<String> solutionSet = new HashSet<>();
        puzzleFixture.searchForSolutions3(s -> {
            solutionSet.add(s.normalize().toString());
            return true;
        });
        String[] foundSolutions = solutionSet.toArray(new String[solutionSet.size()]);
        Arrays.sort(foundSolutions);
        Arrays.sort(puzzleSolutions);
        assertArrayEquals(puzzleSolutions, foundSolutions);

        PUZZLESTRS_TO_NUM_SOLUTIONS.forEach((puzzleStr, expectedNumSolutions) -> {
            solutionSet.clear();
            new Sudoku(puzzleStr).searchForSolutions3(s -> {
                solutionSet.add(s.toString());
                return true;
            });
            assertEquals(expectedNumSolutions, solutionSet.size());
        });
    }

    @Test
    void searchForSolutionsAsync_forBlankPuzzle_hitsTimeLimit() {
        assertFalse(new Sudoku().searchForSolutionsAsync(s -> {}, 1, 250L));
        // assertFalse(new Sudoku().searchForSolutionsAsync(s -> {}, 4, 333L));
        assertFalse(new Sudoku().searchForSolutionsAsync(s -> {}, 8, 250L));
    }

    @Test
    void searchForSolutionsAsync_findsAllSolutions() {
        for (int numThreads : new int[]{1, 8}) {
            Set<String> solutionSet = Collections.synchronizedSet(new HashSet<>());
            AtomicInteger solutionCount = new AtomicInteger();
            Arrays.sort(puzzleSolutions);

            assertTrue(
                puzzleFixture.searchForSolutionsAsync(s -> {
                    solutionSet.add(s.normalize().toString());
                    solutionCount.incrementAndGet();
                }, numThreads, TimeUnit.MINUTES.toMillis(1L))
            );
            assertEquals(puzzleSolutions.length, solutionCount.get());
            String[] foundSolutions = solutionSet.toArray(new String[solutionSet.size()]);
            Arrays.sort(foundSolutions);
            assertArrayEquals(puzzleSolutions, foundSolutions);

            PUZZLESTRS_TO_NUM_SOLUTIONS.forEach((puzzleStr, expectedNumSolutions) -> {
                solutionSet.clear();
                solutionCount.set(0);
                assertTrue(
                    new Sudoku(puzzleStr).searchForSolutionsAsync(s -> {
                        solutionSet.add(s.toString());
                        solutionCount.incrementAndGet();
                    }, numThreads, TimeUnit.MINUTES.toMillis(1L))
                );
                assertEquals(expectedNumSolutions, solutionCount.get());
                assertEquals(expectedNumSolutions, solutionSet.size());
            });
        }
    }

    @Test
    void countSolutions() {
        // Invalid puzzles (0 solutions)
        for (String p : invalidPuzzles) {
            assertEquals(0, new Sudoku(p).countSolutions());
        }

        // Invalid puzzles (multiple solutions)
        for (String p : PUZZLESTRS_TO_NUM_SOLUTIONS.keySet()) {
            assertEquals(
                PUZZLESTRS_TO_NUM_SOLUTIONS.get(p).intValue(),
                new Sudoku(p).countSolutions()
            );
        }

        // Valid sudoku puzzles (single solutions)
        for (String p : GeneratedPuzzles.PUZZLES_24_1000) {
            assertEquals(1, new Sudoku(p).countSolutions());
        }
    }

    @Test
    void countSolutionsAsync() {
        // Repeat of test above, but with the async method.
        // Invalid puzzles (0 solutions)
        for (String p : invalidPuzzles) {
            assertEquals(0L, new Sudoku(p).countSolutionsAsync(1));
            // assertEquals(0L, new Sudoku(p).countSolutionsAsync(2));
            assertEquals(0L, new Sudoku(p).countSolutionsAsync(4));
        }

        // Valid sudoku puzzles (single solutions)
        for (String pStr : GeneratedPuzzles.PUZZLES_24_1000) {
            assertEquals(1L, new Sudoku(pStr).countSolutionsAsync(1));
            // assertEquals(1L, new Sudoku(pStr).countSolutionsAsync(2));
            assertEquals(1L, new Sudoku(pStr).countSolutionsAsync(4));
        }

        // Valid puzzles (multiple solutions)
        for (String pStr : PUZZLESTRS_TO_NUM_SOLUTIONS.keySet()) {
            Sudoku puzzle = new Sudoku(pStr);
            int expectedCount = PUZZLESTRS_TO_NUM_SOLUTIONS.get(pStr).intValue();
            assertEquals(expectedCount, puzzle.countSolutionsAsync(1));
            // assertEquals(expectedCount, puzzle.countSolutionsAsync(2));
            assertEquals(expectedCount, puzzle.countSolutionsAsync(4));
        }
    }

    // @Test
    void sieveFindsAllExpectedMasks() {
        populateSieveForAllDigitCombos(3);

        SudokuMask[] expectedItems = new SudokuMask[] {
            new SudokuMask("000000000000000000000000000100001000000000000100001000000000000000000000000000000"),
            new SudokuMask("001000001000000000001000001000000000000000000000000000000000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000000000000000000001100000001100000000000"),
            new SudokuMask("101000000000000000000000000000000000000000000000000000000000000000000000101000000"),
            new SudokuMask("000000000000000000000000000000000000000011000000000000000000000000011000000000000"),
            new SudokuMask("000000000000000000000000000100000100100000100000000000000000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000101000000000000000000000000101000000000"),
            new SudokuMask("000000000000000000000000000101000000000000000000000000101000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000000000000000100010000100010000000000000"),
            new SudokuMask("000000000000000000011000000000000000000000000101000000000000000110000000000000000"),
            new SudokuMask("000000000000110000000000000000000000000110000000000000000110000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000100010000010010000000000000000000000110000000"),
            new SudokuMask("011000000000000000000000000001000010000000000010000010000000000000000000000000000"),
            new SudokuMask("000010010000000000000001010000000000000000000000011000000000000000000000000000000"),
            new SudokuMask("000000000000000000000000000000000000000000000000101000001001000000000000001100000"),
            new SudokuMask("000000000000000000000000000001000001000000000001000010000000000000000000000000011"),
            new SudokuMask("000000000000000000000000000000000000011000000000000000010000010001000010000000000"),
            new SudokuMask("000101000000000000000000000000110000000000000000000000000000000000000000000011000"),
            new SudokuMask("000000000100000001100000100000000000000000000000000101000000000000000000000000000"),
            new SudokuMask("000111000000000000000000000000000000000000000000000000000000000000000000000111000"),
            new SudokuMask("000110000000000000000000000000110000000000000000000000000000000000000000000110000"),
            new SudokuMask("010100100000000000010100100000000000000000000000000000000000000000000000000000000"),
            new SudokuMask("000000000000000110000000000000000000000000000000000110000000000000000110000000000"),
            new SudokuMask("000000000000000000000110000000000000000000000000110000000000000000110000000000000"),
            new SudokuMask("001100000100100000000000000000000000000000000000000000000000000001001000100001000"),
            new SudokuMask("000000000000100100000100001000000000000000000000000000100000100000000000100000001"),
            new SudokuMask("000000000011000000000000000000000000000000000000000000001000001000100001010100000"),
            new SudokuMask("100010000010000010000010010110000000000000000000000000000000000000000000000000000"),
            new SudokuMask("010010001010010100000000000000000000000000000000000000000000000000000000000000101"),
            new SudokuMask("000000000000011000000000000000011000000000000000000000000010010000000000000001010"),
            new SudokuMask("000000000000000000110000000000000000000000000000000000010010000100000010000010010"),
            new SudokuMask("010000100000001100010100000000000000000101000000000000000000000000000000000000000"),
            new SudokuMask("000001001000010001000000000000011000000000000000000000000000000000000000000011000"),
            new SudokuMask("000000000000000101000000000000000000000000000000000110000000000000010010000010001"),
            new SudokuMask("000000000000000000000000101000010100000000000000010001000000101000000000000000000"),
            new SudokuMask("000000000000000000000000000000010110000000000000010001000000111000000000000000000"),
            new SudokuMask("000000000010001010010010010000000000000000000000000000000011000000000000000000000"),
            new SudokuMask("000100100000001100000100100000000000000101000000000000000000000000000000000000000"),
            new SudokuMask("000000000000000111000000000000000000000000000000000000000000000000010110000010001"),
            new SudokuMask("000000000000000000000000101000000000000000000000000011100000110000000000100000001"),
            new SudokuMask("000000000000000000000000101000010010010010000010000001000000110000000000000000000"),
            new SudokuMask("000001001000110001000000000000101010000010010000000000000000000000000000000000000"),
            new SudokuMask("000000000000100010000010001010000010000010001010100000000000000000000000000000000"),
            new SudokuMask("100010001010010010000000000110000000000000011000000000000000000000000000000000000"),
            new SudokuMask("000001100000001001000000000000100001001100000001000100000000000000000000000000000"),
            new SudokuMask("000000111000000000000000000000000000000000000000000000000100001010100000010000110"),
            new SudokuMask("000000101000000000000000000000000101000000000000000000000100001010100000010000100"),
            new SudokuMask("000000000000000000000000000000100100000000000000010100010100000010100000010010000"),
            new SudokuMask("010001000000000000100100000001100000001001000000000000110000000000000000000000000"),
            new SudokuMask("000000000000000000000101000000000000100001010100010010000000000000110000000000000"),
            new SudokuMask("001000100000000000010000001000000101000000000011000000000000101000000000000000000"),
            new SudokuMask("001000110000000000010000001000000111000000000011000000000000000000000000000000000"),
            new SudokuMask("000000000001100001100001001000000000101000000000000000000000000000101000000000000"),
            new SudokuMask("001001000001100000100001000000000000101000000000000000000000000000101000000000000"),
            new SudokuMask("000000110000000000000000000000000101000000000000000000010010001000000000010010010"),
            new SudokuMask("000000000000000011000000000000100001000100001000100100000000000000000110000000000"),
            new SudokuMask("000000000000000011000000000000000000001100001001100100000000000000000110000000000"),
            new SudokuMask("000000000001110000001001000000001010000010010000000000000000000000101000000000000"),
            new SudokuMask("000000000000110000000000000010001010000010010010100000000101000000000000000000000"),
            new SudokuMask("000000101000000000000000000001001001000001010001000010000000000000000000000000110"),
            new SudokuMask("000000000000000000000011000000000000000000000000110000001001001000100001001100000"),
            new SudokuMask("000000000000000101000000000000100001001100000001000110000000000000000000000000011"),
            new SudokuMask("000000101000111000000000000000001001000110010000000000000000000000000000000000110"),
            new SudokuMask("000000101000011000000000000000001001000100010000000000000110000000000000000000110"),
            new SudokuMask("001000010001100000000001001000000110000000000000000000000000101000101000000000000"),
            new SudokuMask("100010000010010000001010000110000000000000000000000000000000000010000100001000100"),
            new SudokuMask("000000000000000000011000000010000001000100001001100000000000000010000100001000100"),
            new SudokuMask("000000000110000000000000110100010010010010100000000000000000110000000000000000000"),
            new SudokuMask("000000000000000000000000011000010010010010000010000001001000010001000001000000000"),
            new SudokuMask("000000000000101000110000000000000000000110000000000000010010000100001000100010000"),
            new SudokuMask("000000000000100010000010001000100010000010001000100100000000000000000110000000000"),
            new SudokuMask("000000000001001000010001000010000001000100001001100000000000000000000000011000000"),
            new SudokuMask("100000100001001010010001000000000000000000000000000000000000000100000100011000010"),
            new SudokuMask("010100000100000100000100100000000000110000000000000000100000001000000000010000001"),
            new SudokuMask("000000000000000000000000000001010000110001000000000000100000001001010000010001001"),
            new SudokuMask("110000000000000000000000000011000000000000000000000011100001010000000000001001001"),
            new SudokuMask("001000010001001000010001001000000110000000000011000000000000101000000000000000000"),
            new SudokuMask("000001100000001001000000000001000001000000000001000110000000000000010010000010001"),
            new SudokuMask("100101000000000000100010000010010000010000001000100001000000000000000000000011000"),
            new SudokuMask("000010001010010000001000010000000000000000110000000000001100000010000001000100100"),
            new SudokuMask("000000000000110000000000000000001010000010010110000000000100100010001000100000100"),
            new SudokuMask("000000000000000000000000000000001010000110010110000000000110100010001000100000100"),
            new SudokuMask("010010000010000100000100010000000000000001100000001010000000000000010001000100001"),
            new SudokuMask("000000110001010000001001000000001100100000010100010000000000000000000000000000110"),
            new SudokuMask("000000111001010000001001000000001101100000010100010000000000000000000000000000000"),
            new SudokuMask("000001010001000001100001000000000000101000000000000000010100001010100010000000000"),
            new SudokuMask("010000010001000100000000000001000100100000010100000010100000001000000000010000001"),
            new SudokuMask("000000110000101000000000000000000110000110000000000000000010100100001000100000010"),
            new SudokuMask("000000000000101000000000000000000111000110000000000000000010101100001000100000010"),
            new SudokuMask("000000000001001000010001000000000000100100000001010000010010000100100000010010000"),
            new SudokuMask("100001000000000000100010000010100000001100000001100000010001000000000000001010000"),
            new SudokuMask("100001000000000000100010000010100001001100001000000000010001000000000000001010000"),
            new SudokuMask("000001100000001001101000000000100001001100010100000100000000000000000000000000110"),
            new SudokuMask("000110000000000000000000000000010010000010100000001001001000110001000001000101000"),
            new SudokuMask("001000100000001100010100001000000000000101000011000000100000100000000000100000001"),
            new SudokuMask("000000000101000000000000000000100100110000000000010100010000010001100010010010000"),
            new SudokuMask("000101000101000000000000000000100100110000000000010100000000000001100000010011000"),
            new SudokuMask("010010000010010000001100000001001000000001010100000010100100000010010000000000000"),
            new SudokuMask("000100100100011000010000100000011001010100000001000001000000000101000000000000000"),
            new SudokuMask("000000000000000000101000000000111000001000010100000100010100000010000010000011100"),
            new SudokuMask("001000010001100000000001001000010010010010000010000001000000011000101000000000000"),
            new SudokuMask("000100010101000000000001100000000110110000000000000000000000110001100000010001000"),
            new SudokuMask("000100010101000000000001101000000000110000000000000000000000111001100000010001000"),
            new SudokuMask("001000010001100000000001101000010110010010000010000001000000000000101000000000000"),
            new SudokuMask("100000100010000010010000010010000001000100001001100000000000000100000100001000010"),
            new SudokuMask("000010001001010000001001010000000000000000110000011000000000000010100001010100100"),
            new SudokuMask("000010001001010000001001010000000000000000110000011000000100001000100001000100100"),
            new SudokuMask("000000000010010100001100010000000000000001100000001010001100000010010001000100001"),
            new SudokuMask("000000000000000110000110000010010000010001001000100010000000000001010100001001001"),
            new SudokuMask("001010000010100000000000011100000010000010100010001000001000100000001001100100000"),
            new SudokuMask("001000100000101000010000001000000011000110000011000000000010100100001000100000010"),
            new SudokuMask("001000100000111000010000001000000011000000000011000000000110100100001000100000010"),
            new SudokuMask("000100001100010000001000100000011000010000010100000001000100010011000000000001100"),
            new SudokuMask("001001000000100001100000001000100010001010000010000100010000100000001010100010000"),
            new SudokuMask("000001001000010001101000000000101000001000010100000100010100000010000010000010100"),
            new SudokuMask("000000011001010000001001000000001100100000010100010000000100001010100000010000100"),
            new SudokuMask("100000001000010010001010000010001000000000011100100000000101000010000100001000100"),
            new SudokuMask("010000001000010100001100000001001000000001010100000010100100000010010000000000101"),
            new SudokuMask("000010100010001000010000010100000001000100100001001000001010000100000001000100010"),
            new SudokuMask("000110000110000000000000110100010000010000100000001001001000010001000001000101000"),
            new SudokuMask("000011000010000001100000010100100000001000100000001100011000000000000011000110000"),
            new SudokuMask("000100100100001000010000100000010001010100000001000001000010010101000000000001010"),
            new SudokuMask("000000110001001000010001000000000101100100000001010000000010001100100000010000010"),
            new SudokuMask("100000100000001010010010000010000001000100001001100000000011000100000100001000010"),
            new SudokuMask("000100010101000000000001100000010100110000000000010001000000011001100000010001000"),
            new SudokuMask("100100000100000010000010100010010000010000001000100001000001010001000100001001000"),
            new SudokuMask("010100000100000100000100100001010000010001000000000011100000010001010000000001001"),
            new SudokuMask("000001010001000001100001000000100100101000000000010100010000001000100010010010000"),
            new SudokuMask("100001000000000011100010000010100000001000001000100100010001000000000110001010000"),
            new SudokuMask("100000010001000010000011000010000100100000001000110000000001001000100100011000000"),
            new SudokuMask("010000010001000100000101000001000100100001000000010010100000001000110000010000001"),
            new SudokuMask("110000000000000110000110000011000000000001001000100010100001000000010100001000001"),
            new SudokuMask("001000010001100000000011001010000110000010001010100000000001001000100100000000000"),
            new SudokuMask("000101000010000001100000010100100000001000100000001100011000000000000011000101000"),
            new SudokuMask("000011000110000000000000110100010000010000100000001001001000010001000001000011000"),
            new SudokuMask("000111000010000001100000010100110000001000100000001100011000000000000011000000000"),
            new SudokuMask("000000000110000000000000110100110000010000100000001001001000010001000001000111000"),
            new SudokuMask("010011000010000101000100010001100000001001000000000110000000000000000011000110000"),
            new SudokuMask("000001100100001001010000100000000000011000000000000101010010000101000010000010010"),
            new SudokuMask("010100000100001000010100000000010001010100000001000001000010010101000000000001010"),
            new SudokuMask("010000100100000100010000100001010000010001000000000011100000010001010000000001001"),
            new SudokuMask("100101000100000010000010100000110000011000000000000000010001010001000100001010000"),
            new SudokuMask("100001000000000111100010000010100000001000001000100110010001000000000000001010000"),
            new SudokuMask("110000000000000000000110000011000000000001001000100110100001000000010110001000001"),
            new SudokuMask("110000000000000110000000000011000000000001001000110010100001000000110100001000001"),
            new SudokuMask("100000010001000010000111000010000100100000001000000000000001001000110100011000000"),
            new SudokuMask("010000010001000100000111000001000100100001000000110010100000001000000000010000001")
        };

        List<SudokuMask> actualItems = new ArrayList<>(configFixtureSieve.items());
        assertEquals(actualItems.size(), expectedItems.length);
        actualItems.sort((a, b) -> a.compareTo(b));
        SudokuMask[] _actualItems = new SudokuMask[actualItems.size()];
        actualItems.toArray(_actualItems);
        Arrays.sort(expectedItems);

        assertArrayEquals(expectedItems, _actualItems);
    }

    @Test
    void testSwapBands() {
        Counting.forEachCombo(3, 2, (combo) -> {
            String[] chopped = configFixtureStr.split("(?<=\\G.{9})");
            int b1 = combo[0];
            int b2 = combo[1];

            // To get the expected string, perform 3 swaps within chopped and join("")
            for (int i = 0; i < 3; i++) {
                ArraysUtil.swap(chopped, (b1*3)+i, (b2*3)+i);
            }

            Sudoku test = new Sudoku(configFixture);
            test.swapBands(b1, b2);

            assertEquals(String.join("", chopped), test.toString());
        });
    }

    @Test
    void testSwapStacks() {

    }

    @Test
    void toAndFromBytes() {
        Sudoku s = new Sudoku();
        int[] sBoard = s.getBoard();
        Sudoku rehydratedS = new Sudoku(s.toBytes());
        assertArrayEquals(sBoard, rehydratedS.getBoard());

        s = Sudoku.configSeed().solution();
        sBoard = s.getBoard();
        rehydratedS = new Sudoku(s.toBytes());
        assertArrayEquals(sBoard, rehydratedS.getBoard());

        for (String pStr : GeneratedPuzzles.PUZZLES_24_1000) {
            s = new Sudoku(pStr);
            sBoard = s.getBoard();
            rehydratedS = new Sudoku(s.toBytes());
            assertArrayEquals(sBoard, rehydratedS.getBoard());
        }
    }

    private void populateSieveForAllDigitCombos(int level) {
        for (int r = Sudoku.DIGIT_COMBOS_MAP[level].length - 1; r >= 0; r--) {
            SudokuMask pMask = configFixture.maskForDigits(Sudoku.DIGIT_COMBOS_MAP[level][r]);
            configFixtureSieve.addFromFilter(pMask);
        }
    }

    /** All the solutions to `puzzleFigure`. */
    private String[] copyPuzzleFixtureSolutions() {
        return new String[] {
            "123456789854937126967812435745269318691384572238571964486793251312645897579128643",
            "123456789846937125957182643461279538295318476738645912684793251312564897579821364",
            "123456789846937125957812634768249513295381476431675298684723951312594867579168342",
            "123456789846937125957182643461279538298315476735648912684793251312564897579821364",
            "123456789458937126967812345735269418694381572281574693346798251812645937579123864",
            "123456789684937152957182436845279613291365874736814295468793521312548967579621348",
            "123456789846397125957182634764219358591638472238745916685973241312564897479821563",
            "123456789684397152957812463746289315295631874831745296468973521312568947579124638",
            "123456789458937126967812345735269418694183572281574693346798251812645937579321864",
            "123456789846937125957182634768249513295361478431875296684723951312594867579618342",
            "123456789648397152957812346735249618291685473486731295364978521812564937579123864",
            "123456789546987132987312645468279513291538476735641298654793821312864957879125364",
            "123456789684397125957812436746289513295631874831745692468973251312568947579124368",
            "123456789648937125957182364431279856295861473786345912364728591812594637579613248",
            "123456789684937152957812436845279613291365874736184295468793521312548967579621348",
            "123456789846937125957182634768249513295318476431675298684723951312594867579861342",
            "123456789546987132987312645768249513291538476435671298654793821312864957879125364",
            "123456789854937126967182435745269318698314572231578964486793251312645897579821643",
            "123456789648937125957812364431279856295681473786345912364728591812594637579163248",
            "123456789684937152957812436745289613291365874836174295468793521312548967579621348",
            "123456789458937126967812345735269418691384572284571693346798251812645937579123864",
            "123456789648397152957812364736249815291685473485731296364978521812564937579123648",
            "123456789684937125957182463841279536296315874735864912468793251312548697579621348",
            "123456789846397125957812634761249358598631472234785916685973241312564897479128563",
            "123456789648937125957812364736249518591683472284175693365798241812564937479321856",
            "123456789684397152957812436745289613291635874836741295468973521312568947579124368",
            "123456789648397152957812364735249816291685473486731295364978521812564937579123648",
            "123456789684937125957812364831279456295641873746385912368724591412598637579163248",
            "123456789546987132987312645768249513295631478431578296654793821312864957879125364",
            "123456789684937125957182436841279563296315874735864912468793251312548697579621348",
            "123456789684397152957812463745289316291635874836741295468973521312568947579124638",
            "123456789846937125957182634768249513291365478435871296684723951312594867579618342",
            "123456789648397152957812364735249618291685473486731295364978521812564937579123846",
            "123456789546987132987312645468279513295631478731548296654793821312864957879125364",
            "123456789584937162967812435846279513291365874735184296458793621312648957679521348",
            "123456789684937125957182436846279513291365874735814962468793251312548697579621348",
            "123456789458937126967182345735269418694813572281574693346798251812645937579321864",
            "123456789854937126967812435745269318691384572238571694486723951312695847579148263",
            "123456789684397152957812463746289315291635874835741296468973521312568947579124638",
            "123456789845937126967182534758249613291365478436871295584723961312694857679518342",
            "123456789684937125957812364731289456295641873846375912368724591412598637579163248",
            "123456789584937162967182435846279513291365874735814296458793621312648957679521348",
            "123456789456987132987312654568279413294631578731548296645793821312865947879124365",
            "123456789648937125957812364731249856295681473486375912364728591812594637579163248",
            "123456789854937126967182435745269318691348572238571694486723951312695847579814263",
            "123456789684397152957812364736289415291645873845731296368974521412568937579123648",
            "123456789584937162967812435746289513291365874835174296458793621312648957679521348",
            "123456789458937126967812345735269814691384572284571693346798251812645937579123468",
            "123456789845937126967812534758249613291365478436178295584723961312694857679581342",
            "123456789584937126967182435845279613296315874731864952458793261312648597679521348",
            "123456789854937126967812435745269318691348572238571694486723951312695847579184263",
            "123456789648937125957182364731249856295861473486375912364728591812594637579613248",
            "123456789684397125957812436745289613291635874836741592468973251312568947579124368",
            "123456789846937125957812634768249513291365478435178296684723951312594867579681342",
            "123456789684397152957812364735289416291645873846731295368974521412568937579123648",
            "123456789584937126967182435845279613291365874736814952458793261312648597679521348",
            "123456789458937126967812345735269814694183572281574693346798251812645937579321468",
            "123456789684397125957812463745289316291635874836741592468973251312568947579124638",
            "123456789684397152957812346735289614291645873846731295368974521412568937579123468",
            "123456789684937152957182436845279613291364875736815294468793521312548967579621348",
            "123456789458937126967812345735269814694381572281574693346798251812645937579123468",
            "123456789486937125957812634765289413291345876834671592648793251312568947579124368",
            "123456789584937126967182435841279653296315874735864912458793261312648597679521348",
            "123456789854937126967182435745269318698314572231578694486723951312695847579841263",
            "123456789684397125957812436746289513291635874835741692468973251312568947579124368",
            "123456789584937126967812435746289513295361874831574692458793261312648957679125348",
            "123456789684937152957812436745289613291364875836175294468793521312548967579621348",
            "123456789845937126967182534758249613291368475436571298584723961312694857679815342",
            "123456789584937126967182453841279635296315874735864912458793261312648597679521348",
            "123456789458937126967182345735269814694813572281574693346798251812645937579321468",
            "123456789854937126967182435745269318698341572231578694486723951312695847579814263",
            "123456789648937125957812364534279816291685473786341592365728941812594637479163258",
            "123456789684937152957812436845279613291364875736185294468793521312548967579621348",
            "123456789684397152957812364736289415295641873841735296368974521412568937579123648",
            "123456789845937126967812534758249613291368475436571298584723961312694857679185342",
            "123456789648937125957182364534279816291865473786341592365728941812594637479613258",
            "123456789486937125957812634764289513291345876835671492648793251312568947579124368",
            "123456789854937126967812435745269318698341572231578694486723951312695847579184263",
            "123456789648397152957812364736249815295681473481735296364978521812564937579123648",
            "123456789846937125957812634768249513291385476435671298684723951312594867579168342",
            "123456789486937125957812634764289513295341876831675492648793251312568947579124368",
            "123456789854937126967182435548279613291364578736518942485793261312645897679821354",
            "123456789854937126967812345735269814698341572241578693386794251412685937579123468",
            "123456789486397125957812634765289413294135876831764592648973251312548967579621348",
            "123456789684937152957182436845279613296315874731864295468793521312548967579621348",
            "123456789845937126967812534758249613291368475436175298584723961312694857679581342",
            "123456789648937125957182364735249816291865473486371592364728951812594637579613248",
            "123456789854937126967182435548279613296314578731568942485793261312645897679821354",
            "123456789854937126967812345735269418698341572241578693386794251412685937579123864",
            "123456789486397125957812643765289314294135876831764592648973251312548967579621438",
            "123456789654987123987132465745269318896314572231578946468793251312845697579621834",
            "123456789648937125957182364735249816291865473486371952364728591812594637579613248",
            "123456789486937125957812634865279413291345876734681592648793251312568947579124368",
            "123456789684937152957182436845279613296314875731865294468793521312548967579621348",
            "123456789845397162967812354736249815291568473458731296384975621512684937679123548",
            "123456789854937126967182453541279638296318574738564912485793261312645897679821345",
            "123456789654987123987312465745269318896134572231578946468793251312845697579621834",
            "123456789486397125957812634764289513295134876831765492648973251312548967579621348",
            "123456789584937126967812435846279513295361874731584692458793261312648957679125348",
            "123456789584937162967182435846279513295314876731865294458793621312648957679521348",
            "123456789854937126967812345735269814698143572241578693386794251412685937579321468",
            "123456789845397162967812354736249815298561473451738296384975621512684937679123548",
            "123456789648937125957812364735249816291685473486371592364728951812594637579163248",
            "123456789854937126967812345735269418698143572241578693386794251412685937579321864",
            "123456789456987123987312645765249318894631572231578964648793251312865497579124836",
            "123456789486397152957812643764289315295134876831765294648973521312548967579621438",
            "123456789648937125957812364735249816291685473486371952364728591812594637579163248",
            "123456789486937125957812634864279513291345876735681492648793251312568947579124368",
            "123456789584937126967812435845279613291365874736184952458793261312648597679521348",
            "123456789584937126967812435745289613291365874836174952458793261312648597679521348",
            "123456789845397162967812354736249815291538476458761293384975621512684937679123548",
            "123456789584397162967812435746289513295134876831765294458973621312648957679521348",
            "123456789546987123987312654765249318891635472234178965658793241312864597479521836",
            "123456789486937125957812634864279513295341876731685492648793251312568947579124368",
            "123456789684937125957812436846279513291365874735184962468793251312548697579621348",
            "123456789486397152957812643765289314294135876831764295648973521312548967579621438",
            "123456789684937125957812436746289513291365874835174962468793251312548697579621348",
            "123456789845397162967812354736249815298531476451768293384975621512684937679123548",
            "123456789854937126967812345735269418691348572248571693386794251412685937579123864",
            "123456789684397152957812436745289613296134875831765294468973521312548967579621348",
            "123456789486397152957812634765289413294135876831764295648973521312548967579621348",
            "123456789854937126967812345735269814691348572248571693386794251412685937579123468",
            "123456789648937125957182364435279816291865473786341592364728951812594637579613248",
            "123456789684397152957812436745289613296135874831764295468973521312548967579621348",
            "123456789846937125957182643761249538295318476438675912684793251312564897579821364",
            "123456789648937125957182364435279816291865473786341952364728591812594637579613248",
            "123456789846937125957182643761249538298315476435678912684793251312564897579821364",
            "123456789584937162967812435746289513295361874831574296458793621312648957679125348",
            "123456789845397162967812354736249518291538476458761293384975621512684937679123845",
            "123456789486937152957812634765289413291345876834671295648793521312568947579124368",
            "123456789486937125957812634864279513291345876735681942648793251312568497579124368",
            "123456789648937125957812364435279816291685473786341592364728951812594637579163248",
            "123456789845397126967812354734269815698531472251748693386975241512684937479123568",
            "123456789846397152957812634768249315295138476431765298684973521312584967579621843",
            "123456789845397162967812354736249518291568473458731296384975621512684937679123845",
            "123456789648937125957812364435279816291685473786341952364728591812594637579163248",
            "123456789486937125957812634864279513295341876731685942648793251312568497579124368",
            "123456789845397126967812354734269518698531472251748693386975241512684937479123865",
            "123456789584937162967812435846279513295361874731584296458793621312648957679125348",
            "123456789846397152957812634765249318298135476431768295684973521312584967579621843",
            "123456789486937125957812634764289513295341876831675942648793251312568497579124368",
            "123456789845397162967812354738249516291568473456731298384975621512684937679123845",
            "123456789684937125957182436846279513291365874735814692468793251312548967579621348",
            "123456789486937125957812634861279543295341876734685912648793251312568497579124368",
            "123456789846937152957182634564279318298361475731845296685723941312594867479618523",
            "123456789486937152957812634865279413291345876734681295648793521312568947579124368",
            "123456789846397152957812634765249813298135476431768295684973521312584967579621348",
            "123456789486937125957812634764289513291345876835671942648793251312568497579124368",
            "123456789845397126967812354738249615691538472254761893386975241512684937479123568",
            "123456789584937126967182435845279613296315874731864592458793261312648957679521348",
            "123456789854937162967182435645279318298314576731568294486723951312695847579841623",
            "123456789845397162967812354738249516296531478451768293384975621512684937679123845",
            "123456789486937125957812643861279534295341876734685912648793251312568497579124368",
            "123456789584937126967182435845279613291365874736814592458793261312648957679521348",
            "123456789845397126967812354734269518691538472258741693386975241512684937479123865",
            "123456789854937162967182435645279318298341576731568294486723951312695847579814623",
            "123456789846397152957812643765249318298135476431768295684973521312584967579621834",
            "123456789584937126967812435741289653295361874836574912458793261312648597679125348",
            "123456789845397162967812354736249518298531476451768293384975621512684937679123845",
            "123456789845397126967812354734269815691538472258741693386975241512684937479123568",
            "123456789584937126967812453841279635295361874736584912458793261312648597679125348",
            "123456789846397152957812643768249315295138476431765298684973521312584967579621834",
            "123456789486937125957812634761289543295341876834675912648793251312568497579124368",
            "123456789845397162967812354736249518298561473451738296384975621512684937679123845",
            "123456789684937125957812364735289416291645873846371592368724951412598637579163248",
            "123456789584397126967812435745289613296531874831764592458973261312648957679125348",
            "123456789684937125957182436845279613296315874731864592468793251312548967579621348",
            "123456789584937126967812435841279653295361874736584912458793261312648597679125348",
            "123456789846937152957182634564279318298315476731648295685723941312594867479861523",
            "123456789684937125957812364735289416291645873846371952368724591412598637579163248",
            "123456789584397162967812435746289513291534876835761294458973621312648957679125348",
            "123456789684937125957182436845279613291365874736814592468793251312548967579621348",
            "123456789845937162967182534654279318298361475731845296586723941312694857479518623",
            "123456789486937125957812643761289534295341876834675912648793251312568497579124368",
            "123456789486397152957812634765289413294631875831745296648973521312568947579124368",
            "123456789846397125957812634765249813298135476431768592684973251312584967579621348",
            "123456789684937125957812364835279416291645873746381952368724591412598637579163248",
            "123456789584937126967182435846279513291365874735814692458793261312648957679521348",
            "123456789854937126967812435548279613291364578736581942485793261312645897679128354",
            "123456789846937125957812643761249538295381476438675912684793251312564897579128364",
            "123456789845397162967812345736249518291538476458761293384975621512684937679123854",
            "123456789854937162967182435645279318298361574731548296486723951312695847579814623",
            "123456789846397125957812634768249513295138476431765892684973251312584967579621348",
            "123456789684937125957812364835279416291645873746381592368724951412598637579163248",
            "123456789854937126967812453541279638298361574736584912485793261312645897679128345",
            "123456789845937162967182534654279318298361475731548296586723941312694857479815623",
            "123456789584937126967812453741289635295361874836574912458793261312648597679125348",
            "123456789845397162967812345738249516296531478451768293384975621512684937679123854",
            "123456789854937126967812453541279638296381574738564912485793261312645897679128345",
            "123456789846397125957812634765249318298135476431768592684973251312584967579621843",
            "123456789845397162967812345736249518298531476451768293384975621512684937679123854",
            "123456789846397125957812643765249318298135476431768592684973251312584967579621834",
            "123456789846937125957812643461279538295381476738645912684793251312564897579128364",
            "123456789584937126967812435845279613291365874736184592458793261312648957679521348",
            "123456789584937126967812435745289613291365874836174592458793261312648957679521348",
            "123456789648937125957812364436279518291385476785641293364728951812594637579163842",
            "123456789854937162967812435645279318298341576731568294486723951312695847579184623",
            "123456789845397162967812345738249516291568473456731298384975621512684937679123854",
            "123456789845397162967812345736249518291568473458731296384975621512684937679123854",
            "123456789584937126967812435746289513291365874835174692458793261312648957679521348",
            "123456789648937125957812364436279518295183476781645293364728951812594637579361842",
            "123456789854937162967812435645279318298361574731584296486723951312695847579148623",
            "123456789648937125957812364735249816291685473486173592364728951812594637579361248",
            "123456789684937125957812364835279416296145873741683592368724951412598637579361248",
            "123456789584937126967812435846279513291365874735184692458793261312648957679521348",
            "123456789845397162967812345736249518298561473451738296384975621512684937679123854",
            "123456789648937125957812364436279518295381476781645293364728951812594637579163842",
            "123456789648937125957812364735249816296185473481673592364728951812594637579361248",
            "123456789684937152957812463745289316296341875831675294468793521312568947579124638",
            "123456789854937162967812435645279318298361574731548296486723951312695847579184623",
            "123456789846937152957812634765249813291368475438175296684793521312584967579621348",
            "123456789584397126967812435741289653296531874835764912458973261312648597679125348",
            "123456789684937125957182364835279416291645873746813592368724951412598637579361248",
            "123456789684937152957812436745289613296341875831675294468793521312568947579124368",
            "123456789845937162967812534654279318298361475731548296586723941312694857479185623",
            "123456789486937152957812634765289413291364875834175296648793521312548967579621348",
            "123456789584397126967812435745289613296531874831764952458973261312648597679125348",
            "123456789684937125957812364835279416291645873746183592368724951412598637579361248",
            "123456789684937125957812364735289416296145873841673592368724951412598637579361248",
            "123456789684937125957812436845279613291365874736184592468793251312548967579621348",
            "123456789648937125957182364436279518295813476781645293364728951812594637579361842",
            "123456789584397126967812453741289635296531874835764912458973261312648597679125348",
            "123456789684937125957812364735289416291645873846173592368724951412598637579361248",
            "123456789684937125957812436745289613291365874836174592468793251312548967579621348",
            "123456789548937126967182354435279618291863475786541293354728961812694537679315842",
            "123456789486937152957812643765289314291364875834175296648793521312548967579621438",
            "123456789485397162967812354736289415291564873854731296348975621512648937679123548",
            "123456789648937125957812364435279816291685473786143592364728951812594637579361248",
            "123456789485397162967812345736289514291564873854731296348975621512648937679123458",
            "123456789846937152957812634765249318291368475438175296684793521312584967579621843",
            "123456789485397162967812354736289415294561873851734296348975621512648937679123548",
            "123456789648937125957812364435279816296185473781643592364728951812594637579361248",
            "123456789684937125957812436846279513291365874735184692468793251312548967579621348",
            "123456789846937152957812634564279318291368475738145296685723941312594867479681523",
            "123456789485397162967812345736289514294561873851734296348975621512648937679123458",
            "123456789846937152957812643765249318291368475438175296684793521312584967579621834",
            "123456789584397126967812453741289635296135874835764912458973261312648597679521348",
            "123456789648937125957182364435279816296815473781643592364728951812594637579361248",
            "123456789684937125957812436746289513291365874835174692468793251312548967579621348",
            "123456789458937126967182345534279618296813574781564293345728961812695437679341852",
            "123456789846937152957812634564279318291385476738641295685723941312594867479168523",
            "123456789584397126967812435741289653296135874835764912458973261312648597679521348",
            "123456789485397162967812354736289415294531876851764293348975621512648937679123548",
            "123456789648937125957182364735249816296815473481673592364728951812594637579361248",
            "123456789485397162967812345734289516291564873856731294348975621512648937679123458",
            "123456789458937126967812345534279618296183574781564293345728961812695437679341852",
            "123456789485397162967812354736289415291534876854761293348975621512648937679123548",
            "123456789458937126967812345534279618296381574781564293345728961812695437679143852",
            "123456789684397125957812463741289536296135874835764912468973251312548697579621348",
            "123456789648937125957182364534279816296815473781643592365728941812594637479361258",
            "123456789485397162967812345736289514294531876851764293348975621512648937679123458",
            "123456789854937162967812435645279318291368574738541296486723951312695847579184623",
            "123456789584397126967812435745289613296135874831764592458973261312648957679521348",
            "123456789845937162967812534654279318291368475738541296586723941312694857479185623",
            "123456789684937152957812463746289315295341876831675294468793521312568947579124638",
            "123456789684397125957812436741289563296135874835764912468973251312548697579621348",
            "123456789458937126967182345534279618291863574786514293345728961812695437679341852",
            "123456789485397162967812345736289514291534876854761293348975621512648937679123458",
            "123456789684397125957812436745289613296135874831764592468973251312548967579621348",
            "123456789845937162967812534654279318291368475738145296586723941312694857479581623",
            "123456789648937125957812364534279816291685473786143592365728941812594637479361258",
            "123456789648937125957812364534279816296185473781643592365728941812594637479361258",
            "123456789458937126967182345534279618291863574786541293345728961812695437679314852",
            "123456789486937152957812643764289315291365874835174296648793521312548967579621438",
            "123456789584397126967812435745289613296135874831764952458973261312648597679521348",
            "123456789485397162967812345734289516296531874851764293348975621512648937679123458",
            "123456789485397162967812354736289415294561873851743296348975621512638947679124538",
            "123456789846937152957812643768249315291365478435178296684793521312584967579621834",
            "123456789854937162967182435645279318291368574738514296486723951312695847579841623",
            "123456789584397162967812345736289514291543876845761293358974621412638957679125438",
            "123456789546987132987132645768249513295318476431675298654793821312864957879521364",
            "123456789684937152957812463746289315291345876835671294468793521312568947579124638",
            "123456789485397162967812354736289415291543876854761293348975621512638947679124538",
            "123456789854937162967182435645279318291368574738541296486723951312695847579814623",
            "123456789846397125957812634768249513295631478431785962684973251312564897579128346",
            "123456789684937125957812364831279456296145873745683912368724591412598637579361248",
            "123456789546987132987312645768249513291635478435178296654793821312864957879521364",
            "123456789846937125957812634468279513291365478735148296684723951312594867579681342",
            "123456789684397152957812346735289614296145873841763295368974521412538967579621438",
            "123456789845937162967182534654279318291368475738541296586723941312694857479815623",
            "123456789846397125957812634768249513291635478435781962684973251312564897579128346",
            "123456789684937125957812364731289456296145873845673912368724591412598637579361248",
            "123456789548397162967812345736249518291583476485761293354978621812634957679125834",
            "123456789546987132987312645768249513295138476431675298654793821312864957879521364",
            "123456789846937125957182634468279513291365478735841296684723951312594867579618342",
            "123456789846937152957812634768249315291365478435178296684793521312584967579621843",
            "123456789648397152957812346735249618296185473481763295364978521812534967579621834",
            "123456789654987132987312456548279613296138574731564298465793821312845967879621345",
            "123456789846397125957182634768249513291635478435718962684973251312564897579821346",
            "123456789648397152957182346735249618296815473481763295364978521812534967579621834",
            "123456789684937125957182364835279416291645873746813952368724591412598637579361248",
            "123456789654987132987312456548279613296134578731568294465793821312845967879621345",
            "123456789845937126967182354438279615291563478756841293384725961512694837679318542",
            "123456789684937125957812364735289416296145873841673952368724591412598637579361248",
            "123456789854937162967812435645279318291384576738561294486723951312695847579148623",
            "123456789854937126967812435548279613291364578736581294485723961312695847679148352",
            "123456789854937162967812435645279318291348576738561294486723951312695847579184623",
            "123456789845937126967812354438279615291563478756148293384725961512694837679381542",
            "123456789684937125957812364835279416296145873741683952368724591412598637579361248",
            "123456789546987132987312645468279513291635478735148296654793821312864957879521364",
            "123456789648397152957182346735249618291865473486713295364978521812534967579621834",
            "123456789854937126967182435548279613291364578736518294485723961312695847679841352",
            "123456789854937162967182435645279318291348576738561294486723951312695847579814623",
            "123456789546987132987312645468279513295138476731645298654793821312864957879521364",
            "123456789684397125957812364731289546296145873845763912368974251412538697579621438",
            "123456789548397162967182345736249518291865473485713296354978621812634957679521834",
            "123456789684937125957812364835279416291645873746183952368724591412598637579361248",
            "123456789645987132987132546758249613296318475431675298564793821312864957879521364",
            "123456789645987132987312564758249316296138475431675298564793821312864957879521643",
            "123456789845937126967182534458279613291365478736841295584723961312694857679518342",
            "123456789684937125957812364735289416291645873846173952368724591412598637579361248",
            "123456789645987132987132564758249316296318475431675298564793821312864957879521643",
            "123456789845937126967812534458279613291365478736148295584723961312694857679581342",
            "123456789645987132987312546758249613296138475431675298564793821312864957879521364",
            "123456789648397125957182346731249568296815473485763912364978251812534697579621834",
            "123456789854937126967182435548279613296341578731568294485723961312695847679814352",
            "123456789645987132987312546758249613291638475436175298564793821312864957879521364",
            "123456789654987132987132456548279613296318574731564298465793821312845967879621345",
            "123456789645987132987312564758249316291638475436175298564793821312864957879521643",
            "123456789648397125957812346731249568296185473485763912364978251812534697579621834",
            "123456789584397162967812345736289514295143876841765293358974621412638957679521438",
            "123456789854937126967182435548279613296314578731568294485723961312695847679841352",
            "123456789854937162967182435745269318298314576631578294486723951312695847579841623",
            "123456789546987132987132645468279513295318476731645298654793821312864957879521364",
            "123456789684397152957812346735289614296143875841765293368974521412538967579621438",
            "123456789854937162967182435745269318298341576631578294486723951312695847579814623",
            "123456789645987132987312564756249318291638475438175296564793821312864957879521643",
            "123456789684397125957812346731289564296145873845763912368974251412538697579621438",
            "123456789846937125957182634468279513295361478731845296684723951312594867579618342",
            "123456789654987132987132456548279613296314578731568294465793821312845967879621345",
            "123456789648937125957812364735249816291685473486173952364728591812594637579361248",
            "123456789645987132987312564758249613296138475431675298564793821312864957879521346",
            "123456789648937125957182364431279856296815473785643912364728591812594637579361248",
            "123456789854937162967182435745269318291348576638571294486723951312695847579814623",
            "123456789648397152957812346735249618296183475481765293364978521812534967579621834",
            "123456789854937126967182345538279614296341578741568293385724961412695837679813452",
            "123456789645987132987132564758249613296318475431675298564793821312864957879521346",
            "123456789648937125957182364731249856296815473485673912364728591812594637579361248",
            "123456789548397162967812345736249518295183476481765293354978621812634957679521834",
            "123456789648937125957812364735249816296185473481673952364728591812594637579361248",
            "123456789654987132987132456548279613291368574736514298465793821312845967879621345",
            "123456789854937162967812435745269318298341576631578294486723951312695847579184623",
            "123456789645987132987312546758249613291635478436178295564793821312864957879521364",
            "123456789648937125957812364736249518295681473481375296364728951812594637579163842",
            "123456789648937125957182364735249816296815473481673952364728591812594637579361248",
            "123456789645987132987312564758249613291638475436175298564793821312864957879521346",
            "123456789654987132987132456548279613291364578736518294465793821312845967879621345",
            "123456789854937126967812435548279613296341578731568294485723961312695847679184352",
            "123456789648937125957812364431279856296185473785643912364728591812594637579361248",
            "123456789645987132987312546758249613296135478431678295564793821312864957879521364",
            "123456789854937162967812435745269318291384576638571294486723951312695847579148623",
            "123456789648937125957812364736249518291685473485173296364728951812594637579361842",
            "123456789648937125957812364731249856296185473485673912364728591812594637579361248",
            "123456789854937126967812345538279614296341578741568293385724961412695837679183452",
            "123456789548397162967182345736249518295813476481765293354978621812634957679521834",
            "123456789854937162967812435745269318291348576638571294486723951312695847579184623",
            "123456789648937125957812364736249518291685473485371296364728951812594637579163842",
            "123456789648937125957182364435279816296815473781643952364728591812594637579361248",
            "123456789854937126967812345538279614296143578741568293385724961412695837679381452",
            "123456789648397152957182346735249618296813475481765293364978521812534967579621834",
            "123456789648937125957812364435279816296185473781643952364728591812594637579361248",
            "123456789645987132987312564758249613296135478431678295564793821312864957879521346",
            "123456789648937125957812364435279816291685473786143952364728591812594637579361248",
            "123456789645987132987312564758249613291635478436178295564793821312864957879521346",
            "123456789648397152957182346735249618291863475486715293364978521812534967579621834",
            "123456789648937125957182364736249518295861473481375296364728951812594637579613842",
            "123456789645987132987312564758249316291635478436178295564793821312864957879521643",
            "123456789648937125957182364736249518291865473485371296364728951812594637579613842",
            "123456789645987132987312564758249316296135478431678295564793821312864957879521643",
            "123456789645987132987312564756249318298135476431678295564793821312864957879521643",
            "123456789854937126967182435645279318291368574738514692486723951312695847579841263",
            "123456789485937126967812543856279314294361875731584692548723961312698457679145238",
            "123456789648937125957182364436279518295861473781345296364728951812594637579613842",
            "123456789584937126967812453846279315295361874731584692458723961312698547679145238",
            "123456789648937125957812364736249518295183476481675293364728951812594637579361842",
            "123456789485937126967812543756289314294361875831574692548723961312698457679145238",
            "123456789458937126967812345534279618296184573781563294345728961812695437679341852",
            "123456789584937126967182453846279315295361874731845692458723961312698547679514238",
            "123456789854937126967812435645279318291368574738541692486723951312695847579184263",
            "123456789648937125957182364436279518291865473785341296364728951812594637579613842",
            "123456789648937125957182364736249518295813476481675293364728951812594637579361842",
            "123456789548937126967182354731249865295861473486573912354728691812694537679315248",
            "123456789458937126967182345534279618296814573781563294345728961812695437679341852",
            "123456789684937152957812346735289614296341875841675293368794521412568937579123468",
            "123456789854937126967182435645279318291368574738541692486723951312695847579814263",
            "123456789548937126967182354435279618291865473786341295354728961812694537679513842",
            "123456789684937125957182463845279316296315874731864592468723951312598647579641238",
            "123456789645987132987132546758249613296315478431678295564793821312864957879521364",
            "123456789845937126967182354731249568298561473456873912384725691512694837679318245",
            "123456789486937125957812643865279314291345876734681592648723951312598467579164238",
            "123456789648937125957812364736249518291385476485671293364728951812594637579163842",
            "123456789648937152957812364735249618296381475481675293364798521812564937579123846",
            "123456789648937125957812364436279518291685473785143296364728951812594637579361842",
            "123456789584937126967812453746289315295361874831574692458723961312698547679145238",
            "123456789645987132987132564758249316296315478431678295564793821312864957879521643",
            "123456789845937126967182345731249568298561473456873912384725691512694837679318254",
            "123456789486937125957812643765289314291345876834671592648723951312598467579164238",
            "123456789458937126967182345534279618296841573781563294345728961812695437679314852",
            "123456789648937125957812364736249518295381476481675293364728951812594637579163842",
            "123456789648937152957812346735249618296381475481675293364798521812564937579123864",
            "123456789854937126967812435645279318298361574731584692486723951312695847579148263",
            "123456789458937126967182345534279618291864573786513294345728961812695437679341852",
            "123456789645987132987132564758249613296315478431678295564793821312864957879521346",
            "123456789854937126967812435645279318298361574731548692486723951312695847579184263",
            "123456789485937126967812543756289314291364875834571692548723961312698457679145238",
            "123456789684937125957182463845279316291365874736841592468723951312598647579614238",
            "123456789645987132987132564756249318298315476431678295564793821312864957879521643",
            "123456789648937152957812364735249816296381475481675293364798521812564937579123648",
            "123456789845937126967182354431279568298561473756843912384725691512694837679318245",
            "123456789854937126967182435645279318298361574731548692486723951312695847579814263",
            "123456789485937126967812543856279314291364875734581692548723961312698457679145238",
            "123456789648937125957812364436279518291685473785341296364728951812594637579163842",
            "123456789846937125957812634468279513291385476735641298684723951312594867579168342",
            "123456789648397125957812364736249518295183476481765932364978251812534697579621843",
            "123456789684937152957812364735289416296341875841675293368794521412568937579123648",
            "123456789854937126967182345531279468296841573748563912385724691412695837679318254",
            "123456789648937125957812364436279518295681473781345296364728951812594637579163842",
            "123456789684937125957812463845279316291365874736184592468723951312598647579641238",
            "123456789648397125957182364736249518295813476481765932364978251812534697579621843",
            "123456789684937125957182463845279316291365874736814592468723951312598647579641238",
            "123456789854937126967182435548279613291368574736514298485723961312695847679841352",
            "123456789548937126967182354431279865295861473786543912354728691812694537679315248",
            "123456789648397125957182346736249518291865473485713962364978251812534697579621834",
            "123456789485937126967182543856279314294361875731845692548723961312698457679514238",
            "123456789648937152957812364736249815291385476485671293364798521812564937579123648",
            "123456789854937126967182435546279318291368574738514692485723961312695847679841253",
            "123456789645987132987312564758249316296531478431678295564793821312864957879125643",
            "123456789845937126967182534458279613291368475736541298584723961312694857679815342",
            "123456789645987132987312564758249613291638475436571298564793821312864957879125346",
            "123456789458937126967182345531279864294861573786543912345728691812695437679314258",
            "123456789486937125957182643865279314291345876734861592648723951312598467579614238",
            "123456789648937152957812364736249815295381476481675293364798521812564937579123648",
            "123456789645987132987312564758249316291638475436571298564793821312864957879125643",
            "123456789854937126967182435548279613291368574736541298485723961312695847679814352",
            "123456789584937126967182453846279315291365874735841692458723961312698547679514238",
            "123456789645987132987312546758249613291638475436571298564793821312864957879125364",
            "123456789458937126967182345531279864296841573784563912345728691812695437679314258",
            "123456789854937126967182435546279318291368574738541692485723961312695847679814253",
            "123456789584937126967182453846279315291365874735814692458723961312698547679541238",
            "123456789854937126967182435546279318298361574731548692485723961312695847679814253",
            "123456789684937152957812364736289415295341876841675293368794521412568937579123648",
            "123456789485937126967812543856279314291364875734185692548723961312698457679541238",
            "123456789645987132987312564758249613296531478431678295564793821312864957879125346",
            "123456789584937126967812453846279315291365874735184692458723961312698547679541238",
            "123456789684937152957812364736289415291345876845671293368794521412568937579123648",
            "123456789845937126967812354738249615291563478456178293384725961512694837679381542",
            "123456789854937126967812435548279613291368574736541298485723961312695847679184352",
            "123456789645987132987312546758249613296531478431678295564793821312864957879125364",
            "123456789486937125957182643865279314294315876731864592648723951312598467579641238",
            "123456789485937126967182345831279564294561873756843912348725691512698437679314258",
            "123456789845937126967812534458279613291368475736541298584723961312694857679185342",
            "123456789854937126967182453546279318298361574731548692485723961312695847679814235",
            "123456789485937126967182543856279314291364875734815692548723961312698457679541238",
            "123456789548937126967182354735249618291863475486571293354728961812694537679315842",
            "123456789845397126967812345731249658298561473456783912384975261512634897679128534",
            "123456789845937126967182345431279568298561473756843912384725691512694837679318254",
            "123456789584937126967812453746289315291365874835174692458723961312698547679541238",
            "123456789854937126967182453546279318291368574738541692485723961312695847679814235",
            "123456789845937126967182354738249615291563478456871293384725961512694837679318542",
            "123456789845937126967812534458279613291368475736145298584723961312694857679581342",
            "123456789845397126967812345731249658296581473458763912384975261512634897679128534",
            "123456789684937125957812463745289316291365874836174592468723951312598647579641238",
            "123456789485937126967812543756289314291364875834175692548723961312698457679541238",
            "123456789645987132987312564756249318291538476438671295564793821312864957879125643",
            "123456789854937126967182453546279318291368574738514692485723961312695847679841235",
            "123456789854937126967182354531279468296841573748563912385724691412695837679318245",
            "123456789645987132987312564756249318291638475438571296564793821312864957879125643",
            "123456789485397126967812345731289654294561873856743912348975261512638497679124538",
            "123456789684937152957812346735289614291645873846371295368794521412568937579123468",
            "123456789845937126967182354438279615291568473756341298384725961512694837679813542",
            "123456789584937126967182354831279465296541873745863912358724691412698537679315248",
            "123456789845937126967812354738249615291568473456371298384725961512694837679183542",
            "123456789648937152957812346735249618291685473486371295364798521812564937579123864",
            "123456789485397126967812345731289654296541873854763912348975261512638497679124538",
            "123456789845937126967812354738249615291568473456173298384725961512694837679381542",
            "123456789584397126967812354731289645296541873845763912358974261412638597679125438",
            "123456789845937126967812354438279615291568473756341298384725961512694837679183542",
            "123456789645987132987312564756249318298531476431678295564793821312864957879125643",
            "123456789485937126967182345831279564296541873754863912348725691512698437679314258",
            "123456789485937162967812354736289415291564873854371296348795621512648937679123548",
            "123456789854937126967812435546279318298361574731548692485723961312695847679184253",
            "123456789845937126967812354438279615291568473756143298384725961512694837679381542",
            "123456789645987132987312564756249318298631475431578296564793821312864957879125643",
            "123456789854937126967812435546279318298361574731584692485723961312695847679148253",
            "123456789845937126967182354738249615291568473456371298384725961512694837679813542",
            "123456789485937162967812354736289415294561873851374296348795621512648937679123548",
            "123456789485397126967812354731289645294561873856743912348975261512638497679124538",
            "123456789548937126967182354735249618291865473486371295354728961812694537679513842",
            "123456789645987123987312564854279316296531478731648295568723941312894657479165832",
            "123456789456987123987132645865279314291348576734561892648723951312895467579614238",
            "123456789854937126967812453546279318298361574731584692485723961312695847679148235",
            "123456789548397126967812345731249658296581473485763912354978261812634597679125834",
            "123456789645987132987312564854279316296531478731648295568723941312894657479165823",
            "123456789654987123987132465845279316291368574736541892468723951312895647579614238",
            "123456789845937162967812354736249815291568473458371296384795621512684937679123548",
            "123456789548937126967182354731249865295861473486375912354728691812694537679513248",
            "123456789854937126967812453546279318298361574731548692485723961312695847679184235",
            "123456789846937125957182634468279513295318476731645298684723951312594867579861342",
            "123456789584397126967812345731289654296541873845763912358974261412638597679125438",
            "123456789845937162967812354736249815298561473451378296384795621512684937679123548",
            "123456789548937126967182354431279865295861473786345912354728691812694537679513248",
            "123456789846937125957812634468279513295381476731645298684723951312594867579168342",
            "123456789645987123987132564854279316291368475736541892568723941312894657479615238",
            "123456789485397162967812534756289413294135876831764295548973621312648957679521348",
            "123456789485397126967812354731289645296541873854763912348975261512638497679124538",
            "123456789645987123987312564854279316291635478736148295568723941312894657479561832",
            "123456789648937152957812364735249618291685473486371295364798521812564937579123846",
            "123456789854937126967812435546279318291368574738541692485723961312695847679184253",
            "123456789854937126967812453546279318291368574738541692485723961312695847679184235",
            "123456789854937126967812435548279613296381574731564298485723961312695847679148352",
            "123456789654987123987132465845279316291368574736514892468723951312895647579641238",
            "123456789645987123987312564854279316296135478731648295568723941312894657479561832",
            "123456789485397162967812543754289316296135874831764295548973621312648957679521438",
            "123456789845937126967182345731249568298561473456378912384725691512694837679813254",
            "123456789854937126967182435548279613296318574731564298485723961312695847679841352",
            "123456789485397162967812543756289314294135876831764295548973621312648957679521438",
            "123456789845937126967182345431279568298561473756348912384725691512694837679813254",
            "123456789645987132987312564854279316291635478736148295568723941312894657479561823",
            "123456789548397126967812345735249618296581473481763952354978261812634597679125834",
            "123456789456987132987132645865279314291364578734518296648723951312895467579641823",
            "123456789485937162967812345736289514294561873851374296348795621512648937679123458",
            "123456789645987132987312564854279316296135478731648295568723941312894657479561823",
            "123456789456987123987132645865279314291364578734518296648723951312895467579641832",
            "123456789854937126967182345538279614296841573741563298385724961412695837679318452",
            "123456789845937126967182354431279568298561473756348912384725691512694837679813245",
            "123456789654987132987132465845279316291364578736518294468723951312895647579641823",
            "123456789485397126967812534754289613296134875831765492548973261312648957679521348",
            "123456789485397126967812354734289615296541873851763942348975261512638497679124538",
            "123456789654987123987132465845279316291364578736518294468723951312895647579641832",
            "123456789854937126967812345538279614296148573741563298385724961412695837679381452",
            "123456789485937162967812345736289514291564873854371296348795621512648937679123458",
            "123456789845937126967182354731249568298561473456378912384725691512694837679813245",
            "123456789485397162967812543754289316296134875831765294548973621312648957679521438",
            "123456789584397126967812345735289614296541873841763952358974261412638597679125438",
            "123456789485937162967812345734289516291564873856371294348795621512648937679123458",
            "123456789845937126967812534456279318298361475731548692584723961312694857679185243",
            "123456789456987123987132645865279314291348576734561298648723951312895467579614832",
            "123456789654987132987132465845279316291368574736541298468723951312895647579614823",
            "123456789845937126967812534654279318298361475731548692586723941312694857479185263",
            "123456789654987132987132465845279316291368574736514298468723951312895647579641823",
            "123456789645987132987312564854279316291638475736145298568723941312894657479561823",
            "123456789645987123987132564854279316291368475736541298568723941312894657479615832",
            "123456789485937162967812543756289314291364875834175296548793621312648957679521438",
            "123456789845937126967812345731249568298561473456378912384725691512694837679183254",
            "123456789845937126967812534756249318298361475431578692584723961312694857679185243",
            "123456789845937162967812345738249516291568473456371298384795621512684937679123854",
            "123456789645987132987312564854279316296138475731645298568723941312894657479561823",
            "123456789654987123987132465845279316291368574736541298468723951312895647579614832",
            "123456789456987132987132645865279314291348576734561298648723951312895467579614823",
            "123456789485937126967812543756289314291364875834175692548793261312648957679521438",
            "123456789584397126967812345731289654296145873845763912358974261412638597679521438",
            "123456789845937126967812354731249568298561473456378912384725691512694837679183245",
            "123456789845937162967812345736249518291568473458371296384795621512684937679123854",
            "123456789548397126967812345731249658296185473485763912354978261812634597679521834",
            "123456789845937126967812534756249318291368475438571692584723961312694857679185243",
            "123456789456987132987312645865279314294138576731564298648723951312895467579641823",
            "123456789645987132987132564854279316291368475736541298568723941312894657479615823",
            "123456789485937126967812345731289564294561873856374912348725691512698437679143258",
            "123456789654987123987132465845279316291368574736514298468723951312895647579641832",
            "123456789845937126967812534456279318291368475738541692584723961312694857679185243",
            "123456789485937162967812543754289316291365874836174295548793621312648957679521438",
            "123456789845937162967812345736249518298561473451378296384795621512684937679123854",
            "123456789548397126967182345731249658296815473485763912354978261812634597679521834",
            "123456789845937126967812534654279318291368475738541692586723941312694857479185263",
            "123456789485937162967812543754289316291364875836175294548793621312648957679521438",
            "123456789654987132987312465845279316291638574736541298468723951312895647579164823",
            "123456789845937162967812354736249518298561473451378296384795621512684937679123845",
            "123456789485937126967812345831279564294561873756384912348725691512698437679143258",
            "123456789845937126967812534654279318291368475738145692586723941312694857479581263",
            "123456789645987132987312564854279316291638475736541298568723941312894657479165823",
            "123456789584397126967812345735289614296145873841763952358974261412638597679521438",
            "123456789845937162967812354738249516291568473456371298384795621512684937679123845",
            "123456789845937126967812345431279568298561473756348912384725691512694837679183254",
            "123456789845937126967812534456279318291368475738145692584723961312694857679581243",
            "123456789548397126967812345735249618296185473481763952354978261812634597679521834",
            "123456789845937162967812354736249518291568473458371296384795621512684937679123845",
            "123456789485937126967812534756289413291364875834175692548793261312648957679521348",
            "123456789845937126967812354431279568298561473756348912384725691512694837679183245",
            "123456789845937126967812534756249318291368475438175692584723961312694857679581243",
            "123456789548397126967182345735249618296815473481763952354978261812634597679521834",
            "123456789654987123987132465845279316296318574731564892468723951312895647579641238",
            "123456789485937162967812534756289413291364875834175296548793621312648957679521348",
            "123456789845397126967182345731249658296518473458763912384975261512634897679821534",
            "123456789456987123987312645865279314294138576731564298648723951312895467579641832",
            "123456789548397126967182345735249618291865473486713952354978261812634597679521834",
            "123456789584397126967812354731289645296145873845763912358974261412638597679521438",
            "123456789684937152957812364736289415291645873845371296368794521412568937579123648",
            "123456789645987123987312564854279316296138475731645298568723941312894657479561832",
            "123456789648937152957812364736249815295681473481375296364798521812564937579123648",
            "123456789654987123987132465845279316296318574731564298468723951312895647579641832",
            "123456789485937126967812534754289613291364875836175492548793261312648957679521348",
            "123456789684937152957812364736289415295641873841375296368794521412568937579123648",
            "123456789648937152957812364736249815291685473485371296364798521812564937579123648",
            "123456789654987123987132465845279316296314578731568294468723951312895647579641832",
            "123456789845937126967182534456279318298361475731845692584723961312694857679518243",
            "123456789645987123987312564854279316291638475736541298568723941312894657479165832",
            "123456789845937126967182534654279318298361475731845692586723941312694857479518263",
            "123456789648937152957812364735249816291685473486371295364798521812564937579123648",
            "123456789458937126967182345536279814294861573781543962345728691812695437679314258",
            "123456789654987123987312465845279316291638574736541298468723951312895647579164832",
            "123456789546987123987132654864279315295318476731645892658723941312894567479561238",
            "123456789845397126967812354738249615291563478456781932384975261512634897679128543",
            "123456789845937126967812345736249518298561473451378692384725961512694837679183254",
            "123456789548397126967182354735249618296813475481765932354978261812634597679521843",
            "123456789684937152957812364735289416291645873846371295368794521412568937579123648",
            "123456789458937126967182345536279814294861573781543692345728961812695437679314258",
            "123456789845937126967182534756249318298361475431875692584723961312694857679518243",
            "123456789546987123987132654864279315295318476731645298658723941312894567479561832",
            "123456789845397162967812543758249316296135478431768295584973621312684957679521834",
            "123456789845397126967182354738249615291563478456718932384975261512634897679821543",
            "123456789645987123987312564854279316291638475736145298568723941312894657479561832",
            "123456789845937126967812345436279518298561473751348692384725961512694837679183254",
            "123456789548397126967812354735249618296183475481765932354978261812634597679521843",
            "123456789845397162967812543758249316296138475431765298584973621312684957679521834",
            "123456789548397126967182354735249618291863475486715932354978261812634597679521843",
            "123456789485937126967182345836279514294561873751843962348725691512698437679314258",
            "123456789845397126967182354738249615296513478451768932384975261512634897679821543",
            "123456789654987123987132465845279316296341578731568294468723951312895647579614832",
            "123456789845397162967812543756249318298135476431768295584973621312684957679521834",
            "123456789485937126967182345836279514294561873751843692348725961512698437679314258",
            "123456789845937126967182534456279318298361475731548692584723961312694857679815243",
            "123456789845937126967182345436279518298561473751843692384725961512694837679318254",
            "123456789845937126967182534456279318291368475738541692584723961312694857679815243",
            "123456789845937126967182345736249518298561473451873692384725961512694837679318254",
            "123456789546987132987132654864279315295318476731645298658723941312894567479561823",
            "123456789645987123987312564854279316291638475736541892568723941312894657479165238",
            "123456789845397126967812534758249613296138475431765892584973261312684957679521348",
            "123456789485937162967812345734289516291564873856173294348795621512648937679321458",
            "123456789645987123987312564854279316291638475736145892568723941312894657479561238",
            "123456789485937126967812345736289514294561873851374692348725961512698437679143258",
            "123456789845937126967182534756249318291368475438571692584723961312694857679815243",
            "123456789845937126967182345736249518298561473451378692384725961512694837679813254",
            "123456789654987132987132465845279316296314578731568294468723951312895647579641823",
            "123456789485937162967812345734289516291563874856174293348795621512648937679321458",
            "123456789648937152957182364735249618296813475481675293364798521812564937579321846",
            "123456789845397162967812534756249813298135476431768295584973621312684957679521348",
            "123456789485937126967812345836279514294561873751384692348725961512698437679143258",
            "123456789845937126967182534756249318298361475431578692584723961312694857679815243",
            "123456789645987123987312564854279316296138475731645892568723941312894657479561238",
            "123456789845937126967182345436279518298561473751348692384725961512694837679813254",
            "123456789654987132987132465845279316296341578731568294468723951312895647579614823",
            "123456789546987123987312654761249835895631472234578916658793241312864597479125368",
            "123456789648937152957182364736249815295813476481675293364798521812564937579321648",
            "123456789845397162967812534758249316296135478431768295584973621312684957679521843",
            "123456789654987123987132465745219836891364572236578914468793251312845697579621348",
            "123456789485937162967812354736289415291564873854173296348795621512648937679321548",
            "123456789654987132987132465845279316296318574731564298468723951312895647579641823",
            "123456789485937126967812345836279514294561873751384962348725691512698437679143258",
            "123456789456987123987312645865279314294138576731564892648723951312895467579641238",
            "123456789648937152957182364735249816296813475481675293364798521812564937579321648",
            "123456789845397162967812534756249318298135476431768295584973621312684957679521843",
            "123456789485937162967812345736289514291564873854173296348795621512648937679321458",
            "123456789845937126967182534654279318291368475738541692586723941312694857479815263",
            "123456789485937126967812345736289514294561873851374962348725691512698437679143258",
            "123456789654987123987312465845279316291638574736541892468723951312895647579164238",
            "123456789845937126967182534654279318298361475731548692586723941312694857479815263",
            "123456789548937126967182354436279815295861473781345692354728961812694537679513248",
            "123456789648937152957182346735249618296813475481675293364798521812564937579321864",
            "123456789845397162967812534758249316296138475431765298584973621312684957679521843",
            "123456789846397152957812634764239518291685473538741926685973241312564897479128365",
            "123456789845937162967812345738249516291563478456178293384795621512684937679321854",
            "123456789845937126967182354436279518298561473751348692384725961512694837679813245",
            "123456789846397152957182634764239518298615473531748926685973241312564897479821365",
            "123456789845937126967812354436279518298561473751348692384725961512694837679183245",
            "123456789684937152957812346735289614296143875841675293368794521412568937579321468",
            "123456789456987123987132645865279314294318576731564298648723951312895467579641832",
            "123456789845937162967812345736249518291568473458173296384795621512684937679321854",
            "123456789846937125957812634765249318291385476438671592684723951312594867579168243",
            "123456789546987123987312654864279315291538476735641892658723941312894567479165238",
            "123456789456987123987132645865279314294318576731564892648723951312895467579641238",
            "123456789846397125957182634764239518298615473531748962685973241312564897479821356",
            "123456789845937162967812345738249516291568473456173298384795621512684937679321854",
            "123456789648937152957812364735249618296183475481675293364798521812564937579321846",
            "123456789845937162967812543758249316291365478436178295584793621312684957679521834",
            "123456789846937125957182634765249318298315476431678592684723951312594867579861243",
            "123456789846397125957812634764239518291685473538741962685973241312564897479128356",
            "123456789546987123987312654864279315291538476735641298658723941312894567479165832",
            "123456789845937126967812354736249518298561473451378692384725961512694837679183245",
            "123456789648937152957812346735249618296183475481675293364798521812564937579321864",
            "123456789845937162967812534758249316291365478436178295584793621312684957679521843",
            "123456789485937126967812345736289514291564873854173692348725961512698437679341258",
            "123456789645987123987132564854279316296318475731645298568723941312894657479561832",
            "123456789546987132987312654864279315291538476735641298658723941312894567479165823",
            "123456789485937126967812345736289514291564873854173962348725691512698437679341258",
            "123456789548937126967182354736249815295861473481375692354728961812694537679513248",
            "123456789645987123987132564854279316296318475731645892568723941312894657479561238",
            "123456789845937162967812354736249815291568473458173296384795621512684937679321548",
            "123456789846937125957182634465279318298315476731648592684723951312594867579861243",
            "123456789845937162967812534756249813291368475438175296584793621312684957679521348",
            "123456789845937126967182354736249518298561473451378692384725961512694837679813245",
            "123456789845937162967812354736249518291568473458173296384795621512684937679321845",
            "123456789846937125957182634564279318298315476731648592685723941312594867479861253",
            "123456789648937152957812364735249816296183475481675293364798521812564937579321648",
            "123456789854937162967182345735249618298361574641578293386724951412695837579813426",
            "123456789485937126967812345736289514291564873854371692348725961512698437679143258",
            "123456789645987123987132564854279316296315478731648295568723941312894657479561832",
            "123456789648937152957812364736249815295183476481675293364798521812564937579321648",
            "123456789845937162967812543758249316291368475436175298584793621312684957679521834",
            "123456789485937126967812345736289514291564873854371962348725691512698437679143258",
            "123456789546987132987312654864279315295138476731645298658723941312894567479561823",
            "123456789854937162967812345735249618298163574641578293386724951412695837579381426",
            "123456789845937162967812354738249516291563478456178293384795621512684937679321845",
            "123456789846937125957812634465279318291385476738641592684723951312594867579168243",
            "123456789845937162967812543756249318291368475438175296584793621312684957679521834",
            "123456789546987123987312654864279315295138476731645298658723941312894567479561832",
            "123456789546987123987132654864279315295361478731548296658723941312894567479615832",
            "123456789854937162967812345735249618298361574641578293386724951412695837579183426",
            "123456789845937162967812354738249516291568473456173298384795621512684937679321845",
            "123456789846937125957812634564279318291385476738641592685723941312594867479168253",
            "123456789548937126967182354436279815295861473781543692354728961812694537679315248",
            "123456789684937152957812364736289415295143876841675293368794521412568937579321648",
            "123456789456987123987132645865279314294361578731548296648723951312895467579614832",
            "123456789845937126967182354436279518298561473751843692384725961512694837679318245",
            "123456789684937152957812364735289416296143875841675293368794521412568937579321648",
            "123456789845937162967812534758249316291368475436175298584793621312684957679521843",
            "123456789845937126967812345736249518291568473458173962384725691512694837679381254",
            "123456789654987132987312465845279316296138574731564298468723951312895647579641823",
            "123456789845937162967812534756249318291368475438175296584793621312684957679521843",
            "123456789845937126967812345736249518291568473458371962384725691512694837679183254",
            "123456789654987123987312465845279316296138574731564298468723951312895647579641832",
            "123456789854937162967812345735249618291368574648571293386724951412695837579183426",
            "123456789548937126967182354736249815295861473481573692354728961812694537679315248",
            "123456789854937162967182345735249618291368574648571293386724951412695837579813426",
            "123456789845937126967182354736249518298561473451873692384725961512694837679318245",
            "123456789546987132987132654864279315295361478731548296658723941312894567479615823",
            "123456789845937126967812345736249518291568473458371692384725961512694837679183254",
            "123456789654987123987312465845279316296138574731564892468723951312895647579641238",
            "123456789648937152957182364735249816296815473481673295364798521812564937579321648",
            "123456789456987132987132645865279314294361578731548296648723951312895467579614823",
            "123456789854937162967182345735249618291863574648571293386724951412695837579318426",
            "123456789845937126967812345736249518291568473458173692384725961512694837679381254",
            "123456789845937126967812543456279318291368475738541692584723961312694857679185234",
            "123456789546987123987312654864279315295138476731645892658723941312894567479561238",
            "123456789845937126967812534756249813291368475438175692584793261312684957679521348",
            "123456789845937126967812543456279318291368475738145692584723961312694857679581234",
            "123456789648937152957812364735249816296185473481673295364798521812564937579321648",
            "123456789645987132987132564854279316296315478731648295568723941312894657479561823",
            "123456789684937152957812364835279416291645873746381295368794521412568937579123648",
            "123456789684937152957812364735289416296145873841673295368794521412568937579321648",
            "123456789845937126967812543756249318291368475438175692584793261312684957679521834",
            "123456789684937152957812364835279416296341875741685293368794521412568937579123648",
            "123456789845937126967812543756249318291368475438571692584723961312694857679185234",
            "123456789456987132987132645865279314294318576731564298648723951312895467579641823",
            "123456789845937126967812534756249318291368475438175692584793261312684957679521843",
            "123456789854937126967182345735249618298361574641578293386724951412695837579813462",
            "123456789845937126967812345436279518298561473751348962384725691512694837679183254",
            "123456789845937126967812543756249318291368475438175692584723961312694857679581234",
            "123456789645987132987132564854279316296318475731645298568723941312894657479561823",
            "123456789854937126967812345735249618298361574641578293386724951412695837579183462",
            "123456789546987123987312654864279315295631478731548296658723941312894567479165832",
            "123456789845937126967182345436279518298561473751348962384725691512694837679813254",
            "123456789485937126967812345836279514291564873754183962348725691512698437679341258",
            "123456789845937126967812534758249613291368475436175892584793261312684957679521348",
            "123456789854937162967812354536279418298341576741568293385794621412685937679123845",
            "123456789546987123987312654864279315291635478735148296658723941312894567479561832",
            "123456789648937152957812346735249618296185473481673295364798521812564937579321864",
            "123456789854937126967812345735249618298163574641578293386724951412695837579381462",
            "123456789845937126967812345436279518291568473758143962384725691512694837679381254",
            "123456789854937162967812354536279418291348576748561293385794621412685937679123845",
            "123456789845937126967182543456279318291368475738541692584723961312694857679815234",
            "123456789845937126967182345736249518298561473451378962384725691512694837679813254",
            "123456789648937152957812364735249618296185473481673295364798521812564937579321846",
            "123456789854937126967812354531279468296148573748563912385724691412695837679381245",
            "123456789845937126967182543756249318291368475438571692584723961312694857679815234",
            "123456789654987123987312465845279316296134578731568294468723951312895647579641832",
            "123456789845937126967812345736249518298561473451378962384725691512694837679183254",
            "123456789854937126967182345735249618291863574648571293386724951412695837579318462",
            "123456789684937152957812346735289614296145873841673295368794521412568937579321468",
            "123456789456987123987312645865279314294631578731548296648723951312895467579164832",
            "123456789485937162967812345836279514294561873751384296348795621512648937679123458",
            "123456789854937126967812345735249618291368574648571293386724951412695837579183462",
            "123456789485937126967182345836279514291564873754813962348725691512698437679341258",
            "123456789845937162967812345436279518298561473751348296384795621512684937679123854",
            "123456789846937125957812643465279318291385476738641592684723951312594867579168234",
            "123456789648937152957182346735249618296815473481673295364798521812564937579321864",
            "123456789845937162967812534758249316291368475436571298584793621312684957679125843",
            "123456789854937126967182345735249618291368574648571293386724951412695837579813462",
            "123456789485937126967182345831279564296514873754863912348725691512698437679341258",
            "123456789846937125957812643765249318291385476438671592684723951312594867579168234",
            "123456789845937126967182354736249518298561473451378962384725691512694837679813245",
            "123456789648937152957182364735249618296815473481673295364798521812564937579321846",
            "123456789546987132987312654864279315291635478735148296658723941312894567479561823",
            "123456789845937162967812543758249316291368475436571298584793621312684957679125834",
            "123456789485937162967812345836279514291564873754381296348795621512648937679123458",
            "123456789548937126967182354736249815295861473481375962354728691812694537679513248",
            "123456789654987132987312465845279316296134578731568294468723951312895647579641823",
            "123456789485937162967812543754289316291364875836571294548793621312648957679125438",
            "123456789845937162967812345436279518291568473758341296384795621512684937679123854",
            "123456789845937126967812534756249318291368475438571962584723691312694857679185243",
            "123456789485937126967182345836279514291564873754813692348725961512698437679341258",
            "123456789845937126967182543456279318298361475731845692584723961312694857679518234",
            "123456789845937126967182354436279518298561473751348962384725691512694837679813245",
            "123456789845937126967182534756249318291368475438571962584723691312694857679815243",
            "123456789485937126967812345836279514291564873754183692348725961512698437679341258",
            "123456789546987132987312654864279315295631478731548296658723941312894567479165823",
            "123456789654987123987132465745219836291368574836574912468793251312845697579621348",
            "123456789548937126967182354436279815295861473781345962354728691812694537679513248",
            "123456789846937125957182643465279318298315476731648592684723951312594867579861234",
            "123456789456987132987312645865279314294631578731548296648723951312895467579164823",
            "123456789546987123987132654761249835295318476834675912658793241312864597479521368",
            "123456789845937126967812345436279518291568473758143692384725961512694837679381254",
            "123456789648937152957812364735249618291685473486173295364798521812564937579321846",
            "123456789485937162967812534756289413294361875831574296548793621312648957679125348",
            "123456789845937126967812543756249318291368475438571962584723691312694857679185234",
            "123456789845937126967812543456279318298361475731548692584723961312694857679185234",
            "123456789485937162967812543756289314294361875831574296548793621312648957679125438",
            "123456789684937152957812364835279416296145873741683295368794521412568937579321648",
            "123456789845937126967812354736249518298561473451378962384725691512694837679183245",
            "123456789648937152957812346735249618291685473486173295364798521812564937579321864",
            "123456789485937126967812543756289314291364875834571962548723691312698457679145238",
            "123456789845937126967182543456279318298361475731548692584723961312694857679815234",
            "123456789684937152957812364835279416296143875741685293368794521412568937579321648",
            "123456789845937126967812354436279518298561473751348962384725691512694837679183245",
            "123456789684937152957812346735289614291645873846173295368794521412568937579321468",
            "123456789845937126967812345436279518291568473758341692384725961512694837679183254",
            "123456789845937126967182543756249318291368475438571962584723691312694857679815234",
            "123456789845937162967812543756249318298361475431578296584793621312684957679125834",
            "123456789458937162967812345735289614294361578681574293346728951812695437579143826",
            "123456789485937126967812345836279514291564873754381692348725961512698437679143258",
            "123456789845937162967812534756249813298361475431578296584793621312684957679125348",
            "123456789458937162967812345735289614291364578684571293346728951812695437579143826",
            "123456789684937152957182364835279416291645873746813295368794521412568937579321648",
            "123456789648937152957812364735249816291685473486173295364798521812564937579321648",
            "123456789845937162967812534756249318298361475431578296584793621312684957679125843",
            "123456789845937126967182543756249318298361475431875692584723961312694857679518234",
            "123456789684937152957812364835279416291645873746183295368794521412568937579321648",
            "123456789485937126967812345836279514291564873754381962348725691512698437679143258",
            "123456789548937126967182354736249815295861473481573962354728691812694537679315248",
            "123456789684937152957812364735289416291645873846173295368794521412568937579321648",
            "123456789485937162967812543756289314291364875834571926548723691312698457679145238",
            "123456789485937162967812543756289314291364875834571296548793621312648957679125438",
            "123456789458937126967812345735289614294361578681574293346728951812695437579143862",
            "123456789845937126967812345436279518291568473758341962384725691512694837679183254",
            "123456789846937125957182643765249318298315476431678592684723951312594867579861234",
            "123456789458937126967812345735289614291364578684571293346728951812695437579143862",
            "123456789485937162967812534756289413291364875834571296548793621312648957679125348",
            "123456789845937126967182345736249518298561473451873962384725691512694837679318254",
            "123456789845937162967812543756249318291368475438571926584723691312694857679185234",
            "123456789684937152957812364835279416291643875746185293368794521412568937579321648",
            "123456789845937126967182543756249318298361475431578692584723961312694857679815234",
            "123456789845937126967182354736249518298561473451873962384725691512694837679318245",
            "123456789648937152957812364736249815291685473485173296364798521812564937579321648",
            "123456789684937152957812364736289415291645873845173296368794521412568937579321648",
            "123456789845937162967812534756249318291368475438571926584723691312694857679185243",
            "123456789684937152957182364835279416291643875746815293368794521412568937579321648",
            "123456789458937126967812345735289614294163578681574293346728951812695437579341862",
            "123456789845937162967812534756249318291368475438571296584793621312684957679125843",
            "123456789845937126967812543756249318298361475431578692584723961312694857679185234",
            "123456789845937162967812534756249813291368475438571296584793621312684957679125348",
            "123456789458937162967812345735289614294163578681574293346728951812695437579341826",
            "123456789845937162967182534756249318291368475438571926584723691312694857679815243",
            "123456789845937162967812543756249318291368475438571296584793621312684957679125834",
            "123456789845937126967812354736249518291568473458371962384725691512694837679183245",
            "123456789548937126967182354436279815295861473781543962354728691812694537679315248",
            "123456789684937152957812364735289416291643875846175293368794521412568937579321648",
            "123456789845937162967182543756249318291368475438571926584723691312694857679815234",
            "123456789854937162967812354536279418298143576741568293385794621412685937679321845",
            "123456789845937126967812354436279518291568473758341962384725691512694837679183245",
            "123456789845937126967182354436279518298561473751843962384725691512694837679318245",
            "123456789684937152957812346735289614291643875846175293368794521412568937579321468",
            "123456789654987132987312465745239816291648573836571294468723951312895647579164328",
            "123456789456987132987312645765239814291648573834571296648723951312895467579164328",
            "123456789485937162967182345836279514294513876751864293348795621512648937679321458",
            "123456789845937126967182345436279518298561473751843962384725691512694837679318254",
            "123456789456987132987312645765239814294168573831574296648723951312895467579641328",
            "123456789845937126967812354736249518291568473458371692384725961512694837679183245",
            "123456789648937152957812346735249618291683475486175293364798521812564937579321864",
            "123456789845937126967812354436279518291568473758341692384725961512694837679183245",
            "123456789645987132987132564758249316291368475436571928564723891312894657879615243",
            "123456789486937152957812643765289314291345876834671925648723591312598467579164238",
            "123456789485937162967812345836279514291564873754183296348795621512648937679321458",
            "123456789648937152957812364735249816291683475486175293364798521812564937579321648",
            "123456789485937126967812534756289413294361875831574692548793261312648957679125348",
            "123456789486937125957812643765289314291345876834671952648723591312598467579164238",
            "123456789485937126967812534756289413291364875834571692548793261312648957679125348",
            "123456789846937152957812634765249318291385476438671925684723591312594867579168243",
            "123456789485937162967182345836279514291564873754813296348795621512648937679321458",
            "123456789648937152957812364735249618291683475486175293364798521812564937579321846",
            "123456789845937126967812534756249813298361475431578692584793261312684957679125348",
            "123456789458937126967182345536279814291864573784513692345728961812695437679341258",
            "123456789845937126967812534756249813291368475438571692584793261312684957679125348",
            "123456789546987123987312654761249835295138476834675912658793241312864597479521368",
            "123456789846937125957812634765249318291385476438671952684723591312594867579168243",
            "123456789845937162967812345436279518291568473758143296384795621512684937679321854",
            "123456789846937125957812643765249318291385476438671952684723591312594867579168234",
            "123456789548397126967812354734289615291563478685741932356978241812634597479125863",
            "123456789845937126967812354436279518291568473758143692384725961512694837679381245",
            "123456789548397162967812354734289615291563478685741923356978241812634597479125836",
            "123456789846937152957812643765249318291385476438671925684723591312594867579168234",
            "123456789845937126967812534758249613291368475436571892584793261312684957679125348",
            "123456789845937126967812354736249518291568473458173692384725961512694837679381245",
            "123456789485937126967812534754289613291364875836571492548793261312648957679125348",
            "123456789546987132987312654768249315291538476435671928654723891312894567879165243",
            "123456789645987132987312564758249316291638475436571928564723891312894657879165243",
            "123456789548397126967812354734289615295163478681745932356978241812634597479521863",
            "123456789548397162967812354734289615295163478681745923356978241812634597479521836",
            "123456789456987132987312645765239814291648573834571926648793251312865497579124368",
            "123456789845937162967812345438279516291568473756341298384795621512684937679123854",
            "123456789458937126967182345536279814291864573784513962345728691812695437679341258",
            "123456789845937126967812534756249318298361475431578692584793261312684957679125843",
            "123456789485937126967812543756289314291364875834571692548793261312648957679125438",
            "123456789845937162967812345438279516291563478756148293384795621512684937679321854",
            "123456789854937126967812345531279468296148573748563912385724691412695837679381254",
            "123456789854937162967812354538279416296341578741568293385794621412685937679123845",
            "123456789485937162967182345834279516296514873751863294348795621512648937679321458",
            "123456789845937126967812543756249318291368475438571692584793261312684957679125834",
            "123456789845937126967812354736249518291568473458173962384725691512694837679381245",
            "123456789485937126967812543756289314294361875831574692548793261312648957679125438",
            "123456789854937162967812354538279416296143578741568293385794621412685937679321845",
            "123456789458937126967812345531279864296184573784563912345728691812695437679341258",
            "123456789485937162967182345834279516291564873756813294348795621512648937679321458",
            "123456789485937126967812543756289314291364875834175962548723691312698457679541238",
            "123456789845937126967812354436279518291568473758143962384725691512694837679381245",
            "123456789845937126967812543756249318298361475431578692584793261312684957679125834",
            "123456789845937126967812534756249318291368475438571692584793261312684957679125843",
            "123456789458937126967182345531279864296814573784563912345728691812695437679341258",
            "123456789845937126967812543756249318291368475438175962584723691312694857679581234",
            "123456789845937162967812345438279516291568473756143298384795621512684937679321854",
            "123456789485937162967812345834279516291564873756381294348795621512648937679123458",
            "123456789854937162967812354538279416296148573741563298385794621412685937679321845",
            "123456789485937162967812345834279516291564873756183294348795621512648937679321458",
            "123456789845937162967812543756249318291368475438175926584723691312694857679581234",
            "123456789854937162967812345538279416296341578741568293385794621412685937679123854",
            "123456789485937162967812543756289314291364875834175926548723691312698457679541238",
            "123456789854937162967812345538279416296143578741568293385794621412685937679321854",
            "123456789548397126967812354735249618691583472284761935356978241812634597479125863",
            "123456789854937162967812345538279416296148573741563298385794621412685937679321854",
            "123456789684937152957812364836279415295641873741385296368794521412568937579123648",
            "123456789854937162967812345536279418291348576748561293385794621412685937679123854",
            "123456789845397162967812534756249813291538476438761295584973621312684957679125348",
            "123456789845397162967812534756249318291538476438761295584973621312684957679125843",
            "123456789654987132987132465745269813296318574831574926468793251312845697579621348",
            "123456789845397126967812354738249615691583472254761938386975241512634897479128563",
            "123456789845937126967182345436279518291568473758341692384725961512694837679813254",
            "123456789854937162967812345536279418298341576741568293385794621412685937679123854",
            "123456789485397162967812534756289413291534876834761295548973621312648957679125348",
            "123456789845397162967812543756249318291538476438761295584973621312684957679125834",
            "123456789654987132987312465745269813296138574831574926468793251312845697579621348",
            "123456789854937162967812345536279418298143576741568293385794621412685937679321854",
            "123456789845937162967812534756249318291368475438175926584723691312694857679581243",
            "123456789845937126967182345436279518291568473758341962384725691512694837679813254",
            "123456789684937152957812364836279415291645873745183296368794521412568937579321648",
            "123456789845397126967812354738249615291563478654781932386975241512634897479128563",
            "123456789645987132987312564758249316291638475436175928564723891312894657879561243",
            "123456789485397162967812543756289314291534876834761295548973621312648957679125438",
            "123456789546987132987312654765249813291638475834175926658793241312864597479521368",
            "123456789684937152957182364836279415291645873745813296368794521412568937579321648",
            "123456789845397126967812354734289615291563478658741932386975241512634897479128563",
            "123456789845937162967812354438279516291563478756148293384795621512684937679321845",
            "123456789845937162967812354436279518298561473751348296384795621512684937679123845",
            "123456789845937126967182354436279518291568473758341962384725691512694837679813245",
            "123456789845937162967812354438279516291568473756143298384795621512684937679321845",
            "123456789845937126967812534756249318291368475438175962584723691312694857679581243",
            "123456789684937152957812364836279415291645873745381296368794521412568937579123648",
            "123456789845937126967182354436279518291568473758341692384725961512694837679813245",
            "123456789845937162967812354436279518291568473758341296384795621512684937679123845",
            "123456789845937162967812354436279518291568473758143296384795621512684937679321845",
            "123456789845937162967812354438279516291568473756341298384795621512684937679123845",
            "123456789485397162967812534756289413294531876831764295548973621312648957679125348",
            "123456789845397162967812354738249615291563478654781923386975241512634897479128536",
            "123456789846397152957182634768249513291635478534718926685973241312564897479821365",
            "123456789584937162967812453746289315291365874835174926458723691312698547679541238",
            "123456789485397162967812543756289314294531876831764295548973621312648957679125438",
            "123456789845397162967812354734289615291563478658741923386975241512634897479128536",
            "123456789584937126967812453746289315291365874835174962458723691312698547679541238",
            "123456789846397125957182634768249513291635478534718962685973241312564897479821356",
            "123456789458937162967812345536279814294381576781564293345798621812645937679123458",
            "123456789845937126967182345736249518291568473458371692384725961512694837679813254",
            "123456789684937152957812364836279415295341876741685293368794521412568937579123648",
            "123456789684937152957812463745289316291365874836174925468723591312598647579641238",
            "123456789684937125957812463745289316291365874836174952468723591312598647579641238",
            "123456789458937162967812345536279814291384576784561293345798621812645937679123458",
            "123456789845937126967182345736249518291568473458371962384725691512694837679813254",
            "123456789684937152957812364836279415291345876745681293368794521412568937579123648",
            "123456789845397162967812354734289615298561473651743928386975241512634897479128536",
            "123456789845397162967812534756249813298531476431768295584973621312684957679125348",
            "123456789845397162967812534756249318298531476431768295584973621312684957679125843",
            "123456789845397162967182354738249615291568473654713928386975241512634897479821536",
            "123456789458937162967812345534279816296381574781564293345798621812645937679123458",
            "123456789845397162967182354738249615291563478654718923386975241512634897479821536",
            "123456789845397162967812543756249318298531476431768295584973621312684957679125834",
            "123456789845937126967182354736249518291568473458371692384725961512694837679813245",
            "123456789845937126967182354736249518291568473458371962384725691512694837679813245",
            "123456789485937162967812345834279516291563874756184293348795621512648937679321458",
            "123456789845397126967182354738249615291563478654718932386975241512634897479821563",
            "123456789456987132987312645765249813291638574834571926648793251312865497579124368",
            "123456789684937152957812463745289316291364875836175924468723591312598647579641238",
            "123456789645987132987312564758249316291635478436178925564723891312894657879561243",
            "123456789485937162967182345834279516291563874756814293348795621512648937679321458",
            "123456789846937152957812643765249318291368475438175926684723591312594867579681234",
            "123456789458937162967182345536279814291864573784513296345798621812645937679321458",
            "123456789546987132987312654765249813291538476834671925658793241312864597479125368",
            "123456789485937162967182345834279516296513874751864293348795621512648937679321458",
            "123456789846937152957812634765249318291368475438175926684723591312594867579681243",
            "123456789486937152957812643765289314291364875834175926648723591312598467579641238",
            "123456789845397162967812534758249316296531478431768295584973621312684957679125843",
            "123456789548397126967182354735249618291863475684715932356978241812634597479521863",
            "123456789548937126967182354436279815291865473785341692354728961812694537679513248",
            "123456789546987132987312654765249813291638475834571926658793241312864597479125368",
            "123456789458937162967182345534279816291864573786513294345798621812645937679321458",
            "123456789546987132987312654768249315291635478435178926654723891312894567879561243",
            "123456789845397162967812543758249316296531478431768295584973621312684957679125834",
            "123456789548937126967182354436279815291865473785341962354728691812694537679513248",
            "123456789458937162967182345534279816296814573781563294345798621812645937679321458",
            "123456789684937152957812364836279415295143876741685293368794521412568937579321648",
            "123456789485397162967812543754289316296531874831764295548973621312648957679125438",
            "123456789548397162967182354735249618291865473684713925356978241812634597479521836",
            "123456789548937126967182354736249815291865473485371962354728691812694537679513248",
            "123456789854937162967812435745289613298361574631574928486793251312645897579128346",
            "123456789846937152957182643765249318298315476431678925684723591312594867579861234",
            "123456789845937162967182543756249318298361475431578926584723691312694857679815234",
            "123456789648397152957182364736249518291865473584713926365978241812534697479621835",
            "123456789546987132987312654768249315295138476431675928654723891312894567879561243",
            "123456789458937162967182345534279816291863574786514293345798621812645937679321458",
            "123456789548937126967182354736249815291865473485371692354728961812694537679513248",
            "123456789845937162967812543756249318298361475431578926584723691312694857679185234",
            "123456789548397162967182354735249618291863475684715923356978241812634597479521836",
            "123456789846397152957812634768249513291635478534781926685973241312564897479128365",
            "123456789546987132987132654768249315295318476431675928654723891312894567879561243",
            "123456789854937162967812435745289613291364578638571924486793251312645897579128346",
            "123456789846397152957812634764289513291635478538741926685973241312564897479128365",
            "123456789458937162967182345534279816296813574781564293345798621812645937679321458",
            "123456789846937152957182634765249318298315476431678925684723591312594867579861243",
            "123456789846937152957812634768249513291385476534671928685793241312564897479128365",
            "123456789648397152957182364734269518296815473581743926365978241812534697479621835",
            "123456789458937162967182345536279814294813576781564293345798621812645937679321458",
            "123456789845937162967182543756249318298361475431875926584723691312694857679518234",
            "123456789846937152957182643765249318298361475431875926684723591312594867579618234",
            "123456789845937162967812534756249318298361475431578926584723691312694857679185243",
            "123456789648397152957812364734269518296185473581743926365978241812534697479621835",
            "123456789846397125957812634768249513291635478534781962685973241312564897479128356",
            "123456789486937125957182634864279513295314876731865492648793251312548967579621348",
            "123456789486937125957182643865279314294315876731864592648793251312548967579621438",
            "123456789845937162967182534756249318298361475431578926584723691312694857679815243",
            "123456789854937126967812453546279318291368574738541692485793261312685947679124835",
            "123456789485937162967812543756289314294361875831574926548723691312698457679145238",
            "123456789458937162967812345534279816296183574781564293345798621812645937679321458",
            "123456789548937126967182354634279815295861473781543692356728941812694537479315268",
            "123456789846397125957812634764289513291635478538741962685973241312564897479128356",
            "123456789485937126967182543856279314291364875734815692548793261312648957679521438",
            "123456789854937126967812453546279318298361574731548692485793261312685947679124835",
            "123456789485937126967182534854279613296314875731865492548793261312648957679521348",
            "123456789584937162967812453746289315295361874831574926458723691312698547679145238",
            "123456789458937162967812345536279814294183576781564293345798621812645937679321458",
            "123456789845937126967182354634279518298561473751843692386725941512694837479318265",
            "123456789648397125957182364736249518591863472284715936365978241812534697479621853",
            "123456789846397125957812634764289513598631472231745968685973241312564897479128356",
            "123456789485937126967182534854279613291364875736815492548793261312648957679521348",
            "123456789645987132987312564758249316296138475431675928564723891312894657879561243",
            "123456789684937152957812463745289316296341875831675924468723591312598647579164238",
            "123456789486937125957182634865279413294315876731864592648793251312548967579621348",
            "123456789458937162967812345534279816296184573781563294345798621812645937679321458",
            "123456789648397125957812364734269518596183472281745936365978241812534697479621853",
            "123456789845937126967182354634279518298561473751348692386725941512694837479813265",
            "123456789854937126967812435745289613291364578638571942486793251312645897579128364",
            "123456789485937126967182534856279413291364875734815692548793261312648957679521348",
            "123456789845937162967182534756249318298361475431875926584723691312694857679518243",
            "123456789854937126967812435546279813291368574738541692485793261312685947679124358",
            "123456789648397125957182364734269518596813472281745936365978241812534697479621853",
            "123456789548937126967182354634279815295861473781345692356728941812694537479513268",
            "123456789645987132987132564758249316296318475431675928564723891312894657879561243",
            "123456789854937126967812435548279613291368574736541892485793261312685947679124358",
            "123456789846397125957182634768249513591638472234715968685973241312564897479821356",
            "123456789845937126967812354634279518298561473751348692386725941512694837479183265",
            "123456789854937126967812435546279813298361574731548692485793261312685947679124358",
            "123456789584937126967812453746289315295361874831574962458723691312698547679145238",
            "123456789846937125957182634765249318298315476431678952684723591312594867579861243",
            "123456789648937152957812364436279815291685473785143296364798521812564937579321648",
            "123456789846937125957182643765249318298315476431678952684723591312594867579861234",
            "123456789485937126967812543756289314294361875831574962548723691312698457679145238",
            "123456789458937126967182345635279814294861573781543692346728951812695437579314268",
            "123456789648937152957182364436279815295813476781645293364798521812564937579321648",
            "123456789854937126967812435546279318298361574731548692485793261312685947679124853",
            "123456789645987132987312564758249316296531478431678925564723891312894657879165243",
            "123456789845937126967812543756249318298361475431578962584723691312694857679185234",
            "123456789486937152957182643864279315295314876731865294648793521312548967579621438",
            "123456789648937152957812364436279815295183476781645293364798521812564937579321648",
            "123456789854937126967812435546279318291368574738541692485793261312685947679124853",
            "123456789845937126967812534756249318298361475431578962584723691312694857679185243",
            "123456789546987132987312654768249315295631478431578926654723891312894567879165243",
            "123456789548937126967182354634279815291865473785341692356728941812694537479513268",
            "123456789645987132987132564456279318298315476731648295564793821312864957879521643",
            "123456789485937162967182543854279316296315874731864295548793621312648957679521438",
            "123456789485937162967182543854279316296314875731865294548793621312648957679521438",
            "123456789458937126967182345635279814291864573784513692346728951812695437579341268",
            "123456789456987132987132645564279318291368574738514296645793821312845967879621453",
            "123456789845937126967182543756249318298361475431875962584723691312694857679518234",
            "123456789546987132987132654768249315295361478431578926654723891312894567879615243",
            "123456789648937152957812364436279815291385476785641293364798521812564937579123648",
            "123456789846937125957812634768249513291385476534671298685723941312594867479168352",
            "123456789845937126967182534756249318298361475431875962584723691312694857679518243",
            "123456789845937126967812534758249613291368475634175298586723941312694857479581362",
            "123456789456987132987132645564279318298314576731568294645793821312845967879621453",
            "123456789648937152957812364436279815295381476781645293364798521812564937579123648",
            "123456789845937126967182354634279518291568473758341692386725941512694837479813265",
            "123456789645987132987132564758249316296315478431678925564723891312894657879561243",
            "123456789485937162967182543854279316291364875736815294548793621312648957679521438",
            "123456789845937126967812534456279813291368475738145692584793261312684957679521348",
            "123456789845937126967812534758249613291368475634571298586723941312694857479185362",
            "123456789645987132987312564758249316296135478431678925564723891312894657879561243",
            "123456789845937162967812543458279316291368475736145298584793621312684957679521834",
            "123456789845937126967182543756249318298361475431578962584723691312694857679815234",
            "123456789845937126967812354634279518291568473758341692386725941512694837479183265",
            "123456789845937126967182534758249613291368475634571298586723941312694857479815362",
            "123456789648937152957812364436279815291685473785341296364798521812564937579123648",
            "123456789845937126967812534456279813291368475738541692584793261312684957679125348",
            "123456789456987132987312645564279318298134576731568294645793821312845967879621453",
            "123456789485937162967812543854279316291364875736185294548793621312648957679521438",
            "123456789845937126967182534756249318298361475431578962584723691312694857679815243",
            "123456789845937126967812354634279518291568473758143692386725941512694837479381265",
            "123456789845937162967812543458279316291365478736148295584793621312684957679521834",
            "123456789648937152957812364436279815295681473781345296364798521812564937579123648",
            "123456789845937126967812534456279813298361475731548692584793261312684957679125348",
            "123456789645987132987312564456279318291638475738145296564793821312864957879521643",
            "123456789846937152957182634765249318298361475431875926684723591312594867579618243",
            "123456789846937152957812643468279315291365478735148296684793521312584967579621834",
            "123456789854937126967812435745289613298361574631574298486723951312695847579148362",
            "123456789645987132987312564456279318298135476731648295564793821312864957879521643",
            "123456789485937126967812534856279413294361875731584692548793261312648957679125348",
            "123456789485937126967812534856279413291364875734185692548793261312648957679521348",
            "123456789485937162967182543854279316291365874736814295548793621312648957679521438",
            "123456789648937152957812364435279816291685473786341295364798521812564937579123648",
            "123456789486937152957182643864279315291365874735814296648793521312548967579621438",
            "123456789485937126967812534856279413291364875734581692548793261312648957679125348",
            "123456789485937162967812543854279316291365874736184295548793621312648957679521438",
            "123456789648937152957812364435279816296381475781645293364798521812564937579123648",
            "123456789845937162967812534458279316291365478736148295584793621312684957679521843",
            "123456789486937152957812643864279315291365874735184296648793521312548967579621438",
            "123456789648937125957812364736249518291685473584173296365728941812594637479361852",
            "123456789845937126967182354738249615291568473654371298386725941512694837479813562",
            "123456789458937126967182345735269814691843572284571693346728951812695437579314268",
            "123456789845937162967812534458279316291368475736145298584793621312684957679521843",
            "123456789845937126967812354738249615291568473654371298386725941512694837479183562",
            "123456789458937126967812345735269814691384572284571693346728951812695437579143268",
            "123456789456987132987132645568279314291364578734518926645723891312895467879641253",
            "123456789648937125957182364736249518291865473584371296365728941812594637479613852",
            "123456789654987132987132465548279316291364578736518924465723891312895647879641253",
            "123456789846937152957812634468279315291365478735148296684793521312584967579621843",
            "123456789845937126967812354738249615291568473654173298386725941512694837479381562",
            "123456789648937152957812364435279816296183475781645293364798521812564937579321648",
            "123456789548937126967182354735249618291865473684371295356728941812694537479513862",
            "123456789458937126967812345735269814694381572281574693346728951812695437579143268",
            "123456789648937152957812364435279816296185473781643295364798521812564937579321648",
            "123456789458937126967182345735269814694813572281574693346728951812695437579341268",
            "123456789485937126967812543856279314291364875734581692548793261312648957679125438",
            "123456789648937125957812364736249518291685473584371296365728941812594637479163852",
            "123456789654987132987132465548279316291368574736514928465723891312895647879641253",
            "123456789458937126967182345735249618294861573681573294346728951812695437579314862",
            "123456789458937126967812345735269814694183572281574693346728951812695437579341268",
            "123456789845937126967812543456279318291368475738541692584793261312684957679125834",
            "123456789654987132987132465548279316291368574736541928465723891312895647879614253",
            "123456789648937152957812364435279816291683475786145293364798521812564937579321648",
            "123456789845937162967812534456279813291368475738145296584793621312684957679521348",
            "123456789645987132987132564458279316291368475736541928564723891312894657879615243",
            "123456789648937152957812364435279816291685473786143295364798521812564937579321648",
            "123456789845937126967812543456279318298361475731548692584793261312684957679125834",
            "123456789846937152957812634465279813291368475738145296684793521312584967579621348",
            "123456789645987132987132564458279316296315478731648295564793821312864957879521643",
            "123456789645987132987132546458279613296318475731645298564793821312864957879521364",
            "123456789645987132987132564458279316296318475731645298564793821312864957879521643",
            "123456789456987132987132645568279314291348576734561928645723891312895467879614253",
            "123456789485937126967812543856279314294361875731584692548793261312648957679125438",
            "123456789645987132987132564458279613296318475731645298564793821312864957879521346",
            "123456789458937162967182345735249618294861573681573294346728951812695437579314826",
            "123456789645987132987132564458279613296315478731648295564793821312864957879521346",
            "123456789854937126967812345735269418698341572241578693386724951412695837579183264",
            "123456789648937152957182364435279816296813475781645293364798521812564937579321648",
            "123456789845937162967812534456279318291368475738145296584793621312684957679521843",
            "123456789645987132987132546458279613296315478731648295564793821312864957879521364",
            "123456789845937162967812354738249615291568473654173298386725941512694837479381526",
            "123456789854937126967812345735269418691348572248571693386724951412695837579183264",
            "123456789845937162967182534758249613291368475634571298586723941312694857479815326",
            "123456789648937152957182364435279816296815473781643295364798521812564937579321648",
            "123456789485937126967812543856279314291364875734185692548793261312648957679521438",
            "123456789845937162967812543456279318291368475738145296584793621312684957679521834",
            "123456789548937162967182354735249618291865473684371295356728941812694537479513826",
            "123456789654987132987312465548279316291638574736541928465723891312895647879164253",
            "123456789854937126967812345735269418698143572241578693386724951412695837579381264",
            "123456789845937126967812543456279318291368475738145692584793261312684957679521834",
            "123456789645987132987312546458279613296138475731645298564793821312864957879521364",
            "123456789645987132987312564458279316291638475736541928564723891312894657879165243",
            "123456789645987132987312564458279613296138475731645298564793821312864957879521346",
            "123456789846937152957812634768249513291385476534671298685723941312594867479168325",
            "123456789846937152957812634465279318291368475738145296684793521312584967579621843",
            "123456789845937162967812354738249615291568473654371298386725941512694837479183526",
            "123456789645987132987312564458279316296138475731645298564793821312864957879521643",
            "123456789846937152957812643465279318291368475738145296684793521312584967579621834",
            "123456789845937162967182354738249615291568473654371298386725941512694837479813526",
            "123456789854937126967182345735269418698341572241578693386724951412695837579813264",
            "123456789845937162967812534758249613291368475634175298586723941312694857479581326",
            "123456789854937126967182345735269418691843572248571693386724951412695837579318264",
            "123456789845937126967812534456279318291368475738145692584793261312684957679521843",
            "123456789645987132987312564458279316291638475736145928564723891312894657879561243",
            "123456789845937162967812534758249613291368475634571298586723941312694857479185326",
            "123456789854937126967182345735269418691348572248571693386724951412695837579813264",
            "123456789645987132987312564458279316291635478736148925564723891312894657879561243",
            "123456789845937126967812534456279318291368475738541692584793261312684957679125843",
            "123456789645987132987312564458279613296135478731648295564793821312864957879521346",
            "123456789845937126967812534456279318298361475731548692584793261312684957679125843",
            "123456789645987132987312564458279316296135478731648295564793821312864957879521643",
            "123456789854937162967812435745289613298361574631574298486723951312695847579148326",
            "123456789648937152957812364736249518291685473584371296365728941812594637479163825",
            "123456789485937162967812354836279415294561873751384296348795621512648937679123548",
            "123456789458937162967182354536279418291864573784513296345798621812645937679321845",
            "123456789546987132987312654468279315291635478735148926654723891312894567879561243",
            "123456789645987132987312546458279613296135478731648295564793821312864957879521364",
            "123456789648937152957812364736249518291685473584173296365728941812594637479361825",
            "123456789845937162967812354436279815298561473751348296384795621512684937679123548",
            "123456789546987132987312654468279315291538476735641928654723891312894567879165243",
            "123456789458937162967812354536279418291384576784561293345798621812645937679123845",
            "123456789648937152957182364736249518291865473584371296365728941812594637479613825",
            "123456789845397126967812354734269518691583472258741963386975241512634897479128635",
            "123456789654987132987312465745269813296138574831574296468723951312895647579641328",
            "123456789648937152957812346435279618291685473786341295364798521812564937579123864",
            "123456789485937126967812534854279613291364875736185492548793261312648957679521348",
            "123456789648937152957182364435279618296815473781643295364798521812564937579321846",
            "123456789548397126967812354734269815691583472285741963356978241812634597479125638",
            "123456789645987132987312564458279316291635478736148295564793821312864957879521643",
            "123456789654987132987132465745269813296318574831574296468723951312895647579641328",
            "123456789845937126967812534458279613291368475736145892584793261312684957679521348",
            "123456789648937152957182346435279618296815473781643295364798521812564937579321864",
            "123456789845397126967182354734269518698513472251748963386975241512634897479821635",
            "123456789648937152957812346435279618291683475786145293364798521812564937579321864",
            "123456789645987132987312564458279316291638475736145298564793821312864957879521643",
            "123456789548397126967182354734269815695813472281745963356978241812634597479521638",
            "123456789648937152957812346435279618291685473786143295364798521812564937579321864",
            "123456789845937126967812534458279613291368475736541892584793261312684957679125348",
            "123456789584937126967812453846279315291365874735184962458723691312698547679541238",
            "123456789845397126967812354731249568298561473654783912386975241512634897479128635",
            "123456789648937152957812346435279618296185473781643295364798521812564937579321864",
            "123456789486937152957182643865279314291345876734861925648723591312598467579614238",
            "123456789645987132987312564756249813291638475834175296568723941312894657479561328",
            "123456789485937126967812534854279613291364875736581492548793261312648957679125348",
            "123456789845397126967182354731249568698513472254768913386975241512634897479821635",
            "123456789645987132987312546458279613291635478736148295564793821312864957879521364",
            "123456789845397126967182354734219568291568473658743912386975241512634897479821635",
            "123456789648937152957812364435279618296185473781643295364798521812564937579321846",
            "123456789486937152957812643865279314291345876734681925648723591312598467579164238",
            "123456789854937126967182453546279318291368574738514962485723691312695847679841235",
            "123456789645987132987312564756249813291638475834571296568723941312894657479165328",
            "123456789548397126967182354731249865695813472284765913356978241812634597479521638",
            "123456789645987132987312546458279613291638475736145298564793821312864957879521364",
            "123456789584937126967182453846279315291365874735814962458723691312698547679541238",
            "123456789485937162967182354836279415291564873754813296348795621512648937679321548",
            "123456789684937152957812346835279614296145873741683295368794521412568937579321468",
            "123456789546987132987312654765249813291538476834671295658723941312894567479165328",
            "123456789684937152957812463845279316291365874736184925468723591312598647579641238",
            "123456789648397125957812364731249856296185473584763912365978241812534697479621538",
            "123456789645987132987312564458279613291635478736148295564793821312864957879521346",
            "123456789485937162967812354836279415291564873754381296348795621512648937679123548",
            "123456789548397126967812354731249865695183472284765913356978241812634597479521638",
            "123456789645987132987312564458279613291638475736145298564793821312864957879521346",
            "123456789645987132987132564756249813291368475834571296568723941312894657479615328",
            "123456789648397125957182364731249856296815473584763912365978241812534697479621538",
            "123456789684937152957182463845279316291365874736841925468723591312598647579614238",
            "123456789845937126967812543456279318291368475738145962584723691312694857679581234",
            "123456789485937162967812354836279415291564873754183296348795621512648937679321548",
            "123456789548397126967812354734269815695183472281745963356978241812634597479521638",
            "123456789684937152957182463845279316291365874736814925468723591312598647579641238",
            "123456789485937126967812543856279314291364875734185962548723691312698457679541238",
            "123456789548397126967182354734219865291865473685743912356978241812634597479521638",
            "123456789458937162967182345536279418294813576781564293345798621812645937679321854",
            "123456789485937126967182543856279314291364875734815962548723691312698457679541238",
            "123456789648397125957182364734219856291865473586743912365978241812534697479621538",
            "123456789648937152957812364435279618296183475781645293364798521812564937579321846",
            "123456789684937152957182346835279614291645873746813295368794521412568937579321468",
            "123456789648397125957182364731249856596813472284765913365978241812534697479621538",
            "123456789648937152957812364435279618296381475781645293364798521812564937579123846",
            "123456789458937162967812345536279418294381576781564293345798621812645937679123854",
            "123456789684937152957182346835279614291643875746815293368794521412568937579321468",
            "123456789648397125957182364734219856591863472286745913365978241812534697479621538",
            "123456789456987132987132654568279413291364578734518296645793821312845967879621345",
            "123456789684937125957812463845279316291365874736184952468723591312598647579641238",
            "123456789458937162967812345536279418294183576781564293345798621812645937679321854",
            "123456789854937162967812435745289613291364578638571294486723951312695847579148326",
            "123456789648937152957182364435279618296813475781645293364798521812564937579321846",
            "123456789456987132987132654568279413294318576731564298645793821312845967879621345",
            "123456789648397125957812364731249856596183472284765913365978241812534697479621538",
            "123456789684937125957182463845279316291365874736814952468723591312598647579641238",
            "123456789854937126967812435745289613291364578638571294486723951312695847579148362",
            "123456789684937152957812346835279614291645873746381295368794521412568937579123468",
            "123456789845937126967182543456279318291368475738541962584723691312694857679815234",
            "123456789648937152957812346435279618296381475781645293364798521812564937579123864",
            "123456789486937125957812643865279314291345876734681952648723591312598467579164238",
            "123456789854937162967812345536279814298341576741568293385794621412685937679123458",
            "123456789854937126967182453546279318291368574738541962485723691312695847679814235",
            "123456789684937152957812346835279614291645873746183295368794521412568937579321468",
            "123456789456987132987132645568279314294318576731564298645793821312845967879621453",
            "123456789845937162967812534758249613291365478634178295586723941312694857479581326",
            "123456789854937162967812345536279814298143576741568293385794621412685937679321458",
            "123456789456987132987132645568279314291364578734518296645793821312845967879621453",
            "123456789684937152957812346835279614291643875746185293368794521412568937579321468",
            "123456789648937152957182346435279618296813475781645293364798521812564937579321864",
            "123456789584937126967182453846279315291365874735841962458723691312698547679514238",
            "123456789845937126967812534758249613291365478634178295586723941312694857479581362",
            "123456789684937125957182463845279316291365874736841952468723591312598647579614238",
            "123456789648937152957812346435279618296183475781645293364798521812564937579321864",
            "123456789456987132987132645568279413291364578734518296645793821312845967879621354",
            "123456789486937125957182643865279314291345876734861952648723591312598467579614238",
            "123456789485937162967182345836279514294561873751843926348725691512698437679314258",
            "123456789485937126967812543856279314291364875734581962548723691312698457679145238",
            "123456789456987132987132645568279413294318576731564298645793821312845967879621354",
            "123456789845937162967182354436279518298561473751348926384725691512694837679813245",
            "123456789458937162967812354536279418294183576781564293345798621812645937679321845",
            "123456789845937126967812354738249615291563478654178293386725941512694837479381562",
            "123456789458937162967182345536279814294861573781543926345728691812695437679314258",
            "123456789845937162967182345436279518298561473751348926384725691512694837679813254",
            "123456789845937126967812543456279318291368475738541962584723691312694857679185234",
            "123456789845937162967812354738249615291563478654178293386725941512694837479381526",
            "123456789684937152957812346835279614296341875741685293368794521412568937579123468",
            "123456789485937162967182354836279415294513876751864293348795621512648937679321548",
            "123456789846937152957812643465279318291385476738641925684723591312594867579168234",
            "123456789854937126967812453546279318291368574738541962485723691312695847679184235",
            "123456789648937152957812364435279618291683475786145293364798521812564937579321846",
            "123456789548937162967182354436279815295861473781345926354728691812694537679513248",
            "123456789684937152957812346835279614296143875741685293368794521412568937579321468",
            "123456789458937162967182354536279418294813576781564293345798621812645937679321845",
            "123456789846937125957812643465279318291385476738641952684723591312594867579168234",
            "123456789548937162967182354436279815295861473781543926354728691812694537679315248",
            "123456789648937152957812364435279618291685473786143295364798521812564937579321846",
            "123456789485937162967182534856279413294315876731864295548793621312648957679521348",
            "123456789845937162967182354436279518298561473751843926384725691512694837679318245",
            "123456789458937162967812354536279418294381576781564293345798621812645937679123845",
            "123456789485937162967182543856279314294315876731864295548793621312648957679521438",
            "123456789845937162967182534758249613291365478634871295586723941312694857479518326",
            "123456789648937152957812364435279618291685473786341295364798521812564937579123846",
            "123456789684937152957182364835279416296341875741865923368724591412598637579613248",
            "123456789845937162967182345436279518298561473751843926384725691512694837679318254",
            "123456789845937162967182354738249615291563478654871293386725941512694837479318526",
            "123456789584937162967182354836279415295341876741865923358724691412698537679513248",
            "123456789584937162967182453846279315291365874735841926458723691312698547679514238",
            "123456789584937162967812453846279315291365874735184926458723691312698547679541238",
            "123456789485937162967812543856279314291364875734581926548723691312698457679145238",
            "123456789485937162967182543856279314291364875734815296548793621312648957679521438",
            "123456789854937162967812345536279814291348576748561293385794621412685937679123458",
            "123456789845937162967182543456279318291368475738541926584723691312694857679815234",
            "123456789845937126967182354738249615291563478654871293386725941512694837479318562",
            "123456789854937162967182354536279418298341576741568923385724691412695837679813245",
            "123456789845937126967182534758249613291365478634871295586723941312694857479518362",
            "123456789485937162967812543856279314291364875734185296548793621312648957679521438",
            "123456789458937162967812345536279418291384576784561293345798621812645937679123854",
            "123456789584937162967182453846279315291365874735814926458723691312698547679541238",
            "123456789845937162967812543456279318291368475738541926584723691312694857679185234",
            "123456789854937162967182453546279318291368574738514926485723691312695847679841235",
            "123456789854937162967182345536279418298341576741568923385724691412695837679813254",
            "123456789485937162967182534856279413291364875734815296548793621312648957679521348",
            "123456789845937162967812354436279815291568473758341296384795621512684937679123548",
            "123456789854937162967182453546279318291368574738541926485723691312695847679814235",
            "123456789485937162967812534856279413291364875734185296548793621312648957679521348",
            "123456789458937162967182345735249618291863574684571293346728951812695437579314826",
            "123456789485937162967182543856279314291364875734815926548723691312698457679541238",
            "123456789548937126967182354735249618291863475684571293356728941812694537479315862",
            "123456789854937162967812453546279318291368574738541926485723691312695847679184235",
            "123456789854937162967812453546279318291384576738561924485723691312695847679148235",
            "123456789548937162967182354735249618291863475684571293356728941812694537479315826",
            "123456789458937162967182345536279418291864573784513296345798621812645937679321854",
            "123456789458937126967182345735249618291863574684571293346728951812695437579314862",
            "123456789684937152957812463845279316291364875736185924468723591312598647579641238",
            "123456789485937162967812543856279314291364875734185926548723691312698457679541238",
            "123456789845937162967812354436279815291568473758143296384795621512684937679321548",
            "123456789845937162967812543456279318291368475738145926584723691312694857679581234",
            "123456789684937152957182463845279316291364875736815924468723591312598647579641238",
            "123456789458937162967182345536279814291843576784561923345728691812695437679314258",
            "123456789486937152957182643865279314294315876731864295648793521312548967579621438",
            "123456789485937162967182543856279314291345876734861925548723691312698457679514238",
            "123456789486937152957812634865279413291364875734185296648793521312548967579621348",
            "123456789486937152957182643865279314291364875734815296648793521312548967579621438",
            "123456789854937162967182345536279418291843576748561923385724691412695837679318254",
            "123456789486937152957182634865279413294315876731864295648793521312548967579621348",
            "123456789486937152957812643865279314291364875734185296648793521312548967579621438",
            "123456789486937152957182634865279413291364875734815296648793521312548967579621348",
            "123456789654987132987132465745269813291348576836571294468723951312895647579614328",
            "123456789846937152957812643465279318291368475738145926684723591312594867579681234",
            "123456789854937162967812453546279318291348576738561924485723691312695847679184235",
            "123456789456987132987312645765249813291638574834571296648723951312895467579164328",
            "123456789584937162967182354836279415291543876745861923358724691412698537679315248",
            "123456789546987132987312654765249813291638475834175296658723941312894567479561328",
            "123456789486937152957812643865279314291364875734185926648723591312598467579641238",
            "123456789456987132987132645765249813291368574834571296648723951312895467579614328",
            "123456789854937162967182354536279418291843576748561923385724691412695837679318245",
            "123456789584937162967182453846279315291345876735861924458723691312698547679514238",
            "123456789486937152957182643865279314291364875734815926648723591312598467579641238",
            "123456789456987132987312645568279314294138576731564298645793821312845967879621453",
            "123456789645987132987312564756249813291538476834671295568723941312894657479165328",
            "123456789854937162967182453546279318291348576738561924485723691312695847679814235",
            "123456789485937162967182345836279514291543876754861923348725691512698437679314258",
            "123456789546987132987312654765249813291638475834571296658723941312894567479165328",
            "123456789456987132987312645568279413294138576731564298645793821312845967879621354",
            "123456789845937126967812354738249615291568473654371892386795241512684937479123568",
            "123456789846937125957812634465279318291385476738641952684723591312594867579168243",
            "123456789456987132987312654568279413294138576731564298645793821312845967879621345",
            "123456789548397126967182345735249618291865473486713592354978261812634957679521834",
            "123456789546987132987132654765249813291368475834571296658723941312894567479615328",
            "123456789854937162967182345536279418291348576748561923385724691412695837679813254",
            "123456789845937126967812354634279815291568473758341692386795241512684937479123568",
            "123456789854937162967182435546279318291368574738541926485723691312695847679814253",
            "123456789845937126967812534456279318291368475738145962584723691312694857679581243",
            "123456789548397126967182345736249518291865473485713692354978261812634957679521834",
            "123456789854937162967182435546279318291348576738561924485723691312695847679814253",
            "123456789584937162967182354836279415291345876745861923358724691412698537679513248",
            "123456789648937152957812364736249518291385476584671293365728941812594637479163825",
            "123456789854937126967812435546279318291368574738541962485723691312695847679184253",
            "123456789846937152957812634465279318291385476738641925684723591312594867579168243",
            "123456789854937162967812435546279318291368574738541926485723691312695847679184253",
            "123456789548397126967182345735249618296815473481763592354978261812634957679521834",
            "123456789854937162967182354536279418291348576748561923385724691412695837679813245",
            "123456789845937162967182534456279318291368475738541926584723691312694857679815243",
            "123456789845937126967812534456279318291368475738541962584723691312694857679185243",
            "123456789845937126967812354438279615291568473756341892384795261512684937679123548",
            "123456789845937126967812354436279815291568473758341692384795261512684937679123548",
            "123456789845937162967812534456279318291368475738145926584723691312694857679581243",
            "123456789846937152957812634768249513291365478534178296685723941312594867479681325",
            "123456789584397126967812345735289614296145873841763592358974261412638957679521438",
            "123456789845937162967812534456279318291368475738541926584723691312694857679185243",
            "123456789846937152957182634768249513291365478534871296685723941312594867479618325",
            "123456789845937126967182534456279318291368475738541962584723691312694857679815243",
            "123456789548397126967812345735249618296185473481763592354978261812634957679521834",
            "123456789845937126967812354736249815291568473458371692384795261512684937679123548",
            "123456789854937126967182435546279318291368574738541962485723691312695847679814253",
            "123456789645987132987312564458279613291638475736541298564793821312864957879125346",
            "123456789854937162967812435546279813291348576738561294485793621312685947679124358",
            "123456789548937162967182354436279815291865473785341926354728691812694537679513248",
            "123456789845937126967812354738249615291568473456371892384795261512684937679123548",
            "123456789854937162967812435546279813298341576731568294485793621312685947679124358",
            "123456789645987132987312564458279613296531478731648295564793821312864957879125346",
            "123456789854937162967812435546279813291368574738541296485793621312685947679124358",
            "123456789648937125957812364736249518291385476584671293365728941812594637479163852",
            "123456789854937162967812435546279813298361574731548296485793621312685947679124358",
            "123456789845937162967182354436279518291568473758341926384725691512694837679813245",
            "123456789854937162967812435546279318291384576738561924485723691312695847679148253",
            "123456789854937162967182435546279318291368574738514926485723691312695847679841253",
            "123456789654987132987312465548279613291638574736541298465793821312865947879124356",
            "123456789845937162967812534456279813291368475738541296584793621312684957679125348",
            "123456789845937162967182345436279518291568473758341926384725691512694837679813254",
            "123456789846937125957812634768249513291365478534178296685723941312594867479681352",
            "123456789845937162967812534456279813298361475731548296584793621312684957679125348",
            "123456789845397126967812354736249518291568473458731692384975261512684937679123845",
            "123456789485937126967812354736289415291564873854371692348795261512648937679123548",
            "123456789854937126967182435546279318291368574738514962485723691312695847679841253",
            "123456789846937152957812634465279318291368475738145926684723591312594867579681243",
            "123456789846937125957182634768249513291365478534871296685723941312594867479618352",
            "123456789845397126967812345736249518291568473458731692384975261512684937679123854",
            "123456789485937126967812354734289615291564873856371492348795261512648937679123548",
            "123456789854937162967812435546279318291348576738561924485723691312695847679184253",
            "123456789648937152957182364435279816291865473786341925364728591812594637579613248",
            "123456789845397126967812354736249815291568473458731692384975261512684937679123548",
            "123456789654987132987312456548279613291638574736541298465793821312865947879124365",
            "123456789485937126967812354834279615291564873756381492348795261512648937679123548",
            "123456789485937126967812354836279415291564873754381692348795261512648937679123548",
            "123456789485937162967812534856279413294361875731584296548793621312648957679125348",
            "123456789645987132987312546458279613291638475736541298564793821312864957879125364",
            "123456789684937152957812436845279613296341875731685294468793521312568947579124368",
            "123456789845397126967812354738249615291568473456731892384975261512684937679123548",
            "123456789485937162967812534856279413291364875734581296548793621312648957679125348",
            "123456789645987132987312546458279613296531478731648295564793821312864957879125364",
            "123456789485397126967812354736289415291564873854731692348975261512648937679123548",
            "123456789854937162967812345536279418298341576741568923385724691412695837679183254",
            "123456789854937162967812345536279418298143576741568923385724691412695837679381254",
            "123456789458937162967812345536279814291384576784561923345728691812695437679143258",
            "123456789485397126967812345736289514291564873854731692348975261512648937679123458",
            "123456789485937126967812354836279415294561873751384692348795261512648937679123548",
            "123456789845937162967812354436279518298561473751348926384725691512694837679183245",
            "123456789486937125957182643865279314294315876731864952648723591312598467579641238",
            "123456789854937162967812453546279318298341576731568924485723691312695847679184235",
            "123456789854937162967812345536279418291348576748561923385724691412695837679183254",
            "123456789846937152957182643465279318298315476731648925684723591312594867579861234",
            "123456789854937162967812453546279318298361574731548296485793621312685947679124835",
            "123456789458937162967182345536279814294813576781564923345728691812695437679341258",
            "123456789845937126967812354436279815298561473751348692384795261512684937679123548",
            "123456789485397126967812354734289615291564873856731492348975261512648937679123548",
            "123456789684937125957182463845279316296315874731864952468723591312598647579641238",
            "123456789845937162967812543456279318298361475731548296584793621312684957679125834",
            "123456789458937162967812345536279814294183576781564923345728691812695437679341258",
            "123456789854937162967812354536279418291348576748561923385724691412695837679183245",
            "123456789648937152957812364435279816296381475781645923364728591812594637579163248",
            "123456789486937152957182643865279314294315876731864925648723591312598467579641238",
            "123456789845937126967812354634279815298561473751348692386795241512684937479123568",
            "123456789846937125957182643465279318298315476731648952684723591312594867579861234",
            "123456789584937162967182453846279315295314876731865924458723691312698547679541238",
            "123456789458937162967812345536279814294381576781564923345728691812695437679143258",
            "123456789684937152957182463845279316296315874731864925468723591312598647579641238",
            "123456789485937162967812543856279314294361875731584296548793621312648957679125438",
            "123456789584937162967182453846279315295341876731865924458723691312698547679514238",
            "123456789648937152957812364435279816291683475786145923364728591812594637579361248",
            "123456789854937162967812354536279418298143576741568923385724691412695837679381245",
            "123456789485937126967812354736289415294561873851374692348795261512648937679123548",
            "123456789485397126967812354736289415294561873851743692348975261512638947679124538",
            "123456789485937162967182543856279314294315876731864925548723691312698457679541238",
            "123456789684937152957812463845279316296341875731685294468793521312568947579124638",
            "123456789485937162967182345836279514294513876751864923348725691512698437679341258",
            "123456789845937126967812354736249815298561473451378692384795261512684937679123548",
            "123456789684937152957812364835279416291643875746185923368724591412598637579361248",
            "123456789485937162967812345836279514294561873751384926348725691512698437679143258",
            "123456789584937126967182453846279315295361874731845962458723691312698547679514238",
            "123456789854937162967812453546279318298341576731568294485793621312685947679124835",
            "123456789485397126967812354734289615296541873851763492348975261512638947679124538",
            "123456789684937152957182364835279416291643875746815923368724591412598637579361248",
            "123456789684937152957812364835279416296341875741685923368724591412598637579163248",
            "123456789485937126967182543856279314294361875731845962548723691312698457679514238",
            "123456789845937162967182543456279318298361475731548926584723691312694857679815234",
            "123456789854937162967182453546279318298314576731568924485723691312695847679841235",
            "123456789548397126967812345735249618296581473481763592354978261812634957679125834",
            "123456789648937152957182364435279816296813475781645923364728591812594637579361248",
            "123456789845937162967812543456279318298361475731548926584723691312694857679185234",
            "123456789854937162967182453546279318298341576731568924485723691312695847679814235",
            "123456789584397126967812345735289614296541873841763592358974261412638957679125438",
            "123456789845937162967812345436279518298561473751348926384725691512694837679183254",
            "123456789584937126967812453846279315295361874731584962458723691312698547679145238",
            "123456789648937152957812364435279816296183475781645923364728591812594637579361248",
            "123456789845937162967182543456279318298361475731845926584723691312694857679518234",
            "123456789845937162967812543456279318291368475738541296584793621312684957679125834",
            "123456789648937152957812364435279816291685473786341925364728591812594637579163248",
            "123456789854937162967812354536279418298341576741568923385724691412695837679183245",
            "123456789485937126967812543856279314294361875731584962548723691312698457679145238",
            "123456789684937152957812364835279416296143875741685923368724591412598637579361248",
            "123456789845937126967812345436279518291568473758341692384795261512684937679123854",
            "123456789485937162967812543856279314291364875734581296548793621312648957679125438",
            "123456789648937152957812364435279816291685473786143925364728591812594637579361248",
            "123456789845937126967812354436279518291568473758341692384795261512684937679123845",
            "123456789854937162967812453546279318298361574731584926485723691312695847679148235",
            "123456789684937152957182463845279316296341875731865924468723591312598647579614238",
            "123456789854937162967812453546279318291368574738541296485793621312685947679124835",
            "123456789845397126967812354736249815298561473451738692384975261512684937679123548",
            "123456789854937162967182453546279318298361574731548926485723691312695847679814235",
            "123456789684937152957812364835279416291645873746381925368724591412598637579163248",
            "123456789684937152957812463845279316296341875731685924468723591312598647579164238",
            "123456789485397126967812354736289415294561873851734692348975261512648937679123548",
            "123456789684937152957812364835279416296145873741683925368724591412598637579361248",
            "123456789845937126967812345736249518291568473458371692384795261512684937679123854",
            "123456789684937152957812364835279416291645873746183925368724591412598637579361248",
            "123456789845937126967182543456279318298361475731548962584723691312694857679815234",
            "123456789854937162967812453546279318291348576738561294485793621312685947679124835",
            "123456789854937162967812453546279318298361574731548926485723691312695847679184235",
            "123456789845937126967812354736249518291568473458371692384795261512684937679123845",
            "123456789684937152957182364835279416291645873746813925368724591412598637579361248",
            "123456789845937126967812543456279318298361475731548962584723691312694857679185234",
            "123456789684937152957182463845279316296314875731865924468723591312598647579641238",
            "123456789648937152957812364435279816296185473781643925364728591812594637579361248",
            "123456789648937152957182364435279816296815473781643925364728591812594637579361248",
            "123456789485397126967812345736289514294561873851734692348975261512648937679123458",
            "123456789854937126967812453546279318298361574731548962485723691312695847679184235",
            "123456789846937152957182643465279318298361475731845926684723591312594867579618234",
            "123456789845397126967812345736249518298561473451738692384975261512684937679123854",
            "123456789684937152957812463846279315295341876731685294468793521312568947579124638",
            "123456789485937162967812543854279316291364875736581294548793621312648957679125438",
            "123456789845937126967812345436279518298561473751348692384795261512684937679123854",
            "123456789854937126967182453546279318298361574731548962485723691312695847679814235",
            "123456789584937162967182453846279315295361874731845926458723691312698547679514238",
            "123456789486937152957182643865279314294361875731845926648723591312598467579614238",
            "123456789854937162967812453548279316296341578731568294485793621312685947679124835",
            "123456789684937152957812463846279315291345876735681294468793521312568947579124638",
            "123456789845397126967812354736249518298561473451738692384975261512684937679123845",
            "123456789845937126967812354436279518298561473751348692384795261512684937679123845",
            "123456789485937162967182543856279314294361875731845926548723691312698457679514238",
            "123456789485937162967812345836279514291564873754183926348725691512698437679341258",
            "123456789854937126967812453546279318298361574731584962485723691312695847679148235",
            "123456789854937162967812453548279316291368574736541298485793621312685947679124835",
            "123456789845937126967812354736249518298561473451378692384795261512684937679123845",
            "123456789485937162967812345836279514291564873754381926348725691512698437679143258",
            "123456789584937162967812453846279315295361874731584926458723691312698547679145238",
            "123456789684937152957812364735289416296143875841675923368724591412598637579361248",
            "123456789845937126967182543456279318298361475731845962584723691312694857679518234",
            "123456789648937152957812364735249816296185473481673925364728591812594637579361248",
            "123456789845937162967812543458279316291368475736541298584793621312684957679125834",
            "123456789845397126967812354738249615291568473654731892386975241512684937479123568",
            "123456789845937126967812345736249518298561473451378692384795261512684937679123854",
            "123456789485937162967812543856279314294361875731584926548723691312698457679145238",
            "123456789684937152957812364735289416296341875841675923368724591412598637579163248",
            "123456789485937162967182345836279514291564873754813926348725691512698437679341258",
            "123456789684937152957812364735289416296145873841673925368724591412598637579361248",
            "123456789654987132987312465548279316296138574731564928465723891312895647879641253",
            "123456789845937162967812345436279518291568473758143926384725691512694837679381254",
            "123456789648937152957812364735249816296183475481675923364728591812594637579361248",
            "123456789654987132987132465548279316296318574731564928465723891312895647879641253",
            "123456789485937126967812345836279514291564873754381692348795261512648937679123458",
            "123456789845937162967812345436279518291568473758341926384725691512694837679183254",
            "123456789648937152957182364735249816296813475481675923364728591812594637579361248",
            "123456789548937162967182354736249815295861473481573926354728691812694537679315248",
            "123456789485937126967812345836279514294561873751384692348795261512648937679123458",
            "123456789654987132987312465546279318298631574731548296465793821312865947879124653",
            "123456789548937162967182354736249815295861473481375926354728691812694537679513248",
            "123456789648937125957812364436279518291685473785143692364798251812564937579321846",
            "123456789845937162967812534456279318298361475731548296584793621312684957679125843",
            "123456789645987132987132564458279316296318475731645928564723891312894657879561243",
            "123456789684937125957182346835279614291645873746813592368794251412568937579321468",
            "123456789648937152957812364735249816296381475481675923364728591812594637579163248",
            "123456789654987132987312465546279318291638574738541296465793821312865947879124653",
            "123456789458937162967182345536279814291864573784513926345728691812695437679341258",
            "123456789845937162967812534456279318291368475738541296584793621312684957679125843",
            "123456789645987132987312564458279316296138475731645928564723891312894657879561243",
            "123456789684937125957182364835279416291645873746813592368794251412568937579321648",
            "123456789485937126967812345736289514294561873851374692348795261512648937679123458",
            "123456789648937125957812364436279518295681473781345692364798251812564937579123846",
            "123456789845937162967182354736249518298561473451378926384725691512694837679813245",
            "123456789654987132987312465548279316291638574736541298465793821312865947879124653",
            "123456789485937126967812345736289514291564873854371692348795261512648937679123458",
            "123456789845937162967812354436279518291568473758143926384725691512694837679381245",
            "123456789648937125957812364436279518291685473785341692364798251812564937579123846",
            "123456789684937125957182346836279514291645873745813692368794251412568937579321468",
            "123456789845937162967182354736249518298561473451873926384725691512694837679318245",
            "123456789845937162967812354436279518291568473758341926384725691512694837679183245",
            "123456789854937162967812435546279318291348576738561294485793621312685947679124853",
            "123456789845937162967182345736249518291568473458371926384725691512694837679813254",
            "123456789854937162967812435546279318291368574738541296485793621312685947679124853",
            "123456789654987132987312465548279316296134578731568924465723891312895647879641253",
            "123456789645987132987312564458279316291638475736541298564793821312864957879125643",
            "123456789648937125957812346436279518291685473785143692364798251812564937579321864",
            "123456789845937162967182354736249518291568473458371926384725691512694837679813245",
            "123456789845937126967812354634279518291568473758341692386795241512684937479123865",
            "123456789648937152957182364735249816296815473481673925364728591812594637579361248",
            "123456789645987132987312564458279316296135478731648925564723891312894657879561243",
            "123456789684937125957812346835279614291645873746381592368794251412568937579123468",
            "123456789645987132987312564456279318291638475738541296564793821312864957879125643",
            "123456789845937126967812354634279518298561473751348692386795241512684937479123865",
            "123456789845937162967812354736249518298561473451378926384725691512694837679183245",
            "123456789648937125957812346436279518295681473781345692364798251812564937579123864",
            "123456789854937162967812435546279318298341576731568294485793621312685947679124853",
            "123456789548937162967182354736249815291865473485371926354728691812694537679513248",
            "123456789648937152957182364735249816291865473486371925364728591812594637579613248",
            "123456789645987132987312564456279318298631475731548296564793821312864957879125643",
            "123456789684937125957812346835279614291645873746183592368794251412568937579321468",
            "123456789648937125957812346436279518291685473785341692364798251812564937579123864",
            "123456789854937162967812435546279318298361574731548296485793621312685947679124853",
            "123456789654987132987132465548279316296314578731568924465723891312895647879641253",
            "123456789684937125957812346835279614296145873741683592368794251412568937579321468",
            "123456789645987132987132564458279316296315478731648925564723891312894657879561243",
            "123456789485937162967812345736289514294561873851374926348725691512698437679143258",
            "123456789645987132987312564456279318291538476738641295564793821312864957879125643",
            "123456789845937162967812345736249518298561473451378926384725691512694837679183254",
            "123456789854937162967812435548279316296341578731568294485793621312685947679124853",
            "123456789684937125957812346836279514291645873745183692368794251412568937579321468",
            "123456789845937162967182345736249518298561473451378926384725691512694837679813254",
            "123456789684937152957812364735289416291645873846371925368724591412598637579163248",
            "123456789645987132987312564458279316296531478731648925564723891312894657879165243",
            "123456789645987132987312564458279316296531478731648295564793821312864957879125643",
            "123456789485937162967812345736289514291564873854173926348725691512698437679341258",
            "123456789458937126967182354534279618291864573786513492345798261812645937679321845",
            "123456789648937125957812364435279618291685473786341592364798251812564937579123846",
            "123456789854937162967812435548279316291368574736541298485793621312685947679124853",
            "123456789654987132987132465548279316296341578731568924465723891312895647879614253",
            "123456789645987132987312564456279318298531476731648295564793821312864957879125643",
            "123456789684937125957812346836279514295641873741385692368794251412568937579123468",
            "123456789485937162967812345736289514291564873854371926348725691512698437679143258",
            "123456789845937162967182345736249518298561473451873926384725691512694837679318254",
            "123456789648937125957812364435279816291685473786341592364798251812564937579123648",
            "123456789684937152957812364735289416291643875846175923368724591412598637579361248",
            "123456789458937126967812354534279618296184573781563492345798261812645937679321845",
            "123456789845937162967812534458279316291368475736541298584793621312684957679125843",
            "123456789684937125957812346836279514291645873745381692368794251412568937579123468",
            "123456789684937152957812364735289416291645873846173925368724591412598637579361248",
            "123456789458937126967182354534279618296814573781563492345798261812645937679321845",
            "123456789845937162967812345736249518291568473458173926384725691512694837679381254",
            "123456789845937162967812345736249518291568473458371926384725691512694837679183254",
            "123456789648937125957812364435279816296185473781643592364798251812564937579321648",
            "123456789456987132987312645568279314294631578731548926645723891312895467879164253",
            "123456789684937125957812364835279416296145873741683592368794251412568937579321648",
            "123456789854937126967812345538279614296148573741563892385794261412685937679321458",
            "123456789648937125957812364435279816291685473786143592364798251812564937579321648",
            "123456789546987132987312654468279315295138476731645928654723891312894567879561243",
            "123456789546987132987312654468279315295631478731548926654723891312894567879165243",
            "123456789684937125957812364835279416291645873746183592368794251412568937579321648",
            "123456789456987132987312645568279314294138576731564928645723891312895467879641253",
            "123456789648937125957182364435279816296815473781643592364798251812564937579321648",
            "123456789684937125957812364835279416291645873746381592368794251412568937579123648",
            "123456789458937126967812345534279618296184573781563492345798261812645937679321854",
            "123456789458937126967182345534279618291864573786513492345798261812645937679321854",
            "123456789648937152957812364735249816291685473486371925364728591812594637579163248",
            "123456789546987132987132654468279315295318476731645928654723891312894567879561243",
            "123456789458937126967182345534279618296814573781563492345798261812645937679321854",
            "123456789456987132987132645568279314294318576731564928645723891312895467879641253",
            "123456789845937162967812354736249518291568473458371926384725691512694837679183245",
            "123456789648937125957182364435279618296815473781643592364798251812564937579321846",
            "123456789456987123987132645865279314294318576731564892648793251312845967579621438",
            "123456789684937125957812346735289614296145873841673592368794251412568937579321468",
            "123456789684937125957812346735289614291645873846173592368794251412568937579321468",
            "123456789546987132987132654468279315295361478731548926654723891312894567879615243",
            "123456789645987123987132564854279316296318475731645892568793241312864957479521638",
            "123456789648937125957812364435279618296185473781643592364798251812564937579321846",
            "123456789456987132987132645568279314294361578731548926645723891312895467879614253",
            "123456789684937125957812346736289514291645873845173692368794251412568937579321468",
            "123456789648937152957812364735249816291685473486173925364728591812594637579361248",
            "123456789648937125957812364435279618291685473786143592364798251812564937579321846",
            "123456789645987123987312564854279316291638475736145892568793241312864957479521638",
            "123456789648937152957812364735249816291683475486175923364728591812594637579361248",
            "123456789458937126967182345536279814291864573784513692345798261812645937679321458",
            "123456789458937126967182354536279418291864573784513692345798261812645937679321845",
            "123456789645987123987312564854279316296138475731645892568793241312864957479521638",
            "123456789845937162967812354736249518291568473458173926384725691512694837679381245",
            "123456789684937125957812364735289416291645873846173592368794251412568937579321648",
            "123456789458937126967182345536279418291864573784513692345798261812645937679321854",
            "123456789684937125957812364735289416296145873841673592368794251412568937579321648",
            "123456789645987123987312564756249318291638475834175692568793241312864957479521836",
            "123456789845397126967812534758249613691538472234761895586973241312684957479125368",
            "123456789846937125957182634465279318298315476731648952684723591312594867579861243",
            "123456789645987123987312564854279316291638475736541892568793241312864957479125638",
            "123456789648937125957812346435279618291685473786143592364798251812564937579321864",
            "123456789456987123987312645865279314294138576731564892648793251312845967579621438",
            "123456789845937126967812534758249613291368475634571892586793241312684957479125368",
            "123456789654987123987312465845279316291638574736541892468793251312865947579124638",
            "123456789845937126967812534456279318298361475731548962584723691312694857679185243",
            "123456789648937125957812346435279618291685473786341592364798251812564937579123864",
            "123456789645987123987312564756249318291638475834571692568793241312864957479125836",
            "123456789854937126967812435546279318298361574731584962485723691312695847679148253",
            "123456789684397125957812346736289514291645873845731692368974251412568937579123468",
            "123456789854937126967812435546279318298361574731548962485723691312695847679184253",
            "123456789845937126967182534456279318298361475731845962584723691312694857679518243",
            "123456789684937125957812346736289514291645873845371692368794251412568937579123468",
            "123456789485937126967812354736289415291564873854173692348795261512648937679321548",
            "123456789845937126967812534654279813291368475738145692586793241312684957479521368",
            "123456789845397126967812534754269318691538472238741695586973241312684957479125863",
            "123456789648937125957812346435279618296185473781643592364798251812564937579321864",
            "123456789485937126967812354734289615291564873856173492348795261512648937679321548",
            "123456789845937126967812534654279318291368475738145692586793241312684957479521863",
            "123456789854937126967182435546279318298361574731548962485723691312695847679814253",
            "123456789845397126967812534754269318698531472231748695586973241312684957479125863",
            "123456789648937125957182346435279618296815473781643592364798251812564937579321864",
            "123456789854937162967812345538279416296148573741563928385724691412695837679381254",
            "123456789684397125957812346736289514295641873841735692368974251412568937579123468",
            "123456789845937126967182534456279318298361475731548962584723691312694857679815243",
            "123456789458937162967182345534279816296814573781563924345728691812695437679341258",
            "123456789485937126967812345736289514291564873854173692348795261512648937679321458",
            "123456789845937126967812534758249613291368475634175892586793241312684957479521368",
            "123456789854937162967182345538279416296841573741563928385724691412695837679318254",
            "123456789846937152957182634465279318298361475731845926684723591312594867579618243",
            "123456789684937125957812346736289514295641873841375692368794251412568937579123468",
            "123456789458937162967812345534279816296184573781563924345728691812695437679341258",
            "123456789854937126967812435745269318698341572231578694486793251312685947579124863",
            "123456789846937152957182634465279318298315476731648925684723591312594867579861243",
            "123456789845937126967812345736249518291568473458173692384795261512684937679321854",
            "123456789854937126967812435745269318691348572238571694486793251312685947579124863",
            "123456789458937162967182345534279816296841573781563924345728691812695437679314258",
            "123456789845937126967812354738249615291568473456173892384795261512684937679321548",
            "123456789845937162967182534456279318298361475731548926584723691312694857679815243",
            "123456789684397125957812364735289416291645873846731592368974251412568937579123648",
            "123456789648397125957812364735249816291685473486731592364978251812564937579123648",
            "123456789684397125957812346735289614291645873846731592368974251412568937579123468",
            "123456789845937126967812354736249518291568473458173692384795261512684937679321845",
            "123456789845937162967182534456279318298361475731845926584723691312694857679518243",
            "123456789845397126967812534754269318698135472231748695586973241312684957479521863",
            "123456789854937126967812435645279318291368574738541692486793251312685947579124863",
            "123456789648937152957182364736249815295861473481375926364728591812594637579613248",
            "123456789845937126967812354736249815291568473458173692384795261512684937679321548",
            "123456789648397125957812346735249618296185473481763592364978251812534967579621834",
            "123456789854937162967812435546279318298341576731568924485723691312695847679184253",
            "123456789854937162967812435546279318298361574731548926485723691312695847679184253",
            "123456789684937125957812364735289416291645873846371592368794251412568937579123648",
            "123456789845397126967812534754269813698135472231748695586973241312684957479521368",
            "123456789845937162967812534456279318298361475731548926584723691312694857679185243",
            "123456789845937126967812534654279318291368475738541692586793241312684957479125863",
            "123456789648937152957182364436279815295861473781345926364728591812594637579613248",
            "123456789854937162967812435546279318298361574731584926485723691312695847679148253",
            "123456789684937125957812346735289614291645873846371592368794251412568937579123468",
            "123456789648397125957812364736249518295681473481735692364978251812564937579123846",
            "123456789648397125957812364736249518291685473485731692364978251812564937579123846",
            "123456789846937125957812634768249513591368472234175896685793241312584967479621358",
            "123456789648397125957812346736249518291685473485731692364978251812564937579123864",
            "123456789845937126967812534654279318298361475731548692586793241312684957479125863",
            "123456789648937152957812364736249815295681473481375926364728591812594637579163248",
            "123456789648397125957812346736249518295681473481735692364978251812564937579123864",
            "123456789645987123987312564756249318891635472234178695568793241312864957479521836",
            "123456789854937126967812435645279318298361574731548692486793251312685947579124863",
            "123456789648937152957812364436279815295681473781345926364728591812594637579163248",
            "123456789854937162967182435546279318298361574731548926485723691312695847679814253",
            "123456789854937162967182435546279318298341576731568924485723691312695847679814253",
            "123456789845937126967812354436279518291568473758143692384795261512684937679321845",
            "123456789648397125957812364735249618291685473486731592364978251812564937579123846",
            "123456789684397125957812346735289614296145873841763592368974251412538967579621438",
            "123456789854937162967182435546279318298314576731568924485723691312695847679841253",
            "123456789648397125957812346735249618291685473486731592364978251812564937579123864",
            "123456789845937126967812354438279615291568473756143892384795261512684937679321548",
            "123456789845937126967812354436279815291568473758143692384795261512684937679321548",
            "123456789648937152957812364736249815295381476481675923364728591812594637579163248",
            "123456789845937126967812345436279518291568473758143692384795261512684937679321854",
            "123456789648937125957182364534279618296815473781643592365798241812564937479321856",
            "123456789845397126967812534754269813698531472231748695586973241312684957479125368",
            "123456789648937125957812364534279816296185473781643592365798241812564937479321658",
            "123456789648937125957182364534279816296815473781643592365798241812564937479321658",
            "123456789854937162967182345538279416296341578741568923385724691412695837679813254",
            "123456789854937126967812435745269813698341572231578694486793251312685947579124368",
            "123456789845397126967812534754269813691538472238741695586973241312684957479125368",
            "123456789648937125957812364534279618296185473781643592365798241812564937479321856",
            "123456789648937152957812364736249815295183476481675923364728591812594637579361248",
            "123456789648397125957182346736249518291865473485713692364978251812534967579621834",
            "123456789854937162967812345538279416296341578741568923385724691412695837679183254",
            "123456789854937126967812435745269813691348572238571694486793251312685947579124368",
            "123456789648937152957182364736249815295813476481675923364728591812594637579361248",
            "123456789485937126967182354834279615296514873751863492348795261512648937679321548",
            "123456789648397125957182346735249618291865473486713592364978251812534967579621834",
            "123456789648397125957182346735249618296815473481763592364978251812534967579621834",
            "123456789648397125957812364736249518291685473584731692365978241812564937479123856",
            "123456789648937152957812364436279815295381476781645923364728591812594637579163248",
            "123456789485937126967812354836279415291564873754183692348795261512648937679321548",
            "123456789854937126967812435645279813291368574738541692486793251312685947579124368",
            "123456789458937162967812345534279816296381574781564923345728691812695437679143258",
            "123456789846937125957812643761249358295381476438675912684723591312594867579168234",
            "123456789648937125957812364736249518291685473584173692365798241812564937479321856",
            "123456789485937126967182354836279415291564873754813692348795261512648937679321548",
            "123456789845937126967812534654279813291368475738541692586793241312684957479125368",
            "123456789845937126967812543751249368298361475436578912584723691312694857679185234",
            "123456789486937125957812643761289354295341876834675912648723591312598467579164238",
            "123456789648937125957812364534279816291685473786143592365798241812564937479321658",
            "123456789854937162967182354538279416296841573741563928385724691412695837679318245",
            "123456789584937126967812453741289365295361874836574912458723691312698547679145238",
            "123456789485937126967182354834279615291564873756813492348795261512648937679321548",
            "123456789854937162967812345538279416296143578741568923385724691412695837679381254",
            "123456789854937126967812435645279813298361574731548692486793251312685947579124368",
            "123456789684937152957812364836279415295641873741385926368724591412598637579163248",
            "123456789846937125957182643761249358295318476438675912684723591312594867579861234",
            "123456789648937125957812364534279618291685473786143592365798241812564937479321856",
            "123456789854937162967182354538279416296341578741568923385724691412695837679813245",
            "123456789648937125957812364735249618291685473486371592364798251812564937579123846",
            "123456789485937126967812543751289364294361875836574912548723691312698457679145238",
            "123456789485937126967812354834279615291564873756183492348795261512648937679321548",
            "123456789845937126967812534654279813298361475731548692586793241312684957479125368",
            "123456789648937125957812364736249518291685473485371692364798251812564937579123846",
            "123456789684937152957812364836279415295143876741685923368724591412598637579361248",
            "123456789648937152957812364436279815295183476781645923364728591812594637579361248",
            "123456789854937162967812354538279416296341578741568923385724691412695837679183245",
            "123456789684937152957812364836279415295341876741685923368724591412598637579163248",
            "123456789648937125957812364736249518291685473584371692365798241812564937479123856",
            "123456789648937125957812364735249816291685473486371592364798251812564937579123648",
            "123456789458937162967812345534279816296183574781564923345728691812695437679341258",
            "123456789846937125957182643761249358298315476435678912684723591312594867579861234",
            "123456789845937126967182534751249368298361475436875912584723691312694857679518243",
            "123456789854937162967812354538279416296143578741568923385724691412695837679381245",
            "123456789485937126967812345836279514291564873754183692348795261512648937679321458",
            "123456789854937162967812354538279416296148573741563928385724691412695837679381245",
            "123456789648937125957812364534279816291685473786341592365798241812564937479123658",
            "123456789648937125957812364736249518295681473481375692364798251812564937579123846",
            "123456789845937126967182543751249368298361475436578912584723691312694857679815234",
            "123456789846937125957182634761249358295318476438675912684723591312594867579861243",
            "123456789485937126967182345836279514291564873754813692348795261512648937679321458",
            "123456789458937162967182345534279816296813574781564923345728691812695437679341258",
            "123456789684937152957812364736289415295143876841675923368724591412598637579361248",
            "123456789648937125957812364534279618291685473786341592365798241812564937479123856",
            "123456789845937126967182543751249368298361475436875912584723691312694857679518234",
            "123456789648937152957182364436279815295813476781645923364728591812594637579361248",
            "123456789846937125957182634761249358298315476435678912684723591312594867579861243",
            "123456789684937152957812364736289415295641873841375926368724591412598637579163248",
            "123456789845937126967182534751249368298361475436578912584723691312694857679815243",
            "123456789648937125957182364735249618296815473481673592364798251812564937579321846",
            "123456789684937152957812364736289415295341876841675923368724591412598637579163248",
            "123456789648937125957182364735249816296815473481673592364798251812564937579321648",
            "123456789845937126967812354738249615291568473654173892386795241512684937479321568",
            "123456789846937125957812634761249358295381476438675912684723591312594867579168243",
            "123456789845937126967812534751249368298361475436578912584723691312694857679185243",
            "123456789648937152957812364736249815291385476485671923364728591812594637579163248",
            "123456789648937152957812364436279815291685473785341926364728591812594637579163248",
            "123456789485937162967812345734289516291564873856371924348725691512698437679143258",
            "123456789458937126967182345635279814291864573784513692346798251812645937579321468",
            "123456789648937125957812364735249816291685473486173592364798251812564937579321648",
            "123456789485937162967182345834279516296513874751864923348725691512698437679341258",
            "123456789648937152957812364436279815291385476785641923364728591812594637579163248",
            "123456789458937126967182345635279418291864573784513692346798251812645937579321864",
            "123456789648937152957812364736249815291685473485371926364728591812594637579163248",
            "123456789485937162967812345834279516291564873756381924348725691512698437679143258",
            "123456789648937125957812364735249816296185473481673592364798251812564937579321648",
            "123456789584937162967182354835279416296341875741865923358724691412698537679513248",
            "123456789684937152957182364836279415295341876741865923368724591412598637579613248",
            "123456789845937126967812354634279815291568473758143692386795241512684937479321568",
            "123456789684937152957812364836279415291345876745681923368724591412598637579163248",
            "123456789648937152957182364436279815291865473785341926364728591812594637579613248",
            "123456789845937126967182534451279368298361475736845912584723691312694857679518243",
            "123456789845937126967812354634279518291568473758143692386795241512684937479321865",
            "123456789648937125957812364735249618291685473486173592364798251812564937579321846",
            "123456789684937152957812364736289415291345876845671923368724591412598637579163248",
            "123456789845937162967812345438279516291568473756341928384725691512694837679183254",
            "123456789486937125957812643861279354295341876734685912648723591312598467579164238",
            "123456789854937126967812435541279368298361574736584912485723691312695847679148253",
            "123456789648937152957182364736249815291865473485371926364728591812594637579613248",
            "123456789648937125957812364735249618296185473481673592364798251812564937579321846",
            "123456789845937162967182345438279516291568473756341928384725691512694837679813254",
            "123456789485937162967182345834279516296541873751863924348725691512698437679314258",
            "123456789485937162967182345834279516296514873751863924348725691512698437679341258",
            "123456789854937126967182435541279368298361574736548912485723691312695847679814253",
            "123456789486937125957182643861279354295341876734865912648723591312598467579614238",
            "123456789648937152957812364436279815291685473785143926364728591812594637579361248",
            "123456789648937125957812364736249518291685473485173692364798251812564937579321846",
            "123456789584937162967182354835279416296541873741863925358724691412698537679315248",
            "123456789845937162967182345738249516291568473456371928384725691512694837679813254",
            "123456789648937152957812364736249815291685473485173926364728591812594637579361248",
            "123456789854937126967812435541279368298361574736548912485723691312695847679184253",
            "123456789845937162967182345738249516291563478456871923384725691512694837679318254",
            "123456789485937126967182543851279364296341875734865912548723691312698457679514238",
            "123456789845937162967812345738249516291568473456371928384725691512694837679183254",
            "123456789845937162967182354738249516291563478456871923384725691512694837679318245",
            "123456789548937162967182354735249816291863475486571923354728691812694537679315248",
            "123456789845937126967812534451279368298361475736548912584723691312694857679185243",
            "123456789486937125957182643861279354295314876734865912648723591312598467579641238",
            "123456789684937152957812364736289415291645873845371926368724591412598637579163248",
            "123456789845937126967182534451279368298361475736548912584723691312694857679815243",
            "123456789684937152957812364836279415291645873745381926368724591412598637579163248",
            "123456789485937126967182543851279364296314875734865912548723691312698457679541238",
            "123456789648937125957812346736249518291685473485173692364798251812564937579321864",
            "123456789648937125957812346736249518295681473481375692364798251812564937579123864",
            "123456789846937125957182634461279358298315476735648912684723591312594867579861243",
            "123456789648937125957812346736249518291685473485371692364798251812564937579123864",
            "123456789845937162967182354438279516291563478756841923384725691512694837679318245",
            "123456789845937162967182354738249516291568473456371928384725691512694837679813245",
            "123456789845937162967182345438279516291563478756841923384725691512694837679318254",
            "123456789684937152957812364736289415291645873845173926368724591412598637579361248",
            "123456789684937152957812364836279415291645873745183926368724591412598637579361248",
            "123456789846937125957182634461279358295318476738645912684723591312594867579861243",
            "123456789845937162967182354438279516291568473756341928384725691512694837679813245",
            "123456789684937125957182463841279356295361874736845912468723591312598647579614238",
            "123456789485937162967182345834279516291563874756841923348725691512698437679314258",
            "123456789854937126967182435541279368296318574738564912485723691312695847679841253",
            "123456789684937152957182364836279415291645873745813926368724591412598637579361248",
            "123456789548937162967182354435279816291863475786541923354728691812694537679315248",
            "123456789458937162967182345534279816291863574786541923345728691812695437679314258",
            "123456789584937126967182453841279365296315874735864912458723691312698547679541238",
            "123456789648937125957812346735249618291685473486371592364798251812564937579123864",
            "123456789846937125957812634461279358295381476738645912684723591312594867579168243",
            "123456789845937162967812354438279516291568473756341928384725691512694837679183245",
            "123456789854937126967812435541279368296381574738564912485723691312695847679148253",
            "123456789648937125957812346735249618291685473486173592364798251812564937579321864",
            "123456789648937125957182346735249618296815473481673592364798251812564937579321864",
            "123456789845937162967812354738249516291568473456371928384725691512694837679183245",
            "123456789648937125957812346735249618296185473481673592364798251812564937579321864",
            "123456789684937152957182364836279415291345876745861923368724591412598637579613248",
            "123456789485937126967812543851279364294361875736584912548723691312698457679145238",
            "123456789584937126967812453841279365295361874736584912458723691312698547679145238",
            "123456789485937126967182543851279364294361875736845912548723691312698457679514238",
            "123456789684937125957182463841279356296315874735864912468723591312598647579641238",
            "123456789548937162967182354735249816291865473486371925354728691812694537679513248",
            "123456789486937125957182643861279354294315876735864912648723591312598467579641238",
            "123456789584937126967182453841279365295361874736845912458723691312698547679514238",
            "123456789548937162967182354435279816291865473786341925354728691812694537679513248",
            "123456789485937162967182345834279516291563874756814923348725691512698437679341258",
            "123456789854937162967182345635279418291843576748561293386724951412695837579318624",
            "123456789845937162967812345438279516291563478756148923384725691512694837679381254",
            "123456789548937162967182354735249816291865473684371295356728941812694537479513628",
            "123456789845937162967182354634279518298561473751843296386725941512694837479318625",
            "123456789456987123987132645765249318291368574834571296648723951312895467579614832",
            "123456789854937162967182345735269418291843576648571293386724951412695837579318624",
            "123456789485937162967812345834279516291563874756184923348725691512698437679341258",
            "123456789648937152957182364736249815291865473584371296365728941812594637479613528",
            "123456789546987123987312645761249358298531476435678912654723891312894567879165234",
            "123456789845937162967812354738249516291568473456173928384725691512694837679381245",
            "123456789845937162967182354634279518298561473751348296386725941512694837479813625",
            "123456789456987123987312645765249318291638574834571296648723951312895467579164832",
            "123456789846937125957812643461279358295381476738645912684723591312594867579168234",
            "123456789546987123987312645465279318298531476731648952654723891312894567879165234",
            "123456789845937162967182354738249516291563478654871293386725941512694837479318625",
            "123456789546987123987312645765249318291538476438671952654723891312894567879165234",
            "123456789845937162967812345738249516291568473456173928384725691512694837679381254",
            "123456789854937126967812453541279368296381574738564912485723691312695847679148235",
            "123456789854937162967182345635279418298341576741568293386724951412695837579813624",
            "123456789648937152957812364736249815291685473584173296365728941812594637479361528",
            "123456789456987123987132654564279318298341576731568942645723891312895467879614235",
            "123456789458937162967182345534279816291863574786514923345728691812695437679341258",
            "123456789546987123987312645765249318298531476431678952654723891312894567879165234",
            "123456789485937162967812345734289516291564873856173924348725691512698437679341258",
            "123456789648937152957812364736249815291685473584371296365728941812594637479163528",
            "123456789845937162967812354438279516291563478756148923384725691512694837679381245",
            "123456789546987123987132654765249318291368475834571296658723941312894567479615832",
            "123456789854937162967182345635279418291348576748561293386724951412695837579813624",
            "123456789854937126967182453541279368296318574738564912485723691312695847679841235",
            "123456789458937162967182345635279814294861573781543296346728951812695437579314628",
            "123456789546987123987312654765249318291638475834571296658723941312894567479165832",
            "123456789546987123987312645465279318298135476731648952654723891312894567879561234",
            "123456789854937162967182345735269418291348576648571293386724951412695837579813624",
            "123456789846937125957182643461279358295318476738645912684723591312594867579861234",
            "123456789546987123987312645765249318298135476431678952654723891312894567879561234",
            "123456789548937162967182354634279815295861473781543296356728941812694537479315628",
            "123456789548937162967182354735249816291863475684571293356728941812694537479315628",
            "123456789456987123987312654564279318298134576731568942645723891312895467879641235",
            "123456789845937162967812354738249516291563478456178923384725691512694837679381245",
            "123456789485937162967182345834279516291564873756813924348725691512698437679341258",
            "123456789546987123987312654765249318291538476834671295658723941312894567479165832",
            "123456789548937162967182354634279815295861473781345296356728941812694537479513628",
            "123456789648937152957812364736249815291385476584671293365728941812594637479163528",
            "123456789546987123987312645761249358298135476435678912654723891312894567879561234",
            "123456789845937162967182354738249516291568473654371298386725941512694837479813625",
            "123456789485937162967812345734289516291563874856174923348725691512698437679341258",
            "123456789845937162967812345438279516291568473756143928384725691512694837679381254",
            "123456789846937125957182643461279358298315476735648912684723591312594867579861234",
            "123456789546987123987312654765249318291638475834175296658723941312894567479561832",
            "123456789456987123987132654564279318298314576731568942645723891312895467879641235",
            "123456789546987123987312645761249358295138476438675912654723891312894567879561234",
            "123456789845937162967182354634279518291568473758341296386725941512694837479813625",
            "123456789845937162967812345738249516291563478456178923384725691512694837679381254",
            "123456789485937162967812345834279516291564873756183924348725691512698437679341258",
            "123456789546987123987132645465279318298315476731648952654723891312894567879561234",
            "123456789854937126967182453541279368298361574736548912485723691312695847679814235",
            "123456789458937162967182345635279814291864573784513296346728951812695437579341628",
            "123456789458937162967812345735269814291384576684571293346728951812695437579143628",
            "123456789845937162967812354438279516291568473756143928384725691512694837679381245",
            "123456789845937126967182543451279368298361475736845912584723691312694857679518234",
            "123456789546987123987132645761249358298315476435678912654723891312894567879561234",
            "123456789854937162967812345635279418298143576741568293386724951412695837579381624",
            "123456789456987123987132654564279318291348576738561942645723891312895467879614235",
            "123456789458937162967812345635279814291384576784561293346728951812695437579143628",
            "123456789458937162967182345534279816291864573786513924345728691812695437679341258",
            "123456789845937126967182543451279368298361475736548912584723691312694857679815234",
            "123456789546987123987132645765249318298315476431678952654723891312894567879561234",
            "123456789845937162967812354634279518291568473758143296386725941512694837479381625",
            "123456789548937162967182354634279815291865473785341296356728941812694537479513628",
            "123456789546987123987312645465279318291538476738641952654723891312894567879165234",
            "123456789845937162967812354738249516291568473654371298386725941512694837479183625",
            "123456789845937162967812354634279518298561473751348296386725941512694837479183625",
            "123456789845937162967812354738249516291568473654173298386725941512694837479381625",
            "123456789458937162967182345635279814291843576784561293346728951812695437579314628",
            "123456789845937162967812354634279518291568473758341296386725941512694837479183625",
            "123456789546987123987132645761249358295318476438675912654723891312894567879561234",
            "123456789854937162967812345635279418298341576741568293386724951412695837579183624",
            "123456789854937126967812453541279368298361574736584912485723691312695847679148235",
            "123456789854937126967812453541279368298361574736548912485723691312695847679184235",
            "123456789458937162967182345735249816291863574684571293346728951812695437579314628",
            "123456789458937162967182345735269814291843576684571293346728951812695437579314628",
            "123456789854937162967812345635279418291348576748561293386724951412695837579183624",
            "123456789845937126967812543451279368298361475736548912584723691312694857679185234",
            "123456789456987123987132654561279348294318576738564912645723891312895467879641235",
            "123456789854937162967812345735269418291348576648571293386724951412695837579183624",
            "123456789456987123987132654561279348298314576734568912645723891312895467879641235",
            "123456789648937152957182364534279816296815473781643295365728941812594637479361528",
            "123456789456987123987312654561279348294138576738564912645723891312895467879641235",
            "123456789648937152957812364534279816296185473781643295365728941812594637479361528",
            "123456789456987123987132654561279348298341576734568912645723891312895467879614235",
            "123456789845937162967812354738249516291563478654178293386725941512694837479381625",
            "123456789648937152957182364534279816291865473786341295365728941812594637479613528",
            "123456789456987123987312654561279348298134576734568912645723891312895467879641235",
            "123456789654987123987132456541279368296318574738564912465723891312895647879641235",
            "123456789645987123987312546756249318291638475438175962564723891312894657879561234",
            "123456789458937162967182345635279814294813576781564293346728951812695437579341628",
            "123456789648937152957812364534279816291683475786145293365728941812594637479361528",
            "123456789645987123987132546751249368298361475436578912564723891312894657879615234",
            "123456789645987123987312546456279318291638475738145962564723891312894657879561234",
            "123456789648937152957182364534279816296813475781645293365728941812594637479361528",
            "123456789546987123987312645461279358298531476735648912654723891312894567879165234",
            "123456789546987123987312645461279358298135476735648912654723891312894567879561234",
            "123456789654987123987132456546279318291368574738541962465723891312895647879614235",
            "123456789645987123987132546451279368298361475736548912564723891312894657879615234",
            "123456789654987123987132456546279318291368574738514962465723891312895647879641235",
            "123456789648937152957812364534279816291685473786143295365728941812594637479361528",
            "123456789546987123987312645461279358295138476738645912654723891312894567879561234",
            "123456789648937152957812364534279816291685473786341295365728941812594637479163528",
            "123456789845397162967812354734289516298561473651743928386975241512634897479128635",
            "123456789458937162967812345635279814294381576781564293346728951812695437579143628",
            "123456789654987123987132456541279368298361574736548912465723891312895647879614235",
            "123456789645987123987312546451279368298631475736548912564723891312894657879165234",
            "123456789845397162967812354734289516291563478658741923386975241512634897479128635",
            "123456789645987123987312546756249318298631475431578962564723891312894657879165234",
            "123456789645987123987132546456279318298361475731548962564723891312894657879615234",
            "123456789648937152957812364534279816296381475781645293365728941812594637479163528",
            "123456789546987123987132645461279358295318476738645912654723891312894567879561234",
            "123456789654987123987132456546279318298361574731548962465723891312895647879614235",
            "123456789645987123987312546751249368298631475436578912564723891312894657879165234",
            "123456789645987123987312546756249318291638475438571962564723891312894657879165234",
            "123456789648397152957812364734269815296185473581743926365978241812534697479621538",
            "123456789845397162967812354738249516291563478654781923386975241512634897479128635",
            "123456789645987123987132546456279318291368475738541962564723891312894657879615234",
            "123456789546987123987132645461279358298315476735648912654723891312894567879561234",
            "123456789648397152957182364734269815296815473581743926365978241812534697479621538",
            "123456789648937152957812364534279816296183475781645293365728941812594637479361528",
            "123456789645987123987312546456279318298631475731548962564723891312894657879165234",
            "123456789645987123987132546756249318291368475438571962564723891312894657879615234",
            "123456789645987123987312546456279318291638475738541962564723891312894657879165234",
            "123456789458937162967812345635279814294183576781564293346728951812695437579341628",
            "123456789645987123987312546751249368296138475438675912564723891312894657879561234",
            "123456789845397162967182354738249516291568473654713928386975241512634897479821635",
            "123456789654987123987312456546279318291638574738541962465723891312895647879164235",
            "123456789645987123987132546756249318298361475431578962564723891312894657879615234",
            "123456789845397162967182354734269518298513476651748923386975241512634897479821635",
            "123456789645987123987132546751249368296318475438675912564723891312894657879561234",
            "123456789548397162967182354735249816291865473684713925356978241812634597479521638",
            "123456789654987123987312456541279368296138574738564912465723891312895647879641235",
            "123456789548397162967812354734269815295183476681745923356978241812634597479521638",
            "123456789548397162967182354734269815295813476681745923356978241812634597479521638",
            "123456789648397152957182364736249815291865473584713926365978241812534697479621538",
            "123456789645987123987312546451279368296138475738645912564723891312894657879561234",
            "123456789645987123987132546451279368296318475738645912564723891312894657879561234",
            "123456789645987123987132564756249318291368475834571296568723941312894657479615832",
            "123456789654987123987312456546279318298631574731548962465723891312895647879164235",
            "123456789654987123987312456541279368298631574736548912465723891312895647879164235",
            "123456789645987123987312564756249318291538476834671295568723941312894657479165832",
            "123456789548397162967182354735249816291863475684715923356978241812634597479521638",
            "123456789458937162967182345735249816294861573681573294346728951812695437579314628",
            "123456789854937162967182345735269418298341576641578293386724951412695837579813624",
            "123456789458937162967812345735269814294381576681574293346728951812695437579143628",
            "123456789845397162967182354738249516291563478654718923386975241512634897479821635",
            "123456789645987123987312564756249318291638475834175296568723941312894657479561832",
            "123456789854937162967812345735269418298143576641578293386724951412695837579381624",
            "123456789645987123987312564756249318291638475834571296568723941312894657479165832",
            "123456789854937162967812345735269418298341576641578293386724951412695837579183624",
            "123456789654987123987132465745269318291348576836571294468723951312895647579614832",
            "123456789458937162967182345735269814294813576681574293346728951812695437579341628",
            "123456789654987123987312465745269318296138574831574296468723951312895647579641832",
            "123456789458937162967812345735269814294183576681574293346728951812695437579341628",
            "123456789654987123987132465745269318296318574831574296468723951312895647579641832",
            "123456789845397162967812354734269518291583476658741923386975241512634897479128635",
            "123456789548397162967812354734269815291583476685741923356978241812634597479125638",
            "123456789485937162967182543854279316296315874731864925548723691312698457679541238",
            "123456789684937152957812463746289315291345876835671924468723591312598647579164238",
            "123456789845937162967812534758249316291368475436571928584723691312694857679185243",
            "123456789854937162967812435548279316291364578736581924485723691312695847679148253",
            "123456789846937152957812643768249315291365478435178926684723591312594867579681234",
            "123456789846937152957182643768249315291365478435871926684723591312594867579618234",
            "123456789845937162967812534758249316291368475436175928584723691312694857679581243",
            "123456789584937162967182453845279316296341875731865924458723691312698547679514238",
            "123456789486937152957812643764289315291365874835174926648723591312598467579641238",
            "123456789584937162967182453845279316296314875731865924458723691312698547679541238",
            "123456789845937162967812534758249316291365478436178925584723691312694857679581243",
            "123456789845937162967812534458279316291365478736148925584723691312694857679581243",
            "123456789846937152957812634768249315295381476431675928684723591312594867579168243",
            "123456789684937152957812463846279315291345876735681924468723591312598647579164238",
            "123456789846937152957812634768249315291385476435671928684723591312594867579168243",
            "123456789846937152957812643468279315291365478735148926684723591312594867579681234",
            "123456789486937152957182643864279315295361874731845926648723591312598467579614238",
            "123456789486937152957182643864279315291365874735814926648723591312598467579641238",
            "123456789854937162967182435548279316291364578736518924485723691312695847679841253",
            "123456789486937152957182643864279315291365874735841926648723591312598467579614238",
            "123456789684937152957182463846279315295314876731865924468723591312598647579641238",
            "123456789486937152957812643864279315291365874735184926648723591312598467579641238",
            "123456789846937152957182643468279315291365478735841926684723591312594867579618234",
            "123456789845937162967182534758249316291368475436571928584723691312694857679815243",
            "123456789845937162967182534458279316291365478736841925584723691312694857679518243",
            "123456789846937152957182643768249315295361478431875926684723591312594867579618234",
            "123456789684937152957812463846279315295341876731685924468723591312598647579164238",
            "123456789846937152957182634768249315295318476431675928684723591312594867579861243",
            "123456789845937162967182534758249316291365478436871925584723691312694857679518243",
            "123456789684937152957182463846279315291345876735861924468723591312598647579614238",
            "123456789684937152957812463746289315295341876831675924468723591312598647579164238",
            "123456789854937162967812453548279316291364578736581924485723691312695847679148235",
            "123456789845937162967812543458279316291365478736148925584723691312694857679581234",
            "123456789846937152957182643468279315295361478731845926684723591312594867579618234",
            "123456789845937162967812543758249316291365478436178925584723691312694857679581234",
            "123456789684937152957182463846279315295341876731865924468723591312598647579614238",
            "123456789854937162967182453548279316296314578731568924485723691312695847679841235",
            "123456789854937162967812453548279316296341578731568924485723691312695847679184235",
            "123456789846937152957182643768249315295318476431675928684723591312594867579861234",
            "123456789486937152957812643764289315295341876831675924648723591312598467579164238",
            "123456789846937152957812634768249315291365478435178926684723591312594867579681243",
            "123456789485937162967812543754289316291364875836175924548723691312698457679541238",
            "123456789854937162967182453548279316296341578731568924485723691312695847679814235",
            "123456789846937152957812643768249315295381476431675928684723591312594867579168234",
            "123456789846937152957182634768249315291365478435871926684723591312594867579618243",
            "123456789854937162967182453548279316291364578736518924485723691312695847679841235",
            "123456789845937162967182543458279316291365478736841925584723691312694857679518234",
            "123456789845937162967182543758249316291365478436871925584723691312694857679518234",
            "123456789684937152957812463746289315291365874835174926468723591312598647579641238",
            "123456789846937152957812634468279315291365478735148926684723591312594867579681243",
            "123456789584937162967812453745289316291365874836174925458723691312698547679541238",
            "123456789846937152957182634768249315295361478431875926684723591312594867579618243",
            "123456789846937152957182634468279315291365478735841926684723591312594867579618243",
            "123456789854937162967182435548279316296341578731568924485723691312695847679814253",
            "123456789645987132987312564456279318298135476731648925564723891312894657879561243",
            "123456789645987132987312564756249318291638475438175926564723891312894657879561243",
            "123456789645987132987312564756249318298531476431678925564723891312894657879165243",
            "123456789584937162967812453745289316291364875836175924458723691312698547679541238",
            "123456789854937162967182435548279316296314578731568924485723691312695847679841253",
            "123456789486937152957812643764289315291345876835671924648723591312598467579164238",
            "123456789654987132987312465546279318298134576731568924465723891312895647879641253",
            "123456789584937162967182453845279316291364875736815924458723691312698547679541238",
            "123456789645987132987312564756249318291638475438571926564723891312894657879165243",
            "123456789645987132987132564756249318291368475438571926564723891312894657879615243",
            "123456789584937162967812453745289316291364875836571924458723691312698547679145238",
            "123456789456987132987312645564279318291638574738541926645723891312895467879164253",
            "123456789485937162967812543754289316291364875836571924548723691312698457679145238",
            "123456789645987132987312564756249318298135476431678925564723891312894657879561243",
            "123456789854937162967812435548279316296341578731568924485723691312695847679184253",
            "123456789485937162967182543854279316291365874736814925548723691312698457679541238",
            "123456789546987132987312654765249318291538476438671925654723891312894567879165243",
            "123456789645987132987132564756249318298315476431678925564723891312894657879561243",
            "123456789456987132987312645564279318298631574731548926645723891312895467879164253",
            "123456789485937162967812543754289316291365874836174925548723691312698457679541238",
            "123456789645987132987312564456279318298531476731648925564723891312894657879165243",
            "123456789485937162967182543854279316291365874736841925548723691312698457679514238",
            "123456789846937152957182634468279315295361478731845926684723591312594867579618243",
            "123456789645987132987312564456279318291538476738641925564723891312894657879165243",
            "123456789846937152957812643768249315291385476435671928684723591312594867579168234",
            "123456789546987132987312654465279318298631475731548926654723891312894567879165243",
            "123456789845937162967182543758249316291368475436571928584723691312694857679815234",
            "123456789546987132987312654465279318291638475738541926654723891312894567879165243",
            "123456789546987132987132654765249318298361475431578926654723891312894567879615243",
            "123456789546987132987132654765249318298315476431678925654723891312894567879561243",
            "123456789485937162967812543854279316291365874736184925548723691312698457679541238",
            "123456789546987132987312654465279318291638475738145926654723891312894567879561243",
            "123456789546987132987312654765249318298631475431578926654723891312894567879165243",
            "123456789845937162967812543758249316291368475436571928584723691312694857679185234",
            "123456789546987132987312654765249318298135476431678925654723891312894567879561243",
            "123456789584937162967812453845279316291364875736581924458723691312698547679145238",
            "123456789845937162967812543758249316291368475436175928584723691312694857679581234",
            "123456789584937162967812453845279316291364875736185924458723691312698547679541238",
            "123456789645987132987132564456279318298315476731648925564723891312894657879561243",
            "123456789654987132987132465546279318298341576731568924465723891312895647879614253",
            "123456789546987132987312654765249318298531476431678925654723891312894567879165243",
            "123456789654987132987132465546279318298314576731568924465723891312895647879641253",
            "123456789645987132987312564756249318291538476438671925564723891312894657879165243",
            "123456789456987132987132645564279318298361574731548926645723891312895467879614253",
            "123456789846937152957812634768249315291365478534178296685723941312594867479681523",
            "123456789456987132987312645765249318291638574834571296648723951312895467579164823",
            "123456789846937152957182634768249315291365478534871296685723941312594867479618523",
            "123456789645987132987312564756249318298631475431578926564723891312894657879165243",
            "123456789546987132987312654765249318291638475438571926654723891312894567879165243",
            "123456789546987132987132654465279318298361475731548926654723891312894567879615243",
            "123456789845937162967182534758249316291365478634871295586723941312694857479518623",
            "123456789645987132987132564756249318298361475431578926564723891312894657879615243",
            "123456789654987132987132465745269318291348576836571294468723951312895647579614823",
            "123456789854937162967812435745289316291364578638571294486723951312695847579148623",
            "123456789456987132987132645765249318291368574834571296648723951312895467579614823",
            "123456789546987132987312654765249318291638475438175926654723891312894567879561243",
            "123456789845937162967812534758249316291365478634178295586723941312694857479581623",
            "123456789854937162967812435745289316298361574631574298486723951312695847579148623",
            "123456789845937162967812534758249316291368475634175298586723941312694857479581623",
            "123456789546987132987132654765249318291368475834571296658723941312894567479615823",
            "123456789654987132987312465745269318296138574831574296468723951312895647579641823",
            "123456789645987132987312564756249318291638475834175296568723941312894657479561823",
            "123456789845937162967812534758249316291368475634571298586723941312694857479185623",
            "123456789654987132987132465745269318296318574831574296468723951312895647579641823",
            "123456789456987132987132645564279318291368574738514926645723891312895467879641253",
            "123456789645987132987312564756249318291538476834671295568723941312894657479165823",
            "123456789654987132987132465546279318291368574738514926465723891312895647879641253",
            "123456789645987132987312564756249318291638475834571296568723941312894657479165823",
            "123456789546987132987132654465279318298315476731648925654723891312894567879561243",
            "123456789456987132987132645564279318291368574738541926645723891312895467879614253",
            "123456789546987132987132654765249318291368475438571926654723891312894567879615243",
            "123456789546987132987312654765249318291638475834175296658723941312894567479561823",
            "123456789654987132987312465546279318291638574738541926465723891312895647879164253",
            "123456789546987132987312654765249318291638475834571296658723941312894567479165823",
            "123456789546987132987132654465279318291368475738541926654723891312894567879615243",
            "123456789645987132987132564456279318298361475731548926564723891312894657879615243",
            "123456789645987132987132564756249318291368475834571296568723941312894657479615823",
            "123456789456987132987132645564279318291348576738561924645723891312895467879614253",
            "123456789645987132987312564456279318291638475738541926564723891312894657879165243",
            "123456789645987132987312564456279318291638475738145926564723891312894657879561243",
            "123456789645987132987132564456279318291368475738541926564723891312894657879615243",
            "123456789845937162967182534758249316291368475634571298586723941312694857479815623",
            "123456789456987132987132645564279318298341576731568924645723891312895467879614253",
            "123456789654987132987132465546279318291348576738561924465723891312895647879614253",
            "123456789456987132987132645564279318298314576731568924645723891312895467879641253",
            "123456789654987132987132465546279318291368574738541926465723891312895647879614253",
            "123456789645987132987312564456279318298631475731548926564723891312894657879165243",
            "123456789846937152957812634768249315291385476534671298685723941312594867479168523",
            "123456789654987132987132465546279318298361574731548926465723891312895647879614253",
            "123456789654987132987312465546279318298631574731548926465723891312895647879164253",
            "123456789546987132987312654765249318291538476834671295658723941312894567479165823",
            "123456789546987132987312654465279318298531476731648925654723891312894567879165243",
            "123456789546987132987312654465279318291538476738641925654723891312894567879165243",
            "123456789846397152957182634768249315291635478534718926685973241312564897479821563",
            "123456789546987123987312654765249318291638475834571962658793241312864597479125836",
            "123456789546987132987312654864279315291635478735148926658793241312864597479521863",
            "123456789456987123987312645765249318291638574834571962648793251312865497579124836",
            "123456789654987123987132465845279316296314578731568942468793251312845697579621834",
            "123456789456987132987312645564279318298134576731568924645723891312895467879641253",
            "123456789654987123987132465845279316291364578736518942468793251312845697579621834",
            "123456789546987132987312654465279318298135476731648925654723891312894567879561243",
            "123456789456987123987312645865279314294631578731548962648793251312865497579124836",
            "123456789654987132987312465745269318296138574831574926468793251312845697579621843",
            "123456789854937162967812435548279316291368574736541928485723691312695847679184253",
            "123456789485937162967182543854279316296314875731865924548723691312698457679541238",
            "123456789584937162967182453845279316296315874731864925458723691312698547679541238",
            "123456789546987123987312654864279315295631478731548962658793241312864597479125836",
            "123456789854937162967182435548279316291368574736541928485723691312695847679814253",
            "123456789486937152957182643864279315295314876731865924648723591312598467579641238",
            "123456789854937162967182453548279316296318574731564928485723691312695847679841235",
            "123456789546987132987312654864279315295138476731645928658793241312864597479521863",
            "123456789654987132987312465845279316296138574731564928468793251312845697579621843",
            "123456789654987132987132465845279316296314578731568924468793251312845697579621843",
            "123456789854937162967182435548279316291368574736514928485723691312695847679841253",
            "123456789654987132987132465845279316291364578736518924468793251312845697579621843",
            "123456789854937162967182435548279316296318574731564928485723691312695847679841253",
            "123456789654987132987132465745269318296318574831574926468793251312845697579621843",
            "123456789654987132987132465845279316296318574731564928468793251312845697579621843",
            "123456789654987132987312465845279316296134578731568924468793251312845697579621843",
            "123456789654987132987132465845279316291368574736514928468793251312845697579621843",
            "123456789546987132987312654864279315291538476735641928658793241312864597479125863",
            "123456789546987132987312654765249318291638475834571926658793241312864597479125863",
            "123456789845937162967812534458279316291368475736145928584723691312694857679581243",
            "123456789546987132987312654765249318291538476834671925658793241312864597479125863",
            "123456789846937152957182634468279315295318476731645928684723591312594867579861243",
            "123456789546987132987312654765249318291638475834175926658793241312864597479521863",
            "123456789854937162967182435645279318298314576731568924486793251312645897579821643",
            "123456789546987132987312654864279315295631478731548926658793241312864597479125863",
            "123456789846937152957182643468279315295318476731645928684723591312594867579861234",
            "123456789456987132987312645765249318291638574834571926648793251312865497579124863",
            "123456789846937152957812634468279315291385476735641928684723591312594867579168243",
            "123456789854937162967182435645279318291368574738514926486793251312645897579821643",
            "123456789854937162967182435745269318298314576631578924486793251312645897579821643",
            "123456789456987132987312645865279314294631578731548926648793251312865497579124863",
            "123456789845937162967812534458279316291368475736541928584723691312694857679185243",
            "123456789854937126967182435645279318291368574738514962486793251312645897579821643",
            "123456789654987123987312465845279316296134578731568942468793251312845697579621834",
            "123456789845937162967182534458279316291368475736541928584723691312694857679815243",
            "123456789546987123987312654864279315291635478735148962658793241312864597479521836",
            "123456789854937126967182435745219368291368574638574912486793251312645897579821643",
            "123456789485937162967182543854279316296341875731865924548723691312698457679514238",
            "123456789546987123987312654765249318291638475834175962658793241312864597479521836",
            "123456789486937152957812643864279315295341876731685924648723591312598467579164238",
            "123456789846937125957182634761249358298315476534678912685793241312564897479821563",
            "123456789546987132987132654864279315295318476731645928658793241312864597479521863",
            "123456789486937152957812643864279315291345876735681924648723591312598467579164238",
            "123456789854937162967812453548279316296381574731564928485723691312695847679148235",
            "123456789486937152957182643864279315295341876731865924648723591312598467579614238",
            "123456789846397152957812634768249315291635478534781926685973241312564897479128563",
            "123456789846937125957182634564279318298315476731648952685793241312564897479821563",
            "123456789846937152957812643468279315295381476731645928684723591312594867579168234",
            "123456789684937152957812463846279315291365874735184926468723591312598647579641238",
            "123456789854937126967812435645279318298361574731584962486793251312645897579128643",
            "123456789846397152957812634764289315291635478538741926685973241312564897479128563",
            "123456789846937152957182634564279318298315476731648925685793241312564897479821563",
            "123456789684937152957182463846279315295361874731845926468723591312598647579614238",
            "123456789846937152957812634564279318291385476738641925685793241312564897479128563",
            "123456789846937125957812634564279318291385476738641952685793241312564897479128563",
            "123456789854937162967812435645279318298361574731584926486793251312645897579128643",
            "123456789854937162967812435548279316296381574731564928485723691312695847679148253",
            "123456789584937162967812453845279316291365874736184925458723691312698547679541238",
            "123456789846937152957812634768249315291385476534671928685793241312564897479128563",
            "123456789846937152957812634468279315295381476731645928684723591312594867579168243",
            "123456789854937162967812435645279318291384576738561924486793251312645897579128643",
            "123456789485937162967812543854279316291364875736581924548723691312698457679145238",
            "123456789485937162967812543854279316291364875736185924548723691312698457679541238",
            "123456789854937162967812435745289316298361574631574928486793251312645897579128643",
            "123456789854937162967812435745269318291384576638571924486793251312645897579128643",
            "123456789854937162967812435745289316291364578638571924486793251312645897579128643",
            "123456789485937162967182543854279316291364875736815924548723691312698457679541238",
            "123456789584937162967182453845279316291365874736841925458723691312698547679514238",
            "123456789684937152957182463846279315291365874735841926468723591312598647579614238",
            "123456789584937162967182453845279316291365874736814925458723691312698547679541238",
            "123456789486937152957182643864279315291345876735861924648723591312598467579614238",
            "123456789684937152957182463846279315291365874735814926468723591312598647579641238",
            "123456789854937162967182453548279316291368574736514928485723691312695847679841235",
            "123456789845937162967812543458279316291368475736145928584723691312694857679581234",
            "123456789845937162967182543458279316291368475736541928584723691312694857679815234",
            "123456789854937162967812453548279316291368574736541928485723691312695847679184235",
            "123456789854937162967182453548279316291368574736541928485723691312695847679814235",
            "123456789846937152957812643468279315291385476735641928684723591312594867579168234",
            "123456789845937162967812543458279316291368475736541928584723691312694857679185234"
        };
    }
}
