/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.net.URI;

/**
 * 
 * Interface for translation units which are hosted by a linked filesystem.
 * 
 * @author crecoskie
 *
 */
public interface IHasManagedLocation {

	public abstract void setManagedLocation(URI managedLocation);

	public abstract URI getManagedLocation();

}