package com.chemscanner.omniscient.marrow.services

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * THE SOVEREIGN LICENSE GENERATOR & VERIFIER v1.0.
 * Uses SHA-256 Hashing to validate unique client keys without a server.
 */
@Singleton
class MarrowLicenseManager @Inject constructor() {

    // THE ARCHITECT'S SECRET SEED (Never share this string!)
    private val ARCHITECT_SECRET = "XILON_QUANTUM_CORE_PRO_SECRET_777"

    /**
     * VERIFY: Validates if a provided key belongs to a specific client.
     * Logic: Key must match SHA-256(ClientName + ArchitectSecret)
     */
    fun verifyLicense(clientName: String, providedKey: String): Boolean {
        val expectedHash = generateHash(clientName.uppercase())
        return providedKey == expectedHash
    }

    /**
     * GENERATE: Used by Xilon to create keys for his clients.
     * (Normally this would be in a separate offline tool, but kept here for Marrow integrity).
     */
    fun generateKeyForClient(clientName: String): String {
        return generateHash(clientName.uppercase())
    }

    private fun generateHash(input: String): String {
        val data = input + ARCHITECT_SECRET
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
            .take(16) // We take first 16 chars for a clean industrial serial key
            .uppercase()
    }
}
