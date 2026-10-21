# Sudoku-java

Sudoku library and generator.

## Installing

### Maven

Available from Maven Central.

```
<dependency>
  <groupId>io.github.metal-pony</groupId>
  <artifactId>sudoku</artifactId>
  <version>0.1.1</version>
</dependency>
```

## Usage Examples

```java
// Initialize with various data (String, int[81], Sudoku)
Sudoku sudoku = new Sudoku(".....8..98...16.7.94......8.92..186.46..........56.....3...958.28.3.4..6.1..8.423");

// Terminal-friendly printing
println(sudoku);
// .....8..98...16.7.94......8.92..186.46..........56.....3...958.28.3.4..6.1..8.423

println(sudoku.toMedString());
// . . . | . . 8 | . . 9
// 8 . . | . 1 6 | . 7 .
// 9 4 . | . . . | . . 8
// ------+-------+------
// . 9 2 | . . 1 | 8 6 .
// 4 6 . | . . . | . . .
// . . . | 5 6 . | . . .
// ------+-------+------
// . 3 . | . . 9 | 5 8 .
// 2 8 . | 3 . 4 | . . 6
// . 1 . | . 8 . | 4 2 3

// Set or remove digits -- setDigit(cellIndex, digit)
sudoku.setDigit(0, 7);
sudoku.setDigit(5, 0);
sudoku.setDigit(8, 0);
sudoku.setDigit(80, 0);

// Check if a puzzle has a single solution, per "solution flag".
// 0 -> no solution
// 1 -> single solution
// 2 -> multiple solutions
sudoku.solutionsFlag();
// 2 (several clues were removed - the grid is no longer a proper sudoku)

// Iterate over solutions
sudoku.solutions().forEach(println);
// 726438951853916274941275638392741865465893712178562349634129587287354196519687423
// 726895341853416279941273658392741865465938712178562934634129587287354196519687423
// 721938654853416279946275318392741865465892731178563942634129587287354196519687423
// 721438659853916274946275318392741865465892731178563942634129587287354196519687423
// 721938654853416972946275318392741865465892731178563249634129587287354196519687423
// 726845319853916274941732658392471865465298731178563942634129587287354196519687423

// Generate solutions
Sudoku solution = Sudoku.generateConfig();
// 813426975495371682627985413958643721134297568276518349381754296549862137762139854

// Generate puzzles
Sudoku.generatePuzzle(24); // 24 clues
// ...8.5..1.....12..7..........8.1..2..3..2..8...5...7.99...4.....2....9.71.7.6...5
// Note: Less clues does not necessarily make a puzzle difficult,
//   but it does make it more difficult to generate.

// Generate palindrome puzzles
Sudoku puzzle = Sudoku.generatePalindromePuzzle(24);
// 6 . . | 7 . . | . . .
// . 7 . | 4 9 2 | . . .
// . . . | . . . | 4 . .
// ------+-------+------
// . 9 4 | . . . | 8 1 .
// . 8 . | . . . | . 4 .
// . 1 3 | . . . | 7 5 .
// ------+-------+------
// . . 1 | . . . | . . .
// . . . | 8 1 3 | . 9 .
// . . . | . . 7 | . . 2

// Generate puzzles using a mask
SudokuMask mask = new SudokuMask("...........11.11...1..1..1.1...1...11.......1.1.....1...1...1.....1.1...1...1...1");
Sudoku.searchForPuzzles(mask, puzzle -> {
  // callback when a puzzle is found
  println(puzzle.toMedString());
  return false; // Stop searching (true will continue)
});
// . . . | . . . | . . .
// . . 1 | 2 . 3 | 4 . .
// . 8 . | . 7 . | . 9 .
// ------+-------+------
// 5 . . | . 9 . | . . 6
// 3 . . | . . . | . . 4
// . 2 . | . . . | . 3 .
// ------+-------+------
// . . 3 | . . . | 2 . .
// . . . | 9 . 1 | . . .
// 8 . . | . 6 . | . . 9

// Generate a solution's fingerprint(s).
// These will stay the same no matter how the grid is transformed!
println(solution);
// 564792318371468925289315746892543671456871239713926854647259183128634597935187462
solution.dc2(); // fast!
// 6:5:5:4:4:5::16
solution.dc3(); // finer-grained!
// 6::10::d:4:11:4:17:b:1d:15:21:24:53:2c:31:29:20:17:8
// Randomly swaps bands, stacks, rows in-band, columns in-stack, rotates, and swaps digits.
// Essentially still the same solution, but unrecognizable.
solution.scramble();
// 912537684583614297746982315298741536634258971157396428371469852425873169869125743
solution.dc2();
// 6:5:5:4:4:5::16
solution.dc3();
// 6::10::d:4:11:4:17:b:1d:15:21:24:53:2c:31:29:20:17:8
```

## License

MIT

## Contributing

Contributions welcome! Feel free to submit issues and fork + pull requests.
