package com.euxcet.viturering.home

import android.content.Context
import android.os.Bundle
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.euxcet.viturering.databinding.FragmentCardPageBinding
import com.euxcet.viturering.databinding.ItemHomeCardBinding
import com.euxcet.viturering.utils.Utils

class CardPageFragment : Fragment() {
    private var pageNo: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pageNo = it.getInt(ARG_PAGE_NO)
            if (it.containsKey(ARG_PAGE_WIDTH)) {
                pageWidth = it.getInt(ARG_PAGE_WIDTH)
            }
            if (it.containsKey(ARG_PAGE_HEIGHT)) {
                pageHeight = it.getInt(ARG_PAGE_HEIGHT)
            }
        }
    }

    private var homeViewModel: HomeViewModel? = null
    private lateinit var binding: FragmentCardPageBinding

    private var pageWidth = 0
    private var pageHeight = 0

    private val cardViewMap = mutableMapOf<String, View>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateCardView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ViewModelStoreOwner) {
            homeViewModel = ViewModelProvider(context)[HomeViewModel::class.java]
        }
    }

    fun onPageSizeChanged(size: Size) {
        if (size.width == pageWidth && size.height == pageHeight) {
            return
        }
        pageWidth = size.width
        pageHeight = size.height
        updateCardView()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun updateCardView() {
        homeViewModel?.getCardInfoList(pageNo)?.let { cardInfoList ->
            val hCells = cardInfoList.maxOfOrNull { it.hPosition + it.hCells } ?: 0
            val vCells = cardInfoList.maxOfOrNull { it.vPosition + it.vCells } ?: 0
            if (hCells == 0 || vCells == 0) {
                return
            }
            val space = Utils.dpToPx(requireContext(), 20)
            val paddingH =  Utils.dpToPx(requireContext(), 24)
            val paddingV =  Utils.dpToPx(requireContext(), 24)
            val cellWidth = (pageWidth - paddingH * 2 - space * (hCells - 1)) / hCells
            val cellHeight = (pageHeight - paddingV * 2 - space * (vCells - 1)) / vCells
            binding.root.removeAllViews()
            cardInfoList.forEach { cardInfo ->
                val cardBinding = ItemHomeCardBinding.inflate(layoutInflater, binding.root, false)
                val cardWidth = cellWidth * cardInfo.hCells + space * (cardInfo.hCells - 1)
                val cardHeight = cellHeight * cardInfo.vCells + space * (cardInfo.vCells - 1)
                cardBinding.root.layoutParams = ViewGroup.MarginLayoutParams(cardWidth, cardHeight).apply {
                    val left = cardInfo.hPosition * (cellWidth + space) + paddingH
                    val top = cardInfo.vPosition * (cellHeight + space) + paddingV
                    setMargins(left, top, 0, 0)
                }
                if (cardInfo.backgroundRes > 0) {
                    cardBinding.root.setBackgroundResource(cardInfo.backgroundRes)
                } else if (cardInfo.backgroundColor != 0) {
                    cardBinding.root.background.setTint(cardInfo.backgroundColor)
                }
                if (cardInfo.icon > 0) {
                    cardBinding.image.setImageResource(cardInfo.icon)
                }
                cardInfo.title?.let {
                    cardBinding.title.text = it
                }
                binding.root.addView(cardBinding.root)
                cardBinding.root.setOnClickListener {
                    homeViewModel?.openCard(requireContext(), cardInfo.key)
                }
                cardViewMap[cardInfo.key] = cardBinding.root
            }
        }
    }

    private var focusedCardIndex = -1
    fun focusNextCard() {
        focusedCardIndex++
        if (focusedCardIndex >= (homeViewModel?.getCardInfoList(pageNo)?.size ?: 0)) {
            focusedCardIndex = 0
        }
    }

    fun focusPreviousCard() {
        focusedCardIndex--
        if (focusedCardIndex < 0) {
            focusedCardIndex = (homeViewModel?.getCardInfoList(pageNo)?.size ?: 0) - 1
        }
    }


    fun performClick(x: Int, y: Int) {
        cardViewMap.forEach { (key, view) ->
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + view.width
            val bottom = top + view.height
            if (x in left until right && y in top until bottom) {
                homeViewModel?.openCard(requireContext(), key)
            }
        }
    }

    fun onCursorMove(x: Int, y: Int) {
        cardViewMap.forEach { (key, view) ->
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val left = location[0]
            val top = location[1]
            val right = left + view.width
            val bottom = top + view.height
            if (x in left until right && y in top until bottom) {
                // todo highlight

            }
        }
    }

    companion object {
        private const val ARG_PAGE_NO = "PAGE_NO"
        private const val ARG_PAGE_WIDTH = "PAGE_WIDTH"
        private const val ARG_PAGE_HEIGHT = "PAGE_HEIGHT"

        @JvmStatic
        fun newInstance(pageNo: Int, pageWidth: Int?, pageHeight: Int?) =
            CardPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE_NO, pageNo)
                    pageWidth?.let {
                        putInt(ARG_PAGE_WIDTH, it)
                    }
                    pageHeight?.let {
                        putInt(ARG_PAGE_HEIGHT, it)
                    }
                }
            }
    }
}