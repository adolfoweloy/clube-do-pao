package br.com.brncalmeida.clubepao.controller;

import java.util.List;

import org.joda.time.LocalDate;

import br.com.brncalmeida.clubepao.dao.MembroDao;
import br.com.brncalmeida.clubepao.model.Membro;
import br.com.brncalmeida.clubepao.model.Schedule;
import br.com.brncalmeida.clubepao.utils.Util;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.core.Localization;
import br.com.caelum.vraptor.validator.ValidationMessage;

/**
 * Controller com finalidade de apresentar e controlar a programa��o de cada um dos membros para a compra dos p�es
 * 
 * @author bruno.almeida
 * 
 */
@Resource
public class ControleController {
	
	// TODO melhorar frase "Para o dia Sex/Ter n�o h� membros cadastrados."
	
	private final Result result;
	private Validator validator;
	private MembroDao dao;
	private Localization localization;

	/**
	 * Construtor default
	 * 
	 * @param result
	 *            response do vraptor
	 * @param dao
	 *            instancia do dao membros
	 * @param localization
	 *            localizacao formatada pelo vraptor
	 * @param validator
	 *            validador controlado pelo vraptor
	 */
	public ControleController(Result result, MembroDao dao, Localization localization, Validator validator) {
		this.result = result;
		this.dao = dao;
		this.localization = localization;
		this.validator = validator;
	}

	/**
	 * pagina incial para a guia de controle, este metodo tamb�m � utilizado ao final de todas as opera�oes deste controller.
	 */
	@Get
	@Path("/controle")
	public void index() {
		result.include("pagina_ativa", "controle");
	}

	/**
	 * Metodo que gera efetivamente a programa��o dos membros. Busca os membros do dao e intercala nos dias �teis do range escolhido
	 * 
	 * @param data
	 *            data inicial da programa��o
	 */
	@SuppressWarnings("deprecation")
	@Post
	@Path("/controle/gerar")
	public void gerar(final String data) {

		// valida��o se existe membros
		List<Membro> membros = dao.listarTodos();
		if (membros.size() == 0) {
			validator.add(new ValidationMessage(Util.getMessage(localization, "nao.existe.membros"), "erro"));
		}

		// valida��o da data informada
		final String pattern = "yyyy-MM-dd";
		LocalDate dataInicial = null;
		try {
			dataInicial = Util.stringToDate(data, pattern);
		} catch (Exception e) {
			validator.add(new ValidationMessage(Util.getMessage(localization, "data.invalida"), "erro"));
		}

		// caso tenha erros, retornar
		validator.onErrorForwardTo(this).index();

		// regra de neg�cio, data final = data inicial + 30 dias
		Schedule cronograma = new Schedule(dataInicial, dataInicial.plusDays(30), membros);

		// exibi��o das informa��es geradas pelo relat�rio "Schedule.class".
		result.include("calendario", cronograma.getProgramacaoPorMembro());
		result.include("sobrecargas", cronograma.getProgramacaoSobrecarga());
		result.include("sugestoes", cronograma.getSugestao());
		result.include("qtdPaes", membros.size());

		// forward incial
		result.forwardTo(this).index();
	}
}