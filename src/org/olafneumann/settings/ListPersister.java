package org.olafneumann.settings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

class ListPersister implements SettingsPersister<List<?>> {
	private static final String ITEM = "item";

	@Override
	public boolean save(Saver saver, Element xml,Type type, List<?> object) throws SettingsException {
		saver.setSaved(object);
		for (Object item : object) {
			saver.saveXml(xml, item.getClass(), ITEM, item);
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<?> load(Loader loader, Element xml, Type type) throws SettingsException {
		List list = new ArrayList<Object>();
		loader.setLoaded(xml.getAttributeValue(ObjectRegistry.XML_ATTR_ID), list);
		for (Element child : xml.getChildren(ITEM)) {
			if (type instanceof ParameterizedType)
				list.add(loader.loadXml(child, ((ParameterizedType) type).getActualTypeArguments()[0]));
			else
				list.add(loader.loadXml(child, Object.class));
		}
		return list;
	}

	@Override
	public Class<?> getType() {
		return List.class;
	}

	public static final TypeDescription TYPE_DESCRIPTION = new TypeDescription() {
		@Override
		public boolean appliesTo(Type type) {
			if (type instanceof Class<?>)
				return List.class.isAssignableFrom((Class<?>) type);
			else if (type instanceof ParameterizedType)
				return appliesTo(((ParameterizedType) type).getRawType());
			else
				System.err.println("Unknown type: " + type + " - " + type.getClass().getName());
			return false;
		}
	};
}
