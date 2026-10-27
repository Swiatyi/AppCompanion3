package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  HeroiDAO.kt                                 ║
// ║  Os 4 métodos básicos do CRUD + 1 consulta   ║
// ║  filtrada por campo (buscarPorId)            ║
// ╚══════════════════════════════════════════════╝

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface HeroiDAO {

    @Insert
    suspend fun inserir(heroi: Heroi)

    @Query("SELECT * FROM herois")
    suspend fun buscarTodos(): List<Heroi>

    @Update
    suspend fun atualizar(heroi: Heroi)

    @Delete
    suspend fun deletar(heroi: Heroi)

    // Consulta extra, filtrando por campo: usada pela tela de Detalhes
    @Query("SELECT * FROM herois WHERE id = :id")
    suspend fun buscarPorId(id: Int): Heroi?
}
