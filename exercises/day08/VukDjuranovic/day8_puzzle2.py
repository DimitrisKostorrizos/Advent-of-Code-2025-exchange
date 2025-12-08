import math

file_path = "input.txt"
file_lines = None
import numpy as np
from collections import deque, defaultdict


with open(file_path, 'r') as file:
    file_lines = file.readlines()

box_coords = list()

for line in file_lines:
    coords = tuple([int(c) for c in line.strip().split(",")])
    box_coords.append(coords)

#circuits = {i:c for i, c in enumerate(box_coords)}
circuits = [[c] for c in box_coords]
print(circuits)

def calc_distance(p, q):
    return math.sqrt(((p[0] - q[0])**2) + ((p[1] - q[1])**2) + ((p[2] - q[2])**2))

distances = defaultdict()
for i,box in enumerate(box_coords[:-1]):
    distances[i] = defaultdict()
    j = i + 1
    while j < len(box_coords):
        d = calc_distance(box_coords[i], box_coords[j])
        distances[i][j] = d
        j += 1

for i, di in enumerate(distances):
    distances[i] = sorted(distances[i].items(), key=lambda item: item[1])

a = 2


def find_circuit_position_for_a_box(box_co):
    pos = None
    for i, c in enumerate(circuits):
        for j, bbox in enumerate(c):
            if bbox == box_co:
                pos = i
                return pos



while len(circuits) > 1:
    min_d = math.inf
    f_point_pos = 0
    s_point_pos = 0
    for d in distances.items():
        if d[1][0][1] < min_d:
            min_d = d[1][0][1]
            f_point_pos = d[0]
            s_point_pos = d[1][0][0]
            a = 3

    # sad moram da ih dodam u circuit
    # find the circuit in which that point is
    circut_f = find_circuit_position_for_a_box(box_coords[f_point_pos])
    circuit_s = find_circuit_position_for_a_box(box_coords[s_point_pos])

    # moram izbrisati i tu distancu, kako bih mogao da nadjem sljedecu
    if circut_f != circuit_s:
        circuits[circut_f] = circuits[circut_f] + circuits[circuit_s]
        del circuits[circuit_s]
    del distances[f_point_pos][0]
    b = 1

# print(circuits)
# circuit_sizes = sorted([len(c) for c in circuits], reverse=True)
# print(circuit_sizes[0] * circuit_sizes[1] * circuit_sizes[2])
print(box_coords[f_point_pos][0] * box_coords[s_point_pos][0])
