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

import java.math.BigInteger;

import org.eclipse.ptp.core.messages.Messages;

@Deprecated
public final class BigIntegerAttribute extends AbstractAttribute<BigInteger, BigIntegerAttribute, BigIntegerAttributeDefinition> {

	private BigInteger value;

	public BigIntegerAttribute(BigIntegerAttributeDefinition definition, BigInteger initialValue) throws IllegalValueException {
		super(definition);
		setValue(initialValue);
	}

	public BigIntegerAttribute(BigIntegerAttributeDefinition definition, Integer value) throws IllegalValueException {
		this(definition, BigInteger.valueOf(value));
	}

	public BigIntegerAttribute(BigIntegerAttributeDefinition definition, String initialValue) throws IllegalValueException {
		super(definition);
		setValueAsString(initialValue);
	}

	@Override
	protected synchronized int doCompareTo(BigIntegerAttribute other) {
		return value.compareTo(other.value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.AbstractAttribute#doClone()
	 */
	@Override
	protected BigIntegerAttribute doCopy() {
		try {
			return new BigIntegerAttribute(getDefinition(), value);
		} catch (IllegalValueException e) {
			// this shouldn't happen
			throw new RuntimeException(e);
		}
	}

	@Override
	protected synchronized boolean doEquals(BigIntegerAttribute other) {
		return value.equals(other.value);
	}

	@Override
	protected synchronized int doHashCode() {
		return value.hashCode();
	}

	private BigInteger getMaxValue() {
		return getDefinition().getMaxValue();
	}

	private BigInteger getMinValue() {
		return getDefinition().getMinValue();
	}

	public synchronized BigInteger getValue() {
		return value;
	}

	public synchronized String getValueAsString() {
		return value.toString();
	}

	public boolean isValid(String string) {
		try {
			BigInteger val = new BigInteger(string);
			if (val.compareTo(getMinValue()) < 0 || val.compareTo(getMaxValue()) > 0) {
				return false;
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public synchronized void setValue(BigInteger value) throws IllegalValueException {
		if (value.compareTo(getMinValue()) < 0 || value.compareTo(getMaxValue()) > 0) {
			throw new IllegalValueException(Messages.BigIntegerAttribute_0);
		}
		this.value = value;
	}

	public synchronized void setValue(Integer ivalue) throws IllegalValueException {
		BigInteger value = BigInteger.valueOf(ivalue);
		if (value.compareTo(getMinValue()) < 0 || value.compareTo(getMaxValue()) > 0) {
			throw new IllegalValueException(Messages.BigIntegerAttribute_1);
		}
		this.value = value;
	}

	public synchronized void setValueAsString(String string) throws IllegalValueException {
		try {
			BigInteger value = new BigInteger(string);
			if (value.compareTo(getMinValue()) < 0 || value.compareTo(getMaxValue()) > 0) {
				throw new IllegalValueException(Messages.BigIntegerAttribute_2);
			}
			this.value = value;
		} catch (NumberFormatException e) {
			throw new IllegalValueException(e);
		}
	}
}
