package com.example.vc_client_android_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.fragments.HomeFragment
import com.example.vc_client_android_app.fragments.IspFragment
import com.example.vc_client_android_app.fragments.RepairsFragment
import com.example.vc_client_android_app.fragments.NetworkFragment
import com.example.vc_client_android_app.fragments.AccountFragment
import com.example.vc_client_android_app.fragments.ContactFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // Make FAB Draggable
        setupDraggableFab(fabCall)

        fabCall.setOnClickListener {
            openWhatsApp()
        }

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun setupDraggableFab(fab: FloatingActionButton) {
        var dX = 0f
        var dY = 0f
        var startClickTime = 0L
        val clickDragThreshold = 200 // ms

        fab.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        startClickTime = System.currentTimeMillis()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        view.x = event.rawX + dX
                        view.y = event.rawY + dY
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
            .commit()
    }

    fun dialPhone(phoneNumber: String = "0799381260") {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
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
            val uri = Uri.parse("https://wa.me/27799381260?text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    fun composeEmail(subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@valleycomputers.co.za")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}
