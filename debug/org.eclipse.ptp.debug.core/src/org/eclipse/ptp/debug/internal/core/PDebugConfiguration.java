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
package org.eclipse.ptp.debug.internal.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.ICDIDebugger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.debug.core.IPCDIDebugger;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;

public class PDebugConfiguration implements IPDebugConfiguration {
	/**
	 * The configuration element of the extension.
	 */
	private IConfigurationElement fElement;
	private HashSet fModes;
	private HashSet fCPUs;
	private String[] fCoreExt;

	public PDebugConfiguration(IConfigurationElement element) {
		fElement = element;
	}

	private IConfigurationElement getConfigurationElement() {
		return fElement;
	}

	public IPCDIDebugger getDebugger() throws CoreException {
		Object debugger = getConfigurationElement().createExecutableExtension("class"); //$NON-NLS-1$
		if (debugger instanceof IPCDIDebugger) {
			return (IPCDIDebugger)debugger;
		}
		throw new CoreException(new Status(IStatus.ERROR, "PTPDebugCorePlugin", -1, "Error", null)); //$NON-NLS-1$
	}

	public IPCDIDebugger createDebugger() throws CoreException {
		Object debugger = getConfigurationElement().createExecutableExtension("class"); //$NON-NLS-1$
		if (debugger instanceof IPCDIDebugger) {
			return (IPCDIDebugger)debugger;
		}
		return (IPCDIDebugger)debugger;
	}

	public String getName() {
		String name = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
		return name != null ? name : ""; //$NON-NLS-1$
	}

	public String getID() {
		return getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
	}

	public String getPlatform() {
		String platform = getConfigurationElement().getAttribute("platform"); //$NON-NLS-1$
		if (platform == null) {
			return "*"; //$NON-NLS-1$
		}
		return platform;
	}

	public String[] getCPUList() {
		return (String[]) getCPUs().toArray(new String[0]);
	}

	public String[] getModeList() {
		return (String[]) getModes().toArray(new String[0]);
	}

	public boolean supportsMode(String mode) {
		return getModes().contains(mode);
	}

	public boolean supportsCPU(String cpu) {
		String nativeCPU = Platform.getOSArch();
		boolean ret = false;
		if ( nativeCPU.startsWith(cpu) ) {
			ret = getCPUs().contains(CPU_NATIVE);
		}
		return ret || getCPUs().contains(cpu) || getCPUs().contains("*"); //$NON-NLS-1$
	}
	/**
	 * Returns the set of modes specified in the configuration data.
	 * 
	 * @return the set of modes specified in the configuration data
	 */
	protected Set getModes() {
		if (fModes == null) {
			String modes = getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes == null) {
				return new HashSet(0);
			}
			StringTokenizer tokenizer = new StringTokenizer(modes, ","); //$NON-NLS-1$
			fModes = new HashSet(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fModes.add(tokenizer.nextToken().trim());
			}
		}
		return fModes;
	}

	protected Set getCPUs() {
		if (fCPUs == null) {
			String cpus = getConfigurationElement().getAttribute("cpu"); //$NON-NLS-1$
			if (cpus == null) {
				fCPUs = new HashSet(1);
				fCPUs.add(CPU_NATIVE);
			}
			else {
				String nativeCPU = Platform.getOSArch();
				StringTokenizer tokenizer = new StringTokenizer(cpus, ","); //$NON-NLS-1$
				fCPUs = new HashSet(tokenizer.countTokens());
				while (tokenizer.hasMoreTokens()) {
					String cpu = tokenizer.nextToken().trim();
					fCPUs.add(cpu);
					if (nativeCPU.startsWith(cpu)) { // os arch be cpu{le/be}
						fCPUs.add(CPU_NATIVE);
					}
				}
			}
		}
		return fCPUs;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICDebugConfiguration#getCoreFileExtensions()
	 */
	public String[] getCoreFileExtensions() {
		if (fCoreExt == null) {
			List exts = new ArrayList();
			String cexts = getConfigurationElement().getAttribute("coreFileFilter"); //$NON-NLS-1$
			if (cexts != null) {
				StringTokenizer tokenizer = new StringTokenizer(cexts, ","); //$NON-NLS-1$
				while (tokenizer.hasMoreTokens()) {
					String ext = tokenizer.nextToken().trim();
					exts.add(ext);
				}
			}
			exts.add("*.*"); //$NON-NLS-1$
			fCoreExt = (String[])exts.toArray(new String[exts.size()]);
		}
		return fCoreExt;
	}
}
