package org.olafneumann.trainer.print;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import org.olafneumann.trainer.data.TrainerItem;
import org.olafneumann.trainer.data.TrainerModel;

public class TrainerPrinter {
	public static void print(TrainerModel<? extends TrainerItem> model) {
		// TODO Drucken
		PrinterJob job = PrinterJob.getPrinterJob();
		TrainerPrintDocument document = new TrainerPrintDocument(model);
		job.setPrintable(document);
		// job.setPageable(document);
		job.setJobName("Karteikarten drucken...");
		if (job.printDialog()) {
			try {
				job.print();
			}
			catch (PrinterException e1) {}
		}
	}

	private TrainerPrinter() {}
}
