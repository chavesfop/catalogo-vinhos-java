package br.com.projeto.catalogo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}