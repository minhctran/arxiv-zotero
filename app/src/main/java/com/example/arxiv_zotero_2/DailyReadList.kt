package com.example.arxiv_zotero_2

import android.app.DownloadManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONObject
import java.lang.NullPointerException

class DailyReadList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_read_list)
        /////////////

        ////////////////
        var paperList = jsonStringToPapers(intent.getStringExtra("Papers"))
        var userID = intent.getStringExtra("UserID")
        if(userID==null){
            userID = ""
        }
        var apiKey = intent.getStringExtra("APIKey")
        if (apiKey==null){
            apiKey = ""
        }
        Log.d("API Key",apiKey)
        Log.d("userID",userID)
        // test values
        var context = this
//        var paperList = getPapers("test",context)
        var paperListView = findViewById<ListView>(R.id.paper_list)
        val adapter = PaperAdapter(context, paperList)
        paperListView.adapter = adapter
        //on click
        paperListView.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            val selectedPaper = paperList[position]
//            val dateIntent = DailyReadList.newIntent(context,selectedDate)
            Toast.makeText(applicationContext,"Saving \"${selectedPaper.title.substring(0,30)}...\" to Zotero",Toast.LENGTH_LONG).show()
            addPaperToZotero(selectedPaper,context,userID,apiKey)
        }
    }
}

fun addPaperToZotero(paper:Paper,context: Context,userID:String,apiKey:String){

    val queue = Volley.newRequestQueue(context)
    val url = "https://api.zotero.org/users/$userID/items"
    Log.d("url",url)
    val stringRequest = object: StringRequest(Request.Method.POST, url,
        Response.Listener<String> { response ->
            Log.d("A", "Response is: " + response)
            Toast.makeText(context,"\"${paper.title.substring(0,30)}...\" saved!",Toast.LENGTH_SHORT).show()
        },
        Response.ErrorListener {  })
    {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Zotero-API-Key"] = apiKey
            return headers
        }
        override fun getBodyContentType(): String {
            return "application/json"
        }
        override fun getBody(): ByteArray {
            var jsonString:String = Gson().toJson(ZoteroPaper(paper))

            jsonString = "[$jsonString]"
            jsonString = jsonString.replace("\"nameValuePairs\":{}","")
            Log.d("json",jsonString)
            return jsonString.toByteArray()
        }
    }

    queue.add(stringRequest)
}
//
//fun modifyTemplate(paper:Paper,context:Context,template:JSONObject){
//    var paperJson = template
//    paperJson.
//}

//fun requestBlankPaper(paper:Paper,context: Context){
//    val apiKey = "9d63speGZlvtpe9ySaIw7quk"
//    val queue = Volley.newRequestQueue(context)
//    val url = "https://api.zotero.org/items/new?itemType=report"
//    val stringRequest = object: StringRequest(Request.Method.GET, url,
//        Response.Listener<String> { response ->
//            Log.d("A", "Response is: " + response)
//            val template = Gson().toJson(response)
////            modifyTemplate(paper,context,template)
//        },
//        Response.ErrorListener {  })
//    {
//        override fun getHeaders(): MutableMap<String, String> {
//            val headers = HashMap<String, String>()
//            headers["Zotero-API-Key"] = apiKey
//            return headers
//        }
////        override fun getBodyContentType(): String {
////            return "application/json"
////        }
////        override fun getBody(): ByteArray {
////            var jsonString:String = Gson().toJson(ZoteroPaper(Paper("title","abstract","author","url")))
////            return jsonString.toByteArray()
////        }
//    }
//
//    queue.add(stringRequest)
//}

class Tag{
    var tag : String? = null
    var type: Int = 1
    constructor(): super() {
        this.tag = "FromApp"
        this.type = 0
    }
}

class ZoteroPaper{
    var itemType: String? = null
    var tags: ArrayList<Tag> = ArrayList()
    var relations: JSONObject = JSONObject()
    var collections : ArrayList<String> = ArrayList()
    var title: String? = null
    var creators: ArrayList<Creator> = ArrayList()
    var abstractNote: String? = null
    var url: String? = null
    var date: String? = null
    constructor(): super(){}
    constructor(Paper:Paper): super() {
        this.itemType = "journalArticle"
        this.tags = arrayListOf(Tag())
        this.relations = JSONObject()
        this.collections = ArrayList()

        this.title = Paper.title
        val authorsSplit = Paper.authors.split(", ")
        for (i in authorsSplit.indices){
            this.creators.add(Creator(authorsSplit[i]))
        }
        this.abstractNote = Paper.abstract
        this.url = Paper.url
        this.date = getCurrentDateTime(0,"yyyy")
    }
}

class Creator{
    var creatorType: String? = null
    var firstName: String? = null
    var lastName: String? = null
    constructor(): super(){}
    constructor(Author:String): super(){
        this.creatorType = "author"
        val authorSplit = Author.split(" ")
        this.firstName = authorSplit[0]
        this.lastName = authorSplit[authorSplit.size-1]
    }
}