# Darkest Dungeon Companion

App Android **não-oficial** de apoio ao jogo *Darkest Dungeon*. Mostra os heróis, as expedições (dungeons), os chefes de cada região e uma checklist de suprimentos para levar em cada expedição.

Projeto desenvolvido para a disciplina de Desenvolvimento de Aplicativos Móveis.

## O que o app faz

- **Home:** tela inicial com botões para as demais áreas do app.
- **Heróis:** lista de heróis. Dá para adicionar, remover e abrir o detalhe de cada um (onde também dá para editar nome/classe e mexer no stress).
- **Chefes:** lista de chefes. Dá para adicionar, remover e abrir o detalhe (editar, mudar o nível e escolher quais heróis ocupam as 4 posições da party).
- **Expedições:** lista de dungeons com busca, seletor de nível de tocha e o chefe de cada região.
- **Suprimentos:** checklist de itens para a expedição.
- **Persistência:** heróis, chefes e parties ficam salvos no banco **Room**. Ao fechar e abrir o app, os dados continuam lá.

## Tecnologias

| Item | Versão |
|---|---|
| Linguagem | Kotlin 2.2.10 |
| Interface | Jetpack Compose (Material 3, BOM 2026.02.01) |
| Navegação | Navigation Compose 2.10.1 |
| Estado | ViewModel (Lifecycle 2.11.0) |
| Banco de dados | Room 3 (3.0.0-rc01) com KSP 2.3.12 |
| Android Gradle Plugin | 9.3.3 |
| minSdk / compileSdk | 24 / 37 |

## Pré-requisitos

- **Android Studio** em versão recente, compatível com o Android Gradle Plugin 9.3.3. Se o Android Studio pedir para atualizar o plugin ou o Gradle, aceite.
- **Conexão com a internet** na primeira abertura (o Gradle baixa as bibliotecas).
- Um **emulador** (Android 7.0 / API 24 ou superior) **ou** um celular Android com a depuração USB ativada.

## Como abrir e rodar o projeto

1. **Baixe o projeto.** Escolha um dos jeitos:
   - Pelo Git: `git clone <URL-DO-REPOSITORIO>`
   - Ou clique em **Code → Download ZIP** no GitHub e descompacte em uma pasta do seu computador.
2. **Abra no Android Studio.** Na tela inicial, clique em **Open** e selecione a pasta do projeto (a que contém os arquivos `build.gradle.kts` e `settings.gradle.kts`). Não selecione uma pasta de dentro dela.
3. **Espere o Gradle sincronizar.** Aparece uma barra de progresso no canto inferior. Na primeira vez pode demorar alguns minutos, porque baixa as bibliotecas. Se aparecer o aviso **Sync Now**, clique nele.
4. **Escolha onde rodar.** Na barra superior, no seletor de dispositivos:
   - **Emulador:** abra o **Device Manager**, clique em **Create Device**, escolha um celular (por exemplo, Pixel) e uma imagem de sistema com Android 7.0 (API 24) ou superior, e clique em **Finish**.
   - **Celular físico:** ative a **Depuração USB** nas opções de desenvolvedor, conecte o cabo e autorize o computador no celular.
5. **Rode o app.** Clique no botão verde **Run ▶** (ou use `Shift + F10`). O app abre na tela Home.

## Como testar a persistência

1. Abra **Heróis**, preencha nome e classe e clique em **Adicionar Herói**. O herói aparece na lista.
2. Feche o app **por completo** (abra a lista de apps recentes e deslize o app para fora).
3. Abra o app de novo e entre em **Heróis**. O herói adicionado continua lá.
4. Repita o teste removendo um item (ícone de lixeira) e editando um item (tela de detalhe → **Salvar alterações**).

> Na primeira vez que o app roda, ele cadastra uma vez os heróis e chefes iniciais do jogo. Depois disso, tudo o que você adicionar, editar ou remover é preservado.

## Estrutura do projeto

Todos os arquivos ficam em `app/src/main/java/com/example/appcomapanion2/`:

| Arquivo | Função |
|---|---|
| `MainActivity.kt` | Ponto de entrada do app |
| `AppNavigation.kt` / `Rotas.kt` | NavHost e nomes das rotas |
| `MeuViewModel.kt` | Estado do app e acesso ao banco |
| `Heroi.kt`, `Chefe.kt`, `PartyMembro.kt` | Entities do Room (tabelas) |
| `HeroiDAO.kt`, `ChefeDAO.kt`, `PartyDAO.kt` | DAOs (insert, buscar, atualizar, deletar) |
| `AppDatabase.kt` | Banco Room (Singleton) |
| `Modelos.kt` | Data classes que não são tabelas (Dungeon, ItemSuprimento) |
| `BottomNavBar.kt` | Barra de navegação inferior |
| `TelaHome.kt`, `TelaHerois.kt`, `TelaHeroiDetalhe.kt`, `TelaDungeon.kt`, `TelaSuprimentos.kt`, `TelaChefes.kt`, `TelaChefeDetalhe.kt` | Telas do app |
| `ui/theme/` | Cores e tema (visual escuro com dourado) |

## Problemas comuns

- **Erro no Sync sobre versão do Room ou do KSP:** abra `gradle/libs.versions.toml` e confira as linhas `room` e `ksp`. Se o Android Studio sugerir uma versão mais nova, pode atualizar.
- **"SDK not found" ou plataforma Android 37 ausente:** abra **Tools → SDK Manager** e instale a plataforma indicada na mensagem de erro.
- **O app abre, mas os dados não aparecem na primeira vez:** feche o app e abra de novo. Se persistir, desinstale o app do emulador e rode novamente.
- **Quer recomeçar do zero:** desinstale o app do emulador/celular (isso apaga o banco) e rode de novo.

## Integrantes

- Guilherme Swiatyi dos Santos
