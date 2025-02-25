package br.com.alura.screenmatch.testes;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class Exercicios {



    public static void main(String[] args) {


        List<String> input = Arrays.asList("10", "abc", "20", "30x");

        List<Integer> numbers = input.stream()
                .filter(s -> s.matches("\\d+")) // Filtra apenas números inteiros positivos
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        System.out.println(numbers);





    }




}
