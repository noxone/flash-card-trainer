package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.olafneumann.trainer.Settings;
import org.olafneumann.trainer.data.QuestionItem;
import org.olafneumann.trainer.data.TrainerItem;
import org.olafneumann.trainer.data.TrainerModel;
import org.olafneumann.trainer.data.TrainerModelInput;
import org.olafneumann.trainer.data.TrainerModelProvider;
import org.olafneumann.trainer.data.WriteMode;
import org.olafneumann.trainer.print.TrainerPrinter;

public class MainWindow extends JFrame {
	private static final long serialVersionUID = 9151323478968976768L;

	private static MainWindow window = null;

	public static synchronized void showWindow(final Settings settings, final TrainerModel<?> model) {
		if (window == null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						window = new MainWindow(settings, model);
						window.setVisible(true);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized void hideWindow() {
		if (window != null) {
			try {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						window.setVisible(false);
						window.dispose();
						window = null;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized Frame getWindow() {
		return window;
	}

	private static InputArea[] createInputAreas(TrainerModelInput[] inputs) {
		InputArea[] areas = new InputArea[inputs.length];
		for (int i = 0; i < areas.length; ++i) {
			areas[i] = new InputArea(inputs[i]);
		}
		return areas;
	}

	private MainWindowSettings windowSettings = new MainWindowSettings();

	private JTable tblItems;
	private JTextField txtItemFilter;
	private JLabel lblItemFilterClear;
	private InputArea[] inputAreas;
	private JButton btnAddData;
	private JButton btnRemoveData;
	private JButton btnMenu;
	private JButton btnResetGewusst;
	private JLabel lblGewusst;
	private JButton btnFileOpen;
	private JButton btnFileSave;
	private JButton btnTrainer;

	private UndoListener<TrainerItem> trainerItemUndoListener = new UndoListener<TrainerItem>() {
		@Override
		public void undo(TrainerItem item) {
			int index = getModel().addItem(item.getValues());
			setSelectedModelIndex(index);
		}
	};

	private TrainerModel<?> model;
	private UndoManager<TrainerItem> removedItems = new UndoManager<TrainerItem>(trainerItemUndoListener);

	private final Settings settings;

	final JFileChooser fc = new JFileChooser();

	private final Object listUpdate_mutex = new Object();
	private boolean updatingList = false;

	@SuppressWarnings("unchecked")
	private MainWindow(Settings settings, TrainerModel<?> model) {
		super(settings.getTitle());
		this.settings = settings;
		setModel(model);
		setIconImages(Icons.LOGO.getImageList());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		//com.apple.eawt.Application.getApplication().setDockIconImage(Icons.LOGO.getImage());
		//com.apple.eawt.Application.getApplication().setDockIconBadge("5");
		addWindowListener(new TrainerWindowListener());

		addWindowListener(new WindowSettingManager());

		JPanel pnlLeft = new JPanel();
		JPanel pnlLeftTop = new JPanel();
		JPanel pnlLeftTopCenter = new JPanel(new GridLayout(1, 2));
		JPanel pnlLeftTopRight = new JPanel(new GridLayout(1, 1));
		JPanel pnlLeftCenter = new JPanel();
		JPanel pnlLeftBottom = new JPanel();
		JPanel pnlBottom = new JPanel();
		JPanel pnlRight = new JPanel();
		tblItems = new JTable(new ListTableModel<TrainerItem>((AbstractListModel<TrainerItem>) getModel()));
		txtItemFilter = new JTextField();
		lblItemFilterClear = new JLabel(Icons.CLEAR.getImageIcon());
		inputAreas = createInputAreas(getInputs());
		btnAddData = new JButton(Messages.getString("MainWindow.Add"), Icons.BUTTON_ADD.getImageIcon()); //$NON-NLS-1$
		btnRemoveData = new JButton(Messages.getString("MainWindow.Delete"), Icons.BUTTON_REMOVE.getImageIcon()); //$NON-NLS-1$
		btnMenu = new JButton(Icons.ARROW_DOWN.getImageIcon());
		btnFileOpen = new JButton(Messages.getString("MainWindow.Load"), Icons.BUTTON_LOAD.getImageIcon()); //$NON-NLS-1$
		btnFileSave = new JButton(Messages.getString("MainWindow.Save"), Icons.BUTTON_SAVE.getImageIcon()); //$NON-NLS-1$
		btnTrainer = new JButton(Messages.getString("MainWindow.Train"), Icons.BUTTON_TRAINING.getImageIcon()); //$NON-NLS-1$
		btnResetGewusst = new JButton(Messages.getString("MainWindow.MarkAsUnknown"), Icons.BUTTON_RESET.getImageIcon()); //$NON-NLS-1$
		lblGewusst = new JLabel(String.format(Messages.getString("MainWindow.KnownText"), 0, 0, 0, 0)); //$NON-NLS-1$
		JSplitPane splMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);

		setLayout(new BorderLayout());
		pnlLeft.setLayout(new BorderLayout());
		pnlLeftTop.setLayout(new BorderLayout());
		pnlLeftCenter.setLayout(new BorderLayout());
		pnlLeftBottom.setLayout(new GridLayout(1, 2));
		((GridLayout) pnlLeftBottom.getLayout()).setHgap(10);
		pnlBottom.setLayout(new GridLayout(1, 3));
		pnlRight.setLayout(new GridLayout(inputAreas.length, 1));
		tblItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblItems.setAutoCreateRowSorter(true);
		tblItems.setFillsViewportHeight(true);
		tblItems.setShowGrid(false);
		tblItems.setTableHeader(null);
		resetTableCellRenderer();

		add(splMain, BorderLayout.CENTER);
		add(pnlBottom, BorderLayout.SOUTH);
		pnlBottom.add(btnFileOpen);
		pnlBottom.add(btnFileSave);
		pnlBottom.add(btnTrainer);
		splMain.setLeftComponent(pnlLeft);
		splMain.setRightComponent(pnlRight);
		pnlLeftCenter.add(new JScrollPane(tblItems), BorderLayout.CENTER);
		pnlLeftCenter.add(txtItemFilter, BorderLayout.NORTH);
		pnlLeft.add(pnlLeftCenter, BorderLayout.CENTER);
		pnlLeft.add(pnlLeftTop, BorderLayout.NORTH);
		pnlLeft.add(pnlLeftBottom, BorderLayout.SOUTH);
		pnlLeftTop.add(pnlLeftTopCenter, BorderLayout.CENTER);
		pnlLeftTop.add(pnlLeftTopRight, BorderLayout.EAST);
		pnlLeftTopCenter.add(btnAddData);
		pnlLeftTopCenter.add(btnRemoveData);
		pnlLeftTopRight.add(btnMenu);
		pnlLeftBottom.add(btnResetGewusst);
		pnlLeftBottom.add(lblGewusst);
		for (InputArea area : inputAreas)
			pnlRight.add(area);

		createEmptyItem();
		getModel().setHasChanges(false);
		pack();
		setSize(800, 600);
		setLocationRelativeTo(null);
		initFileChooserDialog();

		// Clear-Knopf
		txtItemFilter.setLayout(new BorderLayout());
		txtItemFilter.add(lblItemFilterClear, BorderLayout.EAST);
		lblItemFilterClear.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		lblItemFilterClear.setEnabled(false);
		lblItemFilterClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtItemFilter.setText(""); //$NON-NLS-1$
				txtItemFilter.requestFocus();
			}
		});

		// Handler
		btnAddData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				createEmptyItem();
				inputAreas[0].requestFocus();
			}
		});
		btnRemoveData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doRemoveSelectedItem();
			}
		});
		btnMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doShowMenu(btnMenu);
			}
		});
		btnResetGewusst.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getModel().resetKnown();
				tblItems.repaint();
			}
		});
		txtItemFilter.getDocument().addDocumentListener(new SimpleDocumentListener() {
			@Override
			public void textChanged(DocumentEvent e) {
				lblItemFilterClear.setEnabled(txtItemFilter.getText().length() > 0);
				setTrainerItemFilter(txtItemFilter.getText());
			}
		});
		tblItems.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (getModel().size() == 0)
					return;
				if (!e.getValueIsAdjusting())
					updateUiFromTrainerItem();
			}
		});
		// lstKarten.addListSelectionListener();
		btnFileSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doSaveModelAction();
			}
		});
		btnFileOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				doLoadModelAction();
			}
		});
		btnTrainer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doStartTrainer();
			}
		});
		final DocumentListener kartenSaver = new SimpleDocumentListener() {
			@Override
			public void textChanged(DocumentEvent e) {
				updateTrainerItemFromUi();
			}
		};
		for (int i = 0; i < inputAreas.length; ++i) {
			final int index = i;
			inputAreas[i].addDocumentListener(kartenSaver);
			inputAreas[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getCurrentItem().setKnown(index, inputAreas[index].isChecked());
					tblItems.repaint();
				}
			});
		}
	}

	private final ListDataListener modelListener = new ListDataListener() {
		@Override
		public void intervalRemoved(ListDataEvent e) {
			contentsChanged(e);
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			contentsChanged(e);
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			updateKnownLabel();
		}
	};

	@SuppressWarnings("unchecked")
	private void setModel(TrainerModel<?> model) {
		// TODO Listener h�bsch machen
		if (model != null && model instanceof AbstractListModel)
			((AbstractListModel<?>) model).removeListDataListener(modelListener);
		removedItems.clear();
		setTrainerItemFilter(""); //$NON-NLS-1$
		this.model = model;
		if (tblItems != null) {
			tblItems.setModel(new ListTableModel<TrainerItem>((AbstractListModel<TrainerItem>) model));
			resetTableCellRenderer();
			setSelectedModelIndex(0);
		}
		if (model instanceof AbstractListModel)
			((AbstractListModel<?>) model).addListDataListener(modelListener);
		updateKnownLabel();
	}

	private TrainerModel<?> getModel() {
		return model;
	}

	private void resetTableCellRenderer() {
		if (tblItems != null) {
			TableColumn column = tblItems.getColumnModel().getColumn(0);
			column.setCellRenderer(new ColoredTableCellRenderer());
		}
	}

	private TrainerModelInput[] getInputs() {
		return TrainerModelProvider.getInstance().getInputs(getModel());
	}

	private void createEmptyItem() {
		String[] values = new String[inputAreas.length];
		for (int i = 0; i < values.length; ++i)
			values[i] = ""; //$NON-NLS-1$
		int index = getModel().addItem(values);
		setSelectedModelIndex(toListIndex(index));
	}

	private void setValues(TrainerItem item) {
		for (int i = 0; i < item.getValues().length; ++i) {
			inputAreas[i].setText(item.getValues()[i]);
			inputAreas[i].setChecked(item.isKnown(i));
		}
	}

	private TrainerItem getCurrentItem() {
		if (getModel().size() == 0)
			return null;
		else
			return getModel().get(getSelectedModelIndex());
	}

	private void updateTrainerItemFromUi() {
		synchronized (listUpdate_mutex) {
			if (!updatingList) {
				TrainerItem item = getCurrentItem();
				for (int i = 0; i < inputAreas.length; ++i) {
					item.setValue(i, inputAreas[i].getText());
				}
				tblItems.repaint();
			}
		}
	}

	private void updateUiFromTrainerItem() {
		try {
			synchronized (listUpdate_mutex) {
				updatingList = true;
				TrainerItem item = getCurrentItem();
				if (item != null) {
					setValues(item);
					tblItems.scrollRectToVisible(new Rectangle(0, tblItems.getSelectedRow() * tblItems.getRowHeight(),
							tblItems.getWidth(), (tblItems.getSelectedRow() + 1) * tblItems.getRowHeight()));
				}
				updatingList = false;
			}
		} catch (Exception ex) {
		}
	}

	private void updateKnownLabel() {
		if (lblGewusst != null)
			lblGewusst.setText(String.format(Messages.getString("MainWindow.KnownText"), getModel().countKnownItems(),//$NON-NLS-1$
					getModel().size(), getModel().countKnownItemInputs(), (getModel().size() * getInputs().length)));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setTrainerItemFilter(String filter) {
		if (filter == null)
			filter = ""; //$NON-NLS-1$
		if (txtItemFilter != null && txtItemFilter.isDisplayable()) {
			if (!txtItemFilter.getText().equals(filter))
				txtItemFilter.setText(filter);
			TableRowSorter<ListTableModel<TrainerItem>> sorter = (TableRowSorter<ListTableModel<TrainerItem>>) tblItems
					.getRowSorter();
			if (filter.trim().length() > 0) {
				sorter.setRowFilter((RowFilter) new CollatorTrainerItemFilter(getInputs(), filter));
			} else {
				sorter.setRowFilter(RowFilter.regexFilter(".")); //$NON-NLS-1$
			}
		}
	}

	private void initFileChooserDialog() {
		fc.setMultiSelectionEnabled(false);
		fc.addChoosableFileFilter(WriteMode.getAllSupportedFilesFilter());
		for (WriteMode mode : WriteMode.values())
			fc.addChoosableFileFilter(mode.getFileChooseFileFilter());
		fc.setFileFilter(WriteMode.getAllSupportedFilesFilter());
	}

	private void setSelectedModelIndex(int index) {
		tblItems.getSelectionModel().setSelectionInterval(index, index);
	}

	private int toModelIndex(int listIndex) {
		return tblItems.getRowSorter().convertRowIndexToModel(listIndex);
	}

	private int toListIndex(int modelIndex) {
		return tblItems.getRowSorter().convertRowIndexToView(modelIndex);
	}

	private int getSelectedModelIndex() {
		return toModelIndex(tblItems.getSelectedRow());
	}

	private void doRemoveSelectedItem() {
		int selectedLine = tblItems.getSelectedRow();
		int index = getSelectedModelIndex();
		TrainerItem removedItem = getModel().remove(index);
		if (getModel().size() == 0) {
			txtItemFilter.setText(""); //$NON-NLS-1$
			createEmptyItem();
		} else {
			if (selectedLine >= tblItems.getRowSorter().getViewRowCount())
				--selectedLine;
			setSelectedModelIndex(selectedLine);
		}
		if (!removedItem.isEmpty()) {
			removedItems.push(removedItem);
		}
		tblItems.requestFocus();
	}

	private void doSaveModelAction() {
		fc.setSelectedFile(null);
		if (fc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			WriteMode mode = WriteMode.getMode(fc.getFileFilter());
			if (mode == null)
				mode = WriteMode.getMode(file);
			if (mode == null)
				mode = WriteMode.XML;
			if (!mode.appliesToExtensionOf(file)) {
				file = new File(file.getParentFile(), file.getName() + "." + mode.getExtension()); //$NON-NLS-1$
			}
			if (file.exists()) {
				if (!ONUtils.showQuestion(Messages.getString("MainWindow.FileAlreadyExists"))) { //$NON-NLS-1$
					return;
				}
			}
			try {
				TrainerModelProvider.getInstance().saveModel(getModel(), file, mode);
				getModel().setHasChanges(false);
			} catch (Exception e) {
				e.printStackTrace();
				ONUtils.showError(e);
			}
		}
	}

	private void doLoadModelAction() {
		fc.setSelectedFile(new File("")); //$NON-NLS-1$
		if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			WriteMode mode = WriteMode.getMode(fc.getFileFilter());
			if (mode == null)
				mode = WriteMode.getMode(file);
			TrainerModel<?> model;
			try {
				model = TrainerModelProvider.getInstance().loadModel(file, mode);
				model.setHasChanges(false);
			} catch (Exception e) {
				ONUtils.showError(e);
				model = TrainerModelProvider.getInstance().getDefaultModel();
			}
			if (model != null) {
				setModel(model);
			}
		}
	}

	private void doStartTrainer() {
		QuestionItem question = getModel().randomQuestionItem();
		if (question != null)
			TrainerWindow.showWindow(this, question, this.settings.isShowCaptions());
		else
			ONUtils.showError("Nothing to train...");
	}

	private void doPrintAction() {
		TrainerPrinter.print(getModel());
	}

	private void doShutdown() {
		TrainerWindow.hideWindow();
		dispose();
	}

	private void doShowMenu(JComponent anchor) {
		JPopupMenu menu = new JPopupMenu();

		// undo
		JMenu undo = new JMenu(Messages.getString("MainWindow.UndoDelete")); //$NON-NLS-1$
		for (JMenuItem item : removedItems.createMenuItems()) {
			undo.add(item);
		}

		// printing
		JMenuItem print = createMenuItem(Icons.PRINT.getImageIcon(),
				Messages.getString("MainWindow.Print"), new ActionListener() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent e) {
				doPrintAction();
			}
		});
		print.setEnabled(false);
		// settings
		JMenuItem settings = createMenuItem(null, Messages.getString("MainWindow.Settings"), null); //$NON-NLS-1$
		settings.setEnabled(false);

		// Groups
		JMenuItem groups = createMenuItem(null, "Organize groups", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GroupWindow.showWindow(MainWindow.this, model);
			}
		});

		// exit
		JMenuItem end = createMenuItem(null, Messages.getString("MainWindow.Exit"), new ActionListener() { //$NON-NLS-1$
			@Override
			public void actionPerformed(ActionEvent event) {
				hideWindow();
			}
		});

		menu.add(undo);
		menu.add(new JSeparator());
		menu.add(print);
		menu.add(settings);
		menu.add(groups);
		menu.add(new JSeparator());
		menu.add(end);
		menu.show(anchor, 0, anchor.getHeight());
	}

	private static JMenuItem createMenuItem(Icon icon, String text, ActionListener al) {
		JMenuItem item = new JMenuItem(text, icon);
		if (al != null)
			item.addActionListener(al);
		return item;
	}

	private class TrainerWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			if (!getModel().hasChanges() || ONUtils.showQuestion(Messages.getString("MainWindow.UnsavedChanges"))) { //$NON-NLS-1$
				doShutdown();
			}
		}
	}

	private class WindowSettingManager extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			windowSettings.fileChooserPath = fc.getCurrentDirectory().getAbsolutePath();
			ONUtils.saveSettings(windowSettings);
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			System.exit(0);
		}

		@Override
		public void windowOpened(WindowEvent e) {
			ONUtils.loadSettings(windowSettings);
			try {
				fc.setCurrentDirectory(new File(windowSettings.fileChooserPath));
			} catch (Exception ex) {
			}
		}
	}

	public static class MainWindowSettings {
		public String fileChooserPath = ""; //$NON-NLS-1$
	}
}
