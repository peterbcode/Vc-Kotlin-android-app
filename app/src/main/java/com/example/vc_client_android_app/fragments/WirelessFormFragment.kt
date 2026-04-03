package com.example.vc_client_android_app.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.example.vc_client_android_app.MainActivity
import com.example.vc_client_android_app.R
import com.example.vc_client_android_app.data.AppPreferences
import com.example.vc_client_android_app.views.SignatureView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class WirelessFormFragment : Fragment() {

    private var signatureBitmap: Bitmap? = null
    private var submitButton: MaterialButton? = null
    private var pendingWhatsAppAttachments: List<File> = emptyList()
    private var pendingWhatsAppMessage: String? = null
    private val emailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val mainActivity = activity as? MainActivity ?: return@registerForActivityResult
        val message = pendingWhatsAppMessage ?: return@registerForActivityResult
        val attachments = pendingWhatsAppAttachments

        pendingWhatsAppMessage = null
        pendingWhatsAppAttachments = emptyList()

        sendWhatsAppMessageWithFirebaseLinks(
            attachments = attachments,
            fallbackMessage = message,
            mainActivity = mainActivity,
            onComplete = {
                navigateToAccountDashboard()
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_wireless_form, container, false)

        val etName: TextInputEditText = view.findViewById(R.id.etFormName)
        val etAddress: TextInputEditText = view.findViewById(R.id.etFormAddress)
        val etTown: TextInputEditText = view.findViewById(R.id.etFormTown)
        val etContact: TextInputEditText = view.findViewById(R.id.etFormContact)
        val etEmail: TextInputEditText = view.findViewById(R.id.etFormEmail)
        val etInstallDate: TextInputEditText = view.findViewById(R.id.etFormInstallDate)
        val etDate: TextInputEditText = view.findViewById(R.id.etFormDate)
        
        val rgWifi: RadioGroup = view.findViewById(R.id.rgWifiOptions)
        val cbUps: CheckBox = view.findViewById(R.id.cbUpsRental)
        val rgInstallFee: RadioGroup = view.findViewById(R.id.rgInstallFee)
        val rgPayment: RadioGroup = view.findViewById(R.id.rgPaymentMethod)
        
        val ivSignature: ImageView = view.findViewById(R.id.ivSignatureDisplay)
        val tvPlaceholder: TextView = view.findViewById(R.id.tvSignPlaceholder)
        val btnSubmit: MaterialButton = view.findViewById(R.id.btnSubmitApplication)
        submitButton = btnSubmit

        // Pre-fill from profile and current date
        val profile = AppPreferences.getProfile(requireContext())
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        
        etName.setText(profile.name)
        etEmail.setText(profile.email)
        etContact.setText(profile.phone)
        etAddress.setText(profile.address)
        
        // Automatically populate dates
        etDate.setText(currentDate)
        etInstallDate.setText(currentDate)

        val signArea = ivSignature.parent as View
        signArea.setOnClickListener {
            showSignatureDialog(ivSignature, tvPlaceholder)
        }

        btnSubmit.setOnClickListener {
            val name = etName.textValue()
            val address = etAddress.textValue()
            val town = etTown.textValue()
            val contact = etContact.textValue()
            val email = etEmail.textValue()
            val installDate = etInstallDate.textValue()
            val agreementDate = etDate.textValue()

            if (!validateForm(name, address, town, contact, email, installDate, agreementDate)) {
                return@setOnClickListener
            }

            val selectedWifiId = rgWifi.checkedRadioButtonId
            if (selectedWifiId == -1) {
                Toast.makeText(context, "Please select a WiFi option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val installFeeId = rgInstallFee.checkedRadioButtonId
            if (installFeeId == -1) {
                Toast.makeText(context, "Please select an Installation Fee status", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (signatureBitmap == null) {
                Toast.makeText(context, "Please provide your finger signature", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val wifiOption = view.findViewById<RadioButton>(selectedWifiId).text.toString()
            val installFeeStatus = view.findViewById<RadioButton>(installFeeId).text.toString()
            
            val paymentId = rgPayment.checkedRadioButtonId
            val paymentMethod = when (paymentId) {
                R.id.rbEft -> "EFT"
                R.id.rbOzow -> "Ozow (Instant EFT)"
                else -> "Cash"
            }
            
            val nameParts = name.split(" ", limit = 2)
            val firstName = nameParts.getOrNull(0).orEmpty()
            val surname = nameParts.getOrNull(1).orEmpty()

            val message = buildMessage(
                name = name,
                address = address,
                town = town,
                contact = contact,
                email = email,
                installDate = installDate,
                wifi = wifiOption,
                ups = cbUps.isChecked,
                installFee = installFeeStatus,
                payment = paymentMethod,
                date = agreementDate
            )

            // Prepare files for attachment
            val attachments = mutableListOf<File>()
            
            // 1. Save Signature as Image
            val signatureFile = saveBitmapToFile(signatureBitmap!!, "signature.png")
            if (signatureFile != null) attachments.add(signatureFile)
            
            // 2. Generate Excel-compatible CSV file with exact requested headings
            val csvFile = generateCsvFile(
                firstName = firstName,
                surname = surname,
                town = town,
                address = address,
                contact = contact,
                speed = wifiOption,
                pppoeUser = "", // Not collected in form, leave blank
                password = "",  // Not collected in form, leave blank
                sector = ""     // Not collected in form, leave blank
            )
            if (csvFile != null) attachments.add(csvFile)

            val mainActivity = activity as? MainActivity
            val emailSubject = "New Wireless Fibre Application: ${etName.text}"
            val emailIntent = mainActivity?.buildEmailAttachmentIntent(emailSubject, message, attachments)
            if (mainActivity != null) {
                AppPreferences.saveRecentRequest(
                    requireContext(),
                    "Wireless application",
                    "$name submitted a wireless application for $address",
                    currentDate
                )
                startEmailThenWhatsApp(
                    mainActivity = mainActivity,
                    emailIntent = emailIntent,
                    emailSubject = emailSubject,
                    message = message,
                    attachments = attachments
                )
            }

            if (paymentId == R.id.rbOzow) {
                openOzowPayment()
            }

            AppPreferences.setFormCompleted(requireContext(), true)
            
            Toast.makeText(
                context,
                "Email opens first. WhatsApp will open when you return from the email app.",
                Toast.LENGTH_LONG
            ).show()
        }

        return view
    }

    private fun validateForm(
        name: String,
        address: String,
        town: String,
        contact: String,
        email: String,
        installDate: String,
        agreementDate: String
    ): Boolean {
        if (name.isBlank() || address.isBlank() || town.isBlank() || contact.isBlank() || email.isBlank()) {
            Toast.makeText(requireContext(), "Please complete all customer details", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (installDate.isBlank() || agreementDate.isBlank()) {
            Toast.makeText(requireContext(), "Please confirm the installation and agreement dates", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveBitmapToFile(bitmap: Bitmap, filename: String): File? {
        return try {
            val file = File(requireContext().cacheDir, filename)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun generateCsvFile(
        firstName: String, surname: String, town: String, address: String,
        contact: String, speed: String, pppoeUser: String, password: String, sector: String
    ): File? {
        return try {
            val file = File(requireContext().cacheDir, "application_data.csv")
            val writer = file.printWriter()
            
            // Exact headings from the provided image
            writer.println("CLIENTS NAME,CLIENT SURNAME,Town,CLIENT ADDRESS,CONTACT NUMBER,Speed,PPPOE USERNAME,PASSWORD,Sector")
            
            // Data row
            writer.println("\"$firstName\",\"$surname\",\"$town\",\"$address\",\"$contact\",\"$speed\",\"$pppoeUser\",\"$password\",\"$sector\"")

            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showSignatureDialog(targetImageView: ImageView, placeholder: TextView) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_signature)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val signatureView: SignatureView = dialog.findViewById(R.id.dialogSignatureView)
        val btnClear: Button = dialog.findViewById(R.id.btnDialogClear)
        val btnSave: Button = dialog.findViewById(R.id.btnDialogSave)

        btnClear.setOnClickListener { signatureView.clear() }
        btnSave.setOnClickListener {
            if (!signatureView.isSignatureEmpty()) {
                val bitmap = signatureView.getSignatureBitmap()
                signatureBitmap = bitmap
                targetImageView.setImageBitmap(bitmap)
                placeholder.visibility = View.GONE
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please sign first", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun openOzowPayment() {
        val ozowUrl = "https://pay.ozow.com/"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ozowUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Ozow", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startEmailThenWhatsApp(
        mainActivity: MainActivity,
        emailIntent: Intent?,
        emailSubject: String,
        message: String,
        attachments: List<File>
    ) {
        pendingWhatsAppAttachments = attachments
        pendingWhatsAppMessage = message

        if (emailIntent != null) {
            emailLauncher.launch(emailIntent)
        } else {
            mainActivity.composeEmail(emailSubject, message)
            sendWhatsAppMessageWithFirebaseLinks(
                attachments = attachments,
                fallbackMessage = message,
                mainActivity = mainActivity,
                onComplete = {
                    navigateToAccountDashboard()
                }
            )
            pendingWhatsAppMessage = null
            pendingWhatsAppAttachments = emptyList()
        }
    }

    private fun sendWhatsAppMessageWithFirebaseLinks(
        attachments: List<File>,
        fallbackMessage: String,
        mainActivity: MainActivity,
        onComplete: () -> Unit = {}
    ) {
        if (attachments.isEmpty()) {
            mainActivity.openWhatsAppMessage(fallbackMessage)
            onComplete()
            return
        }

        setSubmittingState(true)
        Toast.makeText(
            requireContext(),
            "Uploading application files for WhatsApp...",
            Toast.LENGTH_SHORT
        ).show()

        ensureFirebaseUser(
            onReady = {
                uploadFilesToFirebase(
                    files = attachments,
                    onSuccess = { uploadedFiles ->
                        setSubmittingState(false)
                        val linkMessage = buildWhatsAppLinkMessage(fallbackMessage, uploadedFiles)
                        mainActivity.openWhatsAppMessage(linkMessage)
                        onComplete()
                    },
                    onFailure = {
                        setSubmittingState(false)
                        Toast.makeText(
                            requireContext(),
                            "Could not upload files. Sending WhatsApp message without links.",
                            Toast.LENGTH_LONG
                        ).show()
                        mainActivity.openWhatsAppMessage(fallbackMessage)
                        onComplete()
                    }
                )
            },
            onFailure = {
                setSubmittingState(false)
                Toast.makeText(
                    requireContext(),
                    "Could not sign in to Firebase. Sending WhatsApp message without links.",
                    Toast.LENGTH_LONG
                ).show()
                mainActivity.openWhatsAppMessage(fallbackMessage)
                onComplete()
            }
        )
    }

    private fun navigateToAccountDashboard() {
        view?.post {
            (activity as? MainActivity)?.loadFragment(AccountFragment())
        }
    }

    private fun TextInputEditText.textValue(): String = text?.toString()?.trim().orEmpty()

    private fun ensureFirebaseUser(onReady: () -> Unit, onFailure: () -> Unit) {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            onReady()
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { onReady() }
            .addOnFailureListener { onFailure() }
    }

    private fun uploadFilesToFirebase(
        files: List<File>,
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: () -> Unit
    ) {
        val storage = FirebaseStorage.getInstance()
        val timestamp = System.currentTimeMillis()
        val uploadTasks = files.filter { it.exists() }.mapIndexed { index, file ->
            val storageRef = storage.reference.child(
                "applications/$timestamp/${index}_${sanitizeFileName(file.name)}"
            )
            uploadFile(storageRef, file)
        }

        if (uploadTasks.isEmpty()) {
            onFailure()
            return
        }

        Tasks.whenAllSuccess<Pair<String, String>>(uploadTasks)
            .addOnSuccessListener { uploadedFiles ->
                onSuccess(uploadedFiles)
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    private fun uploadFile(storageRef: StorageReference, file: File) =
        storageRef.putFile(Uri.fromFile(file))
            .continueWithTask { uploadTask ->
                if (!uploadTask.isSuccessful) {
                    throw uploadTask.exception ?: IllegalStateException("Upload failed")
                }
                storageRef.downloadUrl
            }
            .continueWith { downloadTask ->
                if (!downloadTask.isSuccessful) {
                    throw downloadTask.exception ?: IllegalStateException("Download URL failed")
                }
                file.name to downloadTask.result.toString()
            }

    private fun buildWhatsAppLinkMessage(
        baseMessage: String,
        uploadedFiles: List<Pair<String, String>>
    ): String {
        val links = uploadedFiles.joinToString("\n") { (fileName, url) ->
            "${formatAttachmentLabel(fileName)}: $url"
        }

        return "$baseMessage\n\nAttachments:\n$links"
    }

    private fun formatAttachmentLabel(fileName: String): String {
        return when {
            fileName.contains("signature", ignoreCase = true) -> "Signature"
            fileName.contains("csv", ignoreCase = true) -> "Application CSV"
            else -> fileName
        }
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }

    private fun setSubmittingState(isSubmitting: Boolean) {
        submitButton?.apply {
            isEnabled = !isSubmitting
            text = if (isSubmitting) "Uploading..." else "Submit Application"
        }
    }

    private fun buildMessage(
        name: String, address: String, town: String, contact: String,
        email: String, installDate: String,
        wifi: String, ups: Boolean, installFee: String, payment: String, date: String
    ): String {
        return """
            WIRELESS FIBRE APPLICATION
            --------------------------
            Client: $name
            Address: $address
            Town: $town
            Contact: $contact
            Email: $email
            Install Date: $installDate
            
            Option: $wifi
            UPS Rental: ${if (ups) "Yes (R100)" else "No"}
            Installation Fee: $installFee
            Payment Method: $payment
            
            Agreement Date: $date
            
            Support: support@valleycomputers.co.za
            Accounts: accounts@valleycomputers.co.za
        """.trimIndent()
    }
}
