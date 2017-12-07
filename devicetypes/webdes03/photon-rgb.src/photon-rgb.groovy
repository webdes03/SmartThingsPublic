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
	12/06/2017	- Created initial implementation (Version 1.0)

*/

metadata {
	definition (name: "Particle Photon RGB", namespace: "webdes03", author: "Michael Greene") {
		capability "Switch"
		capability "Color Control"
		capability "refresh"
	}
	
	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
		tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
			attributeState "on", label:'${name}', action:"switch.off", icon:"st.Lighting.light18", backgroundColor:"#00a0dc", nextState:"turningOff"
			attributeState "off", label:'${name}', action:"switch.on", icon:"st.Lighting.light18", backgroundColor:"#ffffff", nextState:"turningOn"
			attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.Lighting.light18", backgroundColor:"#00a0dc", nextState:"on"
			attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.Lighting.light18", backgroundColor:"#ffffff", nextState:"off"
		}
		tileAttribute ("device.color", key: "COLOR_CONTROL") {
			attributeState "color", action:"color control.setColor"
		}
	}
	main("switch")
	details(["switch", "refresh"])
}

preferences {
	input("deviceId", "text", title: "Device ID", required: true, displayDuringSetup: true)
	input("authorizationToken", "text", title: "Authorization Token", required: true, displayDuringSetup: true)
}

def on() {
	log.info "${device.name} ${device.label}: Turning ON"
	sendParticleCommand('on')
}

def off() {
	log.info "${device.name} ${device.label}: Turning OFF"
	sendParticleCommand('off')
}

def refresh(){
	log.info "Polling ${device.name}"
	def params = [
		uri: "https://api.particle.io/v1/devices/$deviceId/status?access_token=$authorizationToken",
		body: [arg: $command]
	]
	httpPostJson(params) { resp ->
		def status = resp.data.return_value
		if (status == 1) {
			status = "on"
		} else if (status == 0) {
			status = "off"
		}
		log.info "${device.name} ${device.label}: Lights: ${status}"
		sendEvent(name: "switch", value: status, isStateChange: true)
	}
}

def setColor(value) {
	log.debug "setAdjustedColor: ${value}"
    
    def rgbString = String.format('%03d', value.red) + "," + String.format('%03d', value.green) + "," + String.format('%03d', value.blue);
	
    sendParticleColorCommand(rgbString, value.hex)
}

private sendParticleCommand(command){
	def params = [
		uri: "https://api.particle.io/v1/devices/$deviceId/$command?access_token=$authorizationToken",
		body: [arg: $command]
	]
	httpPostJson(params) { resp ->
        if (resp.data.return_value == 1) {
        	log.info "Call completed"
            refresh()
        } else {
        	log.warn "Error somewhere"
    	}
	}
}

private sendParticleColorCommand(rgb, hex){
	log.debug "set color: ${rgb}"
	def params = [
		uri: "https://api.particle.io/v1/devices/$deviceId/color?access_token=$authorizationToken",
		body: [arg: "${rgb}"],
        requestContentType: "application/x-www-form-urlencoded"
	]
	httpPostJson(params) { resp ->
        if (resp.data.return_value == 1) {
        	log.info "Call completed"
            sendEvent(name: "switch", value: "on", isStateChange: true)
            sendEvent(name: "color", value: hex, displayed: true)
        } else {
        	log.warn "Error somewhere"
    	}
	}
}