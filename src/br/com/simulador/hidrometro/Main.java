package br.com.simulador.hidrometro;

import br.com.simulador.hidrometro.controller.Controladora;
import br.com.simulador.hidrometro.util.LogManager;
/**
 * Classe principal que serve como ponto de entrada para a aplicação
 * do Simulador de Hidrômetro.
 */
public class Main {
    /**
     * O método principal que inicializa e executa a simulação
     * @param args Argumentos de linha de comando (não utilizados nesta aplicação).
     */
    public static void main(String[] args) {
        // Configura o sistema de log para salvar em arquivo ANTES de tudo
        LogManager.setup(); //log fica em simulador.log

        // Cria a instância da Controladora, que é a classe central que gerencia
        // o modelo, a visão e os loops da simulação.
        Controladora controladora = new Controladora();

        // Inicia os loops de simulação e de atualização da interface gráfica.
        controladora.iniciarSimulacao();

        System.out.println("Simulador de Hidrômetro iniciado com sucesso.");
    }
}