package com.chemscanner.omniscient.utils

import java.security.MessageDigest

/**
 * THE XILON KEY GENERATOR v1.0.
 * UTILITY TOOL FOR THE ARCHITECT.
 * 
 * Cum se foloseste:
 * 1. Deschide acest fisier in Android Studio.
 * 2. Click dreapta oriunde in cod si alege "Run 'XilonKeyGeneratorKt'".
 * 3. Rezultatul va aparea in consola de jos (Run tab).
 */

fun main() {
    // ACESTA ESTE SECRETUL TAU - NU IL MODIFICA
    val ARCHITECT_SECRET = "XILON_QUANTUM_CORE_PRO_SECRET_777"

    // ADAUGA AICI NUMELE CLIENTULUI NOU (CU LITERE MARI)
    val noiiClienti = listOf("SAMSUNG", "CERN", "SONY", "HUAWEI")

    println("\n==================================================")
    println("   SCI-OS MARROW: GENERATOR LICENTE SUVERANE")
    println("==================================================\n")

    noiiClienti.forEach { nume ->
        val key = generateSovereignKey(nume, ARCHITECT_SECRET)
        println(" CLIENT: $nume")
        println(" MASTER KEY: $key")
        println(" STATUS: READY FOR DISPATCH")
        println("--------------------------------------------------")
    }

    println("\n[Sfat]: Copiaza aceste chei in SOVEREIGN_KEY_VAULT.txt")
    println("==================================================\n")
}

private fun generateSovereignKey(clientName: String, secret: String): String {
    val data = clientName.uppercase() + secret
    return MessageDigest.getInstance("SHA-256")
        .digest(data.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
        .take(16)
        .uppercase()
}
