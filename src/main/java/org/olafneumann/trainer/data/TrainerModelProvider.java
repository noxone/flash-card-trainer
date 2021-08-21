package org.olafneumann.trainer.data;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.olafneumann.settings.AnnotationalSettings;
import org.olafneumann.settings.Setting;
import org.olafneumann.settings.SettingsException;
import org.olafneumann.settings.TypeProvider;

public class TrainerModelProvider {
	private static TrainerModelProvider instance = new TrainerModelProvider();

	public static TrainerModelProvider getInstance() {
		return instance;
	}

	private TrainerDefaultModelProvider<?> defaultModelProvider = TrainerDefaultModelProvider.BEAN_TRAINER_MODEL_PROVIDER;
	private final Map<Class<? extends TrainerModel<?>>, TrainerModelInput[]> inputs = new HashMap<>();
	private TypeProvider typeProvider = null;

	private TrainerModelProvider() {
	}

	public void configure(final Class<? extends TrainerModel<?>> modelClass,
			final Collection<TrainerModelInput> inputs) {
		if (inputs != null) {
			this.inputs.put(modelClass, new ArrayList<>(inputs).toArray(new TrainerModelInput[0]));
		} else {
			this.inputs.remove(modelClass);
		}
	}

	public TrainerModelInput[] getInputs(final Class<? extends TrainerModel<?>> modelClass) {
		if (inputs.containsKey(modelClass)) {
			return inputs.get(modelClass);
		}
		return new TrainerModelInput[0];
	}

	@SuppressWarnings("unchecked")
	public TrainerModelInput[] getInputs(final TrainerModel<?> model) {
		return getInputs((Class<? extends TrainerModel<?>>) model.getClass());
	}

	public void setTrainerDefaultModelProvider(TrainerDefaultModelProvider<?> defaultModelProvider) {
		if (defaultModelProvider == null) {
			defaultModelProvider = TrainerDefaultModelProvider.BEAN_TRAINER_MODEL_PROVIDER;
		}
		this.defaultModelProvider = defaultModelProvider;
	}

	public TrainerModel<?> getDefaultModel() {
		return defaultModelProvider.createModel();
	}

	public TypeProvider getTypeProvider() {
		return typeProvider;
	}

	public void setTypeProvider(final TypeProvider typeProvider) {
		this.typeProvider = typeProvider;
	}

	private static final String ZIP_XML_FILENAME = "model.xml";

	public TrainerModel<?> loadModel(final File file, final WriteMode mode)
			throws IOException, ClassNotFoundException, SettingsException {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			if (mode == null) {
				try {
					return loadModelFromObject(in);
				} catch (final Exception e) {
					try {
						return loadModelFromXml(in);
					} catch (final Exception e1) {
						try {
							return loadModelFromZippedXml(in);
						} catch (final Exception e2) {
							throw new RuntimeException(
									"Unable to load model. Cannot determine filetype or file is corrupted.");
						}
					}
				}
			}
			switch (mode) {
			case JavaObject:
				return loadModelFromObject(in);
			case XML:
				return loadModelFromXml(in);
			case ZippedXML:
				return loadModelFromZippedXml(in);
			case AnnotationalSettings:
				return loadModelFromAnnotationalSettings(in);
			case ZippedAnnotationalSettings:
				return loadModelFromZippedAnnotationalSettings(in);
			case Text:
				return loadModelFromText(in);
			default:
				throw new RuntimeException("Unknown write mode: " + mode.name());
			}
		}
		// catch (Exception e) {
		// e.printStackTrace();
		// ONUtils.showError(e);
		// return getDefaultModel();
		// }
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	@SuppressWarnings("resource")
	private TrainerModel<?> loadModelFromXml(final InputStream in) {
		return (TrainerModel<?>) new XMLDecoder(in).readObject();
	}

	private TrainerModel<?> loadModelFromZippedXml(final InputStream in) throws IOException {
		try (ZipInputStream zin = new ZipInputStream(in)) {
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				if (ZIP_XML_FILENAME.equals(entry.getName())) {
					return loadModelFromXml(zin);
				}
			}
			throw new RuntimeException();
		}
	}

	private TrainerModel<?> loadModelFromObject(final InputStream in) throws IOException, ClassNotFoundException {
		return (TrainerModel<?>) new ObjectInputStream(in).readObject();
	}

	private TrainerModel<?> loadModelFromAnnotationalSettings(final InputStream in) throws SettingsException {
		final ModelSaver modelSaver = new ModelSaver(null);
		AnnotationalSettings.loadSettings("trainer", new InputStreamReader(in, Charset.forName("utf-8")), modelSaver,
				getTypeProvider());
		return modelSaver.getModel();
	}

	private TrainerModel<?> loadModelFromZippedAnnotationalSettings(final InputStream in)
			throws IOException, SettingsException {
		try (ZipInputStream zin = new ZipInputStream(in)) {
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				if (ZIP_XML_FILENAME.equals(entry.getName())) {
					return loadModelFromAnnotationalSettings(zin);
				}
			}
			throw new RuntimeException();
		}
	}

	private TrainerModel<?> loadModelFromText(final InputStream in) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")));
		final BeanTrainerModel model = new BeanTrainerModel();
		String line;
		while ((line = reader.readLine()) != null) {
			model.addItem(new BeanTrainerItem(model, line.split(",")));
		}
		return model;
	}

	public void saveModel(final TrainerModel<?> model, final File file, final WriteMode mode)
			throws IOException, SettingsException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (mode == null) {
				throw new RuntimeException("No file type selected.");
			}
			switch (mode) {
			case JavaObject:
				writeModelAsObject(model, fos);
				break;
			case XML:
				writeModelAsXml(model, fos);
				break;
			case ZippedXML:
				writeModelAsZippedXml(model, fos);
				break;
			case AnnotationalSettings:
				writeModelAsAnnotationalSettings(model, fos);
				break;
			case ZippedAnnotationalSettings:
				writeModelAsZippedAnnotationalSettings(model, fos);
				break;
			case Text:
				writeModelAsText(model, fos);
				break;
			default:
				throw new RuntimeException("Unknown write mode: " + mode.name());
			}
		}
		// catch (Exception e) {
		// e.printStackTrace();
		// ONUtils.showError(e);
		// }
		finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	private void writeModelAsObject(final TrainerModel<?> model, final OutputStream out) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(out));
			oos.writeObject(model);
			oos.flush();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (final IOException e) {
				}
			}
		}
	}

	private void writeModelAsZippedXml(final TrainerModel<?> model, final OutputStream out) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(out)) {
			zos.putNextEntry(new ZipEntry(ZIP_XML_FILENAME));
			writeModelAsXml(model, new FilterOutputStream(zos) {
				@Override
				public void close() throws IOException {
				}
			});
		}
	}

	private void writeModelAsXml(final TrainerModel<?> model, final OutputStream out) {
		XMLEncoder enc = null;
		try {
			enc = new XMLEncoder(out);
			enc.writeObject(model);
		} finally {
			if (enc != null) {
				enc.close();
			}
		}
	}

	private void writeModelAsAnnotationalSettings(final TrainerModel<?> model, final OutputStream out)
			throws IOException, SettingsException {
		AnnotationalSettings.saveSettings("trainer", new OutputStreamWriter(out, Charset.forName("utf-8")),
				new ModelSaver(model));
	}

	private void writeModelAsZippedAnnotationalSettings(final TrainerModel<?> model, final OutputStream out)
			throws IOException, SettingsException {
		try (ZipOutputStream zos = new ZipOutputStream(out)) {
			zos.putNextEntry(new ZipEntry(ZIP_XML_FILENAME));
			writeModelAsAnnotationalSettings(model, new FilterOutputStream(zos) {
				@Override
				public void close() throws IOException {
				}
			});
		}
	}

	private void writeModelAsText(final TrainerModel<?> model, final OutputStream out) throws IOException {
		final OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("utf-8"));
		for (final TrainerItem item : model) {
			for (final String value : item.getValues()) {
				writer.append(value).append(",");
			}
			writer.append("\n");
		}
		writer.flush();
	}

	public static class ModelSaver {
		private TrainerModel<?> model;

		public ModelSaver(final TrainerModel<?> model) {
			this.model = model;
		}

		@Setting("model")
		public TrainerModel<?> getModel() {
			return model;
		}

		@Setting("model")
		public void setModel(final TrainerModel<?> model) {
			this.model = model;
		}
	}
}
