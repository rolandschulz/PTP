/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.actions;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.dialogs.ConfigurationChoiceDialog;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.ConfigUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetUtils;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class OpenResourceManagerEditor implements IViewActionDelegate, IJAXBNonNLSConstants {

	private IViewPart part;

	public void init(IViewPart view) {
		this.part = view;
	}

	public void run(IAction action) {
		ConfigurationChoiceDialog dialog = new ConfigurationChoiceDialog(part.getSite().getShell(), null);
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
				fileToOpen = ConfigUtils.exportResource(selected, part.getSite().getShell());
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
			WidgetUtils.errorMessage(part.getSite().getShell(), t, Messages.OpenResourceManagerEditorAction_error,
					Messages.OpenResourceManagerEditorAction_title, false);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
