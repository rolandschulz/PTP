package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.source.SourceViewerConfiguration;

public interface IFortranSourceViewerConfigurationFactory
{
    SourceViewerConfiguration create(FortranEditor editor);
}
