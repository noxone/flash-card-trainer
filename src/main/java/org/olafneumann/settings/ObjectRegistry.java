package org.olafneumann.settings;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.jdom2.Element;

class ObjectRegistry {
	static final String XML_ATTR_ID = "id";

	private final Map<String, Object> map = new HashMap<String, Object>();
	private final Map<Object, String> ids = new IdentityHashMap<Object, String>();

	ObjectRegistry() {}

	void clear() {
		map.clear();
		ids.clear();
	}

	public String save(Object object) {
		String id = getId(object);
		map.put(id, object);
		return id;
	}

	public synchronized String getId(Object object) {
		// return Integer.toHexString(System.identityHashCode(object));
		if (!ids.containsKey(object))
			ids.put(object, Integer.toHexString(ids.size()));
		return ids.get(object);
	}

	public boolean isSaved(Object object) {
		return map.containsKey(getId(object));
	}

	public String getId(Element element) {
		return element.getAttributeValue(XML_ATTR_ID);
	}

	public Object load(Element element) {
		String id = getId(element);
		if (id != null && map.containsKey(id)) {
			return map.get(id);
		} else {
			return null;
		}
	}

	public void setLoaded(Element element, Object object) {
		setLoaded(element.getAttributeValue(XML_ATTR_ID), object);
	}

	public void setLoaded(String id, Object object) {
		map.put(id, object);
	}
}
