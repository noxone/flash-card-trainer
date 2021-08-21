package org.olafneumann.trainer.ui;

import javax.swing.AbstractListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;

class ListTableModel<E> extends AbstractTableModel {
	private static final long serialVersionUID = 7169762466700784750L;
	private AbstractListModel<E> model;

	public ListTableModel(AbstractListModel<E> model) {
		this.model = model;
		model.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
				fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				fireTableRowsInserted(e.getIndex0(), e.getIndex1());
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
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
	public /*E*/Object getValueAt(int rowIndex, int columnIndex) {
		return model.getElementAt(rowIndex);
	}
}
