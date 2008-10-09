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
package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.ICProject;

/**
 * An interface for factories that can retrieve or create ICProjects for a given filename.
 * 
 * @author crecoskie
 *
 */
public interface ICProjectFactory {
	/**
	 * Retrieves or creates an instance of an ICProject which should be associated with the file at the given location.
	 * 
	 * @param filename
	 * @return ICProject
	 */
	ICProject getProjectForFile(String filename);

}
