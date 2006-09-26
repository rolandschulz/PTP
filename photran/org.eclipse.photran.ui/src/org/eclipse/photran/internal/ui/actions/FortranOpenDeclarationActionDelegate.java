package org.eclipse.photran.internal.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.swt.widgets.Display;

public class FortranOpenDeclarationActionDelegate extends FortranEditorActionDelegate
{
    public FortranOpenDeclarationActionDelegate()
    {
        super();
    }

    public FortranOpenDeclarationActionDelegate(AbstractFortranEditor ed)
    {
        super(ed);
    }

    public void run()
    {
        //IFortranAST ast = parseCurrentDocument();
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                MessageDialog.openInformation(null, "Wow!", "Open Declaration");
            }
        });
    }
}
