package com.aguasarah.terceiros.despesa;

import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.caixa.CaixaTerceiroRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DespesaTerceiroService {

    private final DespesaTerceiroRepository despesaTerceiroRepository;
    private final CaixaTerceiroRepository caixaTerceiroRepository;
    private final UsuarioRepository usuarioRepository;

    public List<DespesaTerceiro> listar(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null) {
            return despesaTerceiroRepository.findByDataBetween(inicio, fim);
        }
        return despesaTerceiroRepository.findAll();
    }

    public DespesaTerceiro criar(DespesaTerceiroRequestDTO dto) {
        CaixaTerceiro caixa = null;
        if (dto.caixaTerceiroId() != null) {
            caixa = caixaTerceiroRepository.findById(dto.caixaTerceiroId())
                    .orElseThrow(() -> new EntityNotFoundException("Caixa de terceiros nao encontrado: " + dto.caixaTerceiroId()));
        }

        DespesaTerceiro despesa = DespesaTerceiro.builder()
                .descricao(dto.descricao())
                .categoria(dto.categoria())
                .valor(dto.valor())
                .data(dto.data())
                .caixaTerceiro(caixa)
                .usuario(usuarioLogado())
                .build();
        return despesaTerceiroRepository.save(despesa);
    }

    public DespesaTerceiro buscarPorId(Long id) {
        return despesaTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa de terceiros nao encontrada: " + id));
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
