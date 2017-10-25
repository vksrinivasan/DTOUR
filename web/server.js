/*10.101.96.24*/

express = require('express')
bodyParser = require('body-parser')
path = require('path')
fs = require('fs')
http = require('http');
ejs = require('ejs');

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
		res.send(fs.readFileSync('./views/index.html', 'utf8'))
	}
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