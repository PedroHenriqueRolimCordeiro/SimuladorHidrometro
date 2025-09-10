package br.com.simulador.hidrometro.model;

import br.com.simulador.hidrometro.model.types.DirecaoFluxo;

/**
 * Modela a física do fluxo de entrada de fluido no hidrômetro.
 * Esta classe é responsável por calcular a vazão (m³/s) com base nas
 * características físicas da conexão, como o diâmetro (bitola) e a pressão.
 * Também gerencia a direção do fluxo e detecta condições de passagem de ar.
 */
public class ConexaoEntrada {

    private final double bitolaMm;
    private double pressaoAtualBar;
    private DirecaoFluxo direcao;

    /**
     * Constrói uma nova instância de ConexaoEntrada.
     * @param bitolaMm O diâmetro (bitola) da conexão em milímetros.
     */
    public ConexaoEntrada(double bitolaMm) {
        this.bitolaMm = bitolaMm;
        this.pressaoAtualBar = 0.0;
        this.direcao = DirecaoFluxo.NULO;
    }

    /**
     * Calcula a vazão de água em metros cúbicos por segundo (m³/s).
     * A fórmula utilizada é uma simplificação para fins de simulação, onde a vazão é
     * proporcional ao quadrado da bitola e à raiz quadrada da pressão.
     * @return A vazão calculada em m³/s. Retorna 0.0 se a pressão for nula ou se o fluxo for reverso.
     */
    public double getVazaoAtualM3s() {
        if (pressaoAtualBar <= 0 || direcao != DirecaoFluxo.DIRETO) {
            return 0.0;
        }
        // 'k' é uma constante de fluxo simplificada que engloba fatores como
        // viscosidade, atrito e outras complexidades da mecânica dos fluidos.
        final double k = 0.0001;
        return k * Math.pow(bitolaMm, 2) * Math.sqrt(pressaoAtualBar);
    }

    /**
     * Verifica se as condições atuais indicam a passagem de ar pela tubulação.
     * Na simulação, considera-se que há passagem de ar quando a pressão é positiva,
     * mas muito baixa para representar um fluxo normal de água.
     * @return {@code true} se as condições para passagem de ar forem atendidas, {@code false} caso contrário.
     */
    public boolean isPassandoAr() {
        return pressaoAtualBar > 0 && pressaoAtualBar < 0.1;
    }

    /**
     * Define a pressão atual na conexão. Utilizado pela Controladora para simular eventos.
     * @param pressaoAtualBar O novo valor de pressão em bar.
     */
    public void setPressaoAtualBar(double pressaoAtualBar) {
        this.pressaoAtualBar = pressaoAtualBar;
    }

    /**
     * Define a direção do fluxo na conexão.
     * @param direcao A nova direção do fluxo.
     */
    public void setDirecao(DirecaoFluxo direcao) {
        this.direcao = direcao;
    }

    /**
     * Retorna a pressão atual da conexão.
     * @return O valor da pressão atual em bar.
     */
    public double getPressaoAtualBar() {
        return pressaoAtualBar;
    }
}