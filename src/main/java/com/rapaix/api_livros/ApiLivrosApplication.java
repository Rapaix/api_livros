package com.rapaix.api_livros;

import com.rapaix.api_livros.services.ConsultaLivro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class ApiLivrosApplication implements CommandLineRunner {
    @Autowired
    ConsultaLivro consultaLivro;

    public static void main(String[] args) {
        SpringApplication.run(ApiLivrosApplication.class, args);


    }

    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
			exibirMenu();

        }


    }
	private void exibirMenu() {
		System.out.println("==== Menu ====");
		System.out.println("1 - Buscar livros por autor ou título");
		System.out.println("2 - Listar livros registrados");
		System.out.println("3 - Listar autores registrados");
		System.out.println("4 - Listar livros por idioma");
		System.out.println("5 - Listar autores vivos em determinado ano");
		System.out.println("6 - Sair");
		System.out.print("Escolha uma opção: ");
	}
}
