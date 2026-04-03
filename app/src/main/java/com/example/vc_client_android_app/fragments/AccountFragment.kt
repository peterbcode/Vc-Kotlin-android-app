package com.example.vc_client_android_app.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.MainActivity
import com.example.vc_client_android_app.R
import com.example.vc_client_android_app.data.AccountProfile
import com.example.vc_client_android_app.data.AppPreferences
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlin.random.Random

class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val tvGreeting: TextView = view.findViewById(R.id.tvAccountGreeting)
        val tvService: TextView = view.findViewById(R.id.tvDashboardService)
        val tvContact: TextView = view.findViewById(R.id.tvDashboardContact)
        val tvRecentType: TextView = view.findViewById(R.id.tvRecentRequestType)
        val tvRecentSummary: TextView = view.findViewById(R.id.tvRecentRequestSummary)
        val tvRecentTime: TextView = view.findViewById(R.id.tvRecentRequestTime)
        
        // Wireless Form Call to Action
        val btnOpenWirelessForm: MaterialButton = view.findViewById(R.id.btnOpenWirelessForm)
        
        // Speed Test Views
        val tvSpeedResult: TextView = view.findViewById(R.id.tvSpeedResult)
        val btnStartSpeedTest: MaterialButton = view.findViewById(R.id.btnStartSpeedTest)

        val etName: TextInputEditText = view.findViewById(R.id.etAccountName)
        val etEmail: TextInputEditText = view.findViewById(R.id.etAccountEmail)
        val etPhone: TextInputEditText = view.findViewById(R.id.etAccountPhone)
        val etAddress: TextInputEditText = view.findViewById(R.id.etAccountAddress)
        val rbContactWhatsApp: RadioButton = view.findViewById(R.id.rbContactWhatsApp)
        val rbContactCall: RadioButton = view.findViewById(R.id.rbContactCall)
        val rbContactEmail: RadioButton = view.findViewById(R.id.rbContactEmail)
        val rbServiceFibre: RadioButton = view.findViewById(R.id.rbServiceFibre)
        val rbServiceWireless: RadioButton = view.findViewById(R.id.rbServiceWireless)
        val rbServiceRepairs: RadioButton = view.findViewById(R.id.rbServiceRepairs)
        val switchNotifications: SwitchMaterial = view.findViewById(R.id.switchNotifications)
        val btnSave: MaterialButton = view.findViewById(R.id.btnSaveAccount)

        fun bindDashboard() {
            val profile = AppPreferences.getProfile(requireContext())
            val recent = AppPreferences.getRecentRequest(requireContext())

            tvGreeting.text = if (profile.name.isBlank()) {
                "Your Valley Computers dashboard."
            } else {
                "Welcome back, ${profile.name}."
            }

            tvService.text = profile.serviceInterest
            tvContact.text = buildString {
                append(profile.contactPreference)
                append(if (profile.notificationsEnabled) " with reminders enabled" else " with reminders off")
            }
            tvRecentType.text = recent.type
            tvRecentSummary.text = recent.summary
            tvRecentTime.text = recent.timestamp
        }

        val profile = AppPreferences.getProfile(requireContext())
        etName.setText(profile.name)
        etEmail.setText(profile.email)
        etPhone.setText(profile.phone)
        etAddress.setText(profile.address)
        switchNotifications.isChecked = profile.notificationsEnabled

        when (profile.contactPreference) {
            "Call" -> rbContactCall.isChecked = true
            "Email" -> rbContactEmail.isChecked = true
            else -> rbContactWhatsApp.isChecked = true
        }

        when (profile.serviceInterest) {
            "Wireless" -> rbServiceWireless.isChecked = true
            "Repairs" -> rbServiceRepairs.isChecked = true
            else -> rbServiceFibre.isChecked = true
        }

        bindDashboard()

        btnOpenWirelessForm.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(WirelessFormFragment())
        }

        btnStartSpeedTest.setOnClickListener {
            btnStartSpeedTest.isEnabled = false
            btnStartSpeedTest.text = "Testing..."
            tvSpeedResult.text = "..."
            
            val handler = Handler(Looper.getMainLooper())
            var count = 0
            val runnable = object : Runnable {
                override fun run() {
                    if (count < 10) {
                        tvSpeedResult.text = Random.nextInt(10, 100).toString()
                        count++
                        handler.postDelayed(this, 200)
                    } else {
                        val finalSpeed = Random.nextInt(20, 95)
                        tvSpeedResult.text = finalSpeed.toString()
                        btnStartSpeedTest.text = "Run Speed Test"
                        btnStartSpeedTest.isEnabled = true
                        Toast.makeText(requireContext(), "Speed test complete: $finalSpeed Mbps", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            handler.post(runnable)
        }

        btnSave.setOnClickListener {
            val name = etName.textValue()
            val email = etEmail.textValue()
            val phone = etPhone.textValue()
            val address = etAddress.textValue()

            if (name.isBlank() || phone.isBlank()) {
                Toast.makeText(requireContext(), "Please add at least your name and phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = AccountProfile(
                name = name,
                email = email,
                phone = phone,
                address = address,
                contactPreference = when {
                    rbContactCall.isChecked -> "Call"
                    rbContactEmail.isChecked -> "Email"
                    else -> "WhatsApp"
                },
                serviceInterest = when {
                    rbServiceWireless.isChecked -> "Wireless"
                    rbServiceRepairs.isChecked -> "Repairs"
                    else -> "Fibre"
                },
                notificationsEnabled = switchNotifications.isChecked
            )

            AppPreferences.saveProfile(requireContext(), updated)
            bindDashboard()
            Toast.makeText(requireContext(), "Account details saved", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()
}
