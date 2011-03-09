/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.actions;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ConfigurationChoiceDialog;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.ConfigUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.ui.views.ResourceManagerView;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class OpenResourceManagerEditor implements IViewActionDelegate, IJAXBUINonNLSConstants {

	private ResourceManagerView view;

	public void init(IViewPart view) {
		this.view = (ResourceManagerView) view;
	}

	public void run(IAction action) {
		ConfigurationChoiceDialog dialog = new ConfigurationChoiceDialog(view, view.getState());
		if (dialog.open() == Window.CANCEL) {
			return;
		}

		String selected = dialog.getChoice();
		if (ZEROSTR.equals(selected)) {
			return;
		}

		try {
			File fileToOpen = null;
			if (dialog.isPreset()) {
				fileToOpen = ConfigUtils.exportResource(selected, view.getSite().getShell());
				if (fileToOpen == null) {
					return;
				}
			} else {
				fileToOpen = new File(selected);
			}

			if (fileToOpen.exists() && fileToOpen.isFile()) {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IDE.openEditorOnFileStore(page, fileStore);
			}
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(view.getSite().getShell(), t, Messages.OpenResourceManagerEditorAction_error,
					Messages.OpenResourceManagerEditorAction_title, false);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
