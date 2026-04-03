package com.example.vc_client_android_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.MainActivity
import com.example.vc_client_android_app.R
import com.example.vc_client_android_app.data.AppPreferences
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val tvWelcome: TextView = view.findViewById(R.id.tvWelcome)
        val btnGetConnected: MaterialButton = view.findViewById(R.id.btnGetConnected)
        val btnCheckCoverage: MaterialButton = view.findViewById(R.id.btnCheckCoverage)
        val cardIsp: MaterialCardView = view.findViewById(R.id.cardIsp)
        val cardRepairs: MaterialCardView = view.findViewById(R.id.cardRepairs)
        val cardNetwork: MaterialCardView = view.findViewById(R.id.cardNetwork)
        val cardAccount: MaterialCardView = view.findViewById(R.id.cardAccount)

        val profile = AppPreferences.getProfile(requireContext())
        tvWelcome.text = if (profile.name.isBlank()) {
            "Powering the Riebeek Valley."
        } else {
            "Welcome back, ${profile.name}."
        }

        btnGetConnected.setOnClickListener { (activity as? MainActivity)?.loadFragment(IspFragment()) }

        btnCheckCoverage.setOnClickListener { 
            showCoverageDialog()
        }

        cardIsp.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(IspFragment())
        }

        cardRepairs.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(RepairsFragment())
        }

        cardNetwork.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(NetworkFragment())
        }

        cardAccount.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(AccountFragment())
        }

        return view
    }

    private fun showCoverageDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_coverage, null)
        val etArea: TextInputEditText = dialogView.findViewById(R.id.etCoverageArea)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Check Coverage")
            .setMessage("Enter your town or area to check if Valley Computers provides service in your region.")
            .setView(dialogView)
            .setPositiveButton("Check") { dialog, _ ->
                val rawInput = etArea.text?.toString()?.trim().orEmpty()
                if (rawInput.isBlank()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Area required")
                        .setMessage("Please enter your town or area before checking coverage.")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setPositiveButton
                }

                val areaInput = rawInput.lowercase()
                val swartlandAreas = listOf(
                    "riebeek kasteel",
                    "riebeek-kasteel",
                    "riebeek west",
                    "riebeek-west",
                    "malmesbury",
                    "chatsworth",
                    "swartland",
                    "koringberg",
                    "moorreesburg",
                    "darling"
                )

                val isCovered = swartlandAreas.any { area ->
                    areaInput.contains(area) || area.contains(areaInput)
                }

                if (isCovered) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Good News!")
                        .setMessage("Yes! Valley Computers provides coverage in that area. Would you like to view our ISP packages?")
                        .setPositiveButton("View Packages") { _, _ ->
                            (activity as? MainActivity)?.loadFragment(IspFragment())
                        }
                        .setNegativeButton("Maybe Later", null)
                        .show()
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Not Covered Yet")
                        .setMessage("Unfortunately, we don't currently provide service in \"$rawInput\". We are primarily focused on the Riebeek Valley and Swartland regions.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
