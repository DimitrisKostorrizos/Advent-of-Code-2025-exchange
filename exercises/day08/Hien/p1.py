input_example = """LLR

AAA = (BBB, BBB)
BBB = (AAA, ZZZ)
ZZZ = (ZZZ, ZZZ)"""

input = input_example.replace("(", "").replace(")", "")
instruction, lines = input.split("\n\n")
d = {}

for l in lines.splitlines():
    k, v = l.split(" = ")
    vl, vr = v.split(", ")
    d[k] = {"L": vl, "R": vr}


def find(start="AAA"):
    count = 0
    while True:
        for i in instruction:
            if start == "ZZZ":
                return count
            start = d[start][i]
            count += 1


print(find())
