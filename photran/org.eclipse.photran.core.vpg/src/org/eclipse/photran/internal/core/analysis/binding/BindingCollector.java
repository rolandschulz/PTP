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
import org.eclipse.photran.internal.core.analysis.binding.Definition.Visibility;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.vpg.PhotranVPGWriter;

/**
 * Abstract superclass providing utility methods for several visitor classes
 * that collect bindings in an AST.  See subclasses and {@link Binder}.
 * 
 * @author Jeff Overbey
 * @see Binder
 */
public abstract class BindingCollector extends ASTVisitorWithLoops
{
    protected PhotranVPG vpg = PhotranVPG.getInstance();
	protected PhotranVPGWriter vpgProvider = PhotranVPG.getProvider();

    protected void markSubprogramExport(IFile file, Token subprogramName)
    {
        vpgProvider.markFileAsExportingSubprogram(file, subprogramName.getText());
    }

    protected void markSubprogramImport(IFile file, Token subprogramName)
    {
        vpgProvider.markFileAsImportingSubprogram(file, subprogramName.getText());
    }

    protected void markModuleExport(IFile file, Token moduleName)
    {
        vpgProvider.markFileAsExportingModule(file, moduleName.getText());
    }

    protected void setScopeDefaultVisibilityToPrivate(ScopingNode scope)
    {
        vpgProvider.setDefaultScopeVisibilityToPrivate(scope);
    }
	
    protected void setDefinition(Token ident, Definition def)
    {
        vpgProvider.setDefinitionFor(ident.getTokenRef(), def);
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
    	    ScopingNode enclosingScope = token.getEnclosingScope();
    	    
            Visibility visibility = enclosingScope.isDefaultVisibilityPrivate() ? Visibility.PRIVATE : Visibility.PUBLIC;
            Definition definition = new Definition(token.getText(), token.getTokenRef(), classification, /*visibility,*/ type);
    		vpgProvider.setDefinitionFor(token.getTokenRef(), definition);
    		vpgProvider.markScope(token.getTokenRef(), enclosingScope);
    		vpgProvider.markDefinitionVisibilityInScope(token.getTokenRef(), enclosingScope, visibility);
    		
    		checkForIllegalShadowing(token.getText(), token.getTokenRef(), enclosingScope);
    		
    		return definition;
    	}
    	catch (Exception e)
    	{
    		throw new Error(e);
    	}
    }

    protected void checkForIllegalShadowing(String name, PhotranTokenRef tokenRef, ScopingNode enclosingScope)
    {
        if (enclosingScope.isMainProgram() || enclosingScope.isSubprogram() || enclosingScope.isModule())
        {
            // Force resolution in case this is a post-transform analysis and the scope has been renamed
        	String scopeName = enclosingScope.getName();
            if (scopeName != null &&
        	    PhotranVPG.canonicalizeIdentifier(name).equals(
        	        PhotranVPG.canonicalizeIdentifier(scopeName)))
        	{
        	    vpgProvider.markIllegalShadowing(tokenRef, enclosingScope.getNameToken().getTokenRef());
        	}
        }
    }
    
//    Definition addDefinitionAndDuplicateInLocalScope(Token name, Classification classification, Type type)
//    {
//        Definition result = addDefinition(name, classification, type);
//        // addDefinition called vpg.markScope for the enclosing scope
//        
//        ScopingNode localScope = name.findNearestAncestor(ScopingNode.class);
//        vpg.markScope(name.getTokenRef(), localScope);
//        
//        return result;
//    }

    Definition addDefinition(Token token, Definition.Classification classification)
    {
    	return addDefinition(token, classification, Type.VOID);
    }
    
    void importDefinition(Definition definitionToImport, ScopingNode importIntoScope)
    {
    	try
    	{
    		vpgProvider.markScope(definitionToImport.getTokenRef(), importIntoScope);
    		
    		if (importIntoScope.isDefaultVisibilityPrivate())
    		    vpgProvider.markDefinitionVisibilityInScope(definitionToImport.getTokenRef(), importIntoScope, Visibility.PRIVATE);
    		
    		checkForIllegalShadowing(definitionToImport.getCanonicalizedName(), definitionToImport.getTokenRef(), importIntoScope);
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

    List<PhotranTokenRef> bindNoImplicits(Token identifier)
    {
            List<PhotranTokenRef> result = identifier.manuallyResolveBindingNoImplicits();
            
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
	    		vpgProvider.setDefinitionFor(def, d);
			}
			
			return result;
    }

    void bind(Token identifier, PhotranTokenRef toDefinition)
    {
       	try
    	{
       		vpgProvider.markBinding(identifier.getTokenRef(), toDefinition);
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
       		vpgProvider.markRenamedBinding(identifier.getTokenRef(), toDefinition);
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
