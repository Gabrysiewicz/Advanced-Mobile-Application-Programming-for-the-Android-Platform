package com.example.masterand.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterand.dao.PlayerDao
import com.example.masterand.database.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {

    // This function adds a player to the database
    fun addPlayer(playerDao: PlayerDao, player: Player, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            playerDao.insertPlayer(player)
            onComplete()
        }
    }
    // This function gets players from the database and returns a Flow
    fun getPlayers(playerDao: PlayerDao): Flow<List<Player>> {
        return playerDao.getAllPlayersFlow()
    }
    // This function deletes a player with scores from the database
    fun deletePlayerWithScores(playerDao: PlayerDao, player: Player, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            playerDao.deletePlayerWithScores(player)
            onComplete()
        }
    }
}
