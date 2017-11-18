const http = require('http')
const fs = require('fs')
const PORT = 3001
const IP = "0.0.0.0"
var execute = require('child_process').exec;

const server = http.createServer()
server.listen(PORT, IP, () => {
	disp('Server started');
})

const io = require('socket.io')(server)

io.sockets.on('connection', socket => {
	socket.on('query', info => {
		var source = info[0];
		var destination = info[1];
		var filename = "RUNME.jar";
		
		var to_execute = "java -jar " + filename + " "
		to_execute += source["lat"] + " " + source["lng"] + " "
		to_execute += destination["lat"] + " " + destination["lng"]
		execute(to_execute, (error, stdout, stderr) => {
			if (error) {
				console.log("error while executing the command: ", to_execute, "\n", stderr);
			} else {
				// catch output from stdout, parse it and send to the client
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
				} else {
					var paths_lines = lines.splice(1, num_lines);
					var all_paths = [];
					paths_lines.forEach(path_line => {
						var a_path = [];
						coords = path_line.split(",");
						for (var i = 0; i < coords.length; i=i+2) {
							a_path.push({lat: coords[i], lng: coords[i+1]});
						}
						all_paths.push(a_path);
					})
					socket.emit("paths", all_paths);
				}
			}
		})
	})
})

function disp(a) {
	console.log(a);
}