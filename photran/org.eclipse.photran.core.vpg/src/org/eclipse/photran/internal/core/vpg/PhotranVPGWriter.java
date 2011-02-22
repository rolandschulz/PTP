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
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.FortranAST;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.analysis.binding.Binder;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Visibility;
import org.eclipse.photran.internal.core.analysis.binding.ImplicitSpec;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.binding.VariableAccess;
import org.eclipse.photran.internal.core.analysis.flow.ControlFlowAnalysis;
import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.lexer.FixedFormReplacement;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.sourceform.ISourceForm;
import org.eclipse.photran.internal.core.sourceform.SourceForm;
import org.eclipse.photran.internal.core.util.LRUCache;
import org.eclipse.rephraserengine.core.vpg.ILazyVPGPopulator;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGDependency;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.VPGWriter;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPGWriter;

/**
 * Photran's {@link VPGWriter}; provides methods for manipulating the VPG database.
 * <p>
 * This class includes a number of Photran-specific methods that are used only when collecting name
 * bindings and writing them to the VPG database.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGWriter extends EclipseVPGWriter<IFortranAST, Token, PhotranTokenRef>
{
	protected PhotranVPGWriter(VPGDB<IFortranAST, Token, PhotranTokenRef> db, VPGLog<Token, PhotranTokenRef> log)
	{
	    super(db, log);
	}

    public void markFileAsExportingSubprogram(IFile file, String subprogramName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>("subprogram:" + canonicalizeIdentifier(subprogramName), getFilenameForIFile(file))); //$NON-NLS-1$
    }

    public void markFileAsImportingSubprogram(IFile file, String subprogramName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(getFilenameForIFile(file), "subprogram:" + canonicalizeIdentifier(subprogramName))); //$NON-NLS-1$
    }

    public void markFileAsExportingModule(IFile file, String moduleName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>("module:" + canonicalizeIdentifier(moduleName), getFilenameForIFile(file))); //$NON-NLS-1$
    }

	public void markFileAsImportingModule(IFile file, String moduleName)
	{
	    db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(getFilenameForIFile(file), "module:" + canonicalizeIdentifier(moduleName))); //$NON-NLS-1$
	}

    public void markFileAsUsingCommonBlock(IFile file, String commonBlockName)
    {
        db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(getFilenameForIFile(file), "common:" + canonicalizeIdentifier(commonBlockName))); //$NON-NLS-1$
    }

	public void setDefinitionFor(PhotranTokenRef tokenRef, Definition definition)
	{
	    db.setAnnotation(tokenRef, AnnotationType.DEFINITION_ANNOTATION_TYPE, definition);
	}

    public void markDefinitionVisibilityInScope(PhotranTokenRef definitionTokenRef, ScopingNode scope, Visibility visibility)
    {
        //System.err.println("Marking " + definitionTokenRef.findToken().getText() + " " + visibility + " in " + scope.getRepresentativeToken().findTokenOrReturnNull());

        VPGEdge<IFortranAST, Token, PhotranTokenRef> privateEdge = new VPGEdge<IFortranAST, Token, PhotranTokenRef>(definitionTokenRef, scope.getRepresentativeToken(), EdgeType.DEFINITION_IS_PRIVATE_IN_SCOPE_EDGE_TYPE);
        if (visibility.equals(Visibility.PRIVATE))
            db.ensure(privateEdge);
        else
            db.delete(privateEdge);
    }

    public void markScope(PhotranTokenRef identifier, ScopingNode scope)
    {
        db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(identifier, scope.getRepresentativeToken(), EdgeType.DEFINED_IN_SCOPE_EDGE_TYPE));
    }

    public void markIllegalShadowing(PhotranTokenRef shadowingIdent, PhotranTokenRef shadowedIdent)
    {
        db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(shadowingIdent, shadowedIdent, EdgeType.ILLEGAL_SHADOWING_EDGE_TYPE));
    }

	public void markBinding(PhotranTokenRef reference, PhotranTokenRef definition)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(reference, definition, EdgeType.BINDING_EDGE_TYPE));
	}

	public void markRenamedBinding(PhotranTokenRef reference, PhotranTokenRef definition)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(reference, definition, EdgeType.RENAMED_BINDING_EDGE_TYPE));
	}

	public void setScopeImplicitSpec(ScopingNode scope, ImplicitSpec implicitSpec)
	{
		if (implicitSpec != null)
		    db.setAnnotation(scope.getRepresentativeToken(), AnnotationType.SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE, implicitSpec);
		else
		    db.deleteAnnotation(scope.getRepresentativeToken(), AnnotationType.SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE);
	}

    public void setDefaultScopeVisibilityToPrivate(ScopingNode scope)
    {
        db.setAnnotation(scope.getRepresentativeToken(), AnnotationType.SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE, Boolean.TRUE);
    }

    public void setModuleSymbolTable(Token moduleNameToken, List<Definition> symbolTable)
    {
        PhotranVPG vpg = PhotranVPG.getInstance();
        
        clearModuleSymbolTableEntries(moduleNameToken);

        String filename = "module:" + canonicalizeIdentifier(moduleNameToken.getText()); //$NON-NLS-1$
        PhotranTokenRef tokenRef = vpg.getVPGNode(filename, 0, 0);
        db.setAnnotation(tokenRef, AnnotationType.MODULE_TOKENREF_ANNOTATION_TYPE, moduleNameToken.getTokenRef());

        int entries = 0;
        for (Definition def : symbolTable)
        {
            tokenRef = vpg.getVPGNode(filename, entries++, 0);
            db.setAnnotation(tokenRef, AnnotationType.MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE, def);
        }

        tokenRef = vpg.getVPGNode(filename, 0, 0);
        db.setAnnotation(tokenRef, AnnotationType.MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE, Integer.valueOf(entries));
    }

    protected LRUCache<String, List<Definition>> moduleSymTabCache = new LRUCache<String, List<Definition>>(MODULE_SYMTAB_CACHE_SIZE);
    protected long moduleSymTabCacheHits = 0L, moduleSymTabCacheMisses = 0L;

    public List<Definition> getModuleSymbolTable(String moduleName)
    {
        if (moduleSymTabCache.contains(moduleName))
        {
            moduleSymTabCacheHits++;
            return moduleSymTabCache.get(moduleName);
        }
        moduleSymTabCacheMisses++;

        int entries = countModuleSymbolTableEntries(moduleName);

        if (entries == 0) return new LinkedList<Definition>();

        String filename = "module:" + canonicalizeIdentifier(moduleName); //$NON-NLS-1$
        ArrayList<Definition> result = new ArrayList<Definition>(entries);
        for (int i = 0; i < entries; i++)
        {
            PhotranTokenRef tokenRef = PhotranVPG.getInstance().getVPGNode(filename, i, 0);
            Object entry = db.getAnnotation(tokenRef, AnnotationType.MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE);
            if (entry != null && entry instanceof Definition)
                result.add((Definition)entry);
        }

        moduleSymTabCache.cache(moduleName, result);

        return result;
    }

    protected int countModuleSymbolTableEntries(String canonicalizedModuleName)
    {
        String filename = "module:" + canonicalizedModuleName; //$NON-NLS-1$
        PhotranTokenRef tokenRef = PhotranVPG.getInstance().getVPGNode(filename, 0, 0);
        Object result = db.getAnnotation(tokenRef, AnnotationType.MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE);
        return result == null || !(result instanceof Integer) ? 0 : ((Integer)result).intValue();
    }

    private void clearModuleSymbolTableEntries(Token moduleNameToken)
    {
        PhotranVPG vpg = PhotranVPG.getInstance();
        
        String canonicalizedModuleName = canonicalizeIdentifier(moduleNameToken.getText());

        moduleSymTabCache.remove(canonicalizedModuleName);

        int entries = countModuleSymbolTableEntries(canonicalizedModuleName);
        if (entries > 0)
        {
            String filename = "module:" + canonicalizedModuleName; //$NON-NLS-1$

            for (int i = 0; i < entries; i++)
            {
                PhotranTokenRef tokenRef = vpg.getVPGNode(filename, i, 0);
                db.deleteAnnotation(tokenRef, AnnotationType.MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE);
            }

            PhotranTokenRef tokenRef = vpg.getVPGNode(filename, 0, 0);
            db.setAnnotation(tokenRef, AnnotationType.MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE, Integer.valueOf(0));
        }
    }

    public void printModuleSymTabCacheStatisticsOn(PrintStream out)
    {
        out.println("Module Symbol Table Cache Statistics:"); //$NON-NLS-1$

        long edgeTotal = moduleSymTabCacheHits + moduleSymTabCacheMisses;
        float edgeHitRatio = edgeTotal == 0 ? 0 : ((float)moduleSymTabCacheHits) / edgeTotal * 100;
        out.println("    Hit Ratio:        " + moduleSymTabCacheHits + "/" + edgeTotal + " (" + (long)Math.round(edgeHitRatio) + "%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public void resetStatistics()
    {
        moduleSymTabCacheHits = moduleSymTabCacheMisses = 0L;
    }

    @Override
    public void computeDependencies(final String filename)
    {
        if (PhotranVPG.getInstance().isVirtualFile(filename)) return;

        computeDependenciesUsingFastTokenizer(filename); //computeDependenciesUsingLexer(filename);
    }

    /** @see org.eclipse.photran.managedbuilder.core.makegen.DefaultFortranDependencyCalculator */
    private void computeDependenciesUsingFastTokenizer(String filename)
    {
        try {
            IFile file = getIFileForFilename(filename);
            if (file == null) return;
            
            Reader r = new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset()));
            StreamTokenizer st = new StreamTokenizer(r);
            st.commentChar('!');
            st.eolIsSignificant(false);
            st.slashSlashComments(false);
            st.slashStarComments(false);
            st.wordChars('_', '_');
            
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                if (st.ttype == StreamTokenizer.TT_WORD) {
                    if (st.sval.equalsIgnoreCase("module")) { //$NON-NLS-1$
                        st.nextToken();
                        if (st.ttype == StreamTokenizer.TT_WORD) {
                            markFileAsExportingModule(file, st.sval);
                        } else {
                            st.pushBack();
                        }
                    }
                    else if (st.sval.equalsIgnoreCase("use")) { //$NON-NLS-1$
                        st.nextToken();
                        if (st.ttype == StreamTokenizer.TT_WORD) {
                            markFileAsImportingModule(file, st.sval);
                        } else {
                            st.pushBack();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            FortranCorePlugin.log(e);
        }
    }

    private void computeDependenciesUsingLexer(final String filename)
    {
        ISourceForm sourceForm = determineSourceForm(filename);
        try
        {
            IAccumulatingLexer lexer = new ASTLexerFactory().createLexer(getIFileForFilename(filename), sourceForm);
            long start = System.currentTimeMillis();
            calculateDependencies(filename, lexer);
            PhotranVPG.getInstance().debug("  - Elapsed time in calculateDependencies: " + (System.currentTimeMillis()-start) + " ms", filename); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (Exception e)
        {
            log.logError(e, new PhotranTokenRef(filename, 0, 0));
        }
    }

    private ISourceForm determineSourceForm(final String filename)
    {
        IFile file = getIFileForFilename(filename);
        if (file == null)
            return SourceForm.of(filename);
        else
            return SourceForm.of(file).configuredWith(new IncludeLoaderCallback(file.getProject())
            {
                @Override
                public Reader getIncludedFileAsStream(String fileToInclude) throws FileNotFoundException
                {
                    // When we encounter an INCLUDE directive, set up a file dependency in the VPG

                    db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(
                                filename,
                                getFilenameForIFile(getIncludedFile(fileToInclude))));

                    return super.getIncludedFileAsStream(fileToInclude);
                }

                @Override
                public void logError(String message, IFile topLevelFile, int offset)
                {
                    PhotranVPG.getInstance().getLog().clearEntriesFor(PhotranVPG.getFilenameForIFile(topLevelFile));
                    PhotranVPG.getInstance().getLog().logError(message, new PhotranTokenRef(topLevelFile, offset, 0));
                }
            });
    }

    private void calculateDependencies(String filename, IAccumulatingLexer lexer)
    {
        if (!PhotranVPG.getInstance().isVirtualFile(filename))
        {
            db.deleteAllIncomingDependenciesFor(filename);
            db.deleteAllOutgoingDependenciesFor(filename);
        }

        if (lexer == null) return;

        try
        {
            calculateFileDepsFromModuleAndUseStmts(filename, lexer);
        }
        catch (LexerException e)
        {
            if (e.getFile() != null && e.getFile().getIFile() != null)
                filename = PhotranVPG.getFilenameForIFile(e.getFile().getIFile());

            log.logError(
                e.getMessage(),
                new PhotranTokenRef(filename, e.getTokenOffset(), e.getTokenLength()));

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
    public void computeEdgesAndAnnotationsFromModifiedAST(String filename, IFortranAST ast)
    {
        if (ast == null)
            throw new IllegalArgumentException(filename + " returned null AST"); //$NON-NLS-1$
        
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

    public IFortranAST parse(final String filename)
    {
        return parse(filename, null);
    }

    private IFortranAST parse(String filename, Reader stream)
    {
        if (filename == null || PhotranVPG.getInstance().isVirtualFile(filename)) return null;

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
                checkForErrors(ast, filename);
                PhotranVPG.getInstance().debug("  - Elapsed time in Parser#parse: " + (System.currentTimeMillis()-start) + " ms", filename); //$NON-NLS-1$ //$NON-NLS-2$
                return new FortranAST(file, ast, lexer.getTokenList());
            }
            catch (SyntaxException e)
            {
                if (e.getFile() != null && e.getFile().getIFile() != null)
                    filename = PhotranVPG.getFilenameForIFile(e.getFile().getIFile());

                log.clearEntriesFor(PhotranVPG.getFilenameForIFile(e.getFile().getIFile()));
                log.logError(
                    Messages.bind(
                        Messages.PhotranVPGBuilder_ErrorParsingFileMessage,
                        filename,
                        e.getMessage()),
                    new PhotranTokenRef(filename, e.getTokenOffset(), e.getTokenLength()));
//              else
//                  logError(file, Messages.bind(Messages.PhotranVPGBuilder_ErrorParsingFile, filename), e);
                return null;
            }
            catch (LexerException e)
            {
                if (e.getFile() != null && e.getFile().getIFile() != null)
                    filename = PhotranVPG.getFilenameForIFile(e.getFile().getIFile());

                log.clearEntriesFor(filename);
                log.logError(
                    Messages.bind(
                        Messages.PhotranVPGBuilder_ErrorParsingFileMessage,
                        filename,
                        e.getMessage()),
                    new PhotranTokenRef(filename, e.getTokenOffset(), e.getTokenLength()));
//              }
//              else
//                  logError(file, Messages.bind(Messages.PhotranVPGBuilder_ErrorParsingFile, filename), e);
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
                logError(file, Messages.bind(Messages.PhotranVPGBuilder_ErrorParsingFile, filename), e);
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

    private void checkForErrors(ASTExecutableProgramNode ast, String filename)
    {
        ASTNodeWithErrorRecoverySymbols firstError = PhotranVPG.findFirstErrorIn(ast);
        if (firstError != null)
        {
            PhotranTokenRef errorTokenRef = getErrorTokenRef(filename, firstError.getErrorToken());
            log.clearEntriesFor(filename);
            log.logError(
                Messages.bind(
                    Messages.PhotranVPGBuilder_FileContainsSyntaxErrors,
                    filename),
                errorTokenRef);
        }
    }

    private PhotranTokenRef getErrorTokenRef(String filename, Token errorToken)
    {
        if (isPreprocessed(errorToken))
            return null;
        else
            // errorToken will have its filename set to null, so we must construct a TokenRef manually
            return new PhotranTokenRef(filename,
                                       errorToken.getStreamOffset(),
                                       errorToken.getLength());
    }

    private boolean isPreprocessed(Token token)
    {
        return token.getPreprocessorDirective() != null
            && !(token.getPreprocessorDirective() instanceof FixedFormReplacement);
    }

    private void logError(IFile file, String message, Throwable e)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(": "); //$NON-NLS-1$
        sb.append(e.getMessage());
        sb.append('\n');
        sb.append(e.getClass().getName());
        sb.append(":\n"); //$NON-NLS-1$
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bs));
        sb.append(bs);

        if (file != null)
            log.logError(sb.toString(), new PhotranTokenRef(file, 0, 0));
        else
            log.logError(sb.toString());
    }

    @Override
    public void populateVPG(String filename, IFortranAST ast)
    {
        if (!PhotranVPG.getInstance().isVirtualFile(filename))
        {
            db.deleteAllIncomingDependenciesFor(filename);
            db.deleteAllOutgoingDependenciesFor(filename);
        }

        if (ast == null || isEmpty(ast.getRoot())) return;

        long start = System.currentTimeMillis();
        Binder.bind(ast, getIFileForFilename(filename));
        PhotranVPG.getInstance().debug("  - Elapsed time in Binder#bind: " + (System.currentTimeMillis()-start) + " ms", filename); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override public ILazyVPGPopulator[] getLazyEdgePopulators()
    {
        return new ILazyVPGPopulator[]
        {
            //new BindingPopulator(),
            new ControlFlowPopulator(),
        };
    }

    private static class ControlFlowPopulator implements ILazyVPGPopulator
    {
        public void populateVPG(String filename)
        {
            IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(filename);
            if (ast == null || isEmpty(ast.getRoot())) return;
            
            LoopReplacer.replaceAllLoopsIn(ast.getRoot());
            long start = System.currentTimeMillis();
            ControlFlowAnalysis.analyze(filename, ast.getRoot());
            PhotranVPG.getInstance().debug("  - Elapsed time in ControlFlowAnalysis#analyze: " + (System.currentTimeMillis()-start) + " ms", filename); //$NON-NLS-1$ //$NON-NLS-2$
        }

        public boolean dependentFilesMustBePopulated()
        {
            return false;
        }
        
        public int[] edgeTypesPopulated()
        {
            return new int[] { EdgeType.CONTROL_FLOW_EDGE_TYPE.ordinal() };
        }
        
        public int[] annotationTypesPopulated()
        {
            return new int[0];
        }
    }
    
//    private static class BindingPopulator implements ILazyVPGPopulator
//    {
//        public void populateVPG(String filename)
//        {
//            IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(filename);
//            if (ast == null || isEmpty(ast.getRoot())) return;
//            
//            long start = System.currentTimeMillis();
//            Binder.bindLazy(ast, getIFileForFilename(filename));
//            PhotranVPG.getInstance().debug("  - Elapsed time in Binder#bindLazy: " + (System.currentTimeMillis() - start) + " ms", filename); //$NON-NLS-1$ //$NON-NLS-2$
//        }
//
//        public boolean dependentFilesMustBePopulated()
//        {
//            return true;
//        }
//        
//        public int[] edgeTypesPopulated()
//        {
//            return new int[]
//            {
//                // ReferenceCollector - name bindings (references)
//                EdgeType.BINDING_EDGE_TYPE.ordinal(),
//                
//                // ReferenceCollector - implicit declarations
//                EdgeType.DEFINED_IN_SCOPE_EDGE_TYPE.ordinal(),
//                EdgeType.DEFINITION_IS_PRIVATE_IN_SCOPE_EDGE_TYPE.ordinal()
//            };
//        }
//        
//        public int[] annotationTypesPopulated()
//        {
//            return new int[]
//            {                  
//                // ModuleLoader
//                AnnotationType.MODULE_TOKENREF_ANNOTATION_TYPE.ordinal(),
//                AnnotationType.MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE.ordinal(),
//                AnnotationType.MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE.ordinal(),
//                
//                // ReferenceCollector - variable accesses
//                AnnotationType.VARIABLE_ACCESS_ANNOTATION_TYPE.ordinal(),
//                
//                // ReferenceCollector - implicit declarations
//                AnnotationType.DEFINITION_ANNOTATION_TYPE.ordinal(),
//            };
//        }
//    }
    
    public static boolean isEmpty(ASTExecutableProgramNode ast)
    {
        return ast.findFirstToken() == null;
    }

    public void logError(String message)
    {
        log.logError(message);
    }
    
    public void logError(String message, PhotranTokenRef tokenRef)
    {
        log.logError(message, tokenRef);
    }



//    @Override public void debug(String message, String filename)
//    {
//    }








    // Tested empirically on ibeam-cpp-mod: 5 does better than 3, but 10 does not do better than 5
    private static final int MODULE_SYMTAB_CACHE_SIZE = 5;


    protected Parser parser = new Parser();


    public String describeEdgeType(int edgeType)
    {
        return EdgeType.values()[edgeType].toString();
    }

    public String describeAnnotationType(int annotationType)
    {
        return AnnotationType.values()[annotationType].toString();
    }

    protected String describeToken(String filename, int offset, int length)
    {
        try
        {
            if (offset == -1 && length == 0) return Messages.PhotranVPG_GlobalScope;

            Token token = PhotranVPG.getInstance().acquireTransientAST(filename).findTokenByStreamOffsetLength(offset, length);
            if (token == null)
                return db.describeToken(filename, offset, length);
            else
                return token.getText() + " " + Messages.bind(Messages.PhotranVPG_OffsetN, offset); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            return db.describeToken(filename, offset, length);
        }
    }

    public static String canonicalizeIdentifier(String identifier)
    {
        return PhotranVPG.canonicalizeIdentifier(identifier);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // VPG Error/Warning Log View/Listener Support
    ////////////////////////////////////////////////////////////////////////////////

    private List<IMarker> errorLogMarkers = null;

    /**
     * It is the caller's responsibility to make sure this task is executed in the
     * with the correct scheduling rule.  (The VPG Problems view locks the entire
     * workspace; the CVS plug-in was having problems when marker attributes were
     * being set on resources that were not locked by the scheduling rule.)
     */
    public List<IMarker> recomputeErrorLogMarkers()
    {
        deleteExistingErrorMarkers();
        populateErrorLogMarkers();
        return errorLogMarkers;
    }

    private void deleteExistingErrorMarkers()
    {
        if (errorLogMarkers != null)
        {
            for (IMarker marker : errorLogMarkers)
            {
                try
                {
                    marker.delete();
                }
                catch (CoreException e)
                {
                    e.printStackTrace();
                }
            }

            errorLogMarkers = null;
        }
    }

    private void populateErrorLogMarkers()
    {
        List<VPGLog<Token, PhotranTokenRef>.Entry> errorLog = log.getEntries();
        errorLogMarkers = new ArrayList<IMarker>(errorLog.size());
        for (int i = 0; i < errorLog.size(); i++)
        {
            try
            {
                VPGLog<Token, PhotranTokenRef>.Entry entry = errorLog.get(i);
                errorLogMarkers.add(createMarkerFrom(entry));
            }
            catch (CoreException e)
            {
                // Ignore
            }
        }
    }

    private IMarker createMarkerFrom(VPGLog<Token, PhotranTokenRef>.Entry entry) throws CoreException
    {
        IMarker marker = createMarkerOnResource(entry);
        if (marker != null) setMarkerAttributes(marker, entry);
        return marker;
    }

    private IMarker createMarkerOnResource(VPGLog<Token, PhotranTokenRef>.Entry entry) throws CoreException
    {
        PhotranTokenRef tr = entry.getTokenRef();
        IFile file = tr == null ? null : tr.getFile();
        IResource res = file == null ? ResourcesPlugin.getWorkspace().getRoot() : file;
        return res.createMarker(determineMarkerType(entry));
    }

    private String determineMarkerType(VPGLog<Token, PhotranTokenRef>.Entry entry)
    {
        if (entry.isWarning())
            return "org.eclipse.photran.core.vpg.warningMarker"; //$NON-NLS-1$
        else // (entry.isError())
            return "org.eclipse.photran.core.vpg.errorMarker"; //$NON-NLS-1$
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setMarkerAttributes(IMarker marker, VPGLog<Token, PhotranTokenRef>.Entry entry) throws CoreException
    {
        Map attribs = new HashMap(5);

        PhotranTokenRef tr = entry.getTokenRef();
        if (tr != null)
        {
            attribs.put(IMarker.CHAR_START, tr.getOffset());
            attribs.put(IMarker.CHAR_END, tr.getEndOffset());
        }

        attribs.put(IMarker.MESSAGE, entry.getMessage());
        attribs.put(IMarker.USER_EDITABLE, false);
        attribs.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);

        marker.setAttributes(attribs);
    }

    private boolean isDefinitionCachingEnabled = false;
    public void enableDefinitionCaching() { isDefinitionCachingEnabled = true; }
    public void disableDefinitionCaching() { isDefinitionCachingEnabled = false; }
    public boolean isDefinitionCachingEnabled() { return isDefinitionCachingEnabled; }




    public void markAccess(Token ident, VariableAccess access)
    {
        db.setAnnotation(
            ident.getTokenRef(),
            AnnotationType.VARIABLE_ACCESS_ANNOTATION_TYPE,
            access);
    }

    public void createFlow(PhotranTokenRef from, PhotranTokenRef to)
    {
        db.ensure(
            new VPGEdge<IFortranAST, Token, PhotranTokenRef>(
                from, to, EdgeType.CONTROL_FLOW_EDGE_TYPE));
    }
}