package com.rapaix.api_livros.repository;

import com.rapaix.api_livros.models.Livro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    @EntityGraph(attributePaths = "autores")
    Livro findByTitulo(String titulo);

    @EntityGraph(attributePaths = "autores")
    List<Livro> findByIdioma(String idioma);

    @EntityGraph(attributePaths = "autores")
    List<Livro> findAll();
}
