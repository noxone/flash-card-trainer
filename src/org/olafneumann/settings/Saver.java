package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

public interface Saver {
	<T> void saveXml(Element parent, Type type, String name, T object)throws SettingsException;
	void setSaved(Object object);
}
