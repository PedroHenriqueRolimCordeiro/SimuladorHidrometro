package br.com.simulador.hidrometro.view;

import br.com.simulador.hidrometro.model.types.DadosLeitura;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerencia a interface gráfica (GUI) da simulação.
 * Sua única responsabilidade é carregar a imagem de fundo do hidrômetro
 * e desenhar os dados do volume sobre ela,atualizando a janela em tempo real.
 */
public class Display {

    // Logger adicionado para a classe
    private static final Logger logger = Logger.getLogger(Display.class.getName());

    private final JFrame frame;
    private final JLabel imageLabel;
    private BufferedImage imagemBase;

    // Variável para armazenar a imagem mais recente renderizada.
    private BufferedImage imagemAtualizada;

    /**
     * Constrói a janela do display, carregando os recursos gráficos e
     * inicializando os componentes do Swing.
     */
    public Display() {
        carregarImagemBase();
        frame = new JFrame("Simulador de Hidrômetro");
        imageLabel = new JLabel();
        inicializarComponentesGraficos();
    }

    /**
     * Retorna a imagem mais recente que foi renderizada no display.
     * @return A imagem atual como um objeto BufferedImage.
     */
    public BufferedImage getImagemAtual() {
        return imagemAtualizada;
    }

    /**
     * Carrega a imagem de fundo do hidrômetro a partir da pasta de recursos.
     * O programa é encerrado se a imagem não for encontrada.
     */
    private void carregarImagemBase() {
        try (InputStream stream = getClass().getResourceAsStream("/images/hidrometro_base.jpeg")) {
            if (stream == null) {
                // System.err.println substituído pela chamada de log
                logger.severe("ERRO CRÍTICO: Imagem 'hidrometro_base.jpeg' não encontrada na pasta 'resources/images'.");
                System.exit(1);
            }
            imagemBase = ImageIO.read(stream);
        } catch (Exception e) {
            // e.printStackTrace() substituído pela chamada de log
            logger.log(Level.SEVERE, "ERRO CRÍTICO: Falha ao ler o arquivo de imagem.", e);
            System.exit(1);
        }
    }

    /**
     * Configura e exibe a janela principal (JFrame) e seus componentes.
     */
    private void inicializarComponentesGraficos() {
        imageLabel.setIcon(new ImageIcon(imagemBase));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(imageLabel);
        frame.pack(); // Ajusta o tamanho da janela ao da imagem
        frame.setLocationRelativeTo(null); // Centraliza na tela
        frame.setVisible(true);
    }

    /**
     * Atualiza a tela com os novos dados da simulação.
     * Este método redesenha a imagem a cada chamada, sobrepondo os valores de
     * volume formatados sobre a imagem base do hidrômetro.
     *
     * @param dados O objeto {@link DadosLeitura} contendo o volume atual.
     */
    public void atualizar(DadosLeitura dados) {
        // Cria uma nova imagem em memória para desenhar o frame atual
        // e a armazena na variável de instância da classe.
        this.imagemAtualizada = new BufferedImage(
                imagemBase.getWidth(),
                imagemBase.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = this.imagemAtualizada.createGraphics();


        // Desenha a imagem de fundo e melhora a qualidade do texto
        g.drawImage(imagemBase, 0, 0, null);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Extrai e formata os dados de volume
        double volumeTotal = dados.volumeM3();
        int parteInteira = (int) volumeTotal;
        int centenasDeLitros = (int) ((volumeTotal * 10) % 10);
        int dezenasDeLitros = (int) ((volumeTotal * 100) % 10);

        g.setFont(new Font("Monospaced", Font.BOLD, 42));

        // Renderiza a parte inteira (preta) dígito a dígito para controle de espaçamento
        g.setColor(Color.BLACK);
        String parteInteiraTexto = String.format("%04d", parteInteira);
        int xInicialPreto = 415;
        int espacamentoPadrao = 35;
        for (int i = 0; i < parteInteiraTexto.length(); i++) {
            String digito = parteInteiraTexto.substring(i, i + 1);
            int posX = xInicialPreto + (i * espacamentoPadrao);
            if (i == 3) { //Ajuste manual para o 4° dígito Preto
                posX += 5;
            }
            g.drawString(digito, posX, 390);
        }

        // Renderiza a parte decimal (vermelha)
        g.setColor(Color.RED);
        String centenasTexto = String.valueOf(centenasDeLitros);
        String dezenasTexto = String.valueOf(dezenasDeLitros);
        int xInicialVermelho = 562;
        g.drawString(centenasTexto, xInicialVermelho, 390);
        g.drawString(dezenasTexto, xInicialVermelho + espacamentoPadrao, 390);

        // Libera os recursos gráficos e atualiza a imagem na tela
        g.dispose();
        imageLabel.setIcon(new ImageIcon(this.imagemAtualizada));
    }
}