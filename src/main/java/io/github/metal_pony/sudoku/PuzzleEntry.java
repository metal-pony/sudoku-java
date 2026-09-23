package io.github.metal_pony.sudoku;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.gson.Gson;

/**
 * A PuzzleEntry contains a Sudoku puzzle and can generate
 * an associated SolutionRecord if one does not already exist.
 * PuzzleEntry can also load data from sudoku17 resource files
 * to provide all 17-clue sudoku puzzles.
 */
public class PuzzleEntry {
    /** Name of the resources directory on the classpath.*/
    static final String RESOURCES_DIR = "resources";
    /** Name of the JSON resource containing 17-clue puzzle records.*/
    static final String PUZZLES_17_JSON_RESOURCE = "17-puzzle-records.json";
    /** Name of the text file resource containing 17-clue puzzles.*/
    static final String PUZZLES_17_RESOURCE = "sudoku-17.txt";

    /**
     * Gets the 17-clue puzzle file content as a stream.
     */
    static InputStream puzzles17Stream() {
        String rscName = String.format("/%s/%s", RESOURCES_DIR, PUZZLES_17_RESOURCE);
        return PuzzleEntry.class.getResourceAsStream(rscName);
    }

    Sudoku sudoku;
    SolutionRecord solution;

    /**
     * Creates a new PuzzleEntry with the given puzzle.
     * @param puzzleStr Sudoku puzzle string.
     */
    public PuzzleEntry(String puzzleStr) {
        this.sudoku = new Sudoku(puzzleStr);
        this.solution = null;
    }

    /**
     * Gets a copy of the puzzle.
     * @return Sudoku puzzle.
     */
    public Sudoku puzzle() {
        return new Sudoku(sudoku);
    }

    /**
     * Gets the SolutionRecord associated with this puzzle.
     * If it doesn't exist, the puzzle solution will be generated
     * and a record will be created.
     * @return SolutionRecord for this puzzle.
     * @throws RuntimeException if this puzzle does not have a single solution.
     */
    public SolutionRecord solution() {
        if (solution == null) {
            int flag = puzzle().solutionsFlag();
            if (flag == 1) {
                Sudoku s = puzzle().solution();
                solution = new SolutionRecord(s.toString());
            } else {
                throw new RuntimeException("PuzzleEntry does not have single solution.");
            }
        }
        return solution;
    }

    @Override
    public String toString() {
        return this.sudoku.toString();
    }

    private static final String JSON_FORMAT = """
    {
      "puzzle":   "%s",
      "solution": "%s",
      "dc2":      "%s",
      "dc3":      "%s"
    }""";
    /**
     * Gets a JSON string representation for this record.
     * The result will contain this puzzle string, solution string,
     * dc2 and dc3 fingerprints.
     * @return JSON string representation.
     */
    public String toJson() {
        // Ensure SolutionRecord exists
        solution();
        return String.format(
            JSON_FORMAT,
            toString(),
            solution.toString(),
            solution.dc2(),
            solution.dc3()
        );
    }

    private static final String CSV_FORMAT = "%s,%s,%s";
    /**
     * Gets a csv string representation for this record.
     * The result will contain this puzzle string, solution string,
     * and the dc2 fingerprint.
     * @return CSV string representation.
     */
    public String toCsv() {
        // Ensure SolutionRecord exists
        solution();
        return String.format(
            CSV_FORMAT,
            toString(),
            solution.toString(),
            solution.dc2()
        );
    }

    /**
     * Reads Puzzle entries from the given inputstream.
     * @param inStream InputStream to read from. Should contain JSON entries.
     * @return Array of PuzzleEntries read from the stream.
     */
    public static PuzzleEntry[] readFromJsonInStream(InputStream inStream) {
        try (Reader reader = new InputStreamReader(inStream)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, PuzzleEntry[].class);
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return new PuzzleEntry[0];
        }
    }

    /**
     * Reads all puzzle entries from 'resources/17-puzzle-records.json'.
     * @return Array of all PuzzleEntry.
     */
    public static PuzzleEntry[] all17() {
        return PuzzleEntry.readFromJsonInStream(
            PuzzleEntry.class.getResourceAsStream(String.format(
                "/%s/%s",
                RESOURCES_DIR,
                PUZZLES_17_JSON_RESOURCE
            ))
        );
    }

    /**
     * Reads all 17-clue sudoku puzzles from the resource file.
     * @return List of all 17-clue sudoku puzzles.
     */
    public static List<PuzzleEntry> allSudoku17() {
        List<PuzzleEntry> entries = new ArrayList<>();

        Scanner scanner = new Scanner(
            PuzzleEntry.class.getResourceAsStream(String.format(
                "/%s/%s",
                RESOURCES_DIR,
                PUZZLES_17_RESOURCE
            ))
        );

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            entries.add(new PuzzleEntry(line));
        }

        scanner.close();
        return entries;
    }

    /**
     * Reads all 17-clue sudoku puzzles from the resource file.
     * @return Stream of all 17-clue sudoku puzzles.
     */
    public static Stream<PuzzleEntry> allSudoku17AsStream() {
        Scanner scanner = new Scanner(
            PuzzleEntry.class.getResourceAsStream(String.format(
                "/%s/%s",
                RESOURCES_DIR,
                PUZZLES_17_RESOURCE
            ))
        );
        return Stream.generate(() -> {
            try {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (Sudoku.isValidStr(line)) {
                        return new PuzzleEntry(line);
                    } else {
                        System.err.println("failed to create sudoku from file line\n" + line);
                        return null;
                    }
                } else {
                    scanner.close();
                    return null;
                }
            } catch (IllegalStateException ex) {
                // The scanner may be closed
                return null;
            }
        });
    }

    /**
     * Transforms the sudoku17 puzzles from resources into a CSV file.
     * This can be processed using multiple threads. The order of the
     * lines from the puzzle file will be preserved in the output.
     * @param outFilePath Name of the file to generate.
     * @param numThreads Number of threads to use.
     */
    public static void buildCSV(String outFilePath, int numThreads) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            numThreads, numThreads,
            1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>()
        );

        Queue<Future<String>> entries = new LinkedList<>();

        try (
            InputStream sudoku17inStream = puzzles17Stream();
            Scanner sudoku17Scanner = new Scanner(sudoku17inStream);
        ) {
            while (sudoku17Scanner.hasNextLine()) {
                String line = sudoku17Scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                entries.offer(pool.submit(() -> new PuzzleEntry(line).toCsv()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        pool.shutdown();

        try (
            PrintWriter fOut = new PrintWriter(outFilePath);
        ) {
            int n = 0;
            while (!entries.isEmpty()) {
                Future<String> entry = entries.poll();
                String entryStr = entry.get();
                System.out.printf("[%d] %s\n", n, entryStr);
                fOut.println(entryStr);
                n++;
            }
        } catch (FileNotFoundException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            pool.close();
        }
    }
}
