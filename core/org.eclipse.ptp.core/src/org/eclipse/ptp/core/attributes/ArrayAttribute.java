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

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


public final class ArrayAttribute extends AbstractAttribute {

	/**
	 * Acyclic visitor pattern, PLOPD3, p.79
	 * @author rsqrd
	 *
	 */
	public interface IVisitor extends IAttributeVisitor {

		void visit(ArrayAttribute attribute);

	}

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
	protected static int compareLexigraphically(SortedSet lhs, SortedSet rhs) {
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
	
	private final IAttribute[] attributes;

	private String delimeter;

	public ArrayAttribute(IAttributeDescription description, IAttribute[] attributes) {
		this(description, attributes, ", ");
	}

	public ArrayAttribute(IAttributeDescription description,
			IAttribute[] attributes, String delimiter) {
		super(description);
		this.delimeter = delimiter;
		this.attributes = (IAttribute[]) attributes.clone();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#accept(org.eclipse.ptp.core.attributes.IAttributeVisitor)
	 */
	public void accept(IAttributeVisitor visitor) {
		if (visitor instanceof IVisitor) {
			((IVisitor) visitor).visit(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#create(java.lang.String)
	 */
	public IAttribute create(String string) throws IllegalValue {
		throw new IllegalValue("Cannot create an ArrayAttribute from a String representation.");
	}

	public boolean equals(Object obj) {
		if (obj instanceof ArrayAttribute) {
			ArrayAttribute attr = (ArrayAttribute) obj;
			return Arrays.deepEquals(attributes, attr.attributes);
		}
		return false;
	}

	public IAttribute[] getAttributes() {
		return (IAttribute[]) attributes.clone();
	}

	public String getDelimiter() {
		return delimeter;
	}

	public String getStringRep() {
		StringBuffer buf = new StringBuffer("(");
		for (int i = 0; i < attributes.length-1; ++i) {
			buf.append(attributes[i].toString() + delimeter);
		}
		if (attributes.length > 0) {
			buf.append(attributes[attributes.length-1]);
		}
		buf.append(")");
		return buf.toString();
	}

	public int hashCode() {
		return Arrays.deepHashCode(attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#isValid(java.lang.String)
	 */
	public boolean isValid(String string) {
		throw new UnsupportedOperationException("ArrayAttribute is not mutable");
	}

	public void setDelimiter(String delimiter) {
		this.delimeter = delimiter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.String)
	 */
	public void setValue(String string) throws IllegalValue {
		throw new UnsupportedOperationException("ArrayAttribute is not mutable");
	}

	/**
	 * lexigraphically compare (dictionary compare) the component sets.
	 * 
	 * @param other
	 * @return
	 */
	protected int doCompareTo(AbstractAttribute obj) {
		ArrayAttribute env = (ArrayAttribute) obj;
		SortedSet thisAttrs = new TreeSet(Arrays.asList(attributes));
		SortedSet otherAttrs = new TreeSet(Arrays.asList(env.attributes));
		return compareLexigraphically(thisAttrs, otherAttrs);
	}

}
