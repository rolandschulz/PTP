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


public final class IntegerAttribute extends AbstractAttribute {

	/**
	 * Acyclic Visitor Pattern, PLOPD3, p. 79
	 * @author rsqrd
	 *
	 */
	public interface IVisitor extends IAttributeVisitor {

		void visit(IntegerAttribute attribute);

	}

	private Integer value;
	private int minValue = 0;
	private int maxValue = Integer.MAX_VALUE;

	public IntegerAttribute(IAttributeDescription description, int value) {
		super(description);
		this.value = Integer.valueOf(value);
	}

	public IntegerAttribute(IAttributeDescription description, String string)
	throws IllegalValue {
		super(description);
		try {
			this.value = Integer.valueOf(string);
		}
		catch (NumberFormatException e) {
			throw new IllegalValue(e);
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
		final IntegerAttribute integerAttribute = new IntegerAttribute(getDescription(), string);
		integerAttribute.setValidRange(minValue, maxValue);
		return integerAttribute;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IntegerAttribute) {
			IntegerAttribute attr = (IntegerAttribute) obj;
			return value.equals(attr.value);
		}
		return false;
	}
	public int getMaxValue() {
		return maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public String getStringRep() {
		return value.toString();
	}
	
	public int getValue() {
		return value.intValue();
	}

	public int hashCode() {
		return value.hashCode();
	}

	public boolean isValid(String string) {
		try {
			int val = Integer.parseInt(string);
			if (val < minValue || val > maxValue) {
				return false;
			}
			return true;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	public void setValidRange(int minValue, int maxValue) throws IllegalValue {
		if (minValue > maxValue) {
			throw new IllegalArgumentException("minValue must be less than or equal to maxValue");
		}
		this.minValue = minValue;
		this.maxValue = maxValue;
		if (value.intValue() < this.minValue || value.intValue() > this.maxValue) {
			throw new IllegalValue("The set valid range does not include the present value");
		}
	}
	
	public void setValue(Integer value) throws IllegalValue {
		if (value.intValue() < this.minValue || value.intValue() > this.maxValue) {
			throw new IllegalValue("The set valid range does not include the new value");
		}
		this.value = value;
	}

	public void setValue(String string) throws IAttribute.IllegalValue {
		try {
			Integer value = Integer.valueOf(string);
			if (value.intValue() < this.minValue || value.intValue() > this.maxValue) {
				throw new IllegalValue("The set valid range does not include the new value");
			}
			this.value = value;
		}
		catch (NumberFormatException e) {
			throw new IAttribute.IllegalValue(e);
		}
	}

	protected int doCompareTo(AbstractAttribute arg0) {
		IntegerAttribute attr = (IntegerAttribute) arg0;
		return this.value.compareTo(attr.value);
	}

}
