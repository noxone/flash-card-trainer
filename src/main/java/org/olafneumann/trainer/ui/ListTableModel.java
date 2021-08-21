package org.olafneumann.trainer.ui;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

class ListTableModel<E> extends AbstractTableModel {
	private static final long serialVersionUID = 7169762466700784750L;
	private final AbstractListModel<E> model;

	public ListTableModel(final AbstractListModel<E> model) {
		this.model = model;
		model.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(final ListDataEvent e) {
				fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
			}

			@Override
			public void intervalAdded(final ListDataEvent e) {
				fireTableRowsInserted(e.getIndex0(), e.getIndex1());
			}

			@Override
			public void contentsChanged(final ListDataEvent e) {
				fireTableRowsUpdated(e.getIndex0(), e.getIndex1());
			}
		});
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return model.getSize();
	}

	@Override
	public /* E */Object getValueAt(final int rowIndex, final int columnIndex) {
		return model.getElementAt(rowIndex);
	}
}
