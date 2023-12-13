input_example = """LR

11A = (11B, XXX)
11B = (XXX, 11Z)
11Z = (11B, XXX)
22A = (22B, XXX)
22B = (22C, 22C)
22C = (22Z, 22Z)
22Z = (22B, 22B)
XXX = (XXX, XXX)"""
import math
input = input_example.replace("(", "").replace(")", "")
instruction, lines = input.split("\n\n")
d = {}

for l in lines.splitlines():
    k, v = l.split(" = ")
    vl, vr = v.split(", ")
    d[k] = {"L": vl, "R": vr}

starts = [i for i in d.keys() if i[-1] == "A"]


def count_steps(start):
    count = 0
    while True:
        for i in instruction:
            if start[-1] == "Z":
                return count
            start = d[start][i]
            count += 1

counts = [count_steps(i) for i in starts]

res = counts[0]
for i in range(1, len(counts)):
    res = math.lcm(res, counts[i])

print(res)
