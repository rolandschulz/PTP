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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.attributes.AbstractAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;

/**
 * A {@link AbstractAttributeDefinition} for storing a string-to-string map.
 * 
 * The attribute is called {@link StringMapAttribute}.
 */

public class StringMapAttributeDefinition
		extends
		AbstractAttributeDefinition<Map<String, String>, StringMapAttribute, StringMapAttributeDefinition> {

	private final Map<String, String> defaultValue;

	public StringMapAttributeDefinition(String uniqueId, String name,
			String description, boolean display) {
		this(uniqueId, name, description, display,
				new HashMap<String, String>());
	}

	public StringMapAttributeDefinition(String uniqueId, String name,
			String description, boolean display,
			final Map<String, String> defaultValue) {
		super(uniqueId, name, description, display);
		this.defaultValue = defaultValue;
	}

	public StringMapAttribute create() throws IllegalValueException {
		return new StringMapAttribute(this, defaultValue);
	}

	public StringMapAttribute create(Map<String, String> value)
			throws IllegalValueException {
		return new StringMapAttribute(this, value);
	}

	public StringMapAttribute create(String value) throws IllegalValueException {
		return new StringMapAttribute(this, value);
	}
}
