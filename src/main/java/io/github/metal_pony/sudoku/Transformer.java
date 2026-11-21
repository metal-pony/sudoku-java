package io.github.metal_pony.sudoku;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

import io.github.metal_pony.sudoku.util.ArraysUtil;
import io.github.metal_pony.sudoku.util.Counting;

/**
 * A Transformer encapsulates a series of transforms to be applied to
 * Sudoku instances. It solves the usecase of transforming multiple
 * sudoku instances with the same steps.
 * <br>Steps are always performed in the following order:
 * <ul>
 * <li>Swap rows within bands</li>
 * <li>Swap columns within stacks</li>
 * <li>Swap bands</li>
 * <li>Swap stacks</li>
 * <li>Swap digits</li>
 * <li>Rotations</li>
 * </ul>
 * <br>Includes a method for randomizing transforms.
 */
public class Transformer {
    private static final int FACT9 = Counting.factorial(9).intValueExact();

    /**
     * Creates a new Transformer with a set of random transforms,
     * applies it to the given Sudoku, then returns the Transformer.
     * @param sudoku Sudoku to apply transforms to.
     * @return The created Transformer.
     */
    public static Transformer random(Sudoku sudoku) {
        Transformer transformer = new Transformer().random();
        transformer.apply(sudoku);
        return transformer;
    }

    private int rotation;
    private int bands;
    private int stacks;
    private int[] bandRows;
    private int[] stackCols;
    private int digits;

    /**
     * Creates a new Transformer with no initial transform steps.
     */
    public Transformer() {
        this.rotation = 0;
        this.bands = 0;
        this.stacks = 0;
        this.bandRows = new int[3];
        this.stackCols = new int[3];
        this.digits = 0;
    }

    /**
     * Sets transform steps at random.
     * @return This Transformer for convenience.
     */
    public Transformer random() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        rotation = rand.nextInt(4);
        bands = rand.nextInt(6);
        stacks = rand.nextInt(6);
        for (int i = 0; i < bandRows.length; i++) {
            bandRows[i] = rand.nextInt(6);
            stackCols[i] = rand.nextInt(6);
        }
        digits = rand.nextInt(FACT9);

        return this;
    }

    /**
     * Gets the order in which bands will be transformed.
     * @return Array of indices indicating how bands will be reordered.
     */
    public int[] getBandOrder() {
        return Counting.permutation(3, BigInteger.valueOf(bands));
    }

    /**
     * Gets the order in which stacks will be transformed.
     * @return Array of indices indicating how stacks will be reordered.
     */
    public int[] getStackOrder() {
        return Counting.permutation(3, BigInteger.valueOf(stacks));
    }

    /**
     * Gets the order in which rows within the given band will be transformed.
     * @param band [0..2] Band index.
     * @return Array of indices indicating how rows will be reordered.
     */
    public int[] getBandRowOrder(int band) {
        return Counting.permutation(3, BigInteger.valueOf(bandRows[band]));
    }

    /**
     * Gets the order in which columns within the given stack will be transformed.
     * @param stack [0..2] Stack index.
     * @return Array of indices indicating how columns will be reordered.
     */
    public int[] getStackColOrder(int stack) {
        return Counting.permutation(3, BigInteger.valueOf(stackCols[stack]));
    }

    /**
     * Gets the number of (90-degree clockwise) rotations that will be performed.
     * @return Number of rotations.
     */
    public int getRotation() {
        return rotation;
    }

    /**
     * Gets the order in which digits will be swapped.
     * @return Array indicating how digits will be reordered.
     */
    public int[] getDigitOrder() {
        int[] order = Counting.permutation(9, BigInteger.valueOf(digits));
        for (int i = 0; i < order.length; i++) order[i]++;
        return order;
    }

    /**
     * Extrapolates the permutation(n, r), swaps the values a and b
     * within the permutation array, then computes and returns the new
     * permutation r value (as int).
     */
    private static int swapInPerm(int n, int r, int a, int b) {
        int[] perm = Counting.permutation(n, BigInteger.valueOf(r));
        int ai = ArraysUtil.indexOf(perm, a);
        int bi = ArraysUtil.indexOf(perm, b);
        perm[ai] = b;
        perm[bi] = a;
        return Counting.permutationToR(perm).intValueExact();
    }

    /**
     * Add a band swap. Compounds on existing band swaps.
     * @param a [0..2] Band index.
     * @param b [0..2] Band index.
     * @return This Transformer for convenience.
     */
    public Transformer swapBands(int a, int b) {
        bands = swapInPerm(3, bands, a, b);
        return this;
    }

    /**
     * Add a stack swap. Compounds on existing stack swaps.
     * @param a [0..2] Stack index.
     * @param b [0..2] Stack index.
     * @return This Transformer for convenience.
     */
    public Transformer swapStacks(int a, int b) {
        stacks = swapInPerm(3, stacks, a, b);
        return this;
    }

    /**
     * Add a band-row swap. Compounds on existing band-row swaps.
     * @param band [0..2] Band index.
     * @param a [0..2] Row index.
     * @param b [0..2] Row index.
     * @return This Transformer for convenience.
     */
    public Transformer swapBandRows(int band, int a, int b) {
        bandRows[band] = swapInPerm(3, bandRows[band], a, b);
        return this;
    }

    /**
     * Add a stack-col swap. Compounds on existing stack-col swaps.
     * @param stack [0..2] Stack index.
     * @param a [0..2] Column index.
     * @param b [0..2] Column index.
     * @return This Transformer for convenience.
     */
    public Transformer swapStackCols(int stack, int a, int b) {
        stackCols[stack] = swapInPerm(3, stackCols[stack], a, b);
        return this;
    }

    /**
     * Add a number of (90-degree clockwise) rotations. Compounds on existing rotations.
     * @param n Number of rotations.
     * @return This Transformer for convenience.
     */
    public Transformer rotate(int n) {
        rotation = (((rotation + n) % 4) + 4) % 4;
        return this;
    }

    /**
     * Add a (90-degree clockwise) rotation. Compounds on existing rotations.
     * @return This Transformer for convenience.
     */
    public Transformer rotate() {
        return rotate(1);
    }

    /**
     * Add a digit swap. Compounds on existing digit swaps.
     * @param a [1..9] Digit to swap.
     * @param b [1..9] Digit to swap.
     * @return This Transformer for convenience.
     */
    public Transformer swapDigits(int a, int b) {
        digits = swapInPerm(9, digits, a - 1, b - 1);
        return this;
    }

    /**
     * Add several digit swaps. Swaps are performed by iteration over the array,
     * where the digit elements are swapped with their corresponding indices (+1).
     * <br>This does NOT compound with previously defined digit swaps.
     * @param order [1..9] Array indicating how digits should be reordered.
     * @return This Transformer for convenience.
     */
    public Transformer swapDigits(int[] order) {
        Objects.requireNonNull(order);
        if (order.length != 9) {
            throw new IllegalArgumentException("order must contain digits [1 .. 9]");
        }
        int bs = 0;
        for (int d : order) {
            if (d < 1 || d > 9) {
                throw new IllegalArgumentException("order must contain digits [1 .. 9]");
            }
            int mask = 1 << (d - 1);
            if ((bs & mask) > 0) {
                throw new IllegalArgumentException("order contains duplicate digit");
            }
            bs |= mask;
        }
        // Implied given checks above that this should never be triggered.
        if (bs != 0x1ff) {
            throw new IllegalArgumentException("order must contain all digits [1 .. 9]");
        }
        // order is verified to contain digits [1 .. 9],
        // but we need to rank the permutation of [0 .. 8]
        // so subtract 1 from each element.
        int[] copy = ArraysUtil.copy(order);
        for (int i = 0; i < copy.length; i++) copy[i]--;

        digits = Counting.permutationToR(copy).intValueExact();
        return this;
    }

    private static void apply(int n, int r, BiConsumer<Integer,Integer> func) {
        int[] perm = Counting.permutation(n, BigInteger.valueOf(r));
        for (int i = 0; i < n; i++) {
            if (perm[i] == i) continue;
            int j = ArraysUtil.indexOf(perm, i);
            func.accept(i, j);
            ArraysUtil.swap(perm, i, j);
        }
    }

    /**
     * Applies this Transformer's steps to the given Sudoku.
     * <br>Steps are performed in the following order:
     * <ul>
     * <li>Swap rows within bands</li>
     * <li>Swap columns within stacks</li>
     * <li>Swap bands</li>
     * <li>Swap stacks</li>
     * <li>Swap digits</li>
     * <li>Rotations</li>
     * </ul>
     * <br>Afterwards, sudoku's constraints will be synchronized.
     * @param sudoku Sudoku to transform.
     * @return The given Sudoku for convenience.
     */
    public Sudoku apply(Sudoku sudoku) {
        // Apply band rows transforms
        for (int br = 0; br < 3; br++) {
            final int bandRow = br;
            apply(3, bandRows[br], (i, j) -> {
                System.out.printf("swapping band %d rows %d <-> %d\n", bandRow, i, j);
                sudoku.swapBandRows(bandRow, i, j);
            });
        }
        // Apply stack column transforms
        for (int sc = 0; sc < 3; sc++) {
            final int stackCol = sc;
            apply(3, stackCols[sc], (i, j) -> {
                System.out.printf("swapping stack %d cols %d <-> %d\n", stackCol, i, j);
                sudoku.swapStackCols(stackCol, i, j);
            });
        }
        // Apply bands transform
        apply(3, bands, (i, j) -> {
            System.out.printf("swapping bands %d <-> %d\n", i, j);
            sudoku.swapBands(i, j);
        });
        // Apply stacks transform
        apply(3, stacks, (i, j) -> {
            System.out.printf("swapping stacks %d <-> %d\n", i, j);
            sudoku.swapStacks(i, j);
        });
        // Apply digit-swapping
        apply(9, digits, (i, j) -> {
            System.out.printf("swapping digits %d <-> %d\n", i + 1, j + 1);
            sudoku.swapDigits(i + 1, j + 1);
        });
        // Apply rotation
        if (rotation > 0) System.out.printf("rotating %d\n", rotation);
        sudoku.rotate(rotation);

        // Transform side-effect: sudoku internal constraints are now out of sync.
        // Rebuild constraint values (needed for solving).
        sudoku.resetCandidatesAndValidity();

        return sudoku;
    }

    // TODO
    // Transform iterator
    // next()
    // previous()
    // Could also use builder pattern to enable iteration over specific transforms (e.g. iterate over all band and band row transform combinations).
}
