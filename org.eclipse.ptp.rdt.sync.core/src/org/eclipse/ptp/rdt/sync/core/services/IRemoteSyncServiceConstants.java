/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.services;

/**
 * Constants for access to well known services.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 */
public interface IRemoteSyncServiceConstants {
	public static final String SERVICE_C_INDEX = "org.eclipse.ptp.rdt.sync.core.CIndexingService"; //$NON-NLS-1$
	public static final String SERVICE_BUILD = "org.eclipse.ptp.rdt.sync.core.BuildService"; //$NON-NLS-1$
}
