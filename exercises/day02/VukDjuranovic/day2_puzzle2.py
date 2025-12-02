import numpy as np

file_path = "input.txt"
file_lines = None

invalid_ids = list()


def find_invalid_ids_in_range(r):
    new_invalid_ids = list()
    r_lower_bound = int(r.split('-')[0])
    r_upper_bound = int(r.split('-')[1])

    i = r_lower_bound
    while i <= r_upper_bound:
        # now we check differently.
        # we want to find all divisors that can split the ID in substrings of equal length
        i_str = str(i)
        # since I couldn't find a string that is longer than 12 characters, I set the script to
        # find repeatable substrings that have length at most 6. (we need only one repetion of substring
        # so that product id is invalid).
        for j in range(6):
            k = j + 1
            sep_point = int(len(i_str) % k) # k tells us about the length of each substrings
            # and sep_point tells us if string is divisible into ubstrings of length k
            if sep_point == 0:
                substrings = split_string_into_substrings_of_equal_length(i_str, k)
                if check_all_items_are_same(substrings):
                    new_invalid_ids.append(i)
                    break

        i += 1

    return new_invalid_ids

def split_string_into_substrings_of_equal_length(the_string, k):
    # as the name says, this method splits the string into substrings of same length
    # for example, if string is 101010 and k=2, it will split it into [10, 10, 10]
    sbs = list()
    i = 0
    while i + k <= len(the_string):
        if i + k < len(the_string):
            s = the_string[i: i+k]
            sbs.append(s)
        else:
            s = the_string[i:]
            sbs.append(s)
        i += k

    return sbs

def check_all_items_are_same(arr):
    # returns true if all elements of the array are the same, and false otherwise.
    check = True
    first_item = arr[0]
    i = 1

    if len(arr) == 1:
        return False

    while i < len(arr):
        if arr[i] != first_item:
            return False
        i += 1

    return True


try:
    with open(file_path, 'r') as file:
        file_lines = file.readlines()

        line = file_lines[0]
        # get ranges
        ranges = line.split(',')
        for r in ranges:
            r_lower_bound = int(r.split('-')[0])
            r_upper_bound = int(r.split('-')[1])

            invalid_ids = invalid_ids + find_invalid_ids_in_range(r)

except Exception as e:
    print(f"An error occurred: {e}")


print("Sum of invalid ids is: " + str(np.sum(invalid_ids)))