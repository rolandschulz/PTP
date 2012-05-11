/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem.prefs;



import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.openshmem.Activator;
import org.eclipse.ptp.pldt.openshmem.OpenSHMEMIDs;
import org.eclipse.ptp.pldt.openshmem.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Preference page based on FieldEditorPreferencePage
 * 
 * @author xue
 */

public class OpenSHMEMPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    private static final String INCLUDES_PREFERENCE_LABEL  = Messages.OpenSHMEMPreferencePage_includes_preference_label;
    private static final String INCLUDES_PREFERENCE_BROWSE = Messages.OpenSHMEMPreferencePage_includes_preference_browse_dialog_title;
    private static final String OpenSHMEM_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL = Messages.OpenSHMEMPreferencePage_recognizeAPISByPrefixAlone;
    
    public OpenSHMEMPreferencePage()
    {
        super(FLAT);
        initPreferenceStore();
    }

    public OpenSHMEMPreferencePage(int style)
    {
        super(style);
        initPreferenceStore();
    }

    public OpenSHMEMPreferencePage(String title, ImageDescriptor image, int style)
    {
        super(title, image, style);
        initPreferenceStore();
    }

    public OpenSHMEMPreferencePage(String title, int style)
    {
        super(title, style);
        initPreferenceStore();
    }

    /**
     * Init preference store and set the preference store for the preference page
     */
    private void initPreferenceStore()
    {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    public void init(IWorkbench workbench)
    {
    }

    protected void createFieldEditors()
    {
        BooleanFieldEditor bPrefix = new BooleanFieldEditor(OpenSHMEMIDs.OpenSHMEM_RECOGNIZE_APIS_BY_PREFIX_ALONE, OpenSHMEM_RECOGNIZE_APIS_BY_PREFIX_ALONE_LABEL, getFieldEditorParent());
        addField(bPrefix);
        
        PathEditor pathEditor = new PathEditor(OpenSHMEMIDs.OpenSHMEM_INCLUDES, INCLUDES_PREFERENCE_LABEL,
                INCLUDES_PREFERENCE_BROWSE, getFieldEditorParent());
        addField(pathEditor);

    	
    }
    
}

