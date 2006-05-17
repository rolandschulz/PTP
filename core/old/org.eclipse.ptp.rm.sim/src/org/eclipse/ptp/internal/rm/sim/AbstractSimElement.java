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
package org.eclipse.ptp.internal.rm.sim;

import org.eclipse.ptp.rm.core.attributes.IAttrDesc;
import org.eclipse.ptp.rm.core.attributes.IAttribute;

public class AbstractSimElement {

	private final int id;
	private final String name;
	private final IAttribute[] attributes;

	public AbstractSimElement(int id, String name, IAttribute[] attributes) {
		this.id = id;
		this.name = name;
		this.attributes = (IAttribute[]) attributes.clone();
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public IAttribute[] getAttributes() {
		return (IAttribute[]) attributes.clone();
	}

	public IAttribute getAttribute(IAttrDesc description) {
		for (int i=0; i<attributes.length; ++i) {
			if (attributes[i].getDescription().equals(description)) {
				return attributes[i];
			}
		}
		return null;
	}

	public void setAttribute(IAttrDesc description, IAttribute attr) {
		for (int i=0; i<attributes.length; ++i) {
			if (attributes[i].getDescription().equals(description)) {
				attributes[i] = attr;
			}
		}
	}

}