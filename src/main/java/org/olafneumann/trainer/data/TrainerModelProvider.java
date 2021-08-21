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
	private final Map<Class<? extends TrainerModel<?>>, TrainerModelInput[]> inputs = new HashMap<Class<? extends TrainerModel<?>>, TrainerModelInput[]>();
	private TypeProvider typeProvider = null;

	private TrainerModelProvider() {}

	public void configure(Class<? extends TrainerModel<?>> modelClass, Collection<TrainerModelInput> inputs) {
		if (inputs != null) {
			this.inputs.put(modelClass, new ArrayList<TrainerModelInput>(inputs).toArray(new TrainerModelInput[0]));
		} else {
			this.inputs.remove(modelClass);
		}
	}

	public TrainerModelInput[] getInputs(Class<? extends TrainerModel<?>> modelClass) {
		if (inputs.containsKey(modelClass)) {
			return inputs.get(modelClass);
		} else {
			return new TrainerModelInput[0];
		}
	}

	@SuppressWarnings("unchecked")
	public TrainerModelInput[] getInputs(TrainerModel<?> model) {
		return getInputs((Class<? extends TrainerModel<?>>) model.getClass());
	}

	public void setTrainerDefaultModelProvider(TrainerDefaultModelProvider<?> defaultModelProvider) {
		if (defaultModelProvider == null)
			defaultModelProvider = TrainerDefaultModelProvider.BEAN_TRAINER_MODEL_PROVIDER;
		this.defaultModelProvider = defaultModelProvider;
	}

	public TrainerModel<?> getDefaultModel() {
		return defaultModelProvider.createModel();
	}

	public TypeProvider getTypeProvider() {
		return typeProvider;
	}

	public void setTypeProvider(TypeProvider typeProvider) {
		this.typeProvider = typeProvider;
	}

	private static final String ZIP_XML_FILENAME = "model.xml";

	public TrainerModel<?> loadModel(File file, WriteMode mode) throws IOException, ClassNotFoundException, SettingsException {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			if (mode != null) {
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
			} else {
				try {
					return loadModelFromObject(in);
				}
				catch (Exception e) {
					try {
						return loadModelFromXml(in);
					}
					catch (Exception e1) {
						try {
							return loadModelFromZippedXml(in);
						}
						catch (Exception e2) {
							throw new RuntimeException("Unable to load model. Cannot determine filetype or file is corrupted.");
						}
					}
				}
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
				}
				catch (IOException e) {}
			}
		}
	}

	@SuppressWarnings("resource")
	private TrainerModel<?> loadModelFromXml(InputStream in) {
		return (TrainerModel<?>) new XMLDecoder(in).readObject();
	}

	private TrainerModel<?> loadModelFromZippedXml(InputStream in) throws IOException {
		ZipInputStream zin = null;
		try {
			zin = new ZipInputStream(in);
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				if (ZIP_XML_FILENAME.equals(entry.getName())) {
					return loadModelFromXml(zin);
				}
			}
			throw new RuntimeException();
		}
		finally {
			if (zin != null) {
				zin.close();
			}
		}
	}

	private TrainerModel<?> loadModelFromObject(InputStream in) throws IOException, ClassNotFoundException {
		return (TrainerModel<?>) new ObjectInputStream(in).readObject();
	}

	private TrainerModel<?> loadModelFromAnnotationalSettings(InputStream in) throws SettingsException {
		ModelSaver modelSaver = new ModelSaver(null);
		AnnotationalSettings.loadSettings("trainer", new InputStreamReader(in, Charset.forName("utf-8")), modelSaver, getTypeProvider());
		return modelSaver.getModel();
	}

	private TrainerModel<?> loadModelFromZippedAnnotationalSettings(InputStream in) throws IOException, SettingsException {
		ZipInputStream zin = null;
		try {
			zin = new ZipInputStream(in);
			ZipEntry entry;
			while ((entry = zin.getNextEntry()) != null) {
				if (ZIP_XML_FILENAME.equals(entry.getName())) {
					return loadModelFromAnnotationalSettings(zin);
				}
			}
			throw new RuntimeException();
		}
		finally {
			if (zin != null) {
				zin.close();
			}
		}
	}

	private TrainerModel<?> loadModelFromText(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")));
		BeanTrainerModel model = new BeanTrainerModel();
		String line;
		while ((line = reader.readLine()) != null) {
			model.addItem(new BeanTrainerItem(model, line.split(",")));
		}
		return model;
	}

	public void saveModel(TrainerModel<?> model, File file, WriteMode mode) throws IOException, SettingsException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			if (mode != null) {
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
			} else {
				throw new RuntimeException("No file type selected.");
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
				}
				catch (IOException e) {}
			}
		}
	}

	private void writeModelAsObject(TrainerModel<?> model, OutputStream out) throws IOException {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(out));
			oos.writeObject(model);
			oos.flush();
		}
		finally {
			if (oos != null) {
				try {
					oos.close();
				}
				catch (IOException e) {}
			}
		}
	}

	private void writeModelAsZippedXml(TrainerModel<?> model, OutputStream out) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(out);
		try {
			zos.putNextEntry(new ZipEntry(ZIP_XML_FILENAME));
			writeModelAsXml(model, new FilterOutputStream(zos) {
				@Override
				public void close() throws IOException {}
			});
		}
		finally {
			zos.close();
		}
	}

	private void writeModelAsXml(TrainerModel<?> model, OutputStream out) {
		XMLEncoder enc = null;
		try {
			enc = new XMLEncoder(out);
			enc.writeObject(model);
		}
		finally {
			if (enc != null) {
				enc.close();
			}
		}
	}

	private void writeModelAsAnnotationalSettings(TrainerModel<?> model, OutputStream out) throws IOException, SettingsException {
		AnnotationalSettings.saveSettings("trainer", new OutputStreamWriter(out, Charset.forName("utf-8")), new ModelSaver(model));
	}

	private void writeModelAsZippedAnnotationalSettings(TrainerModel<?> model, OutputStream out) throws IOException, SettingsException {
		ZipOutputStream zos = new ZipOutputStream(out);
		try {
			zos.putNextEntry(new ZipEntry(ZIP_XML_FILENAME));
			writeModelAsAnnotationalSettings(model, new FilterOutputStream(zos) {
				@Override
				public void close() throws IOException {}
			});
		}
		finally {
			zos.close();
		}
	}

	private void writeModelAsText(TrainerModel<?> model, OutputStream out) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("utf-8"));
		for (TrainerItem item : model) {
			for (String value : item.getValues()) {
				writer.append(value).append(",");
			}
			writer.append("\n");
		}
		writer.flush();
	}

	public static class ModelSaver {
		private TrainerModel<?> model;

		public ModelSaver(TrainerModel<?> model) {
			this.model = model;
		}

		@Setting("model")
		public TrainerModel<?> getModel() {
			return model;
		}

		@Setting("model")
		public void setModel(TrainerModel<?> model) {
			this.model = model;
		}
	}
}
