input_task = """"""
input_example = """???.### 1,1,3
.??..??...?##. 1,1,3
?#?#?#?#?#?#?#? 1,3,1,6
????.#...#... 4,1,1
????.######..#####. 1,6,5
?###???????? 3,2,1"""
input = input_example
lines = input.splitlines()


def search(l, nums):
    if len(nums) == 0:
        return 0 if "#" in l else 1
    n = nums[0]
    ll = l.replace("?", "#")
    found = 0
    for i in range(len(ll)):
        if i > 0 and l[i - 1] == "#":
            break
        if ll[i : i + n] == "#" * n:
            if i + n == len(ll) or l[i + n] in ".?":
                found += search(l[i + n + 1 :], nums[1:])

    return found

res = 0
for l in lines:
    p, nums = l.split()
    nums = [int(i) for i in nums.split(",")]
    res += search(p, nums)
print(res)