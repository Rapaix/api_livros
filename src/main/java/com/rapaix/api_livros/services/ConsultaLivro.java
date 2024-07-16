package com.rapaix.api_livros.services;

import com.rapaix.api_livros.models.Autor;
import com.rapaix.api_livros.models.Livro;
import com.rapaix.api_livros.repository.AutorRepository;
import com.rapaix.api_livros.repository.LivroRepository;
import com.rapaix.api_livros.utils.GutendexException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;

@Component
public class ConsultaLivro {

    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;
    private final GutendexService gutendexService;


    @Value("${url.gutendex}")
    private String URL_BASE;

    @Autowired
    public ConsultaLivro(AutorRepository autorRepository, LivroRepository livroRepository, GutendexService gutendexService) {
        this.autorRepository = autorRepository;
        this.livroRepository = livroRepository;
        this.gutendexService = gutendexService;
    }

    public void consultaPorAutor(String autor) throws IOException, InterruptedException {
        URI uri = URI.create(URL_BASE + "?search=" + autor);
        HttpClient client;
        client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
    }

    @Transactional
    public void buscarLivrosPorTermo(Scanner scanner) {
        System.out.print("Digite o termo de busca (autor ou título): ");
        String termoBusca = scanner.nextLine();

        try {
            List<Livro> livrosEncontrados = gutendexService.fetchLivrosFromGutendex(termoBusca);

            if (livrosEncontrados.isEmpty()) {
                System.out.println("Nenhum livro encontrado para '" + termoBusca + "'.");
            } else {
                System.out.println("Livros encontrados para '" + termoBusca + "':");
                for (Livro livro : livrosEncontrados) {
                    imprimirLivro(livro);
                }
            }
        } catch (GutendexException e) {
            System.err.println("Erro ao buscar livros: " + e.getMessage());
        }
    }

    @Transactional
    public void listarLivrosRegistrados() {
        List<Livro> livros = livroRepository.findAll();

        if (livros.isEmpty()) {
            System.out.println("Não há livros registrados.");
        } else {
            System.out.println("Livros registrados:");
            for (Livro livro : livros) {
                imprimirLivro(livro);
            }
        }
    }

    @Transactional
    public void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("Não há autores registrados.");
        } else {
            System.out.println("Autores registrados:");
            for (Autor autor : autores) {
                imprimirAutor(autor);
            }
        }
    }



    @Transactional
    public void listarLivrosPorIdioma(Scanner scanner) {
        System.out.print("Digite o idioma: ");
        String idioma = scanner.nextLine();

        List<Livro> livros = livroRepository.findByIdioma(idioma);

        if (livros.isEmpty()) {
            System.out.println("Nenhum livro encontrado para o idioma '" + idioma + "'.");
        } else {
            System.out.println("Livros encontrados para o idioma '" + idioma + "':");
            for (Livro livro : livros) {
                imprimirLivro(livro);
            }
        }
    }

    @Transactional
    public void listarAutoresVivosNoAno(Scanner scanner) {
        System.out.print("Digite o ano: ");
        int ano = Integer.parseInt(scanner.nextLine());

        List<Autor> autores = autorRepository.findAutoresVivosNoAno(ano);

        if (autores.isEmpty()) {
            System.out.println("Nenhum autor encontrado que estava vivo no ano '" + ano + "'.");
        } else {
            System.out.println("Autores vivos no ano '" + ano + "':");
            for (Autor autor : autores) {
                imprimirAutor(autor);
            }
        }
    }
    private void imprimirAutor(Autor autor) {
        System.out.println("  - Nome: " + autor.getNome());
        System.out.println("    Ano de Nascimento: " + autor.getAnoNascimento());
        System.out.println("    Ano de Falecimento: " + autor.getAnoFalecimento());
    }

    private void imprimirLivro(Livro livro) {
        System.out.println("Título: " + livro.getTitulo());
        System.out.println("Autores:");
        livro.getAutores().forEach(this::imprimirAutor);
        System.out.println("Idioma: " + livro.getIdioma());
        System.out.println("Número de downloads: " + livro.getNumeroDownloads());
        System.out.println("");
    }

}
