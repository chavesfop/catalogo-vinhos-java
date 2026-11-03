package br.com.projeto.catalogo.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.projeto.catalogo.model.Vinho;
import br.com.projeto.catalogo.repository.VinhoRepository;

//A anotação @Service informa ao Spring que essa classe representa um componente da camada de serviços.
//O Spring gerencia sua instância e permite que ela seja injetada em outras classes.
@Service
public class VinhoService {

    private final VinhoRepository repository;

    public VinhoService(VinhoRepository repository) {
        this.repository = repository;
    }
    
    //O Optional<Vinho> representa um resultado que pode ou não conter um vinho.
    public Optional<Vinho> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<Vinho> listarTodos() {
        return repository.findAll();
    }
    
    public Vinho cadastrar(Vinho vinho) {

        // Impede cadastrar um vinho com ID informado
        if (vinho.getId() != null) {
            throw new IllegalArgumentException(
                "O ID nao deve ser informado no cadastro."
            );
        }

        // Valida o preco do vinho
        if (vinho.getPreco() == null ||
            vinho.getPreco().signum() < 0) {

            throw new IllegalArgumentException(
                "O preco nao pode ser negativo."
            );
        }
        
     // Valida a quantidade em estoque
        if (vinho.getQuantidade() == null ||
            vinho.getQuantidade() < 0) {

            throw new IllegalArgumentException(
                "A quantidade nao pode ser negativa."
            );
        }

        // Valida o estoque minimo
        if (vinho.getEstoqueMinimo() == null ||
            vinho.getEstoqueMinimo() < 0) {

            throw new IllegalArgumentException(
                "O estoque minimo nao pode ser negativo."
            );
        }

        // Persiste o vinho no banco
        return repository.save(vinho);
    }
    
    public Optional<Vinho> atualizar(Long id, Vinho dadosAtualizados) {

        // Valida o preco informado
        if (dadosAtualizados.getPreco() == null ||
            dadosAtualizados.getPreco().signum() < 0) {

            throw new IllegalArgumentException(
                "O preco nao pode ser negativo."
            );
        }

        // Busca o vinho existente
        return repository.findById(id)
                .map(vinho -> {

                    // Atualiza os atributos
                    vinho.setNome(dadosAtualizados.getNome());
                    vinho.setUva(dadosAtualizados.getUva());
                    vinho.setPreco(dadosAtualizados.getPreco());

                    // Persiste as alteracoes
                    return repository.save(vinho);
                });
    }
    
    public boolean excluir(Long id) {

        // Verifica se o vinho existe
        if (!repository.existsById(id)) {
            return false;
        }

        // Exclui o vinho utilizando JPA
        repository.deleteById(id);

        return true;
    }
    
    public Optional<Vinho> registrarEntrada(Long id, Integer quantidade) {

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                "A quantidade de entrada deve ser maior que zero."
            );
        }

        return repository.findById(id)
                .map(vinho -> {

                    Integer estoqueAtual = vinho.getQuantidade();

                    if (estoqueAtual == null || estoqueAtual < 0) {
                        throw new IllegalArgumentException(
                            "O estoque atual do vinho e invalido."
                        );
                    }

                    int novoEstoque;

                    try {
                        novoEstoque = Math.addExact(
                            estoqueAtual, quantidade
                        );
                    } catch (ArithmeticException e) {
                        throw new IllegalArgumentException(
                            "A quantidade excede o limite permitido."
                        );
                    }

                    vinho.setQuantidade(novoEstoque);

                    return repository.save(vinho);
                });
    }
    
    public Optional<Vinho> registrarSaida(Long id, Integer quantidade) {

        // Valida a quantidade informada
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                "A quantidade de saida deve ser maior que zero."
            );
        }

        // Busca o vinho no banco
        return repository.findById(id)
                .map(vinho -> {

                    Integer estoqueAtual = vinho.getQuantidade();

                    // Verifica se o estoque possui um valor valido
                    if (estoqueAtual == null || estoqueAtual < 0) {
                        throw new IllegalArgumentException(
                            "O estoque atual do vinho e invalido."
                        );
                    }

                    // Verifica se existe estoque suficiente
                    if (quantidade > estoqueAtual) {
                        throw new IllegalArgumentException(
                            "Estoque insuficiente. Disponivel: "
                            + estoqueAtual
                        );
                    }

                    // Calcula a quantidade restante
                    int novoEstoque = estoqueAtual - quantidade;

                    // Atualiza o estoque
                    vinho.setQuantidade(novoEstoque);

                    // Persiste a alteracao utilizando JPA
                    return repository.save(vinho);
                });
    }
    
    public List<Vinho> listarEstoqueBaixo() {
        return repository.buscarComEstoqueBaixo();
    }
    
    public List<Map<String, Object>> sugerirReposicao() {

        return repository.buscarComEstoqueBaixo()
                .stream()
                .map(vinho -> {

                    int quantidadeSugerida =
                        vinho.getEstoqueMinimo() - vinho.getQuantidade();

                    return Map.<String, Object>of(
                        "id", vinho.getId(),
                        "nome", vinho.getNome(),
                        "quantidadeAtual", vinho.getQuantidade(),
                        "estoqueMinimo", vinho.getEstoqueMinimo(),
                        "quantidadeSugerida", quantidadeSugerida
                    );

                })
                .toList();
    }
    
    
    
}