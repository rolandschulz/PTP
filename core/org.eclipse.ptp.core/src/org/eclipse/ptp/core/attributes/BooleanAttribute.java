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

public class BooleanAttribute extends AbstractAttribute implements IBooleanAttribute {

	private Boolean value;

	public BooleanAttribute(IAttributeDefinition definition, Boolean initialValue) {
		super(definition);
		this.value = initialValue;
	}
	
	public BooleanAttribute(IAttributeDefinition definition, String initialValue) {
		super(definition);
		this.value = Boolean.valueOf(initialValue);
	}
	
	public Boolean getValue() {
		return value.booleanValue();
	}
	
	public String getValueAsString() {
		return value.toString();
	}

	public boolean isValid(String string) {
		if ("true".equalsIgnoreCase(string) || "false".equalsIgnoreCase(string)) {
			return true;
		}
		return false;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public void setValue(String string) throws IllegalValueException {
		if (!isValid(string)) {
			throw new IllegalValueException(string + " is not a legal Boolean");
		}
		value = Boolean.valueOf(string);
	}

}
