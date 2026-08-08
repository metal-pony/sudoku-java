package io.github.metal_pony.sudoku;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import static io.github.metal_pony.sudoku.Constants.*;
import io.github.metal_pony.sudoku.util.ArraysUtil;

public class SieveSearcher {
  class Choice {
    int[] indices;
    int current = -1; // Initialized to -1 so current() also returns -1
    List<Integer> seenContributions;
    List<SudokuMask> overlapping;

    // TODO Keep track of another overlapping; for disjoint sets
    DisjointSetsMap disjointOverlapping;

    Choice() {
      seenContributions = new ArrayList<>(8);
      overlapping = new ArrayList<>(64);
      // disjointOverlapping = new DisjointSetsMap(grid);
    }

    void setChoices(int[] choices) {
      indices = choices;
      current = choices.length - 1;
      while (
        (current >= 0 && seenMask.testBit(choices[current])) //&&
        // TODO This logic is wrong
        //mask.bitCount() + disjointSetsMap.minToSatisfy(mask) >= maxClues
      ) {
        current--;
      }

      if (current >= 0) {
        mask.setBit(indices[current]);
        overlapping = sieve.removeOverlapping(indices[current], overlapping);
        // disjointSetsMap.splitOutOverlapping(mask, disjointOverlapping);
      }
    }

    int current() {
      return (current >= 0) ? indices[current] : -1;
    }

    void putItemsBack() {
      for (int i = overlapping.size() - 1; i >= 0; i--) {
        sieve.rawAdd(overlapping.remove(i));
      }

      // disjointOverlapping.dumpTo(disjointSetsMap);

      if (current >= 0) {
        mask.unsetBit(indices[current]);
      }
    }

    boolean hasNext() {
      return current > 0;
    }

    int next() {
      if (current < 0) return -1;

      putItemsBack();
      seenMask.setBit(indices[current]);
      seenContributions.add(indices[current]);

      while (
        (current >= 0 && seenMask.testBit(indices[current])) //&&
        // TODO This logic is wrong
        // mask.bitCount() + disjointSetsMap.minToSatisfy(mask) >= maxClues
      ) {
        current--;
      }
      if (current < 0) return -1;

      mask.setBit(indices[current]);
      sieve.removeOverlapping(indices[current], overlapping);
      // disjointSetsMap.splitOutOverlapping(mask, disjointOverlapping);
      return indices[current];
    }

    void releaseDead() {
      for (int i = seenContributions.size() - 1; i >= 0; i--)
        seenMask.unsetBit(seenContributions.remove(i));
    }

    public StringBuilder addToString(StringBuilder strb) {
      strb.append('(');
      for (int i = 0; i <= current; i++) {
        if (i > 0) strb.append(',');
        strb.append(indices[i]);
      }
      if (current < indices.length - 1) {
        strb.append('/');
        for (int i = current+1; i < indices.length; i++) {
          if (i > current) strb.append(',');
          strb.append(indices[i]);
        }
      }
      strb.append(')');
      return strb;
    }
  }

  Sudoku grid;
  SudokuSieve sieve;
  /** Cache of the original sieve items. DO NOT MODIFY THIS LIST.*/
  List<SudokuMask> origSieveItems;
  /** Cache of cell indices associated with each sieve item. DO NOT MODIFY THIS MAP.*/
  Map<SudokuMask,int[]> itemsIndices;

  // TODO Use disjointSetsMap
  DisjointSetsMap disjointSetsMap;

  /** The current mask being analyzed during the search.*/
  SudokuMask mask;
  SudokuMask seenMask = new SudokuMask();

  Stack<Choice> choiceStack;

  int maxClues;

  public SieveSearcher(SudokuSieve sieve) {
    this(sieve, null);
  }

  public SieveSearcher(SudokuSieve sieve, SudokuMask baseMask) {
    this.grid = sieve.config();
    this.origSieveItems = new ArrayList<>(sieve.size());
    this.sieve = new SudokuSieve(grid);
    this.itemsIndices = new HashMap<>(sieve.size());

    for (SudokuMask item : sieve.items()) {
      this.sieve.rawAdd(item);
      this.origSieveItems.add(item);
      // int[] prev = itemsIndices.put(item, ArraysUtil.shuffle(item.toIndices()));
      int[] prev = itemsIndices.put(item, item.toIndices());
      if (prev != null)
        throw new RuntimeException("item already exists in indices map");
    }

    // disjointSetsMap = new DisjointSetsMap(grid);
    // disjointSetsMap.build(3);

    // System.out.printf("SieveSearcher initialized with %d sieve items. Disjoint set built with %d items.\n", sieve.size(), disjointSetsMap.size());

    this.choiceStack = new Stack<>();
    this.mask = (baseMask == null) ? new SudokuMask() : new SudokuMask(baseMask);
    this.seenMask = (baseMask == null) ? new SudokuMask() : new SudokuMask(baseMask);
  }

  // Sample disjointSetsMap distribution (sieve level 2)
  // [ 1]: 55
  // [ 2]: 900
  // [ 3]: 5378
  // [ 4]: 12938
  // [ 5]: 15296
  // [ 6]: 10162
  // [ 7]: 4024
  // [ 8]: 929
  // [ 9]: 120
  // [10]: 7
  // Sample disjointSetsMap distribution (sieve level 3)
  // [ 1]: 362
  // [ 2]: 19780
  // [ 3]: 156182
  // [ 4]: 300873
  // [ 5]: 289510
  // [ 6]: 170763
  // [ 7]: 61649
  // [ 8]: 12990
  // [ 9]: 1450
  // [10]: 72

  public void search(int max) {
    this.maxClues = max;
    long sieveSize = sieve.size();

    // This is functioning as the choice Stack.
    // Choice instances are created once (here).
    // When a cell is chosen, all remaining overlapping sieve items are taken from the sieve
    //    and held with the choice.
    // Then when a cell is removed as a choice, the items are put back into the sieve.
    Choice[] choices = new Choice[maxClues];
    for (int i = 0; i < maxClues; i++) choices[i] = new Choice();
    choices[0].setChoices(itemsIndices.get(sieve.first()));
    int depth = 1;

    long checked = 0L;
    long checkedSinceLastPrint = 0L;
    final long checkedToPrint = 10_000_000L;

    long puzzles = 0L; // Number of puzzles found directly from running this search

    // Set<SudokuMask> minPzzls = new HashSet<>();
    // Set<SudokuMask> crunchySet = new HashSet<>();

    while (depth > 0) {
      checked++;
      checkedSinceLastPrint++;
      Choice top = choices[depth - 1];

      // Print progress if it's been a while.
      if (checkedSinceLastPrint > checkedToPrint) {
        checkedSinceLastPrint -= checkedToPrint;
        StringBuilder strb = new StringBuilder();
        for (Choice c : choices) {
          if (c.current() >= 0) {
            c.addToString(strb);
            strb.append(' ');
          }
        }
        // System.out.printf("[%s] checked %12d; seen 0x%s; stack {%s}\n", LocalDateTime.now().toString(), checked, seenMask.toHexString(), strb.toString());
      }

      if (sieve.isEmpty()) {
        int numBits = mask.bitCount();

        Sudoku maskedGrid = grid.filter(mask);
        int flag = maskedGrid.solutionsFlag();
        if (flag == 1) {
          // if (isDerivativeOfAnotherPuzzle(minPzzls)) {
          //   System.out.printf(
          //     "🐟 %d chkd : %d sieve : %d pzls [%d] %s\n",
          //     checked,
          //     sieveSize,
          //     puzzles,
          //     numBits,
          //     maskedGrid.toString()
          //   );
          // } else {
            // for (SudokuMask crunched : searchDown2(crunchySet)) {
            //   puzzles++;
            //   minPzzls.add(crunched);
            //   System.out.printf(
            //     "⭐️ %d chkd : %d sieve : %d pzls [%d] %s\n",
            //     checked,
            //     sieveSize,
            //     puzzles,
            //     crunched.bitCount(),
            //     grid.filterStr(crunched)
            //   );
            // }
            // for (SudokuMask crunched : searchDown2(crunchySet)) {
              puzzles++;
              // minPzzls.add(mask);


              // System.out.printf(
              //   "⭐️ %d chkd : %d sieve : %d pzls [%d] %s\n",
              //   checked,
              //   sieveSize,
              //   puzzles,
              //   numBits,
              //   maskedGrid.toString()
              // );

              System.out.printf("%d [%d] %s\n", puzzles, numBits, maskedGrid.toString());

            // }
            // crunchySet.clear();
          // }
        } else {
          if (numBits < maxClues) {
            // System.out.printf(
            //   "✅ %d chkd : %d sieve : %d pzls [%d] %s\n",
            //   checked,
            //   sieveSize,
            //   puzzles,
            //   numBits,
            //   maskedGrid.toString()
            // );

            final long _sieveSize = sieveSize;
            maskedGrid.searchForSolutions(solution -> {
              SudokuMask diff = grid.diffMask(solution);
              if (sieve.add(diff)) {
                itemsIndices.put(diff, ArraysUtil.shuffle(diff.toIndices()));
                // System.out.printf("[%d] + %s\n", _sieveSize + 1, grid.filterStr(diff));
                // sieveSize++;
                return false;
              }
              return true;
            });
            sieveSize += sieve.size();
            if (sieve.size() == 0) {
              System.out.println("🚨 ?? no new sieve item found");
            } else {
              continue;
            }
            // Since a new sieve item was added, check from top of loop again now that sieve is not empty.
          }
        }

        top.putItemsBack();
        top.releaseDead();
        depth--;

        while (depth > 0 && choices[depth - 1].next() == -1) {
          choices[depth - 1].releaseDead();
          depth--;
        }

        continue;
      }

      // ELSE: sieve NOT empty
      if (depth < maxClues) {
        // choices[depth].setChoices(sieve.first().toIndices());
        choices[depth].setChoices(itemsIndices.get(sieve.first()));
        depth++;
      } else {
        while (depth > 0 && choices[depth - 1].next() == -1) {
          choices[depth - 1].releaseDead();
          depth--;
        }
      }
    }
  }

  public void searchAllSatifyingMasks(int maxBits) {
    this.maxClues = maxBits;
    long sieveSize = sieve.size();

    // This is functioning as the choice Stack.
    // Choice instances are created once (here).
    // When a cell is chosen, all remaining overlapping sieve items are taken from the sieve
    //    and held with the choice.
    // Then when a cell is removed as a choice, the items are put back into the sieve.
    Choice[] choices = new Choice[maxClues];
    for (int i = 0; i < maxClues; i++) choices[i] = new Choice();
    choices[0].setChoices(itemsIndices.get(sieve.first()));
    int depth = 1;

    long checked = 0L;
    long checkedSinceLastPrint = 0L;
    final long checkedToPrint = 1_000_000L;
    long satisfied = 0L; // Number of satisying masks found

    StringBuilder strb = new StringBuilder();
    String lineSep = System.lineSeparator();
    int bufSize = 0;
    int bufMaxSize = 1024;

    while (depth > 0) {
      checked++;
      checkedSinceLastPrint++;
      Choice top = choices[depth - 1];

      // Print progress if it's been a while.
      // if (checkedSinceLastPrint > checkedToPrint) {
      //   checkedSinceLastPrint -= checkedToPrint;
      //   StringBuilder strb2 = new StringBuilder();
      //   for (Choice c : choices) {
      //     if (c.current() >= 0) {
      //       c.addToString(strb2);
      //       strb2.append(' ');
      //     }
      //   }
      //   System.out.printf(
      //     "[%s] checked %12d; found %d; seen 0x%s; stack {%s}\n",
      //     LocalDateTime.now().toString(),
      //     checked, satisfied,
      //     seenMask.toHexString(), strb2.toString()
      //   );
      // }

      if (sieve.isEmpty()) {
        // int numBits = mask.bitCount();

        satisfied++;
        bufSize++;

        // System.out.printf(
        //   "%d chkd; [%d] {%d} %s\n",
        //   checked, satisfied, numBits,
        //   mask.toStringDots()
        // );
        // System.out.println(mask.toStringDots());
        strb.append(mask.toStringDots());
        strb.append(lineSep);

        if (bufSize == bufMaxSize) {
          System.out.print(strb.toString());
          bufSize = 0;
          strb.delete(0, strb.length());
        }

        // Rewind stack by 1 node
        top.putItemsBack();
        top.releaseDead();
        depth--;

        // May need to rewind more if previous nodes have no more choices available
        while (depth > 0 && choices[depth - 1].next() == -1) {
          choices[depth - 1].releaseDead();
          depth--;
        }

        continue;
      }

      // ELSE: (sieve NOT empty)
      if (depth < maxClues) {
        // choices[depth].setChoices(sieve.first().toIndices());
        choices[depth].setChoices(itemsIndices.get(sieve.first()));
        depth++;
      } else {
        while (depth > 0 && choices[depth - 1].next() == -1) {
          choices[depth - 1].releaseDead();
          depth--;
        }
      }
    }

    if (strb.length() > 0) {
      System.out.print(strb.toString());
    }
  }

  public void minsearch() {
    this.maxClues = SPACES;
    long sieveSize = sieve.size();

    // This is functioning as the choice Stack.
    // Choice instances are created once (here).
    // When a cell is chosen, all remaining overlapping sieve items are taken from the sieve
    //    and held with the choice.
    // Then when a cell is removed as a choice, the items are put back into the sieve.
    Choice[] choices = new Choice[maxClues];
    for (int i = 0; i < maxClues; i++) choices[i] = new Choice();
    choices[0].setChoices(itemsIndices.get(sieve.first()));
    int depth = 1;

    long checked = 0L;
    long checkedSinceLastPrint = 0L;
    final long checkedToPrint = 10_000_000L;

    long satisfied = 0L; // Number of masks found that satisfied the sieve
    long puzzles = 0L; // Number of puzzles found directly from running this search

    // HashSet<SudokuMask> searchDownResults = new HashSet<>();
    // HashSet<String> searchDownSeen = new HashSet<>();
    // HashSet<SudokuMask> results = new HashSet<>();

    Set<SudokuMask> minPzzls = new HashSet<>();
    Set<SudokuMask> crunchySet = new HashSet<>();

    while (depth > 0) {
      checked++;
      checkedSinceLastPrint++;
      Choice top = choices[depth - 1];

      // Print progress if it's been a while.
      if (checkedSinceLastPrint > checkedToPrint) {
        checkedSinceLastPrint -= checkedToPrint;
        StringBuilder strb = new StringBuilder();
        for (Choice c : choices) {
          if (c.current() >= 0) {
            c.addToString(strb);
            strb.append(' ');
          }
        }
        System.out.printf("[%s] checked %12d; seen 0x%s; stack {%s}\n", LocalDateTime.now().toString(), checked, seenMask.toHexString(), strb.toString());
      }
      // String maskStr = grid.filterStr(mask);
      // System.out.printf("[%2d] %s%s\n", sieve.size(), " ".repeat(choiceStack.size()), maskStr);
      // System.out.printf("   [%4d] {%12d} >%s%s\n", sieve.size(), checked, " ".repeat(depth), grid.filterStr(mask));

      if (sieve.isEmpty()) {
        // String maskStr = grid.filterStr(mask);
        // System.out.println(maskStr);
        satisfied++;
        // System.out.printf("✅ [%4d] {%12d} >%s%s\n", sieve.size(), c, " ".repeat(depth), maskStr);
        // if (!seen.contains(maskStr)) {
        //   seen.add(maskStr);

        // searchDownResults.clear();
        // searchDownResults.add(new SudokuMask(mask));
        // searchDownSeen.clear();
        // searchDown(searchDownResults, searchDownSeen);

        int numBits = mask.bitCount();

        // for (SudokuMask _mask : searchDownResults) {
          int flag = grid.filter(mask).solutionsFlag();
          if (flag == 1) {
            if (isDerivativeOfAnotherPuzzle(minPzzls)) {
              System.out.printf(
                "🐟 %d chkd : %d sieve : %d pzls [%d] %s\n",
                checked,
                sieveSize,
                puzzles,
                numBits,
                grid.filterStr(mask)
              );
            } else {
              for (SudokuMask crunched : searchDown2(crunchySet)) {
                puzzles++;
                minPzzls.add(crunched);
                numBits = crunched.bitCount();
                System.out.printf(
                  "⭐️ %d chkd : %d sieve : %d pzls [%d] %s\n",
                  checked,
                  sieveSize,
                  puzzles,
                  crunched.bitCount(),
                  grid.filterStr(crunched)
                );

                int newVal = Math.min(numBits, maxClues);
                if (newVal < maxClues) {
                  // maxClues.setVal(newVal);
                  maxClues = newVal;
                }
                // maxClues = Math.min(numBits, maxClues);
              }
              crunchySet.clear();
            }

          } else {
            if (numBits < maxClues) {
              System.out.printf(
                "✅ %d chkd : %d sieve : %d pzls [%d] %s\n",
                checked,
                sieveSize,
                puzzles,
                numBits,
                grid.filterStr(mask)
              );

              final long _sieveSize = sieveSize;
              grid.filter(mask).searchForSolutions(solution -> {
                SudokuMask diff = grid.diffMask(solution);
                if (sieve.add(diff)) {
                  itemsIndices.put(diff, ArraysUtil.shuffle(diff.toIndices()));
                  System.out.printf("[%d] + %s\n", _sieveSize + 1, grid.filterStr(diff));
                  // sieveSize++;
                  return false;
                }
                return true;
              });
              sieveSize += sieve.size();
              if (sieve.size() == 0) {
                System.out.println("🚨 ?? no new sieve item found");
              }
              // Since a new sieve item was added, check from top of loop again now that sieve is not empty.
              continue;
            }

            // int n = maxClues;

            // System.out.printf("   [%4d] {%12d} >%s\n", numBits, checked, maskStr);
            // System.out.printf("   (%6d) {%d} [%d] {%8d} %s\n", puzzles, satisfied, numBits, dupes, grid.filterStr(_mask).toString());
          }
          // maxClues = Math.min(numBits, maxClues);
        // }

        top.putItemsBack();
        top.releaseDead();
        depth--;

        while (depth > 0 && choices[depth - 1].next() == -1) {
          choices[depth - 1].releaseDead();
          depth--;
        }

        continue;
      }

      // ELSE: sieve NOT empty
      if (depth < maxClues) {
        // choices[depth].setChoices(sieve.first().toIndices());
        choices[depth].setChoices(itemsIndices.get(sieve.first()));
        depth++;
      } else {
        while (depth > 0 && choices[depth - 1].next() == -1) {
          choices[depth - 1].releaseDead();
          depth--;
        }
      }
    }
  }

  boolean isDerivativeOfAnotherPuzzle(Set<SudokuMask> pzzlMaskSet) {
    for (SudokuMask pMask : pzzlMaskSet) {
      if (mask.hasBitsSet(pMask)) {
        // System.out.printf("\n   MASK  > %s\nDERIV OF > %s\n\n", mask.toStringDots(), pMask.toStringDots());
        return true;
      }
    }
    return false;
  }

  Set<SudokuMask> searchDown2(Set<SudokuMask> results) {
    SudokuMask root = new SudokuMask(mask);
    Queue<SudokuMask> q = new LinkedList<>();
    q.offer(root);
    HashSet<SudokuMask> seen = new HashSet<>();
    seen.add(root);
    HashSet<SudokuMask> temp = new HashSet<>();
    while (!q.isEmpty()) {
      SudokuMask m = q.poll();

      temp.clear();
      for (int i = 0; i < 81; i++) {
        if (m.testBit(i)) {
          SudokuMask lower = new SudokuMask(m).unsetBit(i);
          int lowerFlag = grid.filter(lower).solutionsFlag();
          if (lowerFlag == 1) {
            temp.add(lower);
          }
        }
      }
      if (temp.isEmpty()) {
        results.add(m);
      } else {
        for (SudokuMask lower : temp) {
          if (!seen.contains(lower)) {
            q.offer(lower);
            seen.add(lower);
          }
        }
      }
    }
    if (results.isEmpty()) {
      System.out.println("-- 🚨 searchDown2 ERROR: RESULTS EMPTY 🚨 --");
    }
    return results;
  }




  void searchDown(Set<SudokuMask> resultSet, Set<SudokuMask> searchDownSeen) {
    for (int i = 0; i < 81; i++) {
      if (mask.testBit(i)) {
        mask.unsetBit(i);

        if (!searchDownSeen.contains(mask)) {
          searchDownSeen.add(new SudokuMask(mask));
          if (SudokuSieve.maskSatisfiesSieve(origSieveItems, mask)) {
            resultSet.add(new SudokuMask(mask));
            searchDown(resultSet, searchDownSeen);
          }
        }

        mask.setBit(i);
      }
    }
  }
}
