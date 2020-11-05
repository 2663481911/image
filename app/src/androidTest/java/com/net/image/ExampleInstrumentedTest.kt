package com.net.image

import android.util.Log
import android.webkit.WebView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import kotlin.concurrent.thread

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.myapplication", appContext.packageName)


        Log.d("packageName", appContext.packageName )
//        val webView = WebView(appContext)
//        //首先设置Webview支持JS代码
//        webView.settings.javaScriptEnabled = true;
//        webView.evaluateJavascript("sum(1, 3)") {
//            Log.d("sum", it)
//        }
//        webView.destroy()

    }
}