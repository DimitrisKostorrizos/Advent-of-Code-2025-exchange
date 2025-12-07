import numpy as np
import pandas as pd

file_path = "input.txt"
file_lines = None

# not the fastest solution :D

with open(file_path, 'r') as file:
    file_lines = file.readlines()

def number_exists_in_column(t_table, col_j):
    for i in range(len(t_table)):
        if t_table[i][col_j] != ' ' and t_table[i][col_j] != '\n':
            return True

    return False

def parse_single_line(raw_line, raw_table):
    output = list()
    number_reached = False
    current_sub = ''
    for i in range(len(raw_line)):
        if (raw_line[i] == ' ' or raw_line[i] == '\n') and number_reached:
            if raw_line[i] == '\n' or not number_exists_in_column(raw_table, i):
                output.append(current_sub)
                current_sub = ''
                number_reached = False
            else:
                current_sub += raw_line[i]
        elif raw_line[i] == ' ' and not number_reached:
            current_sub += raw_line[i]
        #elif raw_line[i] != ' ' :
        else:
            number_reached = True
            current_sub += raw_line[i]

    return output


table_rows = list()
for line in file_lines[:-1]:
    table_rows.append(parse_single_line(line, file_lines[:-1]))

operations = [i for i in file_lines[-1].strip().split(' ') if i != '']
print(table_rows)


#def character_level
table_df = pd.DataFrame(np.array(table_rows))
grand_total = 0
problem_totals = list()
for c in range(len(table_rows[0])):
    j = len(table_rows[0][c]) - 1
    operation = operations[c]
    single_problem_total = 0 if operation == '+' else 1
    table_col = table_df.iloc[:,c]

    # lengths of column for last column do not math because of \n
    if c == len(table_rows[0]) - 1:
        max_length_last_col = 0
        for ii in table_col:
            if len(ii) > max_length_last_col:
                max_length_last_col = len(ii)
        n_table_col = [col_i.ljust(max_length_last_col) for col_i in table_col]
        j = max_length_last_col - 1
        table_col = n_table_col
    numbers = list()
    while j >= 0:
        i = 0
        number_str = ''
        while i < len(table_col):
            number_str += table_col[i][j]
            i += 1
        the_number = int(number_str)
        numbers.append(number_str)

        if operation == '+':
            single_problem_total += the_number
        else:
            single_problem_total *= the_number
        j -= 1
    print('used numbers are: ' + (' ' + operation + ' ').join(numbers))
    print(table_col)
    print('############')


    problem_totals.append(single_problem_total)


print(problem_totals)
print(np.sum(problem_totals))
