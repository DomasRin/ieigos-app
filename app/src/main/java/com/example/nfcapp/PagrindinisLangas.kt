package com.example.nfcapp

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var welcomeText: TextView
    private lateinit var logoutButton: Button
    private lateinit var startNfcButton: ImageButton
    private var nfcAdapter: NfcAdapter? = null
    private var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pagrindinis_langas)

        welcomeText = findViewById(R.id.welcomeText)
        logoutButton = findViewById(R.id.logoutButton)
        startNfcButton = findViewById(R.id.startNfcButton)

        val username = intent.getStringExtra("USERNAME")
        welcomeText.text = "Sveiki, $username!"

        logoutButton.setOnClickListener {
            logout()
        }

        startNfcButton.setOnClickListener {

            val token = getSavedToken()
            if (token.isNullOrEmpty() || isTokenExpired(token)) {
                Toast.makeText(
                    this,
                    "Sesija pasibaigė, prašome prisijungti iš naujo",
                    Toast.LENGTH_SHORT
                ).show()
                logout()
            } else {
                getAccessIdAndStartEmulation()
            }
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        val token = getSavedToken()
        if (!token.isNullOrEmpty() && isTokenExpired(token)) {
            Toast.makeText(
                this,
                "Sesija pasibaigė, esate atjungiamas",
                Toast.LENGTH_SHORT
            ).show()
            logout()
        }
    }

    private fun getAccessIdAndStartEmulation() {
        val token = getSavedToken()
        if (token.isNullOrEmpty()) {
            logoutWithMessage("Sesija pasibaigė, prašome prisijungti iš naujo")
            return
        }

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val baseUrl = prefs.getString(
            "pref_server_url",
            getString(R.string.server_base_url)
        )!!

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$baseUrl/auth/api/user-data")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                if (!response.isSuccessful) {
                    clearToken()
                    runOnUiThread {
                        val msg = if (response.code == 401)
                            "Sesijos patvirtinimas nepavyko, prašome prisijungti iš naujo"
                        else
                            "Klaida patvirtinant sesiją, prašome prisijungti iš naujo"

                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        logout()
                    }
                    return@Thread
                }

                // Parse JSON and start NFC
                val jsonResponse = JSONObject(responseData ?: "{}")
                val customId = jsonResponse.optString("custom_id", null)
                val userId = jsonResponse.optString("user_id", null)

                if (customId == null || userId == null) {
                    throw Exception("Neteisingi serverio duomenys")
                }

                runOnUiThread {
                    enableNfcEmulation(customId, userId)
                }

            } catch (e: Exception) {
                Log.e("API Error", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Klaida patvirtinant sesiją, prašome prisijungti iš naujo",
                        Toast.LENGTH_SHORT
                    ).show()
                    logout()
                }
            }
        }.start()
    }

    private fun logoutWithMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        logout()
    }

    private fun enableNfcEmulation(customId: String, userId: String) {
        val intent = Intent(this, CardEmulationService::class.java)
        intent.putExtra("CUSTOM_ID", customId)
        intent.putExtra("USER_ID", userId)
        startService(intent)

        startNfcButton.isEnabled = false
        startNfcButton.setColorFilter(
            resources.getColor(R.color.colorPrimaryVariant, theme)
        )

        Toast.makeText(this, "Skleidimas prasidėjo", Toast.LENGTH_SHORT).show()
        handler.postDelayed({ stopNfcEmulation() }, 6000)
    }

    private fun stopNfcEmulation() {
        val intent = Intent(this, CardEmulationService::class.java)
        stopService(intent)

        startNfcButton.isEnabled = true
        startNfcButton.setColorFilter(
            resources.getColor(android.R.color.white, theme)
        )

        Toast.makeText(this, "NFC Skleidimas baigtas", Toast.LENGTH_SHORT).show()
    }

    private fun getSavedToken(): String? {
        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }

    private fun clearToken() {
        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        sharedPreferences.edit().remove("auth_token").apply()
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val jsonPayload = JSONObject(payload)
            val exp = jsonPayload.getLong("exp")
            val now = System.currentTimeMillis() / 1000
            now >= exp
        } catch (e: Exception) {
            Log.e("TokenError", "Klaida skaitant sesija: ${e.message}")
            true
        }
    }

    private fun logout() {
        clearToken()
        Toast.makeText(this, "Atsijungiama...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }



}
