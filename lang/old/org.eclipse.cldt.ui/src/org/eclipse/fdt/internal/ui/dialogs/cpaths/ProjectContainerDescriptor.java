/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.internal.ui.dialogs.cpaths;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.fdt.core.model.IPathEntry;
import org.eclipse.fdt.ui.CUIPlugin;
import org.eclipse.fdt.ui.wizards.IPathEntryContainerPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ide.IDE;

public class ProjectContainerDescriptor implements IContainerDescriptor {
	private int[] fFilterType;
	
	public ProjectContainerDescriptor(int[] filterType) {
		fFilterType = filterType;
	}

	public IPathEntryContainerPage createPage() throws CoreException {
		return new ProjectContainerPage(fFilterType);
	}

	public String getName() {
		return CPathEntryMessages.getString("ProjectContainer.label"); //$NON-NLS-1$
	}
	
	public Image getImage() {
		return CUIPlugin.getDefault().getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);

	}

	public boolean canEdit(IPathEntry entry) {
		return false;
	}

	
}
