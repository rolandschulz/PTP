/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.mpi.core.Messages;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for MPI settings
 * <br>
 * Note that "Prompt to include MPI APIs found in other locations" is only applicable
 * to C code, not C++: because C++ MPI APIs are not as easily recognizable - when
 * discovered via CDT AST, they sometimes do not have the "MPI" prefix.
 * C++ MPI APIs include e.g. MPI_Bcast, MPI::Init, and Get_size (coded as "MPI::COMM_WORLD.Get_size()")
 * thus we don't cull by excluding all APIs for C++ code that don't start with a prefix.
 * That said, since we do this, we end up looking at all variables and functions including
 * those found only in user code, e.g. argc and argv.  If we asked the user about all of these,
 * it would be annoying and too slow.
 * <p>This should probably be re-evaluated in the future.
 * An alternative might be a "don't ask me again during this run" checkbox
 * on the popup asking to add the API, that appears during execution.
 * That would remove the annoying part; but performance may still suffer from this extra test?
 * <p>Crude workaround: Run MPI analysis at least once on plain C code, to assure it's finding the
 * header files in the same place that are specified here in the preferences.
 * 
 */

public class MPIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    private static final String MPI_INCLUDES_PREFERENCE_LABEL  = Messages.getString("MPIPreferencePage.mpiIncludePaths"); //$NON-NLS-1$
    private static final String MPI_INCLUDES_PREFERENCE_BROWSE = Messages.getString("MPIPreferencePage.pleaseChooseAdirectory"); //$NON-NLS-1$
    private static final String MPI_BUILD_COMMAND_LABEL = Messages.getString("MPIPreferencePage.mpiBuildCommand"); //$NON-NLS-1$
    private static final String MPI_CPP_BUILD_COMMAND_LABEL = Messages.getString("MPIPreferencePage.mpiCppBuildCommand"); //$NON-NLS-1$
    private static final String PROMPT_FOR_OTHERS_LABEL=Messages.getString("MPIPreferencePage.promptToIncludeOtherLocations");

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
    }

    protected void createFieldEditors()
    {
        PathEditor pathEditor = new PathEditor(MpiIDs.MPI_INCLUDES, MPI_INCLUDES_PREFERENCE_LABEL,
                MPI_INCLUDES_PREFERENCE_BROWSE, getFieldEditorParent());
        addField(pathEditor);
        
        StringFieldEditor sed = new StringFieldEditor(MpiIDs.MPI_BUILD_CMD, MPI_BUILD_COMMAND_LABEL,getFieldEditorParent());
        addField(sed);
        
        StringFieldEditor sedCpp = new StringFieldEditor(MpiIDs.MPI_CPP_BUILD_CMD, MPI_CPP_BUILD_COMMAND_LABEL,getFieldEditorParent());
        addField(sedCpp);
        
        BooleanFieldEditor bed = new BooleanFieldEditor(MpiIDs.MPI_PROMPT_FOR_OTHER_INCLUDES, PROMPT_FOR_OTHERS_LABEL, getFieldEditorParent());
        addField(bed);
        
    }
}
