package com.example.madlab5_medcare_timendate_switch_toggle

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.example.madlab5_medcare_timendate_switch_toggle.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pad the scroll area (not the root) so the background reaches the screen edges
        ViewCompat.setOnApplyWindowInsetsListener(binding.scroll) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            insets
        }

        // Medicine code: clear the error as soon as the user types
        binding.medName.doAfterTextChanged { binding.medNameLayout.error = null }

        // Pickers only show for delivery
        binding.radioGroup.setOnCheckedChangeListener { _, _ -> updatePickerVisibility() }
        updatePickerVisibility()

        // Auto-refill switch: only the label changes
        updateAutoRefillLabel(binding.mySwitch.isChecked)
        binding.mySwitch.setOnCheckedChangeListener { _, isChecked ->
            updateAutoRefillLabel(isChecked)
            // TODO: enable/disable auto-refill
        }

        binding.submitButton.setOnClickListener { submit() }
    }

    private fun updatePickerVisibility() {
        val visibility =
            if (binding.radioGroup.checkedRadioButtonId == R.id.delivery) View.VISIBLE else View.GONE
        binding.datePicker.visibility = visibility
        binding.timePicker.visibility = visibility
    }

    private fun updateAutoRefillLabel(enabled: Boolean) {
        binding.mySwitch.setText(
            if (enabled) R.string.auto_refill_on else R.string.auto_refill_off
        )
    }

    private fun yesNo(value: Boolean) = getString(if (value) R.string.yes else R.string.no)

    private fun submit() {
        val code = binding.medName.text?.toString()?.trim().orEmpty()
        if (code.isEmpty()) {
            binding.medNameLayout.error = getString(R.string.error_code_required)
            return
        }

        val reminders = yesNo(binding.enableNotifications.isChecked)
        val autoRefill = yesNo(binding.mySwitch.isChecked)

        val summary = if (binding.radioGroup.checkedRadioButtonId == R.id.delivery) {
            val cal = Calendar.getInstance().apply {
                set(
                    binding.datePicker.year, binding.datePicker.month, binding.datePicker.dayOfMonth,
                    binding.timePicker.hour, binding.timePicker.minute, 0
                )
            }
            if (cal.timeInMillis < System.currentTimeMillis()) {
                Snackbar.make(binding.root, R.string.error_past_time, Snackbar.LENGTH_SHORT).show()
                return
            }
            val date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(cal.time)
            val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.time)
            getString(R.string.summary_delivery, code, date, time, reminders, autoRefill)
        } else {
            getString(R.string.summary_store, code, reminders, autoRefill)
        }

        binding.summary.text = summary
    }
}