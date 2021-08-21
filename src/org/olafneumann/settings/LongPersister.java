package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

class LongPersister implements SettingsPersister<Long> {
	@Override
	public boolean save(Saver saver, Element xml, Type type, Long object) {
		xml.setText(object.toString());
		return true;
	}

	@Override
	public Long load(Loader loader, Element xml, Type type) {
		return Long.parseLong(xml.getText());
	}

	@Override
	public Class<?> getType() {
		return Long.class;
	}
}
