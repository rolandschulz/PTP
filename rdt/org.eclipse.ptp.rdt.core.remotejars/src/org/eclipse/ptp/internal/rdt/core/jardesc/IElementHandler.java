/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.jardesc;

/**
 * A set of callbacks for handling jardesc elements during an extraction.
 */
public interface IElementHandler {

	void handleFolder(String sourceFolder);

	void handlePackage(String sourceFolder, String packageName);

	void handleFile(String sourceFolder, String packageName, String fileName);

}
