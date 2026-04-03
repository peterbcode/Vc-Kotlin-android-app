package com.example.vc_client_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.MainActivity
import com.example.vc_client_android_app.R
import com.example.vc_client_android_app.data.AppPreferences
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
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
        
        val rgCategory: RadioGroup = view.findViewById(R.id.rgDeviceCategory)
        val cbPower: CheckBox = view.findViewById(R.id.cbIssuePower)
        val cbDisplay: CheckBox = view.findViewById(R.id.cbIssueDisplay)
        val cbSoftware: CheckBox = view.findViewById(R.id.cbIssueSoftware)
        val cbPhysical: CheckBox = view.findViewById(R.id.cbIssuePhysical)
        val swUrgent: SwitchMaterial = view.findViewById(R.id.switchUrgent)

        val btnWhatsApp: MaterialButton = view.findViewById(R.id.btnSendRepairWhatsApp)
        val btnEmail: MaterialButton = view.findViewById(R.id.btnSendRepairEmail)

        val profile = AppPreferences.getProfile(requireContext())
        etName.setText(profile.name)
        etPhone.setText(profile.phone)

        btnWhatsApp.setOnClickListener {
            val request = buildRequest(
                name = etName.textValue(),
                phone = etPhone.textValue(),
                device = etDevice.textValue(),
                issue = etIssue.textValue(),
                category = getRadioText(rgCategory),
                assessment = getSelectedAssessment(cbPower, cbDisplay, cbSoftware, cbPhysical),
                isUrgent = swUrgent.isChecked
            ) ?: return@setOnClickListener

            saveRequest(etName.textValue(), etDevice.textValue(), swUrgent.isChecked)
            (activity as? MainActivity)?.openWhatsAppMessage(request)
        }

        btnEmail.setOnClickListener {
            val request = buildRequest(
                name = etName.textValue(),
                phone = etPhone.textValue(),
                device = etDevice.textValue(),
                issue = etIssue.textValue(),
                category = getRadioText(rgCategory),
                assessment = getSelectedAssessment(cbPower, cbDisplay, cbSoftware, cbPhysical),
                isUrgent = swUrgent.isChecked
            ) ?: return@setOnClickListener

            saveRequest(etName.textValue(), etDevice.textValue(), swUrgent.isChecked)
            (activity as? MainActivity)?.composeEmail("Repair Booking: ${etDevice.textValue()}", request)
        }

        return view
    }

    private fun getRadioText(group: RadioGroup): String {
        val id = group.checkedRadioButtonId
        if (id == -1) return "General"
        return group.findViewById<android.widget.RadioButton>(id)?.text?.toString() ?: "General"
    }

    private fun getSelectedAssessment(vararg boxes: CheckBox): String {
        val list = mutableListOf<String>()
        boxes.forEach { if (it.isChecked) list.add(it.text.toString()) }
        return if (list.isEmpty()) "Standard" else list.joinToString(", ")
    }

    private fun saveRequest(name: String, device: String, urgent: Boolean) {
        AppPreferences.saveRecentRequest(
            requireContext(),
            "Repair Booking",
            "$name booked ${if (urgent) "URGENT" else "standard"} repair for $device",
            timestamp()
        )
    }

    private fun buildRequest(
        name: String, phone: String, device: String, issue: String,
        category: String, assessment: String, isUrgent: Boolean
    ): String? {
        if (name.isBlank() || phone.isBlank() || device.isBlank()) {
            Toast.makeText(requireContext(), "Please provide device and contact details", Toast.LENGTH_SHORT).show()
            return null
        }

        return """
            TECHNICAL REPAIR BOOKING
            ------------------------
            Urgency: ${if (isUrgent) "CRITICAL / URGENT" else "Normal"}
            Category: $category
            Initial Assessment: $assessment
            
            Contact: $name
            Phone: $phone
            Device: $device
            
            Detailed Fault Description:
            ${if (issue.isBlank()) "Technical evaluation required" else issue}
            
            Generated via Valley Computers Technical Tool
        """.trimIndent()
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()

    private fun timestamp(): String =
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
}
