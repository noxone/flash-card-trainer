package org.olafneumann.settings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Element;

class BeanPersister implements SettingsPersister<Object> {
	private static Map<Class<?>, RealBeanPersister> persisters = new HashMap<>();

	private static RealBeanPersister getPersister(final Class<?> clazz) {
		if (!persisters.containsKey(clazz)) {
			persisters.put(clazz, new RealBeanPersister(clazz));
		}
		return persisters.get(clazz);
	}

	private static Class<?> getTypeClass(final Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType) {
			return getTypeClass(((ParameterizedType) type).getRawType());
		} else {
			throw new RuntimeException("Unknown type: " + type + " - " + type.getClass().getName());
		}
	}

	@Override
	public Class<?> getType() {
		return Object.class;
	}

	@Override
	public boolean save(final Saver saver, final Element xml, final Type type, final Object object)
			throws SettingsException {
		if (object != null) {
			return getPersister(object.getClass()).save(saver, xml, type, object);
		}
		return false;
	}

	@Override
	public Object load(final Loader loader, final Element xml, Type type) throws SettingsException {
		try {
			return getPersister(getTypeClass(type)).load(loader, xml, type);
		} catch (final SettingsException e) {
			throw e;
		} catch (final Exception e) {
			type = loader.getType(xml);
			try {
				if (type != null) {
					return getPersister(getTypeClass(type)).load(loader, xml, type);
				}
			} catch (final Exception ei) {
			}
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private static class RealBeanPersister implements SettingsPersister {
		private final Constructor<?> constructor;
		private final Map<String, GetterSetter> getterSetters = new HashMap<>();

		public RealBeanPersister(final Class<?> clazz) {
			// Find Constructor without arguments
			try {
				constructor = clazz.getConstructor();
				constructor.setAccessible(true);
			} catch (final Exception e) {
				throw new RuntimeException("No default constructor found for class: " + clazz.getName(), e);
			}

			// Find getters and setters
			final Map<String, Method> getters = new HashMap<>();
			final Map<String, Method> setters = new HashMap<>();

			// find possible getters and setters
			for (final Method method : clazz.getMethods()) {
				if (!Modifier.isPublic(method.getModifiers())) {
					continue;
				}
				if (!method.isAccessible()) {
					method.setAccessible(true);
				}

				String name = method.getName();
				if (name.startsWith("get") || name.startsWith("is")) {
					if (method.getParameterTypes().length == 0 && !void.class.equals(method.getReturnType())) {
						name = name.startsWith("get") ? name.substring(3) : name.substring(2);
						getters.put(getName(name), method);
					}
				} else if (name.startsWith("set")
						&& (void.class.equals(method.getReturnType()) && method.getParameterTypes().length == 1)) {
					name = name.substring(3);
					setters.put(getName(name), method);
				}
			}

			// find useful getters and setters
			final Set<String> names = new HashSet<>(getters.keySet());
			names.retainAll(setters.keySet());
			for (final String name : names) {
				final Method getter = getters.get(name);
				final Method setter = setters.get(name);
				if (getter.getReturnType().equals(setter.getParameterTypes()[0]) //
						|| getter.getReturnType().isAssignableFrom(setter.getParameterTypes()[0]) //
				) {
					getterSetters.put(name, new MethodGetterSetter(getter, setter));
				}
			}
			//
			// // Find fields to set
			// for (Field field : clazz.getFields()) {
			// String name = getName(field.getName());
			// if (!names.contains(name)) {
			// }
			// }
		}

		private Collection<String> getNames() {
			return getterSetters.keySet();
		}

		private GetterSetter getGetterSetter(final String name) {
			return getterSetters.get(name);
		}

		private Object createObject() throws IllegalArgumentException, InstantiationException, IllegalAccessException,
				InvocationTargetException {
			return constructor.newInstance();
		}

		@Override
		public boolean save(final Saver saver, final Element xml, final Type type, final Object object)
				throws SettingsException {
			saver.setSaved(object);
			// xml.setAttribute("class", object.getClass().getName());
			for (final String name : getNames()) {
				final GetterSetter gs = getGetterSetter(name);
				saver.saveXml(xml, gs.getType(), name, gs.get(object));
			}
			return true;
		}

		@Override
		public Object load(final Loader loader, final Element xml, final Type type) throws SettingsException {
			try {
				return doRealLoading(loader, xml, createObject());
			} catch (final SettingsException e) {
				throw e;
			} catch (final Exception e) {
				// kann der loader helfen?
				final Type typeToBeLoaded = loader.getType(xml);
				if (typeToBeLoaded != null) {
					return tryLoadingWithClass(loader, xml, typeToBeLoaded);
				}

				// mal gucken, ob eine Klasse gespeichert wurde...
				final String className = xml.getAttributeValue("class");
				if (className != null) {
					try {
						final Class<?> clazz = Class.forName(className);
						return tryLoadingWithClass(loader, xml, clazz);
					} catch (final ClassNotFoundException e1) {
						throw new SettingsException("Unable to load class: " + className);
					}
				}
				throw new SettingsException(
						"Unable to instantiate class: " + constructor.getDeclaringClass().getName());
			}
		}

		private Object tryLoadingWithClass(final Loader loader, final Element xml, final Type type)
				throws SettingsException {
			try {
				return doRealLoading(loader, xml, getTypeClass(type).newInstance());
			} catch (final SettingsException se) {
				throw se;
			} catch (final Exception e1) {
				throw new SettingsException("Unable to instantiate class: " + type.toString());
			}
		}

		private Object doRealLoading(final Loader loader, final Element xml, final Object object)
				throws SettingsException {
			loader.setLoaded(xml.getAttributeValue(ObjectRegistry.XML_ATTR_ID), object);
			for (final String name : getNames()) {
				final GetterSetter gs = getGetterSetter(name);
				final Element child = xml.getChild(name);
				if (child != null) {
					gs.set(object, loader.loadXml(child, gs.getType()));
				}
			}
			return object;
		}

		@Override
		public Class<?> getType() {
			return Object.class;
		}

		private static final String NAME_PATTERN_STRING = "([a-zA-Z])([a-zA-Z0-9]*)";
		private static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_STRING);

		private static String getName(String string) {
			if (string.startsWith("get") || string.startsWith("set")) {
				string = string.substring(3);
			}
			if (string.startsWith("is")) {
				string = string.substring(2);
			}

			final StringBuilder sb = new StringBuilder();
			final Matcher matcher = NAME_PATTERN.matcher(string);
			while (matcher.find()) {
				sb.append(matcher.group(1).toUpperCase()).append(matcher.group(2));
			}
			return sb.toString();
		}

		private interface GetterSetter {
			Type getType();

			Object get(Object object);

			void set(Object object, Object value);
		}

		private static class MethodGetterSetter implements GetterSetter {
			private final Method getter;
			private final Method setter;

			MethodGetterSetter(final Method getter, final Method setter) {
				this.getter = getter;
				this.setter = setter;
			}

			@Override
			public Type getType() {
				return getter.getGenericReturnType();
			}

			@Override
			public Object get(final Object object) {
				try {
					return getter.invoke(object);
				} catch (final Exception e) {
					throw new RuntimeException("Unable to invoke getter method.", e);
				}
			}

			@Override
			public void set(final Object object, final Object value) {
				try {
					setter.invoke(object, value);
				} catch (final Exception e) {
					throw new RuntimeException("Unable to invoke setter method.", e);
				}
			}
		}

		// private static class FieldGetterSetter<T> implements GetterSetter<T> {
		// private final Field field;
		//
		// FieldGetterSetter(Field field) {
		// this.field = field;
		// }
		//
		// @SuppressWarnings("unchecked")
		// @Override
		// public T get(Object object) {
		// try {
		// return (T) field.get(object);
		// } catch (IllegalArgumentException | IllegalAccessException e) {
		// throw new RuntimeException("Unable to retrieve field value.", e);
		// }
		// }
		//
		// @Override
		// public void set(Object object, T t) {
		// try {
		// field.set(object, t);
		// } catch (IllegalArgumentException | IllegalAccessException e) {
		// throw new RuntimeException("Unable to set field value.", e);
		// }
		// }
		//
		// @Override
		// public Class<?> getType() {
		// return field.getType();
		// }
		// }

		// static final SettingsPersister<?> SETTING_PERSISTER2 =
		// Proxy.newProxyInstance(BeanPersister.class.getClassLoader(),
		// SettingsPersister.class, new InvocationHandler() {
		// @Override
		// public Object invoke(Object object, Method method, Object[] arguments)
		// throws Throwable {
		// return null;
		// }
		// });
		// new SettingsPersister() {
		// @Override
		// public Class<?> getType() {
		// return Object.class;
		// }
		//
		// @Override
		// public boolean save(Saver saver, Element xml, Object object) {
		// return getPersister(object.getClass()).save(saved(Lo -> ement xml, Type type) {
		// return getPersister(getTypeCl.load(loader, xml, type);
		// }
		// };
	}

	static final TypeDescription TYPE_DESCRIPTION = new TypeDescription() {
		@Override
		public boolean appliesTo(final Type type) {
			return (!(type instanceof Class) || !((Class<?>) type).isPrimitive());
		}
	};
}
