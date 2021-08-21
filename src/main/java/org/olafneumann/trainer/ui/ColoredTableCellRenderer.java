package org.olafneumann.trainer.ui;

import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.olafneumann.trainer.data.TrainerItem;

class ColoredTableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -482079833349029521L;

	@Override
	public ColoredTableCellRenderer getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		ColoredTableCellRenderer renderer = (ColoredTableCellRenderer) super.getTableCellRendererComponent(table,
				value, isSelected, hasFocus, row, column);

		if (!isSelected) {
			TrainerItem item = (TrainerItem) value;
			StringBuffer sb = new StringBuffer();
			sb.append("<html>"); //$NON-NLS-1$
			boolean needSeparator = false;
			String[] values = item.getValues();
			int known = 0;
			for (int i = 0; i < values.length; ++i) {
				if (needSeparator)
					sb.append(" / "); //$NON-NLS-1$
				// TODO Schrift aus dem Input übernehmen
				if (item.isKnown(i))
					++known;
				sb.append(getSpanStart(null, item.isKnown(i) ? "green" : "red")); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append(values[i]);
				needSeparator = values[i] == null || !values[i].trim().isEmpty();
				sb.append("</span>"); //$NON-NLS-1$
			}
			sb.append(" (").append(known).append("/").append(values.length).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append("</html>"); //$NON-NLS-1$
			renderer.setText(sb.toString());
		}
		return renderer;
	}

	private CharSequence getSpanStart(Font font, String color) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span "); //$NON-NLS-1$
		sb.append("style=\""); //$NON-NLS-1$
		if (font != null) {
			sb.append("font-family:").append(font.getName()).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
			sb.append("font-size:").append(font.getSize() / 2).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (color != null) {
			sb.append("color:").append(color).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append("\""); //$NON-NLS-1$
		sb.append(">"); //$NON-NLS-1$
		return sb;
	}
}
