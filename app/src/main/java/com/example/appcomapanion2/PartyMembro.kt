package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  PartyMembro.kt                              ║
// ║  @Entity: tabela "party" — liga um Herói a   ║
// ║  uma POSIÇÃO (0 a 3) da party de um Chefe.   ║
// ║  Antes era uma List<String?> dentro do       ║
// ║  Chefe; o Room não guarda lista dentro de    ║
// ║  uma coluna, então virou uma tabela própria  ║
// ║  (relação muitos-para-muitos Herói ↔ Chefe)  ║
// ╚══════════════════════════════════════════════╝

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "party")
data class PartyMembro(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val chefeId: Int,   // id do Chefe dono dessa party
    val posicao: Int,   // 0, 1, 2 ou 3 (na tela aparece como Posição 1 a 4)
    val heroiId: Int    // id do Heroi que ocupa essa posição
)
