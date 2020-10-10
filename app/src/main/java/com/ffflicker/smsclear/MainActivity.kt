package com.ffflicker.smsclear

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private val PERMISSIONS_REQUEST_READ_SMS = 1
    private val calendar = Calendar.getInstance()
    public val SMS_URI_ALL = "content://sms/"
    public val SMS_URI_INBOX = "content://sms/inbox"
    public val SMS_URI_SEND = "content://sms/sent"
    public val SMS_URI_DRAFT = "content://sms/draft"
    public val SMS_URI_OUTBOX = "content://sms/outbox"
    public val SMS_URI_FAILED = "content://sms/failed"
    public val SMS_URI_QUEUED = "content://sms/queued"

    private lateinit var mKeysAdapter: CAdapter
    private lateinit var mWhiteKeysAdapter: CAdapter

    private lateinit var startDatePickerDialog: DatePickerDialog
    private lateinit var stopDatePickerDialog: DatePickerDialog

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var startTime: Long = 0
    private var stopTime: Long = Long.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startDatePickerDialog = DatePickerDialog(
            this, this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        stopDatePickerDialog = DatePickerDialog(
            this, this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        initRecycler()
        initView()

        // 请求权限
        requestPermission()
    }

    fun initView() {
        key_btn.setOnClickListener(this)
        key_white_btn.setOnClickListener(this)
        tv_start_time.setOnClickListener(this)
        tv_stop_time.setOnClickListener(this)
        scan_btn.setOnClickListener(this)
    }


    fun initRecycler() {
        mKeysAdapter = CAdapter(key_list)
        mWhiteKeysAdapter = CAdapter(key_white_list)

        key_list.adapter = mKeysAdapter
        key_white_list.adapter = mWhiteKeysAdapter

        val keyLinearLayoutManager = LinearLayoutManager(this)
        keyLinearLayoutManager.stackFromEnd = true
        keyLinearLayoutManager.reverseLayout = true

        val whiteKeyLinearLayoutManager = LinearLayoutManager(this)
        whiteKeyLinearLayoutManager.stackFromEnd = true
        whiteKeyLinearLayoutManager.reverseLayout = true

        key_list.layoutManager = keyLinearLayoutManager
        key_white_list.layoutManager = whiteKeyLinearLayoutManager
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.key_btn -> {
                if (key_edit.text.isNotEmpty() && !mKeysAdapter.mData.contains(key_edit.text.toString())) {
                    mKeysAdapter.add(key_edit.text.toString())
                }
                key_edit.text.clear()
            }
            R.id.key_white_btn -> {
                if (key_white_edit.text.isNotEmpty() && !mWhiteKeysAdapter.mData.contains(
                        key_white_edit.text.toString()
                    )
                ) {
                    mWhiteKeysAdapter.add(key_white_edit.text.toString())
                }
                key_white_edit.text.clear()
            }
            R.id.tv_start_time -> {
                startDatePickerDialog.show()
            }
            R.id.tv_stop_time -> {
                stopDatePickerDialog.show()
            }
            R.id.scan_btn -> {

                var data: ArrayList<SMSModel> = querySMS()
                if (data.isEmpty()) {
                    App.clearData()
                    Toast.makeText(this, "扫描无结果", Toast.LENGTH_SHORT).show()
                } else {
                    App.smsData = data
                    startActivity(Intent(this, ResultActivity::class.java))
                }

            }
        }
    }

    fun querySMS(): ArrayList<SMSModel> {
        var selection: StringBuilder = StringBuilder()
        var argArray: ArrayList<String> = ArrayList()
        var mDate: ArrayList<SMSModel> = ArrayList()

        // 限制时间范围
        selection.append("( date > ? ) AND ( date < ? ) ")
        argArray.add(startTime.toString())
        argArray.add(stopTime.toString())

        selection.append("AND (")

        // 构建黑名单
        for (str: String in mKeysAdapter.mData) {
            selection.append("(body LIKE ?) OR")
            argArray.add("%$str%")
        }



        selection.append("(body LIKE null) ) AND (")

        // 构建白名单
        for (str: String in mWhiteKeysAdapter.mData) {
            selection.append("(body NOT LIKE ?) OR")
            argArray.add("%$str%")
        }


        selection.append("(body LIKE null) )")

        val uri: Uri = Uri.parse(SMS_URI_ALL)
        val projection = arrayOf("_id", "address", "person", "body", "date", "type")
        val args: Array<String> = argArray.toTypedArray()
        var cur: Cursor? = contentResolver.query(uri, projection, selection.toString(), args, "date desc")

        if (cur != null && cur.moveToFirst()) {
            val index_id = cur.getColumnIndex("_id")
            val index_Address = cur.getColumnIndex("address")
            val index_Person = cur.getColumnIndex("person")
            val index_Body = cur.getColumnIndex("body")
            val index_Date = cur.getColumnIndex("date")
            val index_Type = cur.getColumnIndex("type")

            do {
                val longId = cur.getLong(index_id)
                val strAddress = cur.getString(index_Address)
                val intPerson = cur.getInt(index_Person)
                val strbody = cur.getString(index_Body)
                val longDate = cur.getLong(index_Date)
                val intType = cur.getInt(index_Type)

                mDate.add(SMSModel(longId, strAddress, intPerson, strbody, longDate, intType))

            } while (cur.moveToNext())
            App.mCursor = cur
        }

        return mDate
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (startDatePickerDialog.datePicker == view) {
            tv_start_time.text = String.format("%d年%d月%d日", year, month + 1, dayOfMonth)
            startTime =
                simpleDateFormat.parse(String.format("%d-%d-%d", year, month + 1, dayOfMonth)).time
        } else {
            tv_stop_time.text = String.format("%d年%d月%d日", year, month + 1, dayOfMonth)
            stopTime =
                simpleDateFormat.parse(String.format("%d-%d-%d", year, month + 1, dayOfMonth)).time
        }
    }

    fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_SMS
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    Array<String>(1) { Manifest.permission.READ_SMS },
                    PERMISSIONS_REQUEST_READ_SMS
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    Array<String>(1) { Manifest.permission.READ_SMS },
                    PERMISSIONS_REQUEST_READ_SMS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_READ_SMS && grantResults.size >= 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            showWaringDialog()
        }
    }

    fun showWaringDialog() {
        AlertDialog.Builder(this)
            .setTitle("歪？")
            .setMessage("请打开短信权限，否则功能无法正常运行！")
            .setPositiveButton("确定") { dialog, which ->
                finish()
            }.show()
    }

}
