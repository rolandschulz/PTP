/*******************************************************************************
 * Copyright (c) 2007 The Regents of the University of California. 
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
public final class StringSetAttributeDefinition extends
		AbstractAttributeDefinition<String, StringSetAttribute, StringSetAttributeDefinition> {

	private final String defaultValue;
	private final List<String> values;

	public StringSetAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final String defaultValue, final List<String> values) throws IllegalValueException {
		super(uniqueId, name, description, display);
		this.defaultValue = defaultValue;
		this.values = new ArrayList<String>(values);
	}

	public StringSetAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final String defaultValue, final String[] values) throws IllegalValueException {
		super(uniqueId, name, description, display);
		this.defaultValue = defaultValue;
		this.values = Arrays.asList(values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttributeDefinition#create()
	 */
	public StringSetAttribute create() throws IllegalValueException {
		return new StringSetAttribute(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttributeDefinition#create(java.lang .String)
	 */
	public StringSetAttribute create(String value) throws IllegalValueException {
		return new StringSetAttribute(this, value);
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return the values
	 */
	public List<String> getValues() {
		return values;
	}

}
