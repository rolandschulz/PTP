/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/

package org.eclipse.fdt.ui.text;

import org.eclipse.core.resources.IProject;
import org.eclipse.fdt.core.model.ITranslationUnit;

/**
 * Invocation context for the CHelpProviderManager
 */
public interface ICHelpInvocationContext {
	
	/**
	 * @return the project
	 */
	IProject getProject();

	/**
	 * @return ITranslationUnit or null
	 */
	ITranslationUnit getTranslationUnit();

}
