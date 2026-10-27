package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  Modelos.kt                                  ║
// ║  Data classes que NÃO são tabelas do banco   ║
// ║  (Heroi, Chefe e PartyMembro têm arquivo     ║
// ║  próprio porque agora são @Entity do Room)   ║
// ╚══════════════════════════════════════════════╝

data class Dungeon(
    val nome: String,
    val risco: String,
    val duracao: String
)

data class ItemSuprimento(
    val nome: String,
    val categoria: String,
    val selecionado: Boolean = false
)
