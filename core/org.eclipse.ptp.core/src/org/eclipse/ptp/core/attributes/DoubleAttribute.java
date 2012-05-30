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

import org.eclipse.ptp.core.messages.Messages;

@Deprecated
public final class DoubleAttribute extends AbstractAttribute<Double, DoubleAttribute, DoubleAttributeDefinition> {

	private Double value;

	public DoubleAttribute(DoubleAttributeDefinition description, Double initialValue) throws IllegalValueException {
		super(description);
		setValue(initialValue);
	}

	public DoubleAttribute(DoubleAttributeDefinition description, String initialValue) throws IllegalValueException {
		super(description);
		setValueAsString(initialValue);
	}

	@Override
	protected synchronized int doCompareTo(DoubleAttribute other) {
		return value.compareTo(other.value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.AbstractAttribute#doClone()
	 */
	@Override
	protected DoubleAttribute doCopy() {
		try {
			return new DoubleAttribute(getDefinition(), value);
		} catch (IllegalValueException e) {
			// shouldn't happen
			throw new RuntimeException(e);
		}
	}

	@Override
	protected synchronized boolean doEquals(DoubleAttribute other) {
		return value.equals(other.value);
	}

	@Override
	protected synchronized int doHashCode() {
		return value.hashCode();
	}

	private double getMaxValue() {
		return getDefinition().getMaxValue();
	}

	private double getMinValue() {
		return getDefinition().getMinValue();
	}

	public synchronized Double getValue() {
		return value;
	}

	public synchronized String getValueAsString() {
		return value.toString();
	}

	public boolean isValid(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public synchronized void setValue(Double value) throws IllegalValueException {
		if (value.doubleValue() < getMinValue() || value.doubleValue() > getMaxValue()) {
			throw new IllegalValueException(Messages.DoubleAttribute_0);
		}
		this.value = value;
	}

	public synchronized void setValueAsString(String string) throws IllegalValueException {
		try {
			Double value = Double.valueOf(string);
			if (value.doubleValue() < getMinValue() || value.doubleValue() > getMaxValue()) {
				throw new IllegalValueException(Messages.DoubleAttribute_1);
			}
			this.value = value;
		} catch (NumberFormatException e) {
			throw new IllegalValueException(e);
		}
	}

}
