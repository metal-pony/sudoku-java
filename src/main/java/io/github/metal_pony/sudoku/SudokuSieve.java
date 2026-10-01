package io.github.metal_pony.sudoku;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static io.github.metal_pony.sudoku.Constants.*;

/**
 * Maintains a collection of unavoidable sets for a given sudoku solution.
 *
 * Can be useful in puzzle searching by providing a fail-fast
 * check against a given puzzle mask.
 */
public class SudokuSieve {
    /**
     * Determines if the given mask `m` has at least one overlapping bit set with each
     * of the elements in the given items collection.
     * @param items SudokuMasks to check the mask against.
     * @param m SudokuMask to check satisfies the given items.
     * @return True if `m` has bit(s) overlapping with each item.
     */
    public static boolean maskSatisfiesSieve(Collection<SudokuMask> items, SudokuMask m) {
        for (SudokuMask i : items) {
            if (!i.intersects(m)) {
                return false;
            }
        }
        return true;
    }

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
     * Generates a number of unavoidable sets using the digit-combos(2) technique.
     */
    public void seed() {
        seed(2);
    }

    /**
     * Generates a number of unavoidable sets using the digit-combos(level) technique.
     * @param level (2 to 4 recommended) Number of digits associated with the digitCombos
     * mask generation.
     */
    public void seed(int level) {
        digitCombos(level).forEach(this::searchForUAs);
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
            emptyCells > 0 &&
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
     * Attempts to add the given unavoidable set to this sieve.
     * A validation step checks it against current elements to block
     * duplicates and supersets (called derivatives) and then verifies
     * it as an unavoidable set.
     *
     * @param item Unavoidable set as a SudokuMask.
     * @return True if the item was verified as a non-duplicate unavoidable set
     * and added to the sieve; otherwise false.
     */
    public synchronized boolean add(SudokuMask item) {
        if (!isDerivative(item) && validate(item)) {
            rawAdd(item);
            return true;
        }
        return false;
    }

    /**
     * Searches the space of the given mask for unavoidable sets.
     *
     * Note: The shape of the search space is directly correlated to
     * the performance of this method. The mask forms a puzzle by erasing
     * digits from the sieve's config, which then undergoes a full solution
     * search along with additional processing for each solution found. If the
     * mask creates a puzzle with a great many solutions, it may take a long
     * time to process.
     *
     * @param searchSpace Indicates puzzle space to search for unavoidable sets.
     * @return Number of items that were added to this sieve.
     */
    public int searchForUAs(SudokuMask searchSpace) {
        int addedCount = 0;
        Sudoku puzzle = _config.filter(new SudokuMask(searchSpace).flip());
        for (Sudoku solution : puzzle.solutions()) {
            if (add(_config.diffMask(solution))) {
                addedCount++;
            }
        }
        return addedCount;
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
     * @param mask SudokuMask to check against the Items.
     * @return True if the mask contains at least one bit intersecting with each sieve item.
     */
    public synchronized boolean doesMaskSatisfy(SudokuMask mask) {
        for (ItemGroup group : _itemGroupsByBitCount) {
            for (SudokuMask item : group.items) {
                if (!item.intersects(mask)) {
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
}
