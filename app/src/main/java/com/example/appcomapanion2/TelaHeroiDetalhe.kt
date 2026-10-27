package com.example.appcomapanion2

// ╔══════════════════════════════════════════════╗
// ║  TelaHeroiDetalhe.kt                         ║
// ║  TELA 3: recebe só o ID pela rota e BUSCA o  ║
// ║  herói no banco (dao.buscarPorId).           ║
// ║  Calcula o nível de stress e permite EDITAR  ║
// ║  (dao.atualizar) — tudo persistido           ║
// ╚══════════════════════════════════════════════╝

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun TelaHeroiDetalhe(navController: NavHostController, viewModel: MeuViewModel, heroiId: Int) {
    var heroi by remember { mutableStateOf<Heroi?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var nomeEdit by remember { mutableStateOf("") }
    var classeEdit by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Busca o herói NO BANCO pelo id e atualiza o que está na tela
    val recarregar: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            heroi = viewModel.buscarHeroiPorId(heroiId)
        }
    }

    // BUSCAR: ao abrir a tela, lê o herói do banco (consulta filtrada)
    LaunchedEffect(Unit) {
        val encontrado = viewModel.buscarHeroiPorId(heroiId)
        heroi = encontrado
        nomeEdit = encontrado?.nome ?: ""
        classeEdit = encontrado?.classe ?: ""
        carregando = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Herói") },
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

        val h = heroi

        if (carregando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Carregando...")
            }
        } else if (h == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Herói não encontrado.")
            }
        } else {
            // ↓ dado CALCULADO a partir do stress do herói, não só reexibido
            val nivelStress = when {
                h.stress < 30 -> "Estável"
                h.stress < 60 -> "Tenso"
                else -> "Crítico — risco de aflição"
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(h.nome, style = MaterialTheme.typography.headlineMedium)
                Text(h.classe, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))
                Text("HP: ${h.hp}", style = MaterialTheme.typography.bodyLarge)
                Text("Stress: ${h.stress}  ·  $nivelStress", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // ATUALIZAR: grava a mudança no banco e relê o herói
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.aliviarStress(h)
                            recarregar()
                        }
                    }) { Text("Aliviar (-10)") }

                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.estressar(h)
                            recarregar()
                        }
                    }) { Text("Estressar (+10)") }
                }

                // -------- EDITAR nome e classe (dao.atualizar) --------
                Spacer(modifier = Modifier.height(24.dp))
                Text("Editar herói", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nomeEdit, onValueChange = { nomeEdit = it },
                    label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = classeEdit, onValueChange = { classeEdit = it },
                    label = { Text("Classe") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val novoNome = nomeEdit
                        val novaClasse = classeEdit
                        if (novoNome.isBlank() || novaClasse.isBlank()) {
                            Toast.makeText(context, "Preencha nome e classe", Toast.LENGTH_SHORT).show()
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                viewModel.atualizarHeroi(h.copy(nome = novoNome, classe = novaClasse))
                                recarregar()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Herói atualizado!", Toast.LENGTH_SHORT).show()
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
