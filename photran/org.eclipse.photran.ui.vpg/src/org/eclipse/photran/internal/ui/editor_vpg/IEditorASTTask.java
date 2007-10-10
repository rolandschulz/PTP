package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

/**
 * An <code>IEditorASTTask</code> can be run if an AST for the file in the editor is available but the VPG is not
 * up-to-date.
 * <p>
 * The AST provided to this method will be based on the contents of the editor, while the AST provided to an
 * {@link IEditorVPGTask} will be based on the last saved version of the file.
 * <p>
 * The list of tasks to run is established in {@link ExperimentalFreeFormFortranEditor#FreeFormVPGEditor()
 * 
 * @author Jeff Overbey
 */
public interface IEditorASTTask
{
    void handle(ASTExecutableProgramNode astRootNode);
}
