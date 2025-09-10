package br.com.simulador.hidrometro.util; // Corrigi para "util" minúsculo, que é a convenção

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
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.INFO);

        try {
            FileHandler fileHandler = new FileHandler("simulador.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);

            Handler[] handlers = logger.getHandlers();
            if (handlers.length > 0 && handlers[0] instanceof ConsoleHandler) {
                logger.removeHandler(handlers[0]);
            }

        } catch (IOException e) {
            // Usamos um logger para registrar a falha na configuração do log.
            Logger.getLogger(LogManager.class.getName()).log(
                    Level.SEVERE,
                    "Falha crítica ao configurar o FileHandler. Os logs podem não ser salvos no arquivo.",
                    e
            );
        }
    }
}