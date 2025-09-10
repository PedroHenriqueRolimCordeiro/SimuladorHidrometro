package br.com.simulador.hidrometro.model;

import br.com.simulador.hidrometro.model.types.DadosLeitura;
import br.com.simulador.hidrometro.model.types.DirecaoFluxo;

/**
 * Representa o hidrômetro, orquestrando seus componentes internos para simular a medição.
 * Utiliza o princípio de Composição para agregar a lógica da {@link ConexaoEntrada}
 * e do {@link Contador}, atuando como a classe central do modelo da simulação.
 */
public class Hidrometro {

    private final ConexaoEntrada entrada;
    private final Contador contador;

    /**
     * Constrói uma nova instância de Hidrometro.
     * @param bitolaMm O diâmetro da conexão de entrada em milímetros.
     * @param volumeMaximoContador O volume máximo do contador antes de zerar (rollover).
     */
    public Hidrometro(double bitolaMm, double volumeMaximoContador) {
        this.entrada = new ConexaoEntrada(bitolaMm);
        this.contador = new Contador(volumeMaximoContador);
    }

    /**
     * Executa um único passo de cálculo da simulação.
     * Calcula o volume do fluido (água ou ar) que passou no intervalo de tempo
     * e o registra no contador.
     * @param deltaTSegundos O intervalo de tempo do passo, em segundos.
     * @param fatorAr O fator de multiplicação aplicado na contagem de ar.
     */
    public void simularPasso(double deltaTSegundos, double fatorAr) {
        double volumeNestePasso = entrada.getVazaoAtualM3s() * deltaTSegundos;

        // Se a passagem de ar for detectada, o volume é recalculado com base em uma vazão de ar simulada.
        if (entrada.isPassandoAr()) {
            final double vazaoArEquivalente = 0.001; // Vazão constante para o ar em m³/s
            volumeNestePasso = (vazaoArEquivalente * deltaTSegundos) * fatorAr;
        }

        contador.registrarVolume(volumeNestePasso);
    }

    /**
     * Retorna os dados de leitura atuais do hidrômetro.
     * @return um objeto {@link DadosLeitura} com o volume e a pressão atuais.
     */
    public DadosLeitura getDadosLeitura() {
        return new DadosLeitura(contador.getVolumeAtual(), entrada.getPressaoAtualBar());
    }

    /**
     * Define a pressão na conexão de entrada. Usado pela Controladora para simular eventos.
     * @param pressao O novo valor de pressão em bar.
     */
    public void setPressaoEntrada(double pressao) {
        this.entrada.setPressaoAtualBar(pressao);
    }

    /**
     * Define a direção do fluxo na conexão de entrada.
     * @param direcao A nova direção do fluxo.
     */
    public void setDirecaoFluxo(DirecaoFluxo direcao) {
        this.entrada.setDirecao(direcao);
    }
}