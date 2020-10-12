package com.ffflicker.smsclear

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_result.*
import java.lang.Exception


class ResultActivity: AppCompatActivity() {

    private lateinit var mAdapter: RAdapter
    private lateinit var currentSystemSMSPackage: String
    private lateinit var myPackageName: String
    private lateinit var systemSMSPackage: String

    val REQ_SYSTEM_CODE = 2
    val REQ_SELF_CODE = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        initView()
        myPackageName = packageName
        systemSMSPackage = Telephony.Sms.getDefaultSmsPackage(this)
        currentSystemSMSPackage = systemSMSPackage
    }

    fun initView() {
        delete_btn.text = "删除全部(" + App.smsData.size + ")"
        delete_btn.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("此操作将删除本手机以下列表所有短信，请谨慎操作！\n\n确定后请将本应用选为默认短信应用，在操作完成后请在选择回系统默认应用")
                .setTitle("⚠️警告")
                .setNegativeButton("确认") { _,_ ->
                    run { popDefautAppChoose(false) }
                }
                .setNeutralButton("取消", null)
                .create()
                .show()
        }

        mAdapter = RAdapter(result_list)
        result_list.adapter = mAdapter
        result_list.layoutManager = LinearLayoutManager(this)
        mAdapter.fillData(App.smsData)
    }

    fun coreClean() {
        var isSuccess = true
        try {
            val c: Cursor? = App.mCursor
            if (null != c && c.moveToFirst()) {
                do {
                    val ida: Array<String> = arrayOf(c.getInt(c.getColumnIndex("_id")).toString())
                    contentResolver.delete(Uri.parse("content://sms/"), "_id=?", ida)
                } while (c.moveToNext())
            }
        }catch (e: Exception) {
            isSuccess = false
        }
        if (isSuccess)
            runOnUiThread { Toast.makeText(this, "删除完成~ 请设置默认短信应用为系统短信应用", Toast.LENGTH_SHORT).show() }
        else
            runOnUiThread { Toast.makeText(this, "删除失败~ 请设置默认短信应用为系统短信应用", Toast.LENGTH_SHORT).show() }

        runOnUiThread {
            popDefautAppChoose(true)
        }
    }

    fun popDefautAppChoose(isSystem: Boolean) {
        if (isSystem) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, systemSMSPackage)
            startActivityForResult(intent, REQ_SYSTEM_CODE)
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName)
            startActivityForResult(intent, REQ_SELF_CODE)
        }
        currentSystemSMSPackage = Telephony.Sms.getDefaultSmsPackage(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SELF_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Thread{ coreClean() }.start()
            } else {
                AlertDialog.Builder(this)
                    .setMessage("您未将默认短信应用设置为本应用 \n" +
                            "或者您当前系统不支持弹框设置默认应用,需要前往[设置-应用-默认应用]手动设置")
                    .setTitle("提醒")
                    .setNegativeButton("确认") { _,_ ->
                        run {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
                            }
                        }
                    }
                    .setNeutralButton("取消", null)
                    .create()
                    .show()
            }
        }

        if (requestCode == REQ_SYSTEM_CODE && resultCode == Activity.RESULT_OK) {
            if (requestCode != REQ_SELF_CODE) {
                AlertDialog.Builder(this)
                    .setMessage("删除完成！\n" +
                            "您当前系统不支持弹框设置默认应用,请务必前往[设置-应用-默认应用]手动设置为系统默认应用，否则短信功能将失效")
                    .setTitle("提醒")
                    .setNeutralButton("确定", null)
                    .create()
                    .show()
            }
            mAdapter.mData.clear()
            mAdapter.notifyDataSetChanged()
            App.clearData()
        }
    }

    override fun onDestroy() {
        App.clearData()
        super.onDestroy()
    }

}

data class SMSModel(var id:Long, var address: String, var person: Int, var body: String, var date: Long, var type: Int)