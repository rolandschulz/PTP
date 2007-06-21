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
public final class EnumeratedAttribute<E extends Enum<E>>
extends AbstractAttribute<E, EnumeratedAttribute<E>, EnumeratedAttributeDefinition<E>> {

	private E value;

	/**
	 * @param definition
	 * @param valueIndex
	 * @throws IllegalValueException
	 */
	public EnumeratedAttribute(EnumeratedAttributeDefinition<E> definition,
			int valueIndex) throws IllegalValueException {
		super(definition);
		setValue(valueIndex);
	}

	/**
	 * @param definition
	 * @param valueString
	 * @throws IllegalValueException
	 */
	public EnumeratedAttribute(EnumeratedAttributeDefinition<E> definition,
			String valueString) throws IllegalValueException {
		super(definition);
		setValueAsString(valueString);
	}

	/**
	 * @param description
	 * @param enumerations
	 * @param value
	 */
	public EnumeratedAttribute(EnumeratedAttributeDefinition<E> definition, E value) {
		super(definition);
		setValue(value);
	}

	/**
     * @return
     */
    public List<? extends E> getEnumerations() {
		return getDefinition().getEnumerations();
	}

    /**
     * @return
     */
    public List<String> getEnumerationStrings() {
		return getDefinition().getEnumerationStrings();
	}
    
	/**
     * @return
     */
    public E getValue() {
        return value;
    }
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#getValueAsString()
	 */
	public String getValueAsString() {
		return value.toString();
	}
	
	/**
	 * @return the valueIndex
	 */
	public int getValueIndex() {
		return value.ordinal();
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
	
	/**
	 * @param value
	 */
	public void setValue(E value) {
		this.value = value;
	}

	/**
	 * @param valueIndex
	 * @throws IllegalValueException
	 */
	public void setValue(int valueIndex) throws IllegalValueException {
		final List<? extends E> enumerations = getEnumerations();
		if (valueIndex < 0 || valueIndex >= enumerations.size()) {
			throw new IllegalValueException("valueIndex is out of range");
		}
		this.value = enumerations.get(valueIndex);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.attributes.IAttribute#setValue(java.lang.String)
	 */
	public void setValueAsString(String string) throws IllegalValueException {
		Class<E> enumClass = getDefinition().getEnumClass();
        E eval;
        try {
        	eval = E.valueOf(enumClass, string);
        } catch (IllegalArgumentException e) {
        	throw new IllegalValueException(e);
        }
		this.value = eval;
	}

    @Override
    protected int doCompareTo(EnumeratedAttribute<E> other) {
        return value.compareTo(other.value);
    }

    @Override
    protected boolean doEquals(EnumeratedAttribute<E> other) {
        return value == other.value;
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }
}
