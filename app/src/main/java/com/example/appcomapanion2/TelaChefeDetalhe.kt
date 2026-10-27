package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  TelaChefeDetalhe.kt                         ║
// ║  TELA 7: recebe só o ID pela rota e BUSCA o  ║
// ║  chefe no banco (dao.buscarPorId).           ║
// ║  Nível mutável RECALCULA HP/Dano; 4 posições ║
// ║  de party escolhíveis (tabela "party"),      ║
// ║  cada uma ligada a um Herói; EDITAR nome e   ║
// ║  descrição — tudo persistido                 ║
// ╚══════════════════════════════════════════════╝

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appcomapanion2.ui.theme.DungeonGold
import com.example.appcomapanion2.ui.theme.DungeonSurfaceAlt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaChefeDetalhe(navController: NavHostController, viewModel: MeuViewModel, chefeId: Int) {
    var chefe by remember { mutableStateOf<Chefe?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var nomeEdit by remember { mutableStateOf("") }
    var descricaoEdit by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Busca o chefe NO BANCO pelo id e atualiza o que está na tela
    val recarregar: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            chefe = viewModel.buscarChefePorId(chefeId)
        }
    }

    // BUSCAR: ao abrir a tela, lê o chefe (filtrado por id), os heróis e a party
    LaunchedEffect(Unit) {
        val encontrado = viewModel.buscarChefePorId(chefeId)
        chefe = encontrado
        nomeEdit = encontrado?.nome ?: ""
        descricaoEdit = encontrado?.descricao ?: ""
        viewModel.carregarHerois()
        viewModel.carregarParty()
        carregando = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Chefe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DungeonSurfaceAlt,
                    titleContentColor = DungeonGold,
                    navigationIconContentColor = DungeonGold
                )
            )
        }
    ) { padding ->

        val c = chefe

        if (carregando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Carregando...")
            }
        } else if (c == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Chefe não encontrado.")
            }
        } else {
            // ↓ valores CALCULADOS a partir do nível (campo do próprio item)
            val hpCalculado = c.hpBase * c.level
            val danoCalculado = c.danoBase * c.level

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(c.nome, style = MaterialTheme.typography.headlineMedium)
                Text(c.descricao, style = MaterialTheme.typography.bodyLarge)
                Text("Região: ${c.regiao}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.mudarNivelChefe(c, -1)
                            recarregar()
                        }
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir nível")
                    }
                    Text("Nível ${c.level}", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.mudarNivelChefe(c, +1)
                            recarregar()
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar nível")
                    }
                }

                Text("HP: $hpCalculado", style = MaterialTheme.typography.bodyLarge)
                Text("Dano: $danoCalculado", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Party escolhida — toque numa posição pra trocar", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // ↓ 4 posições; cada uma pergunta ao banco (tabela party) qual
                // Herói está ali — combina as duas listas
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (posicao in 0..3) {
                        SlotDaParty(
                            posicao = posicao,
                            heroiAtual = viewModel.heroiNaPosicao(c.id, posicao),
                            todosOsHerois = viewModel.herois,
                            idsJaEscalados = viewModel.idsDaParty(c.id),
                            onEscolher = { novoId ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    viewModel.definirHeroiNaPosicao(c.id, posicao, novoId)
                                }
                            }
                        )
                    }
                }

                // -------- EDITAR nome e descrição (dao.atualizar) --------
                Spacer(modifier = Modifier.height(24.dp))
                Text("Editar chefe", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nomeEdit, onValueChange = { nomeEdit = it },
                    label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descricaoEdit, onValueChange = { descricaoEdit = it },
                    label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val novoNome = nomeEdit
                        val novaDescricao = descricaoEdit
                        if (novoNome.isBlank()) {
                            Toast.makeText(context, "O nome não pode ficar vazio", Toast.LENGTH_SHORT).show()
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.atualizarChefe(c.copy(nome = novoNome, descricao = novaDescricao))
                                recarregar()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Chefe atualizado!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Salvar alterações") }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) { Text("← Voltar") }
            }
        }
    }
}

// Uma posição (1 a 4) da party de um chefe. Toque abre um menu com todos
// os heróis (e a opção "Nenhum"); heróis já escalados em OUTRA posição do
// mesmo chefe aparecem desabilitados, pra não duplicar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotDaParty(
    posicao: Int,
    heroiAtual: Heroi?,
    todosOsHerois: List<Heroi>,
    idsJaEscalados: List<Int>,
    onEscolher: (Int?) -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }

    Box {
        Card(modifier = Modifier.fillMaxWidth().clickable { menuAberto = true }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Posição ${posicao + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(90.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    if (heroiAtual != null) {
                        Text(heroiAtual.nome, style = MaterialTheme.typography.bodyLarge)
                        Text(heroiAtual.classe, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Vazio — toque pra escolher", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
            DropdownMenuItem(
                text = { Text("— Nenhum —") },
                onClick = { onEscolher(null); menuAberto = false }
            )
            todosOsHerois.forEach { heroi ->
                val emOutraPosicao = idsJaEscalados.contains(heroi.id) && heroiAtual?.id != heroi.id
                DropdownMenuItem(
                    text = { Text(heroi.nome + if (emOutraPosicao) " (já escalado)" else "") },
                    enabled = !emOutraPosicao,
                    onClick = { onEscolher(heroi.id); menuAberto = false }
                )
            }
        }
    }
}
