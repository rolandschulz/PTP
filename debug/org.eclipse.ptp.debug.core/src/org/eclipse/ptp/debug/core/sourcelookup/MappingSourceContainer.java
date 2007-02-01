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
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.sourcelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.internal.core.sourcelookup.MapEntrySourceContainer;

/**
 * The source container for path mappings.
 */
public class MappingSourceContainer extends AbstractSourceContainer {

	/**
	 * Unique identifier for the mapping source container type (value <code>org.eclipse.cdt.debug.core.containerType.mapping</code>).
	 */
	public static final String TYPE_ID = PTPDebugCorePlugin.getUniqueIdentifier() + ".containerType.mapping";

	private String fName;

	private ArrayList fContainers;

	/**
	 * Constructor for MappingSourceContainer.
	 */
	public MappingSourceContainer(String name) {
		fName = name;
		fContainers = new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.ISourceContainer#getType()
	 */
	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return !fContainers.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		return findSourceElements(name, getSourceContainers());
	}

	protected Object[] findSourceElements(String name, ISourceContainer[] containers) throws CoreException {
		List results = null;
		CoreException single = null;
		MultiStatus multiStatus = null;
		if (isFindDuplicates()) {
			results = new ArrayList();
		}
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer container = containers[i];
			try {
				Object[] objects = container.findSourceElements(name);
				if (objects.length > 0) {
					if (isFindDuplicates()) {
						for (int j = 0; j < objects.length; j++) {
							results.add(objects[j]);
						}
					} else {
						if (objects.length == 1) {
							return objects;
						}
						return new Object[] { objects[0] };
					}
				}
			} catch (CoreException e) {
				if (single == null) {
					single = e;
				} else if (multiStatus == null) {
					multiStatus = new MultiStatus(DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, new IStatus[] { single.getStatus() }, SourceLookupMessages.getString("MappingSourceContainer.0"), null); //$NON-NLS-1$
					multiStatus.add(e.getStatus());
				} else {
					multiStatus.add(e.getStatus());
				}
			}
		}
		if (results == null) {
			if (multiStatus != null) {
				throw new CoreException(multiStatus);
			} else if (single != null) {
				throw single;
			}
			return EMPTY;
		}
		return results.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer#getSourceContainers()
	 */
	public ISourceContainer[] getSourceContainers() throws CoreException {
		return (MapEntrySourceContainer[]) fContainers.toArray(new MapEntrySourceContainer[fContainers.size()]);
	}

	public void addMapEntry(MapEntrySourceContainer entry) {
		fContainers.add(entry);
	}

	public void addMapEntries(MapEntrySourceContainer[] entries) {
		fContainers.addAll(Arrays.asList(entries));
	}

	public void removeMapEntry(MapEntrySourceContainer entry) {
		fContainers.remove(entry);
	}

	public void removeMapEntries(MapEntrySourceContainer[] entries) {
		fContainers.removeAll(Arrays.asList(entries));
	}

	public void clear() {
		Iterator it = fContainers.iterator();
		while (it.hasNext()) {
			((ISourceContainer) it.next()).dispose();
		}
		fContainers.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#dispose()
	 */
	public void dispose() {
		super.dispose();
		Iterator it = fContainers.iterator();
		while (it.hasNext()) {
			((ISourceContainer) it.next()).dispose();
		}
		fContainers.clear();
	}

	public MappingSourceContainer copy() {
		MappingSourceContainer copy = new MappingSourceContainer(fName);
		MapEntrySourceContainer[] entries = new MapEntrySourceContainer[fContainers.size()];
		for (int i = 0; i < entries.length; ++i) {
			copy.addMapEntry(((MapEntrySourceContainer) fContainers.get(i)).copy());
		}
		return copy;
	}

	public void setName(String name) {
		fName = name;
	}

	public IPath getCompilationPath(String sourceName) {
		IPath path = new Path(sourceName);
		IPath result = null;
		try {
			ISourceContainer[] containers = getSourceContainers();
			for (int i = 0; i < containers.length; ++i) {
				MapEntrySourceContainer entry = (MapEntrySourceContainer) containers[i];
				IPath local = entry.getLocalPath();
				if (local.isPrefixOf(path)) {
					result = entry.getBackendPath().append(path.removeFirstSegments(local.segmentCount()));
					break;
				}
			}
		} catch (CoreException e) {
		}
		return result;
	}
}
