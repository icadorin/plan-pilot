package com.israel.planpilot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.util.Date

class FragmentAddActivity : Fragment() {

    companion object {
        private const val ARG_SELECTED_DAY = "selected_day"

        fun newInstance(selectedDay: Int): FragmentAddActivity {
            val fragment = FragmentAddActivity()
            val args = Bundle()
            args.putInt(ARG_SELECTED_DAY, selectedDay)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedDay = arguments?.getInt(ARG_SELECTED_DAY)

    }
}

