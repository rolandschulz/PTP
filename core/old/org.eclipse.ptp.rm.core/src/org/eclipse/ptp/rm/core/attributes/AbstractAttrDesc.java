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

/**
 * Provide a string description of the attribute. Provide a name of the
 * attribute. Know the actual type of the attribute. Create new attributes of
 * the correct type.
 * 
 * @author rsqrd
 * 
 */
public abstract class AbstractAttrDesc implements IAttrDesc {

	private final String description;

	private final String name;

	public AbstractAttrDesc(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Object arg0) {
		// This other object better be an IAttrDesc
		IAttrDesc other = (IAttrDesc) arg0;
		// equality and hash are based only on the name,
		// not the description
		final int retComp = name.compareTo(other.getName());
		return retComp == 0 ? 0 : (retComp > 0 ? 1 : -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.attributes.IAttrDesc#createAttribute(java.lang.String)
	 */
	public IAttribute createAttribute(String attrString) {
		IAttrComponent component = doCreateAttribute(attrString);
		return new AttributeEnvelope(this, new IAttrComponent[] { component });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.attributes.IAttrDesc#createAttribute(java.lang.String[])
	 */
	public IAttribute createAttribute(String[] attrStrings) {
		IAttrComponent[] components = new IAttrComponent[attrStrings.length];
		for (int i = 0; i < attrStrings.length; ++i) {
			// defer creation to subclasses ala the Template Method Pattern
			components[i] = doCreateAttribute(attrStrings[i]);
		}
		return new AttributeEnvelope(this, components);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AbstractAttrDesc) {
			AbstractAttrDesc other = (AbstractAttrDesc) obj;
			// equality and hash are based only on the name,
			// not the description
			return name.equals(other.name);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.attributes.IAttrDesc#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.attributes.IAttrDesc#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// equality and hash are based only on the name,
		// not the description
		return name.hashCode();
	}

	/**
	 * Defer creation to subclasses ala the Template Method Pattern
	 * 
	 * @param strRep
	 * @return
	 */
	protected abstract IAttrComponent doCreateAttribute(String strRep);

}
