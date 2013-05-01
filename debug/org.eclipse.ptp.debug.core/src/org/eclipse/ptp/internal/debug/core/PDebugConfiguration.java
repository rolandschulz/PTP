/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
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
package org.eclipse.ptp.internal.debug.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.internal.debug.core.messages.Messages;

public class PDebugConfiguration implements IPDebugConfiguration {
	private String[] fCoreExt;
	private HashSet<String> fCPUs;
	private final IConfigurationElement fElement;
	private HashSet<String> fModes;

	public PDebugConfiguration(IConfigurationElement element) {
		fElement = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.IPDebugConfiguration#getCoreFileExtensions()
	 */
	public String[] getCoreFileExtensions() {
		if (fCoreExt == null) {
			final List<String> exts = new ArrayList<String>();
			final String cexts = getConfigurationElement().getAttribute("coreFileFilter"); //$NON-NLS-1$
			if (cexts != null) {
				final StringTokenizer tokenizer = new StringTokenizer(cexts, ","); //$NON-NLS-1$
				while (tokenizer.hasMoreTokens()) {
					final String ext = tokenizer.nextToken().trim();
					exts.add(ext);
				}
			}
			exts.add("*.*"); //$NON-NLS-1$
			fCoreExt = exts.toArray(new String[exts.size()]);
		}
		return fCoreExt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugConfiguration#getCPUList()
	 */
	public String[] getCPUList() {
		return getCPUs().toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugConfiguration#getDebugger()
	 */
	public IPDebugger getDebugger() throws CoreException {
		final Object debugger = getConfigurationElement().createExecutableExtension("class"); //$NON-NLS-1$
		if (!(debugger instanceof IPDebugger)) {
			throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), -1,
					Messages.PDebugConfiguration_1, null));
		}
		return (IPDebugger) debugger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugConfiguration#getID()
	 */
	public String getID() {
		return getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugConfiguration#getModeList()
	 */
	public String[] getModeList() {
		return getModes().toArray(new String[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugConfiguration#getName()
	 */
	public String getName() {
		final String name = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
		return name != null ? name : ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.IPDebugConfiguration#getPlatform()
	 */
	public String getPlatform() {
		final String platform = getConfigurationElement().getAttribute("platform"); //$NON-NLS-1$
		if (platform == null) {
			return "*"; //$NON-NLS-1$
		}
		return platform;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.IPDebugConfiguration#supportsCPU(java.lang
	 * .String)
	 */
	public boolean supportsCPU(String cpu) {
		final String nativeCPU = Platform.getOSArch();
		boolean ret = false;
		if (nativeCPU.startsWith(cpu)) {
			ret = getCPUs().contains(CPU_NATIVE);
		}
		return ret || getCPUs().contains(cpu) || getCPUs().contains("*"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.IPDebugConfiguration#supportsMode(java.lang
	 * .String)
	 */
	public boolean supportsMode(String mode) {
		return getModes().contains(mode);
	}

	/**
	 * @return
	 */
	private IConfigurationElement getConfigurationElement() {
		return fElement;
	}

	/**
	 * @return
	 */
	protected Set<String> getCPUs() {
		if (fCPUs == null) {
			final String cpus = getConfigurationElement().getAttribute("cpu"); //$NON-NLS-1$
			if (cpus == null) {
				fCPUs = new HashSet<String>(1);
				fCPUs.add(CPU_NATIVE);
			} else {
				final String nativeCPU = Platform.getOSArch();
				final StringTokenizer tokenizer = new StringTokenizer(cpus, ","); //$NON-NLS-1$
				fCPUs = new HashSet<String>(tokenizer.countTokens());
				while (tokenizer.hasMoreTokens()) {
					final String cpu = tokenizer.nextToken().trim();
					fCPUs.add(cpu);
					if (nativeCPU.startsWith(cpu)) { // os arch be cpu{le/be}
						fCPUs.add(CPU_NATIVE);
					}
				}
			}
		}
		return fCPUs;
	}

	/**
	 * Returns the set of modes specified in the configuration data.
	 * 
	 * @return the set of modes specified in the configuration data
	 */
	protected Set<String> getModes() {
		if (fModes == null) {
			final String modes = getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes == null) {
				return new HashSet<String>(0);
			}
			final StringTokenizer tokenizer = new StringTokenizer(modes, ","); //$NON-NLS-1$
			fModes = new HashSet<String>(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fModes.add(tokenizer.nextToken().trim());
			}
		}
		return fModes;
	}
}
