const http = require('http')
const fs = require('fs')
const PORT = 3001
const IP = "0.0.0.0"
var execute = require('child_process').exec;

const server = http.createServer()
server.listen(PORT, IP, () => {
	disp('Backend Server started');
})

const io = require('socket.io')(server)

io.sockets.on('connection', socket => {
	socket.on('query', info => {
		var source = info[0];
		var destination = info[1];
		var filename = "DTOurCode-all-1.0.jar";
		
		var to_execute = "java -jar " + filename + " "
		// was given by vyas: 40.7858826 -73.9488283 40.7887087 -73.943873
		/*	testing for:
			source: 40.785184, -73.953399 (Ossias A Lawrence MD, 1185 Park Ave, New York, NY 10128)
			destination: 40.7887087 -73.943873 (2000 2nd Ave, New York, NY 10029)
		*/
		to_execute += source["lat"] + " " + source["lng"] + " "
		to_execute += destination["lat"] + " " + destination["lng"]
		console.log("to_execute", to_execute);
		execute(to_execute, (error, stdout, stderr) => {
			if (error) {
				console.log("error while executing the command: ", to_execute, "\n", stderr);
				socket.emit("paths", []);
			} else {
				try {
					var str = stdout;
					var delim = "Paths"
					var start = str.indexOf(delim); // disp(start);

					var rest = str.substring(start+3 + delim.length, str.length);
					rest = rest.replace(/ +/g,'');
					rest = rest.replace(/\t/g,'');
					rest = rest.replace(/[)(]/g,',');
					rest = rest.replace(/,,/g, ",");

					var all_paths = [];
					var lines = rest.split("\n");
					lines.forEach(line => {
						if (line[0]==",") {
							line = line.substring(1, line.length)
						}
						if (line[line.length-1]==",") {
							line = line.substring(0, line.length-2)
						}
						line = line.replace(/,,/g, ",");
						line = line.trim();
						var coords = line.split(",");
						var a_path = [];
						for (var i = 0; i < coords.length; i=i+2) {
							if (coords[i] != '') {
								a_path.push({lat: parseFloat(coords[i]), lng: parseFloat(coords[i+1])});
							}
						}
						all_paths.push(a_path);
						disp(line);
						disp(a_path);
					})
					if (all_paths.length>=1) {
						socket.emit("paths", all_paths);
					} else {
						socket.emit("paths", []);
					}
					disp(all_paths);
				} catch (err) {
					socket.emit("paths", []);
				}
			}
		})
	})
})

function disp(a) {
	console.log(a);
}

/*var paths_lines = lines.splice(1, num_lines);
var all_paths = [];
paths_lines.forEach(path_line => {
	var a_path = [];
	coords = path_line.split(",");
	for (var i = 0; i < coords.length; i=i+2) {
		a_path.push({lat: coords[i], lng: coords[i+1]});
	}
	all_paths.push(a_path);
})*/

/*// catch output from stdout, parse it and send to the client
				disp(stdout);
				stdout = stdout.trim();
				var lines = stdout.split(" ");
				var is_output_correct = true;
				var num_lines = lines.length;
				if (num_lines < 1) {
					is_output_correct = false;
					disp("num_lines < 1");
				} else {
					var num_paths = parseInt(lines[0]);
					if (num_lines != num_paths + 1) {
						is_output_correct = false;
						console.log("num_lines =", num_lines, "num_paths =", num_paths);
					}
				}
				if (!is_output_correct) {
					// if empty array is being sent, just show the default Google maps path to the user
					socket.emit("paths", []);
				} else {*/