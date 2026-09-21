package com.aguasarah.common;

// Excecao para violacoes de regra de negocio (ex: tentar abrir caixa ja aberto,
// vender de um caixa fechado, etc). Vira HTTP 400 no GlobalExceptionHandler.
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
