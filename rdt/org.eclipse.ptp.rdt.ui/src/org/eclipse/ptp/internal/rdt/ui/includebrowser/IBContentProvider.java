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

package org.eclipse.ptp.internal.rdt.ui.includebrowser;

import java.util.ArrayList;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.includebrowser.IBFile;
import org.eclipse.cdt.internal.ui.includebrowser.IBNode;
import org.eclipse.cdt.internal.ui.viewsupport.AsyncTreeContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.includebrowser.IIncludeBrowserService;
import org.eclipse.ptp.internal.rdt.core.includebrowser.LocalIncludeBrowserService;
import org.eclipse.swt.widgets.Display;

/** 
 * This is the content provider for the include browser.
 */
public class IBContentProvider extends AsyncTreeContentProvider {

	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private boolean fComputeIncludedBy = true;
	private IIncludeBrowserService fService = new LocalIncludeBrowserService();

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
		if (parentElement instanceof IBNode) {
			IBNode node = (IBNode) parentElement;
			IIndexFileLocation ifl= node.getRepresentedIFL();
			ICProject project= node.getCProject();
			if (ifl == null) {
				return NO_CHILDREN;
			}
			
			IBFile directiveFile= null;
			IBFile targetFile= null;
			IIndexInclude[] includes;
			if (fComputeIncludedBy) {
				includes= fService.findIncludedBy(ifl, NPM);
			}
			else {
				includes= fService.findIncludesTo(ifl, NPM);
				directiveFile= node.getRepresentedFile();
			}
			if (includes.length > 0) {
				ArrayList<IBNode> result= new ArrayList<IBNode>(includes.length);
				for (int i = 0; i < includes.length; i++) {
					IIndexInclude include = includes[i];
					try {
						if (fComputeIncludedBy) {
							directiveFile= targetFile= new IBFile(project, include.getIncludedByLocation());
						}
						else {
							IIndexFileLocation includesPath= include.getIncludesLocation();
							if (includesPath == null) {
								targetFile= new IBFile(include.getName());
							}
							else {
								targetFile= new IBFile(project, includesPath);
							}
						}
						IBNode newnode= new IBNode(node, targetFile, directiveFile, 
								include.getNameOffset(), 
								include.getNameLength(), 
								include.getIncludedBy().getTimestamp());
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
}
