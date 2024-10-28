package com.euxcet.viturering

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.euxcet.viturering.databinding.ItemGridWordBinding

class WordAdapter: BaseAdapter() {

    private var words: List<String> = mutableListOf()
    private var mFocusedPosition = -1

    fun setWords(words: List<String>) {
        this.words = words
        if (words.isNotEmpty()) {
            mFocusedPosition = 0
        } else {
            mFocusedPosition = -1
        }
        notifyDataSetChanged()
    }

    fun focusNext() {
        mFocusedPosition = (mFocusedPosition + 1) % words.size
        notifyDataSetChanged()
    }

    fun getCurFocusedPosition(): Int {
        return mFocusedPosition
    }

    fun getCurFocusedWord(): String? {
        if (mFocusedPosition < 0 || mFocusedPosition >= words.size) {
            return null
        }
        return words[mFocusedPosition]
    }

    override fun getCount(): Int {
        return words.size
    }

    override fun getItem(position: Int): Any {
        return words[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemGridWordBinding = ItemGridWordBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        binding.text.text = words[position]
        if (position == mFocusedPosition) {
            binding.root.setBackgroundResource(R.drawable.shape_word_focus_back)
        } else {
            binding.root.setBackgroundResource(0)
        }
        return binding.root
    }
}