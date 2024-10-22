package com.euxcet.viturering

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class HomeIconAdapter : BaseAdapter() {

    private val iconList = listOf(
        Icon("writing", R.drawable.icon_writing, "手写输入"),
        Icon("gesture", R.drawable.icon_gesture, "手势识别"),
//        Icon(R.drawable.ic_help, "3D模型"),
        // Icon("setting", R.drawable.icon_setting, "设置")
    )

    override fun getCount(): Int {
        return iconList.size
    }

    override fun getItem(position: Int): Any {
        return iconList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.item_grid_function, parent, false)
        val icon = iconList[position]
        view.findViewById<ImageView>(R.id.img).setImageResource(icon.iconRes)
        view.findViewById<TextView>(R.id.text).text = icon.title
        return view
    }

    fun getKey(position: Int): String? {
        if (position < 0 || position >= iconList.size) {
            return null
        }
        return iconList[position].key
    }

    data class Icon(
        val key: String,
        val iconRes: Int,
        val title: String,
    )
}