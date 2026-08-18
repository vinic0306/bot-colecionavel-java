import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotDoMitiquinho extends ListenerAdapter {

    // 👑 COLOQUE O SEU ID DO DISCORD AQUI!
    private static final String ID_DONO = System.getenv("DONO_ID");

    private Map<String, Double> saldos = GerenciadorDados.carregarSaldos("saldos.json");
    private Map<String, String> apostasPendentes = new HashMap<>();
    private Map<String, Long> diario = GerenciadorDados.carregarDiario("diario.json");
    
    // 🎴 Álbum do Usuário: userID -> Map<ID_Figurinha, Quantidade>
    private Map<String, Map<String, Integer>> albuns = GerenciadorDados.carregarAlbuns("album.json");

    // 🎒 Mochila do Usuário: userID -> Map<ID_Item, Quantidade>
    private Map<String, Map<String, Integer>> mochilas = GerenciadorDados.carregarMochilas("mochila.json");

    // ⚽ Classe para representar cada Jogador da Seleção
    private static class Figurinha {
        String id;
        String nome;
        String posicao;
        String imagemUrl;

        Figurinha(String id, String nome, String posicao, String imagemUrl) {
            this.id = id;
            this.nome = nome;
            this.posicao = posicao;
            this.imagemUrl = imagemUrl;
        }
    }

    // Lista Oficial de Figurinhas - Seleção Brasileira
    private static final List<Figurinha> LISTA_FIGURINHAS = Arrays.asList(
        // Goleiros
        new Figurinha("br_alisson", "Alisson", "Goleiro 🧤", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_ederson", "Ederson", "Goleiro 🧤", "https://i.imgur.com/8Q9Z5Xm.png"),

        // Defensores
        new Figurinha("br_marquinhos", "Marquinhos", "Zagueiro 🛡️", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_militao", "Eder Militão", "Zagueiro 🛡️", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_gabriel", "Gabriel Magalhães", "Zagueiro 🛡️", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_danilo", "Danilo", "Lateral ⚡", "https://i.imgur.com/8Q9Z5Xm.png"),

        // Meio-Campistas
        new Figurinha("br_casemiro", "Casemiro", "Volante 🧠", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_guimaraes", "Bruno Guimarães", "Meio-Campo ⚽", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_paqueta", "Lucas Paquetá", "Meio-Campo 🪄", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_gerson", "Gerson", "Meio-Campo ⚽", "https://i.imgur.com/8Q9Z5Xm.png"),

        // Atacantes
        new Figurinha("br_vinicius", "Vinicius Jr.", "Atacante ⚡", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_rodrygo", "Rodrygo", "Atacante ✨", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_raphinha", "Raphinha", "Atacante 🔥", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_endrick", "Endrick", "Atacante 💎", "https://i.imgur.com/8Q9Z5Xm.png"),
        new Figurinha("br_richarlison", "Richarlison", "Atacante 🎯", "https://i.imgur.com/8Q9Z5Xm.png")
    );

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");

        JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new BotDoMitiquinho())
                .build();

        System.out.println("🤖 Bot de Apostas e Colecionáveis da Seleção ligado!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String texto = event.getMessage().getContentRaw();
        String userId = event.getAuthor().getId();

        // 🟢 Se o usuário nunca interagiu com o bot antes, ganha R$ 100 na hora
        if (!saldos.containsKey(userId)) {
            saldos.put(userId, 100.0);
            GerenciadorDados.salvar("saldos.json", saldos);
            event.getMessage().reply("🎉 **Bem-vindo!** Você ganhou **R$ 100,00** de saldo inicial para colecionar a Seleção!").queue();
        }

        // 👑 COMANDO EXCLUSIVO DO DONO DO BOT: !addsaldo @usuario <valor>
        if (texto.toLowerCase().startsWith("!addsaldo") || texto.toLowerCase().startsWith("!giversaldo")) {
            if (!userId.equals(ID_DONO)) {
                event.getMessage().reply("❌ **Acesso Negado!** Apenas o desenvolvedor do bot pode usar esse comando.").queue();
                return;
            }

            String[] partes = texto.split("\\s+");

            if (partes.length < 3 || event.getMessage().getMentions().getUsers().isEmpty()) {
                event.getMessage().reply("❌ **Uso incorreto!** Use: `!addsaldo @usuario <valor>`").queue();
                return;
            }

            User destino = event.getMessage().getMentions().getUsers().get(0);

            try {
                double valorAdicionar = Double.parseDouble(partes[partes.length - 1].replace(",", "."));
                double saldoAtual = saldos.getOrDefault(destino.getId(), 100.0);
                double novoSaldo = saldoAtual + valorAdicionar;

                if (novoSaldo < 0) {
                    novoSaldo = 0;
                }

                saldos.put(destino.getId(), novoSaldo);
                GerenciadorDados.salvar("saldos.json", saldos);

                event.getMessage().reply(
                    "👑 **ADMINISTRAÇÃO DE SALDO**\n" +
                    "────────────\n" +
                    "Foi adicionado **R$ " + String.format("%.2f", valorAdicionar) + "** para " + destino.getAsMention() + "!\n" +
                    "🪙 **Novo Saldo:** `R$ " + String.format("%.2f", novoSaldo) + "`\n" +
                    "────────────"
                ).queue();

            } catch (NumberFormatException e) {
                event.getMessage().reply("❌ Digite um valor numérico válido! Exemplo: `!addsaldo @usuario 100`").queue();
            }
            return;
        }

        // Comando do menu
        if (texto.equalsIgnoreCase("!menu")) {
            event.getMessage().reply(
                "📜 **PAINEL DE COMANDOS - SELEÇÃO BRASILEIRA**\n" +
                "─────────────────────────\n" +
                "🪙 `!saldo` — *Veja seu saldo em carteira*\n" +
                "📆 `!diario` — *Receba R$ 50 de saldo diário*\n" +
                "🎲 `!ap/dado` — *Apostas com base em um dado (6 lados)*\n" +
                "💸 `!pix @amigo <valor>` — *Transfira dinheiro para um amigo*\n" +
                "🛍️ `!loja` — *Veja os pacotes disponíveis na loja*\n" +
                "🛒 `!comprar pacote [quantidade]` — *Compre pacotes para guardar na mochila*\n" +
                "🎒 `!mochila` — *Veja seus pacotes guardados*\n" +
                "📦 `!abrir pacote [quantidade]` — *Abra pacotes da Seleção Brasileira*\n" +
                "📖 `!album` — *Veja seus jogadores colecionados*\n" +
                "🔄 `!repetidas` — *Veja jogadores repetidos*\n" +
                "🎁 `!trocar @amigo ID` — *Envie um jogador repetido para um amigo*\n" +
                "─────────────────────────"
            ).queue();
            return;
        }

        // Comando do saldo
        if (texto.equalsIgnoreCase("!saldo")) {
            double saldoAtual = saldos.getOrDefault(userId, 100.0);
            event.getMessage().reply("Seu saldo é de: **R$ " + String.format("%.2f", saldoAtual) + "**").queue();
            return;
        }

        // 🏪 LOJA
        if (texto.equalsIgnoreCase("!loja")) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("🛍️ LOJA OFICIAL - SELEÇÃO BRASILEIRA 🇧🇷");
            embed.setDescription(
                "Use `!comprar pacote [quantidade]` para comprar!\n\n" +
                "📦 **Pacote da Seleção Canarinho**\n" +
                "└ 💵 **Preço:** R$ 15,00 por unidade\n" +
                "└ 📝 **Item:** `pacote`\n" +
                "└ ⚽ Contém 5 figurinhas de jogadores para o seu álbum!\n\n" +
                "*(Novas coleções em breve!)*"
            );
            embed.setColor(0x009933);
            event.getMessage().replyEmbeds(embed.build()).queue();
            return;
        }

        // 🛒 COMPRAR
        if (texto.toLowerCase().startsWith("!comprar")) {
            String[] partes = texto.split("\\s+");

            if (partes.length < 2) {
                event.getMessage().reply("❌ Use: `!comprar pacote [quantidade]`\nExemplo: `!comprar pacote` ou `!comprar pacote 3`").queue();
                return;
            }

            String item = partes[1].toLowerCase();
            int quantidade = 1;

            if (partes.length >= 3) {
                try {
                    quantidade = Integer.parseInt(partes[2]);
                    if (quantidade <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    event.getMessage().reply("❌ Digite uma quantidade válida!").queue();
                    return;
                }
            }

            if (!item.equals("pacote")) {
                event.getMessage().reply("❌ Item não encontrado na loja! Use `!loja` para ver o catálogo.").queue();
                return;
            }

            double precoUnitario = 15.0;
            double custoTotal = precoUnitario * quantidade;
            double saldoAtual = saldos.getOrDefault(userId, 100.0);

            if (saldoAtual < custoTotal) {
                event.getMessage().reply("❌ **Saldo insuficiente!** Comprar " + quantidade + "x pacote(s) custa **R$ " + String.format("%.2f", custoTotal) + "**. Seu saldo: **R$ " + String.format("%.2f", saldoAtual) + "**").queue();
                return;
            }

            // Desconta o dinheiro
            saldos.put(userId, saldoAtual - custoTotal);
            GerenciadorDados.salvar("saldos.json", saldos);

            // Adiciona na mochila
            Map<String, Integer> minhaMochila = mochilas.getOrDefault(userId, new HashMap<>());
            minhaMochila.put("pacote", minhaMochila.getOrDefault("pacote", 0) + quantidade);
            mochilas.put(userId, minhaMochila);
            GerenciadorDados.salvar("mochila.json", mochilas);

            event.getMessage().reply(
                "🛒 **COMPRA REALIZADA!**\n" +
                "Você comprou **" + quantidade + "x Pacote(s) da Seleção 🇧🇷** por **R$ " + String.format("%.2f", custoTotal) + "**!\n" +
                "🎒 Guardados na mochila! Use `!mochila` para ver ou `!abrir pacote` para abrir!"
            ).queue();
            return;
        }

        // 🎒 MOCHILA
        if (texto.equalsIgnoreCase("!mochila") || texto.equalsIgnoreCase("!inv")) {
            Map<String, Integer> minhaMochila = mochilas.getOrDefault(userId, new HashMap<>());
            StringBuilder sb = new StringBuilder();

            int pacotes = minhaMochila.getOrDefault("pacote", 0);

            if (pacotes > 0) {
                sb.append("📦 **Pacote Seleção Brasileira:** `").append(pacotes).append("` unidade(s)\n");
            }

            if (sb.length() == 0) {
                sb.append("*Sua mochila está vazia! Compre pacotes na `!loja`.*");
            } else {
                sb.append("\n💡 *Use `!abrir pacote [quantidade]` para abrir!*");
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("🎒 Mochila de " + event.getAuthor().getName());
            embed.setDescription(sb.toString());
            embed.setColor(0xFFDF00);

            event.getMessage().replyEmbeds(embed.build()).queue();
            return;
        }

        // 📦 ABRIR PACOTE
        if (texto.toLowerCase().startsWith("!abrir")) {
            String[] partes = texto.split("\\s+");

            if (partes.length < 2) {
                event.getMessage().reply("❌ Use: `!abrir pacote [quantidade]`\nExemplo: `!abrir pacote` ou `!abrir pacote 2`").queue();
                return;
            }

            String item = partes[1].toLowerCase();
            int quantidadeParaAbrir = 1;

            if (partes.length >= 3) {
                try {
                    quantidadeParaAbrir = Integer.parseInt(partes[2]);
                    if (quantidadeParaAbrir <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    event.getMessage().reply("❌ Digite uma quantidade válida!").queue();
                    return;
                }
            }

            if (!item.equals("pacote")) {
                event.getMessage().reply("❌ Item não reconhecido! Veja os itens na sua `!mochila`.").queue();
                return;
            }

            Map<String, Integer> minhaMochila = mochilas.getOrDefault(userId, new HashMap<>());
            int pacotesPossuidos = minhaMochila.getOrDefault("pacote", 0);

            if (pacotesPossuidos < quantidadeParaAbrir) {
                event.getMessage().reply("❌ Você não tem pacotes suficientes! Você possui: **" + pacotesPossuidos + "**").queue();
                return;
            }

            // Remove o(s) pacote(s)
            if (pacotesPossuidos - quantidadeParaAbrir == 0) {
                minhaMochila.remove("pacote");
            } else {
                minhaMochila.put("pacote", pacotesPossuidos - quantidadeParaAbrir);
            }
            mochilas.put(userId, minhaMochila);
            GerenciadorDados.salvar("mochila.json", mochilas);

            // Sorteia as figurinhas (5 por pacote)
            Random rand = new Random();
            Map<String, Integer> meualbum = albuns.getOrDefault(userId, new HashMap<>());
            List<Figurinha> ultimasTiradas = new ArrayList<>();

            StringBuilder sb = new StringBuilder();
            sb.append("📦 **ABRINDO ").append(quantidadeParaAbrir).append(" PACOTE(S) DA SELEÇÃO 🇧🇷!**\n");
            sb.append("──────────────────────────\n");

            int totalFigurinhas = quantidadeParaAbrir * 5;
            for (int i = 0; i < totalFigurinhas; i++) {
                Figurinha f = LISTA_FIGURINHAS.get(rand.nextInt(LISTA_FIGURINHAS.size()));
                ultimasTiradas.add(f);

                int qte = meualbum.getOrDefault(f.id, 0) + 1;
                meualbum.put(f.id, qte);

                sb.append(f.posicao).append(" **").append(f.nome).append("** (`").append(f.id).append("`) ");
                if (qte > 1) {
                    sb.append("*(REPETIDA x").append(qte).append(")*");
                } else {
                    sb.append("✨ *(NOVO JOGADOR!)*");
                }
                sb.append("\n");
            }

            albuns.put(userId, meualbum);
            GerenciadorDados.salvar("album.json", albuns);

            sb.append("──────────────────────────\n");
            sb.append("🎒 Pacotes restantes: `").append(minhaMochila.getOrDefault("pacote", 0)).append("`");

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("✨ Jogadores Encontrados! ✨");
            embed.setDescription(sb.toString());
            embed.setColor(0x009933);
            if (!ultimasTiradas.isEmpty()) {
                embed.setThumbnail(ultimasTiradas.get(0).imagemUrl);
            }

            event.getMessage().replyEmbeds(embed.build()).queue();
            return;
        }

        // 📖 VER ÁLBUM
        if (texto.equalsIgnoreCase("!album")) {
            Map<String, Integer> meualbum = albuns.getOrDefault(userId, new HashMap<>());
            StringBuilder sb = new StringBuilder();

            int totalColecionadas = meualbum.size();
            sb.append("📊 **Progresso:** `").append(totalColecionadas).append("/").append(LISTA_FIGURINHAS.size()).append("` Jogadores\n\n");

            for (Figurinha f : LISTA_FIGURINHAS) {
                int qte = meualbum.getOrDefault(f.id, 0);
                if (qte > 0) {
                    sb.append("✅ ").append(f.posicao).append(" **").append(f.nome).append("** (`").append(f.id).append("`) - **x").append(qte).append("**\n");
                } else {
                    sb.append("❌ ❓ *Pendente* (`").append(f.id).append("`)\n");
                }
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("📖 Álbum Oficial - Seleção Brasileira 🇧🇷");
            embed.setDescription(sb.toString());
            embed.setColor(0xFFDF00);

            event.getMessage().replyEmbeds(embed.build()).queue();
            return;
        }

        // 🔄 VER REPETIDAS
        if (texto.equalsIgnoreCase("!repetidas")) {
            Map<String, Integer> meualbum = albuns.getOrDefault(userId, new HashMap<>());
            StringBuilder sb = new StringBuilder();
            boolean temRepetida = false;

            for (Figurinha f : LISTA_FIGURINHAS) {
                int qte = meualbum.getOrDefault(f.id, 0);
                if (qte > 1) {
                    temRepetida = true;
                    sb.append("🔄 ").append(f.posicao).append(" **").append(f.nome).append("** (`").append(f.id).append("`) — Sobrando: **").append(qte - 1).append("**\n");
                }
            }

            if (!temRepetida) {
                event.getMessage().reply("✨ Você não possui figurinhas repetidas no momento!").queue();
                return;
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("🔄 Jogadores Repetidos de " + event.getAuthor().getName());
            embed.setDescription(sb.toString() + "\n> 💡 *Use `!trocar @amigo ID_DO_JOGADOR` para doar ou trocar!*");
            embed.setColor(0x002776);

            event.getMessage().replyEmbeds(embed.build()).queue();
            return;
        }

        // 🎁 TROCAR
        if (texto.toLowerCase().startsWith("!trocar")) {
            String[] partes = texto.split("\\s+");

            if (partes.length < 3 || event.getMessage().getMentions().getUsers().isEmpty()) {
                event.getMessage().reply("❌ **Uso incorreto!** Use: `!trocar @amigo ID_FIGURINHA`\nExemplo: `!trocar @amigo br_vinicius`").queue();
                return;
            }

            User destino = event.getMessage().getMentions().getUsers().get(0);
            String idFigurinha = partes[partes.length - 1].toLowerCase();

            if (destino.isBot() || destino.getId().equals(userId)) {
                event.getMessage().reply("❌ Usuário de destino inválido!").queue();
                return;
            }

            Map<String, Integer> meuAlbum = albuns.getOrDefault(userId, new HashMap<>());
            int qteEuTenho = meuAlbum.getOrDefault(idFigurinha, 0);

            if (qteEuTenho <= 1) {
                event.getMessage().reply("❌ Você precisa ter pelo menos **1 repetida** (total de 2 ou mais) dessa figurinha para doar!").queue();
                return;
            }

            Figurinha figEnviada = null;
            for (Figurinha f : LISTA_FIGURINHAS) {
                if (f.id.equalsIgnoreCase(idFigurinha)) {
                    figEnviada = f;
                    break;
                }
            }

            if (figEnviada == null) {
                event.getMessage().reply("❌ ID de jogador inválido! Use `!repetidas` para consultar os IDs.").queue();
                return;
            }

            meuAlbum.put(idFigurinha, qteEuTenho - 1);
            albuns.put(userId, meuAlbum);

            Map<String, Integer> albumDestino = albuns.getOrDefault(destino.getId(), new HashMap<>());
            albumDestino.put(idFigurinha, albumDestino.getOrDefault(idFigurinha, 0) + 1);
            albuns.put(destino.getId(), albumDestino);

            GerenciadorDados.salvar("album.json", albuns);

            event.getMessage().reply("🎁 **TRANSFERÊNCIA CONCLUÍDA!**\nVocê enviou " + figEnviada.posicao + " **" + figEnviada.nome + "** para " + destino.getAsMention() + "!").queue();
            return;
        }

        // 💸 PIX
        if (texto.toLowerCase().startsWith("!pix")) {
            String[] partes = texto.split("\\s+");

            if (partes.length < 3 || event.getMessage().getMentions().getUsers().isEmpty()) {
                event.getMessage().reply("❌ **Uso incorreto!** Use: `!pix @amigo <valor>`").queue();
                return;
            }

            User destino = event.getMessage().getMentions().getUsers().get(0);

            if (destino.isBot() || destino.getId().equals(userId)) {
                event.getMessage().reply("❌ Operação de Pix inválida.").queue();
                return;
            }

            try {
                double valorPix = Double.parseDouble(partes[partes.length - 1].replace(",", "."));

                if (valorPix <= 0) {
                    event.getMessage().reply("❌ O valor do Pix deve ser maior que zero!").queue();
                    return;
                }

                double saldoRemetente = saldos.getOrDefault(userId, 100.0);

                if (saldoRemetente < valorPix) {
                    event.getMessage().reply("❌ **Saldo insuficiente!** Seu saldo atual: **R$ " + String.format("%.2f", saldoRemetente) + "**").queue();
                    return;
                }

                double saldoDestino = saldos.getOrDefault(destino.getId(), 100.0);

                saldos.put(userId, saldoRemetente - valorPix);
                saldos.put(destino.getId(), saldoDestino + valorPix);

                GerenciadorDados.salvar("saldos.json", saldos);

                event.getMessage().reply("💸 Pix de **R$ " + String.format("%.2f", valorPix) + "** enviado com sucesso para " + destino.getAsMention() + "!").queue();

            } catch (NumberFormatException e) {
                event.getMessage().reply("❌ Digite um valor numérico válido.").queue();
            }
            return;
        }

        // 🎲 APOSTAS (DADO)
        if (apostasPendentes.containsKey(userId)) {
            try {
                double valorAposta = Double.parseDouble(texto.replace(",", "."));
                apostasPendentes.remove(userId);

                if (valorAposta <= 0) {
                    event.getMessage().reply("❌ O valor deve ser maior que zero!").queue();
                    return;
                }

                double saldoAtual = saldos.getOrDefault(userId, 100.0);

                if (saldoAtual < valorAposta) {
                    event.getMessage().reply("❌ **Saldo insuficiente!**").queue();
                } else {
                    int dado = new Random().nextInt(6) + 1;

                    if (dado >= 4) {
                        saldoAtual += valorAposta;
                        saldos.put(userId, saldoAtual);
                        GerenciadorDados.salvar("saldos.json", saldos);
                        event.getMessage().reply("🎲 Tirou **" + dado + "**! Você GANHOU **R$ " + String.format("%.2f", valorAposta) + "**! 🎉\nNovo saldo: **R$ " + String.format("%.2f", saldoAtual) + "**").queue();
                    } else {
                        saldoAtual -= valorAposta;
                        saldos.put(userId, saldoAtual);
                        GerenciadorDados.salvar("saldos.json", saldos);
                        event.getMessage().reply("🎲 Tirou **" + dado + "**! Você PERDEU **R$ " + String.format("%.2f", valorAposta) + "**... 💥\nNovo saldo: **R$ " + String.format("%.2f", saldoAtual) + "**").queue();
                    }
                }
            } catch (NumberFormatException e) {
                event.getMessage().reply("❌ Entrada inválida. Aposta cancelada.").queue();
                apostasPendentes.remove(userId);
            }
            return;
        }

        if (texto.equalsIgnoreCase("!ap/dado")) {
            apostasPendentes.put(userId, "dado");
            event.getMessage().reply("🎲 **Jogo do Dado!** Digite abaixo quanto deseja apostar:").queue();
            return;
        }

        // 📅 DIÁRIO
        if (texto.equalsIgnoreCase("!diario")) {
            long agora = System.currentTimeMillis();
            long ultimoResgate = diario.getOrDefault(userId, 0L);
            long vinteQuatroHoras = 24 * 60 * 60 * 1000L;

            if (agora - ultimoResgate < vinteQuatroHoras) {
                event.getMessage().reply("⏳ Você já resgatou sua recompensa hoje! Volte em 24 horas.").queue();
                return;
            }

            double saldoAtual = saldos.getOrDefault(userId, 100.0) + 50.0;
            saldos.put(userId, saldoAtual);
            diario.put(userId, agora);

            GerenciadorDados.salvar("saldos.json", saldos);
            GerenciadorDados.salvar("diario.json", diario);

            event.getMessage().reply("🎁 **RECOMPENSA DIÁRIA!** Você recebeu **+ R$ 50,00**! Novo Saldo: `R$ " + String.format("%.2f", saldoAtual) + "`").queue();
        }
    }
}
