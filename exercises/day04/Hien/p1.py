input="""Card   1: 79  1  6  9 88 95 84 69 83 97 | 42 95  1  6 71 69 61 99 84 12 32 96  9 82 88 97 53 24 28 65 83 38  8 68 79
Card   2: 34 76 23 61 56 74 13 42 18  6 | 18 13 21 64 74 97 34 43 31 23 56 82 76 61 45 69 10 81 48  6  9 30 47 95 42"""

res = 0
for l in input.splitlines():
    draw_numbers, winning_numbers = l.split(":")[1].split("|")
    draw_numbers, winning_numbers = set(draw_numbers.split()), set(winning_numbers.split())
    count = len(draw_numbers.intersection(winning_numbers))
    res += 2**(count-1) if count > 0 else 0
print(res)