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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ptp.internal.rdt.editor.RemoteCEditorHelpResources;
import org.eclipse.ptp.rdt.editor.Activator;
import org.eclipse.swt.custom.StyledTextPrintOptions;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * This is the class for the print preference page
 * 
 * @author batthish
 */
public class PrintPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	protected static final String PRINT_KEY = "com.ibm.tpf.util.print";
	protected static final String FONT_KEY = PRINT_KEY + ".font";
	protected static final String HEADER_KEY = PRINT_KEY + ".header";
	protected static final String FOOTER_KEY = PRINT_KEY + ".footer";
	protected static final String LEFT_KEY = ".left";
	protected static final String RIGHT_KEY = ".right";
	protected static final String CENTER_KEY = ".center";
	protected static final String LINE_NUMBER_KEY = PRINT_KEY+".lineNumbers";

	protected static final String SEPARATOR = StyledTextPrintOptions.SEPARATOR;

	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int CENTER = 2;
	
	public static final String PAGE_ID = "org.eclipse.ptp.rdt.ui.editor.PrintPreferencePage";
	
	protected String SAMPLE = "x=x+1;";

	/**
	 * The constructor for this class
	 */
	public PrintPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		addField(new BooleanFieldEditor(LINE_NUMBER_KEY, PreferenceMessages.LineNumbers, parent));
		addField(new FontFieldEditor(FONT_KEY, PreferenceMessages.Font, SAMPLE, parent));
		addField(new HeaderFooterFieldEditor(parent));
	}

	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control result = super.createContents(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(result, RemoteCEditorHelpResources.PRINT_PREFERENCE_HELP);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/**
	 * Retrieves the font to use for printing
	 * 
	 * @return the font for printing
	 */
	public static Font getSavedFont() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		FontData fd = PreferenceConverter.getFontData(store, FONT_KEY);
		return new Font(Display.getCurrent(), fd);
	}

	/**
	 * Retrieves the header text for the position
	 * 
	 * @param position the position
	 * @return the header text
	 */
	private static String getHeader(int position) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(HEADER_KEY + getPositionKey(position));
	}

	/**
	 * Retrieves the string to use for the header
	 * 
	 * @return the string to use for the header
	 */
	public static String getHeader() {
		return getHeader(LEFT) + SEPARATOR + getHeader(CENTER) + SEPARATOR + getHeader(RIGHT);
	}

	/**
	 * Retrieves the key for the position
	 * 
	 * @param position the position
	 * @return the key
	 */
	private static String getPositionKey(int position) {
		if (position == LEFT)
			return LEFT_KEY;
		else if (position == RIGHT)
			return RIGHT_KEY;
		else
			return CENTER_KEY;
	}

	/**
	 * Retrieves the footer text based on the position
	 * 
	 * @param position the position in the footer
	 * @return the footer text in the position
	 */
	private static String getFooter(int position) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getString(FOOTER_KEY + getPositionKey(position));
	}

	/**
	 * Retrieves the string to use for the footer
	 * 
	 * @return the string to use for the footer
	 */
	public static String getFooter() {
		return getFooter(LEFT) + SEPARATOR + getFooter(CENTER) + SEPARATOR + getFooter(RIGHT);
	}

	/**
	 * Sets the defaults in the store
	 */
	public static void setDefaults() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		PreferenceConverter.setDefault(store, FONT_KEY, JFaceResources.getTextFont().getFontData());
		store.setDefault(HEADER_KEY + LEFT_KEY, HeaderFooterProposalProvider.FILE);
		store.setDefault(HEADER_KEY + RIGHT_KEY, HeaderFooterProposalProvider.DATE+" "+HeaderFooterProposalProvider.TIME);
		store.setDefault(HEADER_KEY + CENTER_KEY, "");
		store.setDefault(FOOTER_KEY + LEFT_KEY, "");
		store.setDefault(FOOTER_KEY + RIGHT_KEY, StyledTextPrintOptions.PAGE_TAG);
		store.setDefault(FOOTER_KEY + CENTER, "");
		store.setDefault(LINE_NUMBER_KEY, false);

	}

	/**
	 * Retrieves whether or not to print line numbers
	 * @return	the line numbers to print		
	 */
	public static boolean getPrintLineNumbers() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return store.getBoolean(LINE_NUMBER_KEY);
	}
}
