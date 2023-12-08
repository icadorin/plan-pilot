package com.israel.planpilot

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.navigation.NavigationView
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.israel.planpilot.Constants.NO_ICON
import com.schedule.ActivityItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class HomeFragment : Fragment() {

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
    private var selectedIconResource: Int = 2131165355
    private var selectedIconResourceGlobal: Int = R.drawable.ic_default_icon
    private val iconCache = HashMap<Int, Drawable>()

    private fun getIconWithoutPreloading(
        context: Context,
        activityItem: ActivityItem
    ): Drawable? {
        val iconResource = activityItem.iconResource

        if (iconResource != -1) {
            var iconDrawable = iconCache[iconResource]
            if (iconDrawable == null) {
                iconDrawable = ContextCompat.getDrawable(context, iconResource)
                iconCache[iconResource] = iconDrawable as Drawable
            }
            val paddingInPixels = context.resources.getDimensionPixelSize(R.dimen.left_padding)

            return InsetDrawable(
                iconDrawable,
                paddingInPixels,
                0,
                paddingInPixels,
                0
            )
        }

        return null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val drawerLayout = view.findViewById<DrawerLayout>(R.id.homeDrawerLayout)
        val navigationView = view.findViewById<NavigationView>(R.id.navigationView)

        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_login -> {
                    Navigation.findNavController(view).navigate(R.id.nav_login)
                    drawerLayout.closeDrawers()
                    return@setNavigationItemSelectedListener true
                }
                else -> return@setNavigationItemSelectedListener false
            }
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            openAddActivityDialog(selectedDateMillis)
        }

        btnClearActivities.setOnClickListener {
            clearAllActivities()
        }

        // Inicializa a data atual
        selectedDateMillis = System.currentTimeMillis()

        // Configura o CalendarView com a data atual
        calendarView.date = selectedDateMillis

        // Atualiza a lista com os dados da data atual
        updateListView(selectedDateMillis)

        listViewActivities.setOnItemClickListener { _, _, position, _ ->
            val activityToDeleteOrEdit = activitiesAdapter.getItem(position)

            if (activityToDeleteOrEdit != null) {
                lifecycleScope.launch {
                    val activityId = getActivityIdByName(activityToDeleteOrEdit.name)
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
                val activities = loadActivitiesFromFile()
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
                    openEditActivityDialog(activityItem)
                }
                1 -> {
                    deleteActivity(activityItem)
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun openAddActivityDialog(selectedDateMillis: Long) {
        val currentDateTime = SimpleDateFormat(
            "HH:mm:ss", Locale.getDefault()
        ).format(Date())

        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
        btnIcon = dialogView.findViewById(R.id.btnIcon)
        this.selectedIconResource = NO_ICON
        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)

        "Adicionar Atividade".also { dialogTitle.text = it }

        val btnIcon = dialogView.findViewById<Button>(R.id.btnIcon)
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

                // ToDo criar repository para os metodos CRUD
                saveActivityToFile(activity)

                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    private fun openEditActivityDialog(activityItem: ActivityItem) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog, null)
        val dialogTitle = dialogView.findViewById<TextView>(R.id.textViewDialogTitle)
        val input = dialogView.findViewById<EditText>(R.id.editTextActivityName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnIcon = dialogView.findViewById<Button>(R.id.btnIcon)

        "Editar Atividade".also { dialogTitle.text = it }
        input.setText(activityItem.name)

        val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog = builder.create()

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {

            // ToDo chamar essa função dentro do btnIcon para tentar atualizar o ícone...
            changeBtnIcon(btnIcon, activityItem.id)
            alertDialog.show()
        }

        btnIcon.setOnClickListener {
            selectIconAndSetBtnIcon(selectedIconResource, requireContext())
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSave.setOnClickListener {
            val editedActivityName = input.text.toString()
            if (editedActivityName.isNotEmpty()) {
                if (activityItem.id.isNotEmpty()) {
                    // Atualiza os detalhes da atividade
                    activityItem.name = editedActivityName
                    activityItem.iconResource = selectedIconResourceGlobal

                    saveActivityToFile(activityItem, isEditOperation = true)

                    alertDialog.dismiss()

                    updateListView(selectedDateMillis)
                } else {
                    println("ID da atividade não encontrado")
                }
            }
            selectedIconResourceGlobal = R.drawable.ic_default_icon
        }
    }

    private suspend fun changeBtnIcon(btnIcon: Button?, activityId: String) {
        withContext(Dispatchers.Main) {
            val matchingActivity = getActivityById(activityId)
            val iconResource = matchingActivity?.iconResource
            if (iconResource != NO_ICON && btnIcon != null) {
                val iconDrawable = matchingActivity?.let { getIconWithoutPreloading(requireContext(), it) }

                btnIcon.setCompoundDrawablesWithIntrinsicBounds(
                    iconDrawable, null, null, null
                )

                btnIcon.text = null

                if (iconResource != null) {
                    selectedIconResource = iconResource
                    selectedIconResourceGlobal = selectedIconResource
                }
            } else if (btnIcon != null) {
                btnIcon.text = "ícone"
                selectedIconResourceGlobal = R.drawable.ic_default_icon
            }
        }
    }

    private fun deleteActivity(activityItem: ActivityItem) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Excluir Atividade")
        builder.setMessage("Deseja excluir a atividade '${activityItem.name}'?")

        builder.setPositiveButton("Excluir") { _, _ ->
            val activityId = activityItem.id
            if (activityId.isNotEmpty()) {
                deleteActivityById(activityId)

                updateListView(selectedDateMillis)
            } else {
                println("ID da atividade não encontrado")
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun deleteActivityById(activityId: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val activities = loadActivitiesFromFile()
                val updatedActivities = activities.filterNot { it.id == activityId }
                saveActivitiesToFile(updatedActivities)

                withContext(Dispatchers.Main) {
                    println("Atividade excluída do arquivo JSON com sucesso")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Erro ao excluir atividade do arquivo JSON: $e")
                }
            }
        }
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
                    val activities = loadActivitiesFromFile()
                    val filteredActivities = activities.filter { it.date != formattedDate }

                    // Limpa o arquivo antes de salvar as atividades filtradas
                    jsonFile.writeText("")

                    // Salva as atividades filtradas no arquivo
                    filteredActivities.forEach { activityItem ->
                        saveActivityToFile(activityItem)
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
                val activities = loadActivitiesFromFile()
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
                val activities = loadActivitiesFromFile()
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
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedIconResource =
                result.data?.getIntExtra(Constants.SELECTED_ICON_EXTRA, R.drawable.ic_default_icon)
            if (selectedIconResource != null) {
                println("Pass")
            }
        }
    }

    private fun saveActivityToFile(activityItem: ActivityItem) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val activities = loadActivitiesFromFile()
                activities.add(activityItem)

                val json = Gson().toJson(activities)
                jsonFile.writeText(json)

                withContext(Dispatchers.Main) {
                    println("Atividade salva no arquivo JSON com sucesso")
                    updateListView(selectedDateMillis)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Erro ao salvar atividade no arquivo JSON: $e")
                }
            }
        }
    }

    private fun saveActivityToFile(activityItem: ActivityItem, isEditOperation: Boolean = false) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val activities = loadActivitiesFromFile()

                if (isEditOperation) {
                    activities.removeAll { it.id == activityItem.id }
                }

                activities.add(activityItem)

                val json = Gson().toJson(activities)
                jsonFile.writeText(json)

                withContext(Dispatchers.Main) {
                    println("Atividade(s) salva(s) no arquivo JSON com sucesso")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Erro ao salvar atividade(s) no arquivo JSON: $e")
                }
            }
        }
    }

    private fun saveActivitiesToFile(activities: List<ActivityItem>) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val json = Gson().toJson(activities)
                jsonFile.writeText(json)

                withContext(Dispatchers.Main) {
                    println("Atividades salvas no arquivo JSON com sucesso")
                    updateListView(selectedDateMillis)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("Erro ao salvar atividades no arquivo JSON: $e")
                }
            }
        }
    }

    private fun loadActivitiesFromFile(): MutableList<ActivityItem> {
        return try {
            val json = jsonFile.readText()
            val typeToken = object : TypeToken<MutableList<ActivityItem>>() {}.type
            Gson().fromJson(json, typeToken) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private suspend fun getActivityById(activityId: String): ActivityItem? {
        return withContext(Dispatchers.IO) {
            try {
                val activities = loadActivitiesFromFile()
                return@withContext activities.find { it.id == activityId.toString() }
            } catch (exception: Exception) {
                println("Erro ao obter atividade pelo ID: $exception")
                return@withContext null
            }
        }
    }
}
