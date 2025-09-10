package br.com.simulador.hidrometro.controller;

import br.com.simulador.hidrometro.config.Configuracao;
import br.com.simulador.hidrometro.model.Hidrometro;
import br.com.simulador.hidrometro.model.types.DirecaoFluxo;
import br.com.simulador.hidrometro.view.Display;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Controladora {
    private final Configuracao config;
    private final Hidrometro hidrometro;
    private final Display display;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    private static final Logger logger = Logger.getLogger(Controladora.class.getName());

    // --- NOVAS VARIÁVEIS PARA CONTROLAR O ESTADO ---
    private boolean emFaltaDeAgua = false;
    private int contadorTempoFaltaAgua = 0;

    public Controladora() {
        this.config = new Configuracao();
        this.hidrometro = new Hidrometro(
                config.getDouble("bitola_mm"),
                config.getDouble("max_volume_m3")
        );
        this.display = new Display();
    }

    public void iniciarSimulacao() {
        long deltaTSimulacaoMs = config.getInt("delta_t_simulacao_ms");
        long intervaloDisplayMs = config.getInt("intervalo_update_display_ms");

        executor.scheduleAtFixedRate(this::loopDeSimulacao, 0, deltaTSimulacaoMs, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::loopDeDisplay, 0, intervaloDisplayMs, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(config::verificarEAtualizar, 5, 5, TimeUnit.SECONDS);
    }

    private void loopDeSimulacao() {
        gerenciarEstadoDaAgua(); // Renomeei o método para refletir a nova lógica

        double deltaTSegundos = config.getInt("delta_t_simulacao_ms") / 1000.0;
        hidrometro.simularPasso(deltaTSegundos, config.getDouble("fator_ar"));
    }

    /**
     * Lógica aprimorada que simula a falta de água em estágios.
     */
    private void gerenciarEstadoDaAgua() {
        double chanceFaltaAgua = config.getDouble("chance_falta_agua");
        int duracaoFaltaTotalMs = config.getInt("duracao_falta_total_ms");
        int duracaoPassagemArMs = config.getInt("duracao_passagem_ar_ms");
        int passosFaltaTotal = duracaoFaltaTotalMs / config.getInt("delta_t_simulacao_ms");
        int passosPassagemAr = duracaoPassagemArMs / config.getInt("delta_t_simulacao_ms");

        // Se já estamos em um evento de falta de água, continua gerenciando ele
        if (emFaltaDeAgua) {
            contadorTempoFaltaAgua++;

            // Estágio 1: Falta total de água (pressão zero)
            if (contadorTempoFaltaAgua <= passosFaltaTotal) {
                hidrometro.setPressaoEntrada(0.0);
            }
            // Estágio 2: Retorno da água empurrando o ar (pressão baixa)
            else if (contadorTempoFaltaAgua <= passosFaltaTotal + passosPassagemAr) {
                hidrometro.setPressaoEntrada(0.05);
            }
            // Estágio 3: Fim do evento
            else {
                emFaltaDeAgua = false;
                contadorTempoFaltaAgua = 0;
                hidrometro.setPressaoEntrada(config.getDouble("pressao_base_bar"));
            }
        }
        // Se não estamos em falta de água, faz o sorteio para ver se um novo evento começa
        else if (Math.random() < chanceFaltaAgua) {
            emFaltaDeAgua = true; // Inicia um novo evento de falta de água
            System.out.println("--- INICIANDO EVENTO DE FALTA DE ÁGUA ---");
            hidrometro.setPressaoEntrada(0.0); // Começa imediatamente com pressão zero
        }
        // Se nada aconteceu, mantém a pressão normal
        else {
            hidrometro.setPressaoEntrada(config.getDouble("pressao_base_bar"));
            hidrometro.setDirecaoFluxo(DirecaoFluxo.DIRETO);
        }
    }

    private void loopDeDisplay() {
        var dadosAtuais = hidrometro.getDadosLeitura();

        logger.log(Level.INFO, String.format("ESTADO: Volume = %.4f m³ | Pressão = %.2f bar",
                dadosAtuais.volumeM3(),
                dadosAtuais.pressaoBar()));

        SwingUtilities.invokeLater(() -> display.atualizar(dadosAtuais));
    }
}