package br.com.simulador.hidrometro;

import br.com.simulador.hidrometro.controller.Controladora;
import br.com.simulador.hidrometro.util.LogManager;

/**
 * Classe principal que serve como ponto de entrada para a aplicação
 * do Simulador de Hidrômetro.
 */
public class Main {
    /**
     * O método principal que inicializa e executa as simulações em threads.
     * @param args Argumentos de linha de comando (não utilizados nesta aplicação).
     */
    public static void main(String[] args) {
        // Configura o sistema de log para salvar em arquivo ANTES de tudo
        LogManager.setup(); //log fica em simulador.log

        for (int i = 1; i <= 5; i++) {
            final String configFile = "resources/config" + i + ".txt";

            Thread threadSimulacao = new Thread(() -> {
                Controladora controladora = new Controladora(configFile);
                controladora.iniciarSimulacao();
            });

            threadSimulacao.setName("SimuladorThread-" + i);
            threadSimulacao.start();
        }
    }
}