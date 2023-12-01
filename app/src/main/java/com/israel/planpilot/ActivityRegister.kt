package com.israel.planpilot

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ActivityRegister : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextConfirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnRegister.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                    this, "Preencha todos os campos", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).
                show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->

                    progressBar.visibility = View.GONE

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this, "Registro bem-sucedido", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            Toast.makeText(
                                this, "Usuário não encontrado", Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(
                                this, "Credenciais inválidas", Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this, "Erro desconhecido", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }
    }
}
