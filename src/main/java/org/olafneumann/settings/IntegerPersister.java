package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

class IntegerPersister implements SettingsPersister<Integer> {
	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final Integer object) {
		xml.setText(object.toString());
		return true;
	}

	@Override
	public Integer load(final Loader loader, final Element xml, final Type type) {
		return Integer.parseInt(xml.getText());
	}

	@Override
	public Class<?> getType() {
		return Integer.class;
	}
}
