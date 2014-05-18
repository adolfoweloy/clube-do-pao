package br.com.brncalmeida.clubepao.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.LocalDate;

import br.com.brncalmeida.clubepao.utils.Util;

/**
 * Classe respons�vel por receber um range de datas e uma lista de membros. A partir destas campos, ir� intercalar a programa��o entre os membros.
 * 
 * @author bruno.almeida
 * 
 */
public class Schedule {
	private LocalDate dataInicial;
	private LocalDate dataFinal;
	private List<Membro> membros;
	private Map<Date, String> programacaoPorMembro;
	private Map<Integer, String> programacaoSobrecarga;
	private SugestaoTrocaDisponibilidades sugestao;

	/**
	 * Construtor default
	 * 
	 * @param dataInicial
	 *            data inicial do range que dever� compor a programa��o
	 * @param dataFinal
	 *            data final do range que dever� compor a programa��o
	 * @param membros
	 *            membros que ir�o compor a programa��o
	 */
	public Schedule(LocalDate dataInicial, LocalDate dataFinal, List<Membro> membros) {
		if (dataInicial == null)
			throw new NullPointerException("Campo data inicial invalido");
		else if (dataFinal == null)
			throw new NullPointerException("campo data final invalido");
		else if (membros == null || membros.size() == 0)
			throw new NullPointerException("campo membros invalido");
		else {
			this.dataInicial = dataInicial;
			this.dataFinal = dataFinal;
			this.membros = membros;
			this.programacaoPorMembro = processarAlocacaoMembro();
			this.programacaoSobrecarga = processarAlocacaoSobrecarga();
			this.sugestao = criarSugestao();
		}
	}

	/**
	 * A sobrecarga da programa��o equivale ao resumo de programa��es. A Key equivale a qtd de programa��es dos membros, j� o value equivale a lista concatenada de
	 * membros.
	 * 
	 * @return map(key=qtd programa��es / value=membros)
	 */
	public Map<Integer, String> getProgramacaoSobrecarga() {
		return Collections.unmodifiableMap(programacaoSobrecarga);
	}

	/**
	 * Programa��o completa dos dias �teis com membro disponivel para o dia.
	 * 
	 * @return map(key=dia programa��o / value=membro correspondente)
	 */
	public Map<Date, String> getProgramacaoPorMembro() {
		return Collections.unmodifiableMap(programacaoPorMembro);
	}

	/**
	 * --Deprecated: Em desenvolvimento-- M�todo respons�vel por apresentar sugest�o de troca de disponibilidades entre membros.
	 * 
	 * @return Sugest�o completa.
	 */
	@Deprecated
	public SugestaoTrocaDisponibilidades getSugestao() {
		return sugestao;
	}

	// ~-~-~-~-~-~-~-~-~-~-~-~-~ M�todos privados ~-~-~-~-~-~-~-~-~-~-~-~-~

	/**
	 * M�todo respons�vel por apresentar sugest�o de troca de disponibilidades entre membros. Em desenvolvimento
	 * 
	 * @return Sugest�o completa.
	 */
	@Deprecated
	private SugestaoTrocaDisponibilidades criarSugestao() {
		double[] qtds = new double[membros.size()];
		int i = 0;

		// criando mapa de qtd de programa��es para um determinado dia
		for (Membro membro : membros) {
			qtds[i++] = membro.getDiasProgramados().size();
		}

		double mediaPaes = Util.getMediaAritmetica(qtds);
		double desvioPadraoPaes = Util.getDesvioPadrao(qtds);
		int rangeInicialOk = (int) (mediaPaes - desvioPadraoPaes);
		int rangeFinalOk = (int) (mediaPaes + desvioPadraoPaes);
		if (rangeInicialOk == 0)
			rangeInicialOk = 1;

		List<Membro> membrosAbaixoRangeOk = new ArrayList<Membro>();
		List<Membro> membrosAcimaRangeOk = new ArrayList<Membro>();
		Set<Disponibilidade> disponibilidades = new LinkedHashSet<Disponibilidade>();
		Set<Disponibilidade> disponibilidadesAusentes = new LinkedHashSet<Disponibilidade>();

		// Mapeando membros com qtd de programa��es abaixo da m�dia e membros com qtd de programa��es acima da m�dia.
		for (Membro membro : membros) {
			int qtd = membro.getDiasProgramados().size();
			if (qtd <= rangeInicialOk) {
				membrosAbaixoRangeOk.add(membro);
			} else if (qtd >= rangeFinalOk) {
				membrosAcimaRangeOk.add(membro);
				disponibilidades.add(getSobrecargaMembro(membro));
			}
		}

		// Criando mapa dia SEM_PAO
		for (Entry<Date, String> dia : programacaoPorMembro.entrySet()) {
			// se dia est� sem membro
			if (dia.getValue() == null) {
				int diaSemana = new LocalDate(dia.getKey()).getDayOfWeek();
				disponibilidadesAusentes.add(Disponibilidade.getDisponibilidadeById(diaSemana));
			}
		}

		Collections.sort(membrosAbaixoRangeOk, new MembrosMenosSobrecarregadosComparator());
		Collections.sort(membrosAcimaRangeOk, new MembrosSobrecarregadosComparator());

		// criando DTO
		SugestaoTrocaDisponibilidades sugestao = new SugestaoTrocaDisponibilidades(membrosAcimaRangeOk, membrosAbaixoRangeOk, disponibilidades, disponibilidadesAusentes);

		return sugestao;
	}

	/**
	 * M�todo respos�vel por buscar se h� sobrecarga de programa��es no membro
	 * 
	 * @param membro
	 * @return Disponibilidade com maior indice de sobrecarga
	 */
	private Disponibilidade getSobrecargaMembro(Membro membro) {
		int[] diasSemana = new int[5];

		// levantamento dos dias utilizados
		for (LocalDate data : membro.getDiasProgramados()) {
			int dia = data.dayOfWeek().get();
			diasSemana[dia - 1]++;
		}

		// defini��o do dia com maior qtd de programa��es
		int maiorDisponibilidade = 0;
		int indice = 0;
		for (int i = 0; i < diasSemana.length; i++) {
			if (diasSemana[i] > maiorDisponibilidade) {
				indice = i;
				maiorDisponibilidade = diasSemana[i];
			}
		}
		return Disponibilidade.getDisponibilidadeById(indice + 1);
	}

	/**
	 * A sobrecarga da programa��o equivale ao resumo de programa��es. A Key equivale a qtd de programa��es dos membros, j� o value equivale a lista concatenada de
	 * membros.
	 * 
	 * @return map(key=qtd programa��es / value=membros)
	 */
	private Map<Date, String> processarAlocacaoMembro() {
		Periodo programacao = new Periodo(dataInicial, dataFinal);
		List<Membro> membrosDisponiveis;
		Disponibilidade disponibilidadeProcurada;

		// Calculando programa��o do per�odo avaliado x membros com disponibilidade
		for (Semana semana : programacao.getSemanas()) {

			// iterando os dias da semana
			for (Entry<LocalDate, Membro> dia : semana.getDias().entrySet()) {
				int diaSemana = dia.getKey().dayOfWeek().get();
				disponibilidadeProcurada = Disponibilidade.getDisponibilidadeById(diaSemana);
				membrosDisponiveis = procurarDisponibilidade(disponibilidadeProcurada, membros);

				// ordenando: 1o = quem tiver a menor qtd de programa��es / 2o = quem tiver menor disponibilidade.
				Collections.sort(membrosDisponiveis, new MembrosMenosSobrecarregadosComparator());

				// iterando membros disponiveis para o dia
				for (Membro membroDisponivel : membrosDisponiveis) {
					if (!semana.existeMembro(membroDisponivel)) {
						membroDisponivel.addDiaProgramado(dia.getKey());
						dia.setValue(membroDisponivel);
						break;
					}
				}
			}
		}
		return programacao.extrairCronograma();
	}

	/**
	 * Programa��o completa dos dias �teis com membro disponivel para o dia.
	 * 
	 * @return map(key=dia programa��o / value=membro correspondente)
	 */
	private Map<Integer, String> processarAlocacaoSobrecarga() {
		Map<Integer, StringBuilder> mapaProgramacao = new TreeMap<Integer, StringBuilder>();
		Map<Integer, String> mapaProgramacaoRetorno = new TreeMap<Integer, String>();
		int qtdProgramacoesMembro;
		StringBuilder builder;

		// iterar todos os membros, classificando pela qtd de programa��es
		for (Membro membro : membros) {
			qtdProgramacoesMembro = membro.getDiasProgramados().size();
			if ((builder = mapaProgramacao.get(qtdProgramacoesMembro)) == null) {
				builder = new StringBuilder();
				mapaProgramacao.put(qtdProgramacoesMembro, builder);
			} else
				builder.append(", ");
			builder.append(membro.getNome());
		}

		// Criando mapa de sobrecarga de programa��es
		for (Entry<Integer, StringBuilder> item : mapaProgramacao.entrySet()) {
			mapaProgramacaoRetorno.put(item.getKey(), item.getValue().toString());
		}
		return mapaProgramacaoRetorno;
	}

	/**
	 * M�todo repons�vel por validar o membro tem a disponibilidade informada
	 * 
	 * @param disponibilidade
	 *            disponibilidade buscada
	 * @param membros
	 *            Lista de membros avaliados
	 * @return Membros que cont�m a disponibilidade informada
	 */
	private List<Membro> procurarDisponibilidade(Disponibilidade disponibilidade, List<Membro> membros) {
		List<Membro> membrosComDisponibilidade = new ArrayList<Membro>();
		for (Membro membro : membros) {
			if (membro.getDisponibilidades().contains(disponibilidade)) {
				membrosComDisponibilidade.add(membro);
			}
		}
		return membrosComDisponibilidade;
	}

}

/**
 * Classe respons�vel por criar regra de ordena��o: 1o avaliar membro com menor programa��es efetivadas, 2o avaliar membro com menor qtd de disponibilidades e 3o menor
 * id.
 * 
 * @author bruno.almeida
 * 
 */
class MembrosMenosSobrecarregadosComparator implements Comparator<Membro> {
	@Override
	public int compare(Membro primeiroMembro, Membro segundoMembro) {
		int dias = primeiroMembro.getDiasProgramados().size();
		int diasSegundoMembro = segundoMembro.getDiasProgramados().size();
		int comparacaoDiasProgramados = new Integer(dias).compareTo(diasSegundoMembro);
		if (comparacaoDiasProgramados != 0)
			return comparacaoDiasProgramados;

		int disponibilidade = primeiroMembro.getDisponibilidades().size();
		int disponibilidadeSegundoMembro = segundoMembro.getDisponibilidades().size();

		int comparacaoDisponibilidade = new Integer(disponibilidade).compareTo(disponibilidadeSegundoMembro);
		if (comparacaoDisponibilidade != 0)
			return comparacaoDisponibilidade;

		return Long.valueOf(primeiroMembro.getId()).compareTo(segundoMembro.getId());
	}
}

/**
 * Classe respons�vel por criar regra de ordena��o: 1o avaliar membro com maior qtd de programa��es efetivadas, 2o avaliar membro com maior qtd de disponibilidades e 3o
 * menor id.
 * 
 * @author bruno.almeida
 * 
 */
class MembrosSobrecarregadosComparator implements Comparator<Membro> {

	@Override
	// TODO confirmar ordem
	public int compare(Membro primeiroMembro, Membro segundoMembro) {
		int dias = primeiroMembro.getDiasProgramados().size();
		int diasSegundoMembro = segundoMembro.getDiasProgramados().size();
		int comparacaoDiasProgramados = new Integer(diasSegundoMembro).compareTo(dias);
		if (comparacaoDiasProgramados != 0)
			return comparacaoDiasProgramados;

		int disponibilidade = primeiroMembro.getDisponibilidades().size();
		int disponibilidadeSegundoMembro = segundoMembro.getDisponibilidades().size();

		int comparacaoDisponibilidade = new Integer(disponibilidadeSegundoMembro).compareTo(disponibilidade);
		if (comparacaoDisponibilidade != 0)
			return comparacaoDisponibilidade;

		return Long.valueOf(primeiroMembro.getId()).compareTo(segundoMembro.getId());
	}

}