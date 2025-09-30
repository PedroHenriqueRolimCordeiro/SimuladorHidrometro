package br.com.simulador.hidrometro.controller;

import br.com.simulador.hidrometro.config.Configuracao;
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
    private final Configuracao config;
    private final Hidrometro hidrometro;
    private final Display display;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
    private static final Logger logger = Logger.getLogger(Controladora.class.getName());

    private boolean emFaltaDeAgua = false;
    private int contadorTempoFaltaAgua = 0;

    private int ultimoMetroCubicoSalvo = -1;

    public Controladora(String caminhoConfiguracao) {
        this.config = new Configuracao(caminhoConfiguracao);
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
        DadosLeitura dadosAtuais = hidrometro.getDadosLeitura();

        logger.log(Level.INFO, String.format("ESTADO: Volume = %.4f m³ | Pressão = %.2f bar",
                dadosAtuais.volumeM3(),
                dadosAtuais.pressaoBar()));

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
                logger.log(Level.INFO, "Medição salva em: " + arquivoDeSaida.getAbsolutePath());

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Ocorreu um erro ao salvar a imagem da medição.", e);
            }
        }
    }
}