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

import org.eclipse.fdt.core.browser.AllTypesCache;
import org.eclipse.fdt.core.browser.ITypeInfo;
import org.eclipse.fdt.core.browser.TypeUtil;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ICModel;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.INamespace;
import org.eclipse.fdt.core.model.ISourceRoot;
import org.eclipse.fdt.core.model.ITranslationUnit;
import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.fdt.ui.PreferenceConstants;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.IShowInTargetList;

public class MembersView extends CBrowsingPart implements IPropertyChangeListener {
	
//	private MemberFilterActionGroup fMemberFilterActionGroup;

	
	public MembersView() {
		setHasWorkingSetFilter(false);
		setHasCustomSetFilter(true);
		FortranUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Answer the property defined by key.
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { FortranUIPlugin.FVIEW_ID };
				}

			};
		}
		return super.getAdapter(key);
	}

	/**
	 * Creates and returns the label provider for this part.
	 * 
	 * @return the label provider
	 * @see org.eclipse.jface.viewers.ILabelProvider
	 */
	protected LabelProvider createLabelProvider() {
	    return new CBrowsingLabelProvider();
	}

	/**
	 * Returns the context ID for the Help system
	 * 
	 * @return	the string used as ID for the Help context
	 */
	protected String getHelpContextId() {
		return ICHelpContextIds.MEMBERS_VIEW;
	}

	protected String getLinkToEditorKey() {
		return PreferenceConstants.LINK_BROWSING_MEMBERS_TO_EDITOR;
	}

	/**
	 * Creates the the viewer of this part.
	 * 
	 * @param parent	the parent for the viewer
	 */
	protected StructuredViewer createViewer(Composite parent) {
	    ElementTreeViewer viewer= new ElementTreeViewer(parent, SWT.MULTI);
//		fMemberFilterActionGroup= new MemberFilterActionGroup(viewer, JavaUI.ID_MEMBERS_VIEW);
		return viewer;
	}
	
	protected ViewerSorter createViewerSorter() {
	    return new CBrowsingViewerSorter();
	}
	
	/**
	 * Adds filters the viewer of this part.
	 */
	protected void addFilters() {
		super.addFilters();
		getViewer().addFilter(new CBrowsingElementFilter());
	}

	protected void fillToolBar(IToolBarManager tbm) {
		tbm.add(new LexicalSortingAction(getViewer(), FortranUIPlugin.ID_MEMBERS_VIEW));
//		fMemberFilterActionGroup.contributeToToolBar(tbm);
		super.fillToolBar(tbm);
	}
	
	/**
	 * Answers if the given <code>element</code> is a valid
	 * input for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid input
	 */
	protected boolean isValidInput(Object element) {
		if (element instanceof ITypeInfo) {
			ITypeInfo type= (ITypeInfo)element;
			if (type.getCElementType() != ICElement.C_NAMESPACE && type.exists())
				return true;
		}
		return false;
	}
	
	/**
	 * Answers if the given <code>element</code> is a valid
	 * element for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid element
	 */
	protected boolean isValidElement(Object element) {
	    if (element instanceof ICElement) {
			if (element instanceof ICModel
			        || element instanceof ICProject
			        || element instanceof ISourceRoot
			        || element instanceof ITranslationUnit)
				return false;
			return true;
		}
		return false;
	}

	/*
	 * Implements method from IViewPart.
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
//		fMemberFilterActionGroup.saveState(memento);
	}	

	protected void restoreState(IMemento memento) {
		super.restoreState(memento);
//		fMemberFilterActionGroup.restoreState(memento);
		getViewer().getControl().setRedraw(false);
		getViewer().refresh();
 		getViewer().getControl().setRedraw(true);
	}

	protected void hookViewerListeners() {
		super.hookViewerListeners();
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				TreeViewer viewer= (TreeViewer)getViewer();
				Object element= ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (viewer.isExpandable(element))
					viewer.setExpandedState(element, !viewer.getExpandedState(element));
			}
		});
	}

/*	boolean isInputAWorkingCopy() {
		Object input= getViewer().getInput();
		if (input instanceof ICElement) {
			ICompilationUnit cu= (ICompilationUnit)((IJavaElement)input).getAncestor(IJavaElement.COMPILATION_UNIT);
			if (cu != null)
				return cu.isWorkingCopy();
		}
		return false;
	}
*/
	protected void restoreSelection() {
		IEditorPart editor= getViewSite().getPage().getActiveEditor();
		if (editor != null)
			setSelectionFromEditor(editor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
//		if (MembersOrderPreferenceCache.isMemberOrderProperty(event.getProperty())) {
//			getViewer().refresh();
//		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.browsing.JavaBrowsingPart#dispose()
	 */
	public void dispose() {
//		if (fMemberFilterActionGroup != null) {
//			fMemberFilterActionGroup.dispose();
//			fMemberFilterActionGroup= null;
//		}
		super.dispose();
		FortranUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.ui.browser.cbrowsing.CBrowsingPart#createContentProvider()
	 */
	protected IContentProvider createContentProvider() {
		return new MembersViewContentProvider(this);
	}

	protected Object findInputForElement(Object element) {
		if (element instanceof ICModel || element instanceof ICProject || element instanceof ISourceRoot) {
			return null;
		}

		if (element instanceof ITypeInfo) {
		    return element;
		}
		
		if (element instanceof ICElement) {
		    ICElement celem = (ICElement)element;
		    if (!celem.exists())
		        return null;
		    
		    if (TypeUtil.isDeclaringType(celem)) {
				ICElement type= TypeUtil.getDeclaringType(celem);
				if (type == null || type instanceof INamespace)
			        return AllTypesCache.getTypeForElement(celem, true, true, null);
				return findInputForElement(type);
		    } else if (TypeUtil.isMemberType(celem)) {
		        return findInputForElement(TypeUtil.getDeclaringType(celem));
		    } else {
		        ITranslationUnit tu = TypeUtil.getTranslationUnit(celem);
		        if (tu != null)
		            return getTypeForTU(tu);
		    }
		}
		return null; 	
	}
		    
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.ui.browser.cbrowsing.CBrowsingPart#findElementToSelect(java.lang.Object)
	 */
	protected Object findElementToSelect(Object element) {
	    if (isValidElement(element)) {
	        return element;
	    }
		return null;
	}
}
