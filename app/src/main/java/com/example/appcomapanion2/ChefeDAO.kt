package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  ChefeDAO.kt                                 ║
// ║  Os 4 métodos básicos do CRUD + 1 consulta   ║
// ║  filtrada por campo (buscarPorId)            ║
// ╚══════════════════════════════════════════════╝

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface ChefeDAO {

    @Insert
    suspend fun inserir(chefe: Chefe)

    @Query("SELECT * FROM chefes")
    suspend fun buscarTodos(): List<Chefe>

    @Update
    suspend fun atualizar(chefe: Chefe)

    @Delete
    suspend fun deletar(chefe: Chefe)

    // Consulta extra, filtrando por campo: usada pela tela de Detalhes
    @Query("SELECT * FROM chefes WHERE id = :id")
    suspend fun buscarPorId(id: Int): Chefe?
}
