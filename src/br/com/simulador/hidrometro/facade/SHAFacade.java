package br.com.simulador.hidrometro.facade;

import java.util.ArrayList;
import java.util.List;

import br.com.simulador.hidrometro.controller.Controladora;

public class SHAFacade {
    private static volatile SHAFacade instance;
    private List<Controladora> controladoras = new ArrayList<Controladora>();
    private final List<Thread> threads = new ArrayList<>();
    private final ArrayList<Boolean> threadsStop = new ArrayList<>();

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

    private boolean testIndice(int id){
        if((controladoras.size() - 1) < id ){
            System.out.println("índice inválido");
            return false;
        }
        return true;
    }

    public int getSizeLista(){
        return controladoras.size();
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
        threadsStop.add(false);
        Controladora controladora = new Controladora(
            bitola_mm,
            pressao_base_bar,
            max_volume_m3,
            fator_ar,
            chance_falta_agua,
            delta_t_simulacao_ms,
            intervalo_update_display_ms,
            duracao_falta_total_ms,
            duracao_passagem_ar_ms,
            threadsStop,
            controladoras.size()
            );
        controladoras.add(controladora);
        threads.add(new Thread(controladora));
        System.out.println("hidrometro criado com índice " + (controladoras.size()-1));
    }


    public void createSha(int i) {
        if(testIndice(i)){
            System.out.println("iniciando SHA do índice " + i);
            threads.get(i).start();
        }
    }


    public void finalizeSha(int id) {
        if(testIndice(id)){
            System.out.println("encerrando SHA do índice " + id);
            threadsStop.set(id, true);
        }
    }

    public void updateHidrometro(int id, int vazao) {
        if(testIndice(id)){
            System.out.println("atualizando vazão do SHA " + id);
            controladoras.get(id).updateIntervalo(vazao);
        }
    }

    public void setImageGenerationEnabled(int id, int salvar) {
        if(testIndice(id)){
            System.out.println("habilitando geração de imagem do SHA " + id);
            controladoras.get(id).setSalvarImagem(salvar);
        }
    }
}
