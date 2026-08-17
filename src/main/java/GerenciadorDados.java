import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class GerenciadorDados {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Salva qualquer objeto Map em um arquivo JSON
    public static void salvar(String caminhoArquivo, Object dados) {
        try (FileWriter writer = new FileWriter(caminhoArquivo)) {
            GSON.toJson(dados, writer);
        } catch (IOException e) {
            System.err.println("❌ Erro ao salvar arquivo " + caminhoArquivo + ": " + e.getMessage());
        }
    }

    // Carrega o arquivo de saldos (userID -> Double)
    public static Map<String, Double> carregarSaldos(String caminhoArquivo) {
        return carregarMapGenerico(caminhoArquivo, new TypeToken<Map<String, Double>>() {}.getType());
    }

    // Carrega o arquivo de diário (userID -> Long)
    public static Map<String, Long> carregarDiario(String caminhoArquivo) {
        return carregarMapGenerico(caminhoArquivo, new TypeToken<Map<String, Long>>() {}.getType());
    }

    // Carrega o arquivo de álbuns (userID -> Map<ID_Figurinha, Quantidade>)
    public static Map<String, Map<String, Integer>> carregarAlbuns(String caminhoArquivo) {
        return carregarMapGenerico(caminhoArquivo, new TypeToken<Map<String, Map<String, Integer>>>() {}.getType());
    }

    // Carrega o arquivo de mochilas (userID -> Map<ID_Item, Quantidade>)
    public static Map<String, Map<String, Integer>> carregarMochilas(String caminhoArquivo) {
        return carregarMapGenerico(caminhoArquivo, new TypeToken<Map<String, Map<String, Integer>>>() {}.getType());
    }

    private static <T> Map<String, T> carregarMapGenerico(String caminhoArquivo, Type tipo) {
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists()) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(arquivo)) {
            Map<String, T> resultado = GSON.fromJson(reader, tipo);
            return resultado != null ? resultado : new HashMap<>();
        } catch (IOException e) {
            System.err.println("❌ Erro ao carregar arquivo " + caminhoArquivo + ": " + e.getMessage());
            return new HashMap<>();
        }
    }
}