/*10.101.96.24*/

express = require('express')
bodyParser = require('body-parser')
path = require('path')
fs = require('fs')
http = require('http');
ejs = require('ejs');
var execute = require('child_process').exec;

// Globals
PORT = 3000
PORT = process.env.PORT || 3000; // this line added so port binds to heroku's port

IP = "0.0.0.0"
app = express()
static_dir = 'public'

server = http.createServer(app);
io = require('socket.io').listen(server);
server.listen(PORT, IP, () => {
	disp('Server started')
})

// View Engine
app.set('view engine', 'ejs')
app.set('views', path.join(__dirname, 'views'))

// Body Parser middleware
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({extended: false}))

// Set static path
app.use(express.static(path.join(__dirname, static_dir)))

app.get('*', (req, res) => {
	if (req.url==='/') {
		res.send(fs.readFileSync('./views/index7_popup.html', 'utf8'))
	}
})


// backend stuff
// was given by vyas: 40.7858826 -73.9488283 40.7887087 -73.943873
/*	testing for:
	source: 40.785184, -73.953399 (Ossias A Lawrence MD, 1185 Park Ave, New York, NY 10128)
	destination: 40.7887087 -73.943873 (2000 2nd Ave, New York, NY 10029)
*/
io.sockets.on('connection', socket => {
	socket.on('query', info => {
		var source = info[0];
		var destination = info[1];
		var filepath = "DTOurCode-all-1.0.jar";
		
		var to_execute = "java -jar " + filepath + " "
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
						// disp(line);
						// disp(a_path);
					})
					if (all_paths.length>=1) {
						socket.emit("paths", all_paths);
					} else {
						socket.emit("paths", []);
					}
					// disp(all_paths);
				} catch (err) {
					socket.emit("paths", []);
				}
			}
		})
	})
})

function disp(to_print) {
	console.log(to_print)
}

// function readFilePromisified(f_name) {
// 	return new Promise(
// 		function (resolve, reject) {
// 			fs.readFile(f_name, 'utf8', (error, data) => {
// 				if (error) {
// 					reject(error);
// 				} else {
// 					resolve(data);
// 				}
// 			})
// 		})
// }