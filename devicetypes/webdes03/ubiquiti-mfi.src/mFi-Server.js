//-------------------------------------------------------------------------------
var http = require('http')
var net = require('net')
var exec = require('child_process').exec
var server = http.createServer(onRequest)
var serverPort = '8083'  // Same is in various groovy files.
var scriptPath = '/home/pi/HomeAutomation/mFi/mfictrl.sh -l'
server.listen(serverPort)
console.log("mFi Server")

//-- For each request received from SmartThings. ----------------------------
function onRequest(request, response){
	console.log(" ")
	console.log(new Date())
	var callString = '/home/pi/HomeAutomation/mFi/mfictrl.sh -l -u ' + request.headers["username"] + ' -a ' + request.headers["password"] + ' -p ' + request.headers["outlet"] + ' ' + request.headers["deviceip"] + ' ' + request.headers["command"]
	console.log(callString)
	
	exec(callString, (err, stdout, stderr) => {
		console.log(stdout)
		response.setHeader("cmd-response", stdout.replace(/\r?\n?/g, ''))
		response.end()
	})

}
//-- Response to mFi Commands ---------------------------------------
function processRequest(request, response) {
	var command = request.headers["command"]
	var deviceIP = request.headers["device-ip"]
	console.log("Sending to IP address: " + deviceIP + " Command: " + command)
//--- Encrypt then send command to device and wait for response. -------
	return new Promise((resolve, reject) => {
		var socket = net.connect(9999, deviceIP)
		socket.setKeepAlive(false)
		socket.setTimeout(2000)  // 2 seconds.
   	 	socket.on('connect', () => {
  			socket.write(encrypt(command))
   	 	})
//-- Decrypt response (less header) then send to SmartThings. ------------------
		socket.on('data', (data) => {
			data = decrypt(data.slice(4)).toString('ascii')
			console.log("Command Response sent to SmartThings!")
			response.setHeader("cmd-response", data)
			response.end()
			socket.end()
			resolve(data)
//-- If a timeout, send a timeout indication to SmartThings. ------------------
		}).on('timeout', () => {
			socket.end()
			response.setHeader("cmd-response", '{"error":"TCP Timeout"}')
			response.end()
			reject('Device TCP timeout')
		}).on('error', (err) => {
			socket.end()
			reject(err)
		})
		
	})
//-- Encrypt the command including a 4 byte TCP header. -----------------------
	function encrypt(input) {
		var buf = Buffer.alloc(input.length)
		var key = 0xAB
		for (var i = 0; i < input.length; i++) {
			buf[i] = input.charCodeAt(i) ^ key
			key = buf[i]
		}
		var bufLength = Buffer.alloc(4)
		bufLength.writeUInt32BE(input.length, 0)
		return Buffer.concat([bufLength, buf], input.length + 4)
	}
//--- Decrypt the response. ---------------------------------------------------
	function decrypt(input, firstKey) {
		var buf = Buffer.from(input)
		var key = 0x2B
		var nextKey
		for (var i = 0; i < buf.length; i++) {
			nextKey = buf[i]
			buf[i] = buf[i] ^ key
			key = nextKey
		}
		return buf
	}
}
