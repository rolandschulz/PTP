/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.ptp.internal.rdt.editor.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A field editor for a header and footer on a page.
 * 
 * @author batthish
 */ 
public class HeaderFooterFieldEditor extends FieldEditor {

	private Composite _composite;
	private Text hLeft;
	private Text hCenter;
	private Text hRight;
	private Text fLeft;
	private Text fCenter;
	private Text fRight;
	private Label _spacer;

	/**
	 * The constructor
	 * 
	 * @param parent the parent composite
	 */
	public HeaderFooterFieldEditor(Composite parent) {
		init(PrintPreferencePage.PRINT_KEY, "");
		createControl(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int columns) {
		if (_composite != null) {
			GridData gd = (GridData) _composite.getLayoutData();
			gd.horizontalSpan = columns - 1;
			gd = (GridData) _spacer.getLayoutData();
			gd.horizontalSpan = columns ;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int columns) {

		_spacer = new Label(parent, SWT.NONE);
		GridData spData = new GridData(GridData.FILL_HORIZONTAL);
		spData.horizontalSpan = columns;
		_spacer.setLayoutData(spData);

		new Label(parent, SWT.NONE);

		_composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 4;
		gd.horizontalSpan = columns - 1;
		_composite.setLayoutData(gd);
		GridLayout gl = new GridLayout(3, true);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		_composite.setLayout(gl);

		Label header = new Label(parent, SWT.NONE);
		header.setText(PreferenceMessages.Header);

		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		Label footer = new Label(parent, SWT.NONE);
		footer.setText(PreferenceMessages.Footer);

		Label left = new Label(_composite, SWT.NONE);
		left.setText(PreferenceMessages.Left);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		left.setLayoutData(gd);

		Label center = new Label(_composite, SWT.NONE);
		center.setText(PreferenceMessages.Center);
		center.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		Label right = new Label(_composite, SWT.NONE);
		right.setText(PreferenceMessages.Right);
		right.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

		Composite page = new Composite(_composite, SWT.BORDER);
		gl = new GridLayout(3, true);
		page.setLayout(gl);
		page.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		page.setLayoutData(gd);

		hLeft = new Text(page, SWT.BORDER | SWT.LEFT);
		hLeft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hLeft.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		new HeaderFooterContentProposalAdapter(hLeft);
		hCenter = new Text(page, SWT.BORDER | SWT.CENTER);
		hCenter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hCenter.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		new HeaderFooterContentProposalAdapter(hCenter);
		hRight = new Text(page, SWT.BORDER | SWT.RIGHT);
		hRight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		hRight.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		new HeaderFooterContentProposalAdapter(hRight);

		spacer = new Label(page, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		spacer.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		spacer.setLayoutData(gd);

		fLeft = new Text(page, SWT.BORDER | SWT.LEFT);
		fLeft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fLeft.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		new HeaderFooterContentProposalAdapter(fLeft);
		fCenter = new Text(page, SWT.BORDER | SWT.CENTER);
		fCenter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fCenter.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		new HeaderFooterContentProposalAdapter(fCenter);
		fRight = new Text(page, SWT.BORDER | SWT.RIGHT);
		fRight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fRight.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		new HeaderFooterContentProposalAdapter(fRight);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		IPreferenceStore store = getPreferenceStore();
		if (hRight != null)
			hRight.setText(store.getString(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.RIGHT_KEY));
		if (hCenter != null)
			hCenter.setText(store.getString(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.CENTER_KEY));
		if (hLeft != null)
			hLeft.setText(store.getString(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.LEFT_KEY));
		if (fRight != null)
			fRight.setText(store.getString(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.RIGHT_KEY));
		if (fCenter != null)
			fCenter.setText(store.getString(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.CENTER_KEY));
		if (fLeft != null)
			fLeft.setText(store.getString(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.LEFT_KEY));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		IPreferenceStore store = getPreferenceStore();
		if (hRight != null)
			hRight.setText(store.getDefaultString(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.RIGHT_KEY));
		if (hCenter != null)
			hCenter.setText(store.getDefaultString(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.CENTER_KEY));
		if (hLeft != null)
			hLeft.setText(store.getDefaultString(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.LEFT_KEY));
		if (fRight != null)
			fRight.setText(store.getDefaultString(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.RIGHT_KEY));
		if (fCenter != null)
			fCenter.setText(store.getDefaultString(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.CENTER_KEY));
		if (fLeft != null)
			fLeft.setText(store.getDefaultString(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.LEFT_KEY));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		IPreferenceStore store = getPreferenceStore();
		if (hRight != null)
			store.setValue(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.RIGHT_KEY, hRight.getText());
		if (hCenter != null)
			store.setValue(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.CENTER_KEY, hCenter.getText());
		if (hLeft != null)
			store.setValue(PrintPreferencePage.HEADER_KEY + PrintPreferencePage.LEFT_KEY, hLeft.getText());
		if (fRight != null)
			store.setValue(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.RIGHT_KEY, fRight.getText());
		if (fCenter != null)
			store.setValue(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.CENTER_KEY, fCenter.getText());
		if (fLeft != null)
			store.setValue(PrintPreferencePage.FOOTER_KEY + PrintPreferencePage.LEFT_KEY, fLeft.getText());

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		return 2;
	}

}
