/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.ui.model;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.elements.IPElement;

/**
 * @author clement chu
 *
 */
public class Element extends PlatformObject implements IElement {
	protected String id = "0";
	protected String name = "";
	protected boolean registered = false;
	protected IElement parent = null;
	protected IPElement pElement = null;
	
	/** 
	 * Constructor
	 * @param parent Parent element
	 * @param id element ID
	 * @param name element name
	 */
	public Element(IElement parent, String id, String name, IPElement pElement) {
		this.parent = parent;
		this.id = id;
		this.name = name;
		this.pElement = pElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElement#cloneElement()
	 */
	public IElement cloneElement() {
		try {
			return (IElement)clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IElement e) {
		return getName().compareTo(e.getName());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElement#getID()
	 */
	public String getID() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElement#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElement#getParent()
	 */
	public IElement getParent() {
		return parent;
	}
	
	public IPElement getPElement() {
		return pElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElement#isRegistered()
	 */
	public boolean isRegistered() {
		return registered;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.model.IElement#setRegistered(boolean)
	 */
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}
}
