package com.ffflicker.smsclear

import android.app.Application
import android.content.Context
import android.database.Cursor

public class App : Application() {

    companion object {
        public lateinit var smsData: ArrayList<SMSModel>
        public var mCursor: Cursor? = null

        public fun clearData() {
            smsData?.clear()
            if (mCursor != null && !mCursor!!.isClosed) {
                mCursor!!.close()
                mCursor = null
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

}
