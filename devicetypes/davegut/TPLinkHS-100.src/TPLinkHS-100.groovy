/*
Forked from https://github.com/DaveGut/Cloud-Based_TP-Link-to-SmartThings-Integration/tree/master/Device%20Handlers

TP-Link Plugs and Switches Version 2.0

Copyright 2017 Dave Gutheinz

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
file except in compliance with the License. You may obtain a copy of the License at:

		http://www.apache.org/licenses/LICENSE-2.0
        
Unless required by applicable law or agreed to in writing, software distributed under 
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.

Notes: 
1.	This Device Handler requires an operating Windows 10 PC (server) that interfaces 
	to the TP-Link Bulbs.
2.	This DH will work only with the TP-LinkServer.js server node version 2.0.
3.	This handler is for the TP-Link Plugs and Switches bulbs.  This handler support the 
	Plug and Switch On/Off.

Update History
	03/12/2017	- Created initial rendition.  Version 1.0
	04/08/2017	- Version 2.0.  Added plug/switch TCP timeout (i.e., not connected to 
    		      network at IP.  Rearranged some functions.  Added color coding to
			      indicate switch/plug is turning on or off.  Added some notes.	

*/
metadata {
	definition (name: "TP-Link_HS100_Switch", namespace: "V2.0", author: "Dave Gutheinz") {
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
}
def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendCmdtoServer('{"system":{"set_relay_state":{"state": 1}}}', "onOffResponse")
}
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('{"system":{"set_relay_state":{"state": 0}}}', "onOffResponse")
}
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "hubActionResponse")
}
private sendCmdtoServer(command, action){
	def headers = [:]
	headers.put("HOST", "$gatewayIP:8082")   // port 8082 must be same as value in TP-LInkServer.js
	headers.put("tplink-iot-ip", deviceIP)
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
	def cmdResponse = parseJson(response.headers["cmd-response"])
	if (cmdResponse.error == "TCP Timeout") {
		log.error "$device.name $device.label: $cmdResponse.error"
 		sendEvent(name: "switch", value: "offline", isStateChange: true)
	} else {
		def status = cmdResponse.system.get_sysinfo.relay_state
		if (status == 1) {
			status = "on"
		} else {
   	     status = "off"
		}
		log.info "${device.name} ${device.label}: Power: ${status}"
		sendEvent(name: "switch", value: status, isStateChange: true)
	}
}