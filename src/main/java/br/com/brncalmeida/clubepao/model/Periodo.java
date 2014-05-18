package br.com.brncalmeida.clubepao.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.joda.time.LocalDate;

/**
 * Classe respons�ve por representar o per�odo completo para cria��o das programa��es
 * 
 * @author bruno.almeida
 * 
 */
public class Periodo {

	private List<Semana> semanas;

	/**
	 * Construtor default, a partir do range de datas, cria v�rias semanas.
	 * 
	 * @param dataInicial
	 *            data inicial do range que ser� criado
	 * @param dataFinal
	 *            data final do range que ser� criado
	 */
	public Periodo(LocalDate dataInicial, LocalDate dataFinal) {

		// valida��o
		if (dataInicial == null)
			throw new NullPointerException("Data inicial n�o pode ser nula");
		if (dataFinal == null)
			throw new NullPointerException("Data final n�o pode ser nula");

		LocalDate dataInicialTemporaria = new LocalDate(dataInicial);
		LocalDate dataFinalTemporaria = null;

		// enquanto data inicial temporaria for menor que data final, criar semana.
		while (dataInicialTemporaria.isBefore(dataFinal)) {
			if (dataInicialTemporaria.dayOfWeek().get() > 0) {
				dataFinalTemporaria = dataInicialTemporaria.plusDays(7 - dataInicialTemporaria.dayOfWeek().get());
			} else {
				dataFinalTemporaria = dataInicialTemporaria.plusDays(7);
			}
			if (dataFinalTemporaria.isAfter(dataFinal)) {
				dataFinalTemporaria = dataFinal;
			}
			semanas().add(new Semana(dataInicialTemporaria, dataFinalTemporaria));
			dataInicialTemporaria = dataFinalTemporaria.plusDays(1);
		}
	}

	/**
	 * extrar mapa de dias programados x membro alocado
	 * 
	 * @return mapa de dias programados (uteis) x membro alocado
	 */
	public Map<Date, String> extrairCronograma() {
		Map<Date, String> retorno = new TreeMap<Date, String>();
		String membro;
		for (Semana semana : semanas()) {

			// cria��o do mapa
			for (Entry<LocalDate, Membro> dia : semana.getDias().entrySet()) {
				if (dia.getValue() == null)
					membro = null;
				else
					membro = dia.getValue().getNome();
				retorno.put(dia.getKey().toDate(), membro);
			}
		}
		return retorno;
	}

	/**
	 * reseta a programa��o de todos os membros em todas as semanas
	 */
	public void resetarProgramacaoMembros() {
		for (Semana semana : semanas()) {
			semana.resetarProgramacaoMembros();
		}
	}

	/**
	 * valida��o se o membro informado existe no periodo
	 * 
	 * @param membro
	 *            membro a ser buscado
	 * @return true = "ja existe membro neste periodo"
	 */
	public boolean existeMembro(Membro membro) {
		boolean retorno = false;
		for (Semana semana : semanas()) {
			if (semana.existeMembro(membro)) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}

	/**
	 * semanas do periodo
	 * 
	 * @return lista de semanas imutavel
	 */
	public List<Semana> getSemanas() {
		return Collections.unmodifiableList(semanas());
	}

	/**
	 * tratamento para o mapa de semanas
	 * 
	 * @return mapa de semana concreto
	 */
	private List<Semana> semanas() {
		if (semanas == null) {
			this.semanas = new ArrayList<Semana>();
		}
		return semanas;
	}
}
