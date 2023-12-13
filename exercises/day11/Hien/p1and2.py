input_example="""...#......
.......#..
#.........
..........
......#...
.#........
.........#
..........
.......#..
#...#....."""
input=input_example
lines = input.splitlines()

empty_rows = []
empty_cols = []
for i, l in enumerate(lines):
    if len(set(l))==1 and l[0]=='.':
        empty_rows.append(i)
        
for i in range(len(lines[0])):
    is_empty=True
    for j in range(len(lines)):
        if lines[j][i]=='#':
            is_empty=False
            break
    if is_empty:
        empty_cols.append(i)

# find all galaxies
d = []
for i, l in enumerate(lines):
    for j, ll in enumerate(l):
        if ll=='#':
            d.append((i,j))
            
def find_sum(n=2):
    res = 0
    for i in range(0, len(d)-1):
        for j in range(i+1,len(d)):
            res += abs(d[i][0]-d[j][0]) + abs(d[i][1]-d[j][1])
            for r in range(min(d[i][0],d[j][0]), max(d[i][0],d[j][0])):
                if r in empty_rows: res += n-1
            for l in range(min(d[i][1],d[j][1]), max(d[i][1],d[j][1])):
                if l in empty_cols: res += n-1
    return res
            
print(find_sum(2))
print(find_sum(1000000))