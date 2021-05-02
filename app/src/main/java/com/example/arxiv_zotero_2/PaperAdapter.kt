package com.example.arxiv_zotero_2

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class PaperAdapter (private val context: Context,
                    private val dataSource:ArrayList<Paper> ) : BaseAdapter(){
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
        val rowView = inflater.inflate(R.layout.paper_item,parent,false)
        val rowTitle = rowView.findViewById(R.id.paper_title) as TextView
        val rowAuthors = rowView.findViewById(R.id.paper_authors) as TextView
        val rowAbstract = rowView.findViewById(R.id.paper_abstract) as TextView
//        val rowRead = rowView.findViewById(R.id.date_item_read) as CheckBox
        val paper = getItem(position) as Paper
        rowTitle.text = paper.title
        rowAuthors.text = paper.authors
        rowAbstract.text = paper.abstract
//        rowRead.isChecked = dateEntry.read
//        if (dateEntry.read){
//            rowView.alpha = 0.25F
//            rowDate.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
//        }
////        rowView.setClickable(true)
//        rowView.setFocusable(true)
//        rowView.setOnClickListener(new onClickListenter(){
//
//        })
        return rowView
    }
}

class Paper{
    var title: String = ""
    var authors: String = ""
    var abstract: String = ""
    var url: String? = null
    constructor(): super(){}
    constructor(Title: String,Authors:String,Abstracts:String,URL: String): super() {
        this.title = Title
        this.authors = Authors
        this.abstract = Abstracts
        this.url = URL
    }
}
class PaperList{
    var papers: ArrayList<Paper> = ArrayList()
    constructor(): super(){}
    constructor(Papers: ArrayList<Paper>): super(){
        this.papers = Papers
    }
}