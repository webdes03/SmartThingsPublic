/*

Wifi Irrigation Control for Particle Photon
Copyright 2018 Michael Greene
mike-greene.com

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
file except in compliance with the License. You may obtain a copy of the License at:

		http://www.apache.org/licenses/LICENSE-2.0
        
Unless required by applicable law or agreed to in writing, software distributed under 
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.

Notes:
1.	This device type requires a Particle Photon device, and is intended for users
	that understand basic electronics and wiring. No warranty is included, use at your
	own risk.

Update History
	05/12/2018	- Created initial implementation (Version 1.0)

*/

metadata {
	definition (name: "wifi-irrigation", namespace: "webdes03", author: "Michael Greene") {
		capability "Switch"
		capability "refresh"
	}
	
	multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
		tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
			attributeState "off", label:'${name}', action:"switch.on", icon:"st.Outdoor.outdoor12", backgroundColor:"#ffffff", nextState:"turningOn"
			attributeState "on", label:'${name}', action:"switch.off", icon:"st.Outdoor.outdoor12", backgroundColor:"#47bd18", nextState:"turningOff"
			attributeState "turningOn", label:'${name}', action:"switch.on", icon:"st.Outdoor.outdoor12", backgroundColor:"#e5e5e5", nextState:"on"
			attributeState "turningOff", label:'${name}', action:"switch.off", icon:"st.Outdoor.outdoor12", backgroundColor:"#e5e5e5", nextState:"off"
		}		
	}
	standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
		state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
	}
	main("switch")
	details(["switch", "refresh"])
}

preferences {
	input("deviceId", "text", title: "Device ID", required: true, displayDuringSetup: true)
	input("authorizationToken", "text", title: "Authorization Token", required: true, displayDuringSetup: true)
    input("relayNumber", "text", title: "Relay ID (1-4)", required: true, displayDuringSetup: true)
}

def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendParticleRelayCommand('relayOn')
}

def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendParticleRelayCommand('relayOff')
}

def refresh(){
	log.info "Polling ${device.name} ${device.label}"
	getParticleRelayStatus()
}

private getParticleRelayStatus() {
	def queryVariable = "valve${relayNumber}"
	log.info "attempting to get status of ${queryVariable}"
	def params = [
		uri: "https://api.particle.io/v1/devices/$deviceId/${queryVariable}?access_token=$authorizationToken",
		body: null

	]
	httpPostJson(params) { resp ->
		def status = "off"
		if (resp.result == 1) {
			status = "on"
		}
		log.info "${device.name} ${device.label}: Status: ${status}"
		sendEvent(name: "switch", value: status, isStateChange: true)
	}
}

private sendParticleRelayCommand(command){
	def params = [
		uri: "https://api.particle.io/v1/devices/$deviceId/$command?access_token=$authorizationToken",
		body: [arg: relayNumber]
	]
	httpPostJson(params) { resp ->
        if (resp.data.return_value == 1) {
			getParticleRelayStatus()
		}
	}
}