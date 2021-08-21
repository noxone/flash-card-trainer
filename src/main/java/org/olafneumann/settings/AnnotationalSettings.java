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

	public static synchronized void loadSettings(String rootName, File file, Object object, TypeProvider typeProvider)
			throws SettingsException, FileNotFoundException {
		loadSettings(rootName, new BufferedReader(new FileReader(file)), object, typeProvider);
	}

	public static void loadSettings(String rootName, Reader reader, Object object, TypeProvider typeProvider) throws SettingsException {
		new AnnotationalSettings(rootName).loadSettings(reader, object, typeProvider);
	}

	public static void saveSettings(String rootName, Writer writer, Object object) throws IOException, SettingsException {
		new AnnotationalSettings(rootName).saveSettings(writer, object);
	}

	public static synchronized void saveSettings(String rootName, File file, Object object) throws IOException, SettingsException {
		saveSettings(rootName, new BufferedWriter(new FileWriter(file)), object);
	}

	private final Map<TypeDescription, SettingsPersister<?>> loaders = new HashMap<TypeDescription, SettingsPersister<?>>();
	private final Map<Type, SettingsPersister<?>> usedPersisters = new HashMap<Type, SettingsPersister<?>>();

	private String rootName;
	private final ObjectRegistry objectRegistry = new ObjectRegistry();

	private TypeProvider typeProvider = TypeProvider.NULL_TYPE_PROVIDER;

	protected AnnotationalSettings(String rootName) {
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

	private SettingsPersister<?> getPersister(Type type) throws SettingsException {
		if (!usedPersisters.containsKey(type)) {
			Set<SettingsPersister<?>> persisters = new HashSet<SettingsPersister<?>>();
			for (Entry<TypeDescription, SettingsPersister<?>> entry : loaders.entrySet()) {
				if (entry.getKey().appliesTo(type))
					persisters.add(entry.getValue());
			}
			// remove unspecific persisters
			List<SettingsPersister<?>> list = new ArrayList<SettingsPersister<?>>(persisters);
			for (int a = 0; a < list.size() - 1; ++a) {
				for (int b = a + 1; b < list.size(); ++b) {
					SettingsPersister<?> first = list.get(a);
					SettingsPersister<?> secon = list.get(b);
					if (first.getType().isAssignableFrom(secon.getType())) {
						// secon ist der spezifischere
						persisters.remove(first);
					} else if (secon.getType().isAssignableFrom(first.getType())) {
						// first ist der spezifischere
						persisters.remove(secon);
					} else {}
				}
			}
			if (persisters.isEmpty()) {
				throw new SettingsException("No SettingsPersister found for type: " + type.toString());
			}
			// if we have more than one persister left now, we have a problem
			if (persisters.size() > 1) {
				System.err.println("Type [" + type.toString() + "] has more than one possible persister: " + persisters);
			}
			usedPersisters.put(type, persisters.iterator().next());
		}
		return usedPersisters.get(type);
	}

	private SettingsPersister<?> getPersister(Element element) {
		return getPersister(element.getAttributeValue(XML_ATTR_PERSISTER));
	}

	private SettingsPersister<?> getPersister(String persisterClassName) {
		if (persisterClassName == null) {
			return null;
		} else {
			for (SettingsPersister<?> persister : loaders.values()) {
				if (persister.getClass().getName().equals(persisterClassName)) {
					return persister;
				}
			}
			if (!persisterClassName.contains("."))
				return getPersister(getClass().getPackage().getName() + "." + persisterClassName);
			else
				return null;
		}
	}

	protected void registerPersister(Type type, SettingsPersister<?> loader) {
		registerPersister(new DefaultTypeDescription(type), loader);
	}

	protected void registerPersister(TypeDescription type, SettingsPersister<?> loader) {
		loaders.put(type, loader);
	}

	protected String getRootName() {
		return rootName != null ? rootName : XML_ROOT;
	}

	void setRootName(String rootName) {
		this.rootName = rootName;
	}

	private final void saveSettings(Writer writer, Object settings) throws IOException, SettingsException {
		try {
			objectRegistry.clear();
			writeXml(createXml(settings), writer);
		}
		finally {
			objectRegistry.clear();
		}
	}

	private final void loadSettings(Reader reader, Object settings, TypeProvider typeProvider) throws SettingsException {
		this.typeProvider = typeProvider != null ? typeProvider : TypeProvider.NULL_TYPE_PROVIDER;
		try {
			objectRegistry.clear();
			Element root = readXml(reader);
			loadXml(settings, root);
		}
		catch (SettingsException e) {
			throw e;
		}
		catch (Exception e) {
			throw new SettingsException(e.getLocalizedMessage(), e);
		}
		finally {
			objectRegistry.clear();
		}
	}

	/**
	 * Lädt Einstellungen der Version 1. Diese Zeichnen sich dadurch aus, dass sämtliche Informationen, die das Programm
	 * speichert, in einer einzigen XML-Datei abgelegt werden. Kann die XML-Datei am aus irgendeinem Grund nicht geladen
	 * werden, weigert sich das gesamte Programm zu starten. Daher wurde Version 2 eingeführt. Hier werden die Dateien
	 * aufgeteilt, so dass das Programm immer fähig ist, zu laden, auch wenn in der History oder den Favouriten ein
	 * Fehler vorliegt.
	 * 
	 * @param root
	 *            Root-XML-Element der edt.xml
	 * @throws SettingsException
	 */
	private void loadXml(Object object, Element root) throws SettingsException {
		for (SettingItem item : getSettingItems(object)) {
			item.set(loadXml(root.getChild(item.getSettingName()), item.getGenericType()));
		}
	}

	private Object loadXml(Element element, Type type) throws SettingsException {
		Object object = objectRegistry.load(element);
		if (object != null) {
			return object;
		} else {
			SettingsPersister<?> persister = getPersister(element);
			if (persister == null)
				persister = getPersister(type/* , generics */);
			if (persister != null) {
				object = persister.load(LOADER, element, type);
				objectRegistry.setLoaded(element, object);
				return object;
			} else {
				throw new RuntimeException("No SettingsLoader registered for type: " + type);
			}
		}
	}

	/**
	 * Speichert die Einstellungen nach version 1.0
	 * 
	 * @throws SettingsException
	 */
	private Element createXml(Object object) throws SettingsException {
		Element root = new Element(getRootName());
		root.setAttribute(new Attribute(XML_ROOT_VERSION, XML_ROOT_VERSION_1));

		for (SettingItem item : getSettingItems(object)) {
			root.addContent(createXml(item.getGenericType(), item.getSettingName(), item.get()));
		}

		return root;
	}

	private Element createXml(Type type, String name, Object value) throws SettingsException {
		if (!objectRegistry.isSaved(value)) {
			@SuppressWarnings("unchecked")
			SettingsPersister<Object> persister = (SettingsPersister<Object>) getPersister(type);
			if (persister != null) {
				Element element = new Element(name);
				element.setAttribute(XML_ATTR_PERSISTER, getPersisterName(persister));
				if (value != null) {
					element.setAttribute(XML_ATTR_ID, objectRegistry.getId(value));
					persister.save(SAVER, element, type, value);
				} else {
					element.setAttribute(XML_ATTR_NULL, "null");
				}
				return element;
			} else
				throw new RuntimeException("Unknown value class: " + value.getClass().getName());
		} else {
			Element element = new Element(name);
			element.setAttribute(XML_ATTR_ID, objectRegistry.getId(value));
			return element;
		}
	}

	private String getPersisterName(SettingsPersister<?> persister) {
		Class<?> clazz = persister.getClass();
		if (clazz.getPackage().equals(getClass().getPackage()))
			return clazz.getSimpleName();
		else
			return clazz.getName();
	}

	private void writeXml(Element root, Writer writer) throws IOException {
		Document doc = new Document(root);
		XMLOutputter Xout = new XMLOutputter(Format.getPrettyFormat());
		Xout.output(doc, writer);
	}

	private Element readXml(Reader reader) throws SettingsException {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(reader);
			return doc.getRootElement();
		}
		catch (Exception e) {
			throw new SettingsException(e.getLocalizedMessage(), e);
		}
	}

	private Collection<SettingItem> getSettingItems(Object object) {
		// if (settingItems == null) {
		// settingItems = loadSettingItems(object);
		// }
		// return settingItems;
		return loadSettingItems(object);
	}

	private Collection<SettingItem> loadSettingItems(Object object) {
		Map<String, Method> getters = new HashMap<String, Method>();
		Map<String, Method> setters = new HashMap<String, Method>();

		for (Method method : object.getClass().getDeclaredMethods()) {
			if (method.getAnnotation(Setting.class) == null || method.isSynthetic())
				continue;

			if (method.getName().startsWith("get") && method.getParameterTypes().length == 0)
				getters.put(method.getName().substring(3), method);
			else if (method.getName().startsWith("is") && method.getParameterTypes().length == 0)
				getters.put(method.getName().substring(2), method);
			else if (method.getName().startsWith("set") && method.getParameterTypes().length == 1 && method.getReturnType() == void.class)
				setters.put(method.getName().substring(3), method);
		}

		Set<String> names = new HashSet<String>(getters.keySet());
		names.retainAll(setters.keySet());

		List<SettingItem> items = new ArrayList<SettingItem>();
		for (String name : names) {
			Method getter = getters.get(name);
			Method setter = setters.get(name);
			String getterName = getter.getAnnotation(Setting.class).value();
			String setterName = getter.getAnnotation(Setting.class).value();

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
		public <T> T loadXml(Element xml, Type type) throws SettingsException {
			return (T) AnnotationalSettings.this.loadXml(xml, type);
		}

		@Override
		public void setLoaded(String id, Object object) {
			objectRegistry.setLoaded(id, object);
		}

		@Override
		public Type getType(Element element) {
			return getTypeProvider().getType(element);
		}
	};

	private final Saver SAVER = new Saver() {
		@Override
		public <T> void saveXml(Element parent, Type type, String name, T object) throws SettingsException {
			parent.addContent(createXml(type, name, object));
		}

		@Override
		public void setSaved(Object object) {
			objectRegistry.save(object);
		}
	};
}