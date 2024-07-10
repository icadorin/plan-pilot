package com.israel.planpilot

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.israel.planpilot.repository.UserRepository

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository()
        sharedPreferences = requireActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnCreateAccount = view.findViewById<Button>(R.id.btnCreateAccount)
        val editTextEmail = view.findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = view.findViewById<EditText>(R.id.editTextPassword)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val checkBoxRememberEmail = view.findViewById<CheckBox>(R.id.checkBoxRememberEmail)
        val checkBoxRememberPassword = view.findViewById<CheckBox>(R.id.checkBoxRememberPassword)

        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)
        val rememberEmail = sharedPreferences.getBoolean("rememberEmail", false)
        val rememberPassword = sharedPreferences.getBoolean("rememberPassword", false)

        if (rememberEmail) {
            savedEmail?.let {
                editTextEmail.setText(it)
                checkBoxRememberEmail.isChecked = true
            }
        }

        if (rememberPassword) {
            savedPassword?.let {
                editTextPassword.setText(it)
                checkBoxRememberPassword.isChecked = true
            }
        }

        btnLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        if (it.isEmailVerified) {

                            val editor = sharedPreferences.edit()
                            if (checkBoxRememberEmail.isChecked) {
                                editor.putString("email", email)
                                editor.putBoolean("rememberEmail", true)
                            } else {
                                editor.remove("email")
                                editor.putBoolean("rememberEmail", false)
                            }

                            if (checkBoxRememberPassword.isChecked) {
                                editor.putString("password", password)
                                editor.putBoolean("rememberPassword", true)
                            } else {
                                editor.remove("password")
                                editor.putBoolean("rememberPassword", false)
                            }
                            editor.apply()

                            userRepository.getUser(it.uid).addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userName = document.getString("name") ?: ""
                                    navigateToMainActivity()
                                } else {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(requireContext(), "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                                    Log.e("LoginFragment", "Usuário não encontrado no Firestore")
                                }
                            }.addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), "Erro ao obter usuário", Toast.LENGTH_SHORT).show()
                                Log.e("LoginFragment", "Erro ao obter usuário", e)
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Por favor, verifique seu email primeiro.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
                } else {
                    progressBar.visibility = View.GONE
                    handleLoginError(task.exception)
                }
            }
        }

        btnCreateAccount.setOnClickListener {
            navigateToRegister()
        }

        return view
    }

    private fun handleLoginError(exception: Exception?) {
        exception?.let {
            when (it) {
                is FirebaseAuthInvalidUserException -> {
                    Toast.makeText(requireContext(), "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                    Log.e("LoginFragment", "Usuário não encontrado", it)
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    Toast.makeText(requireContext(), "Credenciais inválidas", Toast.LENGTH_SHORT).show()
                    Log.e("LoginFragment", "Credenciais inválidas", it)
                }
                else -> {
                    Toast.makeText(requireContext(), "Erro ao criar usuário", Toast.LENGTH_SHORT).show()
                    Log.e("LoginFragment", "Erro desconhecido", it)
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val navController = findNavController()
        val options = NavOptions.Builder()
            .setEnterAnim(android.R.anim.fade_in)
            .setExitAnim(android.R.anim.fade_out)
            .build()
        navController.navigate(R.id.nav_home, null, options)
    }

    private fun navigateToRegister() {
        val registerFragment = RegisterFragment()
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, registerFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
