package br.com.projeto.catalogo.service;

import java.util.List;
import java.util.Optional;

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
}