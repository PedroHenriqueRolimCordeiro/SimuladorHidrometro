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
    public Configuracao() {
        // O uso de um caminho absoluto construído dinamicamente garante que o recarregamento
        // funcione de forma consistente no ambiente de desenvolvimento.
        String projectPath = System.getProperty("user.dir");
        this.arquivoConfig = new File(projectPath, "resources/config.txt");

        if (!arquivoConfig.exists()) {
            logger.severe("ERRO CRÍTICO: Arquivo de configuração não encontrado em: " + arquivoConfig.getAbsolutePath());
            System.exit(1);
        }
        carregar();
    }

    /**
     * Carrega as propriedades do arquivo config.txt para a memória.
     * Este método também armazena o timestamp da última modificação do arquivo
     * para suportar o recarregamento dinâmico.
     */
    private void carregar() {
        try (InputStream input = new FileInputStream(this.arquivoConfig)) {
            propriedades.load(input);
            this.ultimaModificacao = arquivoConfig.lastModified();
            logger.info("Parâmetros de configuração foram carregados/recarregados com sucesso.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ocorreu um erro inesperado ao ler o arquivo de configuração.", e);
        }
    }

    /**
     * Verifica se o arquivo de configuração foi modificado desde a última leitura.
     * Se uma alteração for detectada, o método {@link #carregar()} é invocado para
     * atualizar os parâmetros em memória.
     */
    public void verificarEAtualizar() {
        long modificacaoAtual = arquivoConfig.lastModified();
        if (modificacaoAtual > this.ultimaModificacao) {
            logger.info("Alteração detectada no 'config.txt'. Recarregando parâmetros...");
            carregar();
        }
    }

    /**
     * Obtém um valor de configuração do tipo {@code double}.
     *
     * @param chave A chave da propriedade a ser buscada.
     * @return O valor da propriedade convertido para double. Retorna 0.0 se a chave não for encontrada.
     */
    public double getDouble(String chave) {
        return Double.parseDouble(propriedades.getProperty(chave, "0.0"));
    }

    /**
     * Obtém um valor de configuração do tipo {@code int}.
     *
     * @param chave A chave da propriedade a ser buscada.
     * @return O valor da propriedade convertido para int. Retorna 0 se a chave não for encontrada.
     */
    public int getInt(String chave) {
        return Integer.parseInt(propriedades.getProperty(chave, "0"));
    }
}