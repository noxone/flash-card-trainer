package org.olafneumann.settings;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.jdom2.Element;

class ObjectRegistry {
	static final String XML_ATTR_ID = "id";

	private final Map<String, Object> map = new HashMap<>();
	private final Map<Object, String> ids = new IdentityHashMap<>();

	ObjectRegistry() {
	}

	void clear() {
		map.clear();
		ids.clear();
	}

	public String save(final Object object) {
		final String id = getId(object);
		map.put(id, object);
		return id;
	}

	public synchronized String getId(final Object object) {
		// return Integer.toHexString(System.identityHashCode(object));
		if (!ids.containsKey(object)) {
			ids.put(object, Integer.toHexString(ids.size()));
		}
		return ids.get(object);
	}

	public boolean isSaved(final Object object) {
		return map.containsKey(getId(object));
	}

	public String getId(final Element element) {
		return element.getAttributeValue(XML_ATTR_ID);
	}

	public Object load(final Element element) {
		final String id = getId(element);
		if (id != null && map.containsKey(id)) {
			return map.get(id);
		}
		return null;
	}

	public void setLoaded(final Element element, final Object object) {
		setLoaded(element.getAttributeValue(XML_ATTR_ID), object);
	}

	public void setLoaded(final String id, final Object object) {
		map.put(id, object);
	}
}
