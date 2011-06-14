/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core;

import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * Must be implemented by extensions to the langaugeProperties extension point.
 * 
 * Gets invoked every time the remote indexer is triggered.
 * Supplies the remote language mapper with any additional 
 * properties it may need.
 * 
 */
public interface ILanguagePropertyProvider {

	public Map<String, String> getProperties(String languageId, IProject project);
	
}
