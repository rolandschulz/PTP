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

public final class MutableStringAttribute extends AbstractAttribute implements IMutableAttribute {

	private StringBuffer value;
	
	public MutableStringAttribute(IAttributeDescription description, StringBuffer value) {
		super(description);
		this.value = value;
	}

	public boolean equals(Object obj) {
		if (obj instanceof MutableStringAttribute) {
			MutableStringAttribute attr = (MutableStringAttribute) obj;
			return value.equals(attr.value);
		}
		return false;
	}

	public String getStringRep() {
		return value.toString();
	}
	
	public String getValue() {
		return value.toString();
	}

	public int hashCode() {
		return value.hashCode();
	}

	public boolean isValid(String string) {
		return true;
	}

	public void setValue(String string) throws IllegalValue {
		this.value.replace(0, this.value.length(), string);
	}

	protected int doCompareTo(AbstractAttribute arg0) {
		MutableStringAttribute attr = (MutableStringAttribute) arg0;
		return this.value.toString().compareTo(attr.value.toString());
	}

}
