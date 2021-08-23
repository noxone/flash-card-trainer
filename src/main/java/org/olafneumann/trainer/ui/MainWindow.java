package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

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
				SwingUtilities.invokeAndWait(() -> {
					window = new MainWindow(settings, model);
					window.applyLocales();
					window.setVisible(true);
				});
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized void hideWindow() {
		if (window != null) {
			try {
				SwingUtilities.invokeLater(() -> {
					window.setVisible(false);
					window.dispose();
					window = null;
				});
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized Frame getWindow() {
		return window;
	}

	private static InputArea[] createInputAreas(final TrainerModelInput[] inputs) {
		final InputArea[] areas = new InputArea[inputs.length];
		for (int i = 0; i < areas.length; ++i) {
			areas[i] = new InputArea(inputs[i]);
		}
		return areas;
	}

	private final MainWindowSettings windowSettings = new MainWindowSettings();

	private JTable tblItems;
	private JTextField txtItemFilter;
	private JLabel lblItemFilterClear;
	private List<InputArea> inputAreas;
	private JButton btnAddData;
	private JButton btnRemoveData;
	private JButton btnMenu;
	private JButton btnResetGewusst;
	private JLabel lblGewusst;
	private JButton btnFileOpen;
	private JButton btnFileSave;
	private JButton btnTrainer;

	private final UndoListener<TrainerItem> trainerItemUndoListener = item -> {
		final int index = getModel().addItem(item.getValues());
		setSelectedModelIndex(index);
	};

	private TrainerModel<?> model;
	private final UndoManager<TrainerItem> removedItems = new UndoManager<>(trainerItemUndoListener);

	private final Settings settings;

	final JFileChooser fc = new JFileChooser();

	private final Object listUpdate_mutex = new Object();
	private boolean updatingList = false;

	@SuppressWarnings("unchecked")
	private MainWindow(final Settings settings, final TrainerModel<?> model) {
		super(settings.getTitle());
		this.settings = settings;
		setModel(model);
		setIconImages(Icons.LOGO.getImageList());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		// com.apple.eawt.Application.getApplication().setDockIconImage(Icons.LOGO.getImage());
		// com.apple.eawt.Application.getApplication().setDockIconBadge("5");
		addWindowListener(new TrainerWindowListener());

		addWindowListener(new WindowSettingManager());

		final JPanel pnlLeft = new JPanel();
		final JPanel pnlLeftTop = new JPanel();
		final JPanel pnlLeftTopCenter = new JPanel(new GridLayout(1, 2));
		final JPanel pnlLeftTopRight = new JPanel(new GridLayout(1, 1));
		final JPanel pnlLeftCenter = new JPanel();
		final JPanel pnlLeftBottom = new JPanel();
		final JPanel pnlBottom = new JPanel();
		final JPanel pnlRight = new JPanel();
		tblItems = new JTable(new ListTableModel<>((AbstractListModel<TrainerItem>) getModel()));
		txtItemFilter = new JTextField();
		lblItemFilterClear = new JLabel(Icons.CLEAR.getImageIcon());
		inputAreas = Arrays.asList(createInputAreas(getInputs()));
		btnAddData = new JButton(Messages.getString("MainWindow.Add"), Icons.BUTTON_ADD.getImageIcon()); //$NON-NLS-1$
		btnRemoveData = new JButton(Messages.getString("MainWindow.Delete"), Icons.BUTTON_REMOVE.getImageIcon()); //$NON-NLS-1$
		btnMenu = new JButton(Icons.ARROW_DOWN.getImageIcon());
		btnFileOpen = new JButton(Messages.getString("MainWindow.Load"), Icons.BUTTON_LOAD.getImageIcon()); //$NON-NLS-1$
		btnFileSave = new JButton(Messages.getString("MainWindow.Save"), Icons.BUTTON_SAVE.getImageIcon()); //$NON-NLS-1$
		btnTrainer = new JButton(Messages.getString("MainWindow.Train"), Icons.BUTTON_TRAINING.getImageIcon()); //$NON-NLS-1$
		btnResetGewusst = new JButton(Messages.getString("MainWindow.MarkAsUnknown"), //$NON-NLS-1$
				Icons.BUTTON_RESET.getImageIcon());
		lblGewusst = new JLabel(String.format(Messages.getString("MainWindow.KnownText"), 0, 0, 0, 0)); //$NON-NLS-1$
		final JSplitPane splMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);

		setLayout(new BorderLayout());
		pnlLeft.setLayout(new BorderLayout());
		pnlLeftTop.setLayout(new BorderLayout());
		pnlLeftCenter.setLayout(new BorderLayout());
		pnlLeftBottom.setLayout(new GridLayout(1, 2));
		((GridLayout) pnlLeftBottom.getLayout()).setHgap(10);
		pnlBottom.setLayout(new GridLayout(1, 3));
		pnlRight.setLayout(new GridLayout(inputAreas.size(), 1));
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
		for (final InputArea area : inputAreas) {
			pnlRight.add(area);
		}

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
			public void mouseClicked(final MouseEvent e) {
				txtItemFilter.setText(""); //$NON-NLS-1$
				txtItemFilter.requestFocus();
			}
		});

		// Handler
		btnAddData.addActionListener(arg0 -> {
			createEmptyItem();
			inputAreas.get(0).requestFocus();
		});
		btnRemoveData.addActionListener(event -> doRemoveSelectedItem());
		btnMenu.addActionListener(event -> doShowMenu(btnMenu));
		btnResetGewusst.addActionListener(arg0 -> {
			getModel().resetKnown();
			tblItems.repaint();
		});
		txtItemFilter.getDocument().addDocumentListener(new SimpleDocumentListener() {
			@Override
			public void textChanged(final DocumentEvent e) {
				lblItemFilterClear.setEnabled(txtItemFilter.getText().length() > 0);
				setTrainerItemFilter(txtItemFilter.getText());
			}
		});
		tblItems.getSelectionModel().addListSelectionListener(e -> {
			if (getModel().size() == 0) {
				return;
			}
			if (!e.getValueIsAdjusting()) {
				updateUiFromTrainerItem();
			}
		});
		// lstKarten.addListSelectionListener();
		btnFileSave.addActionListener(event -> doSaveModelAction());
		btnFileOpen.addActionListener(event -> doLoadModelAction());
		btnTrainer.addActionListener(e -> doStartTrainer());
		final DocumentListener kartenSaver = new SimpleDocumentListener() {
			@Override
			public void textChanged(final DocumentEvent e) {
				updateTrainerItemFromUi();
			}
		};
		for (final InputArea inputArea : inputAreas) {
			inputArea.addDocumentListener(kartenSaver);
			inputArea.addActionListener(e -> {
				getCurrentItem().setKnown(inputAreas.indexOf(inputArea), inputArea.isChecked());
				tblItems.repaint();
			});
		}
	}

	private final ListDataListener modelListener = new ListDataListener() {
		@Override
		public void intervalRemoved(final ListDataEvent e) {
			contentsChanged(e);
		}

		@Override
		public void intervalAdded(final ListDataEvent e) {
			contentsChanged(e);
		}

		@Override
		public void contentsChanged(final ListDataEvent e) {
			updateKnownLabel();
		}
	};

	@SuppressWarnings("unchecked")
	private void setModel(final TrainerModel<?> model) {
		// TODO Listener hübsch machen
		if (model != null && model instanceof AbstractListModel) {
			((AbstractListModel<?>) model).removeListDataListener(modelListener);
		}
		removedItems.clear();
		setTrainerItemFilter(""); //$NON-NLS-1$
		this.model = model;
		if (tblItems != null) {
			tblItems.setModel(new ListTableModel<>((AbstractListModel<TrainerItem>) model));
			resetTableCellRenderer();
			setSelectedModelIndex(0);
		}
		if (model instanceof AbstractListModel) {
			((AbstractListModel<?>) model).addListDataListener(modelListener);
		}
		updateKnownLabel();
	}

	private TrainerModel<?> getModel() {
		return model;
	}

	private void resetTableCellRenderer() {
		if (tblItems != null) {
			final TableColumn column = tblItems.getColumnModel().getColumn(0);
			column.setCellRenderer(new ColoredTableCellRenderer());
		}
	}

	private void applyLocales() {
		inputAreas.forEach(InputArea::applyLocaleFromInput);
	}

	private TrainerModelInput[] getInputs() {
		return TrainerModelProvider.getInstance().getInputs(getModel());
	}

	private void createEmptyItem() {
		final String[] values = new String[inputAreas.size()];
		Arrays.fill(values, "");
		final int index = getModel().addItem(values);
		setSelectedModelIndex(toListIndex(index));
	}

	private void setValues(final TrainerItem item) {
		for (int i = 0; i < item.getValues().length; ++i) {
			inputAreas.get(i).setText(item.getValues()[i]);
			inputAreas.get(i).setChecked(item.isKnown(i));
		}
	}

	private TrainerItem getCurrentItem() {
		if (getModel().size() == 0) {
			return null;
		}
		return getModel().get(getSelectedModelIndex());
	}

	private void updateTrainerItemFromUi() {
		synchronized (listUpdate_mutex) {
			if (!updatingList) {
				final TrainerItem item = getCurrentItem();
				for (int i = 0; i < inputAreas.size(); ++i) {
					item.setValue(i, inputAreas.get(i).getText());
				}
				tblItems.repaint();
			}
		}
	}

	private void updateUiFromTrainerItem() {
		try {
			synchronized (listUpdate_mutex) {
				updatingList = true;
				final TrainerItem item = getCurrentItem();
				if (item != null) {
					setValues(item);
					tblItems.scrollRectToVisible(new Rectangle(0, tblItems.getSelectedRow() * tblItems.getRowHeight(),
							tblItems.getWidth(), (tblItems.getSelectedRow() + 1) * tblItems.getRowHeight()));
				}
				updatingList = false;
			}
		} catch (final Exception ex) {
		}
	}

	private void updateKnownLabel() {
		if (lblGewusst != null) {
			lblGewusst.setText(String.format(Messages.getString("MainWindow.KnownText"), getModel().countKnownItems(), //$NON-NLS-1$
					getModel().size(), getModel().countKnownItemInputs(), getModel().size() * getInputs().length));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setTrainerItemFilter(String filter) {
		if (filter == null) {
			filter = ""; //$NON-NLS-1$
		}
		if (txtItemFilter != null && txtItemFilter.isDisplayable()) {
			if (!txtItemFilter.getText().equals(filter)) {
				txtItemFilter.setText(filter);
			}
			final TableRowSorter<ListTableModel<TrainerItem>> sorter = (TableRowSorter<ListTableModel<TrainerItem>>) tblItems
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
		for (final WriteMode mode : WriteMode.values()) {
			fc.addChoosableFileFilter(mode.getFileChooseFileFilter());
		}
		fc.setFileFilter(WriteMode.getAllSupportedFilesFilter());
	}

	private void setSelectedModelIndex(final int index) {
		tblItems.getSelectionModel().setSelectionInterval(index, index);
	}

	private int toModelIndex(final int listIndex) {
		return tblItems.getRowSorter().convertRowIndexToModel(listIndex);
	}

	private int toListIndex(final int modelIndex) {
		return tblItems.getRowSorter().convertRowIndexToView(modelIndex);
	}

	private int getSelectedModelIndex() {
		return toModelIndex(tblItems.getSelectedRow());
	}

	private void doRemoveSelectedItem() {
		int selectedLine = tblItems.getSelectedRow();
		final int index = getSelectedModelIndex();
		final TrainerItem removedItem = getModel().remove(index);
		if (getModel().size() == 0) {
			txtItemFilter.setText(""); //$NON-NLS-1$
			createEmptyItem();
		} else {
			if (selectedLine >= tblItems.getRowSorter().getViewRowCount()) {
				--selectedLine;
			}
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
			if (mode == null) {
				mode = WriteMode.getMode(file);
			}
			if (mode == null) {
				mode = WriteMode.XML;
			}
			if (!mode.appliesToExtensionOf(file)) {
				file = new File(file.getParentFile(), file.getName() + "." + mode.getExtension()); //$NON-NLS-1$
			}
			if (file.exists() && !ONUtils.showQuestion(Messages.getString("MainWindow.FileAlreadyExists"))) { //$NON-NLS-1$
				return;
			}
			try {
				TrainerModelProvider.getInstance().saveModel(getModel(), file, mode);
				getModel().setHasChanges(false);
			} catch (final Exception e) {
				e.printStackTrace();
				ONUtils.showError(e);
			}
		}
	}

	private void doLoadModelAction() {
		fc.setSelectedFile(new File("")); //$NON-NLS-1$
		if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
			final File file = fc.getSelectedFile();
			WriteMode mode = WriteMode.getMode(fc.getFileFilter());
			if (mode == null) {
				mode = WriteMode.getMode(file);
			}
			TrainerModel<?> model;
			try {
				model = TrainerModelProvider.getInstance().loadModel(file, mode);
				model.setHasChanges(false);
			} catch (final Exception e) {
				ONUtils.showError(e);
				model = TrainerModelProvider.getInstance().getDefaultModel();
			}
			if (model != null) {
				setModel(model);
			}
		}
	}

	private void doStartTrainer() {
		final QuestionItem question = getModel().randomQuestionItem();
		if (question != null) {
			TrainerWindow.showWindow(this, question, this.settings.isShowCaptions());
		} else {
			ONUtils.showError("Nothing to train...");
		}
	}

	private void doPrintAction() {
		TrainerPrinter.print(getModel());
	}

	private void doShutdown() {
		TrainerWindow.hideWindow();
		dispose();
	}

	private void doShowMenu(final JComponent anchor) {
		final JPopupMenu menu = new JPopupMenu();

		// undo
		final JMenu undo = new JMenu(Messages.getString("MainWindow.UndoDelete")); //$NON-NLS-1$
		for (final JMenuItem item : removedItems.createMenuItems()) {
			undo.add(item);
		}

		// printing
		final JMenuItem print = createMenuItem(Icons.PRINT.getImageIcon(), Messages.getString("MainWindow.Print"), //$NON-NLS-1$
				e -> doPrintAction());
		print.setEnabled(false);
		// settings
		final JMenuItem settings = createMenuItem(null, Messages.getString("MainWindow.Settings"), null); //$NON-NLS-1$
		settings.setEnabled(false);

		// Groups
		final JMenuItem groups = createMenuItem(null, "Organize groups",
				e -> GroupWindow.showWindow(MainWindow.this, model));

		// exit
		final JMenuItem end = createMenuItem(null, Messages.getString("MainWindow.Exit"), event -> hideWindow());

		menu.add(undo);
		menu.add(new JSeparator());
		menu.add(print);
		menu.add(settings);
		menu.add(groups);
		menu.add(new JSeparator());
		menu.add(end);
		menu.show(anchor, 0, anchor.getHeight());
	}

	private static JMenuItem createMenuItem(final Icon icon, final String text, final ActionListener al) {
		final JMenuItem item = new JMenuItem(text, icon);
		if (al != null) {
			item.addActionListener(al);
		}
		return item;
	}

	private class TrainerWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(final WindowEvent e) {
			if (!getModel().hasChanges() || ONUtils.showQuestion(Messages.getString("MainWindow.UnsavedChanges"))) { //$NON-NLS-1$
				doShutdown();
			}
		}
	}

	private class WindowSettingManager extends WindowAdapter {
		@Override
		public void windowClosing(final WindowEvent e) {
			windowSettings.fileChooserPath = fc.getCurrentDirectory().getAbsolutePath();
			ONUtils.saveSettings(windowSettings);
		}

		@Override
		public void windowClosed(final WindowEvent arg0) {
			System.exit(0);
		}

		@Override
		public void windowOpened(final WindowEvent e) {
			ONUtils.loadSettings(windowSettings);
			try {
				fc.setCurrentDirectory(new File(windowSettings.fileChooserPath));
			} catch (final Exception ex) {
			}
		}
	}

	public static class MainWindowSettings {
		public String fileChooserPath = ""; //$NON-NLS-1$
	}
}
