package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.aspectj.apache.bcel.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

	private Scanner leitura = new Scanner(System.in);
	private ConsumoApi consumo = new ConsumoApi();
	private ConverteDados conversor = new ConverteDados();
	private final String ENDERECO = "https://www.omdbapi.com/?t=";
	private final String API_KEY = "&apikey=6585022c";

	private SerieRepository repository;
	private List<Serie> series = new ArrayList<>();

	// criando lista para guardar DadosSerie.
	private List<DadosSerie> dadosSeries = new ArrayList<>();

	public Principal(SerieRepository repository) {
		this.repository = repository;
	}

	public void exibeMenu() {
		var opcao = -1;
		while (opcao != 0) {
			var menu = """
					1 - Buscar séries
					2 - Buscar episódios
					3 - Imprimir lista local
					4 - Buscar Series por titulo
					5 - Buscar Series pelo nome do Ator
					6 - Top 5 Séries
					7 - Buscar séries pôr categoria
					0 - Sair
					""";

			System.out.println(menu);
			opcao = leitura.nextInt();
			leitura.nextLine();

			switch (opcao) {
			case 1:
				buscarSerieWeb();
				break;
			case 2:
				buscarEpisodioPorSerie();
				break;
			case 3:
				listarSeriesBuscadas();
				break;
			case 4:
				buscarSeriePorTitulo();
				break;
			case 5:
				buscarSeriePorAtor();
				break;
			case 6:
				buscarTop5Series();
				break;
			case 7:
				buscarSeriesPorCategoria();
				break;
			case 0:
				System.out.println("Saindo...");
				break;

			default:
				System.out.println("Opção inválida");
			}
		}
	}

	private void buscarSeriesPorCategoria() {
		System.out.println("Deseja buscar series de que categoria/genero? ");
		var nomeGenero = leitura.nextLine();
		Categoria categoria = Categoria.fromPortugues(nomeGenero);// Atenção aqui, pegando Categoria
		List<Serie>  seriesPorCategoria = repository.findByGenero(categoria);
		System.out.println("Series por categoria/genero: "+ nomeGenero);
		seriesPorCategoria.forEach(System.out::println);
	}

	private void buscarTop5Series() {
		List<Serie> seriesTop = repository.findTop5ByOrderByAvaliacaoDesc();
		seriesTop.forEach(s -> System.out.println(s.getTitulo() + "Avaliação: " + s.getAvaliacao()));

	}

	private void buscarSeriePorAtor() {
		listarSeriesBuscadas();
		System.out.println("Digite o nome do ATOR para busca no Banco de dados com serieRepository: ");
		var nomeAtor = leitura.nextLine();
		System.out.println("Informe a nota da avaliação minima que vc deseja: ");
		var avaliacao = leitura.nextDouble();
		List<Serie> seriesEncontradas = repository
				.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
		System.out.println("Nome Ator");
		seriesEncontradas
				.forEach(s -> System.out.println(s.getTitulo() + "=> " + s.getAtores() + " " + s.getAvaliacao()));

	}

	private void buscarSeriePorTitulo() {
		listarSeriesBuscadas();
		System.out.println("Digite o nome do serie para busca no Banco de dados com serieRepository: ");
		var nomeSerie = leitura.nextLine();
		Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);
		if (serie.isPresent()) {
			System.out.println("Apresentando dados da serie: " + serie.get());

		} else {
			System.out.println("Serie não encontrada.");
		}
	}

	private void listarSeriesBuscadas() {
		System.out.println("Saida Local.");
		series = repository.findAll();

//        series = dadosSeries.stream() // Quando não pegava do Postgre e sim de uma lista local.
//                .map(d -> new Serie(d))
//                .collect(Collectors.toList());
		series.stream().sorted(Comparator.comparing(Serie::getGenero));
		series.forEach(System.out::println);

	}

	private void buscarSerieWeb() {
		DadosSerie dados = getDadosSerie();
		Serie serie = new Serie(dados);// O Repository é sobre Serie.
		// dadosSeries.add(dados);
		repository.save(serie);
		System.out.println(dados);
	}

	private DadosSerie getDadosSerie() {
		System.out.println("Digite o nome da série para busca");
		var nomeSerie = leitura.nextLine();
		var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		return dados;
	}

	private void buscarEpisodioPorSerie() {
		// DadosSerie dadosSerie = getDadosSerie();
		// Listando series no banco de dados
		listarSeriesBuscadas();
		// Todo metodo abaixo poderia ser substituido por:
		// Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);
		System.out.println("Digite o nome da série para busca no banco de dados");
		var nomeSerie = leitura.nextLine();
		Optional<Serie> serie = series.stream()
				.filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase())).findFirst();
		if (serie.isPresent()) {
			var serieEncontrada = serie.get();
			List<DadosTemporada> temporadas = new ArrayList<>();

			for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
				var json = consumo.obterDados(
						ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
				DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
				temporadas.add(dadosTemporada);
			}
			temporadas.forEach(System.out::println);
			List episodios = temporadas.stream().flatMap(d -> d.episodios().stream())
					.map(e -> new Episodio(e.numero(), e)).collect(Collectors.toList());
			serieEncontrada.setEpisodios(episodios);
			repository.save(serieEncontrada);
		} else {
			System.out.println("Série não encontrada");
		}
	}
}