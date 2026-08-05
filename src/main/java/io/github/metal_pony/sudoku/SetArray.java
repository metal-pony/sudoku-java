package io.github.metal_pony.sudoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetArray<T> {
    List<Set<T>> list;
    int length;
    long size;

    public SetArray(int length) {
      this.length = length;
      this.size = 0;
      this.list = new ArrayList<>();
      for (int i = 0; i < length; i++) {
        list.add(new HashSet<>());
      }
    }

    public long size() {
      return size;
    }

    public boolean contains(T data, int index) {
      validateIndex(index);
      return list.get(index).contains(data);
    }

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
