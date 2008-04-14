package org.eclipse.photran.core.vpg;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Binder;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;

import bz.over.vpg.VPGDependency;
import bz.over.vpg.eclipse.EclipseVPG;

/**
 * Photran's Virtual Program Graph.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPG extends EclipseVPG<IFortranAST, Token, PhotranTokenRef, PhotranVPGDB>
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
	
	private static final String[] annotationTypeDescriptions = { "Default visibility for scope is private",
	                                                             "Implicit spec for scope",
	                                                             "Definition",
	                                                             "Type" };
	
	private static PhotranVPG instance = null;
	public PhotranVPGDB db = null;
	
	protected Parser parser = new Parser();
	
	public static PhotranVPG getInstance()
	{
		if (instance == null) instance = new PhotranVPGBuilder();
		return instance;
	}
    
    public static PhotranVPGDB getDatabase()
    {
        return getInstance().db;
    }
	
    public static void printDebug(String message, String filename)
    {
        System.out.println(message + " - " + lastSegmentOf(filename));
    }
    
    @Override protected void debug(String message, String filename)
    {
        PhotranVPG.printDebug(message, filename);
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

	@Override
	protected boolean shouldProcessFile(IFile file)
	{
		String filename = file.getName();
		return hasFixedFormContentType(filename) || hasFreeFormContentType(filename);
	}
	
	private static boolean hasFixedFormContentType(String filename)
	{
		if (inTestingMode()) // Fortran content types not set in testing workspace
			return filename.endsWith(".f");
		else
			return FIXED_FORM_CONTENT_TYPE.equals(getContentType(filename));
	}
	
	private static boolean hasFreeFormContentType(String filename)
	{
		if (inTestingMode()) // Fortran content types not set in testing workspace
			return filename.endsWith(".f90");
		else
			return FREE_FORM_CONTENT_TYPE.equals(getContentType(filename));
	}
	
	private static final String getContentType(String filename)
	{
		IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(filename);
		return contentType == null ? null : contentType.getId();
		
		// In CDT, return CoreModel.getRegistedContentTypeId(file.getProject(), file.getName());
	}

	@Override
	protected boolean shouldProcessProject(IProject project)
	{
		try
		{
		    if (!project.isAccessible()) return false;
		    if (!project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) return false;
		    return inTestingMode() || SearchPathProperties.getProperty(project, SearchPathProperties.ENABLE_VPG_PROPERTY_NAME).equals("true");
		}
		catch (CoreException e)
		{
			throw new Error(e);
		}
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
	
	@Override public PhotranTokenRef createTokenRef(String filename, int offset, int length)
	{
		return new PhotranTokenRef(filename, offset, length);
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
	
	@Override
	protected IFortranAST parse(final String filename)
	{
		if (filename.startsWith("module:")) return null;
		
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
					
					db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(PhotranVPG.this,
								filename,
								getFilenameForIFile(getIncludedFile(fileToInclude))));
					
					return super.getIncludedFileAsStream(fileToInclude);
				}
			});
		}

		try
		{
			IAccumulatingLexer lexer = LexerFactory.createLexer(file, sourceForm);
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
}
