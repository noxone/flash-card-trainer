package org.olafneumann.settings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

class DefaultTypeDescription implements TypeDescription {
	private final Type type;
	private final Type[] generics;

	DefaultTypeDescription(Type type, Type... generics) {
		this.type = type;
		this.generics = generics != null ? generics : new Type[0];
	}

	@Override
	public boolean appliesTo(Type type) {
		if (type instanceof Class) {
			return this.type.equals(type);
		} else if (type instanceof ParameterizedType) {
			return this.type.equals(((ParameterizedType) type).getRawType())
					&& compareGenerics(((ParameterizedType) type).getActualTypeArguments());
		} else {
			System.err.println("Unknown type: " + type + " - " + type.getClass().getName());
		}
		return false;
	}

	private boolean compareGenerics(Type[] generics) {
		if (generics == null || this.generics.length != generics.length)
			return false;
		for (int i = 0; i < generics.length; ++i)
			if (!this.generics[i].equals(generics[i]))
				return false;
		return true;
	}

	@Override
	public String toString() {
		return "DefaultTypeDescription [type=" + type + ", generics=" + Arrays.toString(generics) + "]";
	}
}
