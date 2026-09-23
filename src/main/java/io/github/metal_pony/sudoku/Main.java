package io.github.metal_pony.sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.metal_pony.sudoku.Constants.*;
import io.github.metal_pony.sudoku.util.Counting;

/**
 * [EXPERIMENTAL] Contains some utilities leftover from removing the CLI driver code.
 * None of this should be consumed as part of the sudoku library.
 * This will be removed.
 */
public class Main {
  private Main() {}

  static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
  static final String RESOURCES_DIR = "resources";

  static void out(Object x) { System.out.println(x); }
  static void outf(String format, Object...args) { System.out.printf(format, args); }

  static long timeMs() { return System.currentTimeMillis(); }

  static void sleep(long timeMs) {
    try {
      Thread.sleep(timeMs);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  static InputStream resourceStream(String name) {
    return Main.class.getResourceAsStream(String.format("/%s/%s", RESOURCES_DIR, name));
  }

  static void readAllLines(InputStream inStream, List<String> lines) {
    try (
      BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
    ) {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line.trim());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static void repeatThreadedAndBlock(Runnable runnable, int times, int threads) {
    ThreadPoolExecutor pool = new ThreadPoolExecutor(
      threads, threads,
      1L, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>()
    );
    pool.prestartAllCoreThreads();
    for (int n = 0; n < times; n++) pool.submit(runnable);
    pool.shutdown();
    try {
      pool.awaitTermination(1L, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      pool.close();
      // out("thread pool closed");
    }
  }

  static void runBatchAndBlock(List<Runnable> batch, int threads) {
    ThreadPoolExecutor pool = new ThreadPoolExecutor(
      threads, threads,
      1L, TimeUnit.SECONDS,
      new LinkedBlockingQueue<>()
    );
    pool.prestartAllCoreThreads();
    for (Runnable work : batch) pool.submit(work);
    pool.shutdown();
    try {
      pool.awaitTermination(1L, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      pool.close();
      // out("thread pool closed");
    }
  }

  private static int inBounds(int value, int min, int max) {
    return Math.max(min, Math.min(value, max));
  }

  private static <K,V> void defaultInMap(Map<K,V> map, K key, V defaultValue) {
    V value = map.get(key);
    if (value == null) {
      map.put(key, defaultValue);
    }
  }

  static long timeCpuExecution(Runnable runnable, int n) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    long start = bean.getCurrentThreadCpuTime();
    for (int t = 0; t < n; t++) {
      runnable.run();
    }
    long end = bean.getCurrentThreadCpuTime();
    return TimeUnit.NANOSECONDS.toMillis(end - start);
  }

  static long timeCpuExecution(Runnable runnable) {
    return timeCpuExecution(runnable, 1);
  }

  /**
   * Checks whether the given puzzle, assumed to be a palindrome, is reducible. i.e., if two
   * complementing cells can be cleared while maintaining a single solution for the puzzle.
   * Palindrome puzzles are 'prime' if they are not reducible.
   *
   * @param p
   * @return
   */
  private static boolean isPrimePali(Sudoku p) {
    int flag = p.solutionsFlag();
    if (flag != 1) return false;

    Sudoku test = new Sudoku(p);

    // Quick failure case:
    // A digit exists in the center of the puzzle,
    // which can be removed without affecting solvability,
    // then the palindrome is not "prime".
    int prev1 = test.getDigit(40);
    if (prev1 > 0) {
      test.clearCell(40);
      flag = test.solutionsFlag();
      if (flag == 1) {
        // verboseOutf("❌ %s\n", test.toString());
        return false;
      }
      test.setDigit(40, prev1);
    }

    int prev2 = 0;
    for (int ci = 0; ci < SPACES / 2; ci++) {
      prev1 = test.getDigit(ci);
      if (prev1 > 0) {
        prev2 = test.getDigit(SPACES - ci - 1);
        test.clearCell(ci);
        test.clearCell(SPACES - ci - 1);
        flag = test.solutionsFlag();
        if (flag == 1) {
          // verboseOutf("❌ %s\n", test.toString());
          return false;
        }
        test.setDigit(ci, prev1);
        test.setDigit(SPACES - ci - 1, prev2);
      }
    }

    return true;
  }

  private static void wip_1() {
    final int threads = 8;
    final int clues = 27; //args.cluesOrDefault(24);
    // if (clues < MIN_CLUES || clues > SPACES) {
    //   throw new IllegalArgumentException("clues out of range");
    // }

    // Generate a random palindrome SudokuMask that satisfies the prime sieve.

    long paliGenTimeStart = System.currentTimeMillis();
    SudokuMask paliMask = new SudokuMask();
    final int k = clues / 2;
    final long nck = Counting.NChooseKLong(40, k);
    long r = 0L;
    do {
      r = ThreadLocalRandom.current().nextLong(nck);
      paliMask.palindrome(clues, r);
    } while (!SudokuMask.satisfiesPrimeSieve(paliMask));
    long paliGenTimeEnd = System.currentTimeMillis();
    System.out.printf(
      """
      Generated random palindrome mask.
      palindrome(clues = %d, r = %d)
      (%d ms)
      """,
      clues, r, (paliGenTimeEnd - paliGenTimeStart)
    );
    System.out.println(paliMask.toStringDots());
    System.out.println(paliMask.toMedString());

    System.out.println("Searching for valid puzzles...");
    long startTime = System.currentTimeMillis();
    AtomicLong count = new AtomicLong();
    // Sudoku.searchForPuzzlesAsync(paliMask, (puzzle) -> {
    Sudoku.searchForPuzzles(paliMask, (puzzle) -> {
      System.out.printf("[%d] %s\n", count.incrementAndGet(), puzzle.toString());
      return true;
    });
    long endTime = System.currentTimeMillis();
    System.out.printf(
      "Found %d puzzles of palindrome(clues = %d, r = %d). (%d ms)\n",
      count.get(), clues, r, (endTime - startTime)
    );

    // System.out.println(paliMask.toMedString());

    // Sudoku.searchForPuzzles(paliMask, (puzzle) -> {
    //   System.out.println(puzzle.toString());
    //   return true;
    // });
  }

  /**
   * Takes a list of Sudokus, converts them to MedString representation,
   * and organizes them into a grid with `numPerLine` sudokus per row.
   * A delimiter is used to separate the sudokus.
   *
   * @param sudokus List of Sudoku sudokus to format.
   * @param numPerLine Maximum number of sudokus per output line.
   * @param delim Delimiter between sudokus.
   * @return List of formatted lines of sudokus.
   */
  public static List<String> sudokuLines(List<Sudoku> sudokus, int numPerLine, String delim) {
    final int len = sudokus.size();
    List<String> lines = new ArrayList<>();
    if (len == 0) return lines;

    final int linesPerSudoku = sudokus.get(0).toMedString().split("\n").length;

    String[][] sudokuStrs = new String[numPerLine][];
    for (int j = 0; j < numPerLine; j++) {
      sudokuStrs[j] = new String[linesPerSudoku];
    }

    for (int i = 0; i < len; i+=numPerLine) {
      for (int j = 0; j < numPerLine; j++) {
        Arrays.fill(sudokuStrs[j], "");
        if (i + j < len) {
          sudokuStrs[j] = sudokus.get(i + j).toMedString().split("\n");
        }
      }

      for (int line = 0; line < linesPerSudoku; line++) {
        String[] sudokuLines = new String[numPerLine];
        for (int j = 0; j < numPerLine; j++) {
          sudokuLines[j] = sudokuStrs[j][line];
        }

        lines.add(String.join(delim, sudokuLines).trim());
      }
      lines.add("");
    }
    return lines;
  }
}
