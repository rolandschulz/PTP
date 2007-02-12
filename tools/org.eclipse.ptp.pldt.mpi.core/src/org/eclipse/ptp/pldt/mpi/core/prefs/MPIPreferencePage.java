/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page based on MPIPreferencePage
 * 
 */

public class MPIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    private static final String MPI_INCLUDES_PREFERENCE_LABEL  = "MPI include paths:";
    private static final String MPI_INCLUDES_PREFERENCE_BROWSE = "Please choose a directory:";
    private static final String MPI_BUILD_COMMAND_LABEL = "MPI build command:";

    public MPIPreferencePage()
    {
        super(FLAT);
        initPreferenceStore();
    }

    public MPIPreferencePage(int style)
    {
        super(style);
        initPreferenceStore();
    }

    public MPIPreferencePage(String title, ImageDescriptor image, int style)
    {
        super(title, image, style);
        initPreferenceStore();
    }

    public MPIPreferencePage(String title, int style)
    {
        super(title, style);
        initPreferenceStore();
    }

    /**
     * Init preference store and set the preference store for the preference page
     */
    private void initPreferenceStore()
    {
        IPreferenceStore store = MpiPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    public void init(IWorkbench workbench)
    {
    	IPreferenceStore store = MpiPlugin.getDefault().getPreferenceStore();
    	store.setDefault(MpiIDs.MPI_BUILD_CMD, "mpicc");
    }

    protected void createFieldEditors()
    {
        PathEditor pathEditor = new PathEditor(MpiIDs.MPI_INCLUDES, MPI_INCLUDES_PREFERENCE_LABEL,
                MPI_INCLUDES_PREFERENCE_BROWSE, getFieldEditorParent());
        addField(pathEditor);
        
        StringFieldEditor sed = new StringFieldEditor(MpiIDs.MPI_BUILD_CMD, MPI_BUILD_COMMAND_LABEL,getFieldEditorParent());
        addField(sed);
    }
}
