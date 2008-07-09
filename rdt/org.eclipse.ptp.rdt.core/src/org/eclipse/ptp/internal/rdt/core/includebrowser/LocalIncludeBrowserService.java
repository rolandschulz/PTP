/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.includebrowser.IBContentProvider
 * Version: 1.15
 */

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.includebrowser.IncludeBrowserUI
 * Version: 1.1
 */

package org.eclipse.ptp.internal.rdt.core.includebrowser;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;

public class LocalIncludeBrowserService extends AbstractIncludeBrowserService {

	private static final IIndexInclude[] EMPTY = new IIndexInclude[0];

	private IIndexInclude[] findIncludedBy(IIndex index, IIndexFileLocation ifl, IProgressMonitor pm) {
		try {
			if (ifl != null) {
				IIndexFile[] files= index.getFiles(ifl);
				if (files.length == 1) {
					return index.findIncludedBy(files[0]);
				}
				if (files.length > 0) {
					ArrayList<IIndexInclude> list= new ArrayList<IIndexInclude>();
					HashSet<IIndexFileLocation> handled= new HashSet<IIndexFileLocation>();
					for (int i = 0; i < files.length; i++) {
						final IIndexInclude[] includes = index.findIncludedBy(files[i]);
						for (int j = 0; j < includes.length; j++) {
							IIndexInclude indexInclude = includes[j];
							if (handled.add(indexInclude.getIncludedByLocation())) {
								list.add(indexInclude);
							}
						}
					}
					return list.toArray(new IIndexInclude[list.size()]);
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		} 
		return new IIndexInclude[0];
	}

	private IIndexInclude[] findIncludesTo(IIndex index, IIndexFileLocation ifl, IProgressMonitor pm) {
		try {
			if (ifl != null) {
				IIndexFile[] files= index.getFiles(ifl);
				if (files.length == 1) {
					return index.findIncludes(files[0]);
				}
				if (files.length > 0) {
					ArrayList<IIndexInclude> list= new ArrayList<IIndexInclude>();
					HashSet<IIndexFileLocation> handled= new HashSet<IIndexFileLocation>();
					for (int i = 0; i < files.length; i++) {
						final IIndexInclude[] includes = index.findIncludes(files[i]);
						for (int j = 0; j < includes.length; j++) {
							IIndexInclude indexInclude = includes[j];
							if (handled.add(indexInclude.getIncludesLocation())) {
								list.add(indexInclude);
							}
						}
					}
					return list.toArray(new IIndexInclude[list.size()]);
				}
			}
		}
		catch (CoreException e) {
			CCorePlugin.log(e);
		} 
		return new IIndexInclude[0];
	}

	public IIndexInclude[] findIncludedBy(IIndexFileLocation ifl, IProgressMonitor progress) {
		IIndex index;
		try {
			ICProject[] scope= CoreModel.getDefault().getCModel().getCProjects();
			index= CCorePlugin.getIndexManager().getIndex(scope);
			index.acquireReadLock();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return EMPTY;
		} catch (InterruptedException e) {
			return EMPTY;
		}
		
		try {
			return findIncludedBy(index, ifl, progress);
		} finally {
			index.releaseReadLock();
		}
	}

	public IIndexInclude[] findIncludesTo(IIndexFileLocation ifl, IProgressMonitor progress) {
		IIndex index;
		try {
			ICProject[] scope= CoreModel.getDefault().getCModel().getCProjects();
			index= CCorePlugin.getIndexManager().getIndex(scope);
			index.acquireReadLock();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return EMPTY;
		} catch (InterruptedException e) {
			return EMPTY;
		}
		
		try {
			return findIncludesTo(index, ifl, progress);
		} finally {
			index.releaseReadLock();
		}
	}

	public IIndexInclude findInclude(IInclude input) throws CoreException {
		ICProject project= input.getCProject();
		if (project != null) {
			IIndex index= CCorePlugin.getIndexManager().getIndex(project);
			try {
				index.acquireReadLock();
				try {
					return IndexQueries.elementToInclude(index, input);
				} finally {
					index.releaseReadLock();
				}
			} catch (InterruptedException e) {
				return null;
			}
		}
		return null;
	}
}
