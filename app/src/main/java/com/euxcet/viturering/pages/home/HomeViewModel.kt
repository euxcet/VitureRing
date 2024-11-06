package com.euxcet.viturering.pages.home

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.euxcet.viturering.R
import com.euxcet.viturering.pages.game.GameActivity
import com.euxcet.viturering.pages.model.Car3DActivity
import com.euxcet.viturering.pages.video.VideoActivity

class HomeViewModel(application: Application): AndroidViewModel(application) {

    private val cardInfoList: MutableList<MutableList<CardInfo>> = mutableListOf()

    fun getCardInfoList(pageNo: Int): List<CardInfo> {
        if (pageNo >= cardInfoList.size) {
            return emptyList()
        }
        return cardInfoList[pageNo].toList()
    }

    fun initCardInfoList() {

        // game
        val gameCardInfo = CardInfo(
            key = "game",
            title = "游戏",
            description = "Play the game",
            icon = R.drawable.ic_launcher_foreground,
            backgroundColor = Color.parseColor("#FF5722"),
            hCells = 2,
            vCells = 2,
            hPosition = 0,
            vPosition = 0
        )
        // video
        val videoCardInfo = CardInfo(
            key = "video",
            title = "影音",
            description = "Watch the video",
            icon = R.drawable.ic_play,
            backgroundColor = Color.parseColor("#FFC107"),
            hCells = 4,
            vCells = 4,
            hPosition = 2,
            vPosition = 0
        )
        // 3d model
        val modelCardInfo = CardInfo(
            key = "models",
            title = "3D",
            description = "View the 3D model",
            icon = R.drawable.ic_coordinate_system,
            backgroundColor = Color.parseColor("#FFC107"),
            hCells = 2,
            vCells = 2,
            hPosition = 0,
            vPosition = 2
        )
        cardInfoList.add(mutableListOf(gameCardInfo, videoCardInfo, modelCardInfo))
        // fake pages
        val fake1CardInfo = CardInfo(
            key = "fake1",
            title = "地图",
            description = "Fake1",
            icon = R.drawable.ic_launcher_foreground,
            backgroundColor = Color.parseColor("#FF5722"),
            hCells = 2,
            vCells = 2,
            hPosition = 0,
            vPosition = 0
        )

        val fake2CardInfo = CardInfo(
            key = "fake2",
            title = "会议",
            description = "Fake2",
            icon = R.drawable.ic_launcher_foreground,
            backgroundColor = Color.parseColor("#FF5722"),
            hCells = 4,
            vCells = 4,
            hPosition = 2,
            vPosition = 0
        )

        val fake3CardInfo = CardInfo(
            key = "fake3",
            title = "购物",
            description = "Fake3",
            icon = R.drawable.ic_launcher_foreground,
            backgroundColor = Color.parseColor("#FF5722"),
            hCells = 2,
            vCells = 2,
            hPosition = 0,
            vPosition = 2
        )

        cardInfoList.add(mutableListOf(fake1CardInfo, fake2CardInfo, fake3CardInfo))
    }

    fun openCard(context: Context, key: String) {
        // open card
        when (key) {
            "game" -> {
                val intent = Intent(context, GameActivity::class.java)
                context.startActivity(intent)
            }
            "models" -> {
                val intent = Intent(context, Car3DActivity::class.java)
                context.startActivity(intent)
            }
            "video" -> {
                val intent = Intent(context, VideoActivity::class.java)
                context.startActivity(intent)
            }
            else -> {
            }
        }
    }
}