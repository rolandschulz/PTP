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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.photran.core.FortranAST;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.ChainedVisitor;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.DefinitionCollector;
import org.eclipse.photran.internal.core.analysis.binding.ImplicitSpecCollector;
import org.eclipse.photran.internal.core.analysis.binding.ModuleLoader;
import org.eclipse.photran.internal.core.analysis.binding.ReferenceCollector;
import org.eclipse.photran.internal.core.analysis.binding.SpecificationCollector;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser;

import bz.over.vpg.TokenRef;
import bz.over.vpg.VPGDependency;
import bz.over.vpg.eclipse.EclipseVPG;

/**
 * Photran's Virtual Program Graph.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPG extends EclipseVPG<IFortranAST, Token>
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
	
	public static PhotranVPG getInstance()
	{
		if (instance == null) instance = new PhotranVPGBuilder();
		return instance;
	}
	
	@Override public void start()
	{
		if (!inTestingMode()) super.start();
	}

	protected PhotranVPG()
	{
        super(inTestingMode() ? createTempFile() : Activator.getDefault().getStateLocation().addTrailingSeparator().toOSString() + "vpg",
              "Synchronizing Photran VPG");
    }
	
	private static String createTempFile()
    {
	    try
        {
            File f = File.createTempFile("vpg", null);
            f.deleteOnExit();
            return f.getAbsolutePath();
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
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
			return project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID);
		}
		catch (CoreException e)
		{
			throw new Error(e);
		}
	}
	
	@Override protected byte[] serialize(Serializable annotation) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new ObjectOutputStream(out).writeObject(annotation);
		return out.toByteArray();
	}
	
	@Override protected Serializable deserialize(InputStream binaryStream) throws IOException, ClassNotFoundException
	{
		return (Serializable)new ObjectInputStream(binaryStream).readObject();
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
				return super.describeToken(filename, offset, length);
			else
				return token.getText() + " (offset " + offset + ")";
		}
		catch (Exception e)
		{
			return super.describeToken(filename, offset, length);
		}
	}
	
	@Override public TokenRef<Token> createTokenRef(String filename, int offset, int length)
	{
		return new PhotranTokenRef(filename, offset, length);
	}

	@Override
	protected long getModificationStamp(String filename)
	{
		if (filename.startsWith("module:")) return Long.MIN_VALUE;
		
		return getIFileForFilename(filename).getLocalTimeStamp();
	}

	@Override
	public Token findToken(TokenRef<Token> tokenRef)
	{
		IFortranAST ast = acquireTransientAST(tokenRef.getFilename());
		if (ast == null)
			return null;
		else
			return ast.findTokenByFileOffsetLength(getIFileForFilename(tokenRef.getFilename()), tokenRef.getOffset(), tokenRef.getLength());
	}

	@Override
	protected TokenRef<Token> getTokenRef(Token forToken)
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
					
					ensure(new VPGDependency<IFortranAST, Token>(PhotranVPG.this,
								filename,
								getFilenameForIFile(getIncludedFile(fileToInclude))));
					
					return super.getIncludedFileAsStream(fileToInclude);
				}
			});
		}

		try
		{
			IAccumulatingLexer lexer = LexerFactory.createLexer(file, sourceForm);
			return new FortranAST(file, new Parser().parse(lexer), lexer.getTokenList());
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
			deleteAllIncomingDependenciesFor(filename);
			deleteAllOutgoingDependenciesFor(filename);
		}
		
		if (ast == null) return;
		
		ast.visitTopDownUsing(new ImplicitSpecCollector());
		
		ast.visitBottomUpUsing(new DefinitionCollector(getIFileForFilename(filename)));
		
		ast.visitBottomUpUsing(new SpecificationCollector());

		ast.visitBottomUpUsing(
			new ModuleLoader(getIFileForFilename(filename),
					new NullProgressMonitor()));
					//PhotranVPG.getInstance().getCurrentProgressMonitor()));

		// TODO: Type check here so derived type components can be resolved
		
		ast.visitBottomUpUsing(new ReferenceCollector());
		
		/*
		ChainedVisitor v = new ChainedVisitor();
		v.addTopDownVisitor(new ImplicitSpecCollector());
        v.addBottomUpVisitor(new DefinitionCollector(getIFileForFilename(filename)));
        v.addBottomUpVisitor(new SpecificationCollector());
        v.addBottomUpVisitor(new ModuleLoader(getIFileForFilename(filename), new NullProgressMonitor()));
        v.addBottomUpVisitor(new ReferenceCollector());
        ast.visitUsing(v);
        */
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
		for (String filename : getOutgoingDependenciesFrom("module:" + canonicalizeIdentifier(moduleName)))
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
	
	public Definition getDefinitionFor(TokenRef<Token> tokenRef)
	{
		return (Definition)getAnnotation(tokenRef, DEFINITION_ANNOTATION_TYPE);
	}
	
	public Type getTypeFor(TokenRef<Token> tokenRef)
	{
		return (Type)getAnnotation(tokenRef, TYPE_ANNOTATION_TYPE);
	}
}
