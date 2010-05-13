package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;

/**
 * An <code>IFortranEditorVPGTask</code> can be run only after the VPG has been updated for the file in the editor.
 * <p>
 * The AST provided to this method will be based on the last saved version of the file.  The user may have modified
 * the file since then, so token positions will <i>not</i> necessarily correspond to the contents of the editor.
 * An {@link IFortranEditorASTTask} should be used if accurate token position information is needed.
 * 
 * @author Jeff Overbey
 */
public interface IFortranEditorVPGTask
{
    void handle(IFile file, IFortranAST ast, DefinitionMap<Definition> defMap);
}
