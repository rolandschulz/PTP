/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.Archive;
import org.eclipse.cdt.internal.core.model.ArchiveContainer;
import org.eclipse.cdt.internal.core.model.Binary;
import org.eclipse.cdt.internal.core.model.BinaryContainer;
import org.eclipse.cdt.internal.core.model.CContainerInfo;
import org.eclipse.cdt.internal.core.model.CElementInfo;
import org.eclipse.cdt.internal.core.model.Openable;
import org.eclipse.cdt.internal.core.model.OpenableInfo;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.fdt.core.model.FortranCoreModel;
import org.eclipse.fdt.internal.core.model.FortranModelManager;

public class FortranContainer extends Openable implements ICContainer {
	FortranModelManager factory = FortranModelManager.getDefault();

	public FortranContainer(ICElement parent, IResource res) {
		this(parent, res, ICElement.C_CCONTAINER);
	}

	public FortranContainer(ICElement parent, IResource res, int type) {
		super(parent, res, type);
	}

	/**
	 * Returns a the collection of binary files in this ccontainer
	 * 
	 * @see ICContainer#getBinaries()
	 */
	public IBinary[] getBinaries() throws CModelException {
		List list = getChildrenOfType(C_BINARY);
		IBinary[] array = new IBinary[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * @see ICContainer#getBinary(String)
	 */
	public IBinary getBinary(String name) {
		IFile file = getContainer().getFile(new Path(name));
		return getBinary(file);
	}

	public IBinary getBinary(IFile file) {
		IBinaryFile bin = factory.createBinaryFile(file);
		if (bin instanceof IBinaryObject) {
			return new Binary(this, file, (IBinaryObject) bin);
		}
		return new Binary(this, file, null);
	}

	/**
	 * Returns a the collection of archive files in this ccontainer
	 * 
	 * @see ICContainer#getArchives()
	 */
	public IArchive[] getArchives() throws CModelException {
		List list = getChildrenOfType(C_ARCHIVE);
		IArchive[] array = new IArchive[list.size()];
		list.toArray(array);
		return array;
	}

	/**
	 * @see ICContainer#getArchive(String)
	 */
	public IArchive getArchive(String name) {
		IFile file = getContainer().getFile(new Path(name));
		return getArchive(file);
	}

	public IArchive getArchive(IFile file) {
		IBinaryFile ar = factory.createBinaryFile(file);
		if (ar != null && ar.getType() == IBinaryFile.ARCHIVE) {
			return new Archive(this, file, (IBinaryArchive) ar);
		}
		return new Archive(this, file, null);
	}

	/**
	 * @see ICContainer#getTranslationUnits()
	 */
	public ITranslationUnit[] getTranslationUnits() throws CModelException {
		List list = getChildrenOfType(C_UNIT);
		ITranslationUnit[] array = new ITranslationUnit[list.size()];
		list.toArray(array);
		return array;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.model.ICContainer#getTranslationUnit(java.lang.String)
	 */
	public ITranslationUnit getTranslationUnit(String name) {
		IFile file = getContainer().getFile(new Path(name));
		return getTranslationUnit(file);
	}

	public ITranslationUnit getTranslationUnit(IFile file) {
		String id = FortranCoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
		return new TranslationUnit(this, file, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.model.ICContainer#getCContainers()
	 */
	public ICContainer[] getCContainers() throws CModelException {
		List list = getChildrenOfType(C_CCONTAINER);
		ICContainer[] array = new ICContainer[list.size()];
		list.toArray(array);
		return array;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.model.ICContainer#getCContainer(java.lang.String)
	 */
	public ICContainer getCContainer(String name) {
		IFolder folder = getContainer().getFolder(new Path(name));
		return getCContainer(folder);
	}

	public ICContainer getCContainer(IFolder folder) {
		return new FortranContainer(this, folder);
	}

	public IContainer getContainer() {
		return (IContainer) getResource();
	}

	protected CElementInfo createElementInfo() {
		return new CContainerInfo(this);
	}

	// CHECKPOINT: folders will return the hash code of their path
	public int hashCode() {
		return getPath().hashCode();
	}

	/**
	 * @see Openable
	 */
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
			throws CModelException {
		boolean validInfo = false;
		try {
			IResource res = getResource();
			if (res != null && res.isAccessible()) {
				validInfo = computeChildren(info, res);
			} else {
				throw newNotPresentException();
			}
		} finally {
			if (!validInfo) {
				FortranModelManager.getDefault().removeInfo(this);
			}
		}
		return validInfo;
	}

	/*
	 * (non-Javadoc) Returns an array of non-c resources contained in the
	 * receiver.
	 * 
	 * @see org.eclipse.cdt.core.model.ICContainer#getNonCResources()
	 */
	public Object[] getNonCResources() throws CModelException {
		return ((CContainerInfo) getElementInfo()).getNonCResources(getResource());
	}

	protected boolean computeChildren(OpenableInfo info, IResource res) throws CModelException {
		ArrayList vChildren = new ArrayList();
		try {
			IResource[] resources = null;
			if (res instanceof IContainer) {
				//System.out.println (" Resource: " +
				// res.getFullPath().toOSString());
				IContainer container = (IContainer) res;
				resources = container.members(false);
			}
			if (resources != null) {
				ICProject cproject = getCProject();
				ISourceRoot sroot = getSourceRoot();
				for (int i = 0; i < resources.length; i++) {
					if (sroot.isOnSourceEntry(resources[i])) {
						// Check for Valid C Element only.
						ICElement celement = computeChild(resources[i], cproject);
						if (celement != null) {
							vChildren.add(celement);
						}
					}
				}
			}
		} catch (CoreException e) {
			//System.out.println (e);
			//CPlugin.log (e);
			//e.printStackTrace();
			throw new CModelException(e);
		}
		info.setChildren(vChildren);
		if (info instanceof CContainerInfo) {
			((CContainerInfo) info).setNonCResources(null);
		}
		return true;
	}

	protected ICElement computeChild(IResource res, ICProject cproject) throws CModelException {
		ICElement celement = null;
		switch (res.getType()) {
			case IResource.FILE : {
				IFile file = (IFile) res;
				String id = FortranCoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
				if (id != null) {
					celement = new TranslationUnit(this, file, id);
				} else if (cproject.isOnOutputEntry(file)) {
					IBinaryParser.IBinaryFile bin = factory.createBinaryFile(file);
					if (bin != null) {
						if (bin.getType() == IBinaryFile.ARCHIVE) {
							celement = new Archive(this, file, (IBinaryArchive)bin);
							ArchiveContainer vlib = (ArchiveContainer)cproject.getArchiveContainer();
							vlib.addChild(celement);
						} else {
							celement = new Binary(this, file, (IBinaryObject)bin);
							if (bin.getType() == IBinaryFile.EXECUTABLE || bin.getType() == IBinaryFile.SHARED) {
								BinaryContainer vbin = (BinaryContainer)cproject.getBinaryContainer();
								vbin.addChild(celement);
							}
						}
					}
				}
				break;
			}
			case IResource.FOLDER :
				celement = new FortranContainer(this, res);
				break;
		}
		return celement;
	}
}
