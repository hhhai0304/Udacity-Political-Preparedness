package com.example.android.politicalpreparedness.repository

import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.database.Result
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ElectionRepository(
    private val dao: ElectionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun insertElection(election: Election) {
        withContext(ioDispatcher) {
            dao.insert(election)
        }
    }

    suspend fun getAllElection(): Result<List<Election>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(dao.getAllElection())
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    suspend fun getElectionById(id: Int): Result<Election> = withContext(ioDispatcher) {
        try {
            val election = dao.getElectionById(id)
            if (election != null) {
                return@withContext Result.Success(election)
            } else {
                return@withContext Result.Error("Election not found!")
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e.localizedMessage)
        }
    }

    suspend fun deleteElectionById(id: Int) {
        withContext(ioDispatcher) {
            dao.deletedElectionById(id)
        }
    }

    suspend fun deleteAllElection() {
        withContext(ioDispatcher) {
            dao.clear()
        }
    }
}