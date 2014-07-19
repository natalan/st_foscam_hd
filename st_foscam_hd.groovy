/**
 *  Foscam HD
 *  Modified: Andrei Zharov :: removed unnecessary for me functionality
 *  Author: skp19
 *  Date: 6/18/14
 *
 *  Example device type for HD Foscam cameras.
 *  Code based off of Foscam device type by brian@bevey.org.
 *  Heavily modified to work with the Foscam HD cameras.
 *
 *  This device has the following functions:
 *    - Take a snapshot
 *    - Toggle the infrared lights
 *    - Enable/Disable motion alarm
 *    - Control PTZ
 *    - Reboot
 *
 *  Capability: Image Capture, Polling
 *  Custom Attributes: setStatus, alarmStatus, ledStatus
 *  Custom Commands: alarmOn, alarmOff, toggleAlarm, left, right, up, down,
 *                   stop, set, toggleLED, ledOn, ledOff, ledAuto
 */

preferences {
  input("username", "text",        title: "Username",                description: "Your Foscam camera username")
  input("password", "password",    title: "Password",                description: "Your Foscam camera password")
  input("ip",       "text",        title: "IP address/Hostname",     description: "The IP address or hostname of your Foscam camera")
  input("port",     "text",        title: "Port",                    description: "The port of your Foscam camera")
}

metadata {
  definition (name: "Foscam HD") {
    capability "Polling"
    capability "Image Capture"

    attribute "setStatus",   "string"
    attribute "alarmStatus", "string"
	attribute "ledStatus",   "string"

    command "alarmOn"
    command "alarmOff"
    command "toggleAlarm"
    command "left"
    command "right"
    command "up"
    command "down"
    command "stop"
    command "toggleLED"
    command "ledOn"
    command "ledOff"
    command "ledAuto"
    command "reboot"
  }

  tiles {
    carouselTile("cameraDetails", "device.image", width: 3, height: 2) { }

	standardTile("foscam", "device.alarmStatus", width: 1, height: 1, canChangeIcon: true, inactiveLabel: true, canChangeBackground: false) {
      state "off", label: "off", action: "toggleAlarm", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
      state "on", label: "on", action: "toggleAlarm", icon: "st.camera.dropcam-centered",  backgroundColor: "#53A7C0"
    }
	
    standardTile("camera", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
      state "default", label: "", action: "Image Capture.take", icon: "st.camera.dropcam-centered", backgroundColor: "#FFFFFF"
    }

    standardTile("take", "device.image", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat") {
      state "take", label: "", action: "Image Capture.take", icon: "st.secondary.take", nextState:"taking"
    }

    standardTile("up", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "up", label: "up", action: "up", icon: "st.thermostat.thermostat-up"
    }

    standardTile("alarmStatus", "device.alarmStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
      state "off", label: "off", action: "toggleAlarm", icon: "st.quirky.spotter.quirky-spotter-sound-off", backgroundColor: "#FFFFFF"
      state "on", label: "on", action: "toggleAlarm", icon: "st.quirky.spotter.quirky-spotter-sound-on",  backgroundColor: "#53A7C0"
    }
	
    standardTile("left", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "left", label: "left", action: "left", icon: ""
    }

    standardTile("stop", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "stop", label: "", action: "stop", icon: "st.sonos.stop-btn"
    }
    
    standardTile("right", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "right", label: "right", action: "right", icon: ""
    }

    standardTile("blank", "device.image", width: 1, height: 1, canChangeIcon: false,  canChangeBackground: false, decoration: "flat") {
      state "blank", label: "", action: "stop", icon: "", backgroundColor: "#53A7C0"
    }

    standardTile("down", "device.image", width: 1, height: 1, canChangeIcon: false, canChangeBackground: false, decoration: "flat") {
      state "down", label: "down", action: "down", icon: "st.thermostat.thermostat-down"
    }

	
    standardTile("refresh", "device.alarmStatus", inactiveLabel: false, decoration: "flat") {
      state "refresh", action:"polling.poll", icon:"st.secondary.refresh"
    }

    standardTile("ledStatus", "device.ledStatus", width: 1, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
      state "auto", label: "auto", action: "toggleLED", icon: "st.Lighting.light13", backgroundColor: "#53A7C0"
	  state "off", label: "off", action: "toggleLED", icon: "st.Lighting.light13", backgroundColor: "#FFFFFF"
      state "on", label: "on", action: "toggleLED", icon: "st.Lighting.light11", backgroundColor: "#FFFF00"
	  state "manual", label: "manual", action: "toggleLED", icon: "st.Lighting.light13", backgroundColor: "#FFFF00"
    }
	
    standardTile("reboot", "device.image", inactiveLabel: false, decoration: "flat") {
      state "reboot", label: "reboot", action: "reboot", icon: "st.Health & Wellness.health8"
    }
	
    main "foscam"
      details(["cameraDetails", "take", "ledStatus", "alarmStatus", "up", "left", "stop", "right", "blank", "down", ,"blank", "refresh", "set", "reboot"])
  }
}

private getPictureName() {
  def pictureUuid = java.util.UUID.randomUUID().toString().replaceAll('-', '')
  "image" + "_$pictureUuid" + ".jpg"
}

def take() {
  log.debug("Take a photo")

  api("snapshot", "") {
    log.debug("Image captured")

    if(it.headers.'Content-Type'.contains("image/jpeg")) {
      if(it.data) {
        storeImage(getPictureName(), it.data)
      }
    }
  }
}

def toggleAlarm() {
  if(device.currentValue("alarmStatus") == "on") {
    alarmOff()
  }

  else {
    alarmOn()
  }
}

def alarmOn() {
  api("set_alarm", "isEnable=1") {
    log.debug("Alarm changed to: on")
    sendEvent(name: "alarmStatus", value: "on");
  }
}

def alarmOff() {
  api("set_alarm", "isEnable=0") {
    log.debug("Alarm changed to: off")
    sendEvent(name: "alarmStatus", value: "off");
  }
}

def left() {
  api("decoder_control", "cmd=ptzMoveLeft") {
    log.debug("Executing 'LEFT'")
  }
  stop()
}

def right() {
  api("decoder_control", "cmd=ptzMoveRight") {
    log.debug("Executing 'RIGHT'")
  }
  stop()
}

def up() {
  api("decoder_control", "cmd=ptzMoveUp") {
    log.debug("Executing 'UP'")
  }
  stop()
}

def down() {
  api("decoder_control", "cmd=ptzMoveDown") {
    log.debug("Executing 'DOWN'")
  }
  stop()
}

def stop() {
  api("decoder_control", "cmd=ptzStopRun") {
    log.debug("Executing 'STOP'")
  }
}

//Toggle LED's
def toggleLED() {
  log.debug("Toggle LED")

  if(device.currentValue("ledStatus") == "auto") {
    ledOn()
  }

  else if(device.currentValue("ledStatus") == "on") {
    ledOff()
  }
  
  else {
    ledAuto()
  }
}

def ledOn() {
  api("decoder_control", "cmd=setInfraLedConfig&mode=1") {}
  api("decoder_control", "cmd=openInfraLed") {
    log.debug("LED changed to: on")
    sendEvent(name: "ledStatus", value: "on");
  }
}

def ledOff() {
  api("decoder_control", "cmd=setInfraLedConfig&mode=1") {}
  api("decoder_control", "cmd=closeInfraLed") {
    log.debug("LED changed to: off")
    sendEvent(name: "ledStatus", value: "off");
  }
}

def ledAuto() {
  api("decoder_control", "cmd=setInfraLedConfig&mode=0") {
    log.debug("LED changed to: auto")
    sendEvent(name: "ledStatus", value: "auto");
  }
}

def reboot() {
  api("reboot", "") {
    log.debug("Rebooting")
  }
}

def api(method, args = [], success = {}) {
  def methods = [
    "decoder_control": [uri: "http://${ip}:${port}/cgi-bin/CGIProxy.fcgi${login()}&${args}",                                type: "get"],
    "snapshot":        [uri: "http://${ip}:${port}/cgi-bin/CGIProxy.fcgi${login()}&cmd=snapPicture2",                       type: "get"],
    "set_alarm":       [uri: "http://${ip}:${port}/cgi-bin/CGIProxy.fcgi${login()}&cmd=setMotionDetectConfig&${args}",      type: "get"],
    "reboot":          [uri: "http://${ip}:${port}/cgi-bin/CGIProxy.fcgi${login()}&cmd=rebootSystem",                       type: "get"],
    "camera_control":  [uri: "http://${ip}:${port}/camera_control.cgi${login()}&${args}",                                   type: "get"],
    "get_params":      [uri: "http://${ip}:${port}/cgi-bin/CGIProxy.fcgi${login()}&${args}",                                type: "get"]
  ]

  def request = methods.getAt(method)

  doRequest(request.uri, request.type, success)
}

private doRequest(uri, type, success) {
  log.debug(uri)
  httpGet(uri, success)
}

private login() {
  return "?usr=${username}&pwd=${password}"
}

def poll() {
  //Poll Motion Alarm Status
  api("get_params", "cmd=getMotionDetectConfig") {
    def CGI_Result = new XmlParser().parse(it.data)
	def motionAlarm = CGI_Result.isEnable.text()

	if(motionAlarm == "0") {
      log.info("Polled: Alarm Off")
      sendEvent(name: "alarmStatus", value: "off");
    }

    if(motionAlarm == "1") {
        log.info("Polled: Alarm On")
        sendEvent(name: "alarmStatus", value: "on");
	}
  }
  
  //Poll IR LED Mode
  api("get_params", "cmd=getInfraLedConfig") {
    def CGI_Result = new XmlParser().parse(it.data)
	def ledMode = CGI_Result.mode.text()
  
    if(ledMode == "0") {
      log.info("Polled: LED Mode Auto")
      sendEvent(name: "ledStatus", value: "auto");
    }

    if(ledMode == "1") {
      log.info("Polled: LED Mode Manual")
      sendEvent(name: "ledStatus", value: "manual");
    }
  }
  
  //Reset 
}
