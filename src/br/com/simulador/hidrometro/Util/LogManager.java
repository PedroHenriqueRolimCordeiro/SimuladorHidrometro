package br.com.simulador.hidrometro.util;

import java.io.IOException;
import java.util.logging.*;

/**
 * Gerencia a configuração do sistema de logging para a aplicação.
 * Configura um FileHandler para registrar os logs da simulação em um arquivo.
 */
public class LogManager {

    /**
     * Configura o logger principal para escrever em um arquivo chamado "simulador.log".
     * O arquivo será criado na pasta raiz do projeto.
     */
    public static void setup() {
        // Obtém o logger raiz
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.INFO); // Define o nível mínimo de log a ser registrado

        try {
            // Cria um FileHandler que escreve no arquivo "simulador.log"
            // O "true" no final indica que o log deve ser acrescentado ao arquivo existente (append)
            FileHandler fileHandler = new FileHandler("simulador.log", true);

            // Define um formato de log simples e limpo
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // Adiciona o nosso handler de arquivo ao logger
            logger.addHandler(fileHandler);

            // Remove o handler do console para não imprimir mais no terminal
            Handler[] handlers = logger.getHandlers();
            if (handlers.length > 0 && handlers[0] instanceof ConsoleHandler) {
                logger.removeHandler(handlers[0]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}