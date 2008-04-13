package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor.IFortranReconcilerFactory;
import org.eclipse.ui.texteditor.ITextEditor;

public class FortranVPGReconcilerFactory implements IFortranReconcilerFactory
{
    public IReconciler create(ITextEditor editor)
    {
        if (editor instanceof AbstractFortranEditor)
            return new CReconciler((AbstractFortranEditor)editor,
                                   new FortranVPGReconcilingStrategy((AbstractFortranEditor)editor));
        else
            return null;
    }
}
