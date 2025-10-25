# Command-line Interface

## Commands

```
help                    Display commands and usage.

generateSolutions       Generate sudoku solution(s).
  --amount {n >= 1}     Specify an amount to generate.
  --normalize           Arrange digits such that the top row of the board
                        reads 1 through 9 sequentially.
  --threads             Use all available system threads.
  --threads {n >= 1}    Use a given number of system threads.
                        Not recommended to use more than the system maximum.
  --pretty              Output grids in a more readable form.

generatePuzzles         Generate sudoku puzzle(s).
  --amount {n >= 1}     Specify an amount to generate.
  --normalize           Arrange digits such that the top row of the solution
                        reads 1 through 9 sequentially.
  --threads             Use all available system threads.
  --threads {n >= 1}    Use a given number of system threads.
                        Not recommended to use more than the system maximum.
  --clues {17 - 81}     Generate a puzzle with a given number of clues.
                        Less than 20 is not recommended due to the
                        processing power required.
                        Default 31.
  --difficulty {1 - 3}  Specify the difficulty of the generate puzzle.
    1: Easy             Solvable by finding naked and hidden singles.
    2: Moderate         Solvable by finding {Not yet defined}
    3: Hard             Solvable with more advanced techniques than above.
                        Generation may hang or fail if the number of clues
                        is high, e.g., --clues 80 --difficulty 3 ; since there
                        aren't enough empty spaces on the grid to form a
                        difficult puzzle.
  --solution            Generate a random solution to use for the puzzle(s).
  --solution {str}      Use the given solution for the puzzle(s).
  --pretty              Output puzzles in a more readable form.

solve                   Find solution(s) for a given grid.
  --grid {str}          The grid to find solutions for.
  --all                 Output all solutions.
  --first               Output the first solution found.
  --amount {n >= 1}     Output the first (n) solutions found.
  --count               Output only the number of solutions.
  --threads             Use all available system threads.
  --threads {n >= 1}    Use a given number of system threads.
                        Not recommended to use more than the system maximum.
  --pretty              Output solutions in a more readable form.

scramble                Jumbles the input grid or puzzle randomly.
  --grid {str}          The grid to scramble.

csv                     Transforms input sudoku data into csv.
                        Input should be plaintext puzzles, solutions, or
                        puzzle/solutions csv records.
  --format {str}        The output format.
                        Components should be wrapped in curly braces.
                        Unknown components will be calculated, so be
                        mindful of the CPU usage of level 4 fingerprints.
            COMPONENTS  {puzzle} from input.
                        {solution} from input, or calculated from puzzle.
                        {dc2} digit-combo fingerprint (level 2).
                        {dc3} digit-combo fingerprint (level 3).
                        {dc4} ...
                        {fp2} full-print fingerprint (level 2).
                        {fp3} ...
                        {fp4} ...
            EXAMPLE     csv --format '{puzzle},{solution},{fp3}'
  --threads             Use all available system threads.
  --threads {n >= 1}    Use a given number of system threads.

sieve                   Generate a set of unavoidable sets for a given grid.
  --algo                The strategy to use for seeding the sieve.
                        Usually digit-combos (dc), or full-print (fp),
                        from levels 2 to 4.
                VALUES  "dc2", "dc3", "dc4", "fp2", "fp3", "fp4".
                        Default: fp3.

fingerprint (alt: fp)   Generate a hash for a grid. The fingerprint will be
                        the same regardless of how the grid is transformed
                        via symmetry-preserving transformations. All grids
                        that are essentially similar are guaranteed to share
                        the same fingerprint.
  --algo                The strategy to use. Usually digit-combos (dc), or
                        full-print (fp), from levels 2 to 4.
                        "dc2", "dc3", "dc4", "fp2", "fp3", "fp4".
                        Default: fp3.
  --threads             Use all available system threads.
  --threads {n >= 1}    Use a given number of system threads.
                        Not recommended to use more than the system maximum.

sieveSearch             A hitting-set search for finding lower-clue puzzles.
  --grid {str}          The grid to find puzzles for. Default: random.
  --level {2 - 4}       The level at which to seed the sieve. Default: 3.
  --maxClues {17 - 81}  Maximum number of puzzle clues. The search may discover
                        puzzles with less than this maximum. Default: no max.

minSearch               A hitting-set search designed to find puzzles with
                        the lowest number of clues.
  --grid {str}          The grid to find minimum puzzles for. Default: random.
  --level {2 - 4}       The level at which to seed the sieve. Default: 3.
```
