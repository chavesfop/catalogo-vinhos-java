package br.com.projeto.catalogo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.Map;

import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projeto.catalogo.model.Vinho;
import br.com.projeto.catalogo.repository.VinhoRepository;

@ExtendWith(MockitoExtension.class)
class VinhoServiceTest {

    @Mock
    private VinhoRepository repository;

    @InjectMocks
    private VinhoService service;

    @Test
    void deveCadastrarVinhoComPrecoValido() {

        // Prepara os dados
        Vinho vinho = new Vinho();
        vinho.setNome("Malbec Argentino");
        vinho.setUva("Malbec");
        vinho.setPreco(new BigDecimal("89.90"));
        vinho.setQuantidade(10);
        vinho.setEstoqueMinimo(5);

        // Simula o comportamento do Repository
        when(repository.save(vinho)).thenReturn(vinho);

        // Executa o metodo que queremos testar
        Vinho resultado = service.cadastrar(vinho);

        // Verifica o resultado
        assertNotNull(resultado);
        assertEquals("Malbec Argentino", resultado.getNome());
        assertEquals(new BigDecimal("89.90"), resultado.getPreco());
        assertEquals(10, resultado.getQuantidade());
        assertEquals(5, resultado.getEstoqueMinimo());

        // Confirma que o Repository foi chamado
        verify(repository).save(vinho);
    }

    @Test
    void naoDeveCadastrarVinhoComPrecoNegativo() {

        // Prepara um vinho invalido
        Vinho vinho = new Vinho();
        vinho.setNome("Vinho Teste");
        vinho.setUva("Cabernet Sauvignon");
        vinho.setPreco(new BigDecimal("-50.00"));

        // Verifica se o Service rejeita o cadastro
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.cadastrar(vinho)
        );

        // Confere a mensagem de erro
        assertEquals(
            "O preco nao pode ser negativo.",
            exception.getMessage()
        );

        // Confirma que nenhum registro foi salvo
        verify(repository, never()).save(any(Vinho.class));
    }
    
    @Test
    void naoDeveCadastrarVinhoComQuantidadeNegativa() {

        Vinho vinho = new Vinho();

        vinho.setNome("Vinho Teste");
        vinho.setUva("Malbec");
        vinho.setPreco(new BigDecimal("89.90"));
        vinho.setQuantidade(-5);
        vinho.setEstoqueMinimo(3);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.cadastrar(vinho)
        );

        assertEquals(
            "A quantidade nao pode ser negativa.",
            exception.getMessage()
        );

        verify(repository, never()).save(any(Vinho.class));
    }
    
    @Test
    void naoDeveCadastrarVinhoComEstoqueMinimoNegativo() {

        Vinho vinho = new Vinho();

        vinho.setNome("Vinho Teste");
        vinho.setUva("Cabernet Sauvignon");
        vinho.setPreco(new BigDecimal("129.90"));
        vinho.setQuantidade(10);
        vinho.setEstoqueMinimo(-2);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.cadastrar(vinho)
        );

        assertEquals(
            "O estoque minimo nao pode ser negativo.",
            exception.getMessage()
        );

        verify(repository, never()).save(any(Vinho.class));
    }
    
    @Test
    void deveBuscarVinhoPorId() {

        // Prepara os dados
        Vinho vinho = new Vinho();
        vinho.setNome("Alma Negra");
        vinho.setUva("Blend");
        vinho.setPreco(new BigDecimal("159.90"));

        // Simula o retorno do banco
        when(repository.findById(1L))
            .thenReturn(Optional.of(vinho));

        // Executa o metodo
        Optional<Vinho> resultado = service.buscarPorId(1L);

        // Verifica o resultado
        assertTrue(resultado.isPresent());
        assertEquals("Alma Negra", resultado.get().getNome());

        // Confirma que o Repository foi consultado
        verify(repository).findById(1L);
    }
    
    @Test
    void deveRetornarVazioQuandoVinhoNaoExiste() {

        // Simula um vinho inexistente
        when(repository.findById(9999L))
            .thenReturn(Optional.empty());

        // Executa o metodo
        Optional<Vinho> resultado = service.buscarPorId(9999L);

        // Verifica se o resultado esta vazio
        assertTrue(resultado.isEmpty());

        // Confirma a consulta
        verify(repository).findById(9999L);
    }
    
    @Test
    void deveAtualizarVinhoComSucesso() {

        // Vinho existente no banco
        Vinho vinhoExistente = new Vinho();
        vinhoExistente.setNome("Alma Negra");
        vinhoExistente.setUva("Blend");
        vinhoExistente.setPreco(new BigDecimal("159.90"));

        // Novos dados
        Vinho dadosAtualizados = new Vinho();
        dadosAtualizados.setNome("Alma Negra Misterio");
        dadosAtualizados.setUva("Blend");
        dadosAtualizados.setPreco(new BigDecimal("179.90"));

        // Simula a consulta ao banco
        when(repository.findById(1L))
            .thenReturn(Optional.of(vinhoExistente));

        // Simula a persistencia
        when(repository.save(vinhoExistente))
            .thenReturn(vinhoExistente);

        // Executa a atualizacao
        Optional<Vinho> resultado =
            service.atualizar(1L, dadosAtualizados);

        // Verifica os resultados
        assertTrue(resultado.isPresent());

        assertEquals(
            "Alma Negra Misterio",
            resultado.get().getNome()
        );

        assertEquals(
            new BigDecimal("179.90"),
            resultado.get().getPreco()
        );

        // Verifica as operacoes no Repository
        verify(repository).findById(1L);
        verify(repository).save(vinhoExistente);
    }
    
    @Test
    void naoDeveAtualizarVinhoComPrecoNegativo() {

        // Dados invalidos
        Vinho dadosAtualizados = new Vinho();
        dadosAtualizados.setNome("Vinho Teste");
        dadosAtualizados.setUva("Malbec");
        dadosAtualizados.setPreco(new BigDecimal("-100.00"));

        // Espera uma excecao
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.atualizar(1L, dadosAtualizados)
        );

        // Verifica a mensagem
        assertEquals(
            "O preco nao pode ser negativo.",
            exception.getMessage()
        );

        // Nenhuma operacao deve chegar ao Repository
        verifyNoInteractions(repository);
    }
    
    @Test
    void naoDeveAtualizarVinhoInexistente() {

        // Dados validos
        Vinho dadosAtualizados = new Vinho();
        dadosAtualizados.setNome("Vinho Inexistente");
        dadosAtualizados.setUva("Cabernet Sauvignon");
        dadosAtualizados.setPreco(new BigDecimal("99.90"));

        // Simula um vinho nao encontrado
        when(repository.findById(9999L))
            .thenReturn(Optional.empty());

        // Executa a atualizacao
        Optional<Vinho> resultado =
            service.atualizar(9999L, dadosAtualizados);

        // Verifica que nao encontrou o vinho
        assertTrue(resultado.isEmpty());

        // Confirma a consulta
        verify(repository).findById(9999L);

        // Confirma que nenhum vinho foi salvo
        verify(repository, never()).save(any(Vinho.class));
    }
    
    @Test
    void deveExcluirVinhoExistente() {

        // Simula a existencia do vinho no banco
        when(repository.existsById(1L))
            .thenReturn(true);

        // Executa a exclusao
        boolean resultado = service.excluir(1L);

        // Verifica se a exclusao foi realizada
        assertTrue(resultado);

        // Confirma que o Repository verificou a existencia
        verify(repository).existsById(1L);

        // Confirma que o metodo de exclusao foi chamado
        verify(repository).deleteById(1L);
    }
    
    @Test
    void naoDeveExcluirVinhoInexistente() {

        // Simula um vinho que nao existe no banco
        when(repository.existsById(9999L))
            .thenReturn(false);

        // Executa a exclusao
        boolean resultado = service.excluir(9999L);

        // Verifica que a exclusao nao foi realizada
        assertFalse(resultado);

        // Confirma que a existencia foi consultada
        verify(repository).existsById(9999L);

        // Confirma que nenhuma exclusao foi executada
        verify(repository, never()).deleteById(anyLong());
    }
    
    @Test
    void deveRegistrarEntradaDeEstoque() {

        // Prepara um vinho com 10 garrafas
        Vinho vinho = new Vinho();
        vinho.setNome("Alma Negra");
        vinho.setUva("Blend");
        vinho.setPreco(new BigDecimal("159.90"));
        vinho.setQuantidade(10);
        vinho.setEstoqueMinimo(5);

        // Simula a consulta ao banco
        when(repository.findById(1L))
            .thenReturn(Optional.of(vinho));

        // Simula a persistencia
        when(repository.save(vinho))
            .thenReturn(vinho);

        // Registra a entrada de 5 garrafas
        Optional<Vinho> resultado =
            service.registrarEntrada(1L, 5);

        // Verifica o novo estoque
        assertTrue(resultado.isPresent());
        assertEquals(15, resultado.get().getQuantidade());

        // Confirma as operacoes no Repository
        verify(repository).findById(1L);
        verify(repository).save(vinho);
    }
    
    @Test
    void naoDeveRegistrarEntradaNegativa() {

        // Tenta registrar uma entrada invalida
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrarEntrada(1L, -5)
        );

        // Verifica a mensagem de erro
        assertEquals(
            "A quantidade de entrada deve ser maior que zero.",
            exception.getMessage()
        );

        // Nenhuma operacao deve chegar ao Repository
        verifyNoInteractions(repository);
    }
    
    @Test
    void naoDeveRegistrarEntradaEmVinhoInexistente() {

        // Simula um vinho inexistente
        when(repository.findById(9999L))
            .thenReturn(Optional.empty());

        // Tenta registrar a entrada
        Optional<Vinho> resultado =
            service.registrarEntrada(9999L, 5);

        // Verifica que o vinho nao existe
        assertTrue(resultado.isEmpty());

        // Confirma que houve uma consulta
        verify(repository).findById(9999L);

        // Confirma que nada foi salvo
        verify(repository, never()).save(any(Vinho.class));
    }
    
    @Test
    void deveRegistrarSaidaDeEstoque() {

        // Prepara um vinho com 10 garrafas
        Vinho vinho = new Vinho();
        vinho.setNome("Alma Negra");
        vinho.setUva("Blend");
        vinho.setPreco(new BigDecimal("159.90"));
        vinho.setQuantidade(10);
        vinho.setEstoqueMinimo(5);

        // Simula a consulta ao banco
        when(repository.findById(1L))
            .thenReturn(Optional.of(vinho));

        // Simula a persistencia
        when(repository.save(vinho))
            .thenReturn(vinho);

        // Registra a saida de 3 garrafas
        Optional<Vinho> resultado =
            service.registrarSaida(1L, 3);

        // Verifica o novo estoque
        assertTrue(resultado.isPresent());
        assertEquals(7, resultado.get().getQuantidade());

        // Confirma as operacoes no Repository
        verify(repository).findById(1L);
        verify(repository).save(vinho);
    }
    
    @Test
    void naoDeveRegistrarSaidaComEstoqueInsuficiente() {

        // Prepara um vinho com apenas 3 garrafas
        Vinho vinho = new Vinho();
        vinho.setNome("Angelica Zapata");
        vinho.setUva("Malbec");
        vinho.setPreco(new BigDecimal("189.90"));
        vinho.setQuantidade(3);
        vinho.setEstoqueMinimo(5);

        when(repository.findById(1L))
            .thenReturn(Optional.of(vinho));

        // Tenta retirar 5 garrafas
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrarSaida(1L, 5)
        );

        // Verifica a mensagem de erro
        assertEquals(
            "Estoque insuficiente. Disponivel: 3",
            exception.getMessage()
        );

        // Verifica que o estoque permanece inalterado
        assertEquals(3, vinho.getQuantidade());

        // Confirma que nenhuma alteracao foi salva
        verify(repository, never()).save(any(Vinho.class));
    }
    
    @Test
    void naoDeveRegistrarSaidaNegativa() {

        // Tenta registrar uma saida invalida
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrarSaida(1L, -5)
        );

        // Verifica a mensagem
        assertEquals(
            "A quantidade de saida deve ser maior que zero.",
            exception.getMessage()
        );

        // Nenhuma operacao deve chegar ao Repository
        verifyNoInteractions(repository);
    }
    
    @Test
    void naoDeveRegistrarSaidaEmVinhoInexistente() {

        // Simula um vinho inexistente
        when(repository.findById(9999L))
            .thenReturn(Optional.empty());

        // Tenta registrar a saida
        Optional<Vinho> resultado =
            service.registrarSaida(9999L, 3);

        // Verifica que o vinho nao foi encontrado
        assertTrue(resultado.isEmpty());

        // Confirma que houve uma consulta
        verify(repository).findById(9999L);

        // Confirma que nenhuma alteracao foi salva
        verify(repository, never()).save(any(Vinho.class));
    }
    
    @Test
    void deveSugerirReposicaoDeEstoque() {

        // Prepara um vinho com estoque baixo
        Vinho vinho = new Vinho();
        vinho.setNome("Alma Negra");
        vinho.setUva("Blend");
        vinho.setPreco(new BigDecimal("159.90"));
        vinho.setQuantidade(2);
        vinho.setEstoqueMinimo(5);

        // Simula o ID gerado pelo JPA
        ReflectionTestUtils.setField(vinho, "id", 1L);

        // Simula o retorno do Repository
        when(repository.buscarComEstoqueBaixo())
            .thenReturn(List.of(vinho));

        // Executa o metodo
        List<Map<String, Object>> resultado =
            service.sugerirReposicao();

        // Verifica a quantidade de resultados
        assertEquals(1, resultado.size());

        // Recupera a primeira sugestao
        Map<String, Object> sugestao = resultado.get(0);

        // Verifica as informacoes retornadas
        assertEquals(1L, sugestao.get("id"));
        assertEquals("Alma Negra", sugestao.get("nome"));
        assertEquals(2, sugestao.get("quantidadeAtual"));
        assertEquals(5, sugestao.get("estoqueMinimo"));

        // Verifica o calculo da reposicao
        assertEquals(3, sugestao.get("quantidadeSugerida"));

        // Confirma a consulta ao Repository
        verify(repository).buscarComEstoqueBaixo();
    }
    
    @Test
    void deveRetornarListaVaziaQuandoNaoHaReposicao() {

        // Simula que nao existem vinhos com estoque baixo
        when(repository.buscarComEstoqueBaixo())
            .thenReturn(List.of());

        // Executa o metodo
        List<Map<String, Object>> resultado =
            service.sugerirReposicao();

        // Verifica o resultado
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        // Confirma a consulta ao Repository
        verify(repository).buscarComEstoqueBaixo();
    }
    
    @Test
    void deveListarVinhosComEstoqueBaixo() {

        // Primeiro vinho com estoque baixo
        Vinho vinho1 = new Vinho();
        vinho1.setNome("Alma Negra");
        vinho1.setQuantidade(2);
        vinho1.setEstoqueMinimo(5);

        // Segundo vinho com estoque baixo
        Vinho vinho2 = new Vinho();
        vinho2.setNome("Angelica Zapata");
        vinho2.setQuantidade(1);
        vinho2.setEstoqueMinimo(4);

        // Simula o retorno do Repository
        when(repository.buscarComEstoqueBaixo())
            .thenReturn(List.of(vinho1, vinho2));

        // Executa o metodo
        List<Vinho> resultado = service.listarEstoqueBaixo();

        // Verifica os resultados
        assertEquals(2, resultado.size());

        assertEquals("Alma Negra", resultado.get(0).getNome());
        assertEquals("Angelica Zapata", resultado.get(1).getNome());

        // Confirma a consulta ao Repository
        verify(repository).buscarComEstoqueBaixo();
    }
    
    @Test
    void deveRetornarListaVaziaQuandoNaoHaEstoqueBaixo() {

        // Simula que nenhum vinho esta com estoque baixo
        when(repository.buscarComEstoqueBaixo())
            .thenReturn(List.of());

        // Executa o metodo
        List<Vinho> resultado = service.listarEstoqueBaixo();

        // Verifica o resultado
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        // Confirma a consulta
        verify(repository).buscarComEstoqueBaixo();
    }
    
    @Test
    void naoDeveRegistrarEntradaComQuantidadeZero() {

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrarEntrada(1L, 0)
        );

        assertEquals(
            "A quantidade de entrada deve ser maior que zero.",
            exception.getMessage()
        );

        verifyNoInteractions(repository);
    }
    
    @Test
    void naoDeveRegistrarSaidaComQuantidadeZero() {

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrarSaida(1L, 0)
        );

        assertEquals(
            "A quantidade de saida deve ser maior que zero.",
            exception.getMessage()
        );

        verifyNoInteractions(repository);
    }
    
    @Test
    void naoDeveRegistrarEntradaAcimaDoLimiteInteger() {

        // Prepara um vinho com estoque no limite maximo
        Vinho vinho = new Vinho();

        vinho.setNome("Vinho Teste");
        vinho.setQuantidade(Integer.MAX_VALUE);
        vinho.setEstoqueMinimo(5);

        // Simula a consulta ao banco
        when(repository.findById(1L))
            .thenReturn(Optional.of(vinho));

        // Tenta adicionar mais uma garrafa
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.registrarEntrada(1L, 1)
        );

        // Verifica a mensagem de erro
        assertEquals(
            "A quantidade excede o limite permitido.",
            exception.getMessage()
        );

        // Confirma que o estoque nao foi alterado
        assertEquals(
            Integer.MAX_VALUE,
            vinho.getQuantidade()
        );

        // Confirma que nenhuma alteracao foi salva
        verify(repository, never()).save(any(Vinho.class));
    }
    
}