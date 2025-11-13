package com.example.experiment3  // 替换为你的实际包名

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    // 控件绑定
    private lateinit var lvAnimal: ListView
    private lateinit var lvActionMode: ListView
    private lateinit var tvTest: TextView
    private lateinit var btnShowDialog: Button

    // 动物列表数据
    private val animalNames by lazy {
        arrayOf(
            getString(R.string.lion),
            getString(R.string.tiger),
            getString(R.string.monkey),
            getString(R.string.dog),
            getString(R.string.cat),
            getString(R.string.elephant)
        )
    }
    private val animalIcons = intArrayOf(
        R.drawable.lion, R.drawable.tiger, R.drawable.monkey,
        R.drawable.dog, R.drawable.cat, R.drawable.elephant
    )

    // ActionMode 列表数据（支持动态删除）
    private lateinit var actionModeData: MutableList<String>
    private lateinit var actionModeAdapter: ArrayAdapter<String>

    // 通知常量
    companion object {
        private const val CHANNEL_ID = "animal_notification"
        private const val NOTIFICATION_ID = 1
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    // --------------------------
    // 修复：onCreate 方法（完整实现，无返回语句错误）
    // --------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)  // 必须调用父类方法
        setContentView(R.layout.activity_main)  // 加载布局
        initViews()                  // 初始化控件
        initAnimalListView()         // 初始化动物列表
        initActionModeListView()     // 初始化 ActionMode 多选列表
        setupDialogTrigger()         // 初始化对话框触发
        createNotificationChannel()  // 创建通知渠道
        requestNotificationPermission()  // 请求通知权限
    }

    // 初始化控件
    private fun initViews() {
        lvAnimal = findViewById(R.id.lv_animal)
        lvActionMode = findViewById(R.id.lv_action_mode)
        tvTest = findViewById(R.id.tv_test)
        btnShowDialog = findViewById(R.id.btn_show_dialog)
    }

    // 初始化动物列表
    private fun initAnimalListView() {
        val listData = mutableListOf<Map<String, Any>>()
        animalNames.forEachIndexed { index, name ->
            listData.add(mapOf(
                "icon" to animalIcons[index],
                "name" to name
            ))
        }

        val adapter = SimpleAdapter(
            this,
            listData,
            R.layout.item_list,
            arrayOf("icon", "name"),
            intArrayOf(R.id.iv_animal, R.id.tv_animal_name)
        )
        lvAnimal.adapter = adapter

        lvAnimal.setOnItemClickListener { _, _, position, _ ->
            val selectedAnimal = animalNames[position]
            Toast.makeText(this, getString(R.string.toast_selected, selectedAnimal), Toast.LENGTH_SHORT).show()
            sendNotification(selectedAnimal)
        }
    }

    // --------------------------
    // 修复：ActionMode 多选列表（正确实现 MultiChoiceModeListener）
    // --------------------------
    private fun initActionModeListView() {
        actionModeData = mutableListOf(
            getString(R.string.item_one),
            getString(R.string.item_two),
            getString(R.string.item_three),
            getString(R.string.item_four),
            getString(R.string.item_five)
        )

        actionModeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            actionModeData
        )
        lvActionMode.adapter = actionModeAdapter

        lvActionMode.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        lvActionMode.setMultiChoiceModeListener(object : AbsListView.MultiChoiceModeListener {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.menu_context, menu)
                mode?.title = "0 selected"
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return if (item?.itemId == R.id.item_delete) {
                    val checkedPositions = lvActionMode.checkedItemPositions
                    for (i in actionModeData.size - 1 downTo 0) {
                        if (checkedPositions.get(i)) {
                            actionModeData.removeAt(i)
                        }
                    }
                    actionModeAdapter.notifyDataSetChanged()
                    mode?.finish()
                    true
                } else false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                lvActionMode.clearChoices()
            }

            override fun onItemCheckedStateChanged(
                mode: ActionMode?,
                position: Int,
                id: Long,
                checked: Boolean
            ) {
                val selectedCount = lvActionMode.checkedItemCount
                mode?.title = "$selectedCount selected"
            }
        })
    }

    // 初始化对话框触发
    private fun setupDialogTrigger() {
        btnShowDialog.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_login, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener { dialog.dismiss() }
            dialogView.findViewById<Button>(R.id.btn_sign_in).setOnClickListener {
                val username = dialogView.findViewById<EditText>(R.id.et_username).text.toString()
                val password = dialogView.findViewById<EditText>(R.id.et_password).text.toString()
                Toast.makeText(this, "Username: $username\nPassword: $password", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    // 选项菜单创建
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_text, menu)
        return true
    }

    // 选项菜单点击
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.size_small -> { tvTest.textSize = 10f; true }
            R.id.size_middle -> { tvTest.textSize = 16f; true }
            R.id.size_large -> { tvTest.textSize = 20f; true }
            R.id.color_red -> { tvTest.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light)); true }
            R.id.color_black -> { tvTest.setTextColor(ContextCompat.getColor(this, android.R.color.black)); true }
            R.id.item_normal -> { Toast.makeText(this, getString(R.string.toast_normal_menu), Toast.LENGTH_SHORT).show(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 发送通知
    private fun sendNotification(content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, getString(R.string.toast_notification_permission), Toast.LENGTH_SHORT).show()
            return
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content, content))
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    // 创建通知渠道
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 请求通知权限
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // --------------------------
    // 修复：onRequestPermissionsResult 方法（签名与父类完全一致）
    // --------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,  // 正确参数类型：Array<out String>
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)  // 调用父类方法
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.toast_notification_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.toast_notification_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }
}