package br.com.simulador.hidrometro.model;

/**
 * Representa o relógio numérico do hidrômetro.
 * Sua responsabilidade é acumular o volume de fluido que passa e gerenciar
 * o "rollover" (quando o valor máximo é atingido e o contador zera).
 */
public class Contador {

    private double volumeAcumuladoM3;
    private final double volumeMaximoM3;

    /**
     * Constrói uma nova instância do Contador.
     * @param volumeMaximoM3 O volume máximo que o contador pode registrar antes de zerar.
     */
    public Contador(double volumeMaximoM3) {
        this.volumeAcumuladoM3 = 0.0;
        this.volumeMaximoM3 = volumeMaximoM3;
    }

    /**
     * Adiciona um novo volume ao total acumulado.
     * Este método ignora valores negativos (fluxo reverso) e aplica a lógica de
     * rollover (zerar o contador) se o volume máximo for excedido.
     * @param volumeM3 O volume a ser adicionado, em metros cúbicos.
     */
    public void registrarVolume(double volumeM3) {
        // Apenas volumes positivos são registrados para ignorar o fluxo reverso.
        if (volumeM3 > 0) {
            this.volumeAcumuladoM3 += volumeM3;

            // Se o volume acumulado exceder o máximo, aplica-se o rollover.
            if (this.volumeAcumuladoM3 >= this.volumeMaximoM3) {
                this.volumeAcumuladoM3 %= this.volumeMaximoM3;
            }
        }
    }

    /**
     * Retorna o volume total atualmente registrado pelo contador.
     * @return O valor do volume acumulado em metros cúbicos.
     */
    public double getVolumeAtual() {
        return this.volumeAcumuladoM3;
    }
}