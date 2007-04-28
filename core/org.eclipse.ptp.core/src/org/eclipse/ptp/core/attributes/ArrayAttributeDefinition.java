/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.core.attributes;


public final class ArrayAttributeDefinition extends AbstractAttributeDefinition implements IAttributeDefinition {

	private Object[] defaultValue;

	public ArrayAttributeDefinition(final String uniqueId, final String name, final String description, final Object[] defaultValue) {
		super(uniqueId, name, description);
		this.defaultValue = defaultValue;
	}

	public ArrayAttribute create() throws IllegalValueException {
		return new ArrayAttribute(this, defaultValue);
	}

	public ArrayAttribute create(String value) throws IllegalValueException {
		return new ArrayAttribute(this, value);
	}

	public ArrayAttribute create(Object[] value) throws IllegalValueException {
		return new ArrayAttribute(this, value);
	}
}
