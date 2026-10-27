package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  PartyDAO.kt                                 ║
// ║  Os 4 métodos básicos do CRUD + consultas    ║
// ║  filtradas (por chefe) e de limpeza (quando  ║
// ║  um Herói ou um Chefe é removido)            ║
// ╚══════════════════════════════════════════════╝

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface PartyDAO {

    @Insert
    suspend fun inserir(membro: PartyMembro)

    @Query("SELECT * FROM party")
    suspend fun buscarTodos(): List<PartyMembro>

    @Update
    suspend fun atualizar(membro: PartyMembro)

    @Delete
    suspend fun deletar(membro: PartyMembro)

    // Quem está na party de UM chefe específico
    @Query("SELECT * FROM party WHERE chefeId = :chefeId")
    suspend fun buscarPorChefe(chefeId: Int): List<PartyMembro>

    // Limpeza: herói removido some de TODAS as parties
    @Query("DELETE FROM party WHERE heroiId = :heroiId")
    suspend fun deletarPorHeroi(heroiId: Int)

    // Limpeza: chefe removido leva a própria party junto
    @Query("DELETE FROM party WHERE chefeId = :chefeId")
    suspend fun deletarPorChefe(chefeId: Int)
}
