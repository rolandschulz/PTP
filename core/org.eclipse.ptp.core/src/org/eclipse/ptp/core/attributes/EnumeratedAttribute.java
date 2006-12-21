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
/**
 * 
 */
package org.eclipse.ptp.core.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * @author rsqrd
 *
 */
public class EnumeratedAttribute extends AbstractAttribute {

	/**
	 * Acyclic Visitor Pattern, PLOPD3, p. 79
	 * @author rsqrd
	 *
	 */
	public interface IVisitor extends IAttributeVisitor {

		void visit(EnumeratedAttribute attribute);

	}

	private final ArrayList<String> enumerations;
	private int valueIndex;

	/**
	 * @param description
	 * @param enumerations
	 * @param value
	 * @throws IllegalValue
	 */
	public EnumeratedAttribute(IAttributeDescription description, List<String> enumerations,
			String value) throws IllegalValue {
		super(description);
		this.enumerations = new ArrayList<String>(enumerations);
		valueIndex = this.enumerations.indexOf(value);
		if (valueIndex == -1) {
			throw new IllegalValue("enumerated value: " + value + " is not in set");
		}
	}

	/**
	 * @param description
	 * @throws IllegalValue 
	 */
	public EnumeratedAttribute(IAttributeDescription description, String[] enumerations,
			String value) throws IllegalValue {
		this(description, Arrays.asList(enumerations), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#accept(org.eclipse.ptp.core.attributes.IAttributeVisitor)
	 */
	public void accept(IAttributeVisitor visitor) {
		if (visitor instanceof IVisitor) {
			((IVisitor)visitor).visit(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public EnumeratedAttribute create(String string) throws IllegalValue {
		EnumeratedAttribute ea = new EnumeratedAttribute(getDescription(), enumerations,
				string);
		return ea;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EnumeratedAttribute other = (EnumeratedAttribute) obj;
		if (enumerations == null) {
			if (other.enumerations != null)
				return false;
		} else if (!enumerations.equals(other.enumerations))
			return false;
		if (valueIndex != other.valueIndex)
			return false;
		return true;
	}

	/**
	 * @return the enumerations
	 */
	public List<String> getEnumerations() {
		return Collections.unmodifiableList(enumerations); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.AbstractAttribute#getStringRep()
	 */
	public String getStringRep() {
		return enumerations.get(valueIndex);
	}

	/**
	 * @return the valueIndex
	 */
	public int getValueIndex() {
		return valueIndex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((enumerations == null) ? 0 : enumerations.hashCode());
		result = PRIME * result + valueIndex;
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#isValid(java.lang.String)
	 */
	public boolean isValid(String string) {
		int vi = enumerations.indexOf(string);
		if (vi == -1) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.String)
	 */
	public void setValue(String value) throws IllegalValue {
		int vi = enumerations.indexOf(value);
		if (vi == -1) {
			throw new IllegalValue("enumerated value: " + value + " is not in set");
		}
		valueIndex = vi;
	}

	/**
	 * @param valueIndex the valueIndex to set
	 */
	public void setValueIndex(int valueIndex) throws IllegalValue {
		if (valueIndex < 0 || valueIndex >= enumerations.size()) {
			throw new IllegalValue("valueIndex is out of range");
		}
		this.valueIndex = valueIndex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.AbstractAttribute#doCompareTo(org.eclipse.ptp.core.attributes.AbstractAttribute)
	 */
	protected int doCompareTo(AbstractAttribute other) {
		if (other instanceof EnumeratedAttribute) {
			EnumeratedAttribute eOther = (EnumeratedAttribute) other;
			TreeSet enums = new TreeSet<String>(enumerations);
			TreeSet oEnums = new TreeSet<String>(eOther.enumerations);
			int compare = ArrayAttribute.compareLexigraphically(enums, oEnums);
			if (compare != 0)
				return compare;
			return valueIndex - eOther.valueIndex;
		}
		// return something consistent
		return getClass().toString().compareTo(other.getClass().toString());
	}

}
