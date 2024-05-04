package com.example.android.politicalpreparedness.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.android.politicalpreparedness.network.models.Election

@Dao
interface ElectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg election: Election)

    @Query("SELECT * FROM election_table ORDER BY electionDay ASC")
    fun getAllElection(): List<Election>

    @Query("SELECT * FROM election_table WHERE id = :id")
    fun getElectionById(id: Int): Election?

    @Query("DELETE FROM election_table WHERE id = :id")
    fun deletedElectionById(id: Int)

    @Query("DELETE FROM election_table")
    suspend fun clear()
}