package org.olafneumann.settings;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

class SettingItem {
	private final Object settings;
	private final String name;
	private final Method getter;
	private final Method setter;

	SettingItem(final Object settings, final String name, final Method getter, final Method setter) {
		this.settings = settings;
		this.name = name;
		this.getter = getter;
		this.setter = setter;
	}

	String getSettingName() {
		return name;
	}

	Type getGenericType() {
		return getter.getGenericReturnType();
	}

	void set(final Object object) {
		try {
			setter.invoke(settings, object);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	Object get() {
		try {
			return getter.invoke(settings);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "Item(" + name + ")";
	}
}