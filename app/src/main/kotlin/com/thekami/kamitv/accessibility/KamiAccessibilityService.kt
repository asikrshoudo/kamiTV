package com.thekami.kamitv.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.media.AudioManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class KamiAccessibilityService : AccessibilityService() {

    companion object {
        var instance: KamiAccessibilityService? = null
    }

    override fun onServiceConnected() {
        instance = this
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    // ── Global actions ────────────────────────────────────────────────────────

    fun doBack() = performGlobalAction(GLOBAL_ACTION_BACK)
    fun doHome() = performGlobalAction(GLOBAL_ACTION_HOME)
    fun doRecents() = performGlobalAction(GLOBAL_ACTION_RECENTS)

    // ── D-pad via key inject ──────────────────────────────────────────────────

    fun doKey(keyCode: Int) {
        val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val up   = KeyEvent(KeyEvent.ACTION_UP,   keyCode)
        dispatchKeyEventFromService(down)
        dispatchKeyEventFromService(up)
    }

    // ── Volume ────────────────────────────────────────────────────────────────

    fun volumeUp() {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
    }

    fun volumeDown() {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
    }

    fun volumeMute() {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI)
    }

    // ── Text inject via clipboard ─────────────────────────────────────────────

    fun injectText(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("kami", text))
        val node = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return
        val args = android.os.Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    fun doBackspace() {
        val node = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (node != null) {
            doKey(KeyEvent.KEYCODE_DEL)
        }
    }

    // ── Touchpad gesture ──────────────────────────────────────────────────────

    private var touchX = 960f
    private var touchY = 540f

    fun moveCursor(dx: Float, dy: Float) {
        touchX = (touchX + dx).coerceIn(0f, 1920f)
        touchY = (touchY + dy).coerceIn(0f, 1080f)
    }

    fun tapCursor() {
        val path = Path().apply { moveTo(touchX, touchY) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    fun swipe(fromX: Float, fromY: Float, toX: Float, toY: Float) {
        val path = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 300)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun dispatchKeyEventFromService(event: KeyEvent) {
        try {
            val im = Class.forName("android.hardware.input.InputManager")
                .getMethod("getInstance").invoke(null)
            im!!.javaClass.getMethod("injectInputEvent",
                android.view.InputEvent::class.java, Int::class.javaPrimitiveType)
                .invoke(im, event, 0)
        } catch (_: Exception) {
            // fallback: focus-based navigation
            val root = rootInActiveWindow ?: return
            val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
                ?: root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: return
            val action = when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP    -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                KeyEvent.KEYCODE_DPAD_DOWN  -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_DPAD_CENTER -> AccessibilityNodeInfo.ACTION_CLICK
                else -> return
            }
            focused.performAction(action)
        }
    }
}
