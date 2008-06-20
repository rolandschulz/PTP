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
package org.eclipse.photran.core.vpg;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Binder;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ImplicitSpec;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

import bz.over.vpg.VPGDependency;
import bz.over.vpg.VPGEdge;

/**
 * The <code>PhotranVPGBuilder</code> provides methods for manipulating the
 * VPG database; these methods are used only when collecting bindings.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGBuilder extends PhotranVPG
{
	protected PhotranVPGBuilder()
	{
		super();
	}

	public void markFileAsExportingModule(IFile file, String moduleName)
	{
		db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, "module:" + canonicalizeIdentifier(moduleName), getFilenameForIFile(file)));
	}
	
	public void markFileAsImportingModule(IFile file, String moduleName)
	{
	    db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, getFilenameForIFile(file), "module:" + canonicalizeIdentifier(moduleName)));
	}
	
	public void setDefinitionFor(PhotranTokenRef tokenRef, Definition definition)
	{
	    db.setAnnotation(tokenRef, DEFINITION_ANNOTATION_TYPE, definition);
	}
	
	public void markScope(PhotranTokenRef identifier, ScopingNode scope)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, identifier, scope.getRepresentativeToken(), DEFINED_IN_SCOPE_EDGE_TYPE));
	}
	
	public void markBinding(PhotranTokenRef reference, PhotranTokenRef definition)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, reference, definition, BINDING_EDGE_TYPE));
	}
	
	public void markRenamedBinding(PhotranTokenRef reference, PhotranTokenRef definition)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, reference, definition, RENAMED_BINDING_EDGE_TYPE));
	}
	
	public void setScopeImplicitSpec(ScopingNode scope, ImplicitSpec implicitSpec)
	{
		if (implicitSpec != null)
		    db.setAnnotation(scope.getRepresentativeToken(), SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE, implicitSpec);
		else
		    db.deleteAnnotation(scope.getRepresentativeToken(), SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE);
	}
	
	public void setDefaultScopeVisibilityToPrivate(ScopingNode scope)
	{
	    db.setAnnotation(scope.getRepresentativeToken(), SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE, Boolean.TRUE);
	}
    
    @Override
    protected void calculateDependencies(final String filename)
    {
        if (filename.startsWith("module:")) return;
        
        SourceForm sourceForm = determineSourceForm(filename);
        try
        {
            IAccumulatingLexer lexer = LexerFactory.createLexer(getIFileForFilename(filename), sourceForm, false);
            long start = System.currentTimeMillis();
            calculateDependencies(filename, lexer);
            debug("  - Elapsed time in calculateDependencies: " + (System.currentTimeMillis()-start) + " ms", filename);
        }
        catch (Exception e)
        {
            logError(e);
        }
    }

    private SourceForm determineSourceForm(final String filename)
    {
        IFile file = getIFileForFilename(filename);
        IContentType contentType2 = Platform.getContentTypeManager().findContentTypeFor(filename);
        String contentType = contentType2 == null ? null : contentType2.getId();
        // In CDT, String contentType = CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());

        SourceForm sourceForm;
        if (contentType != null && contentType.equals(FIXED_FORM_CONTENT_TYPE))
            sourceForm = SourceForm.FIXED_FORM;
        else
        {
            sourceForm = SourceForm.preprocessedFreeForm(new IncludeLoaderCallback(file.getProject())
            {
                @Override
                public InputStream getIncludedFileAsStream(String fileToInclude) throws FileNotFoundException
                {
                    // When we encounter an INCLUDE directive, set up a file dependency in the VPG
                    
                    db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(PhotranVPGBuilder.this,
                                filename,
                                getFilenameForIFile(getIncludedFile(fileToInclude))));
                    
                    return super.getIncludedFileAsStream(fileToInclude);
                }
            });
        }
        return sourceForm;
    }

    private void calculateDependencies(String filename, IAccumulatingLexer lexer)
    {
        if (!filename.startsWith("module:"))
        {
            db.deleteAllIncomingDependenciesFor(filename);
            db.deleteAllOutgoingDependenciesFor(filename);
        }
        
        if (lexer == null) return;

        try
        {
            IFile file = getIFileForFilename(filename);
            final int WAITING_FOR_TOKEN = 0, LAST_TOKEN_WAS_USE = 1, LAST_TOKEN_WAS_MODULE = 2;
            
            int state = WAITING_FOR_TOKEN;
            for (Token tok = lexer.yylex(); tok != null && tok.getTerminal() != Terminal.END_OF_INPUT; tok = lexer.yylex())
            {
                if (state == WAITING_FOR_TOKEN && tok.getTerminal() == Terminal.T_USE)
                {
                    state = LAST_TOKEN_WAS_USE;
                }
                else if (state == WAITING_FOR_TOKEN && tok.getTerminal() == Terminal.T_MODULE)
                {
                    state = LAST_TOKEN_WAS_MODULE;
                }
                else if (state == LAST_TOKEN_WAS_USE)
                {
                    if (tok.getTerminal() == Terminal.T_IDENT)
                        markFileAsImportingModule(file, tok.getText());
                    state = WAITING_FOR_TOKEN;
                }
                else if (state == LAST_TOKEN_WAS_MODULE)
                {
                    if (tok.getTerminal() == Terminal.T_IDENT)
                        markFileAsExportingModule(file, tok.getText());
                    state = WAITING_FOR_TOKEN;
                }
            }
        }
        catch (Exception e)
        {
            logError(e);
        }
    }
    
    @Override
    protected IFortranAST parse(final String filename)
    {
        if (filename.startsWith("module:")) return null;
        
        IFile file = getIFileForFilename(filename);
        SourceForm sourceForm = determineSourceForm(filename);
        try
        {
            IAccumulatingLexer lexer = LexerFactory.createLexer(file, sourceForm, true);
            long start = System.currentTimeMillis();
            ASTExecutableProgramNode ast = parser.parse(lexer);
            debug("  - Elapsed time in Parser#parse: " + (System.currentTimeMillis()-start) + " ms", filename);
            return new FortranAST(file, ast, lexer.getTokenList());
        }
        catch (Exception e)
        {
            logError(e);
            return null;
        }
    }

    @Override
    protected void populateVPG(String filename, IFortranAST ast)
    {
        if (!filename.startsWith("module:"))
        {
            db.deleteAllIncomingDependenciesFor(filename);
            db.deleteAllOutgoingDependenciesFor(filename);
        }
        
        if (ast == null) return;

        long start = System.currentTimeMillis();
        Binder.bind(ast, getIFileForFilename(filename));
        debug("  - Elapsed time in Binder#bind: " + (System.currentTimeMillis()-start) + " ms", filename);
    }
}
