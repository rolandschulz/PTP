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

import java.util.List;

import org.eclipse.ptp.core.messages.Messages;

@Deprecated
public final class StringSetAttribute extends AbstractAttribute<String, StringSetAttribute, StringSetAttributeDefinition> {

	private String value;

	/**
	 * @param definition
	 * @throws IllegalValueException
	 */
	public StringSetAttribute(StringSetAttributeDefinition definition) throws IllegalValueException {
		super(definition);
		setValueAsString(definition.getDefaultValue());
	}

	/**
	 * @param definition
	 * @param valueIn
	 * @throws IllegalValueException
	 */
	public StringSetAttribute(StringSetAttributeDefinition definition, String valueIn) throws IllegalValueException {
		super(definition);
		setValueAsString(valueIn);
	}

	@Override
	protected int doCompareTo(StringSetAttribute other) {
		return getValueIndex() - other.getValueIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.AbstractAttribute#doClone()
	 */
	@Override
	protected StringSetAttribute doCopy() {
		try {
			return new StringSetAttribute(getDefinition(), value);
		} catch (IllegalValueException e) {
			// shouldn't happen
			throw new RuntimeException(e);
		}
	}

	@Override
	protected synchronized boolean doEquals(StringSetAttribute other) {
		return value.equals(other.value);
	}

	@Override
	protected synchronized int doHashCode() {
		return value.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#getValue()
	 */
	public String getValue() {
		return getValueAsString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#getValueAsString()
	 */
	public synchronized String getValueAsString() {
		return value;
	}

	public synchronized int getValueIndex() {
		return getDefinition().getValues().indexOf(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#isValid(java.lang.String)
	 */
	public boolean isValid(String valueIn) {
		final List<String> values = getDefinition().getValues();
		final boolean isValid = values.contains(valueIn);
		return isValid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.Object)
	 */
	public void setValue(String value) throws IllegalValueException {
		setValueAsString(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.String)
	 */
	public synchronized void setValueAsString(String valueIn) throws IllegalValueException {
		if (!isValid(valueIn)) {
			throw new IllegalValueException(Messages.StringSetAttribute_0 + valueIn + Messages.StringSetAttribute_1
					+ getDefinition().getName());
		}
		this.value = valueIn;
	}

}
