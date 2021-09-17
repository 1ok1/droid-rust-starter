package com.lok1.rustndkexample

import android.app.Application
import android.util.Log
import java.lang.Exception

class RustNdkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        private val TAG = this.javaClass.simpleName
        private val LIBRARY_NAME = "rust"

        init {
            print(loadNativeLibrary())
        }

        fun loadNativeLibrary(): Boolean {
            try {
                Log.i(TAG, "Attempting to load library: $LIBRARY_NAME")
                System.loadLibrary(LIBRARY_NAME)
            } catch (e: Exception) {
                Log.i(TAG, "Exception loading native library: $e")
                return false
            }
            return true
        }
    }

}