input_task=""""""
input_example = """FF7FSF7F7F7F7F7F---7
L|LJ||||||||||||F--J
FL-7LJLJ||||||LJL-77
F--JF--7||LJLJ7F7FJ-
L---JF-JLJ.||-FJLJJ7
|F|F-JF---7F7-L7L|7|
|FFJF7L7F-JF7|JL---7
7-L-JL7||F7|L7F-7F7|
L.L7LFJ|||||FJL7||LJ
L7JLJL-JLJLJL--JLJ.L"""
input = input_example
matrix = input.splitlines()
nrows, ncols = len(matrix), len(matrix[0])


# get the previous and next pipes from current position
def check(i, j):
    r = []
    if 0 <= i - 1 and matrix[i][j] in "S|LJ" and matrix[i - 1][j] in "S|F7":
        r.append((i - 1, j))
    if i + 1 < nrows and matrix[i][j] in "S|F7" and matrix[i + 1][j] in "S|LJ":
        r.append((i + 1, j))
    if 0 <= j - 1 and matrix[i][j] in "S-7J" and matrix[i][j - 1] in "S-FL":
        r.append((i, j - 1))
    if j + 1 < ncols and matrix[i][j] in "S-FL" and matrix[i][j + 1] in "S-7J":
        r.append((i, j + 1))
    return r[0], r[1]


# find S and find a pipe symbol that can replace S
si, sj = -1, -1
new_S = ""
for i, l in enumerate(matrix):
    if "S" in l:
        si, sj = i, l.index("S")
        r1, r2 = check(si, sj)
        if r1[0] == r2[0]:
            new_S = "-"
        if r1[1] == r2[1]:
            new_S = "|"
        if r1[0] < si and sj < r2[1]:
            new_S = "L"
        if r1[0] < si and sj > r2[1]:
            new_S = "J"
        if r1[0] > si and sj < r2[1]:
            new_S = "F"
        if r1[0] > si and sj > r2[1]:
            new_S = "7"
        break
print(si, sj, new_S)
# copy the pipe loop into a new matrix, replace S by new_S
markings = [["."] * ncols for _ in range(nrows)]
start = (si, sj)
previous = (-1, -1)
while True:
    next1, next2 = check(*start)
    current_pipe = matrix[start[0]][start[1]]
    start, previous = (next2, start) if next1 == previous else (next1, start)
    next_pipe = matrix[start[0]][start[1]]
    markings[start[0]][start[1]] = next_pipe
    if next_pipe == "S":
        break

markings[si][sj] = new_S

# idea: a point is in the circle if there is a odd number of pipe between its position and the boundary
# we need only to check to the left of each point
# count everytime you see '|', 'L(-)*7' and 'F(-)*J'
count = 0
for i in range(nrows):
    for j in range(ncols):
        if markings[i][j] == ".":
            l = "".join(markings[i][:j])
            l = l.replace(".", "")
            l = l.replace("-", "")
            l = l.replace("L7", "|")
            l = l.replace("FJ", "|")
            l = l.replace("LJ", "")
            l = l.replace("F7", "")
            if len(l) % 2 == 1:
                count += 1
print(count)
