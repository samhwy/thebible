package com.sam.thebible

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.navigation.NavigationView
import com.sam.thebible.adapter.ColorSpinnerAdapter
import com.sam.thebible.databinding.ActivityMainBinding
import com.sam.thebible.ui.bookmarks.BookmarksFragment
import com.sam.thebible.ui.main.MainFragment
import com.sam.thebible.utils.SettingsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsManager: SettingsManager

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        uri?.let { exportToUri(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importFromUri(it) }
    }.apply {
        // This ensures we can access files from any directory
    }

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
                } else if (supportFragmentManager.backStackEntryCount > 0) {
                    // Pop back stack if there are fragments in the back stack (like BookmarksFragment)
                    supportFragmentManager.popBackStack()
                } else {
                    val fragment = getCurrentMainFragment()
                    if (fragment != null && fragment.exitSearchMode()) {
                        // Search mode was exited, stay in app
                    } else if (fragment != null && fragment.exitBookmarkMode()) {
                        // Bookmark mode was exited, stay in app
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

    fun getCurrentMainFragment(): MainFragment? {
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
        val isEnglish = settingsManager.languageMode == 1

        val cbDarkMode = dialogView.findViewById<CheckBox>(R.id.cbDarkMode)
        val seekBarFontSize = dialogView.findViewById<SeekBar>(R.id.seekBarFontSize)
        val spinnerFontColor = dialogView.findViewById<Spinner>(R.id.spinnerFontColor)
        val spinnerBackgroundColor = dialogView.findViewById<Spinner>(R.id.spinnerBackgroundColor)

        // Set checkbox text based on language
        cbDarkMode.text = getString(if (isEnglish) R.string.dark_mode_en else R.string.dark_mode)

        // Font colors: white, black, green, yellow, orange
        val fontColors = arrayOf(Color.WHITE, Color.BLACK, Color.GREEN, Color.YELLOW, 0xFFFFA500.toInt())
        val fontColorNames = if (isEnglish) arrayOf(
            getString(R.string.white_en),
            getString(R.string.black_en),
            getString(R.string.green_en),
            getString(R.string.yellow_en),
            getString(R.string.orange_en)
        ) else arrayOf(
            getString(R.string.white),
            getString(R.string.black),
            getString(R.string.green),
            getString(R.string.yellow),
            getString(R.string.orange)
        )

        // Background colors: white, black, parchment, dark gray
        val backgroundColors = arrayOf(Color.WHITE, Color.BLACK, R.color.parchment, R.color.dark_gray)
        val backgroundColorNames = if (isEnglish) arrayOf(
            getString(R.string.white_en),
            getString(R.string.black_en),
            getString(R.string.parchment_en),
            getString(R.string.darkgray_en)
        ) else arrayOf(
            getString(R.string.white),
            getString(R.string.black),
            getString(R.string.parchment),
            getString(R.string.darkgray)
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
            .setTitle(getString(if (isEnglish) R.string.settings_en else R.string.settings))
            .setView(dialogView)
            .create()

        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<android.widget.Button>(R.id.btnOk)
        
        btnCancel.text = getString(if (isEnglish) R.string.cancel_en else R.string.cancel)
        btnOk.text = getString(if (isEnglish) R.string.ok_en else R.string.ok)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            val needsRestart = cbDarkMode.isChecked != settingsManager.isDarkMode

            settingsManager.isDarkMode = cbDarkMode.isChecked
            settingsManager.fontSize = seekBarFontSize.progress
            settingsManager.fontColorIndex = spinnerFontColor.selectedItemPosition
            settingsManager.backgroundColorIndex = spinnerBackgroundColor.selectedItemPosition

            getCurrentMainFragment()?.applySettings(
                settingsManager.languageMode,
                seekBarFontSize.progress,
                spinnerFontColor.selectedItemPosition,
                spinnerBackgroundColor.selectedItemPosition
            )

            if (needsRestart) {
                // Save current position before recreating activity
                val fragment = getCurrentMainFragment()
                if (fragment != null) {
                    Log.d("MainActivity", "checkpoint 3: currentChapter: ${fragment.viewModel.currentChapter.value} ")
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

    private fun showBookmarksFragment() {
        // Instead of replacing with BookmarksFragment, use the MainFragment to display bookmarks
        val mainFragment = getCurrentMainFragment()
        if (mainFragment != null) {
            mainFragment.loadBookmarks()
        } else {
            // Fallback to old behavior if MainFragment is not available
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, BookmarksFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupDrawerHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val spinnerLanguage = headerView.findViewById<Spinner>(R.id.spinnerLanguage)

        val languageOptions = when (settingsManager.languageMode) {
            1 -> arrayOf("中文", "English", "TC&Eng") //arrayOf("Chinese", "English", "Chinese+English")
            else -> arrayOf("中文", "English", "中英對照")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        spinnerLanguage.setSelection(settingsManager.languageMode)

        spinnerLanguage.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                settingsManager.languageMode = position
                updateMenuLanguage(position)
                getCurrentMainFragment()?.applySettings(
                    position,
                    settingsManager.fontSize,
                    settingsManager.fontColorIndex,
                    settingsManager.backgroundColorIndex
                )
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        // Set initial menu language
        updateMenuLanguage(settingsManager.languageMode)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_search -> {
                showSearchDialog()
            }
            R.id.nav_settings -> {
                showSettingsDialog()
            }
            R.id.nav_bookmarks -> {
                showBookmarksFragment()
            }
            R.id.nav_export_bookmarks -> {
                exportBookmarks()
            }
            R.id.nav_import_bookmarks -> {
                importBookmarks()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    private fun exportBookmarks() {
        try {
            exportLauncher.launch("thebible.csv")
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val mainFragment = getCurrentMainFragment()
                if (mainFragment == null) {
                    Toast.makeText(this@MainActivity, "Error: Main fragment not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val csvContent = mainFragment.viewModel.exportBookmarks()
                if (csvContent.isBlank()) {
                    Toast.makeText(this@MainActivity, "No bookmarks to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                Toast.makeText(this@MainActivity, "Bookmarks exported successfully", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Export failed", e)
                Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun importBookmarks() {
        importLauncher.launch(arrayOf("*/*"))
    }

    private fun importFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val csvContent = contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: throw Exception("Could not read file")

                val mainFragment = getCurrentMainFragment()
                mainFragment?.viewModel?.importBookmarks(csvContent)
                Toast.makeText(this@MainActivity, "Bookmarks imported successfully", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateMenuLanguage(languageMode: Int) {
        binding.navView.post {
            val menu = binding.navView.menu
            val isEnglish = languageMode == 1 // 0=Chinese, 1=English, 2=Both(Chinese menu)
            
            menu.findItem(R.id.nav_search)?.title = getString(if (isEnglish) R.string.search_en else R.string.search)
            menu.findItem(R.id.nav_bookmarks)?.title = getString(if (isEnglish) R.string.bookmarks_en else R.string.bookmarks)
            menu.findItem(R.id.nav_export_bookmarks)?.title = getString(if (isEnglish) R.string.export_bookmarks_en else R.string.export_bookmarks)
            menu.findItem(R.id.nav_import_bookmarks)?.title = getString(if (isEnglish) R.string.import_bookmarks_en else R.string.import_bookmarks)
            menu.findItem(R.id.nav_settings)?.title = getString(if (isEnglish) R.string.settings_en else R.string.settings)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}