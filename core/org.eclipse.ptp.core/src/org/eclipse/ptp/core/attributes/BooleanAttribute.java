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

public class BooleanAttribute extends AbstractAttribute {

	/**
	 * Acyclic Visitor Pattern, PLOPD3, p. 79
	 * @author rsqrd
	 *
	 */
	public interface IVisitor {

		void visit(BooleanAttribute attribute);

	}

	private Boolean value;

	public BooleanAttribute(IAttributeDescription description, boolean value) {
		super(description);
		this.value = new Boolean(value);
	}

	public BooleanAttribute(IAttributeDescription description, Boolean value) {
		super(description);
		this.value = value;
	}

	public void accept(IAttributeVisitor visitor) {
		if (visitor instanceof IVisitor) {
			((IVisitor)visitor).visit(this);
		}
	}

	public IAttribute create(String string) throws IllegalValue {
		return new BooleanAttribute(getDescription(), value);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final BooleanAttribute other = (BooleanAttribute) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public String getStringRep() {
		return value.toString();
	}

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	public boolean isValid(String string) {
		if ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
			return true;
		}
		return false;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public void setValue(String string) throws IllegalValue {
		if (!isValid(string)) {
			throw new IllegalValue(string + " is not a legal Boolean");
		}
		value = Boolean.valueOf(string);
	}

	protected int doCompareTo(AbstractAttribute other) {
		BooleanAttribute ba = (BooleanAttribute) other;
		return value.compareTo(ba.value);
	}

}
