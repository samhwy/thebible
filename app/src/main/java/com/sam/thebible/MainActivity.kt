package com.sam.thebible

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.navigation.NavigationView
import com.sam.thebible.adapter.ColorSpinnerAdapter
import com.sam.thebible.databinding.ActivityMainBinding
import com.sam.thebible.ui.main.MainFragment
import com.sam.thebible.utils.SettingsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

        // Setup navigation drawer
        binding.navView.setNavigationItemSelectedListener(this)
        setupDrawerHeader()
        
        // Setup toolbar controls after fragment is loaded
        navController.addOnDestinationChangedListener { _, _, _ ->
            setupToolbarControls()
        }

        // Setup back button handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    val fragment = getCurrentMainFragment()
                    if (fragment != null && fragment.exitSearchMode()) {
                        // Search mode was exited, stay in app
                    } else {
                        finish()
                    }
                }
            }
        })
    }

    private fun setupToolbarControls() {
        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnPrevChapter)?.setOnClickListener {
            getCurrentMainFragment()?.onPrevChapter()
        }

        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnNextChapter)?.setOnClickListener {
            getCurrentMainFragment()?.onNextChapter()
        }

        binding.toolbar.findViewById<android.widget.ImageButton>(R.id.btnMenu)?.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
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



    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)

        val cbDarkMode = dialogView.findViewById<CheckBox>(R.id.cbDarkMode)
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

        // Background colors: white, black, parchment, dark gray, very dark
        val backgroundColors = arrayOf(Color.WHITE, Color.BLACK, 0xFFF5F5DC.toInt(), 0xFF2D2D2D.toInt(), 0xFF1A1A1A.toInt())
        val backgroundColorNames = arrayOf(
            getString(R.string.white),
            getString(R.string.black),
            getString(R.string.parchment),
            "深灰",
            "極深"
        )

        val fontColorAdapter = ColorSpinnerAdapter(this, fontColors, fontColorNames)
        val backgroundColorAdapter = ColorSpinnerAdapter(this, backgroundColors, backgroundColorNames)

        spinnerFontColor.adapter = fontColorAdapter
        spinnerBackgroundColor.adapter = backgroundColorAdapter

        // Set current values
        cbDarkMode.isChecked = settingsManager.isDarkMode
        seekBarFontSize.progress = settingsManager.fontSize
        spinnerFontColor.setSelection(settingsManager.fontColorIndex)
        spinnerBackgroundColor.setSelection(settingsManager.backgroundColorIndex)



        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<android.view.View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.view.View>(R.id.btnOk).setOnClickListener {
            val needsRestart = cbDarkMode.isChecked != settingsManager.isDarkMode

            settingsManager.isDarkMode = cbDarkMode.isChecked
            settingsManager.fontSize = seekBarFontSize.progress
            settingsManager.fontColorIndex = spinnerFontColor.selectedItemPosition
            settingsManager.backgroundColorIndex = spinnerBackgroundColor.selectedItemPosition

            getCurrentMainFragment()?.applySettings(
                settingsManager.showEnglish,
                seekBarFontSize.progress,
                spinnerFontColor.selectedItemPosition,
                spinnerBackgroundColor.selectedItemPosition,
                cbDarkMode.isChecked
            )

            if (needsRestart) {
                // Save current position before recreating activity
                val fragment = getCurrentMainFragment()
                if (fragment != null) {
                    val currentBook = fragment.viewModel.currentBook.value
                    val currentChapter = fragment.viewModel.currentChapter.value ?: 1
                    if (currentBook != null) {
                        settingsManager.saveCurrentPosition(currentBook.code, currentChapter)
                    }
                }
                recreate()
            } else {
                applyToolbarTheme()
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    fun getToolbarSpinners(): Pair<Spinner?, Spinner?> {
        val bookSpinner = binding.toolbar.findViewById<Spinner>(R.id.spinnerBooks)
        val chapterSpinner = binding.toolbar.findViewById<Spinner>(R.id.spinnerChapters)
        return Pair(bookSpinner, chapterSpinner)
    }

    private fun setupDrawerHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val cbChineseEnglish = headerView.findViewById<CheckBox>(R.id.cbChineseEnglish)
        
        cbChineseEnglish.isChecked = settingsManager.showEnglish
        cbChineseEnglish.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.showEnglish = isChecked
            getCurrentMainFragment()?.applySettings(
                isChecked,
                settingsManager.fontSize,
                settingsManager.fontColorIndex,
                settingsManager.backgroundColorIndex,
                settingsManager.isDarkMode
            )
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_search -> {
                showSearchDialog()
            }
            R.id.nav_settings -> {
                showSettingsDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}