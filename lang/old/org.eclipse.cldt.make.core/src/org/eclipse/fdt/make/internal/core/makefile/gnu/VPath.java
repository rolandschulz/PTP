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

import org.eclipse.fdt.make.core.makefile.gnu.IVPath;
import org.eclipse.fdt.make.internal.core.makefile.Directive;

public class VPath extends Directive implements IVPath {

	String pattern;
	String[] directories;

	public VPath(Directive parent, String pat, String[] dirs) {
		super(parent);
		pattern = pat;
		directories = dirs;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(GNUMakefileConstants.DIRECTIVE_VPATH);
		if (pattern != null && pattern.length() > 0) {
			sb.append(' ').append(pattern);
		}
		for (int i = 0; i < directories.length; i++) {
			sb.append(' ').append(directories[i]);
		}
		return sb.toString();
	}

	public String[] getDirectories() {
		return directories;
	}

	public String getPattern() {
		return pattern;
	}
}
