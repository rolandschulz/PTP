/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cldt.internal.ui.preferences;

import org.eclipse.cldt.internal.ui.ICHelpContextIds;
import org.eclipse.cldt.internal.ui.text.FortranSourceViewerConfiguration;
import org.eclipse.cldt.internal.ui.text.FortranTextTools;
import org.eclipse.cldt.ui.FortranUIPlugin;
import org.eclipse.cldt.ui.PreferenceConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

/**
 * FortranTemplatePreferencePage
 */
public class FortranTemplatePreferencePage extends TemplatePreferencePage {

	public FortranTemplatePreferencePage() {
		setPreferenceStore(FortranUIPlugin.getDefault().getPreferenceStore());
		setTemplateStore(FortranUIPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(FortranUIPlugin.getDefault().getTemplateContextRegistry());
	}
	
	/*
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.TEMPLATE_PREFERENCE_PAGE);
	}

	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#getFormatterPreferenceKey()
	 */
	protected String getFormatterPreferenceKey() {
		return PreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok= super.performOk();
		FortranUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}	
	
	/*
	 * @see org.eclipse.ui.texteditor.templates.TemplatePreferencePage#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected SourceViewer createViewer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(PreferencesMessages.getString("TemplatePreferencePage.Viewer.preview")); //$NON-NLS-1$
		GridData data= new GridData();
		data.horizontalSpan= 2;
		label.setLayoutData(data);
		
		SourceViewer viewer= new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		FortranTextTools tools= FortranUIPlugin.getDefault().getTextTools();
		viewer.configure(new FortranSourceViewerConfiguration(tools, null));
		viewer.setEditable(false);
		viewer.setDocument(new Document());
		viewer.getTextWidget().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	
		Font font= JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT);
		viewer.getTextWidget().setFont(font);
		
		Control control= viewer.getControl();
		data= new GridData(GridData.FILL_BOTH);
		data.heightHint= convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
	
		control.getAccessible().addAccessibleListener(new AccessibleAdapter() {			
			public void getName(AccessibleEvent e) {
				e.result = PreferencesMessages.getString("TemplatePreferencePage.preview"); //$NON-NLS-1$
		}});
		
		return viewer;
	}

}