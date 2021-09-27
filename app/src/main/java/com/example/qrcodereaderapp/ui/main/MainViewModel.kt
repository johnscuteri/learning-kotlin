package com.example.qrcodereaderapp.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class MainViewModel : ViewModel() {

    fun getMatches(intentResult: IntentResult, jsonObjects: List<JSONObject>): Pair<Double, String>? {
        for(obj: JSONObject in jsonObjects)
        {
            if (intentResult.contents.toString().equals(obj.getString("id")))
            {
                return Pair(obj.getDouble("price"), obj.getString("name"))
            }
        }
        return null
    }

    fun matchInput(string: String, jsonObjects: List<JSONObject>): Pair<Double, String>? {
        for(obj: JSONObject in jsonObjects)
        {
            if (string.equals(obj.getString("id")))
            {
                return Pair(obj.getDouble("price"), obj.getString("name"))
            }
        }
        return null
    }

    fun loadJSONFromAssetToArray(context: Context): List<JSONObject> {
        var json: String? = null
        json = try {
            val ins: InputStream = context.assets.open("customerdata.json")
            val size: Int = ins.available()
            val buffer = ByteArray(size)
            ins.read(buffer)
            ins.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            Log.d("MainViewModule", ex.toString())
            ex.printStackTrace()
            return emptyList<JSONObject>()
        }
        //Move the Elements over to an arraylist so it can be iterated for display in a list
        var jsonArray = JSONArray(json!!)
        val list = arrayListOf<JSONObject>()
        for (i in 0..2)
        {
            list.add(jsonArray.getJSONObject(i))
        }
        return list
    }



}