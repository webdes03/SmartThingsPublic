/*

RGB Device Control for Particle Photon
Copyright 2017 Michael Greene

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
file except in compliance with the License. You may obtain a copy of the License at:

		http://www.apache.org/licenses/LICENSE-2.0
        
Unless required by applicable law or agreed to in writing, software distributed under 
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.

Notes:
1.	This device handler requires a Particle Photon device, and is intended for users
	that understand basic electronics and wiring. No warranty is included, use at your
	own risk.

Update History
	12/06/2017	- Created initial rendition.  Version 1.0

*/
metadata {
	definition (name: "Particle Photon RGB", namespace: "webdes03", author: "Michael Greene") {
		capability "Switch"
		capability "Color Control"
		command "setAdjustedColor"
	}
	tiles {
		controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
			state "color", action:"setAdjustedColor"
		}
		standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
        	state "on", label:'${name}', action:"switch.off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#79b821", nextState:"turningOff"
            state "off", label:'${name}', action:"switch.on", icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff", nextState:"turningOn"
            state "turningOn", label:'${name}', icon:"st.illuminance.illuminance.bright", backgroundColor:"#79b821"
            state "turningOff", label:'${name}', icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff"
		}
    }
}
preferences {
	input("deviceId", "text", title: "Device ID", required: true, displayDuringSetup: true)
	input("authorizationToken", "text", title: "Authorization Token", required: true, displayDuringSetup: true)
}
def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendParticleCommand('on', "onOffResponse")
}
def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendCmdtoServer('off', "onOffResponse")
}
def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "hubActionResponse")
}
private sendParticleCommand(command, action){
	sendHubCommand(new physicalgraph.device.HubAction(
		method: "POST",
		path: "https://api.particle.io/v1/devices/$deviceId/color?access_token=$authorizationToken",
 		query: [arg: "$command"],
 		[callback: action]
	))
}
def onOffResponse(response){
	log.info "On/Off command response received from server!"
	//refresh()
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