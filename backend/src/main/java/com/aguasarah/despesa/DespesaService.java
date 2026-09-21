package com.aguasarah.despesa;

import com.aguasarah.caixa.Caixa;
import com.aguasarah.caixa.CaixaRepository;
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
public class DespesaService {

    private final DespesaRepository despesaRepository;
    private final CaixaRepository caixaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Despesa> listar(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null) {
            return despesaRepository.findByDataBetween(inicio, fim);
        }
        return despesaRepository.findAll();
    }

    public Despesa criar(Despesa despesa, Long caixaId) {
        despesa.setId(null);
        despesa.setUsuario(usuarioLogado());
        if (caixaId != null) {
            Caixa caixa = caixaRepository.findById(caixaId)
                    .orElseThrow(() -> new EntityNotFoundException("Caixa nao encontrado: " + caixaId));
            despesa.setCaixa(caixa);
        }
        return despesaRepository.save(despesa);
    }

    public Despesa buscarPorId(Long id) {
        return despesaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa nao encontrada: " + id));
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
