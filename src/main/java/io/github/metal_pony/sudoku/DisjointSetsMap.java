package io.github.metal_pony.sudoku;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class DisjointSetsMap extends SetArray<SudokuMask> {
    static class DjNode {
      // The number of UAs this mask contains
      int n;
      SudokuMask mask;
      DjNode() {
        this(0, new SudokuMask());
      }
      DjNode(DjNode other) {
        this(other.n, new SudokuMask(other.mask));
      }
      DjNode(int n, SudokuMask mask) {
        this.n = n;
        this.mask = mask;
      }
      List<DjNode> nexts(List<SudokuMask> maskPool) {
        List<DjNode> nexts = new ArrayList<>();
        for (SudokuMask maybeNext : maskPool) {
          if (!mask.intersects(maybeNext)) {
            nexts.add(new DjNode(n + 1, new SudokuMask(mask).add(maybeNext)));
          }
        }
        return nexts;
      }
      void nexts(Collection<SudokuMask> maskPool, Consumer<DjNode> callback) {
        for (SudokuMask maybeNext : maskPool) {
          if (!mask.intersects(maybeNext)) {
            callback.accept(new DjNode(n + 1, new SudokuMask(mask).add(maybeNext)));
          }
        }
      }
    }

    Sudoku grid;

    public DisjointSetsMap(Sudoku grid) {
      super(Sudoku.SPACES);
      this.grid = grid;
    }

    public void dumpTo(DisjointSetsMap other) {
      for (int i = length - 1; i > 0; i--) {
        if (!list.get(i).isEmpty()) {
          for (SudokuMask item : list.get(i)) {
            other.add(item, i);
          }
          list.get(i).clear();
        }
      }
      size = 0;
    }

    public void splitOutOverlapping(SudokuMask mask, DisjointSetsMap other) {
      for (int i = length - 1; i > 0; i--) {
        final int _i = i;
        list.get(i).removeIf(item -> {
          boolean intersects = mask.intersects(item);
          if (intersects) {
            other.add(item, _i);
            size--;
          }
          return intersects;
        });
      }
    }

    public void build(int level) {
      SudokuSieve sieve = new SudokuSieve(grid);
      sieve.seedThreaded(sieve.digitCombos(level));
      Set<SudokuMask> items = sieve.items();

      Queue<DjNode> q = new LinkedList<>();
      for (SudokuMask m : items) {
        DjNode node = new DjNode(1, new SudokuMask(m));
        q.offer(node);
        add(node.mask, node.n);
        // System.out.printf("[%d] [%2d] %s\n", size() - 1, node.n, grid.filterStr(node.mask));
      }

      while (!q.isEmpty()) {
        DjNode curNode = q.poll();

        curNode.nexts(items, nextNode -> {
          if (add(nextNode.mask, nextNode.n)) {
            // System.out.printf("[%d] [%2d] %s\n", size() - 1, nextNode.n, grid.filterStr(nextNode.mask));
            q.offer(nextNode);
          }
        });
      }
    }

    public SudokuMask largestNonOverlapping(SudokuMask mask) {
      for (int i = length - 1; i > 0; i--) {
        Set<SudokuMask> set = list.get(i);
        if (!set.isEmpty()) {
          for (SudokuMask item : set) {
            if (!mask.intersects(item)) {
              return new SudokuMask(item);
            }
          }
        }
      }
      return null;
    }

    public int minToSatisfy(SudokuMask mask) {
      // int bc = mask.bitCount();
      for (int i = length - 1; i > 0; i--) {
        Set<SudokuMask> set = list.get(i);
        if (!set.isEmpty()) {
          for (SudokuMask item : set) {
            if (!mask.intersects(item)) {
              return i;
            }
          }
        }
      }
      return 0;
    }
  }
