file_path = "input.txt"
file_lines = None



with open(file_path, 'r') as file:
    file_lines = file.readlines()

ranges = list()
available_ingridients = list()
blank_line_reached = False
considered_fresh_count = 0
considered_fresh = set()

for line in file_lines:
    if blank_line_reached:
        available_ingridients.append(int(line.strip()))

    if len(line.strip()) == 0:
        blank_line_reached = True

    if not blank_line_reached:
        rng = line.strip().split('-')
        ranges.append([int(i) for i in rng])

j = 0
sorted_ranges = sorted(ranges, key=lambda r_s: r_s[0])
ultimate_ranges = list()
a = 3
for r in sorted_ranges:
    ul_range_contained = None
    print('Range ' + str(j + 1) + ' out of ' + str(len(ranges)))



    if len(ultimate_ranges) > 0:
        for i, ur in enumerate(ultimate_ranges):
            if r[0] >= ur[0] and r[1] <= ur[1]:
                # this range is already within the ultimate, so we ignore it
                ul_range_contained = ur
                break
            if r[0] >= ur[0] and r[0] <= ur[1] and r[1] > ur[1]:
                # change the upper bound of the interval
                ultimate_ranges[i][1] = r[1]
                ul_range_contained = ultimate_ranges[i]
                break
    else:
        ultimate_ranges.append(r)
        ul_range_contained = r

    if ul_range_contained is None:
        ultimate_ranges.append(r)

    j += 1

j = 0
for ul_range in ultimate_ranges:
    print('Range ' + str(j + 1) + ' out of ' + str(len(ultimate_ranges)))
    considered_fresh_count += ul_range[1] - ul_range[0] + 1
    j +=1

print(ultimate_ranges)
print('Number of ingridients considered fresh is:' + str(considered_fresh_count))