package com.euxcet.viturering.pages.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.euxcet.viturering.R

class HomeIconAdapter : BaseAdapter() {

    private val iconList = listOf(
        Icon("writing", R.drawable.icon_writing, "手写输入"),
        Icon("gesture", R.drawable.icon_gesture, "手势识别"),
        Icon("models", R.drawable.icon_coordinate_system, "3D模型"),
        Icon("health", R.drawable.icon_health_recognition, "健康检测")
        // Icon("setting", R.drawable.icon_setting, "设置")
    )

    private var mFocusedPosition = -1

    fun focusNext() {
        mFocusedPosition = (mFocusedPosition + 1) % iconList.size
        notifyDataSetChanged()
    }

    fun getCurFocusedPosition(): Int {
        return mFocusedPosition
    }

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
        if (position == mFocusedPosition) {
            view.setBackgroundResource(R.drawable.shape_focused_back)
        } else {
            view.setBackgroundResource(0)
        }
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