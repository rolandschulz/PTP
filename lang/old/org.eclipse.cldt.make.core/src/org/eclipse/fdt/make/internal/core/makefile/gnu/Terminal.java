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

import org.eclipse.fdt.make.core.makefile.gnu.ITerminal;
import org.eclipse.fdt.make.internal.core.makefile.Directive;

public abstract class Terminal extends Directive implements ITerminal {

	public Terminal(Directive parent) {
		super(parent);
	}

	public boolean isEndif() {
		return false;
	}

	public boolean isEndef() {
		return false;
	}

	public String toString() {
		return "\n"; //$NON-NLS-1$
	}
}
