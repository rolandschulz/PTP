package org.eclipse.fdt.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.fdt.core.FortranCorePlugin;
import org.eclipse.fdt.internal.ui.ICHelpContextIds;
import org.eclipse.fdt.ui.CUIPlugin;
import org.eclipse.fdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The page for setting c plugin preferences.
 */
public class CPluginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	

	private static final String LINK_TO_EDITOR_LABEL= "CBasePreferencePage.linkToEditor.label"; //$NON-NLS-1$
	private static final String USE_STRUCTURAL_PARSE_MODE_LABEL= "CBasePreferencePage.OutlineView.structuralParseMode.label"; //$NON-NLS-1$
	
	public CPluginPreferencePage() {
		super(GRID);
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
	}
	
	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), ICHelpContextIds.C_PREF_PAGE);
	}	

	/**
	 * @see FieldEditorPreferencePage#createControl(Composite)
	 */	
	protected void createFieldEditors() {
		Composite parent= getFieldEditorParent();

		BooleanFieldEditor linkEditor= new BooleanFieldEditor(PreferenceConstants.PREF_LINK_TO_EDITOR, CUIPlugin.getResourceString(LINK_TO_EDITOR_LABEL), parent);
		addField(linkEditor);

		
		BooleanFieldEditor useStructuralParseMode= new BooleanFieldEditor(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE, CUIPlugin.getResourceString(USE_STRUCTURAL_PARSE_MODE_LABEL), parent);
		addField(useStructuralParseMode);		
	}
	

	public static boolean isLinkToEditor() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_LINK_TO_EDITOR);
	}

	public static void setLinkingEnabled(boolean enable) {
		CUIPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.PREF_LINK_TO_EDITOR, enable);
	}

	public static boolean useStructuralParseMode() {
		return CUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE);
	}
	
	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
		CUIPlugin.getDefault().getPreferenceStore().setValue(FortranCorePlugin.PREF_USE_STRUCTURAL_PARSE_MODE, FortranCorePlugin.getDefault().useStructuralParseMode());
	}
	
	/**
	 * Initializes the default values of this page in the preference bundle.
	 */
	public static void initDefaults(IPreferenceStore prefs) {
		prefs.setDefault(PreferenceConstants.PREF_LINK_TO_EDITOR, false);
		// The field is under Appearance page/preference
		prefs.setDefault(PreferenceConstants.PREF_SHOW_CU_CHILDREN, true);
		prefs.setDefault(PreferenceConstants.PREF_USE_STRUCTURAL_PARSE_MODE, FortranCorePlugin.getDefault().useStructuralParseMode());
		prefs.setDefault(PreferenceConstants.EDITOR_SHOW_SEGMENTS, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (!super.performOk())
			return false;
		// tell the Core Plugin about this preference
		FortranCorePlugin.getDefault().setStructuralParseMode(useStructuralParseMode());
		return true;
	}

}
