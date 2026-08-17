# 🇧🇷 Bot Colecionável da Seleção Brasileira - Discord

Um bot interativo para Discord desenvolvido em **Java** com o ecossistema **JDA**. O projeto simula um sistema completo de **Economia Virtual**, **Mercado de Pacotes** e um **Álbum de Figurinhas Colecionáveis** dos jogadores da Seleção Brasileira.

---

## 🛠️ Tecnologias Utilizadas

* **Java 17+** — Linguagem principal do projeto.
* **JDA (Java Discord API)** — Biblioteca para integração com a API oficial do Discord.
* **Gson (Google)** — Persistência e manipulação de dados locais via JSON.
* **SLF4J / Logback** — Sistema de logs para monitoramento e depuração da aplicação.

---

## ⚡ Principais Funcionalidades

### 🪙 1. Sistema de Economia
* **Saldo em Carteira (`!saldo`):** Consulta do saldo disponível para operações.
* **Recompensa Diária (`!diario`):** Resgate de moedas a cada 24 horas.
* **Transferências Pix (`!pix @usuario <valor>`):** Envio de saldo entre usuários do servidor.
* **Sistema de Apostas (`!ap/dado`):** Minijogo de dados para multiplicar ou arriscar o saldo.

### 🛍️ 2. Loja & Inventário
* **Mercado Oficial (`!loja`):** Catálogo para compra de pacotes de figurinhas.
* **Mochila / Inventário (`!mochila`):** Gerenciamento de itens adquiridos e não abertos.
* **Abertura de Pacotes (`!abrir pacote <qtd>`):** Mecânica de sorteio para revelar figurinhas aleatórias.

### 📖 3. Coleções & Trocas
* **Álbum Virtual (`!album`):** Exibição da coleção com progresso e detalhes dos jogadores (posições, nomes e IDs).
* **Figurinhas Repetidas (`!repetidas`):** Filtro e contagem de cartas duplicadas no acervo.
* **Sistema de Trocas (`!trocar @usuario <ID>`):** Mecânica para doar ou transferir figurinhas excedentes para outros membros.

### 👑 4. Painel de Gestão Administrativa
* **Injeção de Capital (`!addsaldo @usuario <valor>`):** Comando exclusivo do administrador/desenvolvedor para ajuste de saldos.

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
* Java JDK 17 ou superior instalado.
* Gerenciador de dependências (Maven ou Gradle) configurado no seu ambiente.
* Uma aplicação/bot criada no [Discord Developer Portal](https://discord.com/developers/applications).

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone [https://github.com/vinic0306/bot-colecionavel-java.git](https://github.com/vinic0306/bot-colecionavel-java.git)