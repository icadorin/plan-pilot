package com.israel.planpilot

import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentAddActivity : Fragment() {

    companion object {
        private const val ARG_SELECTED_DAY = "selected_day"
        private const val ARG_SELECTED_MONTH = "selected_month"
        private const val ARG_SELECTED_YEAR = "selected_year"

        fun newInstance(
            selectedDay: Int,
            selectedMonth: Int,
            selectedYear: Int
        ): FragmentAddActivity {
            val fragment = FragmentAddActivity()
            val args = Bundle()
            args.putInt(ARG_SELECTED_DAY, selectedDay)
            args.putInt(ARG_SELECTED_MONTH, selectedMonth)
            args.putInt(ARG_SELECTED_YEAR, selectedYear)
            fragment.arguments = args

            return fragment
        }
    }

    // ToDo
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->

        }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setActionBarIcon(R.drawable.ic_menu_white)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_activity, container, false)
    }

    private fun hideKeyboard(view: View?) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        view?.clearFocus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedDay = arguments?.getInt(ARG_SELECTED_DAY)
        val selectedMonth = arguments?.getInt(ARG_SELECTED_MONTH)
        val selectedYear = arguments?.getInt(ARG_SELECTED_YEAR)

        val monthNames = arrayOf(
            "JAN", "FEV", "MAR", "ABR",
            "MAI", "JUN", "JUL", "AGO",
            "SET", "OUT", "NOV", "DEZ"
        )

        val monthName = "${monthNames[selectedMonth!! - 1]}."

        view.findViewById<TextView>(R.id.selectedMonth).text = monthName
        view.findViewById<TextView>(R.id.selectedYear).text = selectedYear.toString()
        view.findViewById<TextView>(R.id.selectedDay).text = selectedDay.toString()

        val nameActivity = view.findViewById<EditText>(R.id.nameActivity)
        val backspaceButton = view.findViewById<Button>(R.id.backspaceButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val triggerTime = view.findViewById<Button>(R.id.triggerTime)
        val closeButton = view.findViewById<Button>(R.id.closeButton)
        val alarmSwitch = view.findViewById<SwitchCompat>(R.id.alarmSwitch)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val timePicker = view.findViewById<Button>(R.id.timePicker)
        var alarmActivated = false

        nameActivity.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                nameActivity.gravity = Gravity.START
                nameActivity.hint = ""
                backspaceButton.visibility = View.VISIBLE
                cancelButton.visibility = View.VISIBLE
                nameActivity.setSelection(nameActivity.text.length)
            } else {
                nameActivity.gravity = Gravity.CENTER
                nameActivity.hint = getString(R.string.activity_name)
                backspaceButton.visibility = View.GONE
                cancelButton.visibility = View.GONE
            }
        }

        backspaceButton.setOnClickListener {
            nameActivity.text.clear()
        }

        cancelButton.setOnClickListener {
            nameActivity.text.clear()
            hideKeyboard(nameActivity)
        }

        nameActivity.imeOptions = EditorInfo.IME_ACTION_DONE
        nameActivity.setRawInputType(InputType.TYPE_CLASS_TEXT)

        nameActivity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(nameActivity)
                true
            } else {
                false
            }
        }

        triggerTime.setOnClickListener {
            timePicker.visibility = View.VISIBLE
            alarmSwitch.visibility = View.VISIBLE
            triggerTime.visibility = View.GONE
            closeButton.visibility = View.VISIBLE
        }

        closeButton.setOnClickListener {
            timePicker.visibility = View.GONE
            alarmSwitch.visibility = View.GONE
            closeButton.visibility = View.GONE
            triggerTime.visibility = View.VISIBLE
        }

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            alarmActivated = isChecked
        }

        timePicker.setOnClickListener {
            val now = Calendar.getInstance()
            val timePickerDialog = com.wdullaer.materialdatetimepicker.time
                .TimePickerDialog
                .newInstance(
                    { _, hourOfDay, minute, _ ->
                        val time = String.format("%02d:%02d", hourOfDay, minute)
                        timePicker.text = time
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            )

            timePickerDialog.accentColor = ContextCompat.getColor(
                requireContext(),
                R.color.midnight_purple
            )

            timePickerDialog.show(childFragmentManager, "TimePickerDialog")
        }

        saveButton.setOnClickListener {

            val repository = ActivityRepository(requireContext())
            try {
                val name = nameActivity.text.toString().trim()
                if (TextUtils.isEmpty(name)) {
                    nameActivity.error = "Nome da atividade é obrigatório"
                } else {
                    val time = timePicker.text.toString()
                    val activity = Activity(
                        name = name,
                        day = selectedDay,
                        month = selectedMonth,
                        year = selectedYear,
                        time = time,
                        contactForMessage = null,
                        alarmTriggerTime = null,
                        alarmActivated = alarmActivated,
                        category = null
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            repository.createActivity(activity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }
}
