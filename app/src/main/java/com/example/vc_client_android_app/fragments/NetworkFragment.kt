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

class NetworkFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        val etName: TextInputEditText = view.findViewById(R.id.etNetworkName)
        val etPhone: TextInputEditText = view.findViewById(R.id.etNetworkPhone)
        val etSite: TextInputEditText = view.findViewById(R.id.etNetworkSite)
        val etScope: TextInputEditText = view.findViewById(R.id.etNetworkScope)
        val etNotes: TextInputEditText = view.findViewById(R.id.etNetworkNotes)
        val rgSiteType: RadioGroup = view.findViewById(R.id.rgSiteType)
        val rgPower: RadioGroup = view.findViewById(R.id.rgPowerSetup)
        val cbWifi: CheckBox = view.findViewById(R.id.cbInfraWifi)
        val cbCable: CheckBox = view.findViewById(R.id.cbInfraCable)
        val cbFiber: CheckBox = view.findViewById(R.id.cbInfraFiber)
        val cbCctv: CheckBox = view.findViewById(R.id.cbInfraCctv)
        val swEmergency: SwitchMaterial = view.findViewById(R.id.switchEmergency)

        // Pre-fill from saved profile
        val profile = AppPreferences.getProfile(requireContext())
        etName.setText(profile.name)
        etPhone.setText(profile.phone)
        etSite.setText(profile.address)

        // Collect infra checkboxes into a list for cleaner passing
        val infraCheckBoxes = listOf(cbWifi, cbCable, cbFiber, cbCctv)

        // Helper to build and validate request, running a side-effect on success
        fun sendRequest(onValidRequest: (String) -> Unit) {
            val request = buildRequest(
                name = etName.textValue(),
                phone = etPhone.textValue(),
                site = etSite.textValue(),
                scope = etScope.textValue(),
                notes = etNotes.textValue(),
                siteType = getRadioText(rgSiteType),
                power = getRadioText(rgPower),
                infra = getSelectedInfra(infraCheckBoxes),
                isEmergency = swEmergency.isChecked
            ) ?: return

            saveRequest(
                name = etName.textValue(),
                scope = etScope.textValue(),
                site = etSite.textValue()
            )
            onValidRequest(request)
        }

        view.findViewById<MaterialButton>(R.id.btnSendNetworkWhatsApp).setOnClickListener {
            sendRequest { request ->
                (activity as? MainActivity)?.openWhatsAppMessage(request)
            }
        }

        view.findViewById<MaterialButton>(R.id.btnSendNetworkEmail).setOnClickListener {
            sendRequest { request ->
                val subject = "Network Engineering Brief: ${etSite.textValue()}"
                (activity as? MainActivity)?.composeEmail(subject, request)
            }
        }
    }

    // --- Request building ---

    private fun buildRequest(
        name: String,
        phone: String,
        site: String,
        scope: String,
        notes: String,
        siteType: String,
        power: String,
        infra: String,
        isEmergency: Boolean
    ): String? {
        if (name.isBlank() || phone.isBlank() || site.isBlank()) {
            val ctx = context
            if (ctx != null) {
                Toast.makeText(ctx, R.string.error_fill_contact_info, Toast.LENGTH_SHORT).show()
            }
            return null
        }

        val priority = if (isEmergency) "EMERGENCY / CRITICAL" else "Standard"
        val scopeText = scope.ifBlank { "Site assessment and survey required" }
        val notesText = notes.ifBlank { "None" }

        return """
            ENGINEERING BRIEF: NETWORK JOB
            ------------------------------
            Priority: $priority
            Environment: $siteType
            Power System: $power
            Infrastructure: $infra
            
            Contact: $name
            Phone: $phone
            Site: $site
            
            Scope of Work:
            $scopeText
            
            Detailed Technical Notes:
            $notesText
            
            Generated via Valley Computers Engineering Tool
        """.trimIndent()
    }

    // --- Persistence ---

    private fun saveRequest(name: String, scope: String, site: String) {
        AppPreferences.saveRecentRequest(
            requireContext(),
            "Network Engineering",
            "$name briefed: $scope at $site",
            timestamp()
        )
    }

    // --- Helpers ---

    private fun getRadioText(group: RadioGroup): String {
        val id = group.checkedRadioButtonId
        if (id == -1) return "Not specified"
        return group.findViewById<android.widget.RadioButton>(id)?.text?.toString() ?: "Unknown"
    }

    private fun getSelectedInfra(checkBoxes: List<CheckBox>): String {
        val selected = checkBoxes.filter { it.isChecked }.map { it.text.toString() }
        return selected.ifEmpty { listOf("General Assessment") }.joinToString(", ")
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()

    private fun timestamp(): String =
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
}
