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
package org.eclipse.ptp.rm.core.attributes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Maintain the relationship between an attribute's value and its description.
 * Specifies a strict-weak ordering of itself and other attributes. Provide a
 * string representation of the attribute.
 * 
 * Contain multiple attribute components as a single attribute unit.
 * 
 * @author rsqrd
 * 
 */
public class AttributeEnvelope implements IAttribute {

	/**
	 * Compare two sets of components lexigraphically, i.e. dictionary ordering,
	 * e.g.<br>
	 * (aa) < (ab)<br>
	 * (aa) < (b)<br>
	 * (aa) < (aaa)
	 * 
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	private static int compareLexigraphically(SortedSet lhs, SortedSet rhs) {
		Iterator first1 = lhs.iterator();
		Iterator first2 = rhs.iterator();
		while (first1.hasNext() && first2.hasNext()) {
			Comparable o1 = (Comparable) first1.next();
			Comparable o2 = (Comparable) first2.next();
			int o1Co2 = o1.compareTo(o2);
			if (o1Co2 != 0) {
				return o1Co2;
			}
		}

		// They are the same up, but may not be of equal size.

		final int size1 = lhs.size();
		final int size2 = rhs.size();

		if (size1 < size2) {
			return -1;
		} else if (size1 > size2) {
			return 1;
		}
		return 0;
	}

	private final SortedSet components;

	private final IAttrDesc description;

	public AttributeEnvelope(IAttrDesc description, IAttrComponent[] components) {
		this.description = description;
		this.components = new TreeSet(Arrays.asList(components));
	}

	/**
	 * First compare the descriptions. If the descriptions are the same then
	 * lexigraphically compare (dictionary compare) the component sets.
	 * 
	 * @param other
	 * @return
	 */
	public int compareTo(Object obj) {
		AttributeEnvelope env = (AttributeEnvelope) obj;

		int compDesc = description.compareTo(env.description);
		if (compDesc != 0) {
			return compDesc;
		}
		return compareLexigraphically(components, env.components);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.attributes.IAttribute#contains(org.eclipse.ptp.rm.core.attributes.IAttribute)
	 */
	public boolean contains(IAttribute attribute) {
		if (!description.equals(attribute.getDescription())) {
			return false;
		}
		if (attribute instanceof AttributeEnvelope) {
			AttributeEnvelope other = (AttributeEnvelope) attribute;
			// look for the other's set to be a subset of this set
			return components.containsAll(other.components);
		} else {
			return components.containsAll(Arrays.asList(attribute
					.getComponents()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AttributeEnvelope) {
			return compareTo(obj) == 0;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.attributes.IAttribute#getComponents()
	 */
	public IAttrComponent[] getComponents() {
		return (IAttrComponent[]) components
				.toArray(new IAttrComponent[components.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.attributes.IAttribute#getDescription()
	 */
	public IAttrDesc getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = 17;
		int c = description.hashCode();
		result = 37 * result + c;
		for (Iterator cit = components.iterator(); cit.hasNext();) {
			c = cit.next().hashCode();
			result = 37 * result + c;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// A single component is converted as is.
		// A set of components is surrounded with parenthesis
		// and separated by spaces

		if (components.size() == 1) {
			return components.iterator().next().toString();
		}
		StringBuffer str = new StringBuffer("(");
		for (Iterator cit = components.iterator(); cit.hasNext();) {
			Object c = cit.next();
			str.append(c.toString());
			if (cit.hasNext()) {
				str.append(" ");
			}
		}
		str.append(")");
		return new String(str);
	}
}
