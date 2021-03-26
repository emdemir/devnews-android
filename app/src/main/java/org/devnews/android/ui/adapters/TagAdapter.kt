package org.devnews.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import org.devnews.android.R
import org.devnews.android.repository.objects.Tag

class TagAdapter(context: Context) :
    ArrayAdapter<Tag>(context, R.layout.list_item_tag, ArrayList()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_tag, parent, false)
        val tag = getItem(position) as Tag

        view.findViewById<TextView>(R.id.tag_name).text = tag.name
        view.findViewById<TextView>(R.id.tag_description).text = tag.description

        return view
    }
}