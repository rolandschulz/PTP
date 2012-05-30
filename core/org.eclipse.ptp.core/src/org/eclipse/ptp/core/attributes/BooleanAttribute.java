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
public class BooleanAttribute extends AbstractAttribute<Boolean, BooleanAttribute, BooleanAttributeDefinition> {

	private Boolean value;

	public BooleanAttribute(BooleanAttributeDefinition definition, Boolean initialValue) {
		super(definition);
		this.value = initialValue;
	}

	public BooleanAttribute(BooleanAttributeDefinition definition, String initialValue) {
		super(definition);
		this.value = Boolean.valueOf(initialValue);
	}

	@Override
	protected synchronized int doCompareTo(BooleanAttribute other) {
		return value.compareTo(other.value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.attributes.AbstractAttribute#doClone()
	 */
	/**
	 * @since 4.0
	 */
	@Override
	protected BooleanAttribute doCopy() {
		return new BooleanAttribute(getDefinition(), value);
	}

	@Override
	protected synchronized boolean doEquals(BooleanAttribute other) {
		return value.equals(other.value);
	}

	@Override
	protected synchronized int doHashCode() {
		return value.hashCode();
	}

	public synchronized Boolean getValue() {
		return value.booleanValue();
	}

	public synchronized String getValueAsString() {
		return value.toString();
	}

	public boolean isValid(String string) {
		if ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		return false;
	}

	public synchronized void setValue(Boolean value) {
		this.value = value;
	}

	public synchronized void setValueAsString(String string) throws IllegalValueException {
		if (!isValid(string)) {
			throw new IllegalValueException(string + Messages.BooleanAttribute_0);
		}
		value = Boolean.valueOf(string);
	}

}
