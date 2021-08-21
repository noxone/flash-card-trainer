package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

public interface Loader {
	<T> T loadXml(Element xml, Type type) throws SettingsException;

	void setLoaded(String id, Object object);

	Type getType(Element element);
}
