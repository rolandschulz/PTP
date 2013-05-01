/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.debug.core.sourcelookup;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;

/**
 * Map a path into a local resource location.
 * 
 * Absolute paths of the form <map_path>/file are mapped into
 * <map_container>/file.
 * 
 * For example, if: <map_path> = /remote/path <map_container> = P/local/path
 * 
 * then the path /remote/path/to/src/file.c is mapped into
 * P/local/path/to/src/file.c
 * 
 * Relative paths are mapped into the corresponding location in <map_container>.
 * 
 * For example: src/file.c is mapped into P/local/path/src/file.c
 * 
 * @author greg
 * @since 4.0
 * 
 */
public class ResourceMappingSourceContainer extends AbstractSourceContainer {
	public static final String TYPE_ID = PTPDebugCorePlugin.getUniqueIdentifier() + ".containerType.resourceMapping"; //$NON-NLS-1$
	private IPath fPath;
	private IContainer fContainer;

	public ResourceMappingSourceContainer() {
		fPath = Path.EMPTY;
		fContainer = null;
	}

	public ResourceMappingSourceContainer(IPath path, IContainer container) {
		fPath = path;
		fContainer = container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ResourceMappingSourceContainer)) {
			return false;
		}
		ResourceMappingSourceContainer entry = (ResourceMappingSourceContainer) o;
		return (entry.getPath().equals(getPath()) && entry.getContainer().equals(getContainer()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.sourcelookup.ISourceContainer#findSourceElements
	 * (java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		IPath path = new Path(name);
		if (path.isAbsolute()) {
			if (!getPath().isPrefixOf(path)) {
				return EMPTY;
			}
			path = path.removeFirstSegments(getPath().segmentCount());
		}

		IFile file = getContainer().getFile(path);
		if (file.exists()) {
			return new Object[] { file };
		}
		return EMPTY;
	}

	/**
	 * Given a path relative to the container, map this relative to the mapping
	 * path (reverse mapping). If the container is a remote item, this returns
	 * null.
	 * 
	 * @param path
	 *            path to map
	 * @return path mapped relative to the mapping path
	 */
	public IPath getCompilationPath(String path) {
		IPath sourcePath = new Path(path);
		if (!sourcePath.isAbsolute()) {
			return getPath().append(sourcePath);
		}
		IContainer container = getContainer();
		IPath localPath = container.getProject().getLocation();
		if (localPath != null) {
			localPath.append(container.getProjectRelativePath());
			if (localPath.isPrefixOf(sourcePath)) {
				return getPath().append(sourcePath.removeFirstSegments(localPath.segmentCount()));
			}
		}
		return null;
	}

	/**
	 * Get the container used for this mapping
	 * 
	 * @return container
	 */
	public IContainer getContainer() {
		return fContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return NLS
				.bind("{0}/{1} <-> {2}", new Object[] { getContainer().getProject().getName(), getContainer().getProjectRelativePath(), getPath().toOSString() }); //$NON-NLS-1$
	}

	/**
	 * Get the mapping path
	 * 
	 * @return mapping path
	 */
	public IPath getPath() {
		return fPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/**
	 * Set the mapping container
	 * 
	 * @param container
	 */
	public void setContainer(IContainer container) {
		fContainer = container;
	}

	/**
	 * Set the mapping path
	 * 
	 * @param path
	 */
	public void setPath(IPath path) {
		fPath = path;
	}
}
