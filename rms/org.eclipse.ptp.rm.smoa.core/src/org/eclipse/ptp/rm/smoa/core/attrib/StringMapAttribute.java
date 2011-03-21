/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.attrib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.ptp.core.attributes.AbstractAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;

/**
 * A {@link AbstractAttribute} that holds string-to-string map.
 * 
 * For the definition, see {@link StringMapAttributeDefinition}.
 */
public class StringMapAttribute
		extends
		AbstractAttribute<Map<String, String>, StringMapAttribute, StringMapAttributeDefinition> {

	Map<String, String> value = new HashMap<String, String>();

	public StringMapAttribute(
			StringMapAttributeDefinition mapAttributeDefinition,
			Map<String, String> value) {
		super(mapAttributeDefinition);

		this.value.putAll(value);
	}

	public StringMapAttribute(
			StringMapAttributeDefinition mapAttributeDefinition, String value)
			throws IllegalValueException {
		super(mapAttributeDefinition);
		setValueAsString(value);
	}

	@Override
	protected int doCompareTo(StringMapAttribute other) {
		return getDefinition().getName().compareToIgnoreCase(
				other.getDefinition().getName());
	}

	@Override
	protected StringMapAttribute doCopy() {
		return new StringMapAttribute(getDefinition(), value);
	}

	@Override
	protected boolean doEquals(StringMapAttribute other) {
		return value.equals(other.value);
	}

	@Override
	protected int doHashCode() {
		return value.hashCode();
	}

	public Map<String, String> getValue() {
		return Collections.unmodifiableMap(value);
	}

	public String getValueAsString() {
		final StringBuilder result = new StringBuilder();
		for (final Entry<String, String> element : value.entrySet()) {
			result.append(element.getKey().replaceAll(" = ", " \\= ") //$NON-NLS-1$ //$NON-NLS-2$
					.replaceAll(" ; ", " \\; ")); //$NON-NLS-1$ //$NON-NLS-2$
			result.append(" = "); //$NON-NLS-1$
			result.append(element.getValue().replaceAll(" ; ", " \\; ")); //$NON-NLS-1$ //$NON-NLS-2$
			result.append(" ; "); //$NON-NLS-1$
		}
		result.delete(result.length() - 3, result.length() - 1);
		return result.toString();
	}

	public boolean isValid(String string) {
		final String[] pairs = string.split(" ; "); //$NON-NLS-1$
		for (final String pair : pairs) {
			final String[] keyVal = pair.split(" = ", 2); //$NON-NLS-1$
			if (keyVal.length != 2) {
				return false;
			}
		}
		return true;
	}

	public void setValue(Map<String, String> value)
			throws IllegalValueException {
		this.value.clear();
		this.value.putAll(value);

	}

	public void setValueAsString(String string) throws IllegalValueException {
		final Map<String, String> newVal = new HashMap<String, String>();

		final String[] pairs = string.split(" ; "); //$NON-NLS-1$

		for (final String pair : pairs) {
			final String[] keyVal = pair.replaceAll(" \\; ", " ; ").split(" = ", 2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (keyVal.length != 2) {
				throw new IllegalValueException("Bad format"); //$NON-NLS-1$
			}
			newVal.put(keyVal[0].replaceAll(" \\= ", " = "), keyVal[1]); //$NON-NLS-1$ //$NON-NLS-2$
		}

		value = newVal;
	}

}
