package com.example.arxiv_zotero_2

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//fun Date.toString(format:String,locale: Locale = Locale.getDefault()) : String {
//
//    return formatter.format(this)
//}

fun getCurrentDateTime(daysAgo:Int, myFormat:String): String {
    val date = Calendar.getInstance()
    date.add(Calendar.DAY_OF_YEAR,-daysAgo)
    val formatter = SimpleDateFormat(myFormat)
    var output = formatter.format(date.time)
    return output
}

fun notArxivDay(daysAgo:Int): Boolean{
    val day = getCurrentDateTime(daysAgo,"EEEE")
    var bool = false
    if ((day == "Friday")||(day == "Saturday")){
        bool =  true
    }
    return bool
}

fun nextDaysAgo(daysAgo: Int): Int{
    var next = daysAgo+1
    while(notArxivDay(next)){
        next += 1
    }
    return next
}

fun updateDateList(fileName:String): ArrayList<MyDate>{
    // test file
//    var testdate = "Tuesday 04/20"
//    var test = MyDates(arrayListOf(MyDate(testdate,false) ))
//    writeToJson(fileName,test)
    // update the list from the current date
    var currentDateList = readFromJson(fileName).dates
    var newDates : ArrayList<MyDate> = ArrayList()

    var lastDate = currentDateList[0].name
    var daysAgo = nextDaysAgo(-1)
    var currentDate = getCurrentDateTime(daysAgo,"EEEE MM/dd")

    // add new dates until meet lastDate
    var count = 0
    var newEntry = false
    while((currentDate!=lastDate)&&(count<=30)){
        newEntry = true
        newDates.add(MyDate(currentDate,false))
        daysAgo = nextDaysAgo(daysAgo)
        currentDate = getCurrentDateTime(daysAgo,"EEEE MM/dd")
        count++
    }

    // add items from currentDateList to newDates until length = 10
    var currentsize = currentDateList.size
    var count2 = 0
    while ((count<=10)&&(count2<currentsize)){
        newDates.add(currentDateList[count2])
        count++
        count2++
    }

    // update the json file if there is a new entry
    if (newEntry){
        writeToJson(fileName,MyDates(newDates))
    }
    return newDates
}