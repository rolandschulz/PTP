package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.ITextEditor;

public interface IFortranSourceViewerConfigurationFactory
{
    SourceViewerConfiguration create(AbstractFortranEditor editor);
}
