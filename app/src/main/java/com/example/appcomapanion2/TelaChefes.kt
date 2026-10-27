package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  TelaChefes.kt                               ║
// ║  TELA 6: segunda lista com CRUD no banco     ║
// ║  (mesmo padrão da tela de Heróis)            ║
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
fun TelaChefes(navController: NavHostController, viewModel: MeuViewModel) {
    var nomeNovo by remember { mutableStateOf("") }
    var descricaoNova by remember { mutableStateOf("") }
    val context = LocalContext.current

    // BUSCAR: assim que a tela abre, lê os chefes do banco
    LaunchedEffect(Unit) {
        viewModel.carregarChefes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chefes") },
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

            // -------- formulário de INSERIR novo chefe (salva no banco) --------
            OutlinedTextField(
                value = nomeNovo, onValueChange = { nomeNovo = it },
                label = { Text("Nome do chefe") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = descricaoNova, onValueChange = { descricaoNova = it },
                label = { Text("Breve descrição") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val nome = nomeNovo
                    val descricao = descricaoNova
                    if (nome.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val deuCerto = viewModel.adicionarChefe(nome, descricao)
                            withContext(Dispatchers.Main) {
                                if (deuCerto) {
                                    Toast.makeText(context, "$nome adicionado aos Chefes!", Toast.LENGTH_SHORT).show()
                                    nomeNovo = ""
                                    descricaoNova = ""
                                } else {
                                    Toast.makeText(context, "Erro ao salvar no banco", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Preencha ao menos o nome", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Adicionar Chefe") }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.chefes, key = { it.id }) { chefe ->
                    CardChefe(
                        chefe = chefe,
                        onClickCard = { navController.navigate(Rotas.chefeDetalhe(chefe.id)) },
                        onRemover = {
                            // REMOVER: apaga a linha do banco
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.removerChefe(chefe)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "${chefe.nome} removido", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// Clique abre Detalhe; ícone remove de verdade
@Composable
fun CardChefe(chefe: Chefe, onClickCard: () -> Unit, onRemover: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClickCard() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(chefe.nome, style = MaterialTheme.typography.titleMedium)
                Text(chefe.descricao, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            IconButton(onClick = onRemover) {
                Icon(Icons.Default.Delete, contentDescription = "Remover chefe")
            }
        }
    }
}
