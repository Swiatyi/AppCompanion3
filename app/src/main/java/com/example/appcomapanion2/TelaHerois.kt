package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  TelaHerois.kt                               ║
// ║  TELA 2: lista que LÊ e ESCREVE no banco     ║
// ║  Buscar: LaunchedEffect(Unit) ao abrir       ║
// ║  Inserir/Remover: Coroutine em Dispatchers.IO║
// ║  Clique no card abre o Detalhe do herói      ║
// ╚══════════════════════════════════════════════╝

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
fun TelaHerois(navController: NavHostController, viewModel: MeuViewModel) {
    var nomeNovo by remember { mutableStateOf("") }
    var classeNova by remember { mutableStateOf("") }
    // herói que o jogador está tentando escalar em algum chefe (abre o diálogo)
    var heroiEscolhendoChefe by remember { mutableStateOf<Heroi?>(null) }
    val context = LocalContext.current

    // BUSCAR: assim que a tela abre, lê tudo do banco
    LaunchedEffect(Unit) {
        viewModel.carregarHerois()
        viewModel.carregarChefes()
        viewModel.carregarParty()
    }

    // -------- diálogo: "em qual chefe esse herói entra?" --------
    val heroiAtual = heroiEscolhendoChefe
    if (heroiAtual != null) {
        AlertDialog(
            onDismissRequest = { heroiEscolhendoChefe = null },
            title = { Text("Escalar ${heroiAtual.nome} em qual chefe?") },
            text = {
                Column {
                    viewModel.chefes.forEach { chefe ->
                        val ocupadas = viewModel.quantosNaParty(chefe.id)
                        val cheia = ocupadas >= 4
                        TextButton(
                            enabled = !cheia,
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val coube = viewModel.adicionarHeroiAoChefe(chefe.id, heroiAtual.id)
                                    withContext(Dispatchers.Main) {
                                        val msg = if (coube) "${heroiAtual.nome} escalado em ${chefe.nome}!"
                                        else "A party de ${chefe.nome} já está cheia"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                heroiEscolhendoChefe = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${chefe.nome}  ($ocupadas/4)${if (cheia) " — cheia" else ""}")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { heroiEscolhendoChefe = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roster de Heróis") },
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
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Toque no + de um herói pra escalar ele na party de um chefe",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))

            // -------- formulário de INSERIR novo herói (salva no banco) --------
            OutlinedTextField(
                value = nomeNovo, onValueChange = { nomeNovo = it },
                label = { Text("Nome do herói") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = classeNova, onValueChange = { classeNova = it },
                label = { Text("Classe") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val nome = nomeNovo
                    val classe = classeNova
                    if (nome.isNotBlank() && classe.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val deuCerto = viewModel.adicionarHeroi(nome, classe)
                            withContext(Dispatchers.Main) {
                                if (deuCerto) {
                                    Toast.makeText(context, "$nome adicionado ao roster!", Toast.LENGTH_SHORT).show()
                                    nomeNovo = ""
                                    classeNova = ""
                                } else {
                                    Toast.makeText(context, "Erro ao salvar no banco", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Preencha nome e classe", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Adicionar Herói") }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.herois, key = { it.id }) { heroi ->
                    CardHeroi(
                        heroi = heroi,
                        onClickCard = { navController.navigate(Rotas.heroiDetalhe(heroi.id)) },
                        onEscalarEmChefe = { heroiEscolhendoChefe = heroi },
                        onRemover = {
                            // REMOVER: apaga a linha do banco
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.removerHeroi(heroi)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "${heroi.nome} removido", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// Clique no card abre Detalhe; ícone remove de verdade; "+" abre o diálogo
// de escalar esse herói na party de algum chefe
@Composable
fun CardHeroi(heroi: Heroi, onClickCard: () -> Unit, onEscalarEmChefe: () -> Unit, onRemover: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClickCard() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(heroi.nome, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${heroi.classe} · HP ${heroi.hp} · Stress ${heroi.stress}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRemover) {
                Icon(Icons.Default.Delete, contentDescription = "Remover herói")
            }
            Button(onClick = onEscalarEmChefe) { Text("+") }
        }
    }
}
