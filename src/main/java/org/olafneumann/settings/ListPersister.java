package org.olafneumann.settings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

class ListPersister implements SettingsPersister<List<?>> {
	private static final String ITEM = "item";

	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final List<?> object)
			throws SettingsException {
		saver.setSaved(object);
		for (final Object item : object) {
			saver.saveXml(xml, item.getClass(), ITEM, item);
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<?> load(final Loader loader, final Element xml, final Type type) throws SettingsException {
		final List list = new ArrayList<>();
		loader.setLoaded(xml.getAttributeValue(ObjectRegistry.XML_ATTR_ID), list);
		for (final Element child : xml.getChildren(ITEM)) {
			if (type instanceof ParameterizedType) {
				list.add(loader.loadXml(child, ((ParameterizedType) type).getActualTypeArguments()[0]));
			} else {
				list.add(loader.loadXml(child, Object.class));
			}
		}
		return list;
	}

	@Override
	public Class<?> getType() {
		return List.class;
	}

	public static final TypeDescription TYPE_DESCRIPTION = type -> {
		if (type instanceof Class<?>) {
			return List.class.isAssignableFrom((Class<?>) type);
		}
		if (type instanceof ParameterizedType) {
			return ListPersister.TYPE_DESCRIPTION.appliesTo(((ParameterizedType) type).getRawType());
		} else {
			System.err.println("Unknown type: " + type + " - " + type.getClass().getName());
		}
		return false;
	};
}
