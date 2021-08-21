package org.olafneumann.trainer.print;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import org.olafneumann.trainer.data.TrainerModel;
import org.olafneumann.trainer.data.TrainerModelInput;
import org.olafneumann.trainer.data.TrainerModelProvider;

public class TrainerPrintDocument implements Printable {
	private final TrainerModel<?> model;

	public TrainerPrintDocument(TrainerModel<?> model) {
		this.model = model;
	}

	@Override
	public int print(Graphics g, PageFormat format, int pageNumber) throws PrinterException {
		if (pageNumber > 0)
			return NO_SUCH_PAGE;

		// Column width
		//double columnWidth = format.getImageableWidth() / 3.0;

		// Determine font metrics
		TrainerModelInput[] inputs = TrainerModelProvider.getInstance().getInputs(model);
		FontMetrics[] metrics = new FontMetrics[inputs.length];
		for (int i = 0; i < inputs.length; ++i) {
			metrics[i] = g.getFontMetrics(inputs[i].getPrintLettering().getFont());
		}

		//		// Calculate Vocab-Size
		//		for (TrainerItem item : model) {
		//			for (int i = 0; i < metrics.length; ++i) {
		//				String value = item.getValues()[i];
		//			}
		//		}

		// Borders
		g.setColor(Color.black);
		g.drawLine(0, 0, (int) format.getImageableWidth(), 0);
		g.drawLine(0, 0, 0, (int) format.getImageableHeight());
		g.drawLine(0, (int) format.getImageableHeight(), (int) format.getImageableWidth(), (int) format.getImageableHeight());
		g.drawLine((int) format.getImageableWidth(), 0, (int) format.getImageableWidth(), (int) format.getImageableHeight());

		return PAGE_EXISTS;
	}
}
