const fs = require('fs')
str = fs.readFileSync('./result.txt', "utf8")
delim = "Paths"
start = str.indexOf(delim); disp(start);

rest = str.substring(start+3 + delim.length, str.length);
rest = rest.replace(/ +/g,'');
rest = rest.replace(/\t/g,'');
rest = rest.replace(/[)(]/g,',');
rest = rest.replace(/,,/g, ",");

all_paths = [];
lines = rest.split("\n");
lines.forEach(line => {
	if (line[0]==",") {
		line = line.substring(1, line.length)
	}
	if (line[line.length-1]==",") {
		line = line.substring(0, line.length-2)
	}
	// line = line.replace(/,,/g, ",");
	a_path = [];
	line = line.trim();
	coords = line.split(",");
	for (var i = 0; i < coords.length; i=i+2) {
		if (coords[i] != '') {
			a_path.push({lat: parseFloat(coords[i]), lng: parseFloat(coords[i+1])});
		}
	}
	all_paths.push(a_path);

	// disp(coords);
	// disp(line);
	// disp(a_path);
})
// disp(lines)
disp(all_paths);
// disp(rest);
function disp(a) {
	console.log(a)
}

// a = "73.94349890000001"
// console.log(parseFloat(a));
// "MinHeap@77e9807f\nTop Paths:\n(40.7852951, -73.9536046)       (40.7872835, -73.9521837)       (40.7893321, -73.943401)     (40.7887087, -73.943873)       \n(40.7852951, -73.9536046)       (40.7880822, -73.9443263)       (40.7870698, -73.9419167)    (40.787697, -73.9414658)        (40.7887087, -73.943873)       \n(40.7852951, -73.9536046)       (40.7735957, -73.9577825)       (40.787697, -73.9414658)     (40.7887087, -73.943873)       "