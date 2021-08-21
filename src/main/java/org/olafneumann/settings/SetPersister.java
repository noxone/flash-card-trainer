package org.olafneumann.settings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.Element;

class SetPersister implements SettingsPersister<Set<?>> {
	private static final String ITEM = "item";

	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final Set<?> object)
			throws SettingsException {
		saver.setSaved(object);
		for (final Object item : object) {
			saver.saveXml(xml, item.getClass(), ITEM, item);
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Set<?> load(final Loader loader, final Element xml, final Type type) throws SettingsException {
		final Set set = new HashSet<>();
		loader.setLoaded(xml.getAttributeValue(ObjectRegistry.XML_ATTR_ID), set);
		for (final Element child : xml.getChildren(ITEM)) {
			if (type instanceof ParameterizedType) {
				set.add(loader.loadXml(child, ((ParameterizedType) type).getActualTypeArguments()[0]));
			} else {
				set.add(loader.loadXml(child, Object.class));
			}
		}
		return set;
	}

	@Override
	public Class<?> getType() {
		return Set.class;
	}

	public static final TypeDescription TYPE_DESCRIPTION = type -> {
		if (type instanceof Class<?>) {
			return Set.class.isAssignableFrom((Class<?>) type);
		}
		if (type instanceof ParameterizedType) {
			return SetPersister.TYPE_DESCRIPTION.appliesTo(((ParameterizedType) type).getRawType());
		} else {
			System.err.println("Unknown type: " + type + " - " + type.getClass().getName());
		}
		return false;
	};
}
