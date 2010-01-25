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
	private IFileStore fileStore;
	private IFileInfo fileInfo = null;
	private ImageDescriptor image = null; 
	
	public DeferredFileStore(IFileStore store) {
		this(store, null);
	}
	
	public DeferredFileStore(IFileStore store, IFileInfo info) {
		this.fileStore = store;
		this.fileInfo = info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		try {
			IFileStore[] stores = fileStore.childStores(EFS.NONE, null);
			DeferredFileStore[] def = new DeferredFileStore[stores.length];
			for (int i = 0; i < stores.length; i++) {
				def[i] = new DeferredFileStore(stores[i]);
			}
			return def;
		} catch (CoreException e) {
			return new Object[0];
		}
	}

	/* (non-Javadoc)
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
        return (IWorkbenchAdapter)PTPRemoteCorePlugin.getAdapter(element, IWorkbenchAdapter.class);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return fileStore.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return fileStore.getParent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object,
			IElementCollector collector, IProgressMonitor monitor) {
		DeferredFileStore[] children = null;
		try {
			IFileInfo[] info = fileStore.childInfos(EFS.NONE, monitor);
			IFileStore[] wrapped = new IFileStore[info.length];
			children = new DeferredFileStore[info.length];
			for (int i = 0; i < wrapped.length; i++) {
				wrapped[i] = fileStore.getChild(info[i].getName());
				children[i] = new DeferredFileStore(wrapped[i], info[i]);
			}
		} catch (CoreException e) {
		}
		if (children != null) {
			collector.add(children, monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(Object object) {
		return null;
	}

	/* (non-Javadoc)
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
