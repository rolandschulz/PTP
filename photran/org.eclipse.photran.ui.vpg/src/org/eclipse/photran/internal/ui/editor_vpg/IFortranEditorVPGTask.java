package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;

/**
 * An <code>IFortranEditorVPGTask</code> can be run only after the VPG has been updated for the file in the editor.
 * <p>
 * The AST provided to this method will be based on the last saved version of the file.  The user may have modified
 * the file since then, so token positions will <i>not</i> necessarily correspond to the contents of the editor.
 * An {@link IFortranEditorASTTask} should be used if accurate token position information is needed.
 * <p>
 * The list of tasks to run is established in {@link OldExperimentalFreeFormFortranEditor#FreeFormVPGEditor()
 * 
 * @author Jeff Overbey
 */
public interface IFortranEditorVPGTask
{
    void handle(IFile file, IFortranAST ast);
}
