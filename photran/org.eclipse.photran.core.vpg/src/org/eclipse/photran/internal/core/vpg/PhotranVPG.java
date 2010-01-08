package org.eclipse.photran.internal.core.vpg;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.cdtinterface.natures.ProjectNatures;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Visibility;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTExternalNameListNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.IProgramUnit;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.core.util.LRUCache;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

/**
 * Photran's Virtual Program Graph.
 *
 * @author Jeff Overbey
 */
public abstract class PhotranVPG extends EclipseVPG<IFortranAST, Token, PhotranTokenRef, PhotranVPGDB, PhotranVPGLog>
{
	// Tested empirically on ibeam-cpp-mod: 5 does better than 3, but 10 does not do better than 5
    private static final int MODULE_SYMTAB_CACHE_SIZE = 5;

    // Copied from FortranCorePlugin to avoid dependencies on the Photran Core plug-in
	// (since our parser declares classes with the same name)
    private static final String FIXED_FORM_CONTENT_TYPE = "org.eclipse.photran.core.fixedFormFortranSource";
    private static final String FREE_FORM_CONTENT_TYPE = "org.eclipse.photran.core.freeFormFortranSource";

	public static final int DEFINED_IN_SCOPE_EDGE_TYPE = 0;
	//public static final int IMPORTED_INTO_SCOPE_EDGE_TYPE = 1;
	public static final int BINDING_EDGE_TYPE = 2;
    public static final int RENAMED_BINDING_EDGE_TYPE = 3;
    public static final int DEFINITION_IS_PRIVATE_IN_SCOPE_EDGE_TYPE = 4;
    public static final int ILLEGAL_SHADOWING_EDGE_TYPE = 5;
    private static final String[] edgeTypeDescriptions =
	{
	    "Definition-scope relationship",
	    "Definition-scope relationship due to module import",
	    "Name binding",
	    "Renamed binding",
	    "Definition is private in scope",
	    "Illegal shadowing",
	};

	public static final int SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE = 0;
	public static final int SCOPE_IS_INTERNAL_ANNOTATION_TYPE = 1;
	public static final int SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE = 2;
	public static final int DEFINITION_ANNOTATION_TYPE = 3;
    public static final int TYPE_ANNOTATION_TYPE = 4;
    public static final int MODULE_TOKENREF_ANNOTATION_TYPE = 5;
    public static final int MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE = 6;
    public static final int MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE = 7;
	private static final String[] annotationTypeDescriptions =
	{
	    "Default visibility for scope is private",
        "Scope is internal",
        "Implicit spec for scope",
	    "Definition",
	    "Type",
	    "Module TokenRef",
	    "Module symbol table entry count",
	    "Module symbol table entry",
	};

	private static PhotranVPG instance = null;
	public PhotranVPGDB db = null;

	protected Parser parser = new Parser();

	public static PhotranVPG getInstance()
	{
		if (instance == null)
	    {
            if (/*inTestingMode() ||*/ FortranPreferences.ENABLE_VPG_LOGGING.getValue())
            {
    		    instance = new PhotranVPGBuilder()
        		{
        		    @Override public void debug(String message, String filename)
        		    {
        		        System.out.println(message + " - " + lastSegmentOfFilename(filename));
        		    }
        		};
            }
            else
            {
                instance = new PhotranVPGBuilder();
            }
	    }
		return instance;
	}

    public static PhotranVPGDB getDatabase()
    {
        return getInstance().db;
    }

    @Override public void debug(String message, String filename)
    {
    }

    @Override protected void debug(long parseTimeMillisec,
                                   long computeEdgesAndAnnotationsMillisec,
                                   String filename)
    {
//        printDebug("- "
//                   + parseTimeMillisec
//                   + "/"
//                   + computeEdgesAndAnnotationsMillisec
//                   + " ms parsing/analysis", filename);

//        // Print a stack trace, filtered to elements in VPG and Photran
//        try
//        {
//            throw new Exception();
//        }
//        catch (Exception e)
//        {
//            StackTraceElement[] st = e.getStackTrace();
//            String lastLine = "";
//            for (int i = 1; i < st.length; i++)
//            {
//                String result = st[i].toString();
//                if (result.equals(lastLine))
//                    continue;
//                else if (result.startsWith("bz.over.vpg") || result.startsWith("org.eclipse.photran"))
//                {
//                    System.out.println("      " + result);
//                    lastLine = result;
//                }
//                else break;
//            }
//        }
    }

    @Override public void start()
	{
		if (!inTestingMode()) super.start();
	}

	protected PhotranVPG()
	{
        super(new PhotranVPGLog(), new PhotranVPGDB(), "Photran indexer", 2);
        db = super.db;
    }

    public static boolean inTestingMode()
	{
		return System.getenv("TESTING") != null;
	}

    @Override
	public String describeEdgeType(int edgeType)
	{
		return edgeTypeDescriptions[edgeType];
	}

	@Override
	public String describeAnnotationType(int annotationType)
	{
		return annotationTypeDescriptions[annotationType];
	}

	protected String describeToken(String filename, int offset, int length)
	{
		try
		{
			if (offset == -1 && length == 0) return "global scope";

			Token token = acquireTransientAST(filename).findTokenByStreamOffsetLength(offset, length);
			if (token == null)
				return db.describeToken(filename, offset, length);
			else
				return token.getText() + " (offset " + offset + ")";
		}
		catch (Exception e)
		{
			return db.describeToken(filename, offset, length);
		}
	}

	@Override
	public Token findToken(PhotranTokenRef tokenRef)
	{
		IFortranAST ast = acquireTransientAST(tokenRef.getFilename());
		if (ast == null)
			return null;
		else
			return ast.findTokenByFileOffsetLength(getIFileForFilename(tokenRef.getFilename()), tokenRef.getOffset(), tokenRef.getLength());
	}

    @Override
    public boolean shouldProcessFile(IFile file)
    {
        String filename = file.getName();
        return hasFixedFormContentType(filename) || hasFreeFormContentType(filename);
    }

    @Override
    public boolean shouldProcessProject(IProject project)
    {
        try
        {
            if (!project.isAccessible()) return false;
            if (!project.hasNature(ProjectNatures.C_NATURE_ID) && !project.hasNature(ProjectNatures.CC_NATURE_ID)) return false;
            return inTestingMode() || SearchPathProperties.getProperty(project, SearchPathProperties.ENABLE_VPG_PROPERTY_NAME).equals("true");
        }
        catch (CoreException e)
        {
            throw new Error(e);
        }
    }

    public String describeWhyCannotProcessProject(IProject project)
    {
        try
        {
            if (!project.isAccessible())
                return "The project " + project.getName() + " is not accessible.  "
                     + "Please make sure that it is open and that the permissions are set correctly.";
            else if (!project.hasNature(ProjectNatures.C_NATURE_ID) && !project.hasNature(ProjectNatures.CC_NATURE_ID))
                return "The project " + project.getName() + " is not a Fortran, C, or C++ project.  "
                     + "Plase convert it to a Fortran, C, or C++ project and enable analysis and "
                     + "refactoring in the project properties.";
            else if (!SearchPathProperties.getProperty(project, SearchPathProperties.ENABLE_VPG_PROPERTY_NAME).equals("true"))
                return "Please enable analysis and refactoring in the project properties for "
                     + project.getName() + ".";
            else
                return null;
        }
        catch (CoreException e)
        {
            throw new Error(e);
        }
    }

    public String describeWhyCannotProcessFile(IFile file)
    {
        if (file.getProject() == null)
            return "The file " + file.getName() + " is not located in a Fortran, C, or C++ project.";
        else if (!shouldProcessProject(file.getProject()))
            return describeWhyCannotProcessProject(file.getProject());
        else if (!shouldProcessFile(file))
            return "The file " + file.getName() + "'s filename extension ("
                 + file.getFileExtension()
                 + ") indicates that this is not a Fortran source file.\n\nIf you believe that this "
                 + "is incorrect, please see the Photran User's Guide for instructions on how to "
                 + "change the file's content type in the workbench preferences.";
        else
            return null;
    }

	@Override
	protected PhotranTokenRef getTokenRef(Token forToken)
	{
		return forToken.getTokenRef();
	}

	public static String canonicalizeIdentifier(String identifier)
	{
		return identifier.trim().toLowerCase().replaceAll("[ \t\r\n]", "");
	}

    private List<IFile> getOutgoingDependenciesFrom(String targetFilename)
    {
        List<IFile> files = new LinkedList<IFile>();
        for (String filename : db.getOutgoingDependenciesFrom(targetFilename))
        {
            IFile file = getIFileForFilename(filename);
            if (file != null) files.add(file);
        }
        return files;
    }

    private List<IFile> getIncomingDependenciesTo(String targetFilename)
    {
        List<IFile> files = new LinkedList<IFile>();
        for (String filename : db.getIncomingDependenciesTo(targetFilename))
            files.add(getIFileForFilename(filename));
        return files;
    }

    public ArrayList<Definition> findAllExternalSubprogramsNamed(String name)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        for (IFile file : findFilesThatExportSubprogram(name))
            result.addAll(findSubprograms(name, file));
        return result;
    }

    private ArrayList<Definition> findSubprograms(String name, IFile file)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        String cname = PhotranVPG.canonicalizeIdentifier(name);

        IFortranAST ast = acquireTransientAST(file);
        if (ast != null)
        {
            ASTExecutableProgramNode node = ast.getRoot();
            for (IProgramUnit pu : node.getProgramUnitList())
            {
                PhotranTokenRef tr = attemptToMatch(cname, pu);
                if (tr != null)
                {
                    Definition d = getDefinitionFor(tr);
                    if (d != null) result.add(d);
                }
            }
        }

        return result;
    }

    private PhotranTokenRef attemptToMatch(String cname, IProgramUnit pu)
    {
        if (pu instanceof ASTSubroutineSubprogramNode)
            return attemptToMatch(cname, ((ASTSubroutineSubprogramNode)pu).getSubroutineStmt());
        else if (pu instanceof ASTFunctionSubprogramNode)
            return attemptToMatch(cname, ((ASTFunctionSubprogramNode)pu).getFunctionStmt());
        else
            return null;
    }

    private PhotranTokenRef attemptToMatch(String cname, ASTSubroutineStmtNode functionStmt)
    {
        return attemptToMatch(cname, functionStmt.getSubroutineName().getSubroutineName());
    }

    private PhotranTokenRef attemptToMatch(String cname, ASTFunctionStmtNode functionStmt)
    {
        return attemptToMatch(cname, functionStmt.getFunctionName().getFunctionName());
    }

    private PhotranTokenRef attemptToMatch(String cname, Token nameToken)
    {
            String thisSub = canonicalizeIdentifier(nameToken.getText());
            if (thisSub.equals(cname))
                return nameToken.getTokenRef();
            else
                return null;
    }

//    private ArrayList<Definition> mapDefinitions(ArrayList<PhotranTokenRef> tokenRefs)
//    {
//        ArrayList<Definition> result = new ArrayList<Definition>();
//        for (PhotranTokenRef tr : tokenRefs)
//        {
//            Definition def = getDefinitionFor(tr);
//            if (def != null)
//                result.add(def);
//        }
//        return result;
//    }

    public ArrayList<Definition> findAllDeclarationsInInterfacesForExternalSubprogram(String name)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        for (IFile file : findFilesThatImportSubprogram(name))
            result.addAll(findInterfaceSubprograms(name, file));
        return result;
    }

    private ArrayList<Definition> findInterfaceSubprograms(String name, IFile file)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();

        IFortranAST ast = acquireTransientAST(file);
        if (ast != null)
            ast.accept(new InterfaceVisitor(result, PhotranVPG.canonicalizeIdentifier(name)));

        return result;
    }

    private final class InterfaceVisitor extends GenericASTVisitor
    {
        private final ArrayList<Definition> result;
        private final String canonicalizedName;

        public InterfaceVisitor(ArrayList<Definition> result, String canonicalizedName)
        {
            this.result = result;
            this.canonicalizedName = canonicalizedName;
        }

        @Override public void visitASTFunctionStmtNode(ASTFunctionStmtNode node)
        {
            addIfDefinedInInterface(PhotranVPG.this.attemptToMatch(canonicalizedName, node));
        }

        @Override public void visitASTSubroutineStmtNode(ASTSubroutineStmtNode node)
        {
            addIfDefinedInInterface(PhotranVPG.this.attemptToMatch(canonicalizedName, node));
        }

        private void addIfDefinedInInterface(PhotranTokenRef tr)
        {
            Definition def = tr == null ? null : getDefinitionFor(tr);
            if (def != null && def.isExternalSubprogramReferenceInInterfaceBlock())
                result.add(def);
        }
    }

    public ArrayList<Definition> findAllDeclarationsInExternalStmts(String name)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();
        for (IFile file : findFilesThatImportSubprogram(name))
            result.addAll(findExternalStmts(name, file));
        return result;
    }

    private ArrayList<Definition> findExternalStmts(String name, IFile file)
    {
        ArrayList<Definition> result = new ArrayList<Definition>();

        IFortranAST ast = acquireTransientAST(file);
        if (ast != null)
            ast.accept(new ExternalStmtVisitor(result, PhotranVPG.canonicalizeIdentifier(name)));

        return result;
    }

    private final class ExternalStmtVisitor extends GenericASTVisitor
    {
        private final ArrayList<Definition> result;
        private final String canonicalizedName;

        public ExternalStmtVisitor(ArrayList<Definition> result, String canonicalizedName)
        {
            this.result = result;
            this.canonicalizedName = canonicalizedName;
        }

        // # R1208
        // <ExternalStmt> ::=
        // <LblDef> T_EXTERNAL <ExternalNameList> T_EOS
        // | <LblDef> T_EXTERNAL T_COLON T_COLON <ExternalNameList> T_EOS
        //
        // <ExternalNameList> ::=
        // <ExternalName>
        // | @:<ExternalNameList> T_COMMA <ExternalName>

        @Override public void visitASTExternalStmtNode(ASTExternalStmtNode node)
        {
            super.traverseChildren(node);

            IASTListNode<ASTExternalNameListNode> list = node.getExternalNameList();
            for (int i = 0; i < list.size(); i++)
                add(PhotranVPG.this.attemptToMatch(canonicalizedName, list.get(i).getExternalName()));
        }

        private void add(PhotranTokenRef tr)
        {
            Definition def = tr == null ? null : getDefinitionFor(tr);
            if (def != null) // && def.getClassification().equals(Classification.EXTERNAL))
                result.add(def);
        }
    }

    public List<IFile> findFilesThatExportSubprogram(String subprogramName)
    {
        return getOutgoingDependenciesFrom("subprogram:" + canonicalizeIdentifier(subprogramName));
    }

    public List<IFile> findFilesThatImportSubprogram(String subprogramName)
    {
        return getIncomingDependenciesTo("subprogram:" + canonicalizeIdentifier(subprogramName));
    }

    public List<IFile> findFilesThatExportModule(String moduleName)
    {
        return getOutgoingDependenciesFrom("module:" + canonicalizeIdentifier(moduleName));
    }

    public List<IFile> findFilesThatUseCommonBlock(String commonBlockName)
    {
        // The unnamed common block is stored with the empty name as its name
        if (commonBlockName == null) commonBlockName = "";

        return getIncomingDependenciesTo("common:" + canonicalizeIdentifier(commonBlockName));
    }

    public Iterable<String> listAllModules()
    {
        return listAllStartingWith("module:");
    }

    public Iterable<String> listAllSubprograms()
    {
        return listAllStartingWith("subprogram:");
    }

    public Iterable<String> listAllCommonBlocks()
    {
        return listAllStartingWith("common:");
    }

    private Iterable<String> listAllStartingWith(String prefix)
    {
        /*
         * When there is a module "module1" declared in module1.f90, the VPG
         * will contain a dependency
         *
         *     module:module1    -----depends-on----->    module1.f90
         *
         * So we can determine all modules by searching the list of dependent
         * filenames.  Note that this will include every module that is
         * declared, even if it is never used.
         */

        TreeSet<String> result = new TreeSet<String>();
        for (String name : db.listAllDependentFilenames())
            if (name.startsWith(prefix))
                result.add(name.substring(prefix.length()));
        return result;
    }

	public Definition getDefinitionFor(PhotranTokenRef tokenRef)
	{
		return (Definition)db.getAnnotation(tokenRef, DEFINITION_ANNOTATION_TYPE);
	}

	public Type getTypeFor(PhotranTokenRef tokenRef)
	{
		return (Type)db.getAnnotation(tokenRef, TYPE_ANNOTATION_TYPE);
	}

	public Visibility getVisibilityFor(Definition def, ScopingNode visibilityInScope)
	{
	    PhotranTokenRef targetScope = visibilityInScope.getRepresentativeToken();

	    for (PhotranTokenRef privateScope : db.getOutgoingEdgeTargets(def.getTokenRef(), DEFINITION_IS_PRIVATE_IN_SCOPE_EDGE_TYPE))
	        if (privateScope.equals(targetScope))
	            return Visibility.PRIVATE;

	    return Visibility.PUBLIC;
	}

    public PhotranTokenRef getModuleTokenRef(String moduleName)
    {
        String filename = "module:" + canonicalizeIdentifier(moduleName);
        PhotranTokenRef tokenRef = createTokenRef(filename, 0, 0);
        //System.err.println("getModuleTokenRef(" + moduleName + ") returning " + db.getAnnotation(tokenRef, MODULE_TOKENREF_ANNOTATION_TYPE));
        return (PhotranTokenRef)db.getAnnotation(tokenRef, MODULE_TOKENREF_ANNOTATION_TYPE);
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

        String filename = "module:" + canonicalizeIdentifier(moduleName);
        ArrayList<Definition> result = new ArrayList<Definition>(entries);
        for (int i = 0; i < entries; i++)
        {
            PhotranTokenRef tokenRef = createTokenRef(filename, i, 0);
            Object entry = db.getAnnotation(tokenRef, MODULE_SYMTAB_ENTRY_ANNOTATION_TYPE);
            if (entry != null && entry instanceof Definition)
                result.add((Definition)entry);
        }

        moduleSymTabCache.cache(moduleName, result);

        return result;
    }

    protected int countModuleSymbolTableEntries(String canonicalizedModuleName)
    {
        String filename = "module:" + canonicalizedModuleName;
        PhotranTokenRef tokenRef = createTokenRef(filename, 0, 0);
        Object result = db.getAnnotation(tokenRef, MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE);
        return result == null || !(result instanceof Integer) ? 0 : ((Integer)result).intValue();
    }

    public void printModuleSymTabCacheStatisticsOn(PrintStream out)
    {
        out.println("Module Symbol Table Cache Statistics:");

        long edgeTotal = moduleSymTabCacheHits + moduleSymTabCacheMisses;
        float edgeHitRatio = edgeTotal == 0 ? 0 : ((float)moduleSymTabCacheHits) / edgeTotal * 100;
        out.println("    Hit Ratio:        " + moduleSymTabCacheHits + "/" + edgeTotal + " (" + (long)Math.round(edgeHitRatio) + "%)");
    }

    public void resetStatistics()
    {
        moduleSymTabCacheHits = moduleSymTabCacheMisses = 0L;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // VPG Error/Warning Log View/Listener Support
    ////////////////////////////////////////////////////////////////////////////////

    private List<IMarker> errorLogMarkers = null;

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
        for (VPGLog<Token, PhotranTokenRef>.Entry entry : errorLog)
        {
            try
            {
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
            return "org.eclipse.photran.core.vpg.warningMarker";
        else // (entry.isError())
            return "org.eclipse.photran.core.vpg.errorMarker";
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

    public static boolean hasFixedFormContentType(IFile file)
    {
        return hasFixedFormContentType(getFilenameForIFile(file));
    }

    public static boolean hasFreeFormContentType(IFile file)
    {
        return hasFreeFormContentType(getFilenameForIFile(file));
    }

    protected static boolean hasFixedFormContentType(String filename)
    {
        if (inTestingMode()) // Fortran content types not set in testing workspace
            return filename.endsWith(".f");
        else
        {
            IContentType ct = getContentTypeOf(filename);
            return ct != null && ct.isKindOf(fixedFormContentType());
        }
    }

    protected static boolean hasFreeFormContentType(String filename)
    {
        if (inTestingMode()) // Fortran content types not set in testing workspace
            return filename.endsWith(".f90");
        else
        {
            IContentType ct = getContentTypeOf(filename);
            return ct != null && ct.isKindOf(freeFormContentType());
        }
    }

    protected static final IContentType getContentTypeOf(String filename)
    {
        return Platform.getContentTypeManager().findContentTypeFor(filename);
    }

    protected static final IContentType fixedFormContentType()
    {
        return Platform.getContentTypeManager().getContentType(FIXED_FORM_CONTENT_TYPE);
    }

    protected static final IContentType freeFormContentType()
    {
        return Platform.getContentTypeManager().getContentType(FREE_FORM_CONTENT_TYPE);
    }

    public boolean doesProjectHaveRefactoringEnabled(IFile file)
    {
        if (PhotranVPG.inTestingMode()) return true;

        String vpgEnabledProperty = SearchPathProperties.getProperty(
            file,
            SearchPathProperties.ENABLE_VPG_PROPERTY_NAME);
        return vpgEnabledProperty != null && vpgEnabledProperty.equals("true");
    }


    private boolean isDefinitionCachingEnabled = false;
    public void enableDefinitionCaching() { isDefinitionCachingEnabled = true; }
    public void disableDefinitionCaching() { isDefinitionCachingEnabled = false; }
    public boolean isDefinitionCachingEnabled() { return isDefinitionCachingEnabled; }
}
