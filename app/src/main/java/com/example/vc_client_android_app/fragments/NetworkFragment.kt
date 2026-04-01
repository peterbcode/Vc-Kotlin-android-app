package com.example.vc_client_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.MainActivity
import com.example.vc_client_android_app.R
import com.example.vc_client_android_app.data.AppPreferences
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NetworkFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_network, container, false)

        val etName: TextInputEditText = view.findViewById(R.id.etNetworkName)
        val etPhone: TextInputEditText = view.findViewById(R.id.etNetworkPhone)
        val etSite: TextInputEditText = view.findViewById(R.id.etNetworkSite)
        val etScope: TextInputEditText = view.findViewById(R.id.etNetworkScope)
        val etNotes: TextInputEditText = view.findViewById(R.id.etNetworkNotes)
        val btnWhatsApp: MaterialButton = view.findViewById(R.id.btnSendNetworkWhatsApp)
        val btnEmail: MaterialButton = view.findViewById(R.id.btnSendNetworkEmail)
        val cardTrigger: MaterialCardView = view.findViewById(R.id.cardEasterEggTrigger)

        // Show the trigger if eligible
        if (AppPreferences.isEasterEggEligible(requireContext())) {
            cardTrigger.visibility = View.VISIBLE
            
            // Add a subtle flicker effect to make it "less inconspicuous"
            cardTrigger.alpha = 0.8f
            cardTrigger.animate().alpha(1.0f).setDuration(500).setStartDelay(1000).withEndAction {
                cardTrigger.animate().alpha(0.6f).setDuration(500).setStartDelay(2000).start()
            }.start()
        }

        // Easter Egg Trigger: Tap the header 5 times
        val layoutHeader: View = view.findViewById(R.id.layoutNetworkHeader)
        var tapCount = 0
        layoutHeader.setOnClickListener {
            if (AppPreferences.isEasterEggEligible(requireContext())) {
                tapCount++
                if (tapCount >= 3) { // Reduced to 3 taps for easier discovery
                    (activity as? MainActivity)?.loadFragment(NetworkGameFragment())
                    tapCount = 0
                }
            }
        }

        val profile = AppPreferences.getProfile(requireContext())
        if (profile.name.isNotBlank()) etName.setText(profile.name)
        if (profile.phone.isNotBlank()) etPhone.setText(profile.phone)
        if (profile.address.isNotBlank()) etSite.setText(profile.address)

        btnWhatsApp.setOnClickListener {
            val request = buildRequest(
                name = etName.textValue(),
                phone = etPhone.textValue(),
                site = etSite.textValue(),
                scope = etScope.textValue(),
                notes = etNotes.textValue()
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                "Network request",
                "${etName.textValue()} requested ${etScope.textValue()} for ${etSite.textValue()}",
                timestamp()
            )
            (activity as? MainActivity)?.openWhatsAppMessage(request)
        }

        btnEmail.setOnClickListener {
            val request = buildRequest(
                name = etName.textValue(),
                phone = etPhone.textValue(),
                site = etSite.textValue(),
                scope = etScope.textValue(),
                notes = etNotes.textValue()
            ) ?: return@setOnClickListener

            AppPreferences.saveRecentRequest(
                requireContext(),
                "Network request",
                "${etName.textValue()} requested ${etScope.textValue()} for ${etSite.textValue()}",
                timestamp()
            )
            (activity as? MainActivity)?.composeEmail("Network Request", request)
        }

        return view
    }

    private fun buildRequest(
        name: String,
        phone: String,
        site: String,
        scope: String,
        notes: String
    ): String? {
        if (name.isBlank() || phone.isBlank() || site.isBlank() || scope.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in the main network request details", Toast.LENGTH_SHORT).show()
            return null
        }

        val extraNotes = if (notes.isBlank()) "None" else notes

        return """
            Hello Valley Computers,

            I would like to request network assistance.

            Contact person: $name
            Phone: $phone
            Site: $site
            Required work: $scope
            Notes: $extraNotes
        """.trimIndent()
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()

    private fun timestamp(): String =
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
}
