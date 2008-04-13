package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.photran.core.IFortranAST;

/**
 * An <code>IFortranEditorASTTask</code> can be run if an AST for the file in the editor is available but the VPG is not
 * up-to-date.
 * <p>
 * The AST provided to this method will be based on the contents of the editor, while the AST provided to an
 * {@link IFortranEditorVPGTask} will be based on the last saved version of the file.
 * <p>
 * The list of tasks to run is established in {@link OldExperimentalFreeFormFortranEditor#FreeFormVPGEditor()
 * 
 * @author Jeff Overbey
 */
public interface IFortranEditorASTTask
{
    void handle(IFortranAST ast);
}
