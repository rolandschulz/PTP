/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.eclipse.cdt.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.CMakeSettingsProvider;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.XMLFileSettingsProvider;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;

/**
 * Options page for {@link XMLFileSettingsProvider}
 */
public class CMakeSettingsProviderOptions extends AbstractLanguageSettingProviderOptionPage {
	Composite parent;
	Text buildDirTextBox;

	@Override
	public void createControl(Composite comp) {
		parent = comp;
		CMakeSettingsProvider provider = (CMakeSettingsProvider) getProvider();
		Composite composite = createCompositeForPageArea(parent);

		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setEnabled(parent.isEnabled());
		label.setText(Messages.CMakeSettingsProviderOptions_0);

		buildDirTextBox = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		buildDirTextBox.setLayoutData(gd);
		buildDirTextBox.setEnabled(parent.isEnabled());

		IPath buildDir = provider.getBuildDir();
		buildDirTextBox.setText(buildDir !=null ? buildDir.toOSString() : ""); //$NON-NLS-1$

		createBrowseButton(composite, buildDirTextBox);

		setControl(composite);
	}

	/**
	 * Create composite for the page.
	 * Copy of org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers.BuiltinSpecsDetectorOptionPage#createCompositeForPageArea
	 */
	private Composite createCompositeForPageArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.marginRight = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Dialog.applyDialogFont(composite);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		composite.setLayoutData(gd);
		return composite;
	}

	/**
	 * Create "Browse" button.
	 * Mostly copied from org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers.BuiltinSpecsDetectorOptionPage#createBrowsButton
	 */
	private void createBrowseButton(Composite composite, final Text textBox) {
		Button button = ControlFactory.createPushButton(composite, Messages.CMakeSettingsProviderOptions_1);
		button.setEnabled(parent.isEnabled());
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
				dialog.setText(Messages.CMakeSettingsProviderOptions_2);
				String dirName = textBox.getText();
				// taking chance that the first word is a path
				int space = dirName.indexOf(' ');
				if (space > 0) {
					dirName = dirName.substring(0, space);
				}
				IPath folder = new Path(dirName).removeLastSegments(1);
				dialog.setFilterPath(folder.toOSString());
				String chosenDir = dialog.open();
				if (chosenDir != null) {
					textBox.insert(chosenDir);
				}
			}
		});
	}
	
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		CMakeSettingsProvider provider = (CMakeSettingsProvider) providerTab.getProvider(providerId);

		IPath buildPathPrev = provider.getBuildDir();
		String buildPathString = buildDirTextBox.getText();
		IPath buildPath = null;
		if (buildPathString.length() > 0) {
			buildPath = new Path(buildPathString);
		}

		if (!equalsWithNulls(buildPathPrev, buildPath)) {
			provider.setBuildDir(buildPath);
			try {
				provider.reset();
			} catch (TransformerException e) {
				RDTSyncUIPlugin.log(Messages.CMakeSettingsProviderOptions_3, e);
			} catch (SAXException e) {
				RDTSyncUIPlugin.log(Messages.CMakeSettingsProviderOptions_3, e);
			} catch (IOException e) {
				RDTSyncUIPlugin.log(Messages.CMakeSettingsProviderOptions_4, e);
			}
		}
		super.performApply(monitor);
	}

	/**
	 * Test if two objects are equal, taking null values into account.
	 * See: http://stackoverflow.com/questions/11011742/java-null-equalsobject-o
	 *
	 * @param a
	 * @param b
	 * @return whether a and b are equal
	 */
	public boolean equalsWithNulls(Object a, Object b) {
		if (a==b) return true;
		if ((a==null)||(b==null)) return false;
		return a.equals(b);
	}
}