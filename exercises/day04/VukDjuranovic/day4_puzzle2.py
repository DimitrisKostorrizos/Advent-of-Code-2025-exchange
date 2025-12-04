import numpy as np
from scipy.signal import convolve2d
import pandas as pd

file_path = "input.txt"
file_lines = None

roll_matrix = list()
kernel = np.ones((3, 3))
liftable_rolls_count = 0

def finding_rolls_for_removal(r_matrix):
    """
    This method will return a n x n matrix, where elements will equal to 1, if a roll can be removed, 0 otherwise

    """
    convolved_matrix = convolve2d(r_matrix, kernel, mode="same", boundary="fill", fillvalue=0)
    only_rolls_non_null = r_matrix * convolved_matrix

    condition_nonzero = (only_rolls_non_null != 0)
    condition_le_four = (only_rolls_non_null <= 4)

    # Combine conditions using the '&' operator
    combined_condition = condition_nonzero & condition_le_four

    liftable_rolls = np.where(combined_condition, 1, 0)
    return liftable_rolls

with open(file_path, 'r') as file:
    file_lines = file.readlines()

    for line in file_lines:
        matrix_row = [1 if i == "@" else 0 for i in list(line.strip())]
        roll_matrix.append(matrix_row)

    roll_matrix = np.array(roll_matrix)

    i = 1
    new_roll_matrix = roll_matrix.copy()
    while i > 0:
        liftable_rolls = finding_rolls_for_removal(new_roll_matrix)
        df_l = pd.DataFrame(liftable_rolls)
        i = np.sum(liftable_rolls)
        liftable_rolls_count += i
        df_before = pd.DataFrame(new_roll_matrix)
        new_roll_matrix = new_roll_matrix - liftable_rolls
        df = pd.DataFrame(new_roll_matrix)
        a = 2




    print('Number of liftable rolls is: ' + str(liftable_rolls_count))
