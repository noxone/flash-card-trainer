package org.olafneumann.trainer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Properties;

import javax.swing.UIManager;

import org.jdom2.Element;
import org.olafneumann.settings.TypeProvider;
import org.olafneumann.trainer.data.BeanTrainerItem;
import org.olafneumann.trainer.data.BeanTrainerModel;
import org.olafneumann.trainer.data.TrainerDefaultModelProvider;
import org.olafneumann.trainer.data.TrainerItem;
import org.olafneumann.trainer.data.TrainerModel;
import org.olafneumann.trainer.data.TrainerModelInputProvider;
import org.olafneumann.trainer.data.TrainerModelProvider;
import org.olafneumann.trainer.ui.MainWindow;
import org.olafneumann.trainer.ui.ONUtils;

public abstract class Trainer<I extends TrainerItem, M extends TrainerModel<I>>
		implements TrainerDefaultModelProvider<M>, TypeProvider {
	private static final String DEFAULT_PROPERTIES_FILENAME = "configuration.properties";

	protected static final File getFileFromArguments(final String[] args) {
		File file = null;
		if (args.length > 0) {
			file = new File(args[0]);
			if (!file.exists()) {
				file = null;
			}
		}
		return file;
	}

	protected void startApplication(final File file) {
		// Window-Style ändern
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
		}

		// letzte Maßnahmen beim Beenden
		Runtime.getRuntime().addShutdownHook(new Thread("Trainer Shutdown") {
			@Override
			public void run() {
				stopApplication();
			}
		});

		// Einstellungen laden
		final Settings settings = new Settings();
		try {
			final Properties properties = createFinalApplicationProperties(getProperties());
			settings.load(properties);
			initTrainerModelProvider(properties);
		} catch (final Exception e) {
			e.printStackTrace();
			ONUtils.showError(e);
			return;
		}

		// ein Modell erzeugen
		TrainerModel<?> model = null;
		if (file != null && file.exists() && file.canRead()) {
			try {
				model = TrainerModelProvider.getInstance().loadModel(file, null);
			} catch (final Exception e) {
			}
		}
		// Kein Modell geladen... wir nehmen das Default-Modell
		if (model == null) {
			model = TrainerModelProvider.getInstance().getDefaultModel();
		}

		// Fenster anzeigen
		startUi(settings, model);
	}

	protected void startUi(final Settings settings, final TrainerModel<?> model) {
		MainWindow.showWindow(settings, model);
	}

	protected void stopApplication() {
		// nothing here yet
	}

	protected String getPropertiesFilename() {
		return DEFAULT_PROPERTIES_FILENAME;
	}

	protected InputStream getApplicationPropertiesInputStream() throws IOException {
		final File configurationFile = new File(getClass().getSimpleName() + ".config");
		if (configurationFile.exists() && configurationFile.canRead()) {
			return new BufferedInputStream(new FileInputStream(configurationFile));
		}
		return null;
	}

	protected Properties createFinalApplicationProperties(final Properties properties) throws IOException {
		final InputStream pis = getApplicationPropertiesInputStream();
		if (pis != null) {
			properties.load(pis);
			pis.close();
		}
		return properties;
	}

	protected void initTrainerModelProvider(final Properties properties) {
		TrainerModelProvider.getInstance().setTrainerDefaultModelProvider(this);
		TrainerModelProvider.getInstance().configure(getModelClass(),
				TrainerModelInputProvider.readTrainerModelInputs(properties));
		TrainerModelProvider.getInstance().setTypeProvider(this);
	}

	private Properties getProperties() throws IOException {
		try (InputStream in = getClass().getResourceAsStream(getPropertiesFilename())) {
			final Properties properties = new Properties();
			properties.load(in);
			return properties;
		}
	}

	protected abstract Class<? extends TrainerModel<?>> getModelClass();

	@Override
	public Type getType(final Element element) {
		if ("model".equals(element.getName()) && element.getParentElement().getParentElement() == null
				&& "trainer".equals(element.getParentElement().getName())) {
			return BeanTrainerModel.class;
		}
		if ("item".equals(element.getName()) && !element.getChildren("Model").isEmpty()
				&& !element.getChildren("Entries").isEmpty()) {
			return BeanTrainerItem.class;
		}

		return null;
	}
}
