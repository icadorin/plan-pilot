package com.israel.planpilot

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class AddActivityDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_activity, null, false)

            builder.setView(view)
            builder.create()
        } ?: throw IllegalStateException("A atividade não pode ser nula")
    }
}
