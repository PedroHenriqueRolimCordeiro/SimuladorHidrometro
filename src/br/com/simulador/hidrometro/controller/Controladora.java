package br.com.simulador.hidrometro.controller;

import br.com.simulador.hidrometro.config.Configuracao;
import br.com.simulador.hidrometro.model.Hidrometro;
import br.com.simulador.hidrometro.model.types.DirecaoFluxo;
import br.com.simulador.hidrometro.view.Display;

import javax.swing.SwingUtilities;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orquestra a simulação do hidrômetro.
 * Esta classe atua como o controlador principal, conectando o modelo (Hidrometro),
 * a visualização (Display) e as configurações. Ela gerencia os loops de tempo
 * e as transições de estado da simulação.
 */
public class Controladora {

    /** Logger para esta classe. */
    private static final Logger logger = Logger.getLogger(Controladora.class.getName());

    private final Configuracao config;
    private final Hidrometro hidrometro;
    private final Display display;
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

    // Variáveis de estado para gerenciar eventos de falta de água.
    /** Sinaliza se um evento de falta de água está ativo. */
    private boolean emFaltaDeAgua = false;
    /** Rastreia a duração (em passos de simulação) do evento atual de falta de água. */
    private int contadorTempoFaltaAgua = 0;

    /**
     * Constrói o controlador, inicializando os componentes principais da simulação.
     */
    public Controladora() {
        this.config = new Configuracao();
        this.hidrometro = new Hidrometro(
                config.getDouble("bitola_mm"),
                config.getDouble("max_volume_m3")
        );
        this.display = new Display();
    }

    /**
     * Inicia os loops principais da simulação.
     * Agenda três tarefas centrais para execução em intervalos fixos:
     * 1. O passo de simulação da física do hidrômetro.
     * 2. A atualização da interface gráfica (Display).
     * 3. A verificação e recarga dinâmica das configurações.
     */
    public void iniciarSimulacao() {
        long deltaTSimulacaoMs = config.getInt("delta_t_simulacao_ms");
        long intervaloDisplayMs = config.getInt("intervalo_update_display_ms");

        executor.scheduleAtFixedRate(this::loopDeSimulacao, 0, deltaTSimulacaoMs, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::loopDeDisplay, 0, intervaloDisplayMs, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(config::verificarEAtualizar, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Executa um único passo da lógica de simulação.
     * Envolve o gerenciamento do estado da água e o avanço da simulação do hidrômetro.
     */
    private void loopDeSimulacao() {
        gerenciarEstadoDaAgua();

        double deltaTSegundos = config.getInt("delta_t_simulacao_ms") / 1000.0;
        hidrometro.simularPasso(deltaTSegundos, config.getDouble("fator_ar"));
    }

    /**
     * Gerencia a simulação de um evento de falta de água.
     * Este método cria um evento em múltiplos estágios, baseado em probabilidades da configuração.
     * Os estágios são:
     * 1. Falta Total: A pressão da água cai para zero.
     * 2. Passagem de Ar: A água retorna, empurrando o ar preso através do hidrômetro em baixa pressão.
     * 3. Normalização: A pressão retorna ao normal e o evento é concluído.
     */
    private void gerenciarEstadoDaAgua() {
        double chanceFaltaAgua = config.getDouble("chance_falta_agua");
        int duracaoFaltaTotalMs = config.getInt("duracao_falta_total_ms");
        int duracaoPassagemArMs = config.getInt("duracao_passagem_ar_ms");
        int passosFaltaTotal = duracaoFaltaTotalMs / config.getInt("delta_t_simulacao_ms");
        int passosPassagemAr = duracaoPassagemArMs / config.getInt("delta_t_simulacao_ms");

        // Se um evento de falta de água já está em andamento, continua gerenciando seu estado.
        if (emFaltaDeAgua) {
            contadorTempoFaltaAgua++;

            // Estágio 1: Falta total de água (pressão zero).
            if (contadorTempoFaltaAgua <= passosFaltaTotal) {
                hidrometro.setPressaoEntrada(0.0);
            }
            // Estágio 2: Retorno da água empurrando o ar (baixa pressão).
            else if (contadorTempoFaltaAgua <= passosFaltaTotal + passosPassagemAr) {
                hidrometro.setPressaoEntrada(0.05);
            }
            // Estágio 3: Fim do evento.
            else {
                emFaltaDeAgua = false;
                contadorTempoFaltaAgua = 0;
                hidrometro.setPressaoEntrada(config.getDouble("pressao_base_bar"));
            }
        }
        // Se não há um evento ativo, sorteia a chance de um novo começar.
        else if (Math.random() < chanceFaltaAgua) {
            emFaltaDeAgua = true;
            logger.log(Level.WARNING, "--- INICIANDO EVENTO DE FALTA DE ÁGUA ---");
            hidrometro.setPressaoEntrada(0.0); // Começa imediatamente com pressão zero.
        }
        // Caso contrário, mantém a pressão da rede normal.
        else {
            hidrometro.setPressaoEntrada(config.getDouble("pressao_base_bar"));
            hidrometro.setDirecaoFluxo(DirecaoFluxo.DIRETO);
        }
    }

    /**
     * Busca os dados de leitura mais recentes do hidrômetro e agenda a atualização da interface gráfica.
     * A atualização é feita na Event Dispatch Thread (EDT) do Swing para garantir a segurança da thread.
     */
    private void loopDeDisplay() {
        var dadosAtuais = hidrometro.getDadosLeitura();

        // Registra o estado atual da simulação no log.
        logger.log(Level.INFO, String.format("ESTADO: Volume = %.4f m³ | Pressão = %.2f bar",
                dadosAtuais.volumeM3(),
                dadosAtuais.pressaoBar()));

        SwingUtilities.invokeLater(() -> display.atualizar(dadosAtuais));
    }
}