package org.olafneumann.trainer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window = new GroupWindow(owner, model);
					window.setVisible(true);
				}
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.requestFocus();
				}
			});
		}
	}

	public static synchronized void hideWindow() {
		if (window != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.setVisible(false);
					window.dispose();
					window = null;
				}
			});
		}
	}

	private JList<TrainerItemGroup<?>> lstGroups;
	private JList<? extends TrainerItem> lstItems;

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
			public void windowClosed(WindowEvent e) {
				hideWindow();
			}
		});

		lstGroups = new JList<TrainerItemGroup<?>>(new GroupListModel(model.getGroups()));
		lstGroups.setMinimumSize(new Dimension(250, 300));
		JPanel pnlLeftBottom = new JPanel(new GridLayout(1, 2));
		JButton btnAddGroup = new JButton("add");
		JButton btnRemoveGroup = new JButton("remove");
		pnlLeftBottom.add(btnAddGroup);
		pnlLeftBottom.add(btnRemoveGroup);
		JPanel pnlLeft = new JPanel(new BorderLayout());
		pnlLeft.add(new JScrollPane(lstGroups), BorderLayout.CENTER);
		pnlLeft.add(pnlLeftBottom, BorderLayout.SOUTH);

		lstItems = new JList<TrainerItem>(new ItemListModel<TrainerItem>(model.getItems()));
		lstItems.setMinimumSize(new Dimension(250, 300));
		lstItems.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JPanel pnlRight = new JPanel(new BorderLayout());
		pnlRight.add(new JScrollPane(lstItems), BorderLayout.CENTER);

		JPanel pnlButtons = new JPanel(new GridLayout(4, 1));
		JButton btnAdd = new JButton("Add");
		JButton btnRemove = new JButton("Remove");
		JButton btnAddAll = new JButton("Add all");
		JButton btnRemoveAll = new JButton("Remove all");
		pnlButtons.add(btnAdd);
		pnlButtons.add(btnRemove);
		pnlButtons.add(btnAddAll);
		pnlButtons.add(btnRemoveAll);

		JButton btnOK = new JButton("OK");
		JPanel pnlSouth = new JPanel(new GridLayout(1, 2));
		pnlSouth.add(Box.createGlue());
		pnlSouth.add(btnOK);

		setLayout(new BorderLayout());
		add(pnlLeft, BorderLayout.WEST);
		add(pnlButtons, BorderLayout.CENTER);
		add(pnlRight, BorderLayout.EAST);
		add(pnlSouth, BorderLayout.SOUTH);

		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hideWindow();
			}
		});
		btnAddGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String n = JOptionPane.showInputDialog("Name");
				BeanTrainerItemGroup group = new BeanTrainerItemGroup();
				group.setName(n);
				model.getGroups().add(group);
				lstGroups.updateUI();
			}
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
		public GroupListModel(List<?> groups) {
			this.items = (List<TrainerItemGroup<?>>) groups;
		}

		@Override
		public int getSize() {
			return items.size();
		}

		@Override
		public TrainerItemGroup<?> getElementAt(int index) {
			return items.get(index);
		}
	}

	private static class ItemListModel<I extends TrainerItem> extends AbstractListModel<I> {
		private static final long serialVersionUID = 5379522965077757124L;

		private final List<I> items;

		public ItemListModel(List<I> items) {
			this.items = items;
		}

		@Override
		public int getSize() {
			return items.size();
		}

		@Override
		public I getElementAt(int index) {
			return items.get(index);
		}
	}
}
