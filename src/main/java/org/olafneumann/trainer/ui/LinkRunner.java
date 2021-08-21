package org.olafneumann.trainer.ui;

import java.awt.Desktop;
import java.net.URI;

import javax.swing.SwingWorker;

public class LinkRunner extends SwingWorker<Void, Void> {
	static boolean isBrowsingSupported() {
		if (!Desktop.isDesktopSupported()) {
			return false;
		} else {
			return Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
		}
	}

	public static void open(URI uri) {
		if (!isBrowsingSupported()) {
			System.err.println(Messages.getString("LinkRunner.BrowsingNotSupported")); //$NON-NLS-1$
			return;
		}
		new LinkRunner(uri).execute();
	}

	private final URI uri;

	private LinkRunner(URI u) {
		if (u == null) {
			throw new NullPointerException();
		}
		uri = u;
	}

	@Override
	protected Void doInBackground() throws Exception {
		Desktop desktop = java.awt.Desktop.getDesktop();
		desktop.browse(uri);
		return null;
	}

	@Override
	protected void done() {
		try {
			get();
		}
		catch (Exception ee) {
			ONUtils.showError(ee);
		}
	}
}