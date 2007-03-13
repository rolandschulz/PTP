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
package org.eclipse.ptp.debug.internal.core;

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
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;

public class PDebugConfiguration implements IPDebugConfiguration {
	/**
	 * The configuration element of the extension.
	 */
	private IConfigurationElement fElement;
	private HashSet<String> fModes;
	private HashSet<String> fCPUs;
	private String[] fCoreExt;

	public PDebugConfiguration(IConfigurationElement element) {
		fElement = element;
	}

	private IConfigurationElement getConfigurationElement() {
		return fElement;
	}

	public IAbstractDebugger createDebugger() throws CoreException {
		Object debugger = getConfigurationElement().createExecutableExtension("class");
		if (debugger instanceof IAbstractDebugger) {
			return (IAbstractDebugger)debugger;
		}
		throw new CoreException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), -1, InternalDebugCoreMessages.getString("DebugConfiguration.0"), null));
	}

	public String getName() {
		String name = getConfigurationElement().getAttribute("name");
		return name != null ? name : "";
	}

	public String getID() {
		return getConfigurationElement().getAttribute("id");
	}

	public String getPlatform() {
		String platform = getConfigurationElement().getAttribute("platform");
		if (platform == null) {
			return "*";
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
		return ret || getCPUs().contains(cpu) || getCPUs().contains("*");
	}
	
	/**
	 * Returns the set of modes specified in the configuration data.
	 * 
	 * @return the set of modes specified in the configuration data
	 */
	protected Set<String> getModes() {
		if (fModes == null) {
			String modes = getConfigurationElement().getAttribute("modes");
			if (modes == null) {
				return new HashSet<String>(0);
			}
			StringTokenizer tokenizer = new StringTokenizer(modes, ",");
			fModes = new HashSet<String>(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fModes.add(tokenizer.nextToken().trim());
			}
		}
		return fModes;
	}

	protected Set<String> getCPUs() {
		if (fCPUs == null) {
			String cpus = getConfigurationElement().getAttribute("cpu");
			if (cpus == null) {
				fCPUs = new HashSet<String>(1);
				fCPUs.add(CPU_NATIVE);
			}
			else {
				String nativeCPU = Platform.getOSArch();
				StringTokenizer tokenizer = new StringTokenizer(cpus, ",");
				fCPUs = new HashSet<String>(tokenizer.countTokens());
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
			List<String> exts = new ArrayList<String>();
			String cexts = getConfigurationElement().getAttribute("coreFileFilter");
			if (cexts != null) {
				StringTokenizer tokenizer = new StringTokenizer(cexts, ",");
				while (tokenizer.hasMoreTokens()) {
					String ext = tokenizer.nextToken().trim();
					exts.add(ext);
				}
			}
			exts.add("*.*");
			fCoreExt = (String[])exts.toArray(new String[exts.size()]);
		}
		return fCoreExt;
	}
}
