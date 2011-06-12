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

/**
 * 
 * An optional interface for ILanguage implementations.
 *  
 * If there is an ILanguagePropertyProvider for the language
 * the properties will be given to the language instance
 * using this interface.
 * 
 */
public interface IConfigurableLanguage {

	void setProperties(Map<String,String> properties);
	
}
