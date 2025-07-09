package com.example.nfcapp

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var settingsButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prisijungimas)

        // --- NFC Patikra ------------------------------------------------------------
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            AlertDialog.Builder(
                ContextThemeWrapper(this, R.style.CustomAlertDialog)
            )
                .setTitle("NFC nepalaikomas")
                .setMessage("Jūsų įrenginys nepalaiko NFC. Programėlė bus uždaryta.")
                .setPositiveButton("Gerai") { _, _ -> finish() }
                .setCancelable(false)
                .show()
            return
        } else if (!nfcAdapter.isEnabled) {
            AlertDialog.Builder(
                ContextThemeWrapper(this, R.style.CustomAlertDialog)
            )
                .setTitle("Įjunkite NFC")
                .setMessage("Norint naudotis šia programėle, įjunkite NFC nustatymuose.")
                .setPositiveButton("Nustatymai") { _, _ ->
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }
                .setNegativeButton("Atšaukti") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
            return
        }
        // ---------------------------------------------------------------------------

        usernameField  = findViewById(R.id.usernameField)
        passwordField  = findViewById(R.id.passwordField)
        loginButton    = findViewById(R.id.loginButton)
        settingsButton = findViewById(R.id.settingsButton)

        settingsButton.setOnClickListener {
            showServerAddressDialog()
        }

        loginButton.setOnClickListener {
            authenticateUser()
        }
    }

    private fun showServerAddressDialog() {
        val prefs   = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val current = prefs.getString(
            "pref_server_url",
            getString(R.string.server_base_url)
        )!!

        val input = EditText(this).apply {
            setText(current)
            hint = "http://tavo.serveris:3000"
            inputType = InputType.TYPE_TEXT_VARIATION_URI
            setSelection(text.length)
        }

        // Apply the same CustomAlertDialog style to the settings dialog
        AlertDialog.Builder(
            ContextThemeWrapper(this, R.style.CustomAlertDialog)
        )
            .setTitle("Serverio URL Adresas")
            .setView(input)
            .setPositiveButton("Pakeisti") { dialog, _ ->
                prefs.edit()
                    .putString("pref_server_url", input.text.toString().trim())
                    .apply()
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Serverio URL išsaugotas",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Atšaukti", null)
            .show()
    }

    private fun authenticateUser() {
        val username = usernameField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Įveskite visus duomenis", Toast.LENGTH_SHORT).show()
            return
        }

        // Load server URL from prefs (fallback to default)
        val prefs   = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val baseUrl = prefs.getString(
            "pref_server_url",
            getString(R.string.server_base_url)
        )!!

        val client = OkHttpClient()
        val json   = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val body = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url("$baseUrl/auth/api/login")
            .post(body)
            .build()

        Thread {
            try {
                val response     = client.newCall(request).execute()
                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData ?: "{}")

                if (response.isSuccessful) {
                    val token = jsonResponse.getString("token")
                    saveToken(token)

                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Prisijungimas sėkmingas!",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(
                            Intent(this, MainActivity::class.java)
                                .putExtra("USERNAME", username)
                        )
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Neteisingi prisijungimo duomenys",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Tinklo klaida: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun saveToken(token: String) {
        val prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        prefs.edit().putString("auth_token", token).apply()
    }
}
