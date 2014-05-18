package br.com.brncalmeida.clubepao.dao;

import java.util.List;

import br.com.brncalmeida.clubepao.model.Disponibilidade;
import br.com.brncalmeida.clubepao.model.Membro;

/**
 * Data Access Object para a entidade Membro.
 * 
 * @author Bruno Almeida
 */
public interface MembroDao {

	/**
	 * Adiciona um novo membro na base de dados.
	 * 
	 * @param membro
	 *            que ser� adicionado
	 */
	void add(Membro membro);

	/**
	 * Adiciona uma lista de membros na base de dados
	 * 
	 * @param membros
	 *            lista de membros
	 */
	void addAll(List<Membro> membros);

	/**
	 * remove um membro da base
	 * 
	 * @param membro
	 *            membro que sera removido
	 */
	void remove(Membro membro);

	/**
	 * remove uma lista de membros da base
	 * 
	 * @param membros
	 *            lista de membros que sera removida
	 */
	void removeAll(List<Membro> membros);

	/**
	 * atualiza��o do membro
	 * 
	 * @param membro
	 *            membro que ser� atualizado
	 */
	void refresh(Membro membro);

	/**
	 * atualiza��o das informa��es do membro
	 * 
	 * @param membro
	 *            membro que tera suas informa�oes atualizadas
	 */
	void update(Membro membro);

	/**
	 * buscar membro pelo id
	 * 
	 * @param id
	 *            id que ser� buscado
	 * @return membro com id informado
	 */
	Membro getById(long id);

	/**
	 * Retorna uma lista de membros que cont�m a disponibilidade informada
	 * 
	 * @param disponibilidade
	 *            que ser� buscada.
	 * @return lista de membros
	 */
	List<Membro> procurarDisponibilidade(Disponibilidade disponibilidade);

	/**
	 * lista todos os membros da base
	 * 
	 * @return lista com os membros
	 */
	List<Membro> listarTodos();

}