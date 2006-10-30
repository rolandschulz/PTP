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

	private final Double value;
	
	public DoubleAttribute(IAttributeDescription description, double value) {
		super(description);
		this.value = Double.valueOf(value);
	}

	public DoubleAttribute(IAttributeDescription description, String string) throws IllegalValue {
		super(description);
		try {
			this.value = Double.valueOf(string);
		}
		catch (NumberFormatException e) {
			throw new IllegalValue(e);
		}
	}

	public IAttribute create(String string) throws IllegalValue {
		return new DoubleAttribute(getDescription(), string);
	}

	public boolean equals(Object obj) {
		if (obj instanceof DoubleAttribute) {
			DoubleAttribute attr = (DoubleAttribute) obj;
			return value.equals(attr.value);
		}
		return false;
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

	protected int doCompareTo(AbstractAttribute arg0) {
		DoubleAttribute attr = (DoubleAttribute) arg0;
		return value.compareTo(attr.value);
	}

}
