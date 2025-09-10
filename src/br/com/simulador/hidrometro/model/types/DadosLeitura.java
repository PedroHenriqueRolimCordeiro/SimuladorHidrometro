package br.com.simulador.hidrometro.model.types;


/**
 * Um DTO (Data Transfer Object) para transportar os dados do Hidrometro para o Display.
 * Utilizar record pois é uma forma mais eficiente para dados imutaveis.
 * @param volumeM3 O volume atual acumulado em metros cúbicos.
 * @param pressaoBar A pressão atual da água em bar.
 */
public record DadosLeitura(double volumeM3, double pressaoBar){

}
