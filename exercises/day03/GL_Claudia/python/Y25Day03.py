#
# see: https://adventofcode.com/2025/day/3
# 
 
from time import time
from itertools import combinations

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

baseDir = "exercises/day03/Claudia"
#########################################################################################
# Day 03
#########################################################################################
DAY="03" 

def doPart1(isTest = True):       # Code is only for Part 1 possible
    data = loadInput(isTest)
    result = 0
    #if isTest:
    #    for i in range(len(data)):
    #        print(data[i])
    #    print()
     
    cntDigits = 2
    
    for bank in data:
        maxJolt = 0
        allCombos = combinations(bank, cntDigits)
        for combo in allCombos:
            maxJolt = max(maxJolt, int(''.join(combo)))  
        result += maxJolt

    if not isTest:
        writeSolutionFile(1, result)
        
    return result



def calcMaxNumber(inputStr):
    x = None
    pos = -1

    for i, n in enumerate(inputStr):
        if not x or n > x:
            x = n
            pos = i

    return x, pos


def calcMaxJoltage(inputStr, cntDigits):
    jolt = ""
    pos = 0

    for i in range(1,cntDigits+1):
        x, px = calcMaxNumber(inputStr[pos:len(inputStr)-cntDigits+i])
        jolt += x
        pos += px + 1

    return int(jolt)


def doAllParts(part = 1, isTest = True):
    data = loadInput(isTest)
    result = 0
    #if isTest:
    #    for i in range(len(data)):
    #        print(data[i])
    #    print()
           
    if part == 1:
        cntDigits = 2
    else:
        cntDigits = 12
    
    for bank in data:
        result += calcMaxJoltage(bank, cntDigits)
    
    if not isTest:
        writeSolutionFile(part, result)
        
    return result


#########################################################################################

print("--- PART 1 ---")
#print(f"Solution Example: {doPart1()}")
#print(f"Solution Part 1:  {doPart1(False)}")
print(f"Solution Example: {doAllParts()}")
print(f"Solution Part 1:  {doAllParts(1, False)}")


print("\n==============\n")
print("--- PART 2 ---")
print(f"Solution Example: {doAllParts(2)}")
print(f"Solution Part 2:  {doAllParts(2, False)}")

#########################################################################################
print()
printTimeTaken()
