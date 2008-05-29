/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.prefs;



import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.lapi.LapiIDs;
import org.eclipse.ptp.pldt.lapi.LapiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Preference page based on FieldEditorPreferencePage
 * 
 * @author xue
 */

public class LAPIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    private static final String INCLUDES_PREFERENCE_LABEL  = "LAPI include paths:";
    private static final String INCLUDES_PREFERENCE_BROWSE = "Please choose a directory for LAPI includes:";
    private static final String LAPI_HELP="Location of LAPI help files:";
    private static final String LAPI_HELP_DEFAULT="Use default";
    private static final String LAPI_HELP_DEFAULT_ID="lapiHelpUseDefault";
    private static final String LAPI_HELP_LINUX="Use Linux location: ";
    private static final String LAPI_HELP_AIX="Use AIX location: ";
    private static final String LAPI_HELP_OTHER="Other:";
    private static final String LAPI_HELP_OTHER_ID="lapiHelpOther";
    
    private static final String LAPI_LOCATION_AIX="/opt/rsct/lapi/eclipse/help";
    private static final String LAPI_LOCATION_LINUX="opt/ibmhpc/lapi/eclipse/help";
    
    private static final String LAPI_WHICH_HELP_ID="default";  // alternatives are: default, aix, linux, other

    public LAPIPreferencePage()
    {
        super(FLAT);
        initPreferenceStore();
    }

    public LAPIPreferencePage(int style)
    {
        super(style);
        initPreferenceStore();
    }

    public LAPIPreferencePage(String title, ImageDescriptor image, int style)
    {
        super(title, image, style);
        initPreferenceStore();
    }

    public LAPIPreferencePage(String title, int style)
    {
        super(title, style);
        initPreferenceStore();
    }

    /**
     * Init preference store and set the preference store for the preference page
     */
    private void initPreferenceStore()
    {
        IPreferenceStore store = LapiPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    public void init(IWorkbench workbench)
    {
    }

    protected void createFieldEditors()
    {
        PathEditor pathEditor = new PathEditor(LapiIDs.LAPI_INCLUDES, INCLUDES_PREFERENCE_LABEL,
                INCLUDES_PREFERENCE_BROWSE, getFieldEditorParent());
        addField(pathEditor);

        //"Use default?"
//        BooleanFieldEditor bed = new BooleanFieldEditor(LAPI_HELP_DEFAULT_ID,LAPI_HELP_DEFAULT,getFieldEditorParent());
//        addField(bed);
  /*      
        int numCol=1;
    	RadioGroupFieldEditor choiceFE = new RadioGroupFieldEditor(LAPI_WHICH_HELP_ID, LAPI_HELP, numCol, new String[][] {
				{ LAPI_HELP_DEFAULT, "choice1" }, 
				{ LAPI_HELP_AIX+LAPI_LOCATION_AIX, "choice2" }, 
				{ LAPI_HELP_LINUX+LAPI_LOCATION_LINUX, "Choice3" },
				{ LAPI_HELP_OTHER, "Choice4" }},
				getFieldEditorParent());
    	addField(choiceFE);
    	
    	StringFieldEditor otherLoc=new StringFieldEditor(LAPI_HELP_OTHER_ID, LAPI_HELP_OTHER,getFieldEditorParent());
    	addField(otherLoc);
    	*/
    	
    }
    
}
