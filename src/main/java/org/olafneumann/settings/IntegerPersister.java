package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

class IntegerPersister implements SettingsPersister<Integer>{
	@Override
	public boolean save(Saver saver, Element xml,Type type, Integer object) {
		xml.setText(object.toString());
		return true;
	}

	@Override
	public Integer load(Loader loader, Element xml, Type type) {
		return Integer.parseInt(xml.getText());
	}

	@Override
	public Class<?> getType() {
		return Integer.class;
	}
}
