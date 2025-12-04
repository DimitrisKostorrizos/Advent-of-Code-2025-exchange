# Day 4: Printing Department

Paper rolls (`@`) arranged on a grid. Forklifts can access a roll if fewer than 4 adjacent positions (8-way neighborhood) contain other rolls.

## Part 1

**Task:** Count accessible paper rolls.

Algorithm checks each roll's 8 neighbors:
```java
for (int[] dir : DIRECTIONS) {
    int nr = row + dir[0], nc = col + dir[1];
    if (inBounds(nr, nc) && grid[nr][nc] == '@') adjacentRolls++;
}
return adjacentRolls < 4;
```

Example grid (10×10) → **13** accessible rolls.

## Part 2

**Task:** Simulate repeated removal of accessible rolls until none remain accessible. Count total removed.

Each iteration: find all rolls with < 4 neighbors, remove them simultaneously, repeat.

```java
while (true) {
    List<int[]> toRemove = findAccessible(grid);
    if (toRemove.isEmpty()) break;
    for (int[] pos : toRemove) grid[pos[0]][pos[1]] = '.';
    totalRemoved += toRemove.size();
}
```

Example: 13 → 12 → 7 → 5 → 2 → 1 → 1 → 1 → 1 = **43** total removed.

## Usage

```bash
# Compile and run Part 1
javac Solution.java && java Solution test.txt

# Compile and run Part 2
javac SolutionPart2.java && java SolutionPart2 test.txt
```

Requires Java 17+ (uses `List.toList()`).
