package br.com.simulador.hidrometro.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import br.com.simulador.hidrometro.controller.Controladora;

public class SHAFacade {
    private static volatile SHAFacade instance;
    private List<Controladora> controladoras = new ArrayList<Controladora>();

    public static SHAFacade getInstance() {
        if (instance == null) {
            synchronized (SHAFacade.class) {
                if (instance == null) {
                    instance = new SHAFacade();
                }
            }
        }
        return instance;
    }

    public void configSimulador(
        double bitola_mm,
        double pressao_base_bar,
        double max_volume_m3,
        double fator_ar,
        double chance_falta_agua,
        int delta_t_simulacao_ms,
        int intervalo_update_display_ms,
        int duracao_falta_total_ms,
        int duracao_passagem_ar_ms
    ) {
        Controladora controladora = new Controladora(
            bitola_mm,
            pressao_base_bar,
            max_volume_m3,
            fator_ar,
            chance_falta_agua,
            delta_t_simulacao_ms,
            intervalo_update_display_ms,
            duracao_falta_total_ms,
            duracao_passagem_ar_ms
            );
        // controladora.iniciarSimulacao();
        controladoras.add(controladora);
        System.out.println("hidrometro criado com índice " + (controladoras.size()-1));
    }


    // public int createSha() {
    //     Controladora controller = new Controladora();
    //     return controller.getId();
    // }

    // public void finalizeSha(UUID id) {
    //     ShaInstance inst = instances.remove(id);
    //     if (inst != null) {
    //         inst.stop();
    //     }
    // }

    // public void updateFlowRate(UUID id, double value) {
    //     ShaInstance inst = instances.get(id);
    //     if (inst != null) {
    //         inst.setFlowRate(value);
    //     }
    // }

    // public void setImageGenerationEnabled(UUID id, boolean enabled) {
    //     ShaInstance inst = instances.get(id);
    //     if (inst != null) {
    //         inst.setImageGenerationEnabled(enabled);
    //     }
    // }
}
