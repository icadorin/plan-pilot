package com.israel.planpilot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.israel.planpilot.model.UserModel
import com.israel.planpilot.repository.UserRepository

class RegisterFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository()

        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val editTextEmail = view.findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = view.findViewById<EditText>(R.id.editTextPassword)
        val editTextConfirmPassword = view.findViewById<EditText>(R.id.editTextConfirmPassword)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        btnRegister.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { task ->
                progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    val userModel = UserModel(auth.uid ?: "", name, email)
                    userRepository.addUser(userModel, auth.uid ?: "").addOnSuccessListener {
                        sendEmailVerification()
                        Toast.makeText(requireContext(), "Registro bem-sucedido. Verifique seu e-mail para confirmação.", Toast.LENGTH_SHORT).show()

                        editTextName.text.clear()
                        editTextEmail.text.clear()
                        editTextPassword.text.clear()
                        editTextConfirmPassword.text.clear()
                    }.addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Erro ao adicionar usuário", Toast.LENGTH_SHORT).show()
                        Log.e("RegisterFragment", "Erro ao adicionar usuário", e)
                    }
                } else {
                    handleRegisterError(task.exception)
                }
            }
        }

        return view
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("RegisterFragment", "E-mail de verificação enviado.")
            } else {
                Log.e("RegisterFragment", "Falha ao enviar e-mail de verificação.", task.exception)
            }
        }
    }

    private fun handleRegisterError(exception: Exception?) {
        exception?.let {
            when (it) {
                is FirebaseAuthInvalidUserException -> {
                    Toast.makeText(requireContext(), "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterFragment", "Usuário não encontrado", it)
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    val errorCode = it.errorCode
                    handleFirebaseAuthError(errorCode)
                }
                else -> {
                    Toast.makeText(requireContext(), "Não foi possível registrar: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterFragment", "Erro desconhecido", it)
                }
            }
        }
    }

    private fun handleFirebaseAuthError(errorCode: String) {
        when (errorCode) {
            "ERROR_INVALID_EMAIL" -> {
                Toast.makeText(requireContext(), "E-mail inválido", Toast.LENGTH_SHORT).show()
                Log.e("RegisterFragment", "E-mail inválido")
            }
            "ERROR_WEAK_PASSWORD" -> {
                Toast.makeText(requireContext(), "Senha fraca. A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                Log.e("RegisterFragment", "Senha fraca")
            }
            else -> {
                Toast.makeText(requireContext(), "Erro ao criar usuário: $errorCode", Toast.LENGTH_SHORT).show()
                Log.e("RegisterFragment", "Erro ao criar usuário: $errorCode")
            }
        }
    }
}
