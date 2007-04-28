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
/**
 * 
 */
package org.eclipse.ptp.core.attributes;

import java.util.List;

/**
 * @author rsqrd
 *
 */
public class EnumeratedAttribute extends AbstractAttribute implements IEnumeratedAttribute {

	private int valueIndex;

	public EnumeratedAttribute(EnumeratedAttributeDefinition definition, int value) throws IllegalValueException {
        super(definition);
        setValueIndex(value);
    }

	/**
	 * @param description
	 * @param enumerations
	 * @param value
	 * @throws IllegalValue
	 */
	public EnumeratedAttribute(IAttributeDefinition definition, String value) throws IllegalValueException {
		super(definition);
		setValue(value);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ptp.core.attributes.IEnumeratedAttribute#getEnumValue()
     */
    public Enum getEnumValue() {
        final Class<? extends Enum> enumClass = getEnumAttrDefinition().getEnumClass();
        if (enumClass == null) {
            return null;
        }
        Enum[] enumElements = enumClass.getEnumConstants();
        return enumElements[valueIndex];
    }

    public String getValue() {
		return getEnumerations().get(valueIndex);
	}

    public String getValueAsString() {
		return getValue();
	}
    
	/**
	 * @return the valueIndex
	 */
	public int getValueIndex() {
		return valueIndex;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#isValid(java.lang.String)
	 */
	public boolean isValid(String string) {
		int vi = getEnumerations().indexOf(string);
		if (vi == -1) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.String)
	 */
	public void setValue(String value) throws IllegalValueException {
		int vi = getEnumerations().indexOf(value);
		if (vi == -1) {
			throw new IllegalValueException("enumerated value: " + value + " is not in set");
		}
		valueIndex = vi;
	}

	public void setValue(Enum value) throws IllegalValueException {
		setValueIndex(value.ordinal());
	}
	
	/**
	 * @param valueIndex the valueIndex to set
	 */
	public void setValueIndex(int valueIndex) throws IllegalValueException {
		if (valueIndex < 0 || valueIndex >= getEnumerations().size()) {
			throw new IllegalValueException("valueIndex is out of range");
		}
		this.valueIndex = valueIndex;
	}

	/**
     * @return
     */
    private IEnumeratedAttributeDefinition getEnumAttrDefinition() {
        return (IEnumeratedAttributeDefinition) getDefinition();
    }
	
	private List<String> getEnumerations() {
		return getEnumAttrDefinition().getEnumerations();
	}
}
