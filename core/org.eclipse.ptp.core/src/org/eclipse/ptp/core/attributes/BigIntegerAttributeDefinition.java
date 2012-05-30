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
public final class BigIntegerAttributeDefinition extends
		AbstractAttributeDefinition<BigInteger, BigIntegerAttribute, BigIntegerAttributeDefinition> {

	private final BigInteger minValue;
	private final BigInteger maxValue;
	private final BigInteger defaultValue;

	public BigIntegerAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final BigInteger defaultValue) {
		super(uniqueId, name, description, display);
		this.defaultValue = defaultValue;
		this.minValue = BigInteger.ZERO;
		this.maxValue = BigInteger.valueOf(Long.MAX_VALUE);
	}

	public BigIntegerAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final BigInteger defaultValue, final BigInteger minValue, final BigInteger maxValue) throws IllegalValueException {
		super(uniqueId, name, description, display);
		if (minValue.compareTo(maxValue) > 0) {
			throw new IllegalArgumentException(Messages.BigIntegerAttributeDefinition_0);
		}
		if (defaultValue.compareTo(minValue) < 0 || defaultValue.compareTo(maxValue) > 0) {
			throw new IllegalValueException(Messages.BigIntegerAttributeDefinition_1);
		}
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public BigIntegerAttribute create() throws IllegalValueException {
		return new BigIntegerAttribute(this, defaultValue);
	}

	public BigIntegerAttribute create(BigInteger value) throws IllegalValueException {
		return new BigIntegerAttribute(this, value);
	}

	public BigIntegerAttribute create(Integer value) throws IllegalValueException {
		return new BigIntegerAttribute(this, value);
	}

	public BigIntegerAttribute create(String value) throws IllegalValueException {
		return new BigIntegerAttribute(this, value);
	}

	public BigInteger getMaxValue() {
		return maxValue;
	}

	public BigInteger getMinValue() {
		return minValue;
	}

}
