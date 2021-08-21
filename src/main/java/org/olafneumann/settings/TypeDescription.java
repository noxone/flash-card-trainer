package org.olafneumann.settings;

import java.lang.reflect.Type;

@FunctionalInterface
public interface TypeDescription {
	boolean appliesTo(Type type);
}
