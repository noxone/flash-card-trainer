package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.olafneumann.trainer.data.BeanTrainerItemGroup;
import org.olafneumann.trainer.data.TrainerItem;
import org.olafneumann.trainer.data.TrainerItemGroup;
import org.olafneumann.trainer.data.TrainerModel;

class GroupWindow extends JDialog {
	private static final long serialVersionUID = 2174230821028451129L;

	private static GroupWindow window = null;

	public static synchronized void showWindow(final JFrame owner, final TrainerModel<?> model) {
		if (window == null) {
			SwingUtilities.invokeLater(() -> {
				window = new GroupWindow(owner, model);
				window.setVisible(true);
			});
		} else {
			SwingUtilities.invokeLater(() -> window.requestFocus());
		}
	}

	public static synchronized void hideWindow() {
		if (window != null) {
			SwingUtilities.invokeLater(() -> {
				window.setVisible(false);
				window.dispose();
				window = null;
			});
		}
	}

	private final JList<TrainerItemGroup<?>> lstGroups;
	private final JList<? extends TrainerItem> lstItems;

	@SuppressWarnings("unchecked")
	private GroupWindow(final JFrame owner, @SuppressWarnings("rawtypes") final TrainerModel model) {
		super(owner);
		setIconImages(Icons.LOGO.getImageList());
		setTitle(owner.getTitle() + " - " + Messages.getString("GroupWindow.Title")); //$NON-NLS-1$ //$NON-NLS-2$
		setModal(true);
		setResizable(false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(600, 400));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(final WindowEvent e) {
				hideWindow();
			}
		});

		lstGroups = new JList<>(new GroupListModel(model.getGroups()));
		lstGroups.setMinimumSize(new Dimension(250, 300));
		final JPanel pnlLeftBottom = new JPanel(new GridLayout(1, 2));
		final JButton btnAddGroup = new JButton("add");
		final JButton btnRemoveGroup = new JButton("remove");
		pnlLeftBottom.add(btnAddGroup);
		pnlLeftBottom.add(btnRemoveGroup);
		final JPanel pnlLeft = new JPanel(new BorderLayout());
		pnlLeft.add(new JScrollPane(lstGroups), BorderLayout.CENTER);
		pnlLeft.add(pnlLeftBottom, BorderLayout.SOUTH);

		lstItems = new JList<>(new ItemListModel<TrainerItem>(model.getItems()));
		lstItems.setMinimumSize(new Dimension(250, 300));
		lstItems.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final JPanel pnlRight = new JPanel(new BorderLayout());
		pnlRight.add(new JScrollPane(lstItems), BorderLayout.CENTER);

		final JPanel pnlButtons = new JPanel(new GridLayout(4, 1));
		final JButton btnAdd = new JButton("Add");
		final JButton btnRemove = new JButton("Remove");
		final JButton btnAddAll = new JButton("Add all");
		final JButton btnRemoveAll = new JButton("Remove all");
		pnlButtons.add(btnAdd);
		pnlButtons.add(btnRemove);
		pnlButtons.add(btnAddAll);
		pnlButtons.add(btnRemoveAll);

		final JButton btnOK = new JButton("OK");
		final JPanel pnlSouth = new JPanel(new GridLayout(1, 2));
		pnlSouth.add(Box.createGlue());
		pnlSouth.add(btnOK);

		setLayout(new BorderLayout());
		add(pnlLeft, BorderLayout.WEST);
		add(pnlButtons, BorderLayout.CENTER);
		add(pnlRight, BorderLayout.EAST);
		add(pnlSouth, BorderLayout.SOUTH);

		btnOK.addActionListener(e -> hideWindow());
		btnAddGroup.addActionListener(e -> {
			final String n = JOptionPane.showInputDialog("Name");
			final BeanTrainerItemGroup group = new BeanTrainerItemGroup();
			group.setName(n);
			model.getGroups().add(group);
			lstGroups.updateUI();
		});

		doEnablingDisabling();
		pack();
		setLocationRelativeTo(owner);
	}

	private void doEnablingDisabling() {
	}

	private static class GroupListModel extends AbstractListModel<TrainerItemGroup<?>> {
		private static final long serialVersionUID = 2758179569000818981L;

		private final List<TrainerItemGroup<?>> items;

		@SuppressWarnings("unchecked")
		public GroupListModel(final List<?> groups) {
			this.items = (List<TrainerItemGroup<?>>) groups;
		}

		@Override
		public int getSize() {
			return items.size();
		}

		@Override
		public TrainerItemGroup<?> getElementAt(final int index) {
			return items.get(index);
		}
	}

	private static class ItemListModel<I extends TrainerItem> extends AbstractListModel<I> {
		private static final long serialVersionUID = 5379522965077757124L;

		private final List<I> items;

		public ItemListModel(final List<I> items) {
			this.items = items;
		}

		@Override
		public int getSize() {
			return items.size();
		}

		@Override
		public I getElementAt(final int index) {
			return items.get(index);
		}
	}
}
