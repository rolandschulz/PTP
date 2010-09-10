/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.utils.verification;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ControlAttributes implements Cloneable {

	private final Map<String, String> currentMap = new HashMap<String, String>();
	private final Map<String, String> defaultMap = new HashMap<String, String>();

	/**
	 * Create an instance without default values.
	 */
	public ControlAttributes(Map<String, String> config) {
		currentMap.putAll(config);
	}

	/**
	 * Create an instance with default values.
	 * 
	 * @since 2.0
	 */
	public ControlAttributes() {
	}

	/**
	 * @since 2.0
	 */
	public Map<String, String> getAttributesAsMap() {
		return Collections.unmodifiableMap(currentMap);
	}

	/**
	 * @since 2.0
	 */
	public Set<String> keySet() {
		Set<String> keys = new HashSet<String>();
		keys.addAll(currentMap.keySet());
		keys.addAll(defaultMap.keySet());
		return keys;
	}

	/** Return the current value of an attribute, if available. */
	private String getCurrent(String attributeName) {
		return currentMap.get(attributeName);
	}

	/** Return the default value of an attribute, if available. */
	private String getDefault(String attributeName) {
		return defaultMap.get(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		ControlAttributes newObject = new ControlAttributes();
		newObject.currentMap.putAll(currentMap);
		newObject.defaultMap.putAll(defaultMap);
		return newObject;
	}

	/***************************************************************************
	 * Safe GETTERS The safe getters return the current value, if available and
	 * parseable. Otherwise, return the default value, if available and
	 * parseable. Otherwise, return some hard-coded fallback value. If the
	 * current value cannot be parsed, no exception is thrown, but the default
	 * value is used.
	 **************************************************************************/
	/**
	 * Get the attribute as string. If not available or not parseable, get
	 * default. If default not available, get <code>null</code>.
	 */
	private String getAttributeOrDefault(String attributeName) {
		String value = getCurrent(attributeName);
		if (value == null) {
			return getDefault(attributeName);
		}
		return value;
	}

	/**
	 * Get the attribute as string. If not available or not parseable, get
	 * default. If default not available or not parseable, get empty string.
	 */
	public String getString(String attributeKey) {
		return getString(attributeKey, ""); //$NON-NLS-1$
	}

	/**
	 * Get the attribute as string. If not available or not parseable, get
	 * default. If default not available or not parseable, get parameter default
	 * value.
	 */
	public String getString(String attributeKey, String defaultValue) {
		String stringValue = getAttributeOrDefault(attributeKey);
		if (stringValue == null)
			return defaultValue;
		return stringValue;
	}

	/**
	 * Get the attribute as text (multi line string). If not available or not
	 * parseable, get default. If default not available or not parseable, get
	 * empty string.
	 */
	public String getText(String attributeKey) {
		return getText(attributeKey, ""); //$NON-NLS-1$
	}

	/**
	 * Get the attribute as text (multi line string). If not available or not
	 * parseable, get default. If default not available or not parseable, get
	 * parameter default value.
	 */
	public String getText(String attributeKey, String defaultValue) {
		String textValue = getAttributeOrDefault(attributeKey);
		if (textValue == null)
			textValue = defaultValue;
		textValue = textValue.replaceAll("\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		textValue = textValue.replaceAll("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		return textValue;
	}

	/**
	 * Get the attribute as boolean. If not available or not parseable, get
	 * default. If default not available or not parseable, get false.
	 */
	public boolean getBoolean(String attributeKey) {
		return getBoolean(attributeKey, false);
	}

	/**
	 * Get the attribute as boolean. If not available or not parseable, get
	 * default. If default not available or not parseable, get parameter default
	 * value.
	 */
	public boolean getBoolean(String attributeKey, boolean defaultValue) {
		String string = getAttributeOrDefault(attributeKey);
		if (string == null) {
			return defaultValue;
		}
		return Boolean.valueOf(string).booleanValue();
	}

	/**
	 * Get the attribute as integer. If not available or not parseable, get
	 * default. If default not available or not parseable, get zero.
	 * 
	 * @since 2.0
	 */
	public int getInt(String attributeKey) {
		return getInt(attributeKey, 0);
	}

	/**
	 * Get the attribute as integer. If not available or not parseable, get
	 * default. If default not available or not parseable, get parameter default
	 * value.
	 * 
	 * @since 2.0
	 */
	public int getInt(String attributeKey, int defaultValue) {
		String string = getAttributeOrDefault(attributeKey);
		if (string == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e1) {
			try {
				string = getDefault(attributeKey);
				if (string == null) {
					return defaultValue;
				}
				return Integer.parseInt(string);
			} catch (NumberFormatException e2) {
				return defaultValue;
			}
		}
	}

	/**
	 * Get the attribute as double. If not available or not parseable, get
	 * default. If default not available or not parseable, get zero.
	 */
	public double getDouble(String attributeKey) {
		return getDouble(attributeKey, 0.0);
	}

	/**
	 * Get the attribute as double. If not available or not parseable, get
	 * default. If default not available or not parseable, get parameter default
	 * value.
	 */
	public double getDouble(String attributeKey, double defaultValue) {
		String string = getAttributeOrDefault(attributeKey);
		if (string == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e1) {
			try {
				string = getDefault(attributeKey);
				if (string == null) {
					return defaultValue;
				}
				return Double.parseDouble(string);
			} catch (NumberFormatException e2) {
				return defaultValue;
			}
		}
	}

	/***************************************************************************
	 * Verifying GETTERS
	 **************************************************************************/

	public int verifyInt(String attributeName, String attributeKey) throws IllegalAttributeException {
		String stringValue = verifyString(attributeName, attributeKey);
		try {
			return Integer.parseInt(stringValue);
		} catch (NumberFormatException e) {
			throw new IllegalAttributeException(e, attributeName, Messages.ControlAttributes_InvalidIntegerNumber, stringValue);
		}
	}

	public double verifyDouble(String attributeName, String attributeKey) throws IllegalAttributeException {
		String stringValue = verifyString(attributeName, attributeKey);
		try {
			return Double.parseDouble(stringValue);
		} catch (NumberFormatException e) {
			throw new IllegalAttributeException(e, attributeName, Messages.ControlAttributes_InvalidDecimalNumber, stringValue);
		}
	}

	public IPath verifyPath(String attributeName, String attributeKey) throws IllegalAttributeException {
		String stringValue = verifyString(attributeName, attributeKey);

		// TODO: Implement a proper checking
		Path path = new Path(""); //$NON-NLS-1$
		if (!path.isValidPath(stringValue)) {
			throw new IllegalAttributeException(attributeName, Messages.ControlAttributes_InvalidPath, stringValue);
		}
		return new Path(stringValue);
	}

	public String verifyString(String attributeName, String attributeKey) throws IllegalAttributeException {
		String stringValue = getAttributeOrDefault(attributeKey);
		if (stringValue == null) {
			throw new IllegalAttributeException(attributeName, Messages.ControlAttributes_MustNotBeEmpty);
		}
		return stringValue;
	}

	/***************************************************************************
	 * SETTERS
	 **************************************************************************/
	public void setAttribute(String attributeKey, String value) {
		currentMap.put(attributeKey, value);
	}

	/**
	 * @since 2.0
	 */
	public void setString(String attributeKey, String value) {
		setAttribute(attributeKey, value);
	}

	/**
	 * @since 2.0
	 */
	public void setInt(String attributeKey, int value) {
		setAttribute(attributeKey, Integer.toString(value));
	}

	/**
	 * @since 2.0
	 */
	public void setDouble(String attributeKey, double value) {
		setAttribute(attributeKey, Double.toString(value));
	}

	/**
	 * @since 2.0
	 */
	public void setBoolean(String attributeKey, boolean value) {
		setAttribute(attributeKey, Boolean.toString(value));
	}

	/**
	 * @since 2.0
	 */
	public void setText(String attributeKey, String value) {
		String textValue = value;
		textValue = textValue.replaceAll("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		textValue = textValue.replaceAll("\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
		setAttribute(attributeKey, textValue);
	}

	private void setDefaultAttribute(String attributeKey, String value) {
		defaultMap.put(attributeKey, value);
	}

	/**
	 * @since 2.0
	 */
	public void setDefaultString(String attributeKey, String value) {
		setDefaultAttribute(attributeKey, value);
	}

	/**
	 * @since 2.0
	 */
	public void setDefaultInt(String attributeKey, int value) {
		setDefaultAttribute(attributeKey, Integer.toString(value));
	}

	/**
	 * @since 2.0
	 */
	public void setDefaultDouble(String attributeKey, double value) {
		setDefaultAttribute(attributeKey, Double.toString(value));
	}

	/**
	 * @since 2.0
	 */
	public void setDefaultBoolean(String attributeKey, boolean value) {
		setDefaultAttribute(attributeKey, Boolean.toString(value));
	}

	/**
	 * @since 2.0
	 */
	public void setDefaultText(String attributeKey, String value) {
		String textValue = value;
		textValue = textValue.replaceAll("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		textValue = textValue.replaceAll("\n", "\\n"); //$NON-NLS-1$ //$NON-NLS-2$
		setDefaultAttribute(attributeKey, textValue);
	}
}
