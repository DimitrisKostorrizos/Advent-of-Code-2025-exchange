input_example = """???.### 1,1,3
.??..??...?##. 1,1,3
?#?#?#?#?#?#?#? 1,3,1,6
????.#...#... 4,1,1
????.######..#####. 1,6,5
?###???????? 3,2,1"""
input = input_example
lines = input.splitlines()

from functools import lru_cache


@lru_cache(maxsize=None)
def search(l, nums):
    if len(nums) == 0:
        return 0 if "#" in l else 1
    n, *others = nums.split(",")
    n, others = int(n), ",".join(others)
    ll = l.replace("?", "#")
    found = 0
    for i in range(len(l) - n + 1):
        if ll[i : i + n] == "#" * n:
            if i + n == len(l) or l[i + n] in ".?":
                found += search(l[i + n + 1 :], others)
        if l[i] == "#":
            break
    return found


res = 0
for l in lines:
    p, nums = l.split()
    res += search("?".join([p] * 5), ",".join([nums] * 5))


print(res)