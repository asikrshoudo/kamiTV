package com.thekami.kamitv.server

import android.view.KeyEvent
import com.thekami.kamitv.accessibility.KamiAccessibilityService
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

object CommandDispatcher {

    fun dispatch(type: String, payload: JsonObject) {
        val svc = KamiAccessibilityService.instance
        when (type) {
            "key" -> {
                val key = payload["key"]?.jsonPrimitive?.contentOrNull ?: return
                when (key) {
                    "up"     -> svc?.doKey(KeyEvent.KEYCODE_DPAD_UP)
                    "down"   -> svc?.doKey(KeyEvent.KEYCODE_DPAD_DOWN)
                    "left"   -> svc?.doKey(KeyEvent.KEYCODE_DPAD_LEFT)
                    "right"  -> svc?.doKey(KeyEvent.KEYCODE_DPAD_RIGHT)
                    "select" -> svc?.doKey(KeyEvent.KEYCODE_DPAD_CENTER)
                    "back"   -> svc?.doBack()
                    "home"   -> svc?.doHome()
                    "recents"-> svc?.doRecents()
                    "vol_up"   -> svc?.volumeUp()
                    "vol_down" -> svc?.volumeDown()
                    "mute"     -> svc?.volumeMute()
                }
            }
            "text" -> {
                val text = payload["text"]?.jsonPrimitive?.contentOrNull ?: return
                svc?.injectText(text)
            }
            "backspace" -> svc?.doBackspace()
            "touch" -> {
                val dx = payload["dx"]?.jsonPrimitive?.doubleOrNull?.toFloat() ?: 0f
                val dy = payload["dy"]?.jsonPrimitive?.doubleOrNull?.toFloat() ?: 0f
                svc?.moveCursor(dx, dy)
            }
            "tap" -> svc?.tapCursor()
        }
    }
}
