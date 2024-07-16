package com.rapaix.api_livros.services;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.rapaix.api_livros.models.Autor;
import com.rapaix.api_livros.models.Book;
import com.rapaix.api_livros.models.Livro;
import com.rapaix.api_livros.repository.AutorRepository;
import com.rapaix.api_livros.repository.LivroRepository;
import com.rapaix.api_livros.utils.GutendexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class GutendexService {

    private static final String GUTENDEX_API_URL = "https://gutendex.com/books";
    private static final Logger logger = LoggerFactory.getLogger(GutendexService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public GutendexService(LivroRepository livroRepository, AutorRepository autorRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
    }

    @Transactional
    public List<Livro> fetchLivrosFromGutendex(String searchTerm) throws GutendexException {
        List<Livro> livros = new ArrayList<>();
        try {
            List<Book> booksFromApi = fetchBooksFromGutendex(searchTerm);

            for (Book book : booksFromApi) {
                Livro existingLivro = livroRepository.findByTitulo(book.getTitle());

                if (existingLivro == null) {
                    Livro novoLivro = new Livro();
                    novoLivro.setTitulo(book.getTitle());
                    novoLivro.setIdioma(book.getLanguage());
                    novoLivro.setNumeroDownloads(book.getDownloadCount());

                    List<Autor> autores = new ArrayList<>();
                    for (Book.Author author : book.getAuthors()) {
                        Autor existingAutor = autorRepository.findByNome(author.getName());
                        if (existingAutor == null) {
                            Autor novoAutor = new Autor();
                            novoAutor.setNome(author.getName());
                            novoAutor.setAnoNascimento(author.getBirthYear());
                            novoAutor.setAnoFalecimento(author.getDeathYear());
                            autorRepository.save(novoAutor);
                            autores.add(novoAutor);
                        } else {
                            if (!entityManager.contains(existingAutor)) {
                                existingAutor = entityManager.merge(existingAutor);
                            }
                            autores.add(existingAutor);
                        }
                    }
                    novoLivro.setAutores(autores);
                    livroRepository.save(novoLivro);
                    livros.add(novoLivro);
                } else {
                    livros.add(existingLivro);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to fetch books from Gutendex API", e);
            throw new GutendexException("Failed to fetch books from Gutendex API", e);
        }
        return livros;
    }

    private List<Book> fetchBooksFromGutendex(String searchTerm) throws IOException {
        String url = GUTENDEX_API_URL + "?search=" + searchTerm.replace(" ", "%20");
        String responseBody = executeHttpGetRequest(url);
        return parseJsonResponse(responseBody);
    }

    private String executeHttpGetRequest(String url) throws IOException {
        StringBuilder response = new StringBuilder();
        URL apiUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        } else {
            throw new GutendexException("Request failed: Status code " + responseCode);
        }

        return response.toString();
    }

    private List<Book> parseJsonResponse(String responseBody) {
        List<Book> books = new ArrayList<>();
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody).get("results");
            for (JsonNode node : jsonNode) {
                Book book = new Book();
                book.setTitle(node.get("title").asText());
                book.setAuthors(parseAuthorsNames(node.withArray("authors")));
                book.setLanguage(node.withArray("languages").get(0).asText());
                book.setDownloadCount(node.get("download_count").asInt());
                books.add(book);
            }
        } catch (IOException e) {
            logger.error("Failed to parse JSON response", e);
            throw new GutendexException("Failed to parse JSON response", e);
        }
        return books;
    }

    private List<Book.Author> parseAuthorsNames(JsonNode authorsArray) {
        List<Book.Author> authors = new ArrayList<>();
        authorsArray.forEach(authorNode -> {
            Book.Author author = new Book.Author();
            author.setName(authorNode.get("name").asText());
            author.setBirthYear(authorNode.get("birth_year").isNull() ? null : authorNode.get("birth_year").asInt());
            author.setDeathYear(authorNode.get("death_year").isNull() ? null : authorNode.get("death_year").asInt());
            authors.add(author);
        });
        return authors;
    }
}