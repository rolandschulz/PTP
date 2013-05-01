/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.internal.debug.ui.propertypages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.internal.debug.core.PDebugModel;
import org.eclipse.ptp.internal.debug.core.PDebugUtils;
import org.eclipse.ptp.internal.debug.ui.dialogfields.SelectionButtonDialogField;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author Clement
 */

public class SignalPropertyPage extends PropertyPage {
	private SelectionButtonDialogField fPassButton;
	private SelectionButtonDialogField fStopButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NONE);
		Font font = parent.getFont();
		composite.setFont(font);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create description field
		try {
			String description = getSignal().getDescription();
			Label label = new Label(composite, SWT.WRAP);
			label.setText(NLS.bind(Messages.SignalPropertyPage_0, new Object[] { description }));
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(font);
		} catch (DebugException e1) {
		}

		// Create pass button
		try {
			boolean pass = getSignal().isPassEnabled();
			fPassButton = new SelectionButtonDialogField(SWT.CHECK);
			fPassButton.setLabelText(Messages.SignalPropertyPage_1);
			fPassButton.setSelection(pass);
			fPassButton.setEnabled(getSignal().canModify());
			fPassButton.doFillIntoGrid(composite, 1);
		} catch (DebugException e) {
		}

		// Create stop button
		try {
			boolean stop = getSignal().isStopEnabled();
			fStopButton = new SelectionButtonDialogField(SWT.CHECK);
			fStopButton.setLabelText(Messages.SignalPropertyPage_2);
			fStopButton.setSelection(stop);
			fStopButton.setEnabled(getSignal().canModify());
			fStopButton.doFillIntoGrid(composite, 1);
		} catch (DebugException e) {
		}

		setValid(true);
		return composite;
	}

	protected SelectionButtonDialogField getPassButton() {
		return fPassButton;
	}

	protected SelectionButtonDialogField getStopButton() {
		return fStopButton;
	}

	public IPSignal getSignal() {
		return (IPSignal) getElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			DebugPlugin.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (!getSignal().canModify()) {
						return;
					}
					if (getPassButton() != null) {
						try {
							getSignal().setPassEnabled(getPassButton().isSelected());
						} catch (DebugException e) {
							failed(Messages.SignalPropertyPage_3, e);
						}
					}
					if (getStopButton() != null) {
						try {
							getSignal().setStopEnabled(getStopButton().isSelected());
						} catch (DebugException e) {
							failed(Messages.SignalPropertyPage_4, e);
						}
					}
				}
			});
		}
		return result;
	}

	protected void failed(String message, Throwable e) {
		MultiStatus ms = new MultiStatus(PDebugModel.getPluginIdentifier(), IPDebugConstants.STATUS_CODE_ERROR, message, null);
		ms.add(new Status(IStatus.ERROR, PDebugModel.getPluginIdentifier(), IPDebugConstants.STATUS_CODE_ERROR, e.getMessage(),
				null));
		PDebugUtils.error(ms, getSignal());
	}
}
