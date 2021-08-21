package org.olafneumann.trainer.print;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import org.olafneumann.trainer.data.TrainerItem;
import org.olafneumann.trainer.data.TrainerModel;

public class TrainerPrinter {
	public static void print(final TrainerModel<? extends TrainerItem> model) {
		// TODO Drucken
		final PrinterJob job = PrinterJob.getPrinterJob();
		final TrainerPrintDocument document = new TrainerPrintDocument(model);
		job.setPrintable(document);
		// job.setPageable(document);
		job.setJobName("Karteikarten drucken...");
		if (job.printDialog()) {
			try {
				job.print();
			} catch (final PrinterException e1) {
			}
		}
	}

	private TrainerPrinter() {
	}
}
