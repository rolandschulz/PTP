/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class TargetTypeElement {

	private String name;
	private TargetEnvironmentManager manager;
	private ITargetTypeExtension extension;
	private List elements;
	
	public TargetTypeElement(String name,ITargetTypeExtension env,TargetEnvironmentManager model) {
		super();
		this.name = name;
		this.extension = env;
		this.elements = new ArrayList();
		this.manager = model;
		
	}

	public List getElements() {
		return elements;
	}

	public void addElement(TargetElement element) {
		this.elements.add(element);
		manager.fireModelChanged(ITargetEnvironmentEventListener.ADDED, null, element);
	}
	
	public void removeElement(ITargetElement element) {
		this.elements.remove(element);
		manager.fireModelChanged(ITargetEnvironmentEventListener.REMOVED, element, null);
	}

	public ITargetTypeExtension getExtension() {
		return extension;
	}

	public void setExtension(ITargetTypeExtension extension) {
		this.extension = extension;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		
		return this.getName() != null ? this.getName() : super.toString();
	}
	
}
