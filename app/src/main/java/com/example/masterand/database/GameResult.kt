package com.example.masterand.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_result_table")
data class GameResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerId: Int,
    val colorCount: Int,
    val score: Int
)