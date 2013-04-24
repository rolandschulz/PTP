/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.ptp.gig.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GIGPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String LOCAL = Messages.GIG_PREFERENCE_PAGE_1;
	public static final String USERNAME = Messages.USERNAME;
	public static final String PASSWORD = Messages.PASSWORD;
	public static final String GKLEE_HOME = "GKLEE_HOME"; //$NON-NLS-1$
	public static final String FLA_KLEE_HOME_DIR = "FLA_KLEE_HOME_DIR"; //$NON-NLS-1$
	public static final String GKLEE_DEBUG_PLUS_ASSERTS_BIN = "Gklee Debug+Asserts bin"; //$NON-NLS-1$
	public static final String LLVM_DEBUG_PLUS_ASSERTS_BIN = "llvm Debug+Asserts bin"; //$NON-NLS-1$
	public static final String LLVM_GCC_LINUX_BIN = "llvm gcc linux bin"; //$NON-NLS-1$
	public static final String BIN = "bin"; //$NON-NLS-1$
	public static final String ADDITIONAL_PATH = Messages.GIG_PREFERENCE_PAGE_0 + " PATH"; //$NON-NLS-1$

	public GIGPreferencePage() {
		super(GRID);
		setPreferenceStore(GIGPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		final Composite composite = this.getFieldEditorParent();
		composite.setLayout(new GridLayout(1, false));

		final Group topGroup = new Group(composite, SWT.NULL);
		topGroup.setLayout(new GridLayout(1, false));
		final Group localRemoteGroup = new Group(topGroup, SWT.NULL);
		localRemoteGroup.setLayout(new GridLayout(0, false));
		final Group remoteGroup = new Group(localRemoteGroup, SWT.NULL);
		remoteGroup.setLayout(new GridLayout(1, false));
		final Group localGroup = new Group(localRemoteGroup, SWT.NULL);
		localGroup.setLayout(new GridLayout(1, false));
		this.addField(new BooleanFieldEditor(LOCAL, Messages.GIG_PREFERENCE_PAGE_1,
				BooleanFieldEditor.DEFAULT, localRemoteGroup));
		this.addField(new StringFieldEditor(GIGPreferencePage.USERNAME, GIGPreferencePage.USERNAME, remoteGroup));
		this.addField(new StringFieldEditor(GIGPreferencePage.PASSWORD, GIGPreferencePage.PASSWORD, remoteGroup));
		this.addField(new StringFieldEditor(Messages.SERVER_NAME, Messages.SERVER_NAME, remoteGroup));
		this.addField(new StringFieldEditor(Messages.TARGET_PROJECT, Messages.TARGET_PROJECT, remoteGroup));
		this.addField(new StringFieldEditor(GKLEE_HOME, GKLEE_HOME, localGroup));
		this.addField(new StringFieldEditor(FLA_KLEE_HOME_DIR, FLA_KLEE_HOME_DIR, localGroup));
		this.addField(new StringFieldEditor(GKLEE_DEBUG_PLUS_ASSERTS_BIN, GKLEE_DEBUG_PLUS_ASSERTS_BIN, localGroup));
		this.addField(new StringFieldEditor(LLVM_DEBUG_PLUS_ASSERTS_BIN, LLVM_DEBUG_PLUS_ASSERTS_BIN, localGroup));
		this.addField(new StringFieldEditor(LLVM_GCC_LINUX_BIN, LLVM_GCC_LINUX_BIN, localGroup));
		this.addField(new StringFieldEditor(BIN, BIN, localGroup));
		this.addField(new StringFieldEditor(ADDITIONAL_PATH, ADDITIONAL_PATH, localGroup));

		final Group tolerance = new Group(topGroup, SWT.NULL);
		tolerance.setLayout(new GridLayout(1, false));
		this.addField(new BooleanFieldEditor(Messages.BANK_OR_WARP, Messages.BANK_OR_WARP, tolerance));
		this.addField(new IntegerFieldEditor(Messages.BANK_CONFLICT_LOW, Messages.BANK_CONFLICT_LOW, tolerance));
		this.addField(new IntegerFieldEditor(Messages.BANK_CONFLICT_HIGH, Messages.BANK_CONFLICT_HIGH, tolerance));
		this.addField(new IntegerFieldEditor(Messages.MEMORY_COALESCING_LOW, Messages.MEMORY_COALESCING_LOW, tolerance));
		this.addField(new IntegerFieldEditor(Messages.MEMORY_COALESCING_HIGH, Messages.MEMORY_COALESCING_HIGH, tolerance));
		this.addField(new IntegerFieldEditor(Messages.WARP_DIVERGENCE_LOW, Messages.WARP_DIVERGENCE_LOW, tolerance));
		this.addField(new IntegerFieldEditor(Messages.WARP_DIVERGENCE_HIGH, Messages.WARP_DIVERGENCE_HIGH, tolerance));

	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
