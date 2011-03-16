/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.rm;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManagerMonitor;

public final class JAXBResourceManager extends AbstractResourceManager implements IJAXBResourceManager, IJAXBNonNLSConstants {

	private final JAXBResourceManagerControl fControl;

	public JAXBResourceManager(IResourceManagerConfiguration jaxbServiceProvider, IResourceManagerControl control,
			IResourceManagerMonitor monitor) {
		super(jaxbServiceProvider, control, monitor);
		fControl = (JAXBResourceManagerControl) control;
	}
}
