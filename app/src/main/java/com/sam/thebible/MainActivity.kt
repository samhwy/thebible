package com.sam.thebible

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.ui.AppBarConfiguration
import com.sam.thebible.adapter.ColorSpinnerAdapter
import com.sam.thebible.databinding.ActivityMainBinding
import com.sam.thebible.ui.main.MainFragment
import com.sam.thebible.utils.SettingsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        settingsManager = SettingsManager(this)
        applyTheme()
        
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        applyToolbarTheme()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        val navController = navHostFragment?.let {
            (it as? androidx.navigation.fragment.NavHostFragment)?.navController
        } ?: throw IllegalStateException("NavHostFragment not found or not set up correctly")

        appBarConfiguration = AppBarConfiguration(navController.graph)
        
        // Setup toolbar controls after fragment is loaded
        navController.addOnDestinationChangedListener { _, _, _ ->
            checkScreenSpace()
            setupToolbarControls()
        }
    }

    private fun setupToolbarControls() {
        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnPrevChapter)?.setOnClickListener {
            getCurrentMainFragment()?.onPrevChapter()
        }
        
        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnNextChapter)?.setOnClickListener {
            getCurrentMainFragment()?.onNextChapter()
        }
        
        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnSearch)?.setOnClickListener {
            showSearchDialog()
        }
        
        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnSettings)?.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun getCurrentMainFragment(): MainFragment? {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        return navHostFragment?.childFragmentManager?.fragments?.firstOrNull() as? MainFragment
    }

    private fun showSearchDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search, null)
        val etSearchKeyword = dialogView.findViewById<EditText>(R.id.etSearchKeyword)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<android.view.View>(R.id.btnSearchCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.view.View>(R.id.btnSearchOk).setOnClickListener {
            val keyword = etSearchKeyword.text.toString()
            if (keyword.isNotBlank()) {
                getCurrentMainFragment()?.onSearch(keyword)
            }
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun applyTheme() {
        if (settingsManager.isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    private fun applyToolbarTheme() {
        if (settingsManager.isDarkMode) {
            binding.toolbar.setBackgroundColor(0xFF424242.toInt())
        }
    }
    
    private fun checkScreenSpace() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels / displayMetrics.density

        if (screenWidth < 400) {
            // Switch to responsive layout for small screens
            binding.toolbar.removeAllViews()
            layoutInflater.inflate(R.layout.toolbar_responsive, binding.toolbar, true)
        }
    }

    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        
        val cbDarkMode = dialogView.findViewById<CheckBox>(R.id.cbDarkMode)
        val cbChineseEnglish = dialogView.findViewById<CheckBox>(R.id.cbChineseEnglish)
        val seekBarFontSize = dialogView.findViewById<SeekBar>(R.id.seekBarFontSize)
        val spinnerFontColor = dialogView.findViewById<Spinner>(R.id.spinnerFontColor)
        val spinnerBackgroundColor = dialogView.findViewById<Spinner>(R.id.spinnerBackgroundColor)
        
        // Font colors: white, black, green, yellow, orange
        val fontColors = arrayOf(Color.WHITE, Color.BLACK, Color.GREEN, Color.YELLOW, 0xFFFFA500.toInt())
        val fontColorNames = arrayOf(
            getString(R.string.white),
            getString(R.string.black),
            getString(R.string.green),
            getString(R.string.yellow),
            getString(R.string.orange)
        )
        
        // Background colors: black, white, parchment
        val backgroundColors = if (settingsManager.isDarkMode) {
            arrayOf(Color.BLACK, 0xFF2D2D2D.toInt(), 0xFF1A1A1A.toInt())
        } else {
            arrayOf(Color.WHITE, Color.BLACK, 0xFFF5F5DC.toInt())
        }
        val backgroundColorNames = arrayOf(
            if (settingsManager.isDarkMode) getString(R.string.black) else getString(R.string.white),
            if (settingsManager.isDarkMode) "深灰" else getString(R.string.black),
            if (settingsManager.isDarkMode) "極深" else getString(R.string.parchment)
        )
        
        val fontColorAdapter = ColorSpinnerAdapter(this, fontColors, fontColorNames)
        val backgroundColorAdapter = ColorSpinnerAdapter(this, backgroundColors, backgroundColorNames)
        
        spinnerFontColor.adapter = fontColorAdapter
        spinnerBackgroundColor.adapter = backgroundColorAdapter
        
        // Set current values
        cbDarkMode.isChecked = settingsManager.isDarkMode
        cbChineseEnglish.isChecked = settingsManager.showEnglish
        seekBarFontSize.progress = settingsManager.fontSize
        spinnerFontColor.setSelection(settingsManager.fontColorIndex)
        spinnerBackgroundColor.setSelection(settingsManager.backgroundColorIndex)
        
        cbDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val newBackgroundColors = if (isChecked) {
                arrayOf(Color.BLACK, 0xFF2D2D2D.toInt(), 0xFF1A1A1A.toInt())
            } else {
                arrayOf(Color.WHITE, Color.BLACK, 0xFFF5F5DC.toInt())
            }
            val newBackgroundColorNames = arrayOf(
                if (isChecked) getString(R.string.black) else getString(R.string.white),
                if (isChecked) "深灰" else getString(R.string.black),
                if (isChecked) "極深" else getString(R.string.parchment)
            )
            spinnerBackgroundColor.adapter = ColorSpinnerAdapter(this, newBackgroundColors, newBackgroundColorNames)
        }
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<android.view.View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.view.View>(R.id.btnOk).setOnClickListener {
            val needsRestart = cbDarkMode.isChecked != settingsManager.isDarkMode
            
            settingsManager.isDarkMode = cbDarkMode.isChecked
            settingsManager.showEnglish = cbChineseEnglish.isChecked
            settingsManager.fontSize = seekBarFontSize.progress
            settingsManager.fontColorIndex = spinnerFontColor.selectedItemPosition
            settingsManager.backgroundColorIndex = spinnerBackgroundColor.selectedItemPosition
            
            getCurrentMainFragment()?.applySettings(
                cbChineseEnglish.isChecked,
                seekBarFontSize.progress,
                spinnerFontColor.selectedItemPosition,
                spinnerBackgroundColor.selectedItemPosition,
                cbDarkMode.isChecked
            )
            
            if (needsRestart) {
                recreate()
            } else {
                applyToolbarTheme()
            }
            
            dialog.dismiss()
        }
        
        dialog.show()
    }

    fun getToolbarSpinners(): Pair<Spinner?, Spinner?> {
        // Try both layouts (normal and responsive)
        val bookSpinner = binding.toolbar.findViewById<Spinner>(R.id.spinnerBooks)
        val chapterSpinner = binding.toolbar.findViewById<Spinner>(R.id.spinnerChapters)
        return Pair(bookSpinner, chapterSpinner)
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}