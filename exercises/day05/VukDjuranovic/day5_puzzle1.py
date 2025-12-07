file_path = "input.txt"
file_lines = None



with open(file_path, 'r') as file:
    file_lines = file.readlines()

ranges = list()
available_ingridients = list()
blank_line_reached = False
fresh_ingridients = list()

for line in file_lines:
    if blank_line_reached:
        available_ingridients.append(int(line.strip()))

    if len(line.strip()) == 0:
        blank_line_reached = True

    if not blank_line_reached:
        range = line.strip().split('-')
        ranges.append([int(i) for i in range])

for avl_ing in available_ingridients:
    for r in ranges:
        if avl_ing >= r[0] and avl_ing <= r[1]:
            fresh_ingridients.append(avl_ing)
            break

print('Number of fresh ingridients is:' + str(len(fresh_ingridients)))