package com.israel.planpilot

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.israel.planpilot.Constants.NO_ICON
import com.israel.planpilot.Constants.ICON_TEXT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HomeFragmentDEF : Fragment() {

    private lateinit var activityRepository: ActivityRepository
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    private lateinit var calendarView: CalendarView
    private lateinit var btnAddActivity: ImageButton
    private lateinit var btnClearActivities: ImageButton
    private lateinit var listViewActivities: ListView
    private lateinit var activitiesAdapter: ActivityItemAdapter
    private lateinit var jsonFile: File
    private var btnIcon: Button? = null
    private var selectedDateMillis: Long = 0
    private var selectedActivityId: String? = null
    private var selectedIconResource: Int = NO_ICON
    private var selectedIconResourceGlobal: Int = R.drawable.ic_default_icon

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_def, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityRepository = ActivityRepository(requireContext())
        jsonFile = File(requireContext().filesDir, "activities.json")

        calendarView = view.findViewById(R.id.calendarView)
        btnAddActivity = view.findViewById(R.id.btnAddActivity) as ImageButton
        btnClearActivities = view.findViewById(R.id.btnClearActivities) as ImageButton
        listViewActivities = view.findViewById(R.id.listViewActivities)

        activitiesAdapter = ActivityItemAdapter(
            requireContext(), R.layout.list_item_activity, mutableListOf()
        )

        listViewActivities.adapter = activitiesAdapter

        btnAddActivity.setOnClickListener {
            addActivity(selectedDateMillis)
        }

        btnClearActivities.setOnClickListener {
            clearAllActivities()
        }

        selectedDateMillis = System.currentTimeMillis()

        calendarView.date = selectedDateMillis

        updateListView(selectedDateMillis)

        listViewActivities.setOnItemClickListener { _, _, position, _ ->
            val activityToDeleteOrEdit = activitiesAdapter.getItem(position)

            if (activityToDeleteOrEdit != null) {
                lifecycleScope.launch {
                    val activityId = getActivityIdByName(activityToDeleteOrEdit.name)
                    println("activityToDeleteOrEdit.name ${activityToDeleteOrEdit.name}")
                    val creationTime: String = getCreationTime(activityId ?: "")

                    if (activityId != null) {
                        val activityItem = ActivityItem(
                            activityId,
                            activityToDeleteOrEdit.name,
                            formattedDate(selectedDateMillis),
                            creationTime,
                            selectedIconResource
                        )

                        showOptionsDialog(activityItem)
                    } else {
                        println("ID da atividade não encontrado (listViewActivities)")
                    }
                }
            }
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateMillis = getDateInMillis(year, month, dayOfMonth)
            updateListView(selectedDateMillis)
        }
    }

    private fun updateListView(selectedDateMillis: Long) {
        activitiesAdapter.clear()
        val formattedDate = formattedDate(selectedDateMillis)

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val activities = activityRepository.getAllActivities()
                val filteredActivities = activities.filter { it.date == formattedDate }
                    .sortedBy { it.time }

                withContext(Dispatchers.Main) {
                    for (activityItem in filteredActivities) {
                        activitiesAdapter.add(activityItem)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Erro ao carregar atividades do arquivo JSON: $e")
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun formattedDate(selectedDateMillis: Long): String {
        return SimpleDateFormat("yyyyMMdd").format(Date(selectedDateMillis))
    }

    private fun showOptionsDialog(activityItem: ActivityItem) {
        val options = arrayOf("Editar", "Excluir")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Opções")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    selectedActivityId = activityItem.id
                    editActivityOption(activityItem)
                }
                1 -> {
                    deleteActivityOption(activityItem)
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun addActivity(selectedDateMillis: Long) {
        val currentDateTime = SimpleDateFormat(
            "HH:mm:ss", Locale.getDefault()
        ).format(Date())

        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
        btnIcon = dialogView.findViewById(R.id.btnIcon)
        this.selectedIconResource = NO_ICON
        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)

        "Adicionar Atividade".also { dialogTitle.text = it }

        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val input = dialogView.findViewById<EditText>(R.id.editTextActivityName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val alertDialog = builder.create()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnIcon?.setOnClickListener {
            selectIconAndSetBtnIcon(selectedIconResource, requireContext())
        }

        btnSave.setOnClickListener {
            val activityName = input.text.toString()
            Log.d("SaveButton", "Activity Name: $activityName")

            if (activityName.isNotEmpty()) {
                val formattedDate = formattedDate(selectedDateMillis)
                val activityId = UUID.randomUUID().toString()
                val selectedIcon = if (selectedIconResource != NO_ICON) selectedIconResource
                else R.drawable.ic_default_icon

                val activity = ActivityItem(
                    activityId,
                    activityName,
                    formattedDate,
                    currentDateTime,
                    selectedIcon
                )

                coroutineScope.launch {
                    activityRepository.saveActivity(activity)
                    alertDialog.dismiss()
                    updateListView(selectedDateMillis)
                }
            }
        }

        alertDialog.show()
    }

    private fun editActivityOption(activityItem: ActivityItem) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)
        val input = dialogView.findViewById<EditText>(R.id.editTextActivityName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        btnIcon = dialogView.findViewById(R.id.btnIcon)

        "Editar Atividade".also { dialogTitle.text = it }
        input.setText(activityItem.name)

        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog = builder.create()

        val ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch(Dispatchers.Main) {
            val resourceActivityItem: ActivityItem? = getActivityById(activityItem.id)
            val iconId: Int = resourceActivityItem?.iconResource ?: NO_ICON

            updateIcon(iconId)
            alertDialog.show()
        }

        btnIcon?.setOnClickListener {
            selectIconAndSetBtnIcon(selectedIconResource, requireContext())
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSave.setOnClickListener {
            val editedActivityName = input.text.toString()
            if (editedActivityName.isNotEmpty()) {
                if (activityItem.id.isNotEmpty()) {
                    activityItem.name = editedActivityName
                    activityItem.iconResource = selectedIconResourceGlobal

                    ioScope.launch {
                        activityRepository.saveActivity(activityItem, isEditOperation = true)
                        withContext(Dispatchers.Main) {
                            alertDialog.dismiss()
                            updateListView(selectedDateMillis)
                        }
                    }
                } else {
                    println("ID da atividade não encontrado")
                }
            }
            selectedIconResourceGlobal = R.drawable.ic_default_icon
        }
    }

    private fun updateIcon(iconId: Int) {
        if (iconId != NO_ICON && btnIcon != null) {

            val iconWithPadding = ContextCompat.getDrawable(requireContext(), iconId)

            val paddingInPixels =
                resources.getDimensionPixelSize(R.dimen.left_padding)

            val insetDrawable = InsetDrawable(
                iconWithPadding, paddingInPixels, 0, paddingInPixels, 0
            )

            btnIcon?.setCompoundDrawablesWithIntrinsicBounds(
                insetDrawable, null, null, null
            )

            btnIcon?.text = null

            selectedIconResource = iconId
            selectedIconResourceGlobal = selectedIconResource
        } else if (btnIcon != null) {
            btnIcon?.text = ICON_TEXT
            selectedIconResourceGlobal = R.drawable.ic_default_icon
        }
    }

    private fun deleteActivityOption(activityItem: ActivityItem) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Excluir Atividade")
        builder.setMessage("Deseja excluir a atividade '${activityItem.name}'?")

        builder.setPositiveButton("Excluir") { _, _ ->
            val activityId = activityItem.id
            if (activityId.isNotEmpty()) {
                coroutineScope.launch {
                    activityRepository.deleteActivityById(activityId)
                    withContext(Dispatchers.Main) {
                        updateListView(selectedDateMillis)
                    }
                }
            } else {
                println("ID da atividade não encontrado")
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun getDateInMillis(year: Int, month: Int, dayOfMonth: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 0, 0, 0)
        return calendar.timeInMillis
    }

    private fun clearAllActivities() {
        if (activitiesAdapter.count == 0) {
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Limpar todas as atividades desta data?")
        builder.setMessage("Deseja limpar todas as atividades? Esta ação não pode ser desfeita.")

        builder.setPositiveButton("Limpar") { _, _ ->
            activitiesAdapter.clear()

            val formattedDate = formattedDate(selectedDateMillis)

            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val activities = coroutineScope.async {
                        activityRepository.loadActivitiesFromFile() }.await()

                    jsonFile.writeText("")

                    activities.filter { it.date != formattedDate }.forEach { activityItem ->
                        coroutineScope.launch {
                            activityRepository.saveActivity(activityItem)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        println("Atividades da data $formattedDate removidas com sucesso")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        println("Erro ao limpar atividades do arquivo JSON: $e")
                    }
                }
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private suspend fun getActivityIdByName(activityName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val activities = coroutineScope.async {
                    activityRepository.loadActivitiesFromFile() }.await()

                val matchingActivity = activities.find { it.name == activityName }

                return@withContext matchingActivity?.id
            } catch (exception: Exception) {
                println("Erro ao obter ID da atividade: $exception")
                return@withContext null
            }
        }
    }

    private suspend fun getCreationTime(activityId: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val activities = coroutineScope.async {
                    activityRepository.loadActivitiesFromFile() }.await()

                val matchingActivity = activities.find { it.id == activityId }

                return@withContext matchingActivity?.time ?: SimpleDateFormat(
                    "HH:mm:ss", Locale.getDefault()).format(Date())
            } catch (exception: Exception) {
                println("Erro ao obter a data de criação da atividade: $exception")
                return@withContext ""
            }
        }
    }

    private fun selectIconAndSetBtnIcon(selectedIconResource: Int, activityContext: Context) {
        val iconSelectionIntent = Intent(activityContext, IconSelectionActivity::class.java)
        iconSelectionIntent.putExtra(Constants.SELECTED_ICON_EXTRA, selectedIconResource)
        iconSelectionLauncher.launch(iconSelectionIntent)
    }

    private val iconSelectionLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val iconId =
            result.data?.getIntExtra(Constants.SELECTED_ICON_EXTRA, R.drawable.ic_default_icon)
            ?: R.drawable.ic_default_icon

        if (result.resultCode == Activity.RESULT_OK) {
            updateIcon(iconId)
        }
    }

    private suspend fun getActivityById(activityId: String): ActivityItem? {
        return withContext(Dispatchers.IO) {
            try {
                val activities = coroutineScope.async {
                    activityRepository.loadActivitiesFromFile() }.await()

                return@withContext activities.find { it.id == activityId }
            } catch (exception: Exception) {
                println("Erro ao obter atividade pelo ID: $exception")
                return@withContext null
            }
        }
    }

}