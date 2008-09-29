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
public abstract class MISPUTableTupleElement extends MISPUTupleElement {

	final public static String NOINDEX = "*"; //$NON-NLS-1$
	
	private String index;
	
	public MISPUTableTupleElement(String name, String value, String index) {
		super(name,value);
		this.index = index;
	}
	
	public MISPUTableTupleElement (MIResult result) {
		super(result);
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

}
