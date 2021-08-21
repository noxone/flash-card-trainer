package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;

class LongPersister implements SettingsPersister<Long> {
	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final Long object) {
		xml.setText(object.toString());
		return true;
	}

	@Override
	public Long load(final Loader loader, final Element xml, final Type type) {
		return Long.parseLong(xml.getText());
	}

	@Override
	public Class<?> getType() {
		return Long.class;
	}
}
