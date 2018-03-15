package br.com.sidtmcafe.controller;

import br.com.sidtmcafe.componentes.AlertMensagem;
import br.com.sidtmcafe.componentes.Tarefa;
import br.com.sidtmcafe.componentes.Variavel;
import br.com.sidtmcafe.database.ConnectionFactory;
import br.com.sidtmcafe.interfaces.FormularioModelo;
import br.com.sidtmcafe.model.dao.*;
import br.com.sidtmcafe.model.model.TabModel;
import br.com.sidtmcafe.model.vo.*;
import br.com.sidtmcafe.service.*;
import br.com.sidtmcafe.view.ViewCadastroProduto;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import static br.com.sidtmcafe.interfaces.Constants.*;

public class ControllerCadastroProduto extends Variavel implements Initializable, FormularioModelo {


    public AnchorPane painelViewCadastroProduto;
    public TitledPane tpnCadastroProduto;
    public JFXTextField txtPesquisaProduto;
    public JFXTreeTableView<TabProdutoVO> ttvProduto;
    public Label lblRegistrosLocalizados;
    public TitledPane tpnDadoCadastral;
    public JFXTextField txtCodigo;
    public JFXTextField txtDescricao;
    public JFXTextField txtPeso;
    public JFXComboBox<SisUnidadeComercialVO> cboUnidadeComercial;
    public JFXComboBox<SisSituacaoSistemaVO> cboSituacaoSistema;
    public JFXTextField txtPrecoFabrica;
    public JFXTextField txtMargem;
    public JFXTextField txtPrecoConsumidor;
    public JFXTextField txtLucroLiquido;
    public JFXTextField txtLucratividade;
    public JFXTextField txtVarejo;
    public JFXTextField txtComissao;
    public Label lblDataCadastro;
    public Label lblDataCadastroDiff;
    public Label lblDataAtualizacao;
    public Label lblDataAtualizacaoDiff;
    public JFXListView<TabProdutoEanVO> listCodigoBarras;
    public JFXTextField txtFiscalNcm;
    public JFXTextField txtFiscalGenero;
    public JFXTextField txtFiscalCest;
    public JFXComboBox<SisFiscalCstOrigemVO> cboFiscalOrigem;
    public JFXComboBox<SisFiscalCstIcmsVO> cboFiscalIcms;
    public JFXComboBox<SisFiscalCstPisCofinsVO> cboFiscalPis;
    public JFXComboBox<SisFiscalCstPisCofinsVO> cboFiscalCofins;

    @Override
    public void fechar() {

    }

    @Override
    public void preencherObjetos() {
        listaTarefas = new ArrayList<>();
        criarTabelas();
        carregaListas();
        preencherCombos();
        preencherTabelas();

        new Tarefa().tarefaAbreCadastroProduto(this, listaTarefas);

        PersonalizarCampo.fieldMaxLen(painelViewCadastroProduto);
        PersonalizarCampo.maskFields(painelViewCadastroProduto);
        PersonalizarCampo.clearField(painelViewCadastroProduto);
    }

    @Override
    public void fatorarObjetos() {

    }

    @Override
    public void escutarTeclas() {
        String tituloTab = ViewCadastroProduto.getTituloJanela();

        ttvProduto.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (n == null) return;
            setTtvProdutoVO(n.getValue());
            exibirDadosProduto();
        });


        ControllerPrincipal.ctrlPrincipal.tabPaneViewPrincipal.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (!(ControllerPrincipal.ctrlPrincipal.getTabAtual().equals(tituloTab))) {
                return;
            }
            if ((n != null) & (n != o))
                setStatusBarFormulario(getStatusFormulario());
        });

        eventCadastroProduto = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (!(ControllerPrincipal.ctrlPrincipal.getTabAtual().equals(tituloTab)))
                    return;
                switch (event.getCode()) {
                    case F1:
                        if (!getStatusBarFormulario().contains(event.getCode().toString())) break;
                        setStatusFormulario("Incluir");
                        setTtvProdutoVO(new TabProdutoVO());
                        break;
                    case F2:
                        if (!getStatusBarFormulario().contains(event.getCode().toString())) break;

                        salvarProduto();
                        setTtvProdutoVO(new TabProdutoDAO().getProdutoVO(getTtvProdutoVO().getId()));
                        produtoVOObservableList.set(indexObservableProduto, getTtvProdutoVO());

                        setStatusFormulario("Pesquisa");
                        carregarPesquisaProduto(txtPesquisaProduto.getText());
                        break;
                    case F3:
                        if (!getStatusBarFormulario().contains(event.getCode().toString())) break;
                        switch (getStatusFormulario().toLowerCase()) {
                            case "incluir":
                                if (new AlertMensagem("Cancelar inclusão", USUARIO_LOGADO_APELIDO
                                        + ", deseja cancelar inclusão no cadastro de produto?",
                                        "ic_cadastro_empresas_white_24dp.png").getRetornoAlert_YES_NO().get() == ButtonType.NO)
                                    return;
                                PersonalizarCampo.clearField((AnchorPane) tpnDadoCadastral.getContent());
                                break;
                            case "editar":
                                if (new AlertMensagem("Cancelar edição", USUARIO_LOGADO_APELIDO
                                        + ", deseja cancelar edição do cadastro de produto?",
                                        "ic_cadastro_empresas_white_24dp.png").getRetornoAlert_YES_NO().get() == ButtonType.NO)
                                    return;
                                setTtvProdutoVO(new TabProdutoDAO().getProdutoVO(getTtvProdutoVO().getId()));
                                produtoVOObservableList.set(indexObservableProduto, getTtvProdutoVO());
                                break;
                        }
                        setStatusFormulario("Pesquisa");
                        carregarPesquisaProduto(txtPesquisaProduto.getText());
                        exibirDadosProduto();
                        break;
                    case F4:
                        if ((!getStatusBarFormulario().contains(event.getCode().toString())) || (getTtvProdutoVO() == null))
                            break;
                        indexObservableProduto = produtoVOObservableList.indexOf(getTtvProdutoVO());
                        setStatusFormulario("Editar");
                        break;
                    case F5:
                        if (!getStatusBarFormulario().contains(event.getCode().toString())) break;

                        salvarProduto();
                        setTtvProdutoVO(new TabProdutoDAO().getProdutoVO(getTtvProdutoVO().getId()));
                        produtoVOObservableList.set(indexObservableProduto, getTtvProdutoVO());

                        setStatusFormulario("Pesquisa");
                        carregarPesquisaProduto(txtPesquisaProduto.getText());
                        exibirDadosProduto();
                        ttvProduto.requestFocus();
                        break;
                    case F6:
                        if (getStatusFormulario().toLowerCase().equals("pesquisa") || (!event.isShiftDown())) break;
                        keyShiftF6();
                        break;
                    case F7:
                        if (!getStatusBarFormulario().contains(event.getCode().toString())) break;
                        txtPesquisaProduto.requestFocus();
                        break;
                    case F12:
                        if (!getStatusBarFormulario().contains(event.getCode().toString())) break;
                        fecharTab(tituloTab);
                        break;
                    case HELP:
                        if (getStatusFormulario().toLowerCase().equals("pesquisa")) return;
                        keyInsert();
                        break;
                    case DELETE:
                        if (getStatusFormulario().toLowerCase().equals("pesquisa")) return;
                        keyDelete();
                        break;
                }
            }
        };

        ControllerPrincipal.ctrlPrincipal.painelViewPrincipal.addEventHandler(KeyEvent.KEY_PRESSED, eventCadastroProduto);

        txtPesquisaProduto.textProperty().addListener((ov, o, n) -> {
            carregarPesquisaProduto(n);
        });

        txtPesquisaProduto.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                ttvProduto.requestFocus();
                ttvProduto.getSelectionModel().select(0);
            }
        });

        txtPrecoFabrica.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    campoFab = true;
                    campoMargem = false;
                    campoCons = false;
                }
            }
        });

        txtMargem.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    campoFab = false;
                    campoMargem = true;
                    campoCons = false;
                }
            }
        });

        txtPrecoConsumidor.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    campoFab = false;
                    campoMargem = false;
                    campoCons = true;
                }
            }
        });

        txtPrecoFabrica.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String o, String n) {
                if (campoFab)
                    vlrConsumidor();
            }
        });

        txtMargem.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String o, String n) {
                if (campoMargem)
                    vlrConsumidor();
            }
        });

        txtPrecoConsumidor.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String o, String n) {
                if (campoCons)
                    vlrMargem();
            }
        });

    }

    @SuppressWarnings("Duplicates")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        preencherObjetos();
        fatorarObjetos();
        escutarTeclas();
        setStatusFormulario("Pesquisa");
        Platform.runLater(() -> {
            painelViewCadastroProduto.fireEvent(ExecutaComandoTecladoMouse.pressTecla(KeyCode.F7));
        });
    }

    EventHandler<KeyEvent> eventCadastroProduto;

    int indexObservableProduto = 0;
    TabProdutoVO ttvProdutoVO;
    List<TabProdutoEanVO> ttvProdutoEanVO;

    List<Pair> listaTarefas;

    ObservableList<TabProdutoVO> produtoVOObservableList;
    FilteredList<TabProdutoVO> produtoVOFilteredList;
    List<TabProdutoEanVO> deletadosProdutoEanVOList;

    boolean campoFab = false;
    boolean campoMargem = false;
    boolean campoCons = false;

    int qtdRegistrosLocalizados = 0;
    String statusFormulario, statusBarFormulario;

    static String STATUSBARPESQUISA = "[F1-Novo]  [F3-Excluir]  [F4-Editar]  [F7-Pesquisar]  [F8-Filtro pesquisa]  [F12-Sair]  ";
    static String STATUSBAREDITAR = "[F3-Cancelar edição]  [F5-Atualizar]  ";
    static String STATUSBARINCLUIR = "[F2-Incluir]  [F3-Cancelar inclusão]  ";

    public TabProdutoVO getTtvProdutoVO() {
        return ttvProdutoVO;
    }

    public void setTtvProdutoVO(TabProdutoVO ttvProdutoVO) {
        if (ttvProdutoVO == null)
            ttvProdutoVO = new TabProdutoVO();
        this.ttvProdutoVO = ttvProdutoVO;

        setTtvProdutoEanVO(getTtvProdutoVO().getProdutoEanVOList());
    }

    public List<TabProdutoEanVO> getTtvProdutoEanVO() {
        return ttvProdutoEanVO;
    }

    public void setTtvProdutoEanVO(List<TabProdutoEanVO> ttvProdutoEanVO) {
        if (ttvProdutoEanVO == null)
            ttvProdutoEanVO = new ArrayList<>();
        this.ttvProdutoEanVO = ttvProdutoEanVO;
    }

    public int getQtdRegistrosLocalizados() {
        return qtdRegistrosLocalizados;
    }

    public void setQtdRegistrosLocalizados(int qtdRegistrosLocalizados) {
        this.qtdRegistrosLocalizados = qtdRegistrosLocalizados;
        atualizaLblRegistrosLocalizados();
    }

    public String getStatusFormulario() {
        return statusFormulario;
    }

    public void setStatusFormulario(String statusFormulario) {
        this.statusFormulario = statusFormulario;
        atualizaLblRegistrosLocalizados();
        setStatusBarFormulario(statusFormulario);
    }

    public String getStatusBarFormulario() {
        return statusBarFormulario;
    }

    public void setStatusBarFormulario(String statusFormulario) {
        if (statusFormulario.toLowerCase().contains("incluir")) {
            PersonalizarCampo.desabilitaCampos((AnchorPane) tpnCadastroProduto.getContent(), true);
            PersonalizarCampo.desabilitaCampos((AnchorPane) tpnDadoCadastral.getContent(), false);
            PersonalizarCampo.clearField((AnchorPane) tpnDadoCadastral.getContent());
            txtCodigo.requestFocus();
            txtCodigo.selectAll();
            this.statusBarFormulario = STATUSBARINCLUIR;
        } else if (statusFormulario.toLowerCase().contains("editar")) {
            PersonalizarCampo.desabilitaCampos((AnchorPane) tpnCadastroProduto.getContent(), true);
            PersonalizarCampo.desabilitaCampos((AnchorPane) tpnDadoCadastral.getContent(), false);
            txtLucroLiquido.setDisable(true);
            txtLucratividade.setDisable(true);
            txtCodigo.requestFocus();
            txtCodigo.selectAll();
            this.statusBarFormulario = STATUSBAREDITAR;
        } else if (statusFormulario.toLowerCase().contains("pesquisa")) {
            PersonalizarCampo.desabilitaCampos((AnchorPane) tpnCadastroProduto.getContent(), false);
            PersonalizarCampo.desabilitaCampos((AnchorPane) tpnDadoCadastral.getContent(), true);
            txtPesquisaProduto.requestFocus();
            this.statusBarFormulario = STATUSBARPESQUISA;
        }
        ControllerPrincipal.ctrlPrincipal.atualizarTeclasStatusBar(statusBarFormulario);
    }

    void atualizaLblRegistrosLocalizados() {
        lblRegistrosLocalizados.setText("[" + getStatusFormulario() + "]  " + String.valueOf(getQtdRegistrosLocalizados()) + " registro(s) localizado(s).");
    }

    void criarTabelas() {
        listaTarefas.add(new Pair("criarTabelaProduto", "criando tabela produto"));
    }

    void carregaListas() {
        listaTarefas.add(new Pair("carregarListaProduto", "carregando lista de produtos"));
    }

    void preencherCombos() {
        listaTarefas.add(new Pair("preencherCboUndCom", "preenchendo dados unidade comercial"));
        listaTarefas.add(new Pair("preencherCboSituacaoSistema", "preenchendo situaão no istema"));
        listaTarefas.add(new Pair("preencherCboFiscalOrigem", "preenchendo dados fiscais de Origem"));
        listaTarefas.add(new Pair("preencherCboFiscalIcms", "preenchendo dados fiscal ICMS"));
        listaTarefas.add(new Pair("preencherCboFiscalPis", "preenchendo dados fiscal PIS"));
        listaTarefas.add(new Pair("preencherCboFiscalCofins", "preenchendo dados fiscal COFINS"));
    }

    void preencherTabelas() {
        listaTarefas.add(new Pair("preencherTabelaProduto", "preenchendo tabela produto"));
    }

    public void carregarListaProduto() {
        produtoVOObservableList = FXCollections.observableArrayList(new TabProdutoDAO().getProdutoVOList());
    }

    public void preencherCboUndCom() {
        cboUnidadeComercial.getItems().clear();
        cboUnidadeComercial.getItems().add(new SisUnidadeComercialVO());
        cboUnidadeComercial.getItems().addAll(new SisUnidadeComercialDAO().getUnidadeComercialVOList());
        cboUnidadeComercial.getSelectionModel().select(0);
    }

    public void preencherCboSituacaoSistema() {
        cboSituacaoSistema.getItems().clear();
        cboSituacaoSistema.getItems().add(new SisSituacaoSistemaVO());
        cboSituacaoSistema.getItems().addAll(new SisSituacaoSistemaDAO().getSituacaoSistemaVOList());
        cboSituacaoSistema.getSelectionModel().select(0);
    }

    public void preencherCboFiscalOrigem() {
        cboFiscalOrigem.getItems().clear();
        cboFiscalOrigem.getItems().addAll(new SisFiscalCstOrigemDAO().getFiscalCstOrigemVOList());
        cboFiscalOrigem.getSelectionModel().select(0);
    }

    public void preencherCboFiscalIcms() {
        cboFiscalIcms.getItems().clear();
        cboFiscalIcms.getItems().add(new SisFiscalCstIcmsVO());
        cboFiscalIcms.getItems().addAll(new SisFiscalCstIcmsDAO().getFiscalCstIcmsVOList());
        cboFiscalIcms.getSelectionModel().select(0);
    }

    public void preencherCboFiscalPis() {
        cboFiscalPis.getItems().clear();
        cboFiscalPis.getItems().add(new SisFiscalCstPisCofinsVO());
        cboFiscalPis.getItems().addAll(new SisFiscalCstPisCofinsDAO().getSisFiscalCstPisCofinsVOList());
        cboFiscalPis.getSelectionModel().select(0);
    }

    public void preencherCboFiscalCofins() {
        cboFiscalCofins.getItems().clear();
        cboFiscalCofins.getItems().add(new SisFiscalCstPisCofinsVO());
        cboFiscalCofins.getItems().addAll(new SisFiscalCstPisCofinsDAO().getSisFiscalCstPisCofinsVOList());
        cboFiscalCofins.getSelectionModel().select(0);
    }

    public void preencherTabelaProduto() {
        try {
            if (produtoVOFilteredList == null)
                carregarPesquisaProduto(txtPesquisaProduto.getText());
            final TreeItem<TabProdutoVO> root = new RecursiveTreeItem<TabProdutoVO>(produtoVOFilteredList, RecursiveTreeObject::getChildren);
            ttvProduto.getColumns().setAll(TabModel.getColunaIdProduto(), TabModel.getColunaCodigo(),
                    TabModel.getColunaDescricao(), TabModel.getColunaUndCom(), TabModel.getColunaVarejo(),
                    TabModel.getColunaPrecoFabrica(), TabModel.getColunaPrecoConsumidor(),
                    TabModel.getColunaSituacaoSistema(), TabModel.getColunaQtdEstoque());
            ttvProduto.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            ttvProduto.setRoot(root);
            ttvProduto.setShowRoot(false);
            setQtdRegistrosLocalizados(produtoVOFilteredList.size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void preencherListaProdutoEan() {
        listCodigoBarras.getItems().clear();
        if (deletadosProdutoEanVOList != null)
            deletadosProdutoEanVOList.clear();
        if (getTtvProdutoEanVO() != null)
            listCodigoBarras.getItems().setAll(getTtvProdutoEanVO());
        listCodigoBarras.getSelectionModel().select(0);
    }

    void carregarPesquisaProduto(String strPesq) {
        String busca = strPesq.toLowerCase().trim();

        produtoVOFilteredList = new FilteredList<TabProdutoVO>(produtoVOObservableList, produto -> true);
        produtoVOFilteredList.setPredicate(produto -> {
            if (produto.getDescricao().toLowerCase().contains(busca)) return true;
            if (produto.getCodigo().toLowerCase().contains(busca)) return true;
            if (produto.getProdutoEanVOList().size() > 0)
                for (TabProdutoEanVO prodEan : produto.getProdutoEanVOList())
                    if (prodEan.getDescricao().toLowerCase().contains(busca)) return true;
            return false;
        });
        preencherTabelaProduto();
    }

    void exibirDadosProduto() {
        if (getTtvProdutoVO() == null) return;
        txtCodigo.setText(getTtvProdutoVO().getCodigo());
        txtDescricao.setText(getTtvProdutoVO().getDescricao());
        txtPeso.setText(PESO_FORMAT.format(getTtvProdutoVO().getPeso()).replace(".", ","));
        cboUnidadeComercial.getSelectionModel().select(getTtvProdutoVO().getUnidadeComercialVO());
        cboSituacaoSistema.getSelectionModel().select(getTtvProdutoVO().getSituacaoSistemaVO());
        txtPrecoFabrica.setText(DECIMAL_FORMAT.format(getTtvProdutoVO().getPrecoFabrica()).replace(".", ","));
        txtPrecoConsumidor.setText(DECIMAL_FORMAT.format(getTtvProdutoVO().getPrecoConsumidor()).replace(".", ","));
        txtMargem.setText("0,00");
        txtLucroLiquido.setText("0,00");
        txtLucratividade.setText("0,00");
        vlrMargem();
        txtVarejo.setText(String.valueOf(getTtvProdutoVO().getVarejo()));
        txtComissao.setText(DECIMAL_FORMAT.format(getTtvProdutoVO().getComissao()).replace(".", ","));
        txtFiscalNcm.setText(FormatarDado.getCampoFormatado(getTtvProdutoVO().getFiscalNcm(), "ncm"));
        txtFiscalCest.setText(FormatarDado.getCampoFormatado(getTtvProdutoVO().getFiscalCest(), "cest"));
        txtFiscalGenero.setText(getTtvProdutoVO().getFiscalGenero());
        cboFiscalOrigem.getSelectionModel().select(getTtvProdutoVO().getFiscalCstOrigemVO());
        cboFiscalIcms.getSelectionModel().select(getTtvProdutoVO().getFiscalCstIcmsVO());
        cboFiscalPis.getSelectionModel().select(getTtvProdutoVO().getFiscalCstPisVO());
        cboFiscalCofins.getSelectionModel().select(getTtvProdutoVO().getFiscalCstCofinsVO());

        LocalDateTime ldtCadastro = getTtvProdutoVO().getDataCadastro().toLocalDateTime();
        lblDataCadastro.setText("data cadastro: " + ldtCadastro.format(DTF_DATAHORA) + " [" + getTtvProdutoVO().getUsuarioCadastroVO().getApelido() + "]");
        lblDataCadastroDiff.setText("tempo de cadastro: " + DataTrabalhada.getStrIntervaloDatas(ldtCadastro.toLocalDate(), null));
        lblDataAtualizacao.setText("");
        lblDataAtualizacaoDiff.setText("");
        if (getTtvProdutoVO().getDataAtualizacao() != null) {
            LocalDateTime ldtAtualizacao = getTtvProdutoVO().getDataAtualizacao().toLocalDateTime();
            lblDataAtualizacao.setText("data atualização: " + ldtAtualizacao.format(DTF_DATAHORA) + " [" + getTtvProdutoVO().getUsuarioAtualizacaoVO().getApelido() + "]");
            lblDataAtualizacaoDiff.setText("tempo de atualização: " + DataTrabalhada.getStrIntervaloDatas(ldtAtualizacao.toLocalDate(), null));
        }

        preencherListaProdutoEan();
    }

    TabProdutoEanVO addProdutoEan() {
        String produto = "o produto: " + txtDescricao.getText();
        String ico = "ic_web_email_white_24dp.png";
        String codigoBarras;
        try {
            codigoBarras = new AlertMensagem("Adicionar dados [Código de Barras]",
                    USUARIO_LOGADO_APELIDO + ", qual o código de barras a ser adicionado para " + produto + " ?",
                    ico).getRetornoAlert_TextField(FormatarDado.gerarMascara("", 13, "#"), "").get();
        } catch (Exception ex) {
            //if (!(ex instanceof NoSuchElementException))
            ex.printStackTrace();
            return null;
        }
        if (codigoBarras == null) return null;
        TabProdutoEanVO produtoEanVO = new TabProdutoEanVO();
        produtoEanVO.setId(0);
        produtoEanVO.setProduto_id(0);
        produtoEanVO.setDescricao(codigoBarras);

        return produtoEanVO;
    }

    TabProdutoEanVO editProdutoEan(TabProdutoEanVO produtoEanVO) {
        String produto = "o produto: " + txtDescricao.getText();
        String ico = "ic_web_email_white_24dp.png";
        String codigoBarras;
        try {
            codigoBarras = new AlertMensagem("Editar informações [Código de Barras]",
                    USUARIO_LOGADO_APELIDO + ", qual alteração será feita no código de barras d" + produto + " ?",
                    ico).getRetornoAlert_TextField(FormatarDado.gerarMascara("", 13, "#"),
                    produtoEanVO.getDescricao()).get();
        } catch (Exception ex) {
            if (!(ex instanceof NoSuchElementException))
                ex.printStackTrace();
            return null;
        }
        if (codigoBarras == null) return produtoEanVO;
        produtoEanVO.setDescricao(codigoBarras);

        return produtoEanVO;
    }

    void vlrConsumidor() {
        Double prcFab, marg, prcCon;
        prcFab = FormatarDado.getDoubleValorCampo(txtPrecoFabrica.getText());
        marg = FormatarDado.getDoubleValorCampo(txtMargem.getText());
        prcCon = ((prcFab * (marg + 100)) / 100);
        txtPrecoConsumidor.setText(DECIMAL_FORMAT.format(prcCon).replace(".", ","));
        vlrLucroLiq();
        vlrLucratividade();
    }

    void vlrMargem() {
        Double prcFab, marg, prcCon;
        prcFab = FormatarDado.getDoubleValorCampo(txtPrecoFabrica.getText());
        prcCon = FormatarDado.getDoubleValorCampo(txtPrecoConsumidor.getText());
        marg = (((prcCon * 100) / prcFab) - 100);
        txtMargem.setText(DECIMAL_FORMAT.format(marg).replace(".", ","));
        vlrLucroLiq();
        vlrLucratividade();
    }

    void vlrLucroLiq() {
        Double prcFab, prcCon, luc;
        prcFab = FormatarDado.getDoubleValorCampo(txtPrecoFabrica.getText());
        prcCon = FormatarDado.getDoubleValorCampo(txtPrecoConsumidor.getText());
        luc = prcCon - prcFab;
        txtLucroLiquido.setText(DECIMAL_FORMAT.format(luc).replace(".", ","));
    }

    void vlrLucratividade() {
        Double prcCon, luc, lucratividade;
        prcCon = FormatarDado.getDoubleValorCampo(txtPrecoConsumidor.getText());
        luc = FormatarDado.getDoubleValorCampo(txtLucroLiquido.getText());
        lucratividade = ((luc * 100) / prcCon);
        txtLucratividade.setText(DECIMAL_FORMAT.format(lucratividade).replace(".", ","));
    }

    void keyInsert() {
        if (listCodigoBarras.isFocused()) {
            TabProdutoEanVO produtoEanVO;
            if ((produtoEanVO = addProdutoEan()) == null) return;
            getTtvProdutoEanVO().add(produtoEanVO);
            listCodigoBarras.getItems().add(produtoEanVO);
            listCodigoBarras.getSelectionModel().selectLast();

            listaTarefas = new ArrayList<>();
            listaTarefas.add(new Pair("pesquisaEan", "Pesquisando dados Ean: [" + produtoEanVO.getDescricao() + "]"));

            WsEanCosmosVO wsEanCosmosVO = new Tarefa().tarefaWsEanCosmos(listaTarefas);

            if (wsEanCosmosVO == null) return;
            txtDescricao.setText(wsEanCosmosVO.getDescricao());
            txtFiscalNcm.setText(FormatarDado.getCampoFormatado(wsEanCosmosVO.getNcm(), "ncm"));
        }
    }

    void keyDelete() {
        if ((listCodigoBarras.isFocused()) && (listCodigoBarras.getSelectionModel().getSelectedIndex() >= 0)) {
            if (deletadosProdutoEanVOList == null)
                deletadosProdutoEanVOList = new ArrayList<>();
            deletadosProdutoEanVOList.add(listCodigoBarras.getSelectionModel().getSelectedItem());
            getTtvProdutoEanVO().remove(listCodigoBarras.getSelectionModel().getSelectedItem());
            listCodigoBarras.getItems().remove(listCodigoBarras.getSelectionModel().getSelectedItem());
        }
    }

    void keyShiftF6() {
        if ((listCodigoBarras.isFocused()) && (listCodigoBarras.getSelectionModel().getSelectedIndex() >= 0)) {
            TabProdutoEanVO produtoEanVO = listCodigoBarras.getSelectionModel().getSelectedItem();
            int index = getTtvProdutoEanVO().indexOf(produtoEanVO);
            produtoEanVO = editProdutoEan(produtoEanVO);
            getTtvProdutoEanVO().set(index, produtoEanVO);
            listCodigoBarras.getItems().set(listCodigoBarras.getSelectionModel().getSelectedIndex(), produtoEanVO);
            listCodigoBarras.getSelectionModel().selectLast();
        }
    }

    TabProdutoVO guardarProduto() {
        TabProdutoVO prod = getTtvProdutoVO();
        prod.setCodigo(txtCodigo.getText());
        prod.setDescricao(txtDescricao.getText());
        prod.setPeso(Double.parseDouble(txtPeso.getText().replace(",", ".")));
        prod.setUnidadeComercial_id(((SisUnidadeComercialVO) cboUnidadeComercial.getSelectionModel().getSelectedItem()).getId());
        prod.setUnidadeComercialVO(cboUnidadeComercial.getSelectionModel().getSelectedItem());
        prod.setSituacaoSistema_id(((SisSituacaoSistemaVO) cboSituacaoSistema.getSelectionModel().getSelectedItem()).getId());
        prod.setSituacaoSistemaVO(cboSituacaoSistema.getSelectionModel().getSelectedItem());
        prod.setPrecoFabrica(Double.parseDouble(txtPrecoFabrica.getText().replace(",", ".")));
        prod.setPrecoConsumidor(Double.parseDouble(txtPrecoConsumidor.getText().replace(",", ".")));
        prod.setVarejo(Integer.parseInt(txtVarejo.getText()));
        prod.setComissao(Double.parseDouble(txtComissao.getText().replace(",", ".")));
        prod.setFiscalCstIcms_id(((SisFiscalCstIcmsVO) cboFiscalIcms.getSelectionModel().getSelectedItem()).getId());
        prod.setFiscalCstIcmsVO(cboFiscalIcms.getSelectionModel().getSelectedItem());
        prod.setFiscalCstPis_id(((SisFiscalCstPisCofinsVO) cboFiscalPis.getSelectionModel().getSelectedItem()).getId());
        prod.setFiscalCstPisVO(cboFiscalPis.getSelectionModel().getSelectedItem());
        prod.setFiscalCstCofins_id(((SisFiscalCstPisCofinsVO) cboFiscalCofins.getSelectionModel().getSelectedItem()).getId());
        prod.setFiscalCstCofinsVO(cboFiscalCofins.getSelectionModel().getSelectedItem());
        prod.setFiscalNcm(txtFiscalNcm.getText().replaceAll("[.]", ""));
        prod.setFiscalCest(txtFiscalCest.getText().replaceAll("[.]", ""));
        prod.setFiscalOrigem_id(((SisFiscalCstOrigemVO) cboFiscalOrigem.getSelectionModel().getSelectedItem()).getId());
        prod.setFiscalCstOrigemVO(cboFiscalOrigem.getSelectionModel().getSelectedItem());
        prod.setFiscalGenero(txtFiscalGenero.getText());
        prod.setUsuarioCadastro_id(Integer.parseInt(USUARIO_LOGADO_ID));
        prod.setUsuarioAtualizacao_id(Integer.parseInt(USUARIO_LOGADO_ID));

        return prod;
    }

    public void salvarProduto() {
        Connection conn = ConnectionFactory.getConnection();
        try {
            conn.setAutoCommit(false);

            setTtvProdutoVO(guardarProduto());
            int idProduto = 0;
            if ((idProduto = getTtvProdutoVO().getId()) == 0) {
                idProduto = new TabProdutoDAO().insertTabProdutoVO(conn, getTtvProdutoVO());
            } else {
                new TabProdutoDAO().updateTabProdutoVO(conn, getTtvProdutoVO());
            }

            if (getTtvProdutoEanVO().size() > 0)
                for (TabProdutoEanVO prodEanVO : getTtvProdutoEanVO())
                    if (prodEanVO.getId() == 0) {
                        prodEanVO.setProduto_id(idProduto);
                        new TabProdutoEanDAO().insertTabProdutoEanVO(conn, prodEanVO);
                    } else {
                        prodEanVO.setProduto_id(idProduto);
                        new TabProdutoEanDAO().updateTabProdutoEanVO(conn, prodEanVO);
                    }

            if (deletadosProdutoEanVOList != null)
                if (deletadosProdutoEanVOList.size() > 0)
                    for (TabProdutoEanVO prodEanVO : deletadosProdutoEanVOList)
                        if (prodEanVO.getId() > 0)
                            new TabProdutoEanDAO().deleteTabProdutoEanVO(conn, prodEanVO);

            conn.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            ConnectionFactory.closeConnection(conn);
        }
    }

    void fecharTab(String tituloTab) {
        for (int i = 0; i < ControllerPrincipal.ctrlPrincipal.tabPaneViewPrincipal.getTabs().size(); i++)
            if (ControllerPrincipal.ctrlPrincipal.tabPaneViewPrincipal.getTabs().get(i).getText().toLowerCase().equals(tituloTab.toLowerCase())) {
                ControllerPrincipal.ctrlPrincipal.fecharTab(i);
                ControllerPrincipal.ctrlPrincipal.painelViewPrincipal.removeEventHandler(KeyEvent.KEY_PRESSED, eventCadastroProduto);
            }
    }

}
