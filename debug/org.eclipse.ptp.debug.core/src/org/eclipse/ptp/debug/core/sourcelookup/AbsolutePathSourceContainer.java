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
package org.eclipse.ptp.debug.core.sourcelookup;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;

/**
 * @author clement
 * 
 */
public class AbsolutePathSourceContainer extends AbstractSourceContainer {
	public static final String TYPE_ID = PTPDebugCorePlugin.getUniqueIdentifier() + ".containerType.absolutePath";

	private Object[] findSourceElementByFile(File file) {
		IFile[] wfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(file.getAbsolutePath()));
		if (wfiles.length > 0)
			return wfiles;

		try {
			// Check the canonical path as well to support case insensitive file systems like Windows.
			wfiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(file.getCanonicalPath()));
			if (wfiles.length > 0)
				return wfiles;

			// The file is not already in the workspace so try to create an external translation unit for it.
			String projectName = getDirector().getLaunchConfiguration().getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project != null) {
				IPath path = Path.fromOSString(file.getCanonicalPath());
				//TODO
				//String id = CoreModel.getRegistedContentTypeId(project.getProject(), path.lastSegment());
				//return new ExternalTranslationUnit[] { new ExternalTranslationUnit(project, new Path(file.getCanonicalPath()), id) };
			}
		} catch (IOException e) { // ignore if getCanonicalPath throws
		} catch (CoreException e) {
		}
		// If we can't create an ETU then fall back on LocalFileStorage.
		return new LocalFileStorage[] { new LocalFileStorage(file) };
	}
	public boolean isValidAbsoluteFilePath(String name) {
		return isValidAbsoluteFilePath(new File(name));
	}
	public boolean isValidAbsoluteFilePath(File file) {
		return file.isAbsolute() && file.exists() && file.isFile();
	}
	public Object[] findSourceElements(String name) throws CoreException {
		if (name != null) {
			File file = new File(name);
			if (isValidAbsoluteFilePath(file)) {
				return findSourceElementByFile(file);
			}
		}
		return new Object[0];
	}
	public String getName() {
		return SourceLookupMessages.getString("AbsolutePathSourceContainer.0");
	}
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
}
