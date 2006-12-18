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



public final class DoubleAttribute extends AbstractAttribute {

	/**
	 * Acyclic Visitor, PLOPD3, p.79
	 * @author rsqrd
	 *
	 */
	public interface IVisitor extends IAttributeVisitor {

		void visit(DoubleAttribute attribute);

	}

	private Double value;
	private double minValue = Double.NEGATIVE_INFINITY;
	private double maxValue = Double.POSITIVE_INFINITY;

	public DoubleAttribute(IAttributeDescription description, double value) {
		super(description);
		this.value = Double.valueOf(value);
	}

	public DoubleAttribute(IAttributeDescription description, String string)
	throws IllegalValue {
		super(description);
		try {
			this.value = Double.valueOf(string);
		}
		catch (NumberFormatException e) {
			throw new IAttribute.IllegalValue(e);
		}
	}

	public void accept(IAttributeVisitor visitor) {
		if (visitor instanceof IVisitor) {
			((IVisitor)visitor).visit(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public IAttribute create(String string) throws IllegalValue {
		final DoubleAttribute doubleAttribute = new DoubleAttribute(getDescription(), string);
		doubleAttribute.setValidRange(minValue, maxValue);
		return doubleAttribute;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DoubleAttribute) {
			DoubleAttribute attr = (DoubleAttribute) obj;
			return value.equals(attr.value);
		}
		return false;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public String getStringRep() {
		return value.toString();
	}

	public double getValue() {
		return value.doubleValue();
	}

	public int hashCode() {
		return value.hashCode();
	}

	public boolean isValid(String string) {
		try {
			Double.parseDouble(string);
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	public void setValidRange(double minValue, double maxValue) throws IllegalValue {
		if (minValue > maxValue) {
			throw new IllegalArgumentException("minValue must be less than or equal to maxValue");
		}
		this.minValue = minValue;
		this.maxValue = maxValue;
		if (value.doubleValue() < this.minValue || value.doubleValue() > this.maxValue) {
			throw new IllegalValue("The set valid range does not include the present value");
		}
	}

	public void setValue(Double value) throws IllegalValue {
		if (value.doubleValue() < this.minValue || value.doubleValue() > this.maxValue) {
			throw new IllegalValue("The set valid range does not include the new value");
		}
		this.value = value;
	}

	public void setValue(String string) throws IllegalValue {
		try {
			Double value = Double.valueOf(string);
			if (value.doubleValue() < this.minValue || value.doubleValue() > this.maxValue) {
				throw new IllegalValue("The set valid range does not include the new value");
			}
			this.value = value;
		}
		catch (NumberFormatException e) {
			throw new IAttribute.IllegalValue(e);
		}
	}

	protected int doCompareTo(AbstractAttribute arg0) {
		DoubleAttribute attr = (DoubleAttribute) arg0;
		return this.value.compareTo(attr.value);
	}

}
