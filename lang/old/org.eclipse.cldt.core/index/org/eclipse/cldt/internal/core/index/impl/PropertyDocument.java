/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.core.index.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.cldt.internal.core.index.IDocument;

/**
 * The properties of a document are stored into a hashtable.
 * @see IDocument
 */

public abstract class PropertyDocument implements IDocument {
	protected Hashtable properties;
	public PropertyDocument() {
		properties= new Hashtable(5);
	}
	/**
	 * @see IDocument#getProperty
	 */
	public String getProperty(String property) {
		return (String) properties.get(property);
	}
	/**
	 * @see IDocument#getPropertyNames
	 */

	public Enumeration getPropertyNames() {
		return properties.keys();
	}
	/**
	 * @see IDocument#setProperty
	 */

	public void setProperty(String property, String value) {
		properties.put(property, value);
	}
}
