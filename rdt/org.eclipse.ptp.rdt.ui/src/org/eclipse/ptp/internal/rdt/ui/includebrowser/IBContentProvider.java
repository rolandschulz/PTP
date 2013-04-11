/*******************************************************************************
 * Copyright (c) 2006, 2013 Wind River Systems, Inc. and others.
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
 * Version: 1.16
 */

package org.eclipse.ptp.internal.rdt.ui.includebrowser;

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.includebrowser.IBFile;
import org.eclipse.cdt.internal.ui.includebrowser.IBNode;
import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIndexIncludeValue;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IncludeBrowserServiceFactory;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteIndexFileLocation;
import org.eclipse.ptp.internal.rdt.ui.util.PathReplacer;
import org.eclipse.ptp.rdt.core.serviceproviders.IIndexServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.widgets.Display;

/** 
 * This is the content provider for the include browser.
 */
public class IBContentProvider extends AsyncTreeContentProvider {

	private boolean fComputeIncludedBy = true;
	private IncludeBrowserServiceFactory fServiceFactory;  

	/**
	 * Constructs the content provider.
	 */
	public IBContentProvider(Display disp) {
		super(disp);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IBNode) {
			IBNode node = (IBNode) element;
			return node.getParent();
		}
		return super.getParent(element);
	}

	@Override
	protected Object[] syncronouslyComputeChildren(Object parentElement) {
		if (parentElement instanceof ITranslationUnit) {
			ITranslationUnit tu = (ITranslationUnit) parentElement;
			return new Object[] { new IBNode(null, new IBFile(tu), null, 0, 0, 0) };
		}
		if (parentElement instanceof IBNode) {
			IBNode node = (IBNode) parentElement;
			if (node.isRecursive() || node.getRepresentedIFL() == null) {
				return NO_CHILDREN;
			}
		}
		// allow for async computation
		return null;
	}

	@Override
	protected Object[] asyncronouslyComputeChildren(Object parentElement, IProgressMonitor monitor) {
		if (parentElement instanceof IBNode) 
		{
			IBNode node = (IBNode) parentElement;
			IIndexFileLocation ifl= node.getRepresentedIFL();
			ICProject project= node.getCProject();
			if (ifl == null) {
				return NO_CHILDREN;
			}
			
			IBFile directiveFile= null;
			IBFile targetFile= null;
			IIndexIncludeValue[] includes;
			
			if (fServiceFactory == null)
				fServiceFactory  = new IncludeBrowserServiceFactory();
			
			IIncludeBrowserService service = fServiceFactory.getIncludeBrowserService(project);
			
			if (service == null)
				return NO_CHILDREN;
			
			if (fComputeIncludedBy) {
				includes = service.findIncludedBy(ifl, project, monitor);
			}
			else {
				includes= service.findIncludesTo(ifl, project, monitor);
				directiveFile= node.getRepresentedFile();
			}
			if (includes.length > 0) {
				ArrayList<IBNode> result= new ArrayList<IBNode>(includes.length);
				for (int i = 0; i < includes.length; i++) {
					IIndexIncludeValue include = includes[i];
					try {
						if (fComputeIncludedBy) {
							IIndexFileLocation loc= include.getIncludedByLocation();
							if (loc != null) {
								final URI uri = PathReplacer.replacePath(project.getProject(), project.getLocationURI(), loc.getURI().getPath());
								loc = new RemoteIndexFileLocation(uri.getPath(), uri, true);
							}
							directiveFile= targetFile= new IBFile(project, loc);
						}
						else {
							IIndexFileLocation includesPath= include.getIncludesLocation();
							if (includesPath == null) {
								targetFile= new IBFile(include.getFullName());
							}
							else {
								final URI uri = PathReplacer.replacePath(project.getProject(), project.getLocationURI(), includesPath.getURI().getPath());
								includesPath = new RemoteIndexFileLocation(uri.getPath(), uri, true);
								
								targetFile= new IBFile(project, includesPath);
							}
						}
						IBNode newnode= new IBNode(node, targetFile, directiveFile, 
								include.getNameOffset(), 
								include.getNameLength(), 
								include.getIncludedByTimestamp());
						newnode.setIsActiveCode(include.isActive());
						newnode.setIsSystemInclude(include.isSystemInclude());
						result.add(newnode);
					}
					catch (CoreException e) {
						CUIPlugin.log(e);
					}
				}

				return result.toArray();
			}
		}
		return NO_CHILDREN;
	}

	public void setComputeIncludedBy(boolean value) {
		fComputeIncludedBy = value;
	}

	public boolean getComputeIncludedBy() {
		return fComputeIncludedBy;
	}
	
	public IncludeBrowserServiceFactory getServiceFactory()
	{
		return this.fServiceFactory;
	}
	
}
