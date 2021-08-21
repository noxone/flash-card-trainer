package org.olafneumann.settings;

import java.lang.reflect.Type;
import java.util.Date;

import org.jdom2.Element;

public class DatePersister implements SettingsPersister<Date> {
	private final LongPersister persister = new LongPersister();

	@Override
	public Class<?> getType() {
		return Date.class;
	}

	@Override
	public boolean save(Saver saver, Element xml, Type type, Date date) throws SettingsException {
		saver.setSaved(date);
		if (date != null)
			return persister.save(saver, xml, type, date.getTime());
		else
			return false;
	}

	@Override
	public Date load(Loader loader, Element xml, Type type) throws SettingsException {
		try {
			return new Date(persister.load(loader, xml, type));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}
