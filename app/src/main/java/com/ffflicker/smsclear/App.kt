package com.ffflicker.smsclear

import android.app.Application
import android.content.Context
import android.database.Cursor

class App : Application() {

    companion object {
        lateinit var smsData: ArrayList<SMSModel>
        var mCursor: Cursor? = null

        fun clearData() {
            smsData?.clear()
            if (mCursor != null && !mCursor!!.isClosed) {
                mCursor!!.close()
                mCursor = null
            }
        }

        fun dp2px(context: Context, dp: Float): Float {
            val scale = context.resources.displayMetrics.density
            return dp * scale + 0.5f
        }

        fun px2dp(context: Context, px: Float): Float {
            val scale = context.resources.displayMetrics.density
            return px / scale + 0.5f
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

}
