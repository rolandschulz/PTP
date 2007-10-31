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
package org.eclipse.ptp.debug.internal.core.sourcelookup;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;

public class MapEntrySourceContainer extends AbstractSourceContainer {
	public static final String TYPE_ID = PTPDebugCorePlugin.getUniqueIdentifier() + ".containerType.mapEntry";
	private IPath fLocalPath;
	private IPath fBackendPath;

	public MapEntrySourceContainer() {
		fBackendPath = Path.EMPTY;
		fLocalPath = Path.EMPTY;
	}
	public MapEntrySourceContainer(IPath backend, IPath local) {
		fBackendPath = backend;
		fLocalPath = local;
	}
	public Object[] findSourceElements(String name) throws CoreException {
		IPath path = new Path(name);
		if (getBackendPath().isPrefixOf(path)) {
			path = path.removeFirstSegments(getBackendPath().segmentCount());
			path = getLocalPath().append(path);
			IFile[] wsFiles = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
			ArrayList<IFile> list = new ArrayList<IFile>();
			for (int j = 0; j < wsFiles.length; ++j) {
				if (wsFiles[j].exists()) {
					list.add(wsFiles[j]);
					if (!isFindDuplicates())
						break;
				}
			}
			if (list.size() > 0)
				return list.toArray();
			File file = path.toFile();
			if (file.exists() && file.isFile()) {
				return new Object[] { new LocalFileStorage(file) };
			}
		}
		return EMPTY;
	}
	public String getName() {
		return MessageFormat.format("{0} - {1}", new Object[] { getBackendPath().toOSString(), getLocalPath().toOSString() });
	}
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}
	public IPath getLocalPath() {
		return fLocalPath;
	}
	public IPath getBackendPath() {
		return fBackendPath;
	}
	public void setLocalPath(IPath local) {
		fLocalPath = local;
	}
	public void setBackendPath(IPath backend) {
		fBackendPath = backend;
	}
	public boolean equals(Object o) {
		if (!(o instanceof MapEntrySourceContainer))
			return false;
		MapEntrySourceContainer entry = (MapEntrySourceContainer) o;
		return (entry.getBackendPath().equals(getBackendPath()) && entry.getLocalPath().equals(getLocalPath()));
	}
	public MapEntrySourceContainer copy() {
		return new MapEntrySourceContainer(fBackendPath, fLocalPath);
	}
}
