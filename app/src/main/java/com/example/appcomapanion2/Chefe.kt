package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  Chefe.kt                                    ║
// ║  @Entity: vira a tabela "chefes" no Room     ║
// ║  "regiao" liga o chefe a um Dungeon.nome     ║
// ╚══════════════════════════════════════════════╝

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "chefes")
data class Chefe(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,
    val descricao: String,
    val hpBase: Int,
    val danoBase: Int,
    val level: Int = 1,
    val regiao: String = "Desconhecida"
)
