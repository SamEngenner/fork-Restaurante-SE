package com.forksystem.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.forksystem.dao.ICategoria;
import com.forksystem.dao.IMesa;
import com.forksystem.dao.IPedido;
import com.forksystem.dao.IProduto;
import com.forksystem.dao.IReserva;
import com.forksystem.entities.Categoria;
import com.forksystem.entities.ItemPedido;
import com.forksystem.entities.Mesa;
import com.forksystem.entities.Pedido;
import com.forksystem.entities.Produto;
import com.forksystem.entities.Reserva;
import com.forksystem.entities.StatusPedido;
import com.forksystem.entities.TipoPagamentos;
import com.forksystem.models.TableModelPedido;
import com.forksystem.ui.ViewMesaPedido;
import com.forksystem.utils.Context;
import com.forksystem.utils.FacturaMesa;

public class MesaPedidoController {

	public ViewMesaPedido gui;
	public ICategoria category = null;
	public IProduto produto = null;
	public IMesa mesa = null;
	public IPedido pedidoDAo = null;
	public IReserva reservaDao = null;
	private Long idPedido = null;
	private Long idReserva = null;
	Map<String, JButton> buttons;

	public TableModelPedido tabela;
	public java.util.Vector<String> dados = new java.util.Vector<String>();
	Boolean control = false;
	TableRowSorter<TableModel> sorter = null;
	private Mesa idMesa = null;

	JButton btn;
	JButton btnProd;
	JLabel label;
	String iconPath = "/img/mesa/mesaop.png";
	String iconPath1 = "/img/mesa/mesaof.png";
	String padrao = "/img/mesa/not.jpg";

	public MesaPedidoController(String m) {
		gui = new ViewMesaPedido(m);
		buttons = new HashMap<String, JButton>();

		category = Context.getInstace().getContexto().getBean(ICategoria.class);
		produto = Context.getInstace().getContexto().getBean(IProduto.class);
		mesa = Context.getInstace().getContexto().getBean(IMesa.class);
		pedidoDAo = Context.getInstace().getContexto().getBean(IPedido.class);
		reservaDao = Context.getInstace().getContexto().getBean(IReserva.class);
		mostrar();
		setIdMesa();
		initializeTable();
		gui.ouvinte(new OuvirObjecto(), gui.getBtnAdicionar());
		gui.ouvinte(new OuvirObjecto(), gui.getBtnExluir());
		gui.ouvinte(new OuvirObjecto(), gui.getBtnConfirmar());
		gui.ouvinte(new OuvirObjecto(), gui.getBtnCancelar());
		gui.ouvinte(new OuvirObjecto(), gui.getBtnImprimir());
		gui.ouvinte(new OuvirObjecto(), gui.getBtnCalcularTotal());
		gui.getBtnImprimir().setEnabled(false);
		gui.getBtnConfirmar().setEnabled(false);

	}

	/*
	 * Metodo para setar o id da mesa em contexto
	 */
	public void setIdMesa() {
		setIdMesa(mesa.findByNome(gui.getLblMesa().getText()));

	}

	public void initializeTable() {
		if (null == pedidoDAo.findByMesa(getIdMesa()) || null == reservaDao.findByMesa(getIdMesa())) {
			List<ItemPedido> item = new ArrayList<ItemPedido>();
			tabela = new TableModelPedido(item, gui.getTable());
			gui.getTable().setModel(tabela);
			gui.getTable().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			System.out.println(gui.getLblMesa().getText());
			System.out.println(mesa.findByNome(gui.getLblMesa().getText()));
		} else {
			mostrarDadosTabela();

		}
	}

	public void mostrarDadosTabela() {
		Pedido pedido = pedidoDAo.findByMesa(mesa.findByNome(gui.getLblMesa().getText()));
		tabela = new TableModelPedido(pedido.getItens(), gui.getTable());
		gui.getTable().setModel(tabela);
		gui.getTable().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		setIdPedido(pedido.getId());
		gui.getTotalGeral().setText(pedido.getValorTotal().toString());

	}

	public void mostrar() {

		for (Categoria dados : category.findAll()) {

			addButton(dados.getNome(), dados.getImg(), String.valueOf(dados.getId()));
			gui.getPanelProduto().removeAll();

		}
		addAction(new OuvirBotoes());

	}

	public void listaProdutosCategoria(final JButton bt, final String m, final String icon) {

		bt.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				buscarProdutoCategoria(Long.parseLong(bt.getActionCommand()));
				System.out.println("oi");
			}
		});

	}

	public void listaProdutos(final JButton bt) {

		bt.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				preencherPedido(produto.findOne(Long.parseLong(bt.getActionCommand().toString())));

			}
		});
	}

	public void buscarProdutoCategoria(Long id) {

		Categoria cate = category.findOne(id);
		gui.getPanelProduto().removeAll();
		for (Produto dados : cate.getProdutos()) {

			// addButtonProduto(dados.getNome(), dados.getImg(),
			// dados.getCodigo());
			btn = new JButton(dados.getNome());
			btn.setIcon(new ImageIcon(dados.getImg()));
			btn.setActionCommand(String.valueOf(dados.getId()));
			btn.setPreferredSize(new Dimension(100, 100));
			btn.setFocusable(true);
			gui.getPanelProduto().add(btn);
			listaProdutos(btn);

		}

	}

	public void alerta(String m) {

		JOptionPane.showMessageDialog(null, m);
	}

	public void preencherPedido(Produto p) {
		int pos = 0;
		boolean achou = false;
		TableModelPedido model = (TableModelPedido) gui.getTable().getModel();

		try {
			for (int j = 0; j < model.getRowCount() && achou == false; j++) {
				Object objecto = model.getValueAt(j, 0);
				if (!"".equals(objecto)) {

					if (p.getNome().equals(objecto)) {
						pos = j;
						achou = true;
					}
				}
			}

			if (achou) {
				alerta("Produto já adicionado");
				return;
			}

			ItemPedido item = new ItemPedido();
			item.setProduto(p);
			item.setQuantidade(new BigDecimal(1));
			item.setValorUnitario(p.getPrecoVenda());
			item.setValorTotal(item.getQuantidade().multiply(item.getValorUnitario()));
			model.addRow(item);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public void addAction(ActionListener list) {

		for (String key : getButtons().keySet()) {

			getButtons().get(key).addActionListener(list);
		}
	}

	public void addButton(String text, String iconPath, String comand) {
		JButton buton = buildButton(text, iconPath, comand);
		buton.setPreferredSize(new Dimension(140, 140));
		buton.setFont(new Font("arial", Font.PLAIN, 10));
		buttons.put(text.toLowerCase(), buton);
		gui.getPanelCategoria().add(buton);

	}

	public void addButtonProduto(String text, String iconPath, String com) {
		final JButton buton = buildButton(text, iconPath, com);
		buton.setPreferredSize(new Dimension(150, 150));
		buton.setFont(new Font("arial", Font.PLAIN, 14));
		buttons.put(text.toLowerCase(), buton);
		gui.getPanelProduto().add(buton);
		buton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				preencherPedido(produto.findOne(Long.parseLong(buton.getActionCommand().toString())));

			}
		});

	}

	private JButton buildButton(String text, String iconPath, String comand) {
		JButton buton = new JButton();
		buton.setText(text);
		buton.setToolTipText(text);
		buton.setHorizontalTextPosition(SwingConstants.CENTER);
		buton.setVerticalTextPosition(SwingConstants.BOTTOM);
		String icon = (iconPath == null ? padrao : iconPath);
		buton.setIcon(new ImageIcon(icon));
		buton.setFocusable(true);
		buton.setActionCommand(comand);

		buton.setBackground(Color.cyan);
		buton.setFocusPainted(true);
		buton.setForeground(Color.BLACK);
		return buton;

	}

	public Map<String, JButton> getButtons() {
		return buttons;
	}

	private static BigDecimal parse(String str) {
		String normalized = str.replaceAll("\\s", "").replace(',', '.');
		return new BigDecimal(normalized);
	}

	public Long getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(Long idPedido) {
		this.idPedido = idPedido;
	}

	public Long getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Long idReserva) {
		this.idReserva = idReserva;
	}

	public Mesa getIdMesa() {
		return idMesa;
	}

	public void setIdMesa(Mesa idMesa) {
		this.idMesa = idMesa;
	}

	public class OuvirObjecto implements ActionListener {

		TableModelPedido model = (TableModelPedido) gui.getTable().getModel();

		public void actionPerformed(ActionEvent e) {

			if (e.getActionCommand().equals("adicionar")) {
				gui.getBtnConfirmar().setEnabled(false);

				try {
					int row = gui.getTable().getSelectedRow();

					if (!(row == -1)) {

						String p = JOptionPane.showInputDialog("Adicionar Quantidade");

						Object id = model.getValueAt(row, 0);
						BigDecimal qtd = new BigDecimal(p);
						BigDecimal preco = new BigDecimal(model.getValueAt(row, 2).toString());
						System.out.println(preco);
						BigDecimal total = new BigDecimal(qtd.multiply(preco).toString());
						System.out.println(total);

						for (ItemPedido pedido : model.getColunas()) {
							if (id.equals(pedido.getProduto().getNome())) {

								int index = model.getColunas().indexOf(pedido);
								pedido.setQuantidade(new BigDecimal(p));

								pedido.setValorTotal(total);
								model.getColunas().set(index, pedido);

							}
						}

						gui.getTable().repaint();
					} else {
						alerta("Seleciona um produto");
					}

				} catch (Exception e2) {
					System.out.println(e2.getMessage());
				}

			}
			if (e.getActionCommand().equals("excluir")) {
				gui.getBtnConfirmar().setEnabled(false);

				try {
					int row = gui.getTable().getSelectedRow();
					if (!(row == -1))
						model.removeRow(row);
					else {
						alerta("Seleciona um produto");
					}

				} catch (Exception e2) {
					System.out.println(e2.getMessage());
				}

			}

			if (e.getActionCommand().equals("cancelar")) {
				gui.dispose();

			}

			if (e.getActionCommand().equals("confirmar")) {
				try {

					if (!gui.getTxtTotal().getText().isEmpty()) {
						Reserva reserva = new Reserva();
						Pedido pedido = new Pedido();
						Mesa mesaId = mesa.findByNome(gui.getLblMesa().getText());

						if (null != getIdReserva()) {
							reserva.setId(getIdReserva());
						}

						if (null != getIdPedido()) {
							pedido.setId(getIdPedido());
						}

						if (reservaDao.findByMesa(mesaId) == null) {
							reserva.setDocumento("00000000");
							reserva.setHora(Calendar.getInstance());
							reserva.setNome("000000");
							reserva.setNumeroPessoas(1);
							reserva.setTelefone(000000000);
							reserva.setMesa(mesaId);
							reservaDao.saveAndFlush(reserva);
						}
						pedido.setDataCriacao(Calendar.getInstance());
						pedido.setMesa(mesaId);
						pedido.setStatus(StatusPedido.PROCESSANDO);
						pedido.setValorTotal(parse(gui.getTxtTotal().getText().toString()));
						pedido.setTipoPagamento(TipoPagamentos.Dinheiro);
						pedido.setValorDesconto(BigDecimal.ZERO);
						for (ItemPedido dados : model.getColunas()) {
							dados.setPedido(pedido);
						}
						pedido.getItens().addAll(model.getColunas());

						pedidoDAo.saveAndFlush(pedido);
						setIdPedido(pedido.getId());
						setIdReserva(reserva.getId());

						alerta("Pedido Confirmado");
						gui.getBtnImprimir().setEnabled(true);
						gui.getBtnConfirmar().setEnabled(false);
					} else {
						alerta("Pressiona primeiro em calcular total");

					}
				} catch (Exception e2) {
					System.out.println(e2.getMessage());
				}

			}

			if (e.getActionCommand().equals("calcular"))

			{

				BigDecimal totalGeral = model.somarTodos().setScale(2, BigDecimal.ROUND_HALF_EVEN);
				gui.getTxtTotal().setText(totalGeral.toString());
				String vl = NumberFormat.getCurrencyInstance().format(totalGeral);
				gui.getTotalGeral().setText(vl);
				gui.getLblTotalGeral().setVisible(true);
				gui.getBtnConfirmar().setEnabled(true);

			}

			if (e.getActionCommand().equals("imprimir"))

			{

				FacturaMesa factura = new FacturaMesa(model.getColunas());
				factura.escrever();

			}

		}

	}

	public class OuvirBotoes implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				Categoria cate = category.findOne(Long.parseLong(e.getActionCommand().toString()));
				gui.getPanelProduto().removeAll();
				for (Produto dados : cate.getProdutos()) {
					addButtonProduto(dados.getNome(), dados.getImg(), dados.getId().toString());

				}
				gui.getPanelProduto().repaint();
			} catch (Exception e2) {
				gui.getPanelProduto().removeAll();
				System.out.println(e2.getMessage());
			}

		}

	}

}
