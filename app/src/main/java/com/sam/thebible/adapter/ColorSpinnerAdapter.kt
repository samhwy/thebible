package com.sam.thebible.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ColorSpinnerAdapter(
    private val context: Context,
    private val colors: Array<Int>,
    private val colorNames: Array<String>
) : BaseAdapter() {

    override fun getCount(): Int = colors.size

    override fun getItem(position: Int): Any = colors[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        
        textView.text = colorNames[position]
        textView.setBackgroundColor(colors[position])
        textView.setTextColor(if (colors[position] == Color.BLACK) Color.WHITE else Color.BLACK)
        textView.setPadding(16, 16, 16, 16)
        
        return view
    }
}