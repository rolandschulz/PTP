package org.eclipse.photran.internal.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Top-level Fortran preference page, which doesn't show anything
 * 
 * @author joverbey
 */
public class MainFortranPreferencePage
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage
{
    public MainFortranPreferencePage()
    {
        super(FieldEditorPreferencePage.GRID);
        setDescription("");
    }

    protected void createFieldEditors()
    {
    }

    public void init(IWorkbench workbench)
    {
    }
}