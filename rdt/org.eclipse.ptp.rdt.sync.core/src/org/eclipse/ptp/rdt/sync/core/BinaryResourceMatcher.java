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
import org.eclipse.core.resources.IResource;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;

/**
 * Matcher for a binary file. Note that it is conservative. It works only for files recognized by CDT and is not yet a general
 * binary file matcher. Ideally this would be a singleton, but the parent expects a default constructor.
 */
public class BinaryResourceMatcher extends ResourceMatcher {
	public boolean match(IResource candidate) {
		try {
			ICElement fileElement = CoreModel.getDefault().create(candidate.getFullPath());
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
	
	// All binary matchers should be equal
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BinaryResourceMatcher) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return Messages.BinaryPatternMatcher_0;
	}
}
