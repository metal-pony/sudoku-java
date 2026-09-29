package io.github.metal_pony.sudoku;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import static io.github.metal_pony.sudoku.Constants.*;

public class TestSudokuSieve {
    // Location of SudokuSieves json files.
    private static final String FIXTURES_RESOURCE_LVL2 = "/sieve-fixtures-lvl-2.json";
    private static final String FIXTURES_RESOURCE_LVL3 = "/sieve-fixtures-lvl-3.json";

    // Reads the json test fixtures (array of SudokuSieve) from resources.
    private static List<SudokuSieve> readTestFixtures(String resourcePath) {
        InputStream fixtureStream = TestSudokuSieve.class.getResourceAsStream(resourcePath);
        Gson gson = new Gson();
        JsonReader reader = gson.newJsonReader(new InputStreamReader(fixtureStream));

        List<SudokuSieve> result = new ArrayList<>();

        try {
            reader.beginArray();
            while (reader.hasNext()) {
                result.add(readSieveJson(reader));
            }
            reader.endArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Reads an individual SudokuSieve json object.
    private static SudokuSieve readSieveJson(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        Sudoku config = null;
        List<BigInteger> items = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            if ("config".equals(name)) {
                String value = reader.nextString();
                config = new Sudoku(value);
                continue;
            } else if ("items".equals(name)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    String itemStr = reader.nextString();
                    BigInteger bigItem = new BigInteger(itemStr);
                    items.add(bigItem);
                }
                reader.endArray();
            } else {
                throw new com.google.gson.JsonParseException(
                    String.format("Unexpected name in sudoku sieve json: %s", name)
                );
            }
        }
        SudokuSieve sieve = new SudokuSieve(config);
        items.forEach(item -> {
            sieve.rawAdd(new SudokuMask(item));
        });
        reader.endObject();

        return sieve;
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
        for (SudokuSieve f : fixtures2) {
            if (f.isEmpty()) {
                throw new RuntimeException("One or more sieve in test fixture2 is empty!");
            }
        }
        fixtures3 = readTestFixtures(FIXTURES_RESOURCE_LVL3);
        if (fixtures3.isEmpty())
            throw new RuntimeException("Test fixtures3 did not load properly!");
        for (SudokuSieve f : fixtures3) {
            if (f.isEmpty()) {
                throw new RuntimeException("One or more sieve in test fixture3 is empty!");
            }
        }
    }

    private final String configFixtureStr = "218574639573896124469123578721459386354681792986237415147962853695318247832745961";
    private Sudoku configFixture;
    private SudokuSieve sieve;
    Sudoku validGrid;
    Sudoku incompleteGrid;

    @BeforeEach
    void before() {
        configFixture = new Sudoku(configFixtureStr);
        sieve = new SudokuSieve(configFixture.toArray());
        validGrid = Sudoku.configSeed().solution();
        incompleteGrid = Sudoku.configSeed();
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
            subject.seed(subject.digitCombos(2));
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
            subject.seed(subject.digitCombos(3));
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
    void seed_whenMasksIsNull_throws() {
        SudokuSieve subject = new SudokuSieve(Sudoku.generateConfig());
        assertThrows(NullPointerException.class, () -> {
            subject.seedThreaded(null);
        });
    }

    @Test
    void seedThreaded_whenNumThreadsIsNotPositive_throws() {
        SudokuSieve subject = new SudokuSieve(Sudoku.generateConfig());
        List.of(0, -1, -2, -4, -10, -100).forEach(numThreads -> {
            assertThrows(IllegalArgumentException.class, () -> {
                subject.seedThreaded(new ArrayList<>(), numThreads);
            });
        });
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
                subject.seedThreaded(subject.digitCombos(2), 1);
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
                subject.seedThreaded(subject.digitCombos(3), 1);
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
        final int EXPECTED_SIEVE_SIZE = 56;
        final int EXPECTED_REMOVED_ITEMS_SIZE = 8;

        populateSieveForAllDigitCombos(2);
        assertEquals(sieve.size(), EXPECTED_SIEVE_SIZE);
        // System.out.println(sieve.toString());
        List<SudokuMask> removed = sieve.removeOverlapping(0);
        // System.out.println("Removed:");
        // System.out.println(configFixtureStr);
        // removed.forEach(r -> {
        //     System.out.println(configFixture.filter(r).toString());
        // });
        assertEquals(removed.size(), EXPECTED_REMOVED_ITEMS_SIZE);
        assertEquals(sieve.size(), EXPECTED_SIEVE_SIZE - EXPECTED_REMOVED_ITEMS_SIZE);

        // Attempt to add the items back
        removed.forEach(item -> sieve.add(item));
        removed.clear();
        assertEquals(removed.size(), 0);
        assertEquals(sieve.size(), EXPECTED_SIEVE_SIZE);
    }

    @Test
    void testIsDerivative() {
        // Always true
        assertTrue(sieve.isDerivative(new SudokuMask()));

        // Returns true if the item is a derivative
        SudokuMask item = new SudokuMask("001000001000000000001000001000000000000000000000000000000000000000000000000000000");
        sieve.rawAdd(item);
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int t = 0; t < 100; t++) {
            SudokuMask clearlyDerivative = new SudokuMask(item);
            while (clearlyDerivative.bitCount() <= item.bitCount()) {
                clearlyDerivative.setBit(rand.nextInt(81));
            }
            assertTrue(sieve.isDerivative(clearlyDerivative));
        }

        sieve = new SudokuSieve(new Sudoku(SieveItemsFixture.grid));
        SieveItemsFixture.items.forEach(_item -> sieve.rawAdd(_item));
        // Now ALL SieveItemsFixture.items (and subsets derived from) should be flagged as derivative
        SieveItemsFixture.items.forEach(_item -> {
            assertTrue(sieve.isDerivative(_item));
        });
    }

    @Test
    void test_validate() {
        sieve = new SudokuSieve(new Sudoku(SieveItemsFixture.grid));
        SieveItemsFixture.items.forEach(_item -> {
            assertTrue(sieve.add(_item));
        });
    }

    @Test
    void testIsDerivate_whenAddingDuplicate_returnsTrue() {
        SudokuMask[] expectedSieveItems = new SudokuMask[] {
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

        populateSieveForAllDigitCombos(2);

        for (SudokuMask dupe : expectedSieveItems) {
            assertTrue(sieve.isDerivative(dupe));
        }
    }

    private void populateSieveForAllDigitCombos(int level) {
        for (int r = DIGIT_COMBOS_MAP[level].length - 1; r >= 0; r--) {
            SudokuMask pMask = configFixture.maskForDigits(DIGIT_COMBOS_MAP[level][r]);
            sieve.addFromFilter(pMask);
        }
    }
}
