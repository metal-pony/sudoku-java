package io.github.metal_pony.sudoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Array-based collection storing a set of items at each index, where
 * each index maintains a unique set of elements. This class is useful
 * for large sets where the data points can be mapped to an associated index.
 * @param <T> Data type for elements stored in the set.
 */
public class SetArray<T> {
    List<Set<T>> list;
    int length;
    long size;

    /**
     * Creates a new SetArray with the given number of subset buckets.
     * @param length Number of subsets to create.
     */
    public SetArray(int length) {
      this.length = length;
      this.size = 0;
      this.list = new ArrayList<>();
      for (int i = 0; i < length; i++) {
        list.add(new HashSet<>());
      }
    }

    /**
     * Gets the combined number of items in this SetArray.
     * @return Total number of items.
     */
    public long size() {
      return size;
    }

    /**
     * Checks if the given data point is contained in the subset at the specified index.
     * @param data The data point to check.
     * @param index The index of the subset to check.
     * @return True if the data point is contained; otherwise false.
     */
    public boolean contains(T data, int index) {
      validateIndex(index);
      return list.get(index).contains(data);
    }

    /**
     * Adds the given data point to the subset at the specified index.
     * @param data The data point to add.
     * @param index The index of the subset to add to.
     * @return True if the data point was added; otherwise false.
     */
    public boolean add(T data, int index) {
      validateIndex(index);
      if (list.get(index).add(data)) {
        size++;
        return true;
      }
      return false;
    }

    private void validateIndex(int index) {
      if (index < 0 || index >= length) {
        throw new IllegalArgumentException("Bad index (" + index + "). Max " + length + ".");
      }
    }
  }
