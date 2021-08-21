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
	public boolean save(final Saver saver, final Element xml, final Type type, final Date date)
			throws SettingsException {
		saver.setSaved(date);
		if (date != null) {
			return persister.save(saver, xml, type, date.getTime());
		}
		return false;
	}

	@Override
	public Date load(final Loader loader, final Element xml, final Type type) throws SettingsException {
		try {
			return new Date(persister.load(loader, xml, type));
		} catch (final NumberFormatException e) {
			return null;
		}
	}
}
