package io.github.metal_pony.sudoku;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.github.metal_pony.sudoku.Constants.*;

/**
 * Data structure associated with a given sudoku solution.
 * Maintains a collection of unavoidable sets of the solution, the 'Items',
 * as SudokuMasks.
 * Provides methods to filter through the items, check if a mask satisfies
 * all sieve items, and search for / seed more items.
 *
 * The Sieve can be used to aid puzzle searching by providing a fail-fast
 * check against a given puzzle mask.
 */
public class SudokuSieve {
    private static class ItemGroup {
        final int order;
        final TreeSet<SudokuMask> items;
        ItemGroup(int order) {
            this.order = order;
            this.items = new TreeSet<>();
        }
    }

    private final Sudoku _config;
    private final int[] board;
    private int size;
    private final ArrayList<ItemGroup> _itemGroupsByBitCount;
    private int[] reductionMatrix;

    /**
     * Creates a new Sieve for the given sudoku configuration.
     * @param config Full and valid sudoku.
     * @throws IllegalArgumentException If the given sudoku is not full and valid.
     */
    public SudokuSieve(Sudoku config) {
        if (!config.isSolved()) {
            throw new IllegalArgumentException("could not create sieve for malformed grid");
        }

        this.board = config.toArray();
        this._config = new Sudoku(this.board);
        this._itemGroupsByBitCount = new ArrayList<>(SPACES + 1);
        for (int n = 0; n <= SPACES; n++) {
            this._itemGroupsByBitCount.add(n, new ItemGroup(n));
        }
        this.reductionMatrix = new int[SPACES];
    }

    /**
     * Creates a new Sieve for the given sudoku board array.
     * @param configBoard Full and valid sudoku board.
     * @throws IllegalArgumentException If the given sudoku board is not full and valid.
     */
    public SudokuSieve(int[] configBoard) {
        this(new Sudoku(configBoard));
    }

    /**
     * Gets the number of items in this Sieve.
     * @return Number of items in the sieve.
     */
    public int size() {
        return size;
    }

    /**
     * Gets whether this Sieve contains no items.
     * @return Whether the sieve contains no items.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Gets the solution associated with this Sieve.
     * @return A new Sudoku instance containing the solution.
     */
    public Sudoku config() {
        return new Sudoku(_config);
    }

    /**
     * Creates and returns a new Set populated with this Sieve's items.
     * @return A new Set containing copies of this sieve's items.
     */
    public Set<SudokuMask> items() {
        return items(new HashSet<>(size));
    }

    /**
     * Populates a Set with copies of this sieve's items.
     * This method is synchronized by the instance lock shared
     * among all other mutating methods.
     * @param set A Set to copy items into.
     * @return The given set, for convenience.
     */
    public synchronized Set<SudokuMask> items(Set<SudokuMask> set) {
        for (ItemGroup group : _itemGroupsByBitCount) {
            for (SudokuMask item : group.items) {
                set.add(new SudokuMask(item));
            }
        }
        return set;
    }

    /**
     * Populates a list with copies of this sieve's items.
     * This method is synchronized by the instance lock shared
     * among all other mutating methods.
     * @param list A List to copy items into.
     * @return The given set, for convenience.
     */
    public synchronized List<SudokuMask> items(List<SudokuMask> list) {
        for (ItemGroup group : _itemGroupsByBitCount) {
            for (SudokuMask item : group.items) {
                list.add(new SudokuMask(item));
            }
        }
        return list;
    }

    /**
     * Maps sudoku cell indices to the number of times the cell appears among sieve items.
     * This method is synchronized by the instance lock shared
     * among all other mutating methods.
     * @return A new array containing the number of times each cell is included
     * among sieve items.
     */
    public synchronized int[] reductionMatrix() {
        return reductionMatrix(new int[SPACES]);
    }

    /**
     * Maps sudoku cell indices to the number of times the cell appears among sieve items.
     * This method is synchronized by the instance lock shared
     * among all other mutating methods.
     * @param arr Array container to copy into; must be length 81.
     * @return The given array, for convenience.
     */
    public synchronized int[] reductionMatrix(int[] arr) {
        if (arr.length != reductionMatrix.length) {
            throw new IllegalArgumentException("arr improper length");
        }
        System.arraycopy(reductionMatrix, 0, arr, 0, reductionMatrix.length);
        return arr;
    }

    /**
     * Gets the first item in the sieve. Items are organized primarily by
     * their number of bits set, ascending, so the first items should have
     * the least number of bits set.
     * @return The first item in the sieve; null if the sieve is empty.
     */
    public synchronized SudokuMask first() {
        for (ItemGroup group : _itemGroupsByBitCount) {
            if (group.items.size() > 0) {
                return new SudokuMask(group.items.first());
            }
        }
        return null;
    }

    /**
     * Finds the first sieve item that is not satisfied by the given mask.
     * @param mask SudokuMask to compare against the sieve items.
     * @return First Sieve item that contains no overlapping bits with mask.
     */
    public synchronized SudokuMask firstNotOverlapping(SudokuMask mask) {
        for (ItemGroup group : _itemGroupsByBitCount) {
            for (SudokuMask item : group.items) {
                if (!mask.intersects(item)) {
                    return new SudokuMask(item);
                }
            }
        }
        return null;
    }

    /**
     * Searches for and returns the first item in the sieve that satifies the given predicate.
     * @param predicate Takes a SudokuMask and returns a boolean.
     * @return The found item; null if no items satisfy the predicate function.
     */
    public synchronized SudokuMask find(Function<SudokuMask,Boolean> predicate) {
        for (ItemGroup group : _itemGroupsByBitCount) {
            if (group.items.isEmpty()) continue;
            for (SudokuMask item : group.items) {
                SudokuMask _item = new SudokuMask(item);
                if (predicate.apply(_item)) {
                    return _item;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the group associated with the given bitCount.
     * @param bitCount Number of bits set in masks associated with the ItemGroup.
     * @return ItemGroup associated with the bitCount.
     */
    ItemGroup groupForBitCount(int bitCount) {
        return _itemGroupsByBitCount.get(bitCount);
    }

    /**
     * Gets a list of items associated with the given bitCount.
     * @param bitCount Number of bits set in masks associated with the Sieve items.
     * @return A new List containing copies of the sieve items associated with the number of clues.
     * @throws IllegalArgumentException If numClues is out of range.
     */
    public List<SudokuMask> getItemByNumClues(int bitCount) {
        if (bitCount < 0 || bitCount > SPACES) {
            throw new IllegalArgumentException("Invalid number of clues");
        }
        List<SudokuMask> results = new ArrayList<>();
        synchronized (this) {
            for (SudokuMask item : groupForBitCount(bitCount).items) {
                results.add(new SudokuMask(item));
            }
        }
        return results;
    }

    /**
     * Seeds this Sieve using the given collection of SudokuMasks.
     * Each mask will be applied to the Sieve's solution, creating a puzzle.
     * Each puzzle will then be solved, and unique solutions collected.
     * Solutions different from this Sieve's will be used to derive new Items.
     * @param masks Collection of SudokuMask that will serve as filters applied
     * to the solution.
     */
    public void seed(Collection<SudokuMask> masks) {
        masks.forEach(mask -> addFromFilter(mask));
    }

    /**
     * Generates a List of SudokuMask. Each mask is a puzzle filter for a combination
     * of sudoku board areas (rows, columns, or regions). Level determines how
     * many areas are used to build the filter masks. For example, when
     * <code>level = 2</code>, the generated List will contain all combinations
     * of 2 rows, combos of 2 columns, and combos of 2 regions.
     *
     * Note: This does not mix area types together within the same masks.
     *
     * @param level (Bounds: [1, 8]) Number of areas in each generated mask.
     * @return List of SudokuMask.
     */
    // TODO These can be precompiled on class load
    public List<SudokuMask> areaCombos(int level) {
        if (level < 1 || level > DIGITS - 1) throw new IllegalArgumentException("Invalid level");

        List<SudokuMask> combos = new ArrayList<>();
        for (int combo : DIGIT_COMBOS_MAP[level]) {
            SudokuMask rowMask = new SudokuMask();
            SudokuMask colMask = new SudokuMask();
            SudokuMask regionMask = new SudokuMask();

            for (int ci = 0; ci < SPACES; ci++) {
                if ((combo & (1 << Sudoku.cellRow(ci))) > 0) {
                    rowMask.setBit(ci);
                }
                if ((combo & (1 << Sudoku.cellCol(ci))) > 0) {
                    colMask.setBit(ci);
                }
                if ((combo & (1 << Sudoku.cellRegion(ci))) > 0) {
                    regionMask.setBit(ci);
                }
            }

            combos.add(rowMask);
            combos.add(colMask);
            combos.add(regionMask);
        }

        return combos;
    }

    /**
     * Generates a List of SudokuMask. Each mask is a filter for the Solution to remove
     * a combination of digits. Level determines how many digits are remove for each mask.
     * For example, when <code>level = 2</code>, the generated List will contain a mask
     * for filtering out each pair of digits.
     * @param level (Bounds: [1, 8]) Number of digits in each generated mask.
     * @return List of SudokuMask.
     */
    public List<SudokuMask> digitCombos(int level) {
        if (level < 1 || level > DIGITS - 1) throw new IllegalArgumentException("Invalid level");

        List<SudokuMask> combos = new ArrayList<>();
        int[] board = _config.toArray();
        for (int combo : DIGIT_COMBOS_MAP[level]) {
            SudokuMask digMask = new SudokuMask();

            for (int ci = 0; ci < SPACES; ci++) {
                if ((combo & (1 << (board[ci]) - 1)) > 0) {
                    digMask.setBit(ci);
                }
            }

            combos.add(digMask);
        }

        return combos;
    }

    /**
     * Generates both digit and area filter masks associated with the given level.
     * @param level (Bounds: [1, 8]) Number of digits in each generated mask.
     * @return A new List containing all the generated area and digit masks.
     */
    public List<SudokuMask> fullPrintCombos(int level) {
        List<SudokuMask> combos = new ArrayList<>();
        combos.addAll(digitCombos(level));
        combos.addAll(areaCombos(level));
        return combos;
    }

    /**
     * Seeds this Sieve using the given collection of SudokuMasks.
     * Each mask will be applied to the Sieve's solution, creating a puzzle.
     * Each puzzle will then be solved, and unique solutions collected.
     * Solutions different from this Sieve's will be used to derive new Items.
     * @param masks Collection of SudokuMask that will serve as filters applied
     * to the solution.
     * @param numThreads Number of threads to split the work.
     */
    public void seedThreaded(Collection<SudokuMask> masks, int numThreads) {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
            numThreads, numThreads,
            1L, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>()
        );

        masks.forEach(mask -> pool.submit(() -> {
            addFromFilter(mask);
        }));

        pool.shutdown();
        try {
            pool.awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Seeds this Sieve using the given collection of SudokuMasks.
     * Each mask will be applied to the Sieve's solution, creating a puzzle.
     * Each puzzle will then be solved, and unique solutions collected.
     * Solutions different from this Sieve's will be used to derive new Items.
     *
     * Note: Uses the maximum number of threads.
     *
     * @param masks Collection of SudokuMask that will serve as filters applied
     * to the solution.
     */
    public void seedThreaded(Collection<SudokuMask> masks) {
        seedThreaded(masks, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Checks whether the given SudokuMask is an unavoidable set.
     * Masks are Unavoidable Sets when the puzzle they create is (1) not reducible
     * by any Sudoku technique, and (2) each empty cell has at least 2 candidates
     * that when used, make the puzzle a valid sudoku.
     * @param mask Mask representing an unavoidable set.
     * @return True if the mask is an unavoidable set; otherwise false.
     */
    public boolean validate(SudokuMask mask) {
        Sudoku p = _config.filter(new SudokuMask(mask).flip());
        int emptyCells = p.numEmptyCells();
        p.reduce();
        return (
            p.numEmptyCells() == emptyCells &&
            p.doBranchesSolveUniquely()
        );
    }

    /**
     * Checks whether the given SudokuMask is derivative of an existing unavoidable set
     * already in this sieve.
     * @param mask Mask to check.
     * @return True if the mask is covered by an unavoidable set mask in this sieve; otherwise false.
     * Empty masks (0 bitCount are always TRUE).
     */
    public synchronized boolean isDerivative(SudokuMask mask) {
        if (mask.bitCount() == 0) return true;

        for (ItemGroup group : _itemGroupsByBitCount) {
            if (group.items.size() > 0) {
                for (SudokuMask item : group.items) {
                    if (mask.hasBitsSet(item)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Adds the given item to the reduction matrix.
     * @param item Mask of the item to add.
     */
    synchronized void addToReductionMatrix(SudokuMask item) {
        for (int i = 0; i < SPACES; i++) {
            if (item.testBit(i)) {
                reductionMatrix[i]++;
            }
        }
    }

    /**
     * Subtracts the given item from the reduction matrix.
     * @param item Mask of the item to subtract.
     */
    synchronized void subtractFromReductionMatrix(SudokuMask item) {
        for (int i = 0; i < SPACES; i++) {
            if (item.testBit(i)) {
                reductionMatrix[i]--;
            }
        }
    }

    /**
     * Adds an item directly into the sieve without validating.
     * @param item Item to add.
     * @return True if the item was added; otherwise false if the item already exists.
     */
    public synchronized boolean rawAdd(SudokuMask item) {
        if (!groupForBitCount(item.bitCount()).items.contains(item)) {
            groupForBitCount(item.bitCount()).items.add(item);
            size++;
            addToReductionMatrix(item);
            return true;
        }
        return false;
    }

    /**
     * Attempts to add the given item to this sieve.
     * @param item Item to add.
     * @return True if the item was added; otherwise false if the item has not bits set;
     * if the item is derivative of an existing item;
     * if the item is not an unavoidable set;
     * if the item was previously added.
     */
    public synchronized boolean add(SudokuMask item) {
        if (
            item.bitCount() > 0 &&
            !isDerivative(item) &&
            validate(item)
        ) {
            rawAdd(item);
            return true;
        }
        return false;
    }

    /**
     * Filters the sieve's grid with the given mask, and for each solution,
     * adds the diff as an item if it validates as an unavoidable set.
     *
     * Note: The bits set in the given mask indicate which cells will be removed.
     *
     * @param mask Used to filter the sudoku grid associated with this sieve.
     * @return Number of Items added to this Sieve.
     */
    public int addFromFilter(SudokuMask mask) {
        AtomicInteger numAdded = new AtomicInteger();
        SudokuMask _mask = new SudokuMask(mask);
        _config.filter(_mask.flip()).searchForSolutions(solution -> {
            SudokuMask diff = _config.diffMask(solution);
            if (
                diff.bitCount() > 0 &&
                !isDerivative(diff) &&
                validate(diff)
            ) {
                rawAdd(diff);
                numAdded.incrementAndGet();
            }
            return true;
        });
        return numAdded.get();
    }

    /**
     * Filters the sieve's grid with the given mask, and for each solution,
     * adds the diff as an item if it validates as an unavoidable set.
     * @param mask Used to filter the sudoku grid associated with this sieve.
     * @return Number of Items added to this Sieve.
     */
    public int addFromPuzzleMask(SudokuMask mask) {
        AtomicInteger numAdded = new AtomicInteger();
        _config.filter(mask).searchForSolutions(solution -> {
            SudokuMask diff = _config.diffMask(solution);
            if (
                diff.bitCount() > 0 &&
                !isDerivative(diff) &&
                validate(diff)
            ) {
                rawAdd(diff);
                numAdded.incrementAndGet();
            }
            return true;
        });
        return numAdded.get();
    }

    /**
     * Removes the specific item if it exists in the sieve.
     * @param item Item to remove.
     * @return True if the item was found and removed; otherwise false.
     */
    public synchronized boolean remove(SudokuMask item) {
        if (groupForBitCount(item.bitCount()).items.remove(item)) {
            size--;
            subtractFromReductionMatrix(item);
            return true;
        }
        return false;
    }

    /**
     * Removes and returns all items that include the given cell index.
     * Items removed are automatically deducted from the reduction matrix.
     * @param cellIndex Cell index.
     * @return A list containing all items that were removed.
     */
    public synchronized List<SudokuMask> removeOverlapping(int cellIndex) {
        return removeOverlapping(cellIndex, new ArrayList<>());
    }

    /**
     * Removes and returns all items that include the given cell index.
     * Items removed are automatically deducted from the reduction matrix.
     * @param cellIndex Cell index.
     * @param removedList A list to add the removed items to.
     * @return The given list for convenience.
     */
    public synchronized List<SudokuMask> removeOverlapping(int cellIndex, List<SudokuMask> removedList) {
        SudokuMask mask = new SudokuMask();
        mask.setBit(cellIndex);
        return removeOverlapping(mask, removedList);
    }

    /**
     * Removes and returns all items that contain overlapping bits with the given mask.
     * Items removed are automatically deducted from the reduction matrix.
     * @param mask SudokuMask compared against Items.
     * @param removedList A list to add the removed items to.
     * @return The given list for convenience.
     */
    public synchronized List<SudokuMask> removeOverlapping(SudokuMask mask, List<SudokuMask> removedList) {
        for (ItemGroup group : _itemGroupsByBitCount) {
            group.items.removeIf((i) -> {
                // boolean shouldRemove = i.testBit(Sudoku.SPACES - 1 - cellIndex);
                boolean shouldRemove = i.intersects(mask);
                if (shouldRemove) {
                    removedList.add(i);
                    size--;
                    subtractFromReductionMatrix(i);
                }
                return shouldRemove;
            });
        }
        return removedList;
    }

    /**
     * Checks whether the given mask intersects with all sieve items.
     * @param puzzleMask SudokuMask to check against the Items.
     * @return True if the mask contains at least one bit intersecting with each sieve item.
     */
    public synchronized boolean doesMaskSatisfy(SudokuMask puzzleMask) {
        for (ItemGroup group : _itemGroupsByBitCount) {
            for (SudokuMask item : group.items) {
                // TODO There's no way this is correct, right?
                if (!item.intersects(puzzleMask)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public synchronized String toString() {
        StringBuilder strb = new StringBuilder();
        strb.append("{\n");

        for (ItemGroup group : _itemGroupsByBitCount) {
            if (group.items.size() > 0) {
                strb.append(String.format("  [%d]: [\n", group.order));
                for (SudokuMask item : group.items) {
                    strb.append(String.format("    %s\n", _config.filter(item).toString()));
                }
                strb.append("  ],\n");
            }
        }

        strb.append("}");
        return strb.toString();
    }

    /**
     * Creates a hash string of this Sieve, based on the combined bitCounts of
     * its Items.
     * @param isLvl2 Whether the sieve is seeded to level 2. This omits Items
     * with odd bitCounts from the hash, as a Sieve at level 2 shouldn't contain them.
     * @return Generated string hash.
     */
    public String hash(boolean isLvl2) {
        StringBuilder strb = new StringBuilder();
        strb.append(size());
        strb.append("=");

        // An item (unavoidable set) includes a minimum of 4 cells
        for (int m = 4, count = 0, max = size(); count < max; m++) {
            ItemGroup group = _itemGroupsByBitCount.get(m);
            int n = group.items.size();
            count += n;

            // In level 2, there can be no UAs using an odd number of cells,
            // because each cell must have at least one complement.
            // Skipping odd numbers avoids "::", keeping the fingerprint short.
            if (isLvl2 && (m & 1) == 1) continue;

            if (n > 0) strb.append(Integer.toString(n, 16));
            if (count < max) strb.append(':');
        }

        return strb.toString();
    }
}
