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


public final class DoubleAttributeDefinition extends AbstractAttributeDefinition implements IDoubleAttributeDefinition {

	private Double minValue = Double.NEGATIVE_INFINITY;
	private Double maxValue = Double.POSITIVE_INFINITY;
	private Double defaultValue;

	public DoubleAttributeDefinition(final String uniqueId, final String name, final String description, final Double defaultValue) {
		super(uniqueId, name, description);
		this.defaultValue = defaultValue;
	}

	public DoubleAttributeDefinition(final String uniqueId, final String name, final String description, final Double defaultValue, final double minValue, final double maxValue) throws IllegalValueException {
		super(uniqueId, name, description);
		if (minValue > maxValue) {
			throw new IllegalArgumentException("minValue must be less than or equal to maxValue");
		}
		if (defaultValue < minValue || defaultValue > maxValue) {
			throw new IllegalValueException("The set valid range does not include the default value");
		}
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public IDoubleAttribute create() throws IllegalValueException {
		return new DoubleAttribute(this, defaultValue);
	}

	public IDoubleAttribute create(String value) throws IllegalValueException {
		return new DoubleAttribute(this, value);
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public Double getMinValue() {
		return minValue;
	}

}
