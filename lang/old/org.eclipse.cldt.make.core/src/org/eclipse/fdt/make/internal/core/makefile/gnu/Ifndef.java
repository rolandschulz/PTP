/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.make.internal.core.makefile.gnu;

import org.eclipse.fdt.make.internal.core.makefile.Directive;


public class Ifndef extends Conditional {

    private static final String EMPTY = ""; //$NON-NLS-1$
	public Ifndef(Directive parent, String var) {
		super(parent, var, EMPTY, EMPTY);
	}

	public boolean isIfndef() {
		return true;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.CONDITIONAL_IFNDEF);
		sb.append(' ').append(getVariable());
		return sb.toString();
	}

	public String getVariable() {
		return getConditional();
	}

}
