import numpy as np

file_path = "test.txt"
file_lines = None
max_joltages = list()

def find_maximum_joltages(joltages, current_str = '', end_of_window = 12):
    """

    :param joltages: the array in which we need to find new maximum
    :param current_str: current extracted max_joltage, at begining it is empty string
    :param end_of_window: tells us until what position within joltages array we can search

    For example if we are searching within 987654321111111, and window size is 12,
    we are allowed to search within the following window joltages[:-end_of_window+1], which is
    [9876]54321111111 the part inside [] brackets.
    :return:
    """
    maxj = 0
    max_pos = 0
    n_joltages = None
    if end_of_window < 0:
        return ''
    if end_of_window == 1 and len(joltages) > 0:
        n_joltages = joltages
    else:
        n_joltages = joltages[:-end_of_window+1]
    for i, j in enumerate(n_joltages):
        if j > maxj:
            maxj = j
            max_pos = i

    current_str += str(maxj)

    end_of_window -= 1
    if len(current_str) <= 12:
        return str(maxj) + find_maximum_joltages(joltages[max_pos+1:], current_str, end_of_window)
    else:
        return ''


with open(file_path, 'r') as file:
    file_lines = file.readlines()

    for line in file_lines:
        joltages = [int(i) for i in list(line.strip())]
        max_joltages.append(find_maximum_joltages(joltages))

    print("Maximum joltages are: ")
    print(max_joltages)
    lents_of_jolts = [len(jl) for jl in max_joltages]
    print("###################")

max_joltages_int = [int(mjl) for mjl in max_joltages]
print("Sum of max joltages is: " + str(np.sum(max_joltages_int)))