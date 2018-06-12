/*

Wifi Irrigation Control for Particle Photon
Copyright 2018 Michael Greene
https://mike-greene.com/wifi-irrigation

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this 
file except in compliance with the License. You may obtain a copy of the License at:

http://www.apache.org/licenses/LICENSE-2.0
        
Unless required by applicable law or agreed to in writing, software distributed under 
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF 
ANY KIND, either express or implied. See the License for the specific language governing 
permissions and limitations under the License.

*/

metadata {
	definition (name: "wifi-irrigation", namespace: "webdes03", author: "Michael Greene") {
		capability "Switch"
		capability "Refresh"
		capability "Polling"
	}
	
	multiAttributeTile(name:"switch", type: "generic", width: 6, height: 4, canChangeIcon: true) {
		tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
			attributeState "unknown", label:'${name}', action:"refresh.refresh", icon:"st.Outdoor.outdoor12", backgroundColor:"#e5e5e5"
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
	input("deviceId", "text", title: "Device ID", required: true, displayDuringSetup: true) // particle device id
	input("authorizationToken", "text", title: "Authorization Token", required: true, displayDuringSetup: true) // particle authorization id
    input("relayNumber", "text", title: "Relay ID (1-4)", required: true, displayDuringSetup: true) // relay number (1-4) the valve is connected to
}

def installed() {
	log.debug "${device.name}: Installed"
	updated()
}

def updated() {
	unschedule()
	log.debug "${device.name}: Initializing"
}

def on() {
	log.info "${device.name}: Turning ON"
	sendParticleRelayCommand('relayOn')
}

def off() {
	log.info "${device.name}: Turning OFF"
	sendParticleRelayCommand('relayOff')
}

def refresh() {
	log.info "Refreshing ${device.name}"
	getParticleRelayStatus()
}

def getParticleRelayStatus() {
	def queryVariable = "valve${relayNumber}"
	log.info "Getting status of $queryVariable"
	def params = [
		uri: "https://api.particle.io/v1/devices/$deviceId/$queryVariable?access_token=$authorizationToken"
	]
	httpGet(params) { resp ->
		def status = "unknown"
		if (resp.data.result == 0) {
			status = "off"
		} else if (resp.data.result == 1) {
			status = "on"
		}
		log.info "${device.name}: Status: ${status}"
		sendEvent(name: "switch", value: status, isStateChange: true)
		runIn(300, getParticleRelayStatus)
	}
}

def sendParticleRelayCommand(command){
	def params = [
		uri: "https://api.particle.io/v1/devices/$deviceId/$command?access_token=$authorizationToken",
		body: [arg: relayNumber]
	]
	httpPostJson(params) { resp ->
        if (resp.data.return_value == 1) {
			getParticleRelayStatus()
		} else {
			sendEvent(name: "switch", value: "unknown", isStateChange: true)
		}
	}
}