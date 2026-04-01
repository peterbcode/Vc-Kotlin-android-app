package com.example.vc_client_android_app.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.vc_client_android_app.MainActivity
import com.example.vc_client_android_app.R
import kotlin.random.Random

class NetworkGameFragment : Fragment() {

    private lateinit var statusText: TextView
    private lateinit var grid: GridLayout
    private lateinit var resetButton: Button
    private val size = 3
    private val nodes = Array(size) { BooleanArray(size) }
    private val buttons = Array(size) { arrayOfNulls<Button>(size) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_network_game, container, false)
        statusText = view.findViewById(R.id.tvGameStatus)
        grid = view.findViewById(R.id.glGameGrid)
        resetButton = view.findViewById(R.id.btnResetGame)
        val backBtn: ImageButton = view.findViewById(R.id.btnBackFromGame)

        backBtn.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(NetworkFragment())
        }

        setupGrid()
        resetButton.setOnClickListener { resetGame() }

        return view
    }

    private fun setupGrid() {
        grid.removeAllViews()
        for (i in 0 until size) {
            for (j in 0 until size) {
                val button = Button(context).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 180
                        height = 180
                        setMargins(8, 8, 8, 8)
                    }
                    setOnClickListener { onNodeClicked(i, j) }
                }
                buttons[i][j] = button
                grid.addView(button)
            }
        }
        resetGame()
    }

    private fun onNodeClicked(r: Int, c: Int) {
        toggleNode(r, c)
        if (r > 0) toggleNode(r - 1, c)
        if (r < size - 1) toggleNode(r + 1, c)
        if (c > 0) toggleNode(r, c - 1)
        if (c < size - 1) toggleNode(r, c + 1)
        
        checkWin()
    }

    private fun toggleNode(r: Int, c: Int) {
        nodes[r][c] = !nodes[r][c]
        updateButton(r, c)
    }

    private fun updateButton(r: Int, c: Int) {
        buttons[r][c]?.setBackgroundColor(if (nodes[r][c]) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
    }

    private fun resetGame() {
        do {
            for (i in 0 until size) {
                for (j in 0 until size) {
                    nodes[i][j] = Random.nextBoolean()
                    updateButton(i, j)
                }
            }
        } while (nodes.all { it.all { b -> b } })
        
        statusText.text = "CRITICAL ERROR:\nNetwork Nodes Offline"
        statusText.setTextColor(0xFFFF5252.toInt())
        resetButton.visibility = View.GONE
    }

    private fun checkWin() {
        if (nodes.all { it.all { b -> b } }) {
            statusText.text = "SYSTEM RESTORED\nMesh Synchronized"
            statusText.setTextColor(0xFF4CAF50.toInt())
            resetButton.visibility = View.VISIBLE
        }
    }
}
