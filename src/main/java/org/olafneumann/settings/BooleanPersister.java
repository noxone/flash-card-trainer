package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

class BooleanPersister implements SettingsPersister<Boolean> {
	@Override
	public boolean save(Saver saver, Element xml,Type type, Boolean object) {
		xml.setText(object.toString());
		return true;
	}

	@Override
	public Boolean load(Loader loader, Element xml, Type type) {
		return Boolean.parseBoolean(xml.getText());
	}

	@Override
	public Class<?> getType() {
		return Boolean.class;
	}
}
