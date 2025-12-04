const fs = require("node:fs");

function findInvalidIDs(range) {
  let sum = 0;

  const [start, end] = range.split("-").map(Number);

  const minLen = start.toString().length;
  const maxLen = end.toString().length;

  for (let len = minLen; len <= maxLen; len++) {
    if (len % 2 !== 0) continue; // skip odd lengths

    const halfLen = len / 2;
    const minHalf = Math.pow(10, halfLen - 1);
    const maxHalf = Math.pow(10, halfLen) - 1;

    for (let halfNum = minHalf; halfNum <= maxHalf; halfNum++) {
      const fullStr = halfNum.toString() + halfNum.toString();
      const fullNum = Number(fullStr);

      if (fullNum >= start && fullNum <= end) {
        sum += fullNum;
      }
    }
  }

  return sum;
}

try {
  const fileData = fs.readFileSync("./input.txt", "utf-8");
  const areas = fileData.split(",");

  let resultSum = 0;
  for (let i = 0; i < areas.length; i++) {
    resultSum += findInvalidIDs(areas[i]);
  }
  console.log(resultSum);
} catch (e) {
  console.error(e);
}
