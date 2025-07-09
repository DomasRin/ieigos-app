package com.example.nfcapp

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class CardEmulationService : HostApduService() {

    // Saugo uzkoduota custom_id, papildyta iki 16 baitu
    private var encryptedId: ByteArray = "0000000000000000".toByteArray()

    // AES sifravimo raktas (16 baitu)
    private val AES_KEY = byteArrayOf(
        0xC4.toByte(), 0xD3.toByte(), 0xA5.toByte(), 0xF1.toByte(),
        0x12.toByte(), 0x98.toByte(), 0x77.toByte(), 0xEE.toByte(),
        0x6A.toByte(), 0xB1.toByte(), 0xD9.toByte(), 0x53.toByte(),
        0xA8.toByte(), 0x2F.toByte(), 0xE4.toByte(), 0x1C.toByte()
    )

    // Metodas, kvieciamas pasileidus servisiui; uzkoduoja custom_id ir user_id
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Paimami custom_id ir user_id is MainActivity
        val customId = intent?.getStringExtra("CUSTOM_ID") ?: ""
        val userId   = intent?.getStringExtra("USER_ID") ?: ""

        // Uzpildo arba apkarpo iki 8 simboliu
        val custom = customId.padEnd(8, '0').substring(0, 8)
        val user   = userId.padStart(8, '0').substring(0, 8)

        // Sujungia custom ir user i 16 baitu teksta (ASCII)
        val plaintext = (custom + user).toByteArray(Charsets.US_ASCII)

        try {
            // Sifruoja su AES/ECB/NoPadding
            val secretKey = SecretKeySpec(AES_KEY, "AES")
            val cipher    = Cipher.getInstance("AES/ECB/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            encryptedId = cipher.doFinal(plaintext)
        } catch (e: Exception) {
            // Klaida sifruojant
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // Metodas, apdorojantis gautas APDU komandas
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        // Jei gaunama SELECT APDU (00 A4 04 00), grazina uzkoduota ID
        if (commandApdu != null &&
            commandApdu.size >= 4 &&
            commandApdu[0] == 0x00.toByte() &&
            commandApdu[1] == 0xA4.toByte() &&
            commandApdu[2] == 0x04.toByte() &&
            commandApdu[3] == 0x00.toByte()
        ) {
            return encryptedId
        }
        // Kitais atvejais grazina UNKNOWN_COMMAND
        return "UNKNOWN_COMMAND".toByteArray()
    }


    override fun onDeactivated(reason: Int) {
    }
}
