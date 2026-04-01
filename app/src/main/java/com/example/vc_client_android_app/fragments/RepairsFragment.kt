package com.example.vc_client_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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

class RepairsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_repairs, container, false)

        val etName: TextInputEditText = view.findViewById(R.id.etRepairName)
        val etPhone: TextInputEditText = view.findViewById(R.id.etRepairPhone)
        val etDevice: TextInputEditText = view.findViewById(R.id.etRepairDevice)
        val etIssue: TextInputEditText = view.findViewById(R.id.etRepairIssue)
        val cbUrgent: CheckBox = view.findViewById(R.id.cbUrgentRepair)
        val btnWhatsApp: MaterialButton = view.findViewById(R.id.btnSendRepairWhatsApp)
        val btnEmail: MaterialButton = view.findViewById(R.id.btnSendRepairEmail)

        val profile = AppPreferences.getProfile(requireContext())
        if (profile.name.isNotBlank()) etName.setText(profile.name)
        if (profile.phone.isNotBlank()) etPhone.setText(profile.phone)

        btnWhatsApp.setOnClickListener {
            val request = buildRequest(
                name = etName.textValue(),
                phone = etPhone.textValue(),
                device = etDevice.textValue(),
                issue = etIssue.textValue(),
                urgent = cbUrgent.isChecked
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                "Repair booking",
                "${etName.textValue()} booked ${etRepairSummary(etDevice.textValue(), cbUrgent.isChecked)}",
                timestamp()
            )
            (activity as? MainActivity)?.openWhatsAppMessage(request)
        }

        btnEmail.setOnClickListener {
            val request = buildRequest(
                name = etName.textValue(),
                phone = etPhone.textValue(),
                device = etDevice.textValue(),
                issue = etIssue.textValue(),
                urgent = cbUrgent.isChecked
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                "Repair booking",
                "${etName.textValue()} booked ${etRepairSummary(etDevice.textValue(), cbUrgent.isChecked)}",
                timestamp()
            )
            (activity as? MainActivity)?.composeEmail("Repair Booking", request)
        }

        return view
    }

    private fun buildRequest(
        name: String,
        phone: String,
        device: String,
        issue: String,
        urgent: Boolean
    ): String? {
        if (name.isBlank() || phone.isBlank() || device.isBlank() || issue.isBlank()) {
            Toast.makeText(requireContext(), "Please complete the repair booking form", Toast.LENGTH_SHORT).show()
            return null
        }

        val urgency = if (urgent) "Urgent" else "Standard"

        return """
            Hello Valley Computers,

            I would like to book a repair.

            Name: $name
            Phone: $phone
            Device: $device
            Urgency: $urgency
            Issue: $issue
        """.trimIndent()
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()

    private fun timestamp(): String =
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

    private fun etRepairSummary(device: String, urgent: Boolean): String =
        if (urgent) "an urgent repair for $device" else "a repair for $device"
}
