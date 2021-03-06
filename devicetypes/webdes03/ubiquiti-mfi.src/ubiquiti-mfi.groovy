metadata {
	definition (name: "Ubiquiti_mFi", namespace: "webdes03", author: "Michael Greene") {
		capability "Switch"
		capability "refresh"
	}
	tiles {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#00a0dc",
				nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff",
				nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#e86d13",
				nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#e86d13",
				nextState:"turningOn"
                attributeState "offline", label:'Bulb Offline', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"##e86d13",
                nextState:"turningOn"				
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
		}         
		main("switch")
		details(["switch", "refresh"])
    }
}
preferences {
	input("deviceIP", "text", title: "Device IP", required: true, displayDuringSetup: true)
	input("gatewayIP", "text", title: "Gateway IP", required: true, displayDuringSetup: true)
	input("outletNumber", "text", title: "Outlet Number", required: true, displayDuringSetup: true)
	input("authUsername", "text", title: "Username", required: true, displayDuringSetup: true)
	input("authPassword", "password", title: "Password", required: true, displayDuringSetup: true)
}
def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendCmdtoServer('ON', "hubActionResponse")
}
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('OFF', "hubActionResponse")
}
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('STATUS', "hubActionResponse")
}
private sendCmdtoServer(command, action){
	def headers = [:]
	headers.put("HOST", "$gatewayIP:8083")   // port 8083 must be same as value in mFi-Server.js
	headers.put("deviceip", deviceIP)
	headers.put("outlet", outletNumber)
	headers.put("username", authUsername)
	headers.put("password", authPassword)
	headers.put("command", command)
	sendHubCommand(new physicalgraph.device.HubAction(
		[headers: headers],
 		device.deviceNetworkId,
 		[callback: action]
	))
}
def onOffResponse(response){
	log.info "On/Off command response received from server!"
	refresh()
}
def hubActionResponse(response){
	def status = "off"
	if (response.headers["cmd-response"].trim() == "ON") {
		status = "on"
	}
	log.info "${device.name} ${device.label}: Power: ${status}"
	sendEvent(name: "switch", value: status, isStateChange: true)
}