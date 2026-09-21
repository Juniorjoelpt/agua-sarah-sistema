package com.aguasarah;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class AguaSarahBackendApplication {
    public static void main(String[] args) {
        // O servidor (VPS) roda no fuso UTC por padrao - sem isso, todo
        // LocalDateTime.now() usado no sistema (data/hora de vendas, caixa,
        // contas a pagar/receber, etc.) grava 3 horas a frente do horario
        // real do Brasil. Forcando o fuso da JVM logo no inicio, antes do
        // Spring subir, tudo passa a usar o horario de Brasilia
        // independente da configuracao do sistema operacional do servidor.
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(AguaSarahBackendApplication.class, args);
    }
}
