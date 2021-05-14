package com.example.arxiv_zotero_2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONObject
import java.io.File

class apiKeyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_key)
        val zoteroKeyFile = filesDir.absolutePath+"/zoteroKey.json"
        var apiKeyview = findViewById<EditText>(R.id.zeroAPIKeyText) as EditText
        var submitButton = findViewById<Button>(R.id.submitButtonAPI) as Button
        var apiKey = ""
        var userID = ""
        submitButton.setOnClickListener{
            apiKey = apiKeyview.text.toString()
            Toast.makeText(applicationContext,"Testing the API key",Toast.LENGTH_SHORT).show()
            // test
            //
            val queue = Volley.newRequestQueue(this)
            val url = "https://api.zotero.org/keys/$apiKey"
            Log.d("url",url)
            val stringRequest = StringRequest(
                Request.Method.GET,url,
                Response.Listener<String> { response ->
                    // start paper list activity after pulling
                    val responseJson =  JSONObject(response)
                    var goodKey = false
                    if (responseJson.has("userID")) {
                        userID = responseJson.getString("userID")
                        if (responseJson.has("access")){
                            val access = JSONObject(responseJson.getString("access"))
                            if (access.has("user")){
                                val userAccess = JSONObject(access.getString("user"))
                                if ((userAccess.has("library"))&&(userAccess.has("files"))&&(userAccess.has("write"))){
                                    val libAccess = userAccess.getString("library").toBoolean()
                                    val fileAccess = userAccess.getString("files").toBoolean()
                                    val writeAccess = userAccess.getString("write").toBoolean()
                                    if (libAccess&&fileAccess&&writeAccess){
                                        goodKey = true
                                        Toast.makeText(applicationContext,"API key okay!",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                    if(goodKey){
                        // write key to file
                        val zoteroKey = ZoteroKey(userID,apiKey,true)
                        var jsonString:String = Gson().toJson(zoteroKey)
                        var file = File(zoteroKeyFile)
                        file.writeText(jsonString)
                        // start main activity
                        val mainActivityIntent = Intent(this,MainActivity::class.java)
                        startActivity(mainActivityIntent)
                    }

                    Log.d("permissions",response)
                    Log.d("userID",userID.toString())
                },
                Response.ErrorListener {
                    Toast.makeText(this,"Error: Can't connect to Zotero",Toast.LENGTH_SHORT).show()
                }
            )
            queue.add(stringRequest)
        }


    }
}

//////////////////////////////////////
fun isZoteroKeyGood(apiKey:String,context: Context){
    val queue = Volley.newRequestQueue(context)
    val url = "https://api.zotero.org/keys/$apiKey"
    Log.d("url",url)
    val stringRequest = StringRequest(
        Request.Method.GET,url,
        Response.Listener<String> { response ->
            // start paper list activity after pulling
            Log.d("permissions",response)
        },
        Response.ErrorListener {
            Toast.makeText(context,"Error: Can't connect to Zotero",Toast.LENGTH_LONG).show()
        }
    )
    queue.add(stringRequest)
}