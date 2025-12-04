import numpy as np

file_path = "input.txt"
file_lines = None
max_joltages = list()

def find_maximum_joltages(joltages, current_str = ''):
    max = 0
    max_pos = 0
    for i, j in enumerate(joltages[:-1]):
        if j > max:
            max = j
            max_pos = i

    if len(current_str) == 0:
        n_joltages = joltages + [0]
        second_max = find_maximum_joltages(n_joltages[max_pos+1:], str(max))
        return int(str(max) + str(second_max))

    return max



with open(file_path, 'r') as file:
    file_lines = file.readlines()

    for line in file_lines:
        joltages = [int(i) for i in list(line.strip())]
        max_joltages.append(find_maximum_joltages(joltages))

    print("Maximum joltages are: ")
    print(max_joltages)
    print("###################")

print("Sum of max joltages is: " + str(np.sum(max_joltages)))