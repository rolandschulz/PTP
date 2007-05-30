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

public abstract class AbstractAttributeDefinition<T,
												  A extends IAttribute<T,A,D>,
												  D extends AbstractAttributeDefinition<T,A,D>>
implements IAttributeDefinition<T,A,D> {
	private final String name;
	private final String description;
	private final String uniqueId;
	
	public AbstractAttributeDefinition(final String uniqueId, final String name,
			final String description) {
		this.uniqueId = uniqueId;
		this.name = name;
		this.description = description;
	}

	public int compareTo(IAttributeDefinition<?, ?, ?> arg0) {
		return this.name.compareTo(arg0.getName());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	final public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final D other = (D) obj;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		return true;
	}
	
	public String getDescription() {
		return description;
	}

	public String getId() {
		return uniqueId;
	}

	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	final public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result +
				((uniqueId == null) ? 0 : uniqueId.hashCode());
		return result;
	}

	public String toString() {
		return name;
	}
}
