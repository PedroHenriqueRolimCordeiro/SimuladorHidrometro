package br.com.simulador.hidrometro.client;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import br.com.simulador.hidrometro.facade.SHAFacade;

public final class CLIClient {
    private CLIClient() {}

    private static double getDouble(Map<String,String> m, String k, double def) {
        if (m == null || !m.containsKey(k)) {
            System.err.println("chave inválida: " + k);
            return def;
        }
        String v = m.get(k);
        if (v == null || v.isBlank()) return def;
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            System.err.println("valor inválido para --" + k + ": " + v + " (esperado double)");
            return def;
        }
    }

    private static int getInt(Map<String,String> m, String k, int def) {
        if (m == null || !m.containsKey(k)) {
            System.err.println("chave inválida: " + k);
            return def;
        }
        String v = m.get(k);
        if (v == null || v.isBlank()) return def;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            System.err.println("valor inválido para --" + k + ": " + v + " (esperado int)");
            return def;
        }
    }

    private static Map<String,String> parseFlags(String[] args) {
        Map<String,String> out = new LinkedHashMap<>();
        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            if (!a.startsWith("--")) continue;

            String key, val;
            int eq = a.indexOf('=');
            if (eq >= 0) {                 // --chave=valor
                key = a.substring(2, eq);
                val = a.substring(eq + 1);
            } else {                       // --chave valor  |  --flag (boolean)
                key = a.substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                val = args[++i];
                } else {
                val = "true";              // presença da flag => true
                }
            }
            out.put(key, val);
        }
        return out;
    };

    public static void run(String[] args) {
        if (args == null || args.length == 0) return;

        SHAFacade facade = SHAFacade.getInstance();
        Map<String, String> opts = parseFlags(args);
        String cmd = args[0].toLowerCase();
        switch (cmd) {
            case "config":
                Map<String,String> m = new HashMap<>();
                double bitola = getDouble(opts, "bitola_mm", 20.0);
                double pressao = getDouble(opts, "pressao_base_bar", 2.5);
                double maxVol = getDouble(opts, "max_volume_m3", 99999.999);
                double fatorAr = getDouble(opts, "fator_ar", 0.1);
                double chanceFalta = getDouble(opts, "chance_falta_agua", 0.0);
                int dtSim = getInt(opts, "delta_t_simulacao_ms", 100);
                int updMs = getInt(opts, "intervalo_update_display_ms", 500);
                int faltaTot = getInt(opts, "duracao_falta_total_ms", 5000);
                int passagemAr = getInt(opts, "duracao_passagem_ar_ms", 3000);
                facade.configSimulador(
                    bitola,
                    pressao,
                    maxVol,
                    fatorAr,
                    chanceFalta,
                    dtSim,
                    updMs,
                    faltaTot,
                    passagemAr
                    );
                break;

            case "create":
                break;

            case "stop":
                System.out.println("OK");
                break;

            case "modify":
                System.out.println("OK");
                break;

            case "images":
                // facade.setImageGenerationEnabled(UUID.fromString(args[1]), enabled);
                System.out.println("OK");
                break;

            default:
                System.err.println("Comando desconhecido");
        }
    }
}
