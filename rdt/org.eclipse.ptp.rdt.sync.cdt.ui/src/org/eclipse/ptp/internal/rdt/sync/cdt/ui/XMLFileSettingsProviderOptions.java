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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.xml.sax.SAXException;

/**
 * Options page for {@link XMLFileSettingsProvider}
 */
public class XMLFileSettingsProviderOptions extends AbstractLanguageSettingProviderOptionPage {
	Composite parent;
	Text XMLFileNameTextBox;
	Text XSLTFileNameTextBox;

	@Override
	public void createControl(Composite comp) {
		parent = comp;
		XMLFileSettingsProvider provider = (XMLFileSettingsProvider) getProvider();
		Composite composite = createCompositeForPageArea(parent);

		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		label.setEnabled(parent.isEnabled());
		label.setText(Messages.XMLFileSettingsProviderOptions_0);

		XMLFileNameTextBox = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		XMLFileNameTextBox.setLayoutData(gd);
		XMLFileNameTextBox.setEnabled(parent.isEnabled());

		IPath XMLFileName = provider.getXMLFile();
		XMLFileNameTextBox.setText(XMLFileName !=null ? XMLFileName.toOSString() : ""); //$NON-NLS-1$

		createBrowseButton(composite, XMLFileNameTextBox);

		final Button enableXSLTButton = new Button(composite, SWT.CHECK);
		gd = new GridData();
		gd.horizontalSpan = 2;
		enableXSLTButton.setLayoutData(gd);
		enableXSLTButton.setEnabled(parent.isEnabled());
		enableXSLTButton.setText(Messages.XMLFileSettingsProviderOptions_1);
		enableXSLTButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				XSLTFileNameTextBox.setEnabled(enableXSLTButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		XSLTFileNameTextBox = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		XSLTFileNameTextBox.setLayoutData(gd);

		IPath XSLTFileName = provider.getXSLTFile();
		XSLTFileNameTextBox.setText(XMLFileName !=null ? XMLFileName.toOSString() : ""); //$NON-NLS-1$
		XSLTFileNameTextBox.setEnabled((XSLTFileName != null && parent.isEnabled()) ? true : false);
		enableXSLTButton.setSelection(XSLTFileNameTextBox.isEnabled());

		createBrowseButton(composite, XSLTFileNameTextBox);


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
		Button button = ControlFactory.createPushButton(composite, Messages.XMLFileSettingsProviderOptions_2);
		button.setEnabled(parent.isEnabled());
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(Messages.XMLFileSettingsProviderOptions_3);
				String fileName = textBox.getText();
				// taking chance that the first word is a compiler path
				int space = fileName.indexOf(' ');
				if (space > 0) {
					fileName = fileName.substring(0, space);
				}
				IPath folder = new Path(fileName).removeLastSegments(1);
				dialog.setFilterPath(folder.toOSString());
				String chosenFile = dialog.open();
				if (chosenFile != null) {
					textBox.insert(chosenFile);
				}
			}
		});
	}
	
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		XMLFileSettingsProvider provider = (XMLFileSettingsProvider) providerTab.getProvider(providerId);

		String XSLTFileName = XSLTFileNameTextBox.getText();
		if (XSLTFileName.length() == 0) {
			provider.setXSLTFile(null);
		} else {
			provider.setXSLTFile(new Path(XSLTFileName));
		}

		IPath XMLFilePathPrev = provider.getXMLFile();
		String XMLFileName = XMLFileNameTextBox.getText();
		IPath XMLFilePath = null;
		if (XMLFileName.length() > 0) {
			XMLFilePath = new Path(XMLFileName);
		}

		if (!equalsWithNulls(XMLFilePathPrev, XMLFilePath)) {
			provider.setXMLFile(XMLFilePath);
			try {
				provider.reset();
			} catch (TransformerException e) {
				RDTSyncUIPlugin.log(Messages.XMLFileSettingsProviderOptions_4, e);
			} catch (SAXException e) {
				RDTSyncUIPlugin.log(Messages.XMLFileSettingsProviderOptions_4, e);
			} catch (IOException e) {
				RDTSyncUIPlugin.log(Messages.XMLFileSettingsProviderOptions_5, e);
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