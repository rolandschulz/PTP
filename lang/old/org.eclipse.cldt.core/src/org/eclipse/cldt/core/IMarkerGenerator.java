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
package org.eclipse.cldt.core;

import org.eclipse.core.resources.IResource;

/**
 * @author sam.robb
 */
public interface IMarkerGenerator {
	int SEVERITY_INFO = 0;
	int SEVERITY_WARNING = 1;
	int SEVERITY_ERROR_RESOURCE = 2;
	int SEVERITY_ERROR_BUILD = 3;

	void addMarker(IResource file, int lineNumber, String errorDesc, int severity, String errorVar);
}
