/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;

/**
 * Matcher for a binary file. Note that it is conservative. It works only for files recognized by CDT and is not yet a general
 * binary file matcher.
 */
public class BinaryPatternMatcher extends PatternMatcher {
	private final IProject project;
	
	public BinaryPatternMatcher(IProject p) {
		project = p;
	}

	public boolean match(String candidate) {
		try {
			ICElement fileElement = CoreModel.getDefault().create(project.getFile(candidate));
			if (fileElement == null) {
				return false;
			}
			int resType = fileElement.getElementType();
			if (resType == ICElement.C_BINARY) {
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException e) {
			// CDT throws this exception for files not recognized. For now, be conservative and allow these files.
			return false;
		}
	}

	public String toString() {
		return Messages.BinaryPatternMatcher_0;
	}
}
