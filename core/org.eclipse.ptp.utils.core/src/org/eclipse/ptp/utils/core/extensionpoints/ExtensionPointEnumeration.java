/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.utils.core.extensionpoints;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

public class ExtensionPointEnumeration implements Enumeration {

	IExtension[] extensions;
	IConfigurationElement[] elements;
	
	IConfigurationElement currentConfigurationElement;
	private int nextConfigurationIndex;
	private int nextExtensionIndex;
	
	public ExtensionPointEnumeration(String extensionPointId) {
		this.nextExtensionIndex = 0;
		this.nextConfigurationIndex = 0;

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extensionPointId);
		if (extensionPoint == null) {
			return;
		}
		
		this.extensions = extensionPoint.getExtensions();
		this.currentConfigurationElement = getNextConfigurationElement();
	}
	
	private IConfigurationElement getNextConfigurationElement() {
		if (extensions == null) {
			return null;
		}
		while (nextExtensionIndex < extensions.length) {
			IExtension extension = extensions[nextExtensionIndex];
			if (nextConfigurationIndex == 0) {
				elements = extension.getConfigurationElements();
			}
			if (nextConfigurationIndex >= elements.length) {
				nextConfigurationIndex = 0;
				nextExtensionIndex++;
				continue;
			}
			IConfigurationElement selectedConfigurationElement = elements[nextConfigurationIndex];
			nextConfigurationIndex++;
			return selectedConfigurationElement;
		}
		return null;
	}

	public boolean hasMoreElements() {
		return currentConfigurationElement != null;
	}

	public Object nextElement() {
		IConfigurationElement element = currentConfigurationElement;
		currentConfigurationElement = getNextConfigurationElement();
		if (element == null) {
			throw new NoSuchElementException();
		}
		return element;
	}

}
