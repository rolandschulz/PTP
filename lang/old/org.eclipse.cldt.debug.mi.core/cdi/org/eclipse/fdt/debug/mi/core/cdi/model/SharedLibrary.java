/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.debug.mi.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.fdt.debug.core.cdi.CDIException;
import org.eclipse.fdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.fdt.debug.mi.core.MIFormat;
import org.eclipse.fdt.debug.mi.core.cdi.Session;
import org.eclipse.fdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.fdt.debug.mi.core.output.MIShared;


/**
 * Place holder for shared library info.
 */
public class SharedLibrary extends CObject implements ICDISharedLibrary {

	MIShared miShared;

	public SharedLibrary(Target target, MIShared slib) {
		super(target);
		miShared = slib;
	}

	public void setMIShared(MIShared slib) {
		miShared = slib;
	}

	public MIShared getMIShared() {
		return miShared;
	}
	/**
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDISharedLibrary#getFileName()
	 */
	public String getFileName() {
		return miShared.getName();
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDISharedLibrary#getStartAddress()
	 */
	public BigInteger getStartAddress() {
		return MIFormat.getBigInteger(miShared.getFrom());
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDISharedLibrary#getEndAddress()
	 */
	public BigInteger getEndAddress() {
		return MIFormat.getBigInteger(miShared.getTo());
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDISharedLibrary#areSymbolsLoaded()
	 */
	public boolean areSymbolsLoaded() {
		return miShared.isRead();
	}

	/**
	 * @see org.eclipse.fdt.debug.core.cdi.model.ICDISharedLibrary#loadSymbols()
	 */
	public void loadSymbols() throws CDIException {
		Target target = (Target)getTarget();
		SharedLibraryManager mgr = ((Session)target.getSession()).getSharedLibraryManager();
		mgr.loadSymbols(target, new ICDISharedLibrary[] { this });
	}

}
