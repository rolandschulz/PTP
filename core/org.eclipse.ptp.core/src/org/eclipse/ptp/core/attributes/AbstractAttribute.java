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

@Deprecated
public abstract class AbstractAttribute<T, A extends AbstractAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>>
		implements IAttribute<T, A, D> {

	private final D definition;

	private boolean enabled;

	public AbstractAttribute(D definition) {
		this.definition = definition;
		this.enabled = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(A other) {
		int results = getDefinition().compareTo(other.getDefinition());
		if (results != 0) {
			return results;
		}
		if (getClass() != other.getClass()) {
			final int hashCode1 = getClass().hashCode();
			final int hashCode2 = other.getClass().hashCode();
			return hashCode1 < hashCode2 ? -1 : (hashCode1 > hashCode2 ? 1 : 0);
		}
		final int doCompareTo = doCompareTo(other);
		return doCompareTo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#copy()
	 */
	/**
	 * @since 4.0
	 */
	public A copy() {
		return doCopy();
	}

	/**
	 * @param other
	 * @return
	 */
	protected abstract int doCompareTo(A other);

	/**
	 * @return
	 * @since 4.0
	 */
	protected abstract A doCopy();

	/**
	 * @param other
	 * @return
	 */
	protected abstract boolean doEquals(A other);

	/**
	 * @return
	 */
	protected abstract int doHashCode();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final A other = (A) obj;
		if (definition == null) {
			if (other.definition != null) {
				return false;
			}
		} else if (!definition.equals(other.definition)) {
			return false;
		}
		final boolean doEquals = doEquals(other);
		if (!doEquals) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#getDefinition()
	 */
	public D getDefinition() {
		return definition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((definition == null) ? 0 : definition.hashCode());
		result = prime * result + doHashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.IAttribute#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return getValueAsString();
	}
}
