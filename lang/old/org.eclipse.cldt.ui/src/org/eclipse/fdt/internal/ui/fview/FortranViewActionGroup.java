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
package org.eclipse.fdt.internal.ui.fview;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.fdt.ui.FortranUIPlugin;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionGroup;

/**
 * This is the action group for all the view actions.
 * It delegates to several subgroups for most of the actions.
 * 
 * @see GotoActionGroup
 * @see OpenFileGroup
 * @see RefactorActionGroup
 * @see SortAndFilterActionGroup
 * @see WorkspaceActionGroup
 * 
 */
public abstract class FortranViewActionGroup extends ActionGroup {

	/**
	 * The resource navigator.
	 */
	protected FortranView cview;
	
	/**
	 * Constructs a new navigator action group and creates its actions.
	 * 
	 * @param navigator the resource navigator
	 */
	public FortranViewActionGroup(FortranView cview) {
		this.cview = cview;
		makeActions();
	}
	
	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try {
			URL installURL = FortranUIPlugin.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}	

	/**
	 * Returns the resource navigator.
	 */
	public FortranView getCView() {
		return cview;
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action.
	 * Does nothing by default.
 	 */
	public void handleKeyPressed(KeyEvent event) {
	}

	/**
	 * Handles a key released event by invoking the appropriate action.
	 * Does nothing by default.
	 */
	public void handleKeyReleased(KeyEvent event) {
	}

	/**
	 * Makes the actions contained in this action group.
	 */
	protected abstract void makeActions();
	
	/**
	 * Called when the context menu is about to open.
	 * Override to add your own context dependent menu contributions.
	 */
	public abstract void fillContextMenu(IMenuManager menu);

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public abstract void fillActionBars(IActionBars actionBars);

	public abstract void updateActionBars();

	/**
	 * Runs the default action in the group.
	 * Does nothing by default.
	 * 
	 * @param selection the current selection
	 */
	public void runDefaultAction(IStructuredSelection selection) {
	}

	public void restoreFilterAndSorterState(IMemento memento) {
	}
	
	public void saveFilterAndSorterState(IMemento memento) {
	}

}
