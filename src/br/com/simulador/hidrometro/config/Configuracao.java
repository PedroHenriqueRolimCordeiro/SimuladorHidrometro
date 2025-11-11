package br.com.simulador.hidrometro.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerencia o carregamento e o acesso aos parâmetros de configuração da simulação.
 * Esta classe é responsável por ler o arquivo {@code config.txt}, localizado na pasta
 * de recursos do projeto. Além disso, implementa um mecanismo de recarregamento dinâmico
 * ("hot-reload"), permitindo que alterações no arquivo de configuração sejam aplicadas
 * em tempo real, sem a necessidade de reiniciar a aplicação.
 */
public class Configuracao {

    private static final Logger logger = Logger.getLogger(Configuracao.class.getName());

    private final Properties propriedades = new Properties();
    private final File arquivoConfig;
    private long ultimaModificacao;

    /**
     * Construtor da classe. Inicializa o caminho para o arquivo de configuração
     * e realiza a primeira carga dos parâmetros.
     */
    public Configuracao(String caminhoConfiguracao) {
        // O uso de um caminho absoluto construído dinamicamente garante que o recarregamento
        // funcione de forma consistente no ambiente de desenvolvimento.
        String projectPath = System.getProperty("user.dir");
        this.arquivoConfig = new File(projectPath, caminhoConfiguracao);

        if (!arquivoConfig.exists()) {
            logger.severe("ERRO CRÍTICO: Arquivo de configuração não encontrado em: " + arquivoConfig.getAbsolutePath());
            System.exit(1);
        }
    }
}