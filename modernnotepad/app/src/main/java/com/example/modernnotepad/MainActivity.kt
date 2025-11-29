package com.example.modernnotepad

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.modernnotepad.adapter.NoteAdapter
import com.example.modernnotepad.database.NoteDatabase
import com.example.modernnotepad.databinding.ActivityMainBinding
import com.example.modernnotepad.dialog.ColorPickerDialog
import com.example.modernnotepad.model.Note
import com.example.modernnotepad.repository.NoteRepository
import com.example.modernnotepad.utils.ThemeHelper
import com.example.modernnotepad.viewmodel.NoteViewModel
import com.example.modernnotepad.viewmodel.NoteViewModelFactory
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private var isSearching = false
    private var currentViewMode = VIEW_MODE_LIST
    private var themeChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    companion object {
        private const val VIEW_MODE_LIST = 0
        private const val VIEW_MODE_GRID = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeFromPreferences(this)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
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

        setupToolbar()
        setupRecyclerView()
        setupViewModel()
        setupCategoryFilter()
        setupSearchFunctionality()
        setupFabMenu()
        setupViewMode()

        observeNotes()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onNoteClick = { note ->
                openNoteEditor(note.id)
            },
            onNoteLongClick = { note ->
                showNoteOptionsMenu(note)
            },
            onNotePin = { note ->
                noteViewModel.togglePinStatus(note)
            }
        )

        updateLayoutManager()
        binding.rvNotes.adapter = noteAdapter
        binding.rvNotes.setHasFixedSize(true)
    }

    private fun updateLayoutManager() {
        binding.rvNotes.layoutManager = when (currentViewMode) {
            VIEW_MODE_GRID -> StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            else -> LinearLayoutManager(this)
        }
    }

    private fun setupViewModel() {
        val noteDatabase = NoteDatabase.getInstance(application)
        val noteDao = noteDatabase.noteDao()
        val repository = NoteRepository(noteDao)
        val factory = NoteViewModelFactory(repository)
        noteViewModel = ViewModelProvider(this, factory).get(NoteViewModel::class.java)
    }

    private fun setupCategoryFilter() {
        noteViewModel.allCategories.observe(this) { categories ->
            binding.chipGroupCategories.removeAllViews()

            // 添加"全部"芯片
            val chipAll = Chip(this).apply {
                text = "全部"
                isCheckable = true
                isChecked = true
                setOnClickListener {
                    loadAllNotes()
                }
            }
            binding.chipGroupCategories.addView(chipAll)

            // 添加分类芯片
            categories.forEach { category ->
                if (category != "全部") {
                    val chip = Chip(this).apply {
                        text = category
                        isCheckable = true
                        setOnClickListener {
                            filterByCategory(category)
                        }
                        setOnLongClickListener {
                            showDeleteCategoryDialog(category)
                            true
                        }
                    }
                    binding.chipGroupCategories.addView(chip)
                }
            }
        }
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                isSearching = query.isNotEmpty()
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    loadAllNotes()
                }
            }
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotEmpty()) performSearch(query)
                true
            } else false
        }
    }

    private fun setupFabMenu() {
        binding.fabAddNote.setOnClickListener {
            openNoteEditor()
        }

        binding.fabColorNote.setOnClickListener {
            openNoteEditorWithColor()
        }

        binding.fabQuickNote.setOnClickListener {
            createQuickNote()
        }
    }

    private fun setupViewMode() {
        currentViewMode = sharedPreferences.getInt("view_mode", VIEW_MODE_LIST)
        updateViewModeIndicator()
    }

    private fun updateViewModeIndicator() {
        binding.btnViewMode.setImageResource(
            if (currentViewMode == VIEW_MODE_GRID) R.drawable.ic_list else R.drawable.ic_grid
        )
    }

    private fun observeNotes() {
        noteViewModel.allNotes.observe(this) { notes ->
            if (!isSearching) {
                noteAdapter.submitList(notes)
                updateEmptyState(notes.isEmpty())
                updateStats(notes)
            }
        }
    }

    private fun updateStats(notes: List<Note>) {
        val totalNotes = notes.size
        val pinnedNotes = notes.count { it.isPinned }
        val recentNotes = notes.count { it.updatedAt > System.currentTimeMillis() - 24 * 60 * 60 * 1000 }

        binding.statsContainer.visibility = if (totalNotes > 0) View.VISIBLE else View.GONE
        binding.tvTotalNotes.text = totalNotes.toString()
        binding.tvPinnedNotes.text = pinnedNotes.toString()
        binding.tvRecentNotes.text = recentNotes.toString()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_view_mode -> {
                toggleViewMode()
                true
            }
            R.id.action_sort -> {
                showSortMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleViewMode() {
        currentViewMode = if (currentViewMode == VIEW_MODE_LIST) VIEW_MODE_GRID else VIEW_MODE_LIST
        sharedPreferences.edit().putInt("view_mode", currentViewMode).apply()
        updateViewModeIndicator()
        updateLayoutManager()
    }

    private fun showSortMenu() {
        val popup = PopupMenu(this, binding.toolbar)
        popup.menuInflater.inflate(R.menu.sort_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.sort_date -> {
                    Toast.makeText(this, "按日期排序", Toast.LENGTH_SHORT).show()
                }
                R.id.sort_title -> {
                    Toast.makeText(this, "按标题排序", Toast.LENGTH_SHORT).show()
                }
                R.id.sort_color -> {
                    Toast.makeText(this, "按颜色排序", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        popup.show()
    }

    private fun openNoteEditor(noteId: Long = -1L) {
        val intent = Intent(this, NoteEditActivity::class.java)
        if (noteId != -1L) intent.putExtra("NOTE_ID", noteId)
        startActivity(intent)
    }

    private fun openNoteEditorWithColor() {
        val dialog = ColorPickerDialog()
        dialog.setOnColorSelectedListener { color ->
            val intent = Intent(this, NoteEditActivity::class.java)
            intent.putExtra("NOTE_COLOR", color)
            startActivity(intent)
        }
        dialog.show(supportFragmentManager, ColorPickerDialog.TAG)
    }

    private fun createQuickNote() {
        lifecycleScope.launch {
            val quickNote = Note(
                title = "快速笔记",
                content = "创建于 ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                color = Note.DEFAULT_COLOR,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            noteViewModel.insertNote(quickNote) { id ->
                openNoteEditor(id)
            }
        }
    }

    private fun showNoteOptionsMenu(note: Note) {
        val popup = PopupMenu(this, binding.rvNotes)
        popup.menuInflater.inflate(R.menu.note_context_menu, popup.menu)

        // 更新置顶按钮文本
        val pinItem = popup.menu.findItem(R.id.action_pin)
        pinItem.title = if (note.isPinned) "取消置顶" else "置顶"

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> openNoteEditor(note.id)
                R.id.action_pin -> noteViewModel.togglePinStatus(note)
                R.id.action_share -> shareNote(note)
                R.id.action_delete -> showDeleteNoteDialog(note)
                R.id.action_change_color -> changeNoteColor(note)
                else -> false
            }
            true
        }
        popup.show()
    }

    private fun shareNote(note: Note) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title)
            putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.content}")
        }
        startActivity(Intent.createChooser(shareIntent, "分享笔记"))
    }

    private fun changeNoteColor(note: Note) {
        val dialog = ColorPickerDialog()
        dialog.setOnColorSelectedListener { color ->
            val updatedNote = note.copy(color = color, updatedAt = System.currentTimeMillis())
            noteViewModel.updateNote(updatedNote)
            // Show a toast to confirm the color change
            Toast.makeText(this, "笔记颜色已更改", Toast.LENGTH_SHORT).show()
            
            // Refresh the note list to reflect the color change immediately
            loadAllNotes()
        }
        dialog.show(supportFragmentManager, ColorPickerDialog.TAG)
    }

    private fun showDeleteNoteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("删除笔记")
            .setMessage("确定要删除\"${note.title}\"吗？")
            .setPositiveButton("删除") { dialog, _ ->
                noteViewModel.deleteNote(note)
                Toast.makeText(this, "笔记已删除", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun loadAllNotes() {
        noteViewModel.allNotes.observe(this) { notes ->
            noteAdapter.submitList(notes)
            updateEmptyState(notes.isEmpty())
        }
    }

    private fun filterByCategory(category: String) {
        noteViewModel.getNotesByCategory(category).observe(this) { notes ->
            noteAdapter.submitList(notes)
            updateEmptyState(notes.isEmpty())
        }
    }

    private fun performSearch(query: String) {
        val searchQuery = "%$query%"
        noteViewModel.searchNotes(searchQuery).observe(this) { notes ->
            noteAdapter.submitList(notes)
            updateEmptyState(notes.isEmpty())

            if (notes.isEmpty()) {
                val emptyStateText = binding.emptyState.root.findViewById<android.widget.TextView>(R.id.tv_empty_state)
                emptyStateText?.text = getString(R.string.no_notes_found_for_search, query)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.root.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvNotes.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.statsContainer.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showDeleteCategoryDialog(category: String) {
        lifecycleScope.launch {
            try {
                val noteCount = noteViewModel.getNoteCountByCategory(category)
                val message = if (noteCount > 0) {
                    "删除分类 \"$category\" 将会把该分类下的 $noteCount 个笔记移动到默认分类。确定要删除吗？"
                } else {
                    "确定要删除分类 \"$category\" 吗？"
                }

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("删除分类")
                    .setMessage(message)
                    .setPositiveButton("删除") { dialog, _ ->
                        deleteCategory(category)
                        dialog.dismiss()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "获取分类信息失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCategory(category: String) {
        lifecycleScope.launch {
            try {
                val noteCount = noteViewModel.getNoteCountByCategory(category)
                if (noteCount > 0) {
                    noteViewModel.updateNotesCategory(category, "默认")
                    Toast.makeText(this@MainActivity, "已将 $noteCount 个笔记移动到默认分类", Toast.LENGTH_SHORT).show()
                }
                noteViewModel.deleteNotesByCategory(category)
                Toast.makeText(this@MainActivity, "分类 \"$category\" 已删除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "删除分类失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ThemeHelper.applyThemeFromPreferences(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the listener to avoid memory leaks
        themeChangeListener?.let {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(it)
        }
    }
}