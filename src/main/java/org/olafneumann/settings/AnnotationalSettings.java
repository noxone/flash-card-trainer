package org.olafneumann.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class AnnotationalSettings {
	private static final String XML_ROOT = "settings";
	private static final String XML_ROOT_VERSION = "version";
	private static final String XML_ROOT_VERSION_1 = "1.0";

	private static final String XML_ATTR_ID = ObjectRegistry.XML_ATTR_ID;
	private static final String XML_ATTR_NULL = "null";
	private static final String XML_ATTR_PERSISTER = "persister";

	public static synchronized void loadSettings(final String rootName, final File file, final Object object,
			final TypeProvider typeProvider) throws SettingsException, FileNotFoundException {
		loadSettings(rootName, new BufferedReader(new FileReader(file)), object, typeProvider);
	}

	public static void loadSettings(final String rootName, final Reader reader, final Object object,
			final TypeProvider typeProvider) throws SettingsException {
		new AnnotationalSettings(rootName).loadSettings(reader, object, typeProvider);
	}

	public static void saveSettings(final String rootName, final Writer writer, final Object object)
			throws IOException, SettingsException {
		new AnnotationalSettings(rootName).saveSettings(writer, object);
	}

	public static synchronized void saveSettings(final String rootName, final File file, final Object object)
			throws IOException, SettingsException {
		saveSettings(rootName, new BufferedWriter(new FileWriter(file)), object);
	}

	private final Map<TypeDescription, SettingsPersister<?>> loaders = new HashMap<>();
	private final Map<Type, SettingsPersister<?>> usedPersisters = new HashMap<>();

	private String rootName;
	private final ObjectRegistry objectRegistry = new ObjectRegistry();

	private TypeProvider typeProvider = TypeProvider.NULL_TYPE_PROVIDER;

	protected AnnotationalSettings(final String rootName) {
		this.rootName = rootName;
		registerPersister(BeanPersister.TYPE_DESCRIPTION, new BeanPersister());
		registerPersister(int.class, new IntegerPersister());
		registerPersister(Integer.class, new IntegerPersister());
		registerPersister(long.class, new LongPersister());
		registerPersister(Long.class, new LongPersister());
		registerPersister(boolean.class, new BooleanPersister());
		registerPersister(Boolean.class, new BooleanPersister());
		registerPersister(String.class, new StringPersister());
		registerPersister(Date.class, new DatePersister());
		registerPersister(ListPersister.TYPE_DESCRIPTION, new ListPersister());
		registerPersister(SetPersister.TYPE_DESCRIPTION, new SetPersister());
		registerPersister(MapPersister.TYPE_DESCRIPTION, new MapPersister());
	}

	private SettingsPersister<?> getPersister(final Type type) throws SettingsException {
		if (!usedPersisters.containsKey(type)) {
			final Set<SettingsPersister<?>> persisters = new HashSet<>();
			for (final Entry<TypeDescription, SettingsPersister<?>> entry : loaders.entrySet()) {
				if (entry.getKey().appliesTo(type)) {
					persisters.add(entry.getValue());
				}
			}
			// remove unspecific persisters
			final List<SettingsPersister<?>> list = new ArrayList<>(persisters);
			for (int a = 0; a < list.size() - 1; ++a) {
				for (int b = a + 1; b < list.size(); ++b) {
					final SettingsPersister<?> first = list.get(a);
					final SettingsPersister<?> secon = list.get(b);
					if (first.getType().isAssignableFrom(secon.getType())) {
						// secon ist der spezifischere
						persisters.remove(first);
					} else if (secon.getType().isAssignableFrom(first.getType())) {
						// first ist der spezifischere
						persisters.remove(secon);
					} else {
					}
				}
			}
			if (persisters.isEmpty()) {
				throw new SettingsException("No SettingsPersister found for type: " + type.toString());
			}
			// if we have more than one persister left now, we have a problem
			if (persisters.size() > 1) {
				System.err
						.println("Type [" + type.toString() + "] has more than one possible persister: " + persisters);
			}
			usedPersisters.put(type, persisters.iterator().next());
		}
		return usedPersisters.get(type);
	}

	private SettingsPersister<?> getPersister(final Element element) {
		return getPersister(element.getAttributeValue(XML_ATTR_PERSISTER));
	}

	private SettingsPersister<?> getPersister(final String persisterClassName) {
		if (persisterClassName == null) {
			return null;
		}
		for (final SettingsPersister<?> persister : loaders.values()) {
			if (persister.getClass().getName().equals(persisterClassName)) {
				return persister;
			}
		}
		if (!persisterClassName.contains(".")) {
			return getPersister(getClass().getPackage().getName() + "." + persisterClassName);
		} else {
			return null;
		}
	}

	protected void registerPersister(final Type type, final SettingsPersister<?> loader) {
		registerPersister(new DefaultTypeDescription(type), loader);
	}

	protected void registerPersister(final TypeDescription type, final SettingsPersister<?> loader) {
		loaders.put(type, loader);
	}

	protected String getRootName() {
		return rootName != null ? rootName : XML_ROOT;
	}

	void setRootName(final String rootName) {
		this.rootName = rootName;
	}

	private final void saveSettings(final Writer writer, final Object settings) throws IOException, SettingsException {
		try {
			objectRegistry.clear();
			writeXml(createXml(settings), writer);
		} finally {
			objectRegistry.clear();
		}
	}

	private final void loadSettings(final Reader reader, final Object settings, final TypeProvider typeProvider)
			throws SettingsException {
		this.typeProvider = typeProvider != null ? typeProvider : TypeProvider.NULL_TYPE_PROVIDER;
		try {
			objectRegistry.clear();
			final Element root = readXml(reader);
			loadXml(settings, root);
		} catch (final SettingsException e) {
			throw e;
		} catch (final Exception e) {
			throw new SettingsException(e.getLocalizedMessage(), e);
		} finally {
			objectRegistry.clear();
		}
	}

	/**
	 * Lädt Einstellungen der Version 1. Diese Zeichnen sich dadurch aus, dass
	 * sämtliche Informationen, die das Programm speichert, in einer einzigen
	 * XML-Datei abgelegt werden. Kann die XML-Datei am aus irgendeinem Grund nicht
	 * geladen werden, weigert sich das gesamte Programm zu starten. Daher wurde
	 * Version 2 eingeführt. Hier werden die Dateien aufgeteilt, so dass das
	 * Programm immer fähig ist, zu laden, auch wenn in der History oder den
	 * Favouriten ein Fehler vorliegt.
	 *
	 * @param root Root-XML-Element der edt.xml
	 * @throws SettingsException
	 */
	private void loadXml(final Object object, final Element root) throws SettingsException {
		for (final SettingItem item : getSettingItems(object)) {
			item.set(loadXml(root.getChild(item.getSettingName()), item.getGenericType()));
		}
	}

	private Object loadXml(final Element element, final Type type) throws SettingsException {
		Object object = objectRegistry.load(element);
		if (object != null) {
			return object;
		}
		SettingsPersister<?> persister = getPersister(element);
		if (persister == null) {
			persister = getPersister(type/* , generics */);
		}
		if (persister != null) {
			object = persister.load(LOADER, element, type);
			objectRegistry.setLoaded(element, object);
			return object;
		} else {
			throw new RuntimeException("No SettingsLoader registered for type: " + type);
		}
	}

	/**
	 * Speichert die Einstellungen nach version 1.0
	 *
	 * @throws SettingsException
	 */
	private Element createXml(final Object object) throws SettingsException {
		final Element root = new Element(getRootName());
		root.setAttribute(new Attribute(XML_ROOT_VERSION, XML_ROOT_VERSION_1));

		for (final SettingItem item : getSettingItems(object)) {
			root.addContent(createXml(item.getGenericType(), item.getSettingName(), item.get()));
		}

		return root;
	}

	private Element createXml(final Type type, final String name, final Object value) throws SettingsException {
		if (objectRegistry.isSaved(value)) {
			final Element element = new Element(name);
			element.setAttribute(XML_ATTR_ID, objectRegistry.getId(value));
			return element;
		}
		@SuppressWarnings("unchecked")
		final SettingsPersister<Object> persister = (SettingsPersister<Object>) getPersister(type);
		if (persister != null) {
			final Element element = new Element(name);
			element.setAttribute(XML_ATTR_PERSISTER, getPersisterName(persister));
			if (value != null) {
				element.setAttribute(XML_ATTR_ID, objectRegistry.getId(value));
				persister.save(SAVER, element, type, value);
			} else {
				element.setAttribute(XML_ATTR_NULL, "null");
			}
			return element;
		} else {
			throw new RuntimeException("Unknown value class: " + value.getClass().getName());
		}
	}

	private String getPersisterName(final SettingsPersister<?> persister) {
		final Class<?> clazz = persister.getClass();
		if (clazz.getPackage().equals(getClass().getPackage())) {
			return clazz.getSimpleName();
		}
		return clazz.getName();
	}

	private void writeXml(final Element root, final Writer writer) throws IOException {
		final Document doc = new Document(root);
		final XMLOutputter Xout = new XMLOutputter(Format.getPrettyFormat());
		Xout.output(doc, writer);
	}

	private Element readXml(final Reader reader) throws SettingsException {
		final SAXBuilder builder = new SAXBuilder();
		try {
			final Document doc = builder.build(reader);
			return doc.getRootElement();
		} catch (final Exception e) {
			throw new SettingsException(e.getLocalizedMessage(), e);
		}
	}

	private Collection<SettingItem> getSettingItems(final Object object) {
		// if (settingItems == null) {
		// settingItems = loadSettingItems(object);
		// }
		// return settingItems;
		return loadSettingItems(object);
	}

	private Collection<SettingItem> loadSettingItems(final Object object) {
		final Map<String, Method> getters = new HashMap<>();
		final Map<String, Method> setters = new HashMap<>();

		for (final Method method : object.getClass().getDeclaredMethods()) {
			if (method.getAnnotation(Setting.class) == null || method.isSynthetic()) {
				continue;
			}

			if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
				getters.put(method.getName().substring(3), method);
			} else if (method.getName().startsWith("is") && method.getParameterTypes().length == 0) {
				getters.put(method.getName().substring(2), method);
			} else if (method.getName().startsWith("set") && method.getParameterTypes().length == 1
					&& method.getReturnType() == void.class) {
				setters.put(method.getName().substring(3), method);
			}
		}

		final Set<String> names = new HashSet<>(getters.keySet());
		names.retainAll(setters.keySet());

		final List<SettingItem> items = new ArrayList<>();
		for (final String name : names) {
			final Method getter = getters.get(name);
			final Method setter = setters.get(name);
			final String getterName = getter.getAnnotation(Setting.class).value();
			final String setterName = getter.getAnnotation(Setting.class).value();

			if (getterName.equals(setterName) && getter.getReturnType().equals(setter.getParameterTypes()[0])) {
				items.add(new SettingItem(object, getterName, getter, setter));
			}
		}
		return items;
	}

	public TypeProvider getTypeProvider() {
		return typeProvider;
	}

	private final Loader LOADER = new Loader() {
		@SuppressWarnings("unchecked")
		@Override
		public <T> T loadXml(final Element xml, final Type type) throws SettingsException {
			return (T) AnnotationalSettings.this.loadXml(xml, type);
		}

		@Override
		public void setLoaded(final String id, final Object object) {
			objectRegistry.setLoaded(id, object);
		}

		@Override
		public Type getType(final Element element) {
			return getTypeProvider().getType(element);
		}
	};

	private final Saver SAVER = new Saver() {
		@Override
		public <T> void saveXml(final Element parent, final Type type, final String name, final T object)
				throws SettingsException {
			parent.addContent(createXml(type, name, object));
		}

		@Override
		public void setSaved(final Object object) {
			objectRegistry.save(object);
		}
	};
}