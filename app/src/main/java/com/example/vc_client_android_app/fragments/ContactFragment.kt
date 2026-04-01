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
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContactFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_contact, container, false)

        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val etName: TextInputEditText = view.findViewById(R.id.etContactName)
        val etSubject: TextInputEditText = view.findViewById(R.id.etContactSubject)
        val etMessage: TextInputEditText = view.findViewById(R.id.etContactMessage)
        val btnCall: MaterialButton = view.findViewById(R.id.btnCall)
        val btnWhatsApp: MaterialButton = view.findViewById(R.id.btnWhatsApp)
        val btnEmail: MaterialButton = view.findViewById(R.id.btnEmail)
        val btnSendContactWhatsApp: MaterialButton = view.findViewById(R.id.btnSendContactWhatsApp)

        tvPhone.text = "079 938 1260"
        tvAddress.text = "6 Church Rd, Riebeek-Kasteel, 7307"

        val profile = AppPreferences.getProfile(requireContext())
        if (profile.name.isNotBlank()) etName.setText(profile.name)
        if (profile.email.isNotBlank()) etSubject.setText("Support request from ${profile.name}")

        btnCall.setOnClickListener {
            (activity as? MainActivity)?.dialPhone()
        }

        btnWhatsApp.setOnClickListener {
            (activity as? MainActivity)?.openWhatsApp()
        }

        btnEmail.setOnClickListener {
            val request = buildSupportMessage(
                name = etName.textValue(),
                subject = etSubject.textValue(),
                message = etMessage.textValue()
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                "Support request",
                "${etName.textValue()} sent \"${etSubject.textValue()}\"",
                timestamp()
            )
            (activity as? MainActivity)?.composeEmail(etSubject.textValue().ifBlank { "Support Request" }, request)
        }

        btnSendContactWhatsApp.setOnClickListener {
            val request = buildSupportMessage(
                name = etName.textValue(),
                subject = etSubject.textValue(),
                message = etMessage.textValue()
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                "Support request",
                "${etName.textValue()} sent \"${etSubject.textValue()}\"",
                timestamp()
            )
            (activity as? MainActivity)?.openWhatsAppMessage(request)
        }

        return view
    }

    private fun buildSupportMessage(
        name: String,
        subject: String,
        message: String
    ): String? {
        if (name.isBlank() || subject.isBlank() || message.isBlank()) {
            Toast.makeText(requireContext(), "Please complete your support message", Toast.LENGTH_SHORT).show()
            return null
        }

        return """
            Hello Valley Computers,

            Support request from: $name
            Subject: $subject

            Message:
            $message
        """.trimIndent()
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()

    private fun timestamp(): String =
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
}
