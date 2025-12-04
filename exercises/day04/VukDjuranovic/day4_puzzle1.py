import numpy as np
from scipy.signal import convolve2d

file_path = "input.txt"
file_lines = None

roll_matrix = list()

with open(file_path, 'r') as file:
    file_lines = file.readlines()

    for line in file_lines:
        matrix_row = [1 if i == "@" else 0 for i in list(line.strip())]
        roll_matrix.append(matrix_row)

    roll_matrix = np.array(roll_matrix)
    kernel = np.ones((3, 3))
    convolved_matrix = convolve2d(roll_matrix, kernel, mode="same", boundary="fill", fillvalue=0)
    only_rolls_non_null = roll_matrix * convolved_matrix

    condition_nonzero = (only_rolls_non_null != 0)
    condition_le_four = (only_rolls_non_null <= 4)

    # Combine conditions using the '&' operator
    combined_condition = condition_nonzero & condition_le_four

    liftable_rolls = np.where(combined_condition, 1, 0)
    print('Number of liftable rolls is: ' + str(np.sum(liftable_rolls)))
