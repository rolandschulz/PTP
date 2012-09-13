/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;

/**
 * Matcher for binary files. Return true for files known by CDT to be binaries and for ELF binaries in general. Together, these two
 * tests should filter most compiler-produced files, since CDT should compile most local files, and remote systems are nearly always
 * Linux systems.
 * 
 * Other binary file types, such as image files, are not excluded, but compiler-produced files are the main concern.
 * 
 * Bug 386525: Decided to remove the option to filter binary files for Juno SR1. So this class is no longer used but is available for
 * possible future development to create a more efficient and useful binary filter.
 */
public class BinaryResourceMatcher extends ResourceMatcher {
	/**
	 * Test if given resource is a binary file.
	 * 
	 * @return whether resource is a binary file
	 */
	public boolean match(IResource candidate) {
		if (!(candidate instanceof IFile)) {
			return false;
		} else if (BinaryResourceMatcher.isCDTBinary((IFile) candidate)) {
			return true;
		} else if (BinaryResourceMatcher.isELFBinary((IFile) candidate)) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isCDTBinary(IFile candidate) {
		try {
			ICElement fileElement = CoreModel.getDefault().create(candidate.getFullPath());
			if (fileElement == null) {
				return false;
			}
			int resType = fileElement.getElementType();
			if (resType == ICElement.C_BINARY) {
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException e) {
			// CDT throws this exception for files not recognized. For now, be conservative and allow these files.
			return false;
		}
	}
	
	private static boolean isELFBinary(IFile candidate) {
		BufferedInputStream fileInput = null;
		try {
			if (candidate.exists()) {
				fileInput = new BufferedInputStream(((IFile) candidate).getContents());
			} else {
				fileInput = RemoteContentProvider.getFileContents((IFile) candidate);
			}
			
			byte[] magicNumber = new byte[4];
			fileInput.read(magicNumber);
			if ((magicNumber[0] == 0x7f) &&
			    (magicNumber[1] == 0x45) && // E
			    (magicNumber[2] == 0x4c) && // L
			    (magicNumber[3] == 0x46)) { // F
				return true;
			} else {
				return false;
			}
		} catch (CoreException e) {
			RDTSyncCorePlugin.log(Messages.BinaryResourceMatcher_1 + candidate.getProjectRelativePath().toString(), e);
			return false;
		} catch (IOException e) {
			RDTSyncCorePlugin.log(Messages.BinaryResourceMatcher_1 + candidate.getProjectRelativePath().toString(), e);
			return false;
		} catch (MissingConnectionException e) {
			return false;
		} finally {
			if (fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException e) {
					RDTSyncCorePlugin.log(e);
				}
			}
		}
	}
	
	// All binary matchers are equal
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof BinaryResourceMatcher) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return Messages.BinaryResourceMatcher_0;
	}
}
