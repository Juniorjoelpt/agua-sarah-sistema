package com.aguasarah.frota;

import com.aguasarah.cliente.Cliente;
import com.aguasarah.cliente.ClienteRepository;
import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.despesa.CategoriaDespesa;
import com.aguasarah.despesa.Despesa;
import com.aguasarah.despesa.DespesaRepository;
import com.aguasarah.usuario.Usuario;
import com.aguasarah.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FrotaService {

    private final MotoristaRepository motoristaRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final ClienteRotaFixaRepository clienteRotaFixaRepository;
    private final ClienteRepository clienteRepository;
    private final CarregamentoRepository carregamentoRepository;
    private final PrestacaoContasRepository prestacaoContasRepository;
    private final DespesaRepository despesaRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Caminhao> listarCaminhoes() {
        return caminhaoRepository.findByAtivoTrue();
    }

    @Transactional
    public Caminhao criarCaminhao(CaminhaoRequestDTO dto) {
        Motorista motorista = Motorista.builder()
                .nome(dto.nomeMotorista())
                .telefone(dto.telefoneMotorista())
                .ativo(true)
                .build();
        motorista = motoristaRepository.save(motorista);

        Caminhao caminhao = Caminhao.builder()
                .placa(dto.placa())
                .motorista(motorista)
                .tipoRota(dto.tipoRota())
                .ativo(true)
                .build();
        caminhao = caminhaoRepository.save(caminhao);

        if (dto.tipoRota() == TipoRota.FIXA && dto.clienteIdsRotaFixa() != null) {
            for (Long clienteId : dto.clienteIdsRotaFixa()) {
                Cliente cliente = clienteRepository.findById(clienteId)
                        .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + clienteId));
                clienteRotaFixaRepository.save(ClienteRotaFixa.builder()
                        .caminhao(caminhao)
                        .cliente(cliente)
                        .build());
            }
        }

        return caminhao;
    }

    public Caminhao buscarCaminhao(Long id) {
        return caminhaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Caminhao nao encontrado: " + id));
    }

    @Transactional
    public Caminhao atualizarCaminhao(Long id, CaminhaoRequestDTO dto) {
        Caminhao caminhao = buscarCaminhao(id);
        caminhao.setPlaca(dto.placa());
        caminhao.setTipoRota(dto.tipoRota());

        Motorista motorista = caminhao.getMotorista();
        motorista.setNome(dto.nomeMotorista());
        motorista.setTelefone(dto.telefoneMotorista());
        motoristaRepository.save(motorista);

        caminhaoRepository.save(caminhao);

        // recria as associacoes de rota fixa do zero, conforme a lista enviada
        List<ClienteRotaFixa> atuais = clienteRotaFixaRepository.findByCaminhaoId(id);
        clienteRotaFixaRepository.deleteAll(atuais);
        if (dto.tipoRota() == TipoRota.FIXA && dto.clienteIdsRotaFixa() != null) {
            for (Long clienteId : dto.clienteIdsRotaFixa()) {
                Cliente cliente = clienteRepository.findById(clienteId)
                        .orElseThrow(() -> new EntityNotFoundException("Cliente nao encontrado: " + clienteId));
                clienteRotaFixaRepository.save(ClienteRotaFixa.builder()
                        .caminhao(caminhao)
                        .cliente(cliente)
                        .build());
            }
        }

        return caminhao;
    }

    public void inativarCaminhao(Long id) {
        Caminhao caminhao = buscarCaminhao(id);
        caminhao.setAtivo(false);
        caminhaoRepository.save(caminhao);
    }

    public List<Cliente> clientesDaRotaFixa(Long caminhaoId) {
        return clienteRotaFixaRepository.findByCaminhaoId(caminhaoId).stream()
                .map(ClienteRotaFixa::getCliente)
                .toList();
    }

    // Carregamento vale para qualquer caminhao, seja de rota fixa ou variavel -
    // os dois tipos carregam agua e precisam prestar contas no final.
    @Transactional
    public Carregamento abrirCarregamento(AbrirCarregamentoRequestDTO dto) {
        Caminhao caminhao = buscarCaminhao(dto.caminhaoId());

        BigDecimal valorCarregamento = dto.precoVenda().multiply(BigDecimal.valueOf(dto.quantidadeCarregada()));

        Carregamento carregamento = Carregamento.builder()
                .caminhao(caminhao)
                .dataCarregamento(LocalDate.now())
                .rota(dto.rota())
                .quantidadeCarregada(dto.quantidadeCarregada())
                .precoVenda(dto.precoVenda())
                .valorCarregamento(valorCarregamento)
                .status(StatusCarregamento.PENDENTE)
                .build();
        return carregamentoRepository.save(carregamento);
    }

    @Transactional
    public PrestacaoContas registrarPrestacaoContas(PrestacaoContasRequestDTO dto) {
        Carregamento carregamento = carregamentoRepository.findById(dto.carregamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Carregamento nao encontrado: " + dto.carregamentoId()));

        if (carregamento.getStatus() == StatusCarregamento.PRESTADO) {
            throw new RegraNegocioException("Este carregamento ja teve a prestacao de contas registrada");
        }

        int quantidadeVendida = carregamento.getQuantidadeCarregada() - dto.quantidadeAvaria() - dto.quantidadeDevolvida();
        if (quantidadeVendida < 0) {
            throw new RegraNegocioException("Avaria + devolvido não pode ser maior que a quantidade carregada");
        }

        BigDecimal precoVenda = carregamento.getPrecoVenda();
        BigDecimal valorAvaria = precoVenda.multiply(BigDecimal.valueOf(dto.quantidadeAvaria()));
        BigDecimal valorVendaEsperado = precoVenda.multiply(BigDecimal.valueOf(quantidadeVendida));
        BigDecimal totalRecebido = dto.valorRecebidoEspecie().add(dto.valorRecebidoPix());
        BigDecimal diferenca = valorVendaEsperado.subtract(totalRecebido);

        BigDecimal totalDespesas = despesaRepository.findByCarregamentoId(carregamento.getId()).stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucro = valorVendaEsperado.subtract(totalDespesas);

        PrestacaoContas prestacao = PrestacaoContas.builder()
                .carregamento(carregamento)
                .quantidadeAvaria(dto.quantidadeAvaria())
                .valorAvaria(valorAvaria)
                .quantidadeDevolvida(dto.quantidadeDevolvida())
                .quantidadeVendida(quantidadeVendida)
                .valorRecebidoEspecie(dto.valorRecebidoEspecie())
                .valorRecebidoPix(dto.valorRecebidoPix())
                .diferenca(diferenca)
                .totalDespesas(totalDespesas)
                .lucro(lucro)
                .dataPrestacao(LocalDateTime.now())
                .usuarioConferencia(usuarioLogado())
                .build();
        prestacao = prestacaoContasRepository.save(prestacao);

        carregamento.setStatus(StatusCarregamento.PRESTADO);
        carregamentoRepository.save(carregamento);

        return prestacao;
    }

    public List<Carregamento> historicoCarregamentos(Long caminhaoId) {
        return carregamentoRepository.findByCaminhaoIdOrderByDataCarregamentoDesc(caminhaoId);
    }

    @Transactional
    public Despesa registrarDespesaCarregamento(Long carregamentoId, DespesaCarregamentoRequestDTO dto) {
        Carregamento carregamento = carregamentoRepository.findById(carregamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Carregamento nao encontrado: " + carregamentoId));

        Despesa despesa = Despesa.builder()
                .descricao(dto.descricao())
                .categoria(CategoriaDespesa.FROTA)
                .valor(dto.valor())
                .data(LocalDate.now())
                .carregamento(carregamento)
                .usuario(usuarioLogado())
                .build();
        return despesaRepository.save(despesa);
    }

    public List<Despesa> listarDespesasCarregamento(Long carregamentoId) {
        return despesaRepository.findByCarregamentoId(carregamentoId);
    }

    public PrestacaoContas buscarPrestacaoPorCarregamento(Long carregamentoId) {
        return prestacaoContasRepository.findByCarregamentoId(carregamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Prestação de contas não encontrada para este carregamento"));
    }

    // historico consolidado, por data e opcionalmente filtrado por caminhao - usado na tela de Frota
    public List<PrestacaoContas> listarPrestacoes(LocalDate inicio, LocalDate fim, Long caminhaoId) {
        boolean temPeriodo = inicio != null && fim != null;
        LocalDateTime inicioDT = temPeriodo ? inicio.atStartOfDay() : null;
        LocalDateTime fimDT = temPeriodo ? fim.atTime(23, 59, 59) : null;

        if (caminhaoId != null && temPeriodo) {
            return prestacaoContasRepository.findByCarregamento_CaminhaoIdAndDataPrestacaoBetweenOrderByDataPrestacaoDesc(caminhaoId, inicioDT, fimDT);
        }
        if (caminhaoId != null) {
            return prestacaoContasRepository.findByCarregamento_CaminhaoIdOrderByDataPrestacaoDesc(caminhaoId);
        }
        if (temPeriodo) {
            return prestacaoContasRepository.findByDataPrestacaoBetweenOrderByDataPrestacaoDesc(inicioDT, fimDT);
        }
        return prestacaoContasRepository.findAllByOrderByDataPrestacaoDesc();
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
