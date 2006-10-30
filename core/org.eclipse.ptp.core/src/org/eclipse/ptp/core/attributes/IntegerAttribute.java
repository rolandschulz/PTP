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

	private final Integer value;
	
	public IntegerAttribute(IAttributeDescription description, int value) {
		super(description);
		this.value = Integer.valueOf(value);
	}

	public IntegerAttribute(IAttributeDescription description, String string) throws IllegalValue {
		super(description);
		try {
			this.value = Integer.valueOf(string);
		}
		catch (NumberFormatException e) {
			throw new IllegalValue(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public IAttribute create(String string) throws IllegalValue {
		return new IntegerAttribute(getDescription(), string);
	}

	public boolean equals(Object obj) {
		if (obj instanceof IntegerAttribute) {
			IntegerAttribute attr = (IntegerAttribute) obj;
			return value.equals(attr.value);
		}
		return false;
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

	protected int doCompareTo(AbstractAttribute arg0) {
		IntegerAttribute attr = (IntegerAttribute) arg0;
		return this.value.compareTo(attr.value);
	}
}
