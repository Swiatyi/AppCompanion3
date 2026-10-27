package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  MeuViewModel.kt                             ║
// ║  Agora quem conversa com o BANCO (Room).     ║
// ║  As telas chamam estas funções; aqui dentro  ║
// ║  ficam os DAOs, o try/catch e o Log.e.       ║
// ║  Heróis, Chefes e Party vêm do banco;        ║
// ║  Dungeons e Suprimentos continuam em memória ║
// ╚══════════════════════════════════════════════╝

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// AndroidViewModel = ViewModel que recebe o "application" (precisa dele
// pra criar o banco, que exige um Context)
class MeuViewModel(application: Application) : AndroidViewModel(application) {

    // ============================================================
    // BANCO — os 3 DAOs vêm do AppDatabase (Singleton)
    // ============================================================
    private val db = AppDatabase.getDatabase(application)
    private val heroiDao = db.heroiDAO()
    private val chefeDao = db.chefeDAO()
    private val partyDao = db.partyDAO()

    // Cópia, na memória, da ÚLTIMA leitura do banco — é o que a tela
    // desenha. Quem manda é o banco; essas listas são só um espelho.
    var herois by mutableStateOf<List<Heroi>>(emptyList())
        private set

    var chefes by mutableStateOf<List<Chefe>>(emptyList())
        private set

    var party by mutableStateOf<List<PartyMembro>>(emptyList())
        private set

    // Primeira vez que o app roda no aparelho: banco vazio, então
    // cadastramos os heróis e chefes do jogo (uma vez só).
    init {
        CoroutineScope(Dispatchers.IO).launch {
            semearDadosIniciais()
        }
    }

    private suspend fun semearDadosIniciais() {
        val prefs = getApplication<Application>()
            .getSharedPreferences("companion", Context.MODE_PRIVATE)

        if (prefs.getBoolean("dados_iniciais_ok", false)) return

        try {
            val heroisIniciais = listOf(
                Heroi(nome = "Reynauld", classe = "Crusader", hp = 20, stress = 35),
                Heroi(nome = "Dismas", classe = "Highwayman", hp = 17, stress = 42),
                Heroi(nome = "Junia", classe = "Plague Doctor", hp = 15, stress = 20),
                Heroi(nome = "Paracelsus", classe = "Occultist", hp = 14, stress = 55),
                Heroi(nome = "Baldwin", classe = "Man-at-Arms", hp = 22, stress = 15)
            )
            val chefesIniciais = listOf(
                Chefe(nome = "Necromancer", descricao = "Comanda os mortos-vivos nas Ruins.", hpBase = 80, danoBase = 12, regiao = "Ruins"),
                Chefe(nome = "Brigand Pope", descricao = "Líder fanático dos Bandidos.", hpBase = 110, danoBase = 18, regiao = "Warrens"),
                Chefe(nome = "The Hag", descricao = "Captura e cozinha heróis no Weald.", hpBase = 95, danoBase = 15, regiao = "Weald"),
                Chefe(nome = "Siren", descricao = "Hipnotiza a party inteira na Cove.", hpBase = 90, danoBase = 14, regiao = "Cove"),
                Chefe(nome = "The Fanatic", descricao = "Guarda fanático dos segredos do Darkest Dungeon.", hpBase = 130, danoBase = 20, regiao = "Darkest Dungeon")
            )

            heroisIniciais.forEach { heroiDao.inserir(it) }
            chefesIniciais.forEach { chefeDao.inserir(it) }

            prefs.edit().putBoolean("dados_iniciais_ok", true).apply()

            carregarHerois()
            carregarChefes()
        } catch (e: Exception) {
            Log.e("Erro ao semear", "Msg: ${e.message}")
        }
    }

    // ============================================================
    // HERÓIS
    // ============================================================

    // BUSCAR (todos) — chamada pelas telas num LaunchedEffect(Unit)
    suspend fun carregarHerois() {
        withContext(Dispatchers.IO) {
            try {
                herois = heroiDao.buscarTodos()
            } catch (e: Exception) {
                Log.e("Erro ao buscar heróis", "${e.message}")
            }
        }
    }

    // BUSCAR (filtrado por id) — usada pela tela de Detalhes
    suspend fun buscarHeroiPorId(id: Int): Heroi? {
        return withContext(Dispatchers.IO) {
            try {
                heroiDao.buscarPorId(id)
            } catch (e: Exception) {
                Log.e("Erro ao buscar herói", "${e.message}")
                null
            }
        }
    }

    // INSERIR — devolve true se deu certo
    suspend fun adicionarHeroi(nome: String, classe: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                heroiDao.inserir(Heroi(nome = nome, classe = classe, hp = 15, stress = 0))
                carregarHerois()
                true
            } catch (e: Exception) {
                Log.e("Erro do Add", "Msg: ${e.message}")
                false
            }
        }
    }

    // ATUALIZAR
    suspend fun atualizarHeroi(heroi: Heroi) {
        withContext(Dispatchers.IO) {
            try {
                heroiDao.atualizar(heroi)
                carregarHerois()
            } catch (e: Exception) {
                Log.e("Erro ao atualizar herói", "Msg: ${e.message}")
            }
        }
    }

    suspend fun aliviarStress(heroi: Heroi) {
        atualizarHeroi(heroi.copy(stress = (heroi.stress - 10).coerceAtLeast(0)))
    }

    suspend fun estressar(heroi: Heroi) {
        atualizarHeroi(heroi.copy(stress = (heroi.stress + 10).coerceAtMost(100)))
    }

    // REMOVER — também tira o herói de qualquer party em que estivesse
    suspend fun removerHeroi(heroi: Heroi) {
        withContext(Dispatchers.IO) {
            try {
                partyDao.deletarPorHeroi(heroi.id)
                heroiDao.deletar(heroi)
                carregarHerois()
                carregarParty()
            } catch (e: Exception) {
                Log.e("Erro ao remover herói", "Msg: ${e.message}")
            }
        }
    }

    // ============================================================
    // CHEFES
    // ============================================================

    suspend fun carregarChefes() {
        withContext(Dispatchers.IO) {
            try {
                chefes = chefeDao.buscarTodos()
            } catch (e: Exception) {
                Log.e("Erro ao buscar chefes", "${e.message}")
            }
        }
    }

    suspend fun buscarChefePorId(id: Int): Chefe? {
        return withContext(Dispatchers.IO) {
            try {
                chefeDao.buscarPorId(id)
            } catch (e: Exception) {
                Log.e("Erro ao buscar chefe", "${e.message}")
                null
            }
        }
    }

    suspend fun adicionarChefe(nome: String, descricao: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                chefeDao.inserir(
                    Chefe(
                        nome = nome,
                        descricao = descricao.ifBlank { "Sem descrição ainda." },
                        hpBase = 70,
                        danoBase = 10
                    )
                )
                carregarChefes()
                true
            } catch (e: Exception) {
                Log.e("Erro do Add", "Msg: ${e.message}")
                false
            }
        }
    }

    suspend fun atualizarChefe(chefe: Chefe) {
        withContext(Dispatchers.IO) {
            try {
                chefeDao.atualizar(chefe)
                carregarChefes()
            } catch (e: Exception) {
                Log.e("Erro ao atualizar chefe", "Msg: ${e.message}")
            }
        }
    }

    suspend fun mudarNivelChefe(chefe: Chefe, delta: Int) {
        atualizarChefe(chefe.copy(level = (chefe.level + delta).coerceIn(1, 10)))
    }

    suspend fun removerChefe(chefe: Chefe) {
        withContext(Dispatchers.IO) {
            try {
                partyDao.deletarPorChefe(chefe.id)
                chefeDao.deletar(chefe)
                carregarChefes()
                carregarParty()
            } catch (e: Exception) {
                Log.e("Erro ao remover chefe", "Msg: ${e.message}")
            }
        }
    }

    // ============================================================
    // PARTY — 4 posições por chefe, cada uma com um Herói (ou vazia)
    // ============================================================

    suspend fun carregarParty() {
        withContext(Dispatchers.IO) {
            try {
                party = partyDao.buscarTodos()
            } catch (e: Exception) {
                Log.e("Erro ao buscar party", "${e.message}")
            }
        }
    }

    // Consultas que só olham o espelho em memória (não mexem no banco)
    fun heroiNaPosicao(chefeId: Int, posicao: Int): Heroi? {
        val membro = party.find { it.chefeId == chefeId && it.posicao == posicao } ?: return null
        return herois.find { it.id == membro.heroiId }
    }

    fun quantosNaParty(chefeId: Int): Int = party.count { it.chefeId == chefeId }

    fun idsDaParty(chefeId: Int): List<Int> =
        party.filter { it.chefeId == chefeId }.map { it.heroiId }

    // Escolhe QUAL herói ocupa a posição (0 a 3) de um chefe.
    // heroiId = null limpa a posição. Usa inserir, atualizar e deletar.
    suspend fun definirHeroiNaPosicao(chefeId: Int, posicao: Int, heroiId: Int?) {
        withContext(Dispatchers.IO) {
            try {
                val membros = partyDao.buscarPorChefe(chefeId)
                val naPosicao = membros.find { it.posicao == posicao }

                // o mesmo herói não pode ocupar 2 posições do mesmo chefe
                if (heroiId != null) {
                    membros
                        .filter { it.heroiId == heroiId && it.posicao != posicao }
                        .forEach { partyDao.deletar(it) }
                }

                if (heroiId == null) {
                    if (naPosicao != null) partyDao.deletar(naPosicao)
                } else if (naPosicao != null) {
                    partyDao.atualizar(naPosicao.copy(heroiId = heroiId))
                } else {
                    partyDao.inserir(PartyMembro(chefeId = chefeId, posicao = posicao, heroiId = heroiId))
                }

                carregarParty()
            } catch (e: Exception) {
                Log.e("Erro ao escalar herói", "Msg: ${e.message}")
            }
        }
    }

    // Botão "+" da tela de Heróis: joga o herói na primeira vaga livre
    // do chefe escolhido. Devolve false se a party já estava cheia (4/4).
    suspend fun adicionarHeroiAoChefe(chefeId: Int, heroiId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val membros = partyDao.buscarPorChefe(chefeId)
                if (membros.any { it.heroiId == heroiId }) {
                    true // já está nessa party, nada a fazer
                } else {
                    val ocupadas = membros.map { it.posicao }
                    val livre = (0..3).firstOrNull { it !in ocupadas }
                    if (livre == null) {
                        false // party cheia
                    } else {
                        partyDao.inserir(PartyMembro(chefeId = chefeId, posicao = livre, heroiId = heroiId))
                        carregarParty()
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("Erro ao escalar herói", "Msg: ${e.message}")
                false
            }
        }
    }

    // ============================================================
    // DUNGEONS — lista fixa, continua em memória (não é tabela)
    // ============================================================
    val dungeons = listOf(
        Dungeon("Ruins", "Médio", "Curta"),
        Dungeon("Warrens", "Alto", "Média"),
        Dungeon("Weald", "Baixo", "Curta"),
        Dungeon("Cove", "Alto", "Longa"),
        Dungeon("Darkest Dungeon", "Muito Alto", "Longa")
    )

    // ============================================================
    // SUPRIMENTOS — checklist temporário, continua em memória
    // ============================================================
    val itensSuprimento = mutableStateListOf(
        ItemSuprimento("Tochas", "Iluminação"),
        ItemSuprimento("Comida", "Sobrevivência"),
        ItemSuprimento("Antídoto", "Cura"),
        ItemSuprimento("Bandagem", "Cura"),
        ItemSuprimento("Chave de Baú", "Utilidade"),
        ItemSuprimento("Pá", "Utilidade")
    )

    fun toggleSuprimento(index: Int) {
        itensSuprimento[index] = itensSuprimento[index].copy(selecionado = !itensSuprimento[index].selecionado)
    }

    fun marcarTodosSuprimentos() {
        for (i in itensSuprimento.indices) itensSuprimento[i] = itensSuprimento[i].copy(selecionado = true)
    }

    fun limparTodosSuprimentos() {
        for (i in itensSuprimento.indices) itensSuprimento[i] = itensSuprimento[i].copy(selecionado = false)
    }
}
