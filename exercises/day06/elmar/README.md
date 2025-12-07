# Day 6: Trash Compactor

Parse vertical math problems from a horizontal worksheet. Problems are separated by columns of spaces, operator (`+` or `*`) at the bottom.

## Part 1

**Task:** Read numbers row-by-row, solve problems, sum results.

Each row segment within a problem is one number:
```
123 328  51 64
 45 64  387 23
  6 98  215 314
*   +   *   +
```

Problems (left to right):
- `123 * 45 * 6 = 33210`
- `328 + 64 + 98 = 490`
- `51 * 387 * 215 = 4243455`
- `64 + 23 + 314 = 401`

Grand total: **4277556**

**Approach:** Scan columns left-to-right, group non-space columns into problems. Parse each row segment as a number.

## Part 2

**Task:** Cephalopod math - read numbers column-by-column (digits top-to-bottom).

Same worksheet, but each column forms a number:
- Leftmost problem: `1 * 24 * 356 = 8544`
- Second: `369 + 248 + 8 = 625`
- Third: `32 * 581 * 175 = 3253600`
- Rightmost: `623 + 431 + 4 = 1058`

Grand total: **3263827**

**Approach:** Same column grouping, but read each column vertically (digits top-to-bottom form a number).

## Usage

```bash
kotlinc solution.kt -include-runtime -d solution.jar
java -jar solution.jar input.txt        # Part 1

kotlinc solution_part2.kt -include-runtime -d solution_part2.jar
java -jar solution_part2.jar input.txt  # Part 2
```

Requires Kotlin (`sudo apt install kotlin` or via SDKMAN).
