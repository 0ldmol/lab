package com.example.modernnotepad

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.modernnotepad.databinding.ActivitySettingsBinding
import com.example.modernnotepad.utils.ThemeHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeFromPreferences(this)
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setupViews()
        loadSettingsFragment()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadSettingsFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsContainer, SettingsFragment())
            .commit()
    }

    override fun onResume() {
        super.onResume()
        ThemeHelper.applyThemeFromPreferences(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
        
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            // Handle theme preference changes immediately
            if (preference.key == "theme_preference") {
                // Apply the theme change immediately
                ThemeHelper.applyThemeFromPreferences(requireContext())
                // Recreate the activity to apply the new theme
                requireActivity().recreate()
                return true
            }
            return super.onPreferenceTreeClick(preference)
        }
    }
}