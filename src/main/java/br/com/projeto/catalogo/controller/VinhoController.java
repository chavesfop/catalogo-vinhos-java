package br.com.projeto.catalogo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}