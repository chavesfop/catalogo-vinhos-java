package br.com.projeto.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.projeto.catalogo.model.Vinho;

/**
 * JpaRepository: interface do Spring Data JPA que oferece operacoes de persistencia.
 * Vinho: entidade que sera manipulada.
 * Long: tipo da chave primaria da entidade.
 */
public interface VinhoRepository extends JpaRepository<Vinho, Long> {

}