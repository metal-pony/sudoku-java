package io.github.metal_pony.sudoku;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

import io.github.metal_pony.sudoku.util.ArraysUtil;
import io.github.metal_pony.sudoku.util.Counting;

/**
 * A Transformer encapsulates a series of transforms to be applied to
 * Sudoku instances. It solves the usecase of transforming multiple
 * sudoku instances with the same steps.
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

    private static enum TransformType {
        BAND, STACK, BANDROW, STACKCOL, ROTATION, DIGIT
    }

    private static abstract class Transform {
        TransformType type;
        Transform(TransformType type) {
            this.type = type;
        }
        abstract void perform(Sudoku sudoku);
    }

    private static class BandSwap extends Transform {
        int i, j;
        BandSwap(int i, int j) {
            super(TransformType.BAND);
            this.i = i;
            this.j = j;
        }
        @Override
        void perform(Sudoku sudoku) { sudoku.swapBands(i, j); }
    }

    private static class StackSwap extends Transform {
        int i, j;
        StackSwap(int i, int j) {
            super(TransformType.STACK);
            this.i = i;
            this.j = j;
        }
        @Override
        void perform(Sudoku sudoku) { sudoku.swapStacks(i, j); }
    }

    private static class BandRowSwap extends Transform {
        int band, i, j;
        BandRowSwap(int band, int i, int j) {
            super(TransformType.BANDROW);
            this.band = band;
            this.i = i;
            this.j = j;
        }
        @Override
        void perform(Sudoku sudoku) { sudoku.swapBandRows(band, i, j); }
    }

    private static class StackColSwap extends Transform {
        int stack, i, j;
        StackColSwap(int stack, int i, int j) {
            super(TransformType.STACKCOL);
            this.stack = stack;
            this.i = i;
            this.j = j;
        }
        @Override
        void perform(Sudoku sudoku) { sudoku.swapStackCols(stack, i, j); }
    }

    private static class Rotation extends Transform {
        int n;
        Rotation(int n) {
            super(TransformType.ROTATION);
            this.n = n;
        }
        @Override
        void perform(Sudoku sudoku) { sudoku.rotate(n); }
    }

    private static class DigitSwap extends Transform {
        int i, j;
        DigitSwap(int i, int j) {
            super(TransformType.DIGIT);
            this.i = i;
            this.j = j;
        }
        @Override
        void perform(Sudoku sudoku) { sudoku.swapDigits(i, j); }
    }

    private List<Transform> steps;

    /**
     * Creates a new Transformer with no initial transform steps.
     */
    public Transformer() {
        this.steps = new ArrayList<>();
    }

    private static void extrapolate(int n, int r, BiConsumer<Integer,Integer> func) {
        int[] perm = Counting.permutation(n, BigInteger.valueOf(r));
        for (int i = 0; i < n; i++) {
            if (perm[i] == i) continue;
            int j = ArraysUtil.indexOf(perm, i);
            func.accept(i, j);
            ArraysUtil.swap(perm, i, j);
        }
    }

    /**
     * Sets transform steps at random.
     * @return This Transformer for convenience.
     */
    public Transformer random() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        extrapolate(3, rand.nextInt(6), (i, j) -> { swapBands(i, j); });
        extrapolate(3, rand.nextInt(6), (i, j) -> { swapStacks(i, j); });
        for (int index = 0; index < 3; index++) {
            final int _index = index;
            extrapolate(3, rand.nextInt(6), (i, j) -> { swapBandRows(_index, i, j); });
            extrapolate(3, rand.nextInt(6), (i, j) -> { swapStackCols(_index, i, j); });
        }
        rotate(rand.nextInt(4));
        extrapolate(9, rand.nextInt(FACT9), (i, j) -> { swapDigits(i, j); });

        return this;
    }

    /**
     * Add a band swap. Compounds on existing band swaps.
     * @param a [0..2] Band index.
     * @param b [0..2] Band index.
     * @return This Transformer for convenience.
     */
    public Transformer swapBands(int a, int b) {
        steps.add(new BandSwap(a, b));
        return this;
    }

    /**
     * Adds random band swaps.
     * May occasionally choose to add no swaps at all.
     * @return This Transformer for convenience.
     */
    public Transformer randomizeBands() {
        extrapolate(3, ThreadLocalRandom.current().nextInt(6), (i, j) -> { swapBands(i, j); });
        return this;
    }

    /**
     * Add a stack swap. Compounds on existing stack swaps.
     * @param a [0..2] Stack index.
     * @param b [0..2] Stack index.
     * @return This Transformer for convenience.
     */
    public Transformer swapStacks(int a, int b) {
        steps.add(new StackSwap(a, b));
        return this;
    }

    /**
     * Adds random stack swaps.
     * May occasionally choose to add no swaps at all.
     * @return This Transformer for convenience.
     */
    public Transformer randomizeStacks() {
        extrapolate(3, ThreadLocalRandom.current().nextInt(6), (i, j) -> { swapStacks(i, j); });
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
        steps.add(new BandRowSwap(band, a, b));
        return this;
    }

    /**
     * Adds random row swaps in the given band.
     * May occasionally choose to add no swaps at all.
     * @return This Transformer for convenience.
     */
    public Transformer randomizeBandRows(int band) {
        extrapolate(3, ThreadLocalRandom.current().nextInt(6), (i, j) -> { swapBandRows(band, i, j); });
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
        steps.add(new StackColSwap(stack, a, b));
        return this;
    }

    /**
     * Adds random column swaps in the given stack.
     * May occasionally choose to add no swaps at all.
     * @return This Transformer for convenience.
     */
    public Transformer randomizeStackCols(int stack) {
        extrapolate(3, ThreadLocalRandom.current().nextInt(6), (i, j) -> { swapStackCols(stack, i, j); });
        return this;
    }

    /**
     * Add a board rotation as a number of 90-degree clockwise turns.
     * If 0, no step will be added.
     * @param turns Number of rotations.
     * @return This Transformer for convenience.
     */
    public Transformer rotate(int turns) {
        if (turns != 0) steps.add(new Rotation(turns));
        return this;
    }

    /**
     * Add a (90-degree clockwise) rotation step.
     * @return This Transformer for convenience.
     */
    public Transformer rotate() {
        return rotate(1);
    }

    /**
     * Adds a board rotation step of 1, 2, or 3 turns.
     * @return This Transformer for convenience.
     */
    public Transformer rotateRandomly() {
        return rotate(1 + ThreadLocalRandom.current().nextInt(3));
    }

    /**
     * Add a digit swap. Compounds on existing digit swaps.
     * @param a [1..9] Digit to swap.
     * @param b [1..9] Digit to swap.
     * @return This Transformer for convenience.
     */
    public Transformer swapDigits(int a, int b) {
        steps.add(new DigitSwap(a, b));
        return this;
    }

    /**
     * Add several digit swaps. Swaps are performed by iteration over the array,
     * where the digit elements are swapped with their corresponding indices (+1).
     * Compounds on existing digit swaps.
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
        // order is verified to contain digits [1 .. 9]
        int[] copy = ArraysUtil.copy(order);
        for (int i = 0; i < copy.length; i++) {
            int iDigit = i + 1;
            if (copy[i] == iDigit) continue;
            int j = ArraysUtil.indexOf(copy, iDigit);
            swapDigits(iDigit, copy[i]);
            ArraysUtil.swap(copy, i, j);
        }

        return this;
    }

    /**
     * Seeds this Transformer with random digit swaps.
     * May (extremely rarely) choose to add no digit swap steps.
     * @return This Transformer for convenience.
     */
    public Transformer scrambleDigits() {
        extrapolate(9, ThreadLocalRandom.current().nextInt(FACT9), (i, j) -> { swapDigits(i, j); });
        return this;
    }

    /**
     * Applies this Transformer's steps to the given Sudoku.
     * <br>Afterwards, sudoku's constraints will be synchronized.
     * @param sudoku Sudoku to transform.
     * @return The given Sudoku for convenience.
     */
    public Sudoku apply(Sudoku sudoku) {
        steps.forEach(step -> step.perform(sudoku));

        // Transform side-effect: sudoku internal constraints are now out of sync.
        // Rebuild constraint values (needed for solving).
        sudoku.resetCandidatesAndValidity();

        return sudoku;
    }
}
