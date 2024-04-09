package com.israel.planpilot

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class FragmentAddActivity2 : Fragment() {

    companion object {
        private const val ARG_SELECTED_DAY = "selected_day"
        private const val ARG_SELECTED_MONTH = "selected_month"
        private const val ARG_SELECTED_YEAR = "selected_year"

        fun newInstance(
            selectedDay: Int,
            selectedMonth: Int,
            selectedYear: Int
        ): FragmentAddActivity2 {
            val fragment = FragmentAddActivity2()
            val args = Bundle()
            args.putInt(ARG_SELECTED_DAY, selectedDay)
            args.putInt(ARG_SELECTED_MONTH, selectedMonth)
            args.putInt(ARG_SELECTED_YEAR, selectedYear)
            fragment.arguments = args

            return fragment
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.SET_ALARM)) {
                showExplanationAndAskAgain()
            }
        }
    }

    private fun showExplanationAndAskAgain() {
        requestPermissionLauncher.launch(Manifest.permission.SET_ALARM)
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
        val alarmSwitch = view.findViewById<SwitchCompat>(R.id.alarmSwitch)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val timePicker = view.findViewById<Button>(R.id.timePicker)
        val alarmTone = view.findViewById<ImageButton>(R.id.alarmTone)

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
        nameActivity.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        nameActivity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(nameActivity)
                true
            } else {
                false
            }
        }

        alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
            ActivityUtils.setAlarmSwitchListener(isChecked)
        }

        timePicker.setOnClickListener {
            ActivityUtils.setTimePicker(timePicker, childFragmentManager)
        }

        alarmTone.setOnClickListener {
            ActivityUtils.setupAlarmToneButton(view, requireContext())
        }

//        saveButton.setOnClickListener {
//            ActivityUtils.saveActivity(
//                view,
//                nameActivity,
//                timePicker,
//                alarmSwitch,
//                selectedDay,
//                selectedMonth,
//                selectedYear,
//                lifecycleScope,
//                context
//            )
//        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }
}