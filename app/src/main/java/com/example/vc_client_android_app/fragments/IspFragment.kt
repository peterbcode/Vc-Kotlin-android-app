package com.example.vc_client_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
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

class IspFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_isp, container, false)

        val rbFibre: RadioButton = view.findViewById(R.id.rbFibre)
        val etName: TextInputEditText = view.findViewById(R.id.etIspName)
        val etPhone: TextInputEditText = view.findViewById(R.id.etIspPhone)
        val etAddress: TextInputEditText = view.findViewById(R.id.etIspAddress)
        val etNotes: TextInputEditText = view.findViewById(R.id.etIspNotes)
        val cbCoverageOnly: CheckBox = view.findViewById(R.id.cbCoverageOnly)
        val btnWhatsApp: MaterialButton = view.findViewById(R.id.btnSendIspWhatsApp)
        val btnEmail: MaterialButton = view.findViewById(R.id.btnSendIspEmail)

        val profile = AppPreferences.getProfile(requireContext())
        if (profile.name.isNotBlank()) etName.setText(profile.name)
        if (profile.phone.isNotBlank()) etPhone.setText(profile.phone)
        if (profile.address.isNotBlank()) etAddress.setText(profile.address)
        if (profile.serviceInterest == "Wireless") {
            view.findViewById<RadioButton>(R.id.rbWireless).isChecked = true
        }

        btnWhatsApp.setOnClickListener {
            val requestType = if (cbCoverageOnly.isChecked) "Coverage check" else "ISP request"
            val request = buildRequest(
                connectionType = if (rbFibre.isChecked) "Fibre" else "Wireless",
                name = etName.textValue(),
                phone = etPhone.textValue(),
                address = etAddress.textValue(),
                notes = etNotes.textValue(),
                coverageOnly = cbCoverageOnly.isChecked
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                requestType,
                "${etName.textValue()} for ${if (rbFibre.isChecked) "Fibre" else "Wireless"} at ${etAddress.textValue()}",
                timestamp()
            )
            (activity as? MainActivity)?.openWhatsAppMessage(request)
        }

        btnEmail.setOnClickListener {
            val requestType = if (cbCoverageOnly.isChecked) "Coverage check" else "ISP request"
            val request = buildRequest(
                connectionType = if (rbFibre.isChecked) "Fibre" else "Wireless",
                name = etName.textValue(),
                phone = etPhone.textValue(),
                address = etAddress.textValue(),
                notes = etNotes.textValue(),
                coverageOnly = cbCoverageOnly.isChecked
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                requestType,
                "${etName.textValue()} for ${if (rbFibre.isChecked) "Fibre" else "Wireless"} at ${etAddress.textValue()}",
                timestamp()
            )
            (activity as? MainActivity)?.composeEmail("ISP Request", request)
        }

        return view
    }

    private fun buildRequest(
        connectionType: String,
        name: String,
        phone: String,
        address: String,
        notes: String,
        coverageOnly: Boolean
    ): String? {
        if (name.isBlank() || phone.isBlank() || address.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in name, phone and address", Toast.LENGTH_SHORT).show()
            return null
        }

        val requestType = if (coverageOnly) "Coverage check" else "New connection"
        val extraNotes = if (notes.isBlank()) "None" else notes

        return """
            Hello Valley Computers,

            I would like to submit an ISP request.

            Request type: $requestType
            Connection type: $connectionType
            Name: $name
            Phone: $phone
            Address: $address
            Notes: $extraNotes
        """.trimIndent()
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()

    private fun timestamp(): String =
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
}
