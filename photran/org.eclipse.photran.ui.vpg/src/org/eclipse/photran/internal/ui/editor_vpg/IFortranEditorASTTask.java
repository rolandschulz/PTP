/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

/**
 * An <code>IFortranEditorASTTask</code> can be run if an AST for the file in the editor is available but the VPG is not
 * up-to-date.
 * <p>
 * The AST provided to this method will be based on the contents of the editor, while the AST provided to an
 * {@link IFortranEditorVPGTask} will be based on the last saved version of the file.
 * 
 * @author Jeff Overbey
 */
public interface IFortranEditorASTTask
{
    /** @return true to run this task again the next time the editor is updated, or false to run it only once */
    boolean handle(ASTExecutableProgramNode ast, TokenList tokenList, DefinitionMap<Definition> defMap);
}
