/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/package org.eclipse.ptp.core.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class ArrayAttribute<T extends Comparable<T>> 
extends AbstractAttribute<ArrayAttribute<T>> {

	private List<T> value;

	public ArrayAttribute(ArrayAttributeDefinition<T> definition, String initialValue)
	throws IllegalValueException {
		super(definition);
		setValue(initialValue);
	}

	public <U extends T> ArrayAttribute(ArrayAttributeDefinition<T> definition, U[] initialValue) {
		super(definition);
		setValue(initialValue);
	}

	public <U extends T> ArrayAttribute(ArrayAttributeDefinition<T> definition,
			List<U> initialValue) {
		super(definition);
		setValue(initialValue);
	}

	public T[] getValue() {
		return (T[])this.value.toArray();
	}
	
	public <U extends T> void setValue(U[] value) {
		this.value = Arrays.asList((T[])value.clone());
	}

	public <U extends T> void setValue(List<U> value) {
		this.value = new ArrayList<T>(value);
	}

	public <U extends T> void addAll(U[] value) {
		this.value.addAll(Arrays.asList(value));
	}

	public String getValueAsString() {
		return Arrays.toString(value.toArray());
	}

	public boolean isValid(String string) {
		return true;
	}

	public void setValue(String string) throws IllegalValueException {
		String[] values = string.split("");
		try {
			setValue((T[])values);
		} catch (ClassCastException e) {
			throw new IllegalValueException(e);
		}
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ptp.core.attributes.AbstractAttribute#doCompareTo(org.eclipse.ptp.core.attributes.AbstractAttribute)
     */
    @Override
    protected int doCompareTo(ArrayAttribute<T> other) {
    	// This is a lexicographic compare.
    	
        int results = 0;
        Iterator<T> it1 = value.iterator();
        Iterator<T> it2 = other.value.iterator();
        while (it1.hasNext() && it2.hasNext()) {
        	T o1 = it1.next();
        	T o2 = it2.next();
        	results = o1.compareTo(o2);
        	if (results != 0) {
        		return results;
        	}
        }
        // If they compared the same up to here
        // then the lexicographic compare is based
        // on their sizes, the shortest
        // one is less than the longer one.
        return value.size() - other.value.size();
    }

    @Override
    protected boolean doEquals(ArrayAttribute<T> other) {
        return value.equals(other.value);
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }
}
