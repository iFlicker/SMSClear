package com.ffflicker.smsclear.smspermission

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HeadlessSmsSendService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}