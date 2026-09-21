package com.aguasarah.terceiros.frota;

import com.aguasarah.common.RegraNegocioException;
import com.aguasarah.terceiros.caixa.CaixaTerceiro;
import com.aguasarah.terceiros.caixa.CaixaTerceiroRepository;
import com.aguasarah.terceiros.caixa.StatusCaixaTerceiro;
import com.aguasarah.terceiros.cliente.ClienteTerceiro;
import com.aguasarah.terceiros.cliente.ClienteTerceiroRepository;
import com.aguasarah.terceiros.despesa.CategoriaDespesaTerceiro;
import com.aguasarah.terceiros.despesa.DespesaTerceiro;
import com.aguasarah.terceiros.despesa.DespesaTerceiroRepository;
import com.aguasarah.terceiros.produto.ProdutoTerceiro;
import com.aguasarah.terceiros.produto.ProdutoTerceiroRepository;
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
public class FrotaTerceiroService {

    private final CaminhaoTerceiroRepository caminhaoTerceiroRepository;
    private final ClienteTerceiroRepository clienteTerceiroRepository;
    private final ProdutoTerceiroRepository produtoTerceiroRepository;
    private final CarregamentoTerceiroRepository carregamentoTerceiroRepository;
    private final PrestacaoContasTerceiroRepository prestacaoContasTerceiroRepository;
    private final DespesaTerceiroRepository despesaTerceiroRepository;
    private final CaixaTerceiroRepository caixaTerceiroRepository;
    private final UsuarioRepository usuarioRepository;

    /* ---------- caminhoes ---------- */

    public List<CaminhaoTerceiro> listarCaminhoes() {
        return caminhaoTerceiroRepository.findByAtivoTrue();
    }

    public CaminhaoTerceiro criarCaminhao(CaminhaoTerceiroRequestDTO dto) {
        ClienteTerceiro cliente = clienteTerceiroRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente terceiro nao encontrado: " + dto.clienteId()));
        CaminhaoTerceiro caminhao = CaminhaoTerceiro.builder()
                .cliente(cliente)
                .placa(dto.placa())
                .motorista(dto.motorista())
                .ativo(true)
                .build();
        return caminhaoTerceiroRepository.save(caminhao);
    }

    public CaminhaoTerceiro buscarCaminhao(Long id) {
        return caminhaoTerceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Caminhao terceiro nao encontrado: " + id));
    }

    public CaminhaoTerceiro atualizarCaminhao(Long id, CaminhaoTerceiroRequestDTO dto) {
        ClienteTerceiro cliente = clienteTerceiroRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente terceiro nao encontrado: " + dto.clienteId()));
        CaminhaoTerceiro caminhao = buscarCaminhao(id);
        caminhao.setCliente(cliente);
        caminhao.setPlaca(dto.placa());
        caminhao.setMotorista(dto.motorista());
        caminhao.setAtivo(dto.ativo());
        return caminhaoTerceiroRepository.save(caminhao);
    }

    public void inativarCaminhao(Long id) {
        CaminhaoTerceiro caminhao = buscarCaminhao(id);
        caminhao.setAtivo(false);
        caminhaoTerceiroRepository.save(caminhao);
    }

    /* ---------- carregamento (debita o caixa de terceiros na hora) ---------- */

    @Transactional
    public CarregamentoTerceiro abrirCarregamento(AbrirCarregamentoTerceiroRequestDTO dto) {
        CaminhaoTerceiro caminhao = buscarCaminhao(dto.caminhaoId());
        ProdutoTerceiro produto = produtoTerceiroRepository.findById(dto.produtoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto terceiro nao encontrado: " + dto.produtoId()));

        CaixaTerceiro caixaAberto = caixaTerceiroRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixaTerceiro.ABERTO)
                .orElseThrow(() -> new RegraNegocioException("Nenhum caixa de terceiros aberto - abra o caixa antes de lançar um carregamento"));

        BigDecimal valorCarregamento = dto.precoVenda().multiply(BigDecimal.valueOf(dto.quantidadeCarregada()));

        CarregamentoTerceiro carregamento = CarregamentoTerceiro.builder()
                .caminhao(caminhao)
                .produto(produto)
                .caixaTerceiro(caixaAberto)
                .dataCarregamento(LocalDate.now())
                .rota(dto.rota())
                .quantidadeCarregada(dto.quantidadeCarregada())
                .precoVenda(dto.precoVenda())
                .valorCarregamento(valorCarregamento)
                .status(StatusCarregamentoTerceiro.PENDENTE)
                .build();
        carregamento = carregamentoTerceiroRepository.save(carregamento);

        // contabilizado como se o cliente tivesse comprado da Agua Sarah na hora -
        // debita o caixa de terceiros imediatamente (via uma despesa)
        DespesaTerceiro debito = DespesaTerceiro.builder()
                .descricao("Carregamento " + caminhao.getPlaca() + " - " + produto.getNome() + " - " + dto.rota())
                .categoria(CategoriaDespesaTerceiro.FROTA)
                .valor(valorCarregamento)
                .data(LocalDate.now())
                .caixaTerceiro(caixaAberto)
                .usuario(usuarioLogado())
                .build();
        despesaTerceiroRepository.save(debito);

        return carregamento;
    }

    @Transactional
    public DespesaTerceiro registrarDespesaCarregamento(Long carregamentoId, DespesaCarregamentoTerceiroRequestDTO dto) {
        CarregamentoTerceiro carregamento = carregamentoTerceiroRepository.findById(carregamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Carregamento de terceiros nao encontrado: " + carregamentoId));

        DespesaTerceiro despesa = DespesaTerceiro.builder()
                .descricao(dto.descricao())
                .categoria(CategoriaDespesaTerceiro.FROTA)
                .valor(dto.valor())
                .data(LocalDate.now())
                .carregamento(carregamento)
                .usuario(usuarioLogado())
                .build();
        return despesaTerceiroRepository.save(despesa);
    }

    public List<DespesaTerceiro> listarDespesasCarregamento(Long carregamentoId) {
        return despesaTerceiroRepository.findByCarregamentoId(carregamentoId);
    }

    public List<CarregamentoTerceiro> historicoCarregamentos(Long caminhaoId) {
        return carregamentoTerceiroRepository.findByCaminhaoIdOrderByDataCarregamentoDesc(caminhaoId);
    }

    /* ---------- prestacao de contas (credita o lucro no caixa de terceiros) ---------- */

    @Transactional
    public PrestacaoContasTerceiro registrarPrestacaoContas(PrestacaoContasTerceiroRequestDTO dto) {
        CarregamentoTerceiro carregamento = carregamentoTerceiroRepository.findById(dto.carregamentoId())
                .orElseThrow(() -> new EntityNotFoundException("Carregamento de terceiros nao encontrado: " + dto.carregamentoId()));

        if (carregamento.getStatus() == StatusCarregamentoTerceiro.PRESTADO) {
            throw new RegraNegocioException("Este carregamento ja teve a prestacao de contas registrada");
        }

        int quantidadeVendida = carregamento.getQuantidadeCarregada() - dto.quantidadeAvaria() - dto.quantidadeDevolvida();
        if (quantidadeVendida < 0) {
            throw new RegraNegocioException("Avaria + devolvido não pode ser maior que a quantidade carregada");
        }

        CaixaTerceiro caixaAberto = caixaTerceiroRepository.findFirstByStatusOrderByDataAberturaDesc(StatusCaixaTerceiro.ABERTO)
                .orElseThrow(() -> new RegraNegocioException("Nenhum caixa de terceiros aberto - abra o caixa para registrar a prestação de contas"));

        BigDecimal precoVenda = carregamento.getPrecoVenda();
        BigDecimal valorAvaria = precoVenda.multiply(BigDecimal.valueOf(dto.quantidadeAvaria()));
        BigDecimal valorVendaEsperado = precoVenda.multiply(BigDecimal.valueOf(quantidadeVendida));

        BigDecimal totalDespesas = despesaTerceiroRepository.findByCarregamentoId(carregamento.getId()).stream()
                .map(DespesaTerceiro::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucro = valorVendaEsperado.subtract(totalDespesas);

        PrestacaoContasTerceiro prestacao = PrestacaoContasTerceiro.builder()
                .carregamento(carregamento)
                .caixaTerceiro(caixaAberto)
                .quantidadeAvaria(dto.quantidadeAvaria())
                .valorAvaria(valorAvaria)
                .quantidadeDevolvida(dto.quantidadeDevolvida())
                .quantidadeVendida(quantidadeVendida)
                .totalDespesas(totalDespesas)
                .lucro(lucro)
                .dataPrestacao(LocalDateTime.now())
                .usuarioConferencia(usuarioLogado())
                .build();
        prestacao = prestacaoContasTerceiroRepository.save(prestacao);

        carregamento.setStatus(StatusCarregamentoTerceiro.PRESTADO);
        carregamentoTerceiroRepository.save(carregamento);

        return prestacao;
    }

    public PrestacaoContasTerceiro buscarPrestacaoPorCarregamento(Long carregamentoId) {
        return prestacaoContasTerceiroRepository.findByCarregamentoId(carregamentoId)
                .orElseThrow(() -> new EntityNotFoundException("Prestação de contas não encontrada para este carregamento"));
    }

    // historico consolidado, por data e opcionalmente filtrado por caminhao - relatorio da frota de terceiros
    public List<PrestacaoContasTerceiro> listarPrestacoes(LocalDate inicio, LocalDate fim, Long caminhaoId) {
        boolean temPeriodo = inicio != null && fim != null;
        LocalDateTime inicioDT = temPeriodo ? inicio.atStartOfDay() : null;
        LocalDateTime fimDT = temPeriodo ? fim.atTime(23, 59, 59) : null;

        if (caminhaoId != null && temPeriodo) {
            return prestacaoContasTerceiroRepository.findByCarregamento_CaminhaoIdAndDataPrestacaoBetweenOrderByDataPrestacaoDesc(caminhaoId, inicioDT, fimDT);
        }
        if (caminhaoId != null) {
            return prestacaoContasTerceiroRepository.findByCarregamento_CaminhaoIdOrderByDataPrestacaoDesc(caminhaoId);
        }
        if (temPeriodo) {
            return prestacaoContasTerceiroRepository.findByDataPrestacaoBetweenOrderByDataPrestacaoDesc(inicioDT, fimDT);
        }
        return prestacaoContasTerceiroRepository.findAllByOrderByDataPrestacaoDesc();
    }

    private Usuario usuarioLogado() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Usuario logado nao encontrado: " + login));
    }
}
