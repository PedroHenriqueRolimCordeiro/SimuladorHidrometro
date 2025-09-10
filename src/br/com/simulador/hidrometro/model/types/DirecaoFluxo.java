package br.com.simulador.hidrometro.model.types;

/**
 * Enum para representar os estados possíveis da direção do fluxo de água.
 * Usar um enum torna o código mais seguro e legível do que usar números ou strings.
 */
public enum DirecaoFluxo {
    DIRETO,  // Fluxo normal, que deve ser contado.
    REVERSO, // Fluxo no sentido contrário, que não deve ser contado.
    NULO     // Sem fluxo.
}
