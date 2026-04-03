package com.example.vc_client_android_app

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.fragments.*
import com.example.vc_client_android_app.data.AppPreferences
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MainActivity : AppCompatActivity() {
    private companion object {
        const val SUPPORT_EMAIL = "support@valleycomputers.co.za"
        const val SUPPORT_PHONE = "0799381260"
        const val WHATSAPP_NUMBER = "27799381260"
        const val WHATSAPP_PACKAGE = "com.whatsapp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.top_app_bar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val fabCall = findViewById<FloatingActionButton>(R.id.fab_call)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_isp -> {
                    loadFragment(IspFragment())
                    true
                }
                R.id.nav_repairs -> {
                    loadFragment(RepairsFragment())
                    true
                }
                R.id.nav_network -> {
                    loadFragment(NetworkFragment())
                    true
                }
                R.id.nav_contact -> {
                    loadFragment(ContactFragment())
                    true
                }
                else -> false
            }
        }

        // Make FAB Draggable with constraints
        setupDraggableFab(fabCall)

        fabCall.setOnClickListener {
            openWhatsApp()
        }

        // Logic for first-time form requirement
        if (savedInstanceState == null) {
            val profile = AppPreferences.getProfile(this)
            if (profile.name.isBlank()) {
                loadFragment(HomeFragment())
                bottomNav.selectedItemId = R.id.nav_home
            } else if (!profile.formCompleted) {
                loadFragment(WirelessFormFragment())
                Toast.makeText(this, "Please complete your application form", Toast.LENGTH_LONG).show()
            } else {
                loadFragment(HomeFragment())
                bottomNav.selectedItemId = R.id.nav_home
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_account -> {
                loadFragment(AccountFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupDraggableFab(fab: FloatingActionButton) {
        var dX = 0f
        var dY = 0f
        var startClickTime = 0L
        val clickDragThreshold = 200 // ms

        fab.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                val parent = view.parent as View
                val parentWidth = parent.width
                val parentHeight = parent.height

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        startClickTime = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        var newX = event.rawX + dX
                        var newY = event.rawY + dY

                        // Constraint X
                        newX = newX.coerceIn(0f, (parentWidth - view.width).toFloat())
                        // Constraint Y
                        newY = newY.coerceIn(0f, (parentHeight - view.height).toFloat())

                        view.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start()
                    }
                    MotionEvent.ACTION_UP -> {
                        val clickDuration = System.currentTimeMillis() - startClickTime
                        if (clickDuration < clickDragThreshold) {
                            view.performClick()
                        }
                    }
                    else -> return false
                }
                return true
            }
        })
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .commit()
    }

    fun dialPhone(phoneNumber: String = SUPPORT_PHONE) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            launchIntent(intent, "Unable to dial")
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to dial", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp() {
        openWhatsAppMessage("Hello Valley Computers, I would like some assistance.")
    }

    fun openWhatsAppMessage(message: String) {
        try {
            val encodedMessage = Uri.encode(message)
            val uri = Uri.parse("https://wa.me/$WHATSAPP_NUMBER?text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (isPackageInstalled(WHATSAPP_PACKAGE)) {
                    setPackage(WHATSAPP_PACKAGE)
                }
            }
            launchIntent(intent, "WhatsApp is not installed")
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsAppWithAttachments(message: String, files: List<File>) {
        try {
            val intent = buildWhatsAppAttachmentIntent(message, files)
            if (intent != null) {
                startActivity(intent)
            } else {
                openWhatsAppMessage(message)
            }
        } catch (e: Exception) {
            Log.e("WhatsApp", "Error sending attachments", e)
            openWhatsAppMessage(message)
        }
    }

    fun composeEmailWithAttachments(subject: String, body: String, files: List<File>) {
        try {
            val intent = buildEmailAttachmentIntent(subject, body, files)
            if (intent != null) {
                startActivity(intent)
            } else {
                composeEmail(subject, body)
            }
        } catch (e: Exception) {
            Log.e("Email", "Error sending email attachments", e)
            composeEmail(subject, body)
        }
    }

    fun composeEmail(subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                val uriText = "mailto:$SUPPORT_EMAIL" +
                        "?subject=" + Uri.encode(subject) +
                        "&body=" + Uri.encode(body)
                data = Uri.parse(uriText)
            }
            launchIntent(Intent.createChooser(intent, "Send email..."), "No email app found")
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildClipData(uris: List<Uri>): ClipData? {
        if (uris.isEmpty()) return null

        val clipData = ClipData.newRawUri("attachments", uris.first())
        uris.drop(1).forEach { uri ->
            clipData.addItem(ClipData.Item(uri))
        }
        return clipData
    }

    private fun grantUriPermissionsToPackage(packageName: String, uris: List<Uri>) {
        uris.forEach { uri ->
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun grantUriPermissionsToResolvedApps(intent: Intent, uris: List<Uri>) {
        packageManager.queryIntentActivities(intent, 0).forEach { resolveInfo ->
            grantUriPermissionsToPackage(resolveInfo.activityInfo.packageName, uris)
        }
    }

    fun buildWhatsAppAttachmentIntent(message: String, files: List<File>): Intent? {
        val zipFile = buildWhatsAppZip(files) ?: return null
        val uris = collectAttachmentUris(listOf(zipFile), "WhatsApp") ?: return null
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uris.first())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = buildClipData(uris)
        }
        grantUriPermissionsToResolvedApps(baseIntent, uris)
        return Intent.createChooser(baseIntent, "Share application ZIP")
    }

    fun buildEmailAttachmentIntent(subject: String, body: String, files: List<File>): Intent? {
        val uris = collectAttachmentUris(files, "Email") ?: return null
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = buildClipData(uris)
        }
        grantUriPermissionsToResolvedApps(intent, uris)
        return Intent.createChooser(intent, "Send Email")
    }

    private fun collectAttachmentUris(files: List<File>, logTag: String): ArrayList<Uri>? {
        val uris = ArrayList<Uri>()
        for (file in files) {
            if (file.exists()) {
                uris.add(FileProvider.getUriForFile(this, "${packageName}.fileprovider", file))
            } else {
                Log.e(logTag, "File does not exist: ${file.absolutePath}")
            }
        }

        return uris.takeIf { it.isNotEmpty() }
    }

    private fun buildWhatsAppZip(files: List<File>): File? {
        val existingFiles = files.filter { it.exists() }
        if (existingFiles.isEmpty()) return null

        return try {
            val zipFile = File(cacheDir, "wireless_application_attachments.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOutput ->
                existingFiles.forEach { file ->
                    FileInputStream(file).use { input ->
                        zipOutput.putNextEntry(ZipEntry(file.name))
                        input.copyTo(zipOutput)
                        zipOutput.closeEntry()
                    }
                }
            }
            zipFile
        } catch (e: Exception) {
            Log.e("WhatsApp", "Error creating ZIP attachment", e)
            null
        }
    }

    private fun launchIntent(intent: Intent, errorMessage: String): Boolean {
        return if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            true
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }
}
