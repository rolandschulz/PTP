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

public abstract class AbstractAttributeDefinition implements IAttributeDefinition {
	private final String name;
	private final String description;
	private final String uniqueId;
	
	public AbstractAttributeDefinition(final String uniqueId, final String name, final String description) {
		this.uniqueId = uniqueId;
		this.name = name;
		this.description = description;
	}

	public int compareTo(IAttributeDefinition arg0) {
		return this.name.compareTo(arg0.getName());
	}

	public boolean equals(Object obj) {
		if (obj instanceof AbstractAttributeDefinition) {
			AbstractAttributeDefinition attrDesc = (AbstractAttributeDefinition) obj;
			return this.name.equals(attrDesc.name);
		}
		return false;
	}
	
	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return uniqueId;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public String toString() {
		return name;
	}
}
