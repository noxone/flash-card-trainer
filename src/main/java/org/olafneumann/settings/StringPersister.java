package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.CDATA;
import org.jdom2.Element;

class StringPersister implements SettingsPersister<String> {
	@Override
	public boolean save(Saver saver, Element xml,Type type, String object) {
		saver.setSaved(object);
		xml.setContent(new CDATA(object));
		return true;
	}

	@Override
	public String load(Loader loader, Element xml, Type type) {
		return xml.getText();
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}
}