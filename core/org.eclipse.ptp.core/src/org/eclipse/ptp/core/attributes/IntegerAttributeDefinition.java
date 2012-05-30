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

import org.eclipse.ptp.core.messages.Messages;

@Deprecated
public final class IntegerAttributeDefinition extends
		AbstractAttributeDefinition<Integer, IntegerAttribute, IntegerAttributeDefinition> {

	private final Integer minValue;
	private final Integer maxValue;
	private final Integer defaultValue;

	public IntegerAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final Integer defaultValue) {
		super(uniqueId, name, description, display);
		this.defaultValue = defaultValue;
		this.minValue = 0;
		this.maxValue = Integer.MAX_VALUE;
	}

	public IntegerAttributeDefinition(final String uniqueId, final String name, final String description, final boolean display,
			final Integer defaultValue, final Integer minValue, final Integer maxValue) throws IllegalValueException {
		super(uniqueId, name, description, display);
		if (minValue > maxValue) {
			throw new IllegalArgumentException(Messages.IntegerAttributeDefinition_0);
		}
		if (defaultValue < minValue || defaultValue > maxValue) {
			throw new IllegalValueException(Messages.IntegerAttributeDefinition_1);
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
	public IntegerAttribute create() throws IllegalValueException {
		return new IntegerAttribute(this, defaultValue);
	}

	public IntegerAttribute create(Integer value) throws IllegalValueException {
		return new IntegerAttribute(this, value);
	}

	public IntegerAttribute create(String value) throws IllegalValueException {
		return new IntegerAttribute(this, value);
	}

	public Integer getMaxValue() {
		return maxValue;
	}

	public Integer getMinValue() {
		return minValue;
	}

}
