package com.example.vc_client_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.MainActivity
import com.example.vc_client_android_app.R
import com.example.vc_client_android_app.data.AppPreferences
import com.google.android.material.button.MaterialButton

class AuthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        val btnGoogle: MaterialButton = view.findViewById(R.id.btnGoogleSignIn)
        val btnEmail: MaterialButton = view.findViewById(R.id.btnEmailSignIn)
        val tvSkip: TextView = view.findViewById(R.id.tvSkipAuth)

        btnGoogle.setOnClickListener {
            // Logic for Google Sign In would go here
            Toast.makeText(requireContext(), "Redirecting to Google Sign-In...", Toast.LENGTH_SHORT).show()
            simulateLogin("Google User")
        }

        btnEmail.setOnClickListener {
            simulateLogin("Email User")
        }

        tvSkip.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(HomeFragment())
        }

        return view
    }

    private fun simulateLogin(name: String) {
        val profile = AppPreferences.getProfile(requireContext()).copy(name = name)
        AppPreferences.saveProfile(requireContext(), profile)
        
        Toast.makeText(requireContext(), "Welcome, $name", Toast.LENGTH_SHORT).show()
        (activity as? MainActivity)?.loadFragment(AccountFragment())
    }
}
