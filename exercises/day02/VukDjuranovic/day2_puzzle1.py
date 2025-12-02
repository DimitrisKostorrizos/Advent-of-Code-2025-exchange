import numpy as np

file_path = "input.txt"
file_lines = None

invalid_ids = list()

try:
    with open(file_path, 'r') as file:
        file_lines = file.readlines()

        line = file_lines[0]
        # get ranges
        ranges = line.split(',')
        for r in ranges:
            r_lower_bound = int(r.split('-')[0])
            r_upper_bound = int(r.split('-')[1])

            i = r_lower_bound
            while i <= r_upper_bound:
                i_str = str(i)
                if len(i_str) % 2 == 0:
                    # we want to check if first part of the string is same as the second part of the string
                    # if it is, that is an invalid id
                    middle_point = int(len(i_str)/2)
                    if i_str[:middle_point] == i_str[middle_point:]:
                        invalid_ids.append(i)
                i += 1

except Exception as e:
    print(f"An error occurred: {e}")


print("Sum of invalid ids is: " + str(np.sum(invalid_ids)))