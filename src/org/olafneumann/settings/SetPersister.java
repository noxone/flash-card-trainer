package org.olafneumann.settings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.Element;

class SetPersister implements SettingsPersister<Set<?>> {
	private static final String ITEM = "item";

	@Override
	public boolean save(Saver saver, Element xml,Type type, Set<?> object) throws SettingsException {
		saver.setSaved(object);
		for (Object item : object) {
			saver.saveXml(xml, item.getClass(), ITEM, item);
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Set<?> load(Loader loader, Element xml, Type type) throws SettingsException {
		Set set = new HashSet<Object>();
		loader.setLoaded(xml.getAttributeValue(ObjectRegistry.XML_ATTR_ID), set);
		for (Element child : xml.getChildren(ITEM)) {
			if (type instanceof ParameterizedType)
				set.add(loader.loadXml(child, ((ParameterizedType) type).getActualTypeArguments()[0]));
			else
				set.add(loader.loadXml(child, Object.class));
		}
		return set;
	}

	@Override
	public Class<?> getType() {
		return Set.class;
	}

	public static final TypeDescription TYPE_DESCRIPTION = new TypeDescription() {
		@Override
		public boolean appliesTo(Type type) {
			if (type instanceof Class<?>)
				return Set.class.isAssignableFrom((Class<?>) type);
			else if (type instanceof ParameterizedType)
				return appliesTo(((ParameterizedType) type).getRawType());
			else
				System.err.println("Unknown type: " + type + " - " + type.getClass().getName());
			return false;
		}
	};
}
