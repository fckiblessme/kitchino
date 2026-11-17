package com.kitchino.app.auth

import org.mindrot.jbcrypt.BCrypt

object PinHasher {
    private const val COST = 10

    fun hash(pin : String) : String =
        BCrypt.hashpw(pin, BCrypt.gensalt(COST))

    fun verify(pin: String, hash: String): Boolean =
        BCrypt.checkpw(pin, hash)
}