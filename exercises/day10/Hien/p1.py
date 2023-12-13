input_task=""""""
input_example=""".....
.S-7.
.|.|.
.L-J.
....."""
input=input_task
matrix = input.splitlines()
nrows, ncols = len(matrix), len(matrix[0])
res = 0
si, sj = -1, -1

# find position of S
for i, l in enumerate(matrix):
    if 'S' in l:
        si, sj = i, l.index('S')
        break

# get the previous and next pipe for the given position
def check(i,j):
    r = []
    if 0<=i-1 and matrix[i][j] in 'S|LJ' and matrix[i-1][j] in 'S|F7':
        r.append((i-1,j))
    if i+1<nrows and matrix[i][j] in 'S|F7' and matrix[i+1][j] in 'S|LJ':
        r.append((i+1,j))
    if 0<=j-1 and matrix[i][j] in 'S-7J' and matrix[i][j-1] in 'S-FL':
        r.append((i,j-1))
    if j+1<ncols and matrix[i][j] in 'S-FL' and matrix[i][j+1] in 'S-7J':
        r.append((i,j+1))
    return r[0], r[1]

count=0
start = (si, sj)
previous= (-1, -1)

# traveling over the pipes
while True:
    count+=1
    next1, next2 = check(*start)
    start, previous = (next2, start) if next1==previous else (next1, start)
    if matrix[start[0]][start[1]]=='S':
        break
print(count/2)