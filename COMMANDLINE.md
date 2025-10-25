# Command-line Interface

## Commands

```
`gen`
    Generation tool.
    By default, generate a random full grid.

    OPTIONS

    --normalize
        Rearranges grid values such that the top row is sequential.

    --amount {number; default 1}
        Number of grids to generate.

    --threads [number]
        Number of threads to use.
        If omitted, defaults to a single thread.
        If used without a specific number, defaults to maximum threads.
        Specifying more threads than the number of cores available on the
        system is technically allowed, but not recommended as it will
        cause performance issues.

    OPTIONS SPECIFIC TO PUZZLE GENERATION

    --clues {number}
        Generate a puzzle with the specified number of clues.
        Min: 17; Max 81.

        A NOTE ABOUT LOW-CLUE PUZZLE GENERATION

        Numbers closer to the 17 minimum are much more costly to generate.
        Generally speaking, most systems will be able to easily generate
        puzzles with 21+ clues.
        Be aware that lower number of clues does not necessarily correlate
        to puzzle difficulty.
        A lower clue count in addition to higher dfficulty will,
        for obvious reasons, compound the cost of generation.
        Not all full grids have valid puzzles with fewer than 21 clues.
        For generating puzzles with fewer than 21 clues, it is recommended
        to also specify sieve options, --useSieve and --level 3, which
        will indicate to the tool to use a different algorithm more
        capable of finding such puzzles.

    --difficulty {number; 1 - 3}
        Specify the difficulty of the generate puzzle.
        VALUES

        1: Easy       Techniques: finding naked and hidden singles.
        2: Moderate   Techniques: Not yet defined
        3: Hard       Techniques: Everything else.

        Generation may fail if the number of clues is high, e.g.,
        --clues 80 --difficulty 3 ; since there aren't enough empty spaces
        on the grid to form a difficult puzzle.

    --useSieve
        Whether to use an alternative algorithm, the sieve search, in
        generating puzzles. Should be used for lower-clue puzzles and
        in conjunction with --level.

    --grid {grid string}

    --level {number}



```

- full grids
- puzzles
- sieves
- fingerprints

### solve

### csv
