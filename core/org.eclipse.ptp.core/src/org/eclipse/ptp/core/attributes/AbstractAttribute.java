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

public abstract class AbstractAttribute<T extends AbstractAttribute<T>> implements IAttribute {

	private final IAttributeDefinition definition;
	private boolean enabled;

	public AbstractAttribute(IAttributeDefinition definition) {
		this.definition = definition;
		this.enabled = true;
	}
	
	public int compareTo(IAttribute other) {
        int results = getDefinition().compareTo(other.getDefinition());
        if (results != 0) {
            return results;
        }
        if (getClass() != other.getClass()) {
            final int hashCode1 = getClass().hashCode();
            final int hashCode2 = other.getClass().hashCode();
            return hashCode1 < hashCode2 ? -1 : (hashCode1 > hashCode2 ? 1 : 0);
        }
        @SuppressWarnings("unchecked")
        final int doCompareTo = doCompareTo((T) other);
        return doCompareTo;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        final AbstractAttribute other = (AbstractAttribute) obj;
        if (definition == null) {
            if (other.definition != null)
                return false;
        } else if (!definition.equals(other.definition))
            return false;
        @SuppressWarnings("unchecked")
        final boolean doEquals = doEquals((T) other);
        if (!doEquals)
            return false;
        return true;
    }

	public IAttributeDefinition getDefinition() {
		return definition;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + doHashCode();
        return result;
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

    /**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

    protected abstract int doCompareTo(T other);

    protected abstract boolean doEquals(T other);
    
    protected abstract int doHashCode();
}
