package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  Heroi.kt                                    ║
// ║  @Entity: vira a tabela "herois" no Room     ║
// ║  (antes era só uma data class em memória)    ║
// ╚══════════════════════════════════════════════╝

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "herois")
data class Heroi(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,
    val classe: String,
    val hp: Int,
    val stress: Int
)
