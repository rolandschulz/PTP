/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.binding;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.PhotranVPGBuilder;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTPrivateSequenceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;

/**
 * Abstract superclass providing utility methods for the various ASTVisitors
 * that collect bindings in an AST.
 * 
 * @author Jeff Overbey
 */
public abstract class BindingCollector extends ASTVisitor
{
	protected PhotranVPGBuilder vpg = (PhotranVPGBuilder)PhotranVPG.getInstance();

    protected void markModuleExport(IFile file, String moduleName)
    {
        vpg.markFileAsExportingModule(file, moduleName);
    }

    protected void setScopeDefaultVisibilityToPrivate(ScopingNode scope)
    {
        vpg.setDefaultScopeVisibilityToPrivate(scope);
    }
	
    protected void setDefinition(Token ident, Definition def)
    {
        vpg.setDefinitionFor(ident.getTokenRef(), def);
    }

	// addDefinition() and bindRenamedEntity() accept null to support the module loader
	// (its code is cleaner if it doesn't have to special-case nulls)
	
    /**
     * @param definitionIsInOuterScope For example, SubroutineStmt nodes are contained within SubroutineSubprogram nodes.  However,
     * the subroutine they declare is not a member of its own scope: it is a member of the outer scope.  Set this parameter to true
     * in this case.
     */
    Definition addDefinition(Token token, Definition.Classification classification, Type type)
    {
    	if (token == null) return null;
    	
    	try
    	{
            Definition definition = new Definition(token.getTokenRef(), classification, type);
    		vpg.setDefinitionFor(token.getTokenRef(), definition);
    		vpg.markScope(token.getTokenRef(), token.getEnclosingScope());
    		return definition;
    	}
    	catch (Exception e)
    	{
    		throw new Error(e);
    	}
    }

    Definition addDefinition(Token token, Definition.Classification classification)
    {
    	return addDefinition(token, classification, Type.VOID);
    }
    
    void importDefinition(Definition definitionToImport, ScopingNode importIntoScope)
    {
    	try
    	{
    		vpg.markScope(definitionToImport.getTokenRef(), importIntoScope);
    	}
    	catch (Exception e)
    	{
    		throw new Error(e);
    	}
    }

    List<PhotranTokenRef> bind(Token identifier)
    {
	    	List<PhotranTokenRef> result = identifier.manuallyResolveBinding();
	    	
			for (PhotranTokenRef def : result)
	    		bind(identifier, def);
			
			return result;
    }

    List<PhotranTokenRef> bindAsParam(Token identifier)
    {
	    	List<PhotranTokenRef> result = bind(identifier);
	    	
			for (PhotranTokenRef def : result)
			{
	    		Definition d = vpg.getDefinitionFor(def);
	    		d.markAsSubprogramArgument();
	    		vpg.setDefinitionFor(def, d);
			}
			
			return result;
    }

    void bind(Token identifier, PhotranTokenRef toDefinition)
    {
       	try
    	{
       		vpg.markBinding(identifier.getTokenRef(), toDefinition);
    	}
    	catch (Exception e)
    	{
    		throw new Error(e);
    	}
    }
    
    void bindRenamedEntity(Token identifier, PhotranTokenRef toDefinition)
    {
    	if (identifier == null) return;
    	
       	try
    	{
       		vpg.markRenamedBinding(identifier.getTokenRef(), toDefinition);
    	}
    	catch (Exception e)
    	{
    		throw new Error(e);
    	}
    }

    void dontbind(Token identifier)
    {
    	// A derived type component or subroutine parameter name was found;
    	// don't attempt to bind it, since this would require type checking
    }
}
