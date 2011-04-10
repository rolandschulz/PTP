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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerMonitor;

/**
 * JAXB resource manager monitor used for testing. Currently unimplemented
 */
public class JAXBResourceManagerMonitor extends AbstractResourceManagerMonitor {

	public JAXBResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		super(config);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doShutdown() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}
}