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


public final class StringAttribute
extends AbstractAttribute<String,StringAttribute,StringAttributeDefinition> {

	private StringBuffer value = new StringBuffer();

	public StringAttribute(StringAttributeDefinition description, String initialValue) {
		super(description);
		setValueAsString(initialValue);
	}

	public String getValue() {
		return value.toString();
	}
	
	public String getValueAsString() {
		return getValue();
	}
	
	public boolean isValid(String string) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.Object)
	 */
	public void setValue(String value) throws IllegalValueException {
		setValueAsString(value);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ptp.core.attributes.IAttribute#setValueAsString(java.lang.String)
     */
    public void setValueAsString(String string) {
		this.value.replace(0, this.value.length(), string);
	}

    @Override
    protected int doCompareTo(StringAttribute other) {
        return value.toString().compareTo(other.value.toString());
    }

    @Override
    protected boolean doEquals(StringAttribute other) {
        return value.equals(other.value);
    }

	@Override
    protected int doHashCode() {
        return value.hashCode();
    }
}
