package org.olafneumann.settings;

import java.lang.reflect.Type;

import org.jdom2.Element;
import org.olafneumann.trainer.data.BeanTrainerModel;

@FunctionalInterface
public interface TypeProvider {
	Type getType(Element element);

	TypeProvider NULL_TYPE_PROVIDER = element -> BeanTrainerModel.class;
}
