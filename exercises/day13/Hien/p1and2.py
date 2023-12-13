input_example = """#.##..##.
..#.##.#.
##......#
##......#
..#.##.#.
..##..##.
#.#.##.#.

#...##..#
#....#..#
..##..###
#####.##.
#####.##.
..##..###
#....#..#"""
input = input_example
ms = input.split("\n\n")


def find(m, target_diff=0):
    lines = m.splitlines()
    res = 0
    for i in range(1, len(lines[0])):
        diff_count = 0
        for l in lines:
            for a, b in zip(l[:i][::-1], l[i:]):
                if a != b:
                    diff_count += 1
        if diff_count == target_diff:
            res += i
    return res


res = 0
for m in ms:
    m2 = "\n".join(["".join(i) for i in zip(*m.split())])
    res += find(m, 0)
    res += find(m2, 0) * 100
print(res)

res = 0
for m in ms:
    m2 = "\n".join(["".join(i) for i in zip(*m.split())])
    res += find(m, 1)
    res += find(m2, 1) * 100

print(res)
