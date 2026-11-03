package br.com.projeto.catalogo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.projeto.catalogo.model.Vinho;
import br.com.projeto.catalogo.repository.VinhoRepository;

@RestController
@RequestMapping("/api/vinhos")
public class VinhoController {

    private final VinhoRepository repository;

    public VinhoController(VinhoRepository repository) {
        this.repository = repository;
    }

    //HTTP GET para /api/vinhos
    //O spring transforma em JSON
    @GetMapping
    public List<Vinho> listar() {
        return repository.findAll();
    }
    
    //HTTP POST para /api/vinhos
    /* {
	    "nome": "Nome do Vinho",
	    "uva": "Uva do Vinho",
	    "preco": 99.90
	  }
	*/
    //@RequestBody Converte automaticamente o JSON recebido em um objeto Java da classe Vinho.
    @PostMapping
    public ResponseEntity<Vinho> cadastrar(@RequestBody Vinho vinho) {

    	//Utiliza o Spring Data JPA e o Hibernate para persistir o vinho no banco SQLite.
        Vinho vinhoSalvo = repository.save(vinho);

        //Retorna o código HTTP 201, indicando que o recurso foi criado com sucesso.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vinhoSalvo);
    }
    
    //HTTP GET para /api/vinhos/{id}
    // @PathVariable captura o valor 1 da URL e disponibiliza esse valor na variavel id
    @GetMapping("/{id}")
    public ResponseEntity<Vinho> buscarPorId(@PathVariable Long id) {
		//Retorna um vinho caso encontre, e caso não encontre retorna um HTTP 404
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    //HTTP PUT para /api/vinhos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Vinho> atualizar(
            @PathVariable Long id,
            @RequestBody Vinho dadosAtualizados) {

    	//Primeiro, verificamos se o vinho existe no banco.
        return repository.findById(id)
                .map(vinho -> {
                	//Atualizamos os atributos do objeto com os dados recebidos no JSON.
                    vinho.setNome(dadosAtualizados.getNome());
                    vinho.setUva(dadosAtualizados.getUva());
                    vinho.setPreco(dadosAtualizados.getPreco());
                    
                    //O Spring Data JPA utiliza o Hibernate para persistir as alterações no SQLite.
                    Vinho vinhoAtualizado = repository.save(vinho);
                	
                    //Retorna o vinho com os valores atualizado
                    return ResponseEntity.ok(vinhoAtualizado);
                })
                //caso não encontre o vinho retorna HTTP 404.
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    //HTTP DELETE para /api/vinhos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

    	//Verifica se o vinho existe no banco de dados.
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        //Utiliza o Spring Data JPA para excluir o registro do SQLite.
        repository.deleteById(id);

        //Retorna o código HTTP 204 No Content, indicando que a exclusão foi realizada com sucesso.
        return ResponseEntity.noContent().build();
    }
}