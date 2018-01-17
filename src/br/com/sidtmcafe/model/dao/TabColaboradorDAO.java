package br.com.sidtmcafe.model.dao;

import br.com.sidtmcafe.database.ConnectionFactory;
import br.com.sidtmcafe.model.vo.TabColaboradorVO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TabColaboradorDAO extends BuscaBandoDados {

    ResultSet rs;

    String comandoSql = "";
    TabColaboradorVO colaboradorVO;
    List<TabColaboradorVO> colaboradorVOList;

    public TabColaboradorVO getColaboradorVO(int idTabColaboradorVO) {
        buscaTabColaboradorVO(idTabColaboradorVO);
        if (colaboradorVO == null)
            colaboradorVO = new TabColaboradorVO();
        return colaboradorVO;
    }

    public List<TabColaboradorVO> getColaboradorVOList() {
        buscaTabColaboradorVO(0);
        if (colaboradorVOList == null)
            colaboradorVOList.add(new TabColaboradorVO());
        return colaboradorVOList;
    }

    void buscaTabColaboradorVO(int idTabColaboradorVO) {
        comandoSql = "SELECT * FROM tabColaborador ";
        if (idTabColaboradorVO > 0) comandoSql += " WHERE id = '" + idTabColaboradorVO + "' ";
        comandoSql += "ORDER BY nome ";

        colaboradorVOList = new ArrayList<>();
        rs = getResultadosBandoDados(comandoSql);
        try {
            while (rs.next()) {
                colaboradorVO = new TabColaboradorVO();
                colaboradorVO.setId(rs.getInt("id"));
                colaboradorVO.setNome(rs.getString("nome"));
                colaboradorVO.setApelido(rs.getString("apelido"));
                colaboradorVO.setSenha(rs.getString("senha"));
                colaboradorVO.setSenhaSalt(rs.getString("senhaSalt"));

                colaboradorVO.setCargo_id(rs.getInt("cargo_id"));
                colaboradorVO.setCargoVO(new TabCargoDAO().getCargoVO(colaboradorVO.getCargo_id()));

                colaboradorVO.setLoja_id(rs.getInt("loja_id"));
                colaboradorVO.setLojaVO(new TabLojaDAO().getLojaVO(colaboradorVO.getLoja_id()));

                colaboradorVO.setSituacaoSistema_id(rs.getInt("situacaoSistema_id"));
                colaboradorVO.setEndereco_ids(rs.getString("endereco_ids"));
                colaboradorVO.setTelefone_ids(rs.getString("telefone_ids"));
                colaboradorVO.setContato_ids(rs.getString("contato_ids"));
                colaboradorVO.setEmailHomePage_ids(rs.getString("emailHomePage_ids"));

                colaboradorVOList.add(colaboradorVO);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            ConnectionFactory.closeConnection(con, stmt, rs);
        }
    }
}