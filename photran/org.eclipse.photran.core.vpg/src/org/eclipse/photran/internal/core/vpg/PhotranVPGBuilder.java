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
package org.eclipse.photran.internal.core.vpg;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.FortranAST;
import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.analysis.binding.Binder;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Visibility;
import org.eclipse.photran.internal.core.analysis.binding.ImplicitSpec;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.sourceform.ISourceForm;
import org.eclipse.photran.internal.core.lexer.sourceform.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.rephraserengine.core.vpg.VPGDependency;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;

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

    public void markFileAsExportingSubprogram(IFile file, String subprogramName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, "subprogram:" + canonicalizeIdentifier(subprogramName), getFilenameForIFile(file)));
    }

    public void markFileAsImportingSubprogram(IFile file, String subprogramName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, getFilenameForIFile(file), "subprogram:" + canonicalizeIdentifier(subprogramName)));
    }

    public void markFileAsExportingModule(IFile file, String moduleName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, "module:" + canonicalizeIdentifier(moduleName), getFilenameForIFile(file)));
    }

	public void markFileAsImportingModule(IFile file, String moduleName)
	{
	    db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, getFilenameForIFile(file), "module:" + canonicalizeIdentifier(moduleName)));
	}

    public void markFileAsUsingCommonBlock(IFile file, String commonBlockName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, getFilenameForIFile(file), "common:" + canonicalizeIdentifier(commonBlockName)));
    }

	public void setDefinitionFor(PhotranTokenRef tokenRef, Definition definition)
	{
	    db.setAnnotation(tokenRef, DEFINITION_ANNOTATION_TYPE, definition);
	}

    public void markDefinitionVisibilityInScope(PhotranTokenRef definitionTokenRef, ScopingNode scope, Visibility visibility)
    {
        //System.err.println("Marking " + definitionTokenRef.findToken().getText() + " " + visibility + " in " + scope.getRepresentativeToken().findTokenOrReturnNull());

        VPGEdge<IFortranAST, Token, PhotranTokenRef> privateEdge = new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, definitionTokenRef, scope.getRepresentativeToken(), DEFINITION_IS_PRIVATE_IN_SCOPE_EDGE_TYPE);
        if (visibility.equals(Visibility.PRIVATE))
            db.ensure(privateEdge);
        else
            db.delete(privateEdge);
    }

    public void markScope(PhotranTokenRef identifier, ScopingNode scope)
    {
        db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, identifier, scope.getRepresentativeToken(), DEFINED_IN_SCOPE_EDGE_TYPE));
    }

    public void markIllegalShadowing(PhotranTokenRef shadowingIdent, PhotranTokenRef shadowedIdent)
    {
        db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, shadowingIdent, shadowedIdent, ILLEGAL_SHADOWING_EDGE_TYPE));
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

    public void setModuleSymbolTable(Token moduleNameToken, List<Definition> symbolTable)
    {
        clearModuleSymbolTableEntries(moduleNameToken);

        String filename = "module:" + canonicalizeIdentifier(moduleNameToken.getText());
        PhotranTokenRef tokenRef = createTokenRef(filename, 0, 0);
        db.setAnnotation(tokenRef, MODULE_TOKENREF_ANNOTATION_TYPE, moduleNameToken.getTokenRef());

        int entries = 0;
        for (Definition def : symbolTable)
        {
            tokenRef = createTokenRef(filename, entries++, 0);
            db.setAnnotation(tokenRef, MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE, def);
        }

        tokenRef = createTokenRef(filename, 0, 0);
        db.setAnnotation(tokenRef, MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE, Integer.valueOf(entries));
    }

    private void clearModuleSymbolTableEntries(Token moduleNameToken)
    {
        String canonicalizedModuleName = canonicalizeIdentifier(moduleNameToken.getText());

        moduleSymTabCache.remove(canonicalizedModuleName);

        int entries = countModuleSymbolTableEntries(canonicalizedModuleName);
        if (entries > 0)
        {
            String filename = "module:" + canonicalizedModuleName;

            for (int i = 0; i < entries; i++)
            {
                PhotranTokenRef tokenRef = createTokenRef(filename, i, 0);
                db.deleteAnnotation(tokenRef, MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE);
            }

            PhotranTokenRef tokenRef = createTokenRef(filename, 0, 0);
            db.setAnnotation(tokenRef, MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE, Integer.valueOf(0));
        }
    }

    @Override
    public boolean isVirtualFile(String filename)
    {
        return filename.startsWith("module:") || filename.startsWith("common:") || filename.startsWith("subprogram:");
    }

    @Override public PhotranTokenRef createTokenRef(String filename, int offset, int length)
    {
        return new PhotranTokenRef(filename, offset, length);
    }

    @Override
    protected void calculateDependencies(final String filename)
    {
        if (isVirtualFile(filename)) return;

        ISourceForm sourceForm = determineSourceForm(filename);
        try
        {
            IAccumulatingLexer lexer = new ASTLexerFactory().createLexer(getIFileForFilename(filename), sourceForm);
            long start = System.currentTimeMillis();
            calculateDependencies(filename, lexer);
            debug("  - Elapsed time in calculateDependencies: " + (System.currentTimeMillis()-start) + " ms", filename);
        }
        catch (Exception e)
        {
            log.logError(e, new PhotranTokenRef(filename, 0, 0));
        }
    }

    private ISourceForm determineSourceForm(final String filename)
    {
        IFile file = getIFileForFilename(filename);
        return SourceForm.of(file).configuredWith(new IncludeLoaderCallback(file.getProject())
            {
                @Override
                public Reader getIncludedFileAsStream(String fileToInclude) throws FileNotFoundException
                {
                    // When we encounter an INCLUDE directive, set up a file dependency in the VPG

                    db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(PhotranVPGBuilder.this,
                                filename,
                                getFilenameForIFile(getIncludedFile(fileToInclude))));

                    return super.getIncludedFileAsStream(fileToInclude);
                }

                @Override
                public void logError(String message, IFile topLevelFile, int offset)
                {
                    PhotranVPG.getInstance().log.clearEntriesFor(PhotranVPG.getFilenameForIFile(topLevelFile));
                    PhotranVPG.getInstance().log.logError(message, new PhotranTokenRef(topLevelFile, offset, 0));
                }
            });
    }

    private void calculateDependencies(String filename, IAccumulatingLexer lexer)
    {
        if (!isVirtualFile(filename))
        {
            db.deleteAllIncomingDependenciesFor(filename);
            db.deleteAllOutgoingDependenciesFor(filename);
        }

        if (lexer == null) return;

        try
        {
            calculateFileDepsFromModuleAndUseStmts(filename, lexer);
        }
        catch (Exception e)
        {
            log.logError(e);
        }
    }

    /**
     * Calculates what modules are imported or exported by a particular file and marks these dependencies in the VPG.
     *
     * Since the dependency calculation will be run on every file in the workspace, it is essential that this method
     * be fast (while remaining accurate).  This implementation uses the lexer to find the token sequences "USE x"
     * and "MODULE x", which is equivalent to but significantly faster than parsing the file and traversing the AST
     * to find USE and MODULE statements.
     */
    private void calculateFileDepsFromModuleAndUseStmts(String filename, IAccumulatingLexer lexer) throws Exception
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

    @Override
    protected void doCommitChangeFromAST(String filename)
    {
        if (isVirtualFile(filename)) return;

        IFortranAST ast = acquireTransientAST(filename);
        if (ast == null)
            throw new IllegalArgumentException(filename + " returned null AST");
        
        try
        {
            final String charset = Charset.defaultCharset().name(); // Platform's default charset
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            ast.getRoot().printOn(new PrintStream(out, false, charset), null);
            ast = parse(filename, new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), charset));

            computeEdgesAndAnnotations(filename, ast);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Error(e);
        }
    }

    @Override public String getSourceCodeFromAST(IFortranAST ast)
    {
        return ast.getRoot().toString();
    }

    @Override
    protected IFortranAST parse(final String filename)
    {
        return parse(filename, null);
    }

    private IFortranAST parse(final String filename, Reader stream)
    {
        if (filename == null || isVirtualFile(filename)) return null;

        IFile file = getIFileForFilename(filename); if (file == null) return null;
        try
        {
            ISourceForm sourceForm = determineSourceForm(filename);
            try
            {
                if (stream == null) stream = new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset()));
                IAccumulatingLexer lexer = new ASTLexerFactory().createLexer(stream, file, filename, sourceForm);
                long start = System.currentTimeMillis();
                ASTExecutableProgramNode ast = parser.parse(lexer);
                debug("  - Elapsed time in Parser#parse: " + (System.currentTimeMillis()-start) + " ms", filename);
                return new FortranAST(file, ast, lexer.getTokenList());
            }
            catch (SyntaxException e)
            {
                if (e.getFile() != null && e.getFile().getIFile() != null)
                {
                    log.clearEntriesFor(PhotranVPG.getFilenameForIFile(e.getFile().getIFile()));
                    log.logError("Error parsing " + filename + ": " + e.getMessage(),
                        new PhotranTokenRef(e.getFile().getIFile(), e.getTokenOffset(), e.getTokenLength()));
                }
                else
                    logError(file, "Error parsing " + filename, e);
                return null;
            }
            catch (LexerException e)
            {
                if (e.getFile() != null && e.getFile().getIFile() != null)
                {
                    log.clearEntriesFor(PhotranVPG.getFilenameForIFile(e.getFile().getIFile()));
                    log.logError("Error parsing " + filename + ": " + e.getMessage(),
                        new PhotranTokenRef(e.getFile().getIFile(), e.getTokenOffset(), e.getTokenLength()));
                }
                else
                    logError(file, "Error parsing " + filename, e);
                return null;
            }
    //        catch (CoreException e)
    //        {
    //            IFile errorFile = getFileFromStatus(e.getStatus());
    //            if (errorFile != null)
    //                log.logError("Error parsing " + filename + ": " + e.getMessage(),
    //                    new PhotranTokenRef(errorFile, 0, 0));
    //            else
    //                logError(file, "Error parsing " + filename, e);
    //            return null;
    //        }
            catch (Throwable e)
            {
                logError(file, "Error parsing " + filename, e);
                return null;
            }
        }
        finally
        {
            try
            {
                if (stream != null) stream.close();
            }
            catch (Throwable x)
            {
                // Ignore
            }
        }
    }

    private void logError(IFile file, String message, Throwable e)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(": ");
        sb.append(e.getMessage());
        sb.append('\n');
        sb.append(e.getClass().getName());
        sb.append(":\n");
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bs));
        sb.append(bs);

        if (file != null)
            log.logError(sb.toString(), new PhotranTokenRef(file, 0, 0));
        else
            log.logError(sb.toString());
    }

    @Override
    protected void populateVPG(String filename, IFortranAST ast)
    {
        if (!isVirtualFile(filename))
        {
            db.deleteAllIncomingDependenciesFor(filename);
            db.deleteAllOutgoingDependenciesFor(filename);
        }

        if (ast == null || isEmpty(ast.getRoot())) return;

        long start = System.currentTimeMillis();
        Binder.bind(ast, getIFileForFilename(filename));
        debug("  - Elapsed time in Binder#bind: " + (System.currentTimeMillis()-start) + " ms", filename);
    }

    public static boolean isEmpty(ASTExecutableProgramNode ast)
    {
        return ast.findFirstToken() == null;
    }
}
