/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.actions;

import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.actions.RebuildIndexAction;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.core.index.IndexBuildSequenceController;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;



public class RemoteRebuildIndexAction implements IObjectActionDelegate {
	private ISelection fSelection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
	public void run(IAction action) {
		IStructuredSelection cElements= SelectionConverter.convertSelectionToCElements(fSelection);
		for (Iterator<?> i = cElements.iterator(); i.hasNext();) {
			Object elem = i.next();
			if (elem instanceof ICProject) {
				if(RemoteNature.hasRemoteNature(((ICProject)elem).getProject())){
					IndexBuildSequenceController indexBuildSequenceController = IndexBuildSequenceController.getIndexBuildSequenceController(((ICProject)elem).getProject());
					
					Shell shell = new Shell();
					
					if(indexBuildSequenceController.isOptionalIndex()){
						boolean continueIndex = MessageDialog.openQuestion(shell, Messages.getString("RemoteRebuildIndexOption.question"), Messages.getString("RemoteRebuildIndexOption.message")); //$NON-NLS-1$ //$NON-NLS-2$
				        if(continueIndex){
				        	indexBuildSequenceController.setIndexRunning();
				        	CCorePlugin.getIndexManager().reindex((ICProject) elem);
				        }
					}else{
						CCorePlugin.getIndexManager().reindex((ICProject) elem);
					}
				}else{
					CCorePlugin.getIndexManager().reindex((ICProject) elem);
				}
			}
		}
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
	}

}
