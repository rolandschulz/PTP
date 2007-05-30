/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.core.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class ArrayAttribute<T extends Comparable<? super T>> 
extends AbstractAttribute<List<? extends T>, ArrayAttribute<T>, ArrayAttributeDefinition<T>> {

	private List<T> value;

	public <U extends T> ArrayAttribute(ArrayAttributeDefinition<T> definition,
			List<U> initialValue) {
		super(definition);
		setValue(initialValue);
	}

	public ArrayAttribute(ArrayAttributeDefinition<T> definition, String initialValue)
	throws IllegalValueException {
		super(definition);
		setValueAsString(initialValue);
	}

	public <U extends T> ArrayAttribute(ArrayAttributeDefinition<T> definition, U[] initialValue) {
		super(definition);
		setValue(initialValue);
	}

	public <U extends T> void addAll(U[] value) {
		if (value != null) {
			this.value.addAll(Arrays.asList(value));
		}
	}
	
	public <U extends T> void addAll(List<U> value) {
		if (value != null) {
			this.value.addAll(value);
		}
	}
	
	public List<T> getValue() {
		return Collections.unmodifiableList(value);
	}

	public String getValueAsString() {
		return Arrays.toString(value.toArray());
	}

	public boolean isValid(String string) {
		try {
			@SuppressWarnings("unused")
			T obj = (T) string;
		} catch (ClassCastException e) {
			return false;
		}
		return true;
	}

	public void setValue(List<? extends T> value) {
		if (value != null) {
			this.value = new ArrayList<T>(value);
		}
		else {
			this.value = new ArrayList<T>();
		}
	}

	public <U extends T> void setValue(U[] value) {
		if (value != null) {
			this.value = Arrays.asList((T[])value.clone());
		}
		else {
			this.value = new ArrayList<T>();
		}
	}
	
    public void setValueAsString(String string) throws IllegalValueException {
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
