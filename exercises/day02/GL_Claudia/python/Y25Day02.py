#
# see: https://adventofcode.com/2025/day/2
# 
 
from time import time
from collections import Counter

__startTime = time()

def loadInput(isTest = True):
    global __startTime

    if isTest:
        filename = f"{baseDir}/input-example.txt"
    else:
        filename = f"{baseDir}/input.txt"

    with open(filename) as f:
        content = [line.strip() for line in f][0].split(',')
        
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

baseDir = "exercises/day02/Claudia"
#########################################################################################
# Day 02
#########################################################################################
DAY="02" 

def invalidID(n,part):
    n = str(n)

    minCntSplit = 2
    if part==1:
        maxCntSplit=2
    else:
        maxCntSplit=len(n)

    for cntSplit in range(minCntSplit, maxCntSplit+1):
        if len(n)%cntSplit==0:
            isInvalid = True
            lenSplit = len(n)//cntSplit
            firstSplit = n[:lenSplit]
            i = lenSplit
            while i < len(n):
                if n[i:i+lenSplit] != firstSplit:
                    isInvalid = False
                i += lenSplit
            if isInvalid:
                return True
    return False


def doAllParts(part = 1, isTest = True):
    data = loadInput(isTest)
    cntData = len(data)
    result = 0
    #if isTest:
    #    for i in range(len(data)):
    #        print(data[i])
    #    print()
    for i in range(cntData):
        first,last = data[i].split('-')
        first = int(first)
        last = int(last)
        for n in range(first, last+1):
            if invalidID(n, part):
                result += n
    
    if not isTest:
        writeSolutionFile(part, result)
        
    return result

#########################################################################################

print("--- PART 1 ---")
print(f"Solution Example: {doAllParts()}")
print(f"Solution Part 1:  {doAllParts(1, False)}")


print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {doAllParts(2)}")
print(f"Solution Part 2:  {doAllParts(2, False)}")

#########################################################################################
print()
printTimeTaken()
