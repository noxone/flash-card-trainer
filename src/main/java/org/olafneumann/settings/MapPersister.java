package org.olafneumann.settings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Element;

class MapPersister implements SettingsPersister<Map<?, ?>> {
	private static final String ENTRY = "entry";
	private static final String KEY = "key";
	private static final String VALUE = "value";

	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final Map<?, ?> map)
			throws SettingsException {
		saver.setSaved(map);
		for (final Entry<?, ?> entry : map.entrySet()) {
			final Element item = new Element(ENTRY);
			xml.addContent(item);
			saver.saveXml(item, entry.getKey().getClass(), KEY, entry.getKey());
			saver.saveXml(item, entry.getValue().getClass(), VALUE, entry.getValue());
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<?, ?> load(final Loader loader, final Element xml, final Type type) throws SettingsException {
		final Map map = new HashMap();
		loader.setLoaded(xml.getAttributeValue(ObjectRegistry.XML_ATTR_ID), map);
		for (final Element child : xml.getChildren(ENTRY)) {
			final Object key = type instanceof ParameterizedType //
					? loader.loadXml(child.getChild(KEY), ((ParameterizedType) type).getActualTypeArguments()[0]) //
					: loader.loadXml(child.getChild(KEY), Object.class);
			final Object value = type instanceof ParameterizedType //
					? loader.loadXml(child.getChild(VALUE), ((ParameterizedType) type).getActualTypeArguments()[0]) //
					: loader.loadXml(child.getChild(VALUE), Object.class);
			map.put(key, value);
		}
		return map;
	}

	@Override
	public Class<?> getType() {
		return Map.class;
	}

	public static final TypeDescription TYPE_DESCRIPTION = type -> {
		if (type instanceof Class<?>) {
			return Map.class.isAssignableFrom((Class<?>) type);
		}
		if (type instanceof ParameterizedType) {
			return MapPersister.TYPE_DESCRIPTION.appliesTo(((ParameterizedType) type).getRawType());
		} else {
			System.err.println("Unknown type: " + type + " - " + type.getClass().getName());
		}
		return false;
	};
}
