package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.ui.texteditor.ITextEditor;

public interface IFortranReconcilerFactory
{
    IReconciler create(ITextEditor editor);
}
