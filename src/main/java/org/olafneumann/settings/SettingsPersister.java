package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

public interface SettingsPersister<T> {
	Class<?> getType();

	boolean save(Saver saver, Element xml, Type type, T object) throws SettingsException;

	T load(Loader loader, Element xml, Type type) throws SettingsException;
}
