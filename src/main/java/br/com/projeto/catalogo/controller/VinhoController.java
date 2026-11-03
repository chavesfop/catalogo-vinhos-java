package br.com.projeto.catalogo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.com.projeto.catalogo.service.VinhoService;

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

@RestController
@RequestMapping("/api/vinhos")
public class VinhoController {

	private final VinhoService service;

	public VinhoController(VinhoService service) {
	    this.service = service;
	}

    //HTTP GET para /api/vinhos
    //O spring transforma em JSON
    @GetMapping
    public List<Vinho> listar() {
        return service.listarTodos();
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
    public ResponseEntity<?> cadastrar(@RequestBody Vinho vinho) {

        try {
        	//tenta cadastrar um vinho
            Vinho vinhoSalvo = service.cadastrar(vinho);

            //em caso de sucesso retorna o vinho cadastrado
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(vinhoSalvo);

        } catch (IllegalArgumentException e) {
        	//caso tenha algum parametro errado, como preço negativo ou id do vinho exibe um erro
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }
    
    //HTTP GET para /api/vinhos/{id}
    // @PathVariable captura o valor 1 da URL e disponibiliza esse valor na variavel id
    @GetMapping("/{id}")
    public ResponseEntity<Vinho> buscarPorId(@PathVariable Long id) {
		//Retorna um vinho caso encontre, e caso não encontre retorna um HTTP 404
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    //HTTP PUT para /api/vinhos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody Vinho dadosAtualizados) {

        try {

            Optional<Vinho> vinhoAtualizado =
                    service.atualizar(id, dadosAtualizados);

            if (vinhoAtualizado.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(vinhoAtualizado.get());

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of("erro", e.getMessage()));
        }
    }
    
    //HTTP DELETE para /api/vinhos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        boolean excluido = service.excluir(id);

        if (!excluido) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}