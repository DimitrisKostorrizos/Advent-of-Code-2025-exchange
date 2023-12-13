input_example = """0 3 6 9 12 15
1 3 6 10 15 21
10 13 16 21 30 45"""
input = input_example
lines = input.splitlines()


def find_missing(l):
    if len(set(l)) == 1:
        return l[0], l[0]
    ff, ll = find_missing([l[i] - l[i - 1] for i in range(1, len(l))])
    return l[0] - ff, l[-1] + ll


res_first, res_last = 0, 0
for l in lines:
    first, last = find_missing([int(i) for i in l.split()])
    res_first += first
    res_last += last
print(res_first, res_last)