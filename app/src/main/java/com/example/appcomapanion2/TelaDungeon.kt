package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  TelaDungeon.kt                              ║
// ║  TELA 4: Expedições — busca + seletor de     ║
// ║  tocha + dungeons, cada uma com o chefe da   ║
// ║  região (lido do banco)                      ║
// ╚══════════════════════════════════════════════╝

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appcomapanion2.ui.theme.DungeonGold
import com.example.appcomapanion2.ui.theme.DungeonSurfaceAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDungeon(navController: NavHostController, viewModel: MeuViewModel) {
    var filtro by remember { mutableStateOf("") }
    var nivelTocha by remember { mutableStateOf(50) }
    val dungeonsFiltradas = viewModel.dungeons.filter { it.nome.contains(filtro, ignoreCase = true) }

    // BUSCAR: ao abrir a tela, lê os chefes do banco (pra mostrar o chefe de cada região)
    LaunchedEffect(Unit) {
        viewModel.carregarChefes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expedições") },
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

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                label = { Text("Buscar dungeon") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nível de Tocha: $nivelTocha%", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { if (nivelTocha > 0) nivelTocha -= 10 }) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                }
                IconButton(onClick = { if (nivelTocha < 100) nivelTocha += 10 }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Dungeons disponíveis", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dungeonsFiltradas) { dungeon ->
                    // ↓ busca na lista de CHEFES qual deles pertence a essa região
                    // (ordem não importa, por isso .find em vez de depender de índice)
                    val chefeDaRegiao = viewModel.chefes.find { it.regiao == dungeon.nome }
                    CardDungeon(
                        dungeon = dungeon,
                        chefeDaRegiao = chefeDaRegiao,
                        onClickChefe = {
                            chefeDaRegiao?.let { navController.navigate(Rotas.chefeDetalhe(it.id)) }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDungeon(dungeon: Dungeon, chefeDaRegiao: Chefe?, onClickChefe: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(dungeon.nome, style = MaterialTheme.typography.titleMedium)
                    Text("Duração: ${dungeon.duracao}", style = MaterialTheme.typography.bodySmall)
                }
                AssistChip(onClick = {}, label = { Text(dungeon.risco) })
            }

            // ↓ dado vindo da lista de CHEFES, exibido dentro do card de Dungeon —
            // combina as duas listas também aqui, além do Detalhe do Chefe
            if (chefeDaRegiao != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = onClickChefe,
                    label = { Text("Chefe: ${chefeDaRegiao.nome}") }
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Chefe: a definir", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
