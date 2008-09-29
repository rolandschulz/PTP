/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.cdi.command.output;

import org.eclipse.cdt.debug.mi.core.output.MIResult;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public abstract class MISPUTupleElement {
	
	final public static String EMPTY = ""; //$NON-NLS-1$
	
	private String name;
	
	private String value;
	
	public MISPUTupleElement(String name, String value) {
		setName(name);
		setValue(value);
	}
	
	public MISPUTupleElement (MIResult result) {
		parseResult(result);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	protected abstract void parseResult(MIResult result);

}
