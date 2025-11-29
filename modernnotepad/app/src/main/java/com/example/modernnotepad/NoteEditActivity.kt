package com.example.modernnotepad

import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.modernnotepad.databinding.ActivityNoteEditBinding
import com.example.modernnotepad.database.NoteDatabase
import com.example.modernnotepad.dialog.ColorPickerDialog
import com.example.modernnotepad.model.Note
import com.example.modernnotepad.repository.NoteRepository
import com.example.modernnotepad.utils.ThemeHelper
import com.example.modernnotepad.viewmodel.NoteViewModel
import com.example.modernnotepad.viewmodel.NoteViewModelFactory
import kotlinx.coroutines.launch
import java.util.Date

// 添加扩展函数来检查是否启用了暗色模式
private fun AppCompatActivity.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

class NoteEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteEditBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private var currentNoteId: Long = -1L
    private var hasUnsavedChanges = false
    private var autoSaveJob: kotlinx.coroutines.Job? = null
    private var currentColor: Int = Note.DEFAULT_COLOR
    private var themeChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeFromPreferences(this)
        super.onCreate(savedInstanceState)

        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        
        // Set up theme change listener
        themeChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "theme_preference") {
                // Recreate the activity to apply the new theme
                recreate()
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(themeChangeListener)

        currentColor = intent.getIntExtra("NOTE_COLOR", Note.DEFAULT_COLOR)

        setupEditorBackground()
        setupViewModel()
        setupViews()
        setupTextWatchers()
        setupBackPressHandler()

        if (currentNoteId != -1L) {
            loadNoteData()
        } else {
            updateColorIndicator()
        }
    }

    private fun setupViewModel() {
        val noteDatabase = NoteDatabase.getInstance(application)
        val noteDao = noteDatabase.noteDao()
        val repository = NoteRepository(noteDao)
        val factory = NoteViewModelFactory(repository)
        noteViewModel = ViewModelProvider(this, factory).get(NoteViewModel::class.java)
    }

    private fun setupViews() {
        setupCategoryAutoComplete()
        setupColorPicker()
        setupToolbar()

        binding.btnSave.setOnClickListener {
            saveNoteAndFinish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        currentNoteId = intent.getLongExtra("NOTE_ID", -1L)
        supportActionBar?.title = if (currentNoteId == -1L) getString(R.string.new_note) else getString(R.string.edit_note)
    }

    private fun setupCategoryAutoComplete() {
        noteViewModel.allCategories.observe(this) { categories ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
            binding.actvCategory.setAdapter(adapter)
        }
    }

    private fun setupColorPicker() {
        binding.btnColorPicker.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun showColorPickerDialog() {
        val dialog = ColorPickerDialog()
        dialog.setOnColorSelectedListener { color ->
            currentColor = color
            updateColorIndicator()
            hasUnsavedChanges = true
        }
        dialog.show(supportFragmentManager, ColorPickerDialog.TAG)
    }

    private fun updateColorIndicator() {
        binding.colorIndicator.setBackgroundColor(currentColor)

        // 如果颜色是白色，添加边框
        if (currentColor == Note.DEFAULT_COLOR) {
            binding.colorIndicator.setBackgroundResource(R.drawable.color_indicator_white)
        } else {
            val drawable = ContextCompat.getDrawable(this, R.drawable.color_indicator)
            drawable?.setTint(currentColor)
            binding.colorIndicator.background = drawable
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hasUnsavedChanges = true
                updateCharacterCount()

                if (sharedPreferences.getBoolean("auto_save", true)) {
                    autoSaveJob?.cancel()
                    autoSaveJob = lifecycleScope.launch {
                        kotlinx.coroutines.delay(1000)
                        autoSaveNote()
                    }
                }
            }
        }

        binding.etTitle.addTextChangedListener(textWatcher)
        binding.etContent.addTextChangedListener(textWatcher)
        binding.actvCategory.addTextChangedListener(textWatcher)
    }

    private fun updateCharacterCount() {
        val content = binding.etContent.text.toString()
        binding.tvCharacterCount.text = "${content.length} 字符"
    }

    private suspend fun autoSaveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()

        if (title.isEmpty()) return

        try {
            if (currentNoteId == -1L) {
                val newNote = Note(
                    title = title,
                    content = content,
                    category = if (category.isEmpty()) "默认" else category,
                    color = currentColor,
                    createdAt = Date().time,
                    updatedAt = Date().time
                )
                noteViewModel.insertNote(newNote) { id ->
                    currentNoteId = id
                }
            } else {
                val oldNote = noteViewModel.getNoteById(currentNoteId)
                oldNote?.let {
                    val updatedNote = it.copy(
                        title = title,
                        content = content,
                        category = if (category.isEmpty()) "默认" else category,
                        color = currentColor,
                        updatedAt = Date().time
                    )
                    noteViewModel.updateNote(updatedNote)
                }
            }
            hasUnsavedChanges = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadNoteData() {
        lifecycleScope.launch {
            try {
                val note = noteViewModel.getNoteById(currentNoteId)
                note?.let {
                    binding.etTitle.setText(it.title)
                    binding.etContent.setText(it.content)
                    binding.actvCategory.setText(it.category)
                    currentColor = it.color
                    updateColorIndicator()
                    updateCharacterCount()
                    hasUnsavedChanges = false
                } ?: run {
                    Toast.makeText(this@NoteEditActivity, R.string.note_not_exist, Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@NoteEditActivity, "加载笔记失败", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupBackPressHandler() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges) {
                    showUnsavedChangesDialog()
                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("未保存的更改")
            .setMessage("您有未保存的更改，是否保存？")
            .setPositiveButton("保存") { dialog, _ ->
                saveNoteAndFinish()
                dialog.dismiss()
            }
            .setNegativeButton("不保存") { dialog, _ ->
                finish()
                dialog.dismiss()
            }
            .setNeutralButton("取消", null)
            .show()
    }

    private fun saveNoteAndFinish() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.enter_title, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (currentNoteId == -1L) {
                    val newNote = Note(
                        title = title,
                        content = content,
                        category = if (category.isEmpty()) "默认" else category,
                        color = currentColor,
                        createdAt = Date().time,
                        updatedAt = Date().time
                    )
                    noteViewModel.insertNote(newNote)
                    Toast.makeText(this@NoteEditActivity, "笔记已创建", Toast.LENGTH_SHORT).show()
                } else {
                    val oldNote = noteViewModel.getNoteById(currentNoteId)
                    oldNote?.let {
                        val updatedNote = it.copy(
                            title = title,
                            content = content,
                            category = if (category.isEmpty()) "默认" else category,
                            color = currentColor,
                            updatedAt = Date().time
                        )
                        noteViewModel.updateNote(updatedNote)
                        Toast.makeText(this@NoteEditActivity, "笔记已更新", Toast.LENGTH_SHORT).show()
                    }
                }
                hasUnsavedChanges = false
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@NoteEditActivity, "保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupEditorBackground() {
        val background = sharedPreferences.getString("editor_background", "default")
        val rootLayout = binding.root
        
        // 检查是否启用了暗色模式
        val isDarkMode = isDarkThemeOn()

        when (background) {
            "green" -> {
                if (isDarkMode) {
                    // 在暗色模式下使用较深的绿色
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background))
                } else {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.eye_protection_green))
                }
            }
            "gray" -> {
                if (isDarkMode) {
                    // 在暗色模式下使用深灰色
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background))
                } else {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray_background))
                }
            }
            "blue" -> {
                if (isDarkMode) {
                    // 在暗色模式下使用深色背景
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background))
                } else {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue_background))
                }
            }
            else -> {
                // 默认情况下，在暗色模式下使用深色背景，在亮色模式下使用白色背景
                if (isDarkMode) {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background))
                } else {
                    rootLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (hasUnsavedChanges) {
            showUnsavedChangesDialog()
        } else {
            finish()
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        autoSaveJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        ThemeHelper.applyThemeFromPreferences(this)
        setupEditorBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the listener to avoid memory leaks
        themeChangeListener?.let {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(it)
        }
    }
}