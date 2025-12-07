import java.io.File

/*
 * Advent of Code 2025 - Day 6: Trash Compactor (Part 1)
 *
 * Parse vertical math problems from a horizontal worksheet.
 * Each problem has numbers stacked vertically with an operator (+ or *) at the bottom.
 * Problems are separated by columns of spaces.
 */

data class Problem(val numbers: List<Long>, val operator: Char)

fun parseProblems(lines: List<String>): List<Problem> {
    if (lines.isEmpty()) return emptyList()

    val width = lines.maxOf { it.length }
    val grid = lines.map { it.padEnd(width) }

    val problems = mutableListOf<Problem>()
    var col = 0

    while (col < width) {
        // Skip separator columns (all spaces)
        if (grid.all { it[col] == ' ' }) {
            col++
            continue
        }

        // Find extent of this problem (until all-space column)
        val startCol = col
        while (col < width && !grid.all { it[col] == ' ' }) {
            col++
        }

        // Extract numbers and operator from columns [startCol, col)
        val numbers = mutableListOf<Long>()
        var operator = '+'

        for (row in grid.indices) {
            val segment = grid[row].substring(startCol, col).trim()
            if (segment.isEmpty()) continue

            when {
                segment == "+" || segment == "*" -> operator = segment[0]
                else -> segment.toLongOrNull()?.let { numbers.add(it) }
            }
        }

        if (numbers.isNotEmpty()) {
            problems.add(Problem(numbers, operator))
        }
    }

    return problems
}

fun solve(filename: String): Long {
    val lines = File(filename).readLines()
    val problems = parseProblems(lines)

    return problems.sumOf { p ->
        when (p.operator) {
            '+' -> p.numbers.sum()
            '*' -> p.numbers.reduce { acc, n -> acc * n }
            else -> 0L
        }
    }
}

fun main(args: Array<String>) {
    val filename = args.firstOrNull() ?: "input.txt"
    val result = solve(filename)
    println("Part 1: $result")
}
