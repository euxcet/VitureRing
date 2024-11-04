package com.euxcet.viturering.home

import android.util.Size
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class HomePageAdapter(activity: FragmentActivity, private val pageCount: Int) : FragmentStateAdapter(activity) {

    private val fragmentMap = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int {
        return pageCount
    }

    private var pageSize: Size? = null

    fun setPageSize(size: Size) {
        pageSize = size
        fragmentMap.forEach {
            (it.value as CardPageFragment).onPageSizeChanged(size)
        }
    }

    override fun createFragment(position: Int): Fragment {
        fragmentMap[position] = CardPageFragment.newInstance(position, pageSize?.width, pageSize?.height)
        return fragmentMap[position]!!
    }

    fun getFragment(position: Int): Fragment? {
        return fragmentMap[position]
    }
}