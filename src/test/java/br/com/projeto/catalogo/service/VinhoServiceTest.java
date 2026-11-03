package br.com.projeto.catalogo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.math.BigDecimal;
import java.util.Optional;

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

        // Simula o comportamento do Repository
        when(repository.save(vinho)).thenReturn(vinho);

        // Executa o metodo que queremos testar
        Vinho resultado = service.cadastrar(vinho);

        // Verifica o resultado
        assertNotNull(resultado);
        assertEquals("Malbec Argentino", resultado.getNome());
        assertEquals(new BigDecimal("89.90"), resultado.getPreco());

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
    
    
}