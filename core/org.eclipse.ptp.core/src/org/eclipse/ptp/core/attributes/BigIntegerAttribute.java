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

public final class BigIntegerAttribute
extends AbstractAttribute<BigInteger,BigIntegerAttribute,BigIntegerAttributeDefinition> {

	private BigInteger value;

	public BigIntegerAttribute(BigIntegerAttributeDefinition definition,
			BigInteger initialValue) throws IllegalValueException {
		super(definition);
		setValue(initialValue);
	}

	public BigIntegerAttribute(BigIntegerAttributeDefinition definition,
			String initialValue) throws IllegalValueException {
		super(definition);
		setValueAsString(initialValue);
	}

	public BigIntegerAttribute(BigIntegerAttributeDefinition definition,
			Integer value) throws IllegalValueException {
		this(definition, BigInteger.valueOf(value));
	}

	public BigInteger getValue() {
		return value;
	}
	
	public String getValueAsString() {
		return value.toString();
	}

	public boolean isValid(String string) {
		try {
			BigInteger val = new BigInteger(string);
			if (val.compareTo(getMinValue()) < 0 || val.compareTo(getMaxValue()) > 0) {
				return false;
			}
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	public void setValue(BigInteger value) throws IllegalValueException {
		if (value.compareTo(getMinValue()) < 0 || value.compareTo(getMaxValue()) > 0) {
			throw new IllegalValueException("The set valid range does not include the new value");
		}
		this.value = value;
	}

	public void setValue(Integer ivalue) throws IllegalValueException {
		BigInteger value = BigInteger.valueOf(ivalue);
		if (value.compareTo(getMinValue()) < 0 || value.compareTo(getMaxValue()) > 0) {
			throw new IllegalValueException("The set valid range does not include the new value");
		}
		this.value = value;
	}

	public void setValueAsString(String string) throws IllegalValueException {
		try {
			BigInteger value = new BigInteger(string);
			if (value.compareTo(getMinValue()) < 0 || value.compareTo(getMaxValue()) > 0) {
				throw new IllegalValueException("The set valid range does not include the new value");
			}
			this.value = value;
		}
		catch (NumberFormatException e) {
			throw new IllegalValueException(e);
		}
	}
	
	private BigInteger getMinValue() {
		return getDefinition().getMinValue();
	}
	
	private BigInteger getMaxValue() {
		return getDefinition().getMaxValue();
	}

    @Override
    protected int doCompareTo(BigIntegerAttribute other) {
        return value.compareTo(other.value);
    }

    @Override
    protected boolean doEquals(BigIntegerAttribute other) {
        return value.equals(other.value);
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }
}
