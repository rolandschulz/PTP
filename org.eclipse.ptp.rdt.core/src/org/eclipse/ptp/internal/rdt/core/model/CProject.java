/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.util.Map;

import org.eclipse.cdt.core.model.BufferChangedEvent;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class CProject extends Parent implements ICProject {
	private static final long serialVersionUID = 1L;

	public CProject(String name) {
		super(null, ICElement.C_PROJECT, name);
		fCProject = this;
	}
	
	public ICElement findElement(IPath path) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRoot findSourceRoot(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRoot findSourceRoot(IPath path) {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRoot[] getAllSourceRoots() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IArchiveContainer getArchiveContainer() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IBinaryContainer getBinaryContainer() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IIncludeReference[] getIncludeReferences() throws CModelException {
		return new IIncludeReference[0];
	}

	public ILibraryReference[] getLibraryReferences() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getNonCResources() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOption(String optionName, boolean inheritCCoreOptions) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getOptions(boolean inheritCCoreOptions) {
		// TODO Auto-generated method stub
		return null;
	}

	public IOutputEntry[] getOutputEntries() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IProject getProject() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		return root.getProject(getElementName());
	}

	public IPathEntry[] getRawPathEntries() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getRequiredProjectNames() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IPathEntry[] getResolvedPathEntries() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRoot getSourceRoot(ISourceEntry entry) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRoot[] getSourceRoots() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isOnOutputEntry(IResource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOnSourceRoot(IResource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOnSourceRoot(ICElement element) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setOption(String optionName, String optionValue) {
		// TODO Auto-generated method stub

	}

	public void setOptions(Map<String, String> newOptions) {
		// TODO Auto-generated method stub

	}

	public void setRawPathEntries(IPathEntry[] entries, IProgressMonitor monitor)
			throws CModelException {
		// TODO Auto-generated method stub

	}

	public void close() throws CModelException {
		// TODO Auto-generated method stub

	}

	public IBuffer getBuffer() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasUnsavedChanges() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isConsistent() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	public void makeConsistent(IProgressMonitor progress)
			throws CModelException {
		// TODO Auto-generated method stub

	}

	public void makeConsistent(IProgressMonitor progress, boolean forced)
			throws CModelException {
		// TODO Auto-generated method stub

	}

	public void open(IProgressMonitor progress) throws CModelException {
		// TODO Auto-generated method stub

	}

	public void save(IProgressMonitor progress, boolean force)
			throws CModelException {
		// TODO Auto-generated method stub

	}

	public void bufferChanged(BufferChangedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return getElementName();
	}
}
