file_path = "input.txt"
file_lines = None
import numpy as np



with open(file_path, 'r') as file:
    file_lines = file.readlines()

table_rows = list()
for line in file_lines[:-1]:
    table_rows.append([int(i) for i in line.strip().split(' ') if i != ''])

operations = [i for i in file_lines[-1].strip().split(' ') if i != '']


grand_total = 0
problem_totals = list()
problems_elements = list()
for c in range(len(table_rows[0])):
    i = len(table_rows) - 1
    operation = operations[c]
    single_problem_total = 0 if operation == '+' else 1
    while i >= 0:
        if operation == '+':
            single_problem_total += table_rows[i][c]
        else:
            single_problem_total *= table_rows[i][c]

        i -= 1
    problem_totals.append(single_problem_total)

print(np.sum(problem_totals))


#print(table_rows)