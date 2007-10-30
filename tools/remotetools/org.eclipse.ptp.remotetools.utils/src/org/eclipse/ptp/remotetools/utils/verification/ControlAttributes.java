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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class ControlAttributes implements Cloneable {

	Map currentMap = null;

	Map defaultMap = null;

	/** Create an instance without default values. */
	public ControlAttributes(Map config) {
		super();
		this.currentMap = config;
		this.defaultMap = new HashMap();
	}

	/** Create an instance with default values. */
	public ControlAttributes(Map currentMap, Map defaultMap) {
		super();
		this.currentMap = currentMap;
		this.defaultMap = defaultMap;
	}

	/** Return the current value of an attribute, if available. */
	private String getCurrent(String attributeName) {
		if (currentMap == null) {
			return null;
		}
		return (String) currentMap.get(attributeName);
	}

	/** Return the default value of an attribute, if available. */
	private String getDefault(String attributeName) {
		if (defaultMap == null) {
			return null;
		}
		return (String) defaultMap.get(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		ControlAttributes newObject = new ControlAttributes(null);
		if (currentMap != null) {
			newObject.currentMap = new HashMap(currentMap);
		}
		if (defaultMap != null) {
			newObject.defaultMap = new HashMap(defaultMap);
		}
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
	public String getAttributeOrDefault(String attributeName) {
		String value = getCurrent(attributeName);
		if (value == null) {
			return getDefault(attributeName);
		} else {
			return value;
		}
	}

	/**
	 * Get the attribute as string. If not available or not parseable, get
	 * default. If default not available or not parseable, get empty string.
	 */
	public String getString(String attributeKey) {
		return getString(attributeKey, "");
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
		return getText(attributeKey, "");
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
		textValue = textValue.replaceAll("\\n", "\n");
		textValue = textValue.replaceAll("\\\\", "\\");
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
	 * Get the attribute as boolean. If not available or not
	 * parseable, get default. If default not available or not parseable, get
	 * parameter default value.
	 */
	public boolean getBoolean(String attributeKey, boolean defaultValue) {
		String string = getAttributeOrDefault(attributeKey);
		if (string == null) {
			return defaultValue;
		} else {
			return Boolean.valueOf(string).booleanValue();
		}
	}

	/**
	 * Get the attribute as integer. If not available or not parseable, get
	 * default. If default not available or not parseable, get zero.
	 */
	public int getInteger(String attributeKey) {
		return getInteger(attributeKey, 0);
	}

	/**
	 * Get the attribute as integer. If not available or not
	 * parseable, get default. If default not available or not parseable, get
	 * parameter default value.
	 */
	public int getInteger(String attributeKey, int defaultValue) {
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
				} else {
					return Integer.parseInt(string);
				}
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
		return getDoubleAttribute(attributeKey, 0.0);
	}

	/**
	 * Get the attribute as double. If not available or not
	 * parseable, get default. If default not available or not parseable, get
	 * parameter default value.
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
				} else {
					return Double.parseDouble(string);
				}
			} catch (NumberFormatException e2) {
				return defaultValue;
			}
		}
	}

	/***************************************************************************
	 * Old GETTERS They are deprecated.
	 **************************************************************************/
	/** @deprecated */
	public boolean getBooleanAttribute(String attributeKey, boolean defaultValue) {
		return getBoolean(attributeKey, defaultValue);
	}

	/** @deprecated */
	public int getIntegerAttribute(String attributeKey, int defaultValue) {
		return getInteger(attributeKey, defaultValue);
	}

	/** @deprecated */
	public double getDoubleAttribute(String attributeKey, double defaultValue) {
		return getDouble(attributeKey, defaultValue);
	}

	/** @deprecated */
	public String getStringAttribute(String attributeKey, String defaultValue) {
		return getString(attributeKey, defaultValue);
	}

	/** @deprecated */
	public String getTextAttribute(String attributeKey, String defaultValue) {
		return getText(attributeKey, defaultValue);
	}

	/***************************************************************************
	 * Verifying GETTERS
	 **************************************************************************/
	/**
	 * @deprecated
	 */
	public int verifyIntAttribute(String attributeName, String attributeKey,
			int defaultValue) throws IllegalAttributeException {
		String stringValue = getAttributeOrDefault(attributeKey);
		if (stringValue == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(stringValue);
		} catch (NumberFormatException e) {
			throw new IllegalAttributeException(e, attributeName,
					"is not valid integer number", stringValue);
		}
	}

	public int verifyInt(String attributeName, String attributeKey)
			throws IllegalAttributeException {
		String stringValue = verifyString(attributeName, attributeKey);
		try {
			return Integer.parseInt(stringValue);
		} catch (NumberFormatException e) {
			throw new IllegalAttributeException(e, attributeName,
					"is not valid integer number", stringValue);
		}
	}

	/**
	 * @deprecated
	 */
	public double verifyDoubleAttribute(String attributeName,
			String attributeKey, double defaultValue)
			throws IllegalAttributeException {
		String stringValue = getAttributeOrDefault(attributeKey);
		if (stringValue == null)
			return defaultValue;
		try {
			return Double.parseDouble(stringValue);
		} catch (NumberFormatException e) {
			throw new IllegalAttributeException(e, attributeName,
					"is not valid decimal number", stringValue);
		}
	}

	public double verifyDouble(String attributeName, String attributeKey)
			throws IllegalAttributeException {
		String stringValue = verifyString(attributeName, attributeKey);
		try {
			return Double.parseDouble(stringValue);
		} catch (NumberFormatException e) {
			throw new IllegalAttributeException(e, attributeName,
					"is not valid decimal number", stringValue);
		}
	}

	public IPath verifyPath(String attributeName, String attributeKey)
			throws IllegalAttributeException {
		String stringValue = verifyString(attributeName, attributeKey);

		// TODO: Implement a proper checking
		Path path = new Path("");
		if (!path.isValidPath(stringValue)) {
			throw new IllegalAttributeException(attributeName,
					"is not valid path", stringValue);
		}
		return new Path(stringValue);
	}

	public String verifyString(String attributeName, String attributeKey)
			throws IllegalAttributeException {
		String stringValue = getAttributeOrDefault(attributeKey);
		if (stringValue == null) {
			throw new IllegalAttributeException(attributeName,
					"must not be empty");
		}
		return stringValue;
	}

	/***************************************************************************
	 * SETTERS
	 **************************************************************************/
	public void setAttribute(String attributeKey, String value) {
		currentMap.put(attributeKey, value);
	}

	public void setStringAttribute(String attributeKey, String value) {
		setAttribute(attributeKey, value);
	}

	public void setIntAttribute(String attributeKey, int value) {
		setAttribute(attributeKey, Integer.toString(value));
	}

	public void setDoubleAttribute(String attributeKey, double value) {
		setAttribute(attributeKey, Double.toString(value));
	}

	public void setBooleanAttribute(String attributeKey, boolean value) {
		setAttribute(attributeKey, Boolean.toString(value));
	}

	public void setTextAttribute(String attributeKey, String value) {
		String textValue = value;
		textValue = textValue.replaceAll("\\", "\\\\");
		textValue = textValue.replaceAll("\n", "\\n");
		setAttribute(attributeKey, textValue);
	}

}
