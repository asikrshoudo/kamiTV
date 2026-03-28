package com.thekami.kamitv.server

import kotlin.random.Random

object PinManager {
    private var currentPin: String = generate()

    fun getPin(): String = currentPin
    fun regenerate(): String { currentPin = generate(); return currentPin }
    fun verify(pin: String): Boolean = pin.trim() == currentPin
    private fun generate(): String = Random.nextInt(100_000, 1_000_000).toString()
}
