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
package org.eclipse.cldt.make.internal.core.makefile;

import org.eclipse.cldt.make.core.makefile.IBadDirective;

public class BadDirective extends Directive implements IBadDirective {

	String line;

	public BadDirective(Directive parent, String s) {
		super(parent);
		line = s;
	}

	public String toString() {
		return line;
	}

}
