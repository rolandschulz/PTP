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

import java.util.Arrays;

public final class StringSetAttribute extends AbstractAttribute {

	private String value;

	/**
	 * @param definition
	 * @throws IllegalValueException
	 */
	public StringSetAttribute(StringSetAttributeDefinition definition)
	throws IllegalValueException {
		super(definition);
		setValue(definition.getDefaultValue());
	}

	/**
	 * @param definition
	 * @param valueIn
	 * @throws IllegalValueException
	 */
	public StringSetAttribute(StringSetAttributeDefinition definition,
			String valueIn) throws IllegalValueException {
		super(definition);
		setValue(valueIn);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#getValueAsString()
	 */
	public String getValueAsString() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#isValid(java.lang.String)
	 */
	public boolean isValid(String valueIn) {
		final String[] values = getStringSetAttributeDefinition().getValues();
		final boolean isValid = Arrays.asList(values).contains(valueIn);
		return isValid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.String)
	 */
	public void setValue(String valueIn) throws IllegalValueException {
		if (!isValid(valueIn)) {
			throw new IllegalValueException("value: " + valueIn +
					" is not in StringSetAttribute: " +	getDefinition().getName());
		}
		this.value = valueIn;
	}

	private StringSetAttributeDefinition getStringSetAttributeDefinition() {
		return (StringSetAttributeDefinition) getDefinition();
	}

}
