package com.aguasarah.precocliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrecoClienteRepository extends JpaRepository<PrecoCliente, Long> {
    List<PrecoCliente> findByClienteId(Long clienteId);
    Optional<PrecoCliente> findByClienteIdAndProdutoId(Long clienteId, Long produtoId);

    // @Modifying + @Query faz isso rodar como um DELETE de verdade, executado na
    // hora - sem isso (deleteByClienteId "magico" do Spring Data, so por nome de
    // metodo), o Hibernate so marca as linhas antigas pra remover e so executa o
    // DELETE no fim da transacao, DEPOIS dos INSERTs das linhas novas (ordem
    // padrao do Hibernate: insert antes de delete). Resultado: ao editar um preco
    // que ja existia pra aquele cliente+produto, o INSERT da linha nova batia de
    // frente com a linha antiga (que ainda nao tinha sido apagada) e violava a
    // constraint unica (cliente_id, produto_id) - "Duplicate entry".
    @Modifying
    @Query("delete from PrecoCliente p where p.cliente.id = :clienteId")
    void deleteByClienteId(@Param("clienteId") Long clienteId);
}
