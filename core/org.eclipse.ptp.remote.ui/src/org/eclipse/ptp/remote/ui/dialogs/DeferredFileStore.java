/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class DeferredFileStore implements IDeferredWorkbenchAdapter {
	private final IFileStore fileStore;
	private IFileInfo fileInfo;
	private ImageDescriptor image;
	private boolean excludeHidden = false;

	/**
	 * @since 7.0
	 */
	public DeferredFileStore(IFileStore store, boolean exclude) {
		this(store, null, exclude);
	}

	/**
	 * @since 7.0
	 */
	public DeferredFileStore(IFileStore store, IFileInfo info, boolean exclude) {
		this.fileStore = store;
		this.fileInfo = info;
		this.excludeHidden = exclude;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		try {
			IFileStore[] stores = fileStore.childStores(EFS.NONE, null);
			List<DeferredFileStore> def = new ArrayList<DeferredFileStore>();
			for (int i = 0; i < stores.length; i++) {
				if (!(excludeHidden && stores[i].getName().startsWith("."))) { //$NON-NLS-1$
					def.add(new DeferredFileStore(stores[i], excludeHidden));
				}
			}
			return def.toArray();
		} catch (CoreException e) {
			return new Object[0];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		if (fileInfo == null) {
			fileInfo = fileStore.fetchInfo();
		}
		if (image == null) {
			FileSystemElement element = new FileSystemElement(fileStore.getName(), null, fileInfo.isDirectory());
			IWorkbenchAdapter adapter = getAdapter(element);
			if (adapter != null) {
				image = adapter.getImageDescriptor(object);
			}
		}
		return image;
	}

	/**
	 * Return the IWorkbenchAdapter for element or the element if it is
	 * an instance of IWorkbenchAdapter. If it does not exist return
	 * null.
	 * 
	 * @param element
	 * @return IWorkbenchAdapter or <code>null</code>
	 */
	protected IWorkbenchAdapter getAdapter(Object element) {
		return (IWorkbenchAdapter) PTPRemoteCorePlugin.getAdapter(element, IWorkbenchAdapter.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return fileStore.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return fileStore.getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object,
	 * org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		ArrayList<DeferredFileStore> children = new ArrayList<DeferredFileStore>();
		try {
			IFileInfo[] childInfos = fileStore.childInfos(EFS.NONE, monitor);
			for (IFileInfo info : childInfos) {
				if (!(excludeHidden && info.getName().startsWith("."))) { //$NON-NLS-1$
					children.add(new DeferredFileStore(fileStore.getChild(info.getName()), info, excludeHidden));
				}
			}
		} catch (CoreException e) {
			// Ignore
		}
		if (children != null) {
			collector.add(children.toArray(), monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(Object object) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
	 */
	public boolean isContainer() {
		if (fileInfo == null) {
			fileInfo = fileStore.fetchInfo();
		}
		return fileInfo.isDirectory();
	}

	/**
	 * Get the filestore backing this object
	 * 
	 * @return
	 */
	public IFileStore getFileStore() {
		return fileStore;
	}
}
