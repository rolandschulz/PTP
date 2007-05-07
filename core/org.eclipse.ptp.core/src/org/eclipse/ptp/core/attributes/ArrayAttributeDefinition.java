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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class ArrayAttributeDefinition<T extends Comparable<T>>
extends AbstractAttributeDefinition {

	private List<T> defaultValue;

	public <U extends T> ArrayAttributeDefinition(final String uniqueId, final String name,
			final String description, final U[] defaultValue) {
		super(uniqueId, name, description);
		if (defaultValue != null) {
			this.defaultValue = Arrays.asList((T[])defaultValue.clone());
		}
		else {
			this.defaultValue = new ArrayList<T>();
		}
	}

	public ArrayAttribute<T> create() {
		return new ArrayAttribute<T>(this, defaultValue);
	}

	public ArrayAttribute<T> create(String value) throws IllegalValueException {
		return new ArrayAttribute<T>(this, value);
	}

	public <U extends T> ArrayAttribute<T> create(U[] value) {
		return new ArrayAttribute<T>(this, value);
	}
}
