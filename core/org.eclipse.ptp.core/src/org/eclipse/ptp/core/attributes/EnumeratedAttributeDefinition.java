/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated
public final class EnumeratedAttributeDefinition<E extends Enum<E>> extends
		AbstractAttributeDefinition<E, EnumeratedAttribute<E>, EnumeratedAttributeDefinition<E>> {

	private final E defaultValue;
	private final Class<E> enumClass;

	@SuppressWarnings("unchecked")
	public EnumeratedAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final E defaultValueIn) {
		super(uniqueId, name, description, display);
		final Class<E> eClass = (Class<E>) defaultValueIn.getClass();
		this.enumClass = eClass;
		this.defaultValue = defaultValueIn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public EnumeratedAttribute<E> create() {
		return new EnumeratedAttribute<E>(this, defaultValue);
	}

	public EnumeratedAttribute<E> create(E value) {
		return new EnumeratedAttribute<E>(this, value);
	}

	public EnumeratedAttribute<E> create(int value) throws IllegalValueException {
		return new EnumeratedAttribute<E>(this, value);
	}

	public EnumeratedAttribute<E> create(String value) throws IllegalValueException {
		return new EnumeratedAttribute<E>(this, value);
	}

	public Class<E> getEnumClass() {
		return enumClass;
	}

	public List<E> getEnumerations() {
		return Arrays.asList(enumClass.getEnumConstants());
	}

	public List<String> getEnumerationStrings() {
		final List<E> values = getEnumerations();
		ArrayList<String> strings = new ArrayList<String>(values.size());
		for (E value : values) {
			strings.add(value.toString());
		}
		return strings;
	}
}
