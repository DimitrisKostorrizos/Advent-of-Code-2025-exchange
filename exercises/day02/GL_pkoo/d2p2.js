const fs = require("node:fs");

function checkId(id) {
  const idString = String(id);
  if (idString.length < 2) return 0;
  const idArray = idString.split("");

  const returnSet = new Set();
  const halfLen = idString.length / 2;

  let searchString = "";
  for (let i = 0; i < halfLen; i++) {
    searchString += idArray.shift();

    const tmpArr = [];
    for (let j = 0; j < idString.length; j += searchString.length) {
      const slicedText = idString.slice(j, j + searchString.length);
      tmpArr.push(slicedText);
    }
    if (tmpArr.every((v) => v === searchString)) {
      returnSet.add(Number(idString));
    }
  }
  if (returnSet.size > 0) {
    let returnSum = 0;
    returnSet.forEach((v1) => {
      returnSum += Number(v1);
    });
    return returnSum;
  }
  return 0;
}

try {
  const fileData = fs.readFileSync("./input.txt", "utf-8");
  const areas = fileData.split(",");

  let resultSum = 0;
  for (let i = 0; i < areas.length; i++) {
    const [start, end] = areas[i].split("-").map(Number);
    for (let currentNum = start; currentNum <= end; currentNum++) {
      resultSum += Number(checkId(currentNum));
    }
  }
  console.log(`result: ${resultSum}`);
} catch (e) {
  console.error(e);
}
