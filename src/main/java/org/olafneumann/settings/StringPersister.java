package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.CDATA;
import org.jdom2.Element;

class StringPersister implements SettingsPersister<String> {
	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final String object) {
		saver.setSaved(object);
		xml.setContent(new CDATA(object));
		return true;
	}

	@Override
	public String load(final Loader loader, final Element xml, final Type type) {
		return xml.getText();
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}
}