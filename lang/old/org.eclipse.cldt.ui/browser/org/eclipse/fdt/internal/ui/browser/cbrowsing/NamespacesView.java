/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.ui.browser.cbrowsing;

import org.eclipse.core.resources.IProject;
import org.eclipse.fdt.core.browser.AllTypesCache;
import org.eclipse.fdt.core.browser.ITypeInfo;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ICModel;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.IEnumeration;
import org.eclipse.fdt.core.model.ISourceRoot;
import org.eclipse.fdt.core.model.IStructure;
import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.fdt.core.model.ITypeDef;
import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.fdt.ui.PreferenceConstants;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.part.IShowInTargetList;

public class NamespacesView extends CBrowsingPart {

//	private SelectAllAction fSelectAllAction;

	/**
	 * Creates and returns the label provider for this part.
	 * 
	 * @return the label provider
	 * @see org.eclipse.jface.viewers.ILabelProvider
	 */
	protected LabelProvider createLabelProvider() {
	    return new NamespacesViewLabelProvider();
	}

	/**
	 * Answer the property defined by key.
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { FortranUIPlugin.FVIEW_ID, IPageLayout.ID_RES_NAV  };
				}

			};
		}
		return super.getAdapter(key);
	}
	
	/**
	 * Creates the viewer of this part dependent on the current
	 * layout.
	 * 
	 * @param parent the parent for the viewer
	 */
	protected StructuredViewer createViewer(Composite parent) {
		StructuredViewer viewer;
//		if(isInListState())
			viewer= createTableViewer(parent);
//		else
//			viewer= createTreeViewer(parent);
	
//		fWrappedViewer.setViewer(viewer);
//		return fWrappedViewer;
		return viewer;
	}
	private ElementTableViewer createTableViewer(Composite parent) {
		return new ElementTableViewer(parent, SWT.MULTI);
	}
	
	/**
	 * Creates the the content provider of this part.
	 */
	protected IContentProvider createContentProvider() {
		return new NamespacesViewContentProvider(this);
	}
	
	protected ViewerSorter createViewerSorter() {
	    return new TypeInfoSorter();
	}

	/**
	 * Adds filters the viewer of this part.
	 */
	protected void addFilters() {
		super.addFilters();
		getViewer().addFilter(new CBrowsingElementFilter());
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * input for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid input
	 */
	protected boolean isValidInput(Object element) {
		return (element instanceof ICProject || element instanceof ISourceRoot);
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * element for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid element
	 */
	protected boolean isValidElement(Object element) {
		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo)element;
			if (info.exists() && info.getCElementType() == ICElement.C_NAMESPACE) {
				// make sure it has types other than namespaces
				ITypeInfo[] types = info.getEnclosedTypes();
				if (types != null) {
					for (int i = 0; i < types.length; ++i) {
						if (types[i].getCElementType() != ICElement.C_NAMESPACE) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns the context ID for the Help system
	 * 
	 * @return	the string used as ID for the Help context
	 */
	protected String getHelpContextId() {
		return ICHelpContextIds.TYPES_VIEW;
	}
	
	protected String getLinkToEditorKey() {
		return PreferenceConstants.LINK_BROWSING_TYPES_TO_EDITOR;
	}

	protected void createActions() {
		super.createActions();
//		fSelectAllAction= new SelectAllAction((TableViewer)getViewer());
	}

	protected void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		
		// Add selectAll action handlers.
//		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, fSelectAllAction);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.ui.browser.cbrowsing.CBrowsingPart#findInputForElement(java.lang.Object)
	 */
	protected Object findInputForElement(Object element) {
		if (element instanceof ICModel) {
			return null;
		}

		if (element instanceof ICProject || element instanceof ISourceRoot) {
			if (exists(element))
				return element;
		}
		
		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo)element;
			ISourceRoot root = findSourceRoot(info);
			if (exists(root) && !isProjectSourceRoot(root))
				return root;
			ICProject cProject = findCProject(info);
			if (exists(cProject))
				return cProject;
		}
		
		if (element instanceof ICElement) {
			ICElement cElem = (ICElement)element;
			ISourceRoot root = findSourceRoot(cElem);
			if (exists(root) && !isProjectSourceRoot(root))
				return root;
			ICProject cProject = findCProject(cElem);
			if (exists(cProject))
				return cProject;
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.ui.browser.cbrowsing.CBrowsingPart#findElementToSelect(java.lang.Object)
	 */
	protected Object findElementToSelect(Object element) {
		if (element instanceof ICModel || element instanceof ICProject || element instanceof ISourceRoot) {
			return null;
		}

		if (element instanceof ICElement) {
		    ICElement celem = (ICElement)element;
		    if (celem instanceof ITranslationUnit) {
		        IProject project = celem.getCProject().getProject();
		        return AllTypesCache.getGlobalNamespace(project);
		    } else if (celem.getElementType() == ICElement.C_NAMESPACE) {
		        return AllTypesCache.getTypeForElement(celem, true, true, null);
		    } else {
		        ICElement parent = (ICElement)element;
		        while (parent != null) {
		            if ((parent instanceof IStructure
				        || parent instanceof IEnumeration
				        || parent instanceof ITypeDef)
				        && parent.exists()) {
					    ITypeInfo info = AllTypesCache.getTypeForElement(parent, true, true, null);
					    if (info != null) {
					        return info.getEnclosingNamespace(true);
					    }
		            }
					parent = parent.getParent();
		        }
			}
			return null;
		}

		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo) element;
			if (info.getCElementType() == ICElement.C_NAMESPACE && info.exists()) {
				return info;
			}
			return info.getEnclosingNamespace(true);
		}

		return null;
	}
}
