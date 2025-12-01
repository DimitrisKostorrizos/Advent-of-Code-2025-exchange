#
# see: https://adventofcode.com/2025/day/1
# 
 
from time import time

__startTime = time()

def loadInput(isTest = True):
    global __startTime

    if isTest:
        filename = f"{baseDir}/input-example.txt"
    else:
        filename = f"{baseDir}/input.txt"

    with open(filename) as f:
        content = [line.strip() for line in f]
        
    return content


def writeSolutionFile(part, solution):
    filename = f"{baseDir}/solution-for-input.txt"
    parameter = "w" if part==1 else "a"

    with open(filename, parameter) as f:
        f.write(f"Part {part}: {solution}\n")


def printTimeTaken():
    global __startTime
    __endTime = time()
    print("Time: {:.3f}s".format(__endTime-__startTime))

print()

baseDir = "exercises/day01/Claudia"
#########################################################################################
# Day 01
#########################################################################################
DAY="01"

def doAllParts(part = 1, isTest = True):
    position = 50
    data = loadInput(isTest)
    cntData = len(data)
    #addInfo = ''
    result = 0

    #if isTest:
    #    print(f'- The dial starts by pointing at {position}.')

    for i in range(cntData):
        direction = data[i][0]
        distance = int(data[i][1:])
        
        if part == 2:
            result += int(data[i][1:]) // 100   # full rotations => including 0 

        distance = (int(data[i][1:]) % 100)
        
        if direction == "L":
            oldPosition = position
            position -= distance
            if position < 0:
                position += 100
                if part == 2 and oldPosition != 0:     # passed 0 during rotation
                    #addInfo = '; during this rotation, it points at 0 once' 
                    result += 1    
        else:
            oldPosition = position
            position += distance
            if position > 99:
                position -= 100
                if part == 2 and oldPosition != 0 and position != 0:    # passed 0 during rotation  
                    #addInfo = '; during this rotation, it points at 0 once'  
                    result += 1
        if position == 0:
            result += 1     # landed on 0

        #if isTest:
        #    print(f'- The dial is rotated {direction}{data[i][1:]} to point at {position}{addInfo}.') 
        #addInfo = ''
        
    if not isTest:
        writeSolutionFile(part, result)
  
    return result

#########################################################################################

print("--- PART 1 ---")
print(f"Solution Example: {doAllParts()}")
print(f"Solution Part 1:  {doAllParts(1, False)}")

print()

print("--- PART 2 ---")
print(f"Solution Example: {doAllParts(2)}")
print(f"Solution Part 1:  {doAllParts(2, False)}")

#########################################################################################
print()
printTimeTaken()
