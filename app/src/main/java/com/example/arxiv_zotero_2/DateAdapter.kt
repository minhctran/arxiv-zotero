package com.example.arxiv_zotero_2

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView

class DateAdapter(private val context: Context,
                  private val dataSource:ArrayList<MyDate> ) : BaseAdapter(){
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int{
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.date_item,parent,false)
        val rowDate = rowView.findViewById(R.id.date_item_date) as TextView
//        val rowRead = rowView.findViewById(R.id.date_item_read) as CheckBox
        val dateEntry = getItem(position) as MyDate
        rowDate.text = dateEntry.name
        rowDate.textSize = 24F
//        rowRead.isChecked = dateEntry.read
        if (dateEntry.read){
            rowView.alpha = 0.25F
            rowDate.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
//        rowView.setClickable(true)
//        rowView.setFocusable(true)
//        rowView.setOnClickListener(new onClickListenter(){
//
//        })
        return rowView
    }
}