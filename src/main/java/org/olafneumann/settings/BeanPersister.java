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
	private static Map<Class<?>, RealBeanPersister> persisters = new HashMap<Class<?>, RealBeanPersister>();

	private static RealBeanPersister getPersister(Class<?> clazz) {
		if (!persisters.containsKey(clazz)) {
			persisters.put(clazz, new RealBeanPersister(clazz));
		}
		return persisters.get(clazz);
	}

	private static Class<?> getTypeClass(Type type) {
		if (type instanceof Class)
			return (Class<?>) type;
		else if (type instanceof ParameterizedType)
			return getTypeClass(((ParameterizedType) type).getRawType());
		else
			throw new RuntimeException("Unknown type: " + type + " - " + type.getClass().getName());
	}

	@Override
	public Class<?> getType() {
		return Object.class;
	}

	@Override
	public boolean save(Saver saver, Element xml, Type type, Object object) throws SettingsException {
		if (object != null)
			return getPersister(object.getClass()).save(saver, xml, type, object);
		else
			return false;
	}

	@Override
	public Object load(Loader loader, Element xml, Type type) throws SettingsException {
		try {
			return getPersister(getTypeClass(type)).load(loader, xml, type);
		}
		catch (SettingsException e) {
			throw e;
		}
		catch (Exception e) {
			type = loader.getType(xml);
			try {
				if (type != null)
					return getPersister(getTypeClass(type)).load(loader, xml, type);
			}
			catch (Exception ei) {}
			if (e instanceof RuntimeException)
				throw (RuntimeException) e;
			else
				throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private static class RealBeanPersister implements SettingsPersister {
		private final Constructor<?> constructor;
		private final Map<String, GetterSetter> getterSetters = new HashMap<String, GetterSetter>();

		public RealBeanPersister(Class<?> clazz) {
			// Find Constructor without arguments
			try {
				constructor = clazz.getConstructor();
				constructor.setAccessible(true);
			}
			catch (Exception e) {
				throw new RuntimeException("No default constructor found for class: " + clazz.getName(), e);
			}

			// Find getters and setters
			Map<String, Method> getters = new HashMap<String, Method>();
			Map<String, Method> setters = new HashMap<String, Method>();

			// find possible getters and setters
			for (Method method : clazz.getMethods()) {
				if (!Modifier.isPublic(method.getModifiers()))
					continue;
				if (!method.isAccessible())
					method.setAccessible(true);

				String name = method.getName();
				if (name.startsWith("get") || name.startsWith("is")) {
					if (method.getParameterTypes().length == 0 && !void.class.equals(method.getReturnType())) {
						name = name.startsWith("get") ? name.substring(3) : name.substring(2);
						getters.put(getName(name), method);
					}
				} else if (name.startsWith("set")) {
					if (void.class.equals(method.getReturnType()) && method.getParameterTypes().length == 1) {
						name = name.substring(3);
						setters.put(getName(name), method);
					}
				}
			}

			// find useful getters and setters
			Set<String> names = new HashSet<String>(getters.keySet());
			names.retainAll(setters.keySet());
			for (String name : names) {
				Method getter = getters.get(name);
				Method setter = setters.get(name);
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

		private GetterSetter getGetterSetter(String name) {
			return getterSetters.get(name);
		}

		private Object createObject() throws IllegalArgumentException, InstantiationException, IllegalAccessException,
				InvocationTargetException {
			return constructor.newInstance();
		}

		@Override
		public boolean save(Saver saver, Element xml, Type type, Object object) throws SettingsException {
			saver.setSaved(object);
			// xml.setAttribute("class", object.getClass().getName());
			for (String name : getNames()) {
				GetterSetter gs = getGetterSetter(name);
				saver.saveXml(xml, gs.getType(), name, gs.get(object));
			}
			return true;
		}

		@Override
		public Object load(Loader loader, Element xml, Type type) throws SettingsException {
			try {
				return doRealLoading(loader, xml, createObject());
			}
			catch (SettingsException e) {
				throw e;
			}
			catch (Exception e) {
				// kann der loader helfen?
				Type typeToBeLoaded = loader.getType(xml);
				if (typeToBeLoaded != null) {
					return tryLoadingWithClass(loader, xml, typeToBeLoaded);
				}

				// mal gucken, ob eine Klasse gespeichert wurde...
				String className = xml.getAttributeValue("class");
				if (className != null) {
					try {
						Class<?> clazz = Class.forName(className);
						return tryLoadingWithClass(loader, xml, clazz);
					}
					catch (ClassNotFoundException e1) {
						throw new SettingsException("Unable to load class: " + className);
					}
				}
				throw new SettingsException("Unable to instantiate class: " + constructor.getDeclaringClass().getName());
			}
		}

		private Object tryLoadingWithClass(Loader loader, Element xml, Type type) throws SettingsException {
			try {
				return doRealLoading(loader, xml, getTypeClass(type).newInstance());
			}
			catch (SettingsException se) {
				throw se;
			}
			catch (Exception e1) {
				throw new SettingsException("Unable to instantiate class: " + type.toString());
			}
		}

		private Object doRealLoading(Loader loader, Element xml, Object object) throws SettingsException {
			loader.setLoaded(xml.getAttributeValue(ObjectRegistry.XML_ATTR_ID), object);
			for (String name : getNames()) {
				GetterSetter gs = getGetterSetter(name);
				Element child = xml.getChild(name);
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
			if (string.startsWith("get") || string.startsWith("set"))
				string = string.substring(3);
			if (string.startsWith("is"))
				string = string.substring(2);

			StringBuilder sb = new StringBuilder();
			Matcher matcher = NAME_PATTERN.matcher(string);
			while (matcher.find()) {
				sb.append(matcher.group(1).toUpperCase()).append(matcher.group(2));
			}
			return sb.toString();
		}

		private static interface GetterSetter {
			Type getType();

			Object get(Object object);

			void set(Object object, Object value);
		}

		private static class MethodGetterSetter implements GetterSetter {
			private final Method getter;
			private final Method setter;

			MethodGetterSetter(Method getter, Method setter) {
				this.getter = getter;
				this.setter = setter;
			}

			@Override
			public Type getType() {
				return getter.getGenericReturnType();
			}

			@Override
			public Object get(Object object) {
				try {
					return getter.invoke(object);
				}
				catch (Exception e) {
					throw new RuntimeException("Unable to invoke getter method.", e);
				}
			}

			@Override
			public void set(Object object, Object value) {
				try {
					setter.invoke(object, new Object[] { value });
				}
				catch (Exception e) {
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
		// return getPersister(object.getClass()).save(saver, xml, object);
		// }
		//
		// @Override
		// public Object load(Loader loader, Element xml, Type type) {
		// return getPersister(getTypeClass(type)).load(loader, xml, type);
		// }
		// };
	}

	static final TypeDescription TYPE_DESCRIPTION = new TypeDescription() {
		@Override
		public boolean appliesTo(Type type) {
			return !(type instanceof Class && ((Class<?>) type).isPrimitive());
		}
	};
}
