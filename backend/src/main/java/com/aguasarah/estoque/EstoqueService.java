package com.aguasarah.estoque;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.fornecedor.Fornecedor;
import com.aguasarah.fornecedor.FornecedorRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final InsumoRepository insumoRepository;
    private final MovimentacaoInsumoRepository movimentacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FornecedorRepository fornecedorRepository;

    public List<Insumo> listarInsumos() {
        return insumoRepository.findAll();
    }

    public Insumo criarInsumo(Insumo insumo) {
        insumo.setId(null);
        if (insumo.getQuantidadeAtual() == null) insumo.setQuantidadeAtual(0);
        return insumoRepository.save(insumo);
    }

    public Insumo buscarInsumo(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Insumo nao encontrado: " + id));
    }

    @Transactional
    public MovimentacaoInsumo registrarMovimentacao(MovimentacaoInsumoRequestDTO dto) {
        Insumo insumo = buscarInsumo(dto.insumoId());

        if (dto.tipo() == TipoMovimentacaoInsumo.SAIDA && insumo.getQuantidadeAtual() < dto.quantidade()) {
            throw new RegraNegocioException("Quantidade insuficiente em estoque para essa saida");
        }

        int novaQuantidade = dto.tipo() == TipoMovimentacaoInsumo.ENTRADA
                ? insumo.getQuantidadeAtual() + dto.quantidade()
                : insumo.getQuantidadeAtual() - dto.quantidade();
        insumo.setQuantidadeAtual(novaQuantidade);
        insumoRepository.save(insumo);

        Fornecedor fornecedor = dto.fornecedorId() != null
                ? fornecedorRepository.findById(dto.fornecedorId())
                        .orElseThrow(() -> new EntityNotFoundException("Fornecedor nao encontrado: " + dto.fornecedorId()))
                : null;

        MovimentacaoInsumo mov = MovimentacaoInsumo.builder()
                .insumo(insumo)
                .tipo(dto.tipo())
                .quantidade(dto.quantidade())
                .data(LocalDateTime.now())
                .usuario(usuarioLogado())
                .fornecedor(fornecedor)
                .observacao(dto.observacao())
                .build();
        return movimentacaoRepository.save(mov);
    }

    public List<MovimentacaoInsumo> historico(Long insumoId) {
        return movimentacaoRepository.findByInsumoIdOrderByDataDesc(insumoId);
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
