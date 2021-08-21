package org.olafneumann.settings;

import java.lang.reflect.Type;

public interface TypeDescription {
	boolean appliesTo(Type type);
}
