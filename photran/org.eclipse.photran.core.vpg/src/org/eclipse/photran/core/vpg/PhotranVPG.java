package org.eclipse.photran.core.vpg;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser;
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
	
	private static final String[] edgeTypeDescriptions = { "Definition-scope relationship", "Definition-scope relationship due to module import", "Binding", "Renamed binding" };
	
	public static final int SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE = 0;
	public static final int SCOPE_IS_INTERNAL_ANNOTATION_TYPE = 1;
	public static final int SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE = 2;
	public static final int DEFINITION_ANNOTATION_TYPE = 3;
    public static final int TYPE_ANNOTATION_TYPE = 4;
    public static final int MODULE_TOKENREF_ANNOTATION_TYPE = 5;
    public static final int MODULE_SYMTAB_ANNOTATION_TYPE = 6;
	
	private static final String[] annotationTypeDescriptions = { "Default visibility for scope is private",
	                                                             "Implicit spec for scope",
	                                                             "Definition",
	                                                             "Type",
	                                                             "Module TokenRef",
	                                                             "Module symbol table" };
	
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
		return acquireTransientAST(getFilenameForIFile(file));
	}
	
	public IFortranAST acquirePermanentAST(IFile file)
	{
		return acquirePermanentAST(getFilenameForIFile(file));
	}
	
	public void releaseAST(IFile file)
	{
		releaseAST(getFilenameForIFile(file));
	}

	public static String canonicalizeIdentifier(String moduleName)
	{
		return moduleName.trim().toLowerCase().replaceAll("[ \t\r\n]", "");
	}

	public List<IFile> findFilesThatExportModule(String moduleName)
	{
		List<IFile> files = new LinkedList<IFile>();
		for (String filename : db.getOutgoingDependenciesFrom("module:" + canonicalizeIdentifier(moduleName)))
		{
			IFile file = getIFileForFilename(filename);
			if (file == null)
			{
				System.err.println("************** CAN'T MAP " + filename + " TO AN IFILE");
				try {
					ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor()
					{
						public boolean visit(IResource resource) throws CoreException
						{
							System.err.println(resource.getFullPath().toOSString());
							return true;
						}
						
					});
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (file != null) files.add(file);
		}
		return files;
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
    
    @SuppressWarnings("unchecked")
    public List<Definition> getModuleSymbolTable(String moduleName)
    {
        String filename = "module:" + canonicalizeIdentifier(moduleName);
        PhotranTokenRef tokenRef = createTokenRef(filename, 0, 0);
        //System.err.println("getModuleSymbolTable(" + moduleName + ") returning " + db.getAnnotation(tokenRef, MODULE_SYMTAB_ANNOTATION_TYPE));
        return (List<Definition>)db.getAnnotation(tokenRef, MODULE_SYMTAB_ANNOTATION_TYPE);
    }
}
