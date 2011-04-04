package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.Collection;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;

public class UpdateModelFactory implements IJAXBNonNLSConstants {

	enum ControlType {
		TEXT, COMBO, SPINNER, CHECK, VIEWER;

		public static ControlType getType(Object object) {
			if (object instanceof Property) {
				Property p = (Property) object;
				Object value = p.getValue();
				String clzz = p.getType();
				if (clzz != null) {
					return getTypeFromClass(clzz);
				}
				if (value != null) {
					return getType(value);
				}
				return TEXT;
			} else if (object instanceof Attribute) {
				Attribute a = (Attribute) object;
				if (a.getChoice() != null) {
					return COMBO;
				}
				String clzz = a.getType();
				if (clzz != null) {
					return getTypeFromClass(clzz);
				}
				Object value = a.getValue();
				if (value != null) {
					return getType(value);
				}
				return TEXT;
			} else if (object instanceof Collection) {
				return COMBO;
			} else if (object instanceof Integer) {
				return SPINNER;
			} else if (object instanceof Boolean) {
				return CHECK;
			} else {
				return TEXT;
			}
		}

		private static ControlType getTypeFromClass(String clzz) {
			if (clzz.indexOf(NT) > 0) {
				return SPINNER;
			}
			if (clzz.indexOf(BOOL) >= 0) {
				return CHECK;
			}
			if (clzz.indexOf(IST) > 0) {
				return COMBO;
			}
			if (clzz.indexOf(ET) > 0) {
				return COMBO;
			}
			if (clzz.indexOf(ECTOR) > 0) {
				return COMBO;
			}
			return TEXT;
		}
	}

}
