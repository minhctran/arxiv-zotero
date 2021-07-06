package com.example.arxiv_zotero_2

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import org.json.XML
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ///////////// Check if Zotero key is good. Otherwise need to get new API keys
        var zoteroKeyFile = filesDir.absolutePath+"/zoteroKey.json"
        var zoteroKey = loadZoteroKey(zoteroKeyFile,this)
        var zoteroKeyString : String = Gson().toJson(zoteroKey)
        var userID = zoteroKey.userID
        var apiKey = zoteroKey.apiKey
        if(!(zoteroKey.success)){
            Toast.makeText(applicationContext,"No Zotero key found",Toast.LENGTH_LONG).show()
            val intentAPI = Intent(this,apiKeyActivity::class.java)
            startActivity(intentAPI)
        }
        Log.d("key",zoteroKeyString)
        /////////////////////////////
        //
        var fileName = filesDir.absolutePath+"/dates.json"
        //
        var updatedDateList = updateDateList(fileName)

        //
        var listView = findViewById<ListView>(R.id.listview)
        var adapter = DateAdapter(this, updatedDateList)
        listView.adapter = adapter

        // list papers on click
        val context = this
        val duration = Toast.LENGTH_LONG

        val intent = Intent(this,DailyReadList::class.java)
        listView.onItemClickListener = AdapterView.OnItemClickListener{ _, _, position, _ ->
            // get selected Date
            val selectedDate = updatedDateList[position].name

            // pull the list of papers for the date
            Toast.makeText(applicationContext,"Pulling papers for "+selectedDate,Toast.LENGTH_LONG).show()
            val queue = Volley.newRequestQueue(this)
            val url = getArXivAddress(toNonNullString(selectedDate))
            Log.d("url",url)
            val stringRequest = StringRequest(Request.Method.GET,url,
                    Response.Listener<String> { response ->
                        // start paper list activity after pulling
                        val papers = parseArXivResponse(response)
                        intent.putExtra("Papers", paperListToJsonString(papers))
                        Log.d("User ID before intent"," This is the user ID: $userID")
                        intent.putExtra("UserID",userID)
                        intent.putExtra("APIKey",apiKey)
                        // set this date to "read"
                        updatedDateList[position].read=true
                        adapter.notifyDataSetChanged()
                        writeToJson(fileName,MyDates(updatedDateList))
                        // start reading
                        startActivity(intent)
                    },
                    Response.ErrorListener {
                        Toast.makeText(applicationContext,"Error: Can't connect to arXiv",Toast.LENGTH_LONG).show()
                    }
            )
            queue.add(stringRequest)
        }
    }
}
//////////////////////////////////////
class ZoteroKey{
    var userID : String = ""
    var apiKey : String = ""
    var success: Boolean = false
    constructor(): super(){}
    constructor(UserID: String, APIKey: String, Success:Boolean): super() {
        this.userID = UserID
        this.apiKey = APIKey
        this.success = Success
    }

}
//////////////////////////////////////
fun loadZoteroKey(fileName:String, context: Context) :ZoteroKey{
    ////// Load key from file
    var file = File(fileName)
    var fileExist = file.exists()
    var key : ZoteroKey = ZoteroKey("","",false)
    if(fileExist){
        var bufferedReader: BufferedReader = file.bufferedReader()
        var inputString = bufferedReader.use{it.readText()}
        key = Gson().fromJson(inputString, ZoteroKey::class.java)
    }
//    isZoteroKeyGood(key.apiKey,context)
    return key
}

//////////////////////////////////////
fun toNonNullString(str:String?):String{
    if(str==null){
        return "empty"
    }else{
        return str
    }

}

//@RequiresApi(Build.VERSION_CODES.O)
fun getArXivAddress(dateString:String):String{


    val year = getCurrentDateTime(0,"yyyy")
    val dayOfWeek = dateString.split(" ")[0]
    val month = dateString.split(" ")[1].split("/")[0]
    val day = dateString.split(" ")[1].split("/")[1]
    val deadline = "1800"
    var toDate = LocalDate.parse(day+"-"+month+"-"+year, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    var fromDate = toDate.minusDays(1)
    if(dayOfWeek=="Monday"){
        fromDate = fromDate.minusDays(2)
    } else {
        if(dayOfWeek=="Sunday"){
            toDate = toDate.minusDays(2)
            fromDate = fromDate.minusDays(2)
        }
    }
    val fromDateString = fromDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))+deadline
    val toDateString = toDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))+deadline
    return "https://export.arxiv.org/api/query?search_query=submittedDate:[$fromDateString+TO+$toDateString]+AND+cat:quant-ph&max_results=100"
}

fun parseArXivResponse(response:String):ArrayList<Paper>{

    val PRETTY_PRINT_INDENT_FACTOR = 2
    val jsonObj = XML.toJSONObject(response)
    val feed = jsonObj.getJSONObject("feed")
    val entries = feed.getJSONArray("entry")
    var papers : ArrayList<Paper> = ArrayList()
    var npapers = entries.length()-1

    for(j in 0..npapers){
        val entry = entries.getJSONObject(j)
        val title = entry.getString("title").replace("\n"," ").replace("  "," ")
        val abstract = entry.getString("summary").replace("\n"," ")
        val url = entry.getString("id")

        var authors = ""
        try{
            authors+= entry.getJSONObject("author").getString("name")
        }catch(e: Exception ){
            val authorArray = entry.getJSONArray("author")
            var nauthors = authorArray.length()-1
            for (k in 0..nauthors){
                authors += authorArray.getJSONObject(k).getString("name")
                if(k < authorArray.length()-1){
                    authors+=", "
                }
            }
        }

        papers.add(Paper(title,authors,abstract,url))
    }
//    val responseText = entries.getJSONObject(0).toString(PRETTY_PRINT_INDENT_FACTOR).substring(0,1500)

    return papers
}

fun paperListToJsonString(papers:ArrayList<Paper>):String{
    var gson = Gson()
    var jsonString:String = gson.toJson(PaperList(papers))
    return jsonString
}

fun jsonStringToPapers(jsonString:String?):ArrayList<Paper>{
    var gson = Gson()
    var paperList = gson.fromJson(jsonString,PaperList::class.java).papers
    return paperList
}

class MyDate{
    var name: String? = null
    var read: Boolean = false
    constructor(): super(){}
    constructor(Name: String,Read:Boolean): super() {
        this.name = Name
        this.read = Read
    }
}
class MyDates{
    var dates: ArrayList<MyDate> = ArrayList()
    constructor(): super(){}
    constructor(Dates: ArrayList<MyDate>): super(){
        this.dates = Dates
    }
}
fun writeToJson(fileName:String,myDates:MyDates){
    //var test = MyDates(arrayListOf(MyDate("Minh"),MyDate("Nhung")))
    var gson = Gson()
    var jsonString:String = gson.toJson(myDates)
    var file = File(fileName)
    file.writeText(jsonString)
}

fun readFromJson(fileName:String):MyDates {
    var gson = Gson()
    var output = MyDates(arrayListOf(MyDate("You are up to date!",true)))
    var file = File(fileName)
    var fileExist = file.exists()
    if(fileExist){
        var bufferedReader: BufferedReader = file.bufferedReader()
        var inputString = bufferedReader.use{it.readText()}
        output = gson.fromJson(inputString, MyDates::class.java)
    }
    return output
}
