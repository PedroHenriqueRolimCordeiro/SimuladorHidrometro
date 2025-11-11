package br.com.simulador.hidrometro.controller;

import br.com.simulador.hidrometro.model.Hidrometro;
import br.com.simulador.hidrometro.model.types.DadosLeitura;
import br.com.simulador.hidrometro.model.types.DirecaoFluxo;
import br.com.simulador.hidrometro.view.Display;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Controladora {
    private final Hidrometro hidrometro;
    private final Display display;
    private final double bitola_mm;
    private final double pressao_base_bar;
    private final double max_volume_m3;
    private final double fator_ar;
    private final double chance_falta_agua;
    private final int delta_t_simulacao_ms;
    private final int intervalo_update_display_ms;
    private final int duracao_falta_total_ms;
    private final int duracao_passagem_ar_ms;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    private static final Logger logger = Logger.getLogger(Controladora.class.getName());

    private boolean emFaltaDeAgua = false;
    private int contadorTempoFaltaAgua = 0;

    private int ultimoMetroCubicoSalvo = -1;

    public Controladora(
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
        this.bitola_mm = bitola_mm;
        this.pressao_base_bar = pressao_base_bar;
        this.max_volume_m3 = max_volume_m3;
        this.fator_ar = fator_ar;
        this.chance_falta_agua = chance_falta_agua;
        this.delta_t_simulacao_ms = delta_t_simulacao_ms;
        this.intervalo_update_display_ms = intervalo_update_display_ms;
        this.duracao_falta_total_ms = duracao_falta_total_ms;
        this.duracao_passagem_ar_ms = duracao_passagem_ar_ms;
        this.hidrometro = new Hidrometro(bitola_mm, max_volume_m3);
        this.display = new Display();
    }

    public void iniciarSimulacao() {

        executor.scheduleAtFixedRate(this::loopDeSimulacao, 0, delta_t_simulacao_ms, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::loopDeDisplay, 0, intervalo_update_display_ms, TimeUnit.MILLISECONDS);
        //executor.scheduleAtFixedRate(config::verificarEAtualizar, 5, 5, TimeUnit.SECONDS);
    }

    private void loopDeSimulacao() {
        gerenciarEstadoDaAgua(); // Renomeei o método para refletir a nova lógica

        double deltaTSegundos = delta_t_simulacao_ms / 1000.0;
        hidrometro.simularPasso(deltaTSegundos, fator_ar);
    }

    /**
     * Lógica aprimorada que simula a falta de água em estágios.
     */
    private void gerenciarEstadoDaAgua() {
        double chanceFaltaAgua = chance_falta_agua;
        int duracaoFaltaTotalMs = duracao_falta_total_ms;
        int duracaoPassagemArMs = duracao_passagem_ar_ms;
        int passosFaltaTotal = duracaoFaltaTotalMs / delta_t_simulacao_ms;
        int passosPassagemAr = duracaoPassagemArMs / delta_t_simulacao_ms;

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
                hidrometro.setPressaoEntrada(pressao_base_bar);
            }
        }
        // Se não estamos em falta de água, faz o sorteio para ver se um novo evento começa
        else if (Math.random() < chanceFaltaAgua) {
            emFaltaDeAgua = true; // Inicia um novo evento de falta de água
            hidrometro.setPressaoEntrada(0.0); // Começa imediatamente com pressão zero
        }
        // Se nada aconteceu, mantém a pressão normal
        else {
            hidrometro.setPressaoEntrada(pressao_base_bar);
            hidrometro.setDirecaoFluxo(DirecaoFluxo.DIRETO);
        }
    }

    private void loopDeDisplay() {
        DadosLeitura dadosAtuais = hidrometro.getDadosLeitura();
        // A lógica de salvar a imagem agora está dentro do 'invokeLater' para garantir
        // que ela seja executada somente APÓS a atualização da imagem no display.
        SwingUtilities.invokeLater(() -> {
            display.atualizar(dadosAtuais);
            verificarESalvarImagem(dadosAtuais.volumeM3());
        });
    }

    /**
     * Verifica se a parte inteira do volume foi alterada (novo m³ completado)
     * e, em caso afirmativo, salva a imagem atual do hidrômetro em um arquivo.
     * @param volumeAtualM3 O volume atual medido pelo hidrômetro.
     */
    private void verificarESalvarImagem(double volumeAtualM3) {
        int metroCubicoAtual = (int) volumeAtualM3;

        // Condição: O m³ atual é maior que zero e é diferente do último que foi salvo.
        if (metroCubicoAtual > 0 && metroCubicoAtual != this.ultimoMetroCubicoSalvo) {
            this.ultimoMetroCubicoSalvo = metroCubicoAtual;

            BufferedImage imagemParaSalvar = display.getImagemAtual();

            if (imagemParaSalvar == null) {
                return;
            }

            try {
                // A matrícula agora é uma variável local, pois só é usada neste método.
                // IMPORTANTE: Altere o valor abaixo para a sua matrícula SUAP.
                final String matriculaSUAP = "202311250023";

                File diretorio = new File("Medicoes_" + matriculaSUAP);

                // Verifica se o diretório não existe E se a criação falhou.
                if (!diretorio.exists() && !diretorio.mkdirs()) {
                    logger.log(Level.SEVERE, "Falha ao criar o diretório para salvar a medição: " + diretorio.getAbsolutePath());
                    return; // Aborta a operação de salvamento se o diretório não pôde ser criado.
                }

                int numeroArquivo = ((metroCubicoAtual - 1) % 99) + 1;
                String nomeArquivo = String.format("%02d.jpeg", numeroArquivo);
                File arquivoDeSaida = new File(diretorio, nomeArquivo);

                ImageIO.write(imagemParaSalvar, "jpeg", arquivoDeSaida);

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Ocorreu um erro ao salvar a imagem da medição.", e);
            }
        }
    }
}