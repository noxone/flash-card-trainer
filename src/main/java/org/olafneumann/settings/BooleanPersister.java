package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

class BooleanPersister implements SettingsPersister<Boolean> {
	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final Boolean object) {
		xml.setText(object.toString());
		return true;
	}

	@Override
	public Boolean load(final Loader loader, final Element xml, final Type type) {
		return Boolean.parseBoolean(xml.getText());
	}

	@Override
	public Class<?> getType() {
		return Boolean.class;
	}
}
