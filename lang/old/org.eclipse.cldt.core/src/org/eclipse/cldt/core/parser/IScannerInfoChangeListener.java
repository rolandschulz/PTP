package org.eclipse.cldt.core.parser;

import org.eclipse.cldt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

public interface IScannerInfoChangeListener {
	
	/**
	 * The listener must implement this method in order to receive the new 
	 * information from the provider.
	 * 
	 * @param info
	 */
	public void changeNotification(IResource project, IScannerInfo info);

}
