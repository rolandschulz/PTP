package org.eclipse.photran.core.vpg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.IProgramUnit;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;

import bz.over.vpg.eclipse.EclipseVPG;

/**
 * Photran's Virtual Program Graph.
 * 
 * @author Jeff Overbey
 */
public abstract class PhotranVPG extends EclipseVPG<IFortranAST, Token, PhotranTokenRef, PhotranVPGDB>
{
	// Copied from FortranCorePlugin to avoid dependencies on the Photran Core plug-in
	// (since our parser declares classes with the same name)
    public static final String FIXED_FORM_CONTENT_TYPE = "org.eclipse.photran.core.fixedFormFortranSource";
    public static final String FREE_FORM_CONTENT_TYPE = "org.eclipse.photran.core.freeFormFortranSource";
    
	public static final int DEFINED_IN_SCOPE_EDGE_TYPE = 0;
	public static final int IMPORTED_INTO_SCOPE_EDGE_TYPE = 1;
	public static final int BINDING_EDGE_TYPE = 2;
    public static final int RENAMED_BINDING_EDGE_TYPE = 3;
    private static final String[] edgeTypeDescriptions =
	{
	    "Definition-scope relationship",
	    "Definition-scope relationship due to module import",
	    "Binding",
	    "Renamed binding",
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
            if (FortranPreferences.ENABLE_VPG_LOGGING.getValue())
            {
    		    instance = new PhotranVPGBuilder()
        		{
        		    @Override public void debug(String message, String filename)
        		    {
        		        System.out.println(message + " - " + lastSegmentOf(filename));
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

    private static String lastSegmentOf(String filename)
    {
        return filename.substring(filename.lastIndexOf('/') + 1);
    }

    @Override public void start()
	{
		if (!inTestingMode()) super.start();
	}

	protected PhotranVPG()
	{
        super(new PhotranVPGDB(), "Photran indexer", 2);
        db = super.db;
    }

    public static boolean inTestingMode()
	{
		return System.getenv("TESTING") != null;
	}

	protected String describeEdgeType(int edgeType)
	{
		return edgeTypeDescriptions[edgeType];
	}

	protected String describeAnnotationType(int annotationType)
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
	protected PhotranTokenRef getTokenRef(Token forToken)
	{
		return forToken.getTokenRef();
	}
	
	public IFortranAST acquireTransientAST(IFile file)
	{
	    return file == null ? null : acquireTransientAST(getFilenameForIFile(file));
	}
	
	public IFortranAST acquirePermanentAST(IFile file)
	{
	    return file == null ? null : acquirePermanentAST(getFilenameForIFile(file));
	}
	
	public void releaseAST(IFile file)
	{
	    if (file != null) releaseAST(getFilenameForIFile(file));
	}

	public static String canonicalizeIdentifier(String moduleName)
	{
		return moduleName.trim().toLowerCase().replaceAll("[ \t\r\n]", "");
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
	
	public Definition getDefinitionFor(PhotranTokenRef tokenRef)
	{
		return (Definition)db.getAnnotation(tokenRef, DEFINITION_ANNOTATION_TYPE);
	}
	
	public Type getTypeFor(PhotranTokenRef tokenRef)
	{
		return (Type)db.getAnnotation(tokenRef, TYPE_ANNOTATION_TYPE);
	}
    
    public PhotranTokenRef getModuleTokenRef(String moduleName)
    {
        String filename = "module:" + canonicalizeIdentifier(moduleName);
        PhotranTokenRef tokenRef = createTokenRef(filename, 0, 0);
        //System.err.println("getModuleTokenRef(" + moduleName + ") returning " + db.getAnnotation(tokenRef, MODULE_TOKENREF_ANNOTATION_TYPE));
        return (PhotranTokenRef)db.getAnnotation(tokenRef, MODULE_TOKENREF_ANNOTATION_TYPE);
    }
    
    public List<Definition> getModuleSymbolTable(String moduleName)
    {
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
        return result;
   }

    protected int countModuleSymbolTableEntries(String canonicalizedModuleName)
    {
        String filename = "module:" + canonicalizedModuleName;
        PhotranTokenRef tokenRef = createTokenRef(filename, 0, 0);
        Object result = db.getAnnotation(tokenRef, MODULE_SYMTAB_ENTRY_COUNT_ANNOTATION_TYPE);
        return result == null || !(result instanceof Integer) ? 0 : ((Integer)result).intValue();
    }
}
