package com.example.masterand.dao

import androidx.room.*
import com.example.masterand.database.GameResult
import com.example.masterand.database.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Insert
    suspend fun insertPlayer(player: Player): Long

    @Insert
    suspend fun insertGameResult(gameResult: GameResult)

    @Query("SELECT * FROM player_table")
    suspend fun getAllPlayers(): List<Player>

    @Query("SELECT * FROM game_result_table WHERE playerId = :playerId")
    suspend fun getPlayerGameResults(playerId: Int): List<GameResult>

    @Query("SELECT * FROM player_table")
    fun getAllPlayersFlow(): Flow<List<Player>>

    // Get Player by ID as suspend function to make it asynchronous
    @Query("SELECT * FROM player_table WHERE id = :playerId")
    suspend fun getPlayerById(playerId: Int): Player?

    @Transaction
    @Query("SELECT * FROM game_result_table ORDER BY colorCount DESC, score DESC")
    suspend fun getHighScores(): List<GameResultWithPlayer>

    @Transaction
    @Query("DELETE FROM player_table WHERE id = :playerId")
    suspend fun deletePlayer(playerId: Int)

    @Transaction
    @Query("DELETE FROM game_result_table WHERE playerId = :playerId")
    suspend fun deletePlayerScores(playerId: Int)

    // Combined method to delete player and their scores
    suspend fun deletePlayerWithScores(player: Player) {
        deletePlayer(player.id)
        deletePlayerScores(player.id)
    }

    @Update
    suspend fun updatePlayer(player: Player)

    // DEVELOPER, TODO: comment if clearing database on startup will also be commented :MainActivity
    @Query("DELETE FROM player_table")
    suspend fun clearPlayers()
    @Query("DELETE FROM game_result_table")
    suspend fun clearGameResults()
}
// Its here because I didnt knew to which package it should belong. TODO: maybe move it somewhere? maybe not?
data class GameResultWithPlayer(
    @Embedded val gameResult: GameResult,
    @Relation(
        parentColumn = "playerId",
        entityColumn = "id"
    )
    val player: Player
)