/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.internal.ui.util.PixelConverter;
import org.eclipse.fdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.fdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.fdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class CPathOrderExportPage extends CPathBasePage {

	private ListDialogField fCPathList;
	
	public CPathOrderExportPage(ListDialogField cPathList) {
		super(CPathEntryMessages.getString("OrderExportsPage.title")); //$NON-NLS-1$
		setDescription(CPathEntryMessages.getString("OrderExportsPage.description")); //$NON-NLS-1$
		fCPathList = cPathList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.ui.dialogs.AbstractCOptionPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		PixelConverter converter = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);

		LayoutUtil.doDefaultLayout(composite, new DialogField[]{fCPathList}, true);
		LayoutUtil.setHorizontalGrabbing(fCPathList.getListControl(null));

		int buttonBarWidth = converter.convertWidthInCharsToPixels(24);
		fCPathList.setButtonsMinWidth(buttonBarWidth);
	}

	public Image getImage() {
		return FortranPluginImages.get(FortranPluginImages.IMG_OBJS_ORDER);
	}
	/*
	 * @see BuildPathBasePage#getSelection
	 */
	public List getSelection() {
		return fCPathList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */
	public void setSelection(List selElements) {
		fCPathList.selectElements(new StructuredSelection(selElements));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#isEntryKind(int)
	 */
	public boolean isEntryKind(int kind) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.ui.dialogs.AbstractCOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.fdt.ui.dialogs.AbstractCOptionPage#performDefaults()
	 */
	public void performDefaults() {
	}

}