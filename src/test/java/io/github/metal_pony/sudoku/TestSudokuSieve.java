package io.github.metal_pony.sudoku;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import io.github.metal_pony.sudoku.util.ArraysUtil;

import static io.github.metal_pony.sudoku.Constants.*;

public class TestSudokuSieve {
    // Location of SudokuSieves json files.
    private static final String FIXTURES_RESOURCE_LVL2 = "/sieve-fixtures-lvl-2.json";
    private static final String FIXTURES_RESOURCE_LVL3 = "/sieve-fixtures-lvl-3.json";

    // Reads the json test fixtures (array of SudokuSieve) from resources.
    private static List<SudokuSieve> readTestFixtures(String resourcePath) {
        Gson gson = Main.buildGsonForSudoku();
        InputStream fixtureStream = TestSudokuSieve.class.getResourceAsStream(resourcePath);
        JsonReader reader = gson.newJsonReader(new InputStreamReader(fixtureStream));
        SudokuSieve[] sieves = gson.fromJson(reader, SudokuSieve[].class);
        return Arrays.asList(sieves);
    }

    /** List of valid SudokuSieves, seeded with digitCombos(2).*/
    private static List<SudokuSieve> fixtures2;
    /** List of valid SudokuSieves, seeded with digitCombos(3).*/
    private static List<SudokuSieve> fixtures3;

    @BeforeAll
    static void beforeAll() {
        fixtures2 = readTestFixtures(FIXTURES_RESOURCE_LVL2);
        if (fixtures2.isEmpty())
            throw new RuntimeException("Test fixtures2 did not load properly!");

        fixtures3 = readTestFixtures(FIXTURES_RESOURCE_LVL3);
        if (fixtures3.isEmpty())
            throw new RuntimeException("Test fixtures3 did not load properly!");
    }

    /** Returns a random sieve fixture (either 2 or 3).*/
    static SudokuSieve getRandomFixture() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        List<SudokuSieve> list = (rand.nextDouble() < 0.5) ? fixtures2 : fixtures3;
        return ArraysUtil.chooseRandom(list);
    }

    @Test
    void fixtureConfigsAreValid() {
        assertTrue(fixtures2.size() > 0);
        fixtures2.forEach(f -> {
            Sudoku config = f.config();
            assertTrue(config.isSolved());
            assertTrue(config.isSolved());
        });
        assertTrue(fixtures3.size() > 0);
        fixtures3.forEach(f -> {
            Sudoku config = f.config();
            assertTrue(config.isSolved());
            assertTrue(config.isSolved());
        });
    }

    @Test
    void seed_resultsMatchFixture2() {
        for (int i = 0; i < fixtures2.size(); i++) {
            SudokuSieve fixture = fixtures2.get(i);
            Sudoku config = fixture.config();
            List<SudokuMask> fixtureItems = fixture.items(new ArrayList<>());

            SudokuSieve subject = new SudokuSieve(config);
            assertEquals(0, subject.size());
            subject.seed();
            List<SudokuMask> subjectItems = subject.items(new ArrayList<>());

            assertEquals(fixture.size(), subject.size());
            for (int j = 0; j < fixtureItems.size(); j++) {
                SudokuMask fItem = fixtureItems.get(j);
                SudokuMask subjItem = subjectItems.get(j);

                assertEquals(fItem, subjItem, String.format(
                    "Expected item was not found in seeded sieve:\n[%s] %s",
                    fItem.toBigInt().toString(), fItem.toStringDots()
                ));
            }
        }
    }

    @Test
    void seed_resultsMatchFixture3() {
        for (int i = 0; i < fixtures3.size(); i++) {
            SudokuSieve fixture = fixtures3.get(i);
            Sudoku config = fixture.config();
            List<SudokuMask> fixtureItems = fixture.items(new ArrayList<>());

            SudokuSieve subject = new SudokuSieve(config);
            assertEquals(0, subject.size());
            subject.seed(3);
            List<SudokuMask> subjectItems = subject.items(new ArrayList<>());

            assertEquals(fixture.size(), subject.size());
            for (int j = 0; j < fixtureItems.size(); j++) {
                SudokuMask fItem = fixtureItems.get(j);
                SudokuMask subjItem = subjectItems.get(j);

                assertEquals(fItem, subjItem, String.format(
                    "Expected item was not found in seeded sieve:\n[%s] %s",
                    fItem.toBigInt().toString(), fItem.toStringDots()
                ));
            }
        }
    }

    @Test
    void seedThreaded_resultsMatchFixture2() {
        for (int i = 0; i < fixtures2.size(); i++) {
            SudokuSieve fixture = fixtures2.get(i);
            Sudoku config = fixture.config();
            List<SudokuMask> fixtureItems = fixture.items(new ArrayList<>());

            List.of(1, 2, 8, 16, 64).forEach(numThreads -> {
                SudokuSieve subject = new SudokuSieve(config);
                assertEquals(0, subject.size());

                List<Runnable> seedWork = new ArrayList<>();
                subject.digitCombos(2).forEach(mask -> {
                    seedWork.add(() -> subject.searchForUAs(mask));
                });
                Main.runWithThreads(seedWork, numThreads);

                List<SudokuMask> subjectItems = subject.items(new ArrayList<>());
                assertEquals(fixture.size(), subject.size());
                for (int j = 0; j < fixtureItems.size(); j++) {
                    SudokuMask fItem = fixtureItems.get(j);
                    SudokuMask subjItem = subjectItems.get(j);

                    assertEquals(fItem, subjItem, String.format(
                        "Expected item was not found in seeded sieve:\n[%s] %s",
                        fItem.toBigInt().toString(), fItem.toStringDots()
                    ));
                }
            });
        }
    }

    @Test
    void seedThreaded_resultsMatchFixture3() {
        for (int i = 0; i < fixtures3.size(); i++) {
            SudokuSieve fixture = fixtures3.get(i);
            Sudoku config = fixture.config();
            List<SudokuMask> fixtureItems = fixture.items(new ArrayList<>());

            List.of(1, 2, 8, 16, 64).forEach(numThreads -> {
                SudokuSieve subject = new SudokuSieve(config);
                assertEquals(0, subject.size());

                List<Runnable> seedWork = new ArrayList<>();
                subject.digitCombos(3).forEach(mask -> {
                    seedWork.add(() -> subject.searchForUAs(mask));
                });
                Main.runWithThreads(seedWork, numThreads);

                List<SudokuMask> subjectItems = subject.items(new ArrayList<>());
                assertEquals(fixture.size(), subject.size());
                for (int j = 0; j < fixtureItems.size(); j++) {
                    SudokuMask fItem = fixtureItems.get(j);
                    SudokuMask subjItem = subjectItems.get(j);

                    assertEquals(fItem, subjItem, String.format(
                        "Expected item was not found in seeded sieve:\n[%s] %s",
                        fItem.toBigInt().toString(), fItem.toStringDots()
                    ));
                }
            });
        }
    }

    @Test
    void testRemoveOverlapping_thenAddItemsBack() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        SudokuSieve fixture = fixtures3.get(rand.nextInt(fixtures3.size()));

        // Only modify subject; never fixture.
        SudokuSieve subject = new SudokuSieve(fixture.config());
        fixture.items(new ArrayList<>()).forEach(subject::add);
        assertEquals(fixture.items(), subject.items());

        for (int ci = 0; ci < SPACES; ci++) {
            List<SudokuMask> removed = subject.removeOverlapping(ci);
            assertEquals(fixture.size(), subject.size() + removed.size());

            // Removed items overlap with the cell
            for (SudokuMask removedItem : removed) {
                assertTrue(removedItem.testBit(ci));
            }

            // Remaining items do NOT overlap with the cell
            for (SudokuMask remainingItem : subject.items()) {
                assertFalse(remainingItem.testBit(ci));
            }

            // Removed items can be added back again
            for (SudokuMask removedItem : removed) {
                assertTrue(subject.add(removedItem));
            }

            // Items are now back in sync
            assertEquals(fixture.items(), subject.items());
        }
    }

    @Test
    void testIsDerivative() {
        SudokuSieve subject = ArraysUtil.chooseRandom(fixtures3);
        // Do NOT modify list or items directly because they are shared among the tests below.
        List<SudokuMask> items = subject.items(new ArrayList<>());

        // Always true when mask is empty, even if the sieve is empty.
        assertTrue(subject.isDerivative(new SudokuMask()));
        SudokuSieve emptySieve = new SudokuSieve(Sudoku.generateConfig());
        assertTrue(emptySieve.isDerivative(new SudokuMask()));

        for (int i = 0; i < items.size(); i++) {
            // Do NOT modify this item.
            SudokuMask item = items.get(i);

            // Existing items are always considered derivatives.
            assertTrue(subject.isDerivative(item));

            // Existing items + bits = always a derivative.
            for (int bit : new SudokuMask(item).flip().toIndices()) {
                assertTrue(subject.isDerivative(new SudokuMask(item).setBit(bit)));
            }

            // Existing items - bits = NEVER a derivative.
            for (int bit : item.toIndices()) {
                assertFalse(subject.isDerivative(new SudokuMask(item).unsetBit(bit)));
            }

            // Combinations of items are always derivatives.
            for (int j = i + 1; j < items.size(); j++) {
                SudokuMask comboItem = new SudokuMask(item).add(items.get(j));
                assertTrue(subject.isDerivative(comboItem));
            }
        }
    }

    @Test
    void validate() {
        List<SudokuSieve> allFixtures = new ArrayList<>();
        allFixtures.addAll(fixtures2);
        allFixtures.addAll(fixtures3);

        for (SudokuSieve subject : allFixtures) {
            // Do NOT modify list or items directly because they are shared among the tests below.
            List<SudokuMask> items = subject.items(new ArrayList<>());

            // Empty mask = invalid
            assertFalse(subject.validate(new SudokuMask()));
            // Full mask = invalid
            assertFalse(subject.validate(SudokuMask.full()));

            for (int i = 0; i < items.size(); i++) {
                // Do NOT modify this item.
                SudokuMask item = items.get(i);

                // Existing items = valid
                assertTrue(subject.validate(item));

                // Existing items + bits (i.e. derivative items) = invalid
                for (int bit : new SudokuMask(item).flip().toIndices()) {
                    assertFalse(subject.validate(new SudokuMask(item).setBit(bit)));
                }

                // Existing items - bits = invalid
                for (int bit : item.toIndices()) {
                    assertFalse(
                        subject.validate(new SudokuMask(item).unsetBit(bit)),
                        String.format(
                            "%s\n%s",
                            subject.config().toString(),
                            new SudokuMask(item).unsetBit(bit).toBigInt().toString()
                        )
                    );
                }
            }
        }

        // This combinatorial check was separated to conserve test runtime.
        SudokuSieve subject = fixtures2.getFirst();
        List<SudokuMask> items = subject.items(new ArrayList<>());
        for (int i = 0; i < items.size(); i++) {
            // Do NOT modify this item.
            SudokuMask item = items.get(i);
            // Combinations of items = invalid
            for (int j = i + 1; j < items.size(); j++) {
                SudokuMask comboItem = new SudokuMask(item).add(items.get(j));
                assertFalse(subject.validate(comboItem));
            }
        }
    }
}
