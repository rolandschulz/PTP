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
package org.eclipse.ptp.debug.core.model;

import java.math.BigInteger;

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugException;

/**
 * Represents a module
 * 
 * @author clement
 * 
 */
public interface IPModule extends IPDebugElement {

	/**
	 * Type constant which identifies executables.
	 */
	public static final int EXECUTABLE = 1;

	/**
	 * Type constant which identifies shared libraries.
	 */
	public static final int SHARED_LIBRARY = 2;

	/**
	 * Returns the type of this module. The returned value will be one of
	 * 
	 * @return the type of this module
	 */
	public int getType();

	/**
	 * Returns the name of this module.
	 * 
	 * @return the name of this module
	 */
	public String getName();

	/**
	 * Returns the image name of this module. The name may or may not contain a
	 * full path.
	 * 
	 * @return the image name of this module
	 */
	public IPath getImageName();

	/**
	 * Returns the full path of the file from which symbols to be loaded.
	 * 
	 * @return the full path of the file from which symbols to be loaded
	 */
	public IPath getSymbolsFileName();

	/**
	 * Associate the specified file as a symbol provider for this module. If <code>null</code> is passed as a file name the internal
	 * symbols search
	 * mechanism will be used.
	 * 
	 * @param symbolsFile
	 *            the symbol provider for this module.
	 * @throws DebugException
	 *             if this method fails. Reasons include:
	 */
	public void setSymbolsFileName(IPath symbolsFile) throws DebugException;

	/**
	 * Returns the base address of this module.
	 * 
	 * @return the base address of this module
	 */
	public BigInteger getBaseAddress();

	/**
	 * Returns the size of this module.
	 * 
	 * @return the size of this module
	 */
	public long getSize();

	/**
	 * Returns whether the symbols of this module are read.
	 * 
	 * @return whether the symbols of this module are read
	 */
	public boolean areSymbolsLoaded();

	/**
	 * Returns whether the module's symbols can be loaded or reloaded.
	 * 
	 * @return whether the module's symbols can be loaded or reloaded
	 */
	public boolean canLoadSymbols();

	/**
	 * Loads the module symbols from the specified file.
	 * 
	 * @throws DebugException
	 *             if this method fails. Reasons include:
	 */
	public void loadSymbols() throws DebugException;

	/**
	 * Returns the name of the platform.
	 * 
	 * @return the name of the platform
	 */
	public String getPlatform();

	/**
	 * Returns whether this module is little endian.
	 * 
	 * @return whether this module is little endian
	 */
	public boolean isLittleEndian();

	/**
	 * Returns the CPU identifier.
	 * 
	 * @return the CPU identifier
	 */
	public String getCPU();
}
