/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.AbstractLanguage;
import org.eclipse.cdt.core.model.BufferChangedEvent;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.internal.core.dom.parser.ASTTranslationUnit;
import org.eclipse.cdt.internal.core.index.IndexBasedFileContentProvider;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.RemoteIndexerInputAdapter;
import org.eclipse.ptp.internal.rdt.core.RemoteUtil;
import org.eclipse.ptp.internal.rdt.core.miners.CDTMiner;
import org.eclipse.ptp.internal.rdt.core.miners.RemoteLanguageMapper;
import org.eclipse.ptp.rdt.core.IConfigurableLanguage;

/**
 * @author crecoskie
 *
 */
public class TranslationUnit extends Parent implements ITranslationUnit {
	private static final long serialVersionUID = 1L;

	private String fLanguageId;
	transient protected ILanguage fLanguage;

	private IScannerInfo fScannerInfo;
	private Map<String,String> fLanguageProperties; // may be null as most languages don't need additional config
	private boolean isHeaderUnit = false;
	
	
	public TranslationUnit(ICElement parent, String name, String projectName, URI locationURI) {
		super(parent, ICElement.C_UNIT, name);
		
		if(projectName == null || locationURI == null) {
			throw new IllegalArgumentException();
		}
		
		setCProject(new CProject(projectName));
			
		fLocation = locationURI;
	}

	public TranslationUnit(Parent parent, ITranslationUnit element) {
		super(parent, element.getElementType(), element.getElementName());
		ICProject project = element.getCProject();
		if (project instanceof CProject) {
			setCProject(project);
		} else if (project != null) {
			setCProject(new CProject(project.getElementName()));
		}
		else {
			throw new IllegalArgumentException();
		}
		try {
			setLanguage(element.getLanguage());
		} catch (CoreException e) {
			throw new IllegalArgumentException(e);
		}
		setLocationURI(element.getLocationURI());
		
		if(element instanceof IHasManagedLocation) {
			IHasManagedLocation hml = (IHasManagedLocation) element;
			setManagedLocation(hml.getManagedLocation());
		}
		
		else {
			// we are adapting a local TU to a remote TU
			// we need to get a hold of the managed URI
			URI managedURI = EFSExtensionManager.getDefault().getLinkedURI(element.getLocationURI());
			setManagedLocation(managedURI);
		}
		
		if(element instanceof IHasRemotePath) {
			IHasRemotePath hasRemotePath = (IHasRemotePath) element;
			setRemotePath(hasRemotePath.getRemotePath());
		}
		
		else {
			// we are adapting a local TU to a remote TU
			// we need to get a hold of the mapped path
			String remotePath = EFSExtensionManager.getDefault().getPathFromURI(element.getLocationURI());
			setRemotePath(remotePath);
		}
		
		isHeaderUnit = element.isHeaderUnit();
	}
	
	/**
	 * This method must be called before a Translation Unit object is serialized
	 * to set up the context so that an AST can be generated on the remote side.
	 */
	public void setASTContext(IScannerInfo scannerInfo, Map<String,String> languageProperties) {
		fScannerInfo = scannerInfo;
		fLanguageProperties = languageProperties;
	}
	
	public String getLanguageId() {
		return fLanguageId;
	}
	
	public Map<String,String> getLanguageProperties() {
		return fLanguageProperties;
	}

	public IInclude createInclude(String name, boolean isStd,
			ICElement sibling, IProgressMonitor monitor) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public INamespace createNamespace(String namespace, ICElement sibling,
			IProgressMonitor monitor) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IUsing createUsing(String name, boolean isDirective,
			ICElement sibling, IProgressMonitor monitor) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IWorkingCopy findSharedWorkingCopy(IBufferFactory bufferFactory) {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getAST() throws CoreException {
		return getAST(null, 0);
	}

	
	
	public void setLanguage(ILanguage language) {
		fLanguage = language;
		fLanguageId = language == null ? null : language.getId();
	}
	
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.core/model
	 * Class: org.eclipse.cdt.internal.core.model.TranslationUnit
	 * Version: 1.120
	 */
	private IncludeFileContentProvider getIncludeFileContentProvider(int style, IIndex index, int linkageID) {
		final ASTFilePathResolver pathResolver = new RemoteIndexerInputAdapter();

		IncludeFileContentProvider fileContentsProvider;
		if ((style & AST_SKIP_NONINDEXED_HEADERS) != 0) {
			fileContentsProvider= IncludeFileContentProvider.getEmptyFilesProvider();
		} else {
			fileContentsProvider= IncludeFileContentProvider.getSavedFilesProvider();
		}
		
		if (index != null && (style & AST_SKIP_INDEXED_HEADERS) != 0) {
			IndexBasedFileContentProvider ibcf= new IndexBasedFileContentProvider(index, pathResolver, linkageID,
					fileContentsProvider);
			if ((style & AST_CONFIGURE_USING_SOURCE_CONTEXT) != 0) {
				ibcf.setSupportFillGapFromContextToHeader(true);
			}
			fileContentsProvider= ibcf;
		}
		
		return fileContentsProvider;
	}

	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.core/model
	 * Class: org.eclipse.cdt.internal.core.model.TranslationUnit
	 * Version: 1.120
	 */
	public IASTTranslationUnit getAST(IIndex index, int style) throws CoreException {
		checkState();
		
		IScannerInfo scanInfo= getScannerInfo(true);
		if (scanInfo == null) {
			return null;
		}
		
		FileContent fileContent= FileContent.create(getPathForASTFileLocation(), getContents());
		if (fileContent == null) {
			return null;
		}

		ILanguage language= getLanguage();
			
		if (language == null) {
			return null;
		}

		IncludeFileContentProvider crf= getIncludeFileContentProvider(style, index, language.getLinkageID());
		int options= 0;
		if ((style & AST_SKIP_FUNCTION_BODIES) != 0) {
			options |= ILanguage.OPTION_SKIP_FUNCTION_BODIES;
		}
		if ((style & AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS) != 0) {
			options |= ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;
		}
		if ((style & AST_PARSE_INACTIVE_CODE) != 0) {
			options |= ILanguage.OPTION_PARSE_INACTIVE_CODE;
		}
		if (isSourceUnit()) {
			options |= ILanguage.OPTION_IS_SOURCE_UNIT;
		}
		IParserLogService log = new DefaultLogService();
		
		ASTTranslationUnit ast = (ASTTranslationUnit) ((AbstractLanguage) language).getASTTranslationUnit(
				fileContent, scanInfo, crf, index, options, log);
		ast.setOriginatingTranslationUnit(this);

		return ast;

		
	}
	
	/**
	 * TODO this is just wrong!
	 */
	public int getParserOptions(String filePath) {
		if (filePath.endsWith(".cpp") || filePath.endsWith(".C") || filePath.endsWith(".c")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return ILanguage.OPTION_IS_SOURCE_UNIT;
		}
		return 0;
	}

	public CodeReader getCodeReader() {
	
		
		URI uri = null;
		
		if(fManagedLocation != null)
			uri = fManagedLocation;
		else
			uri = fLocation;
		
		if(uri == null)
			return null;
		
		String filePath = fRemotePath != null ? fRemotePath : uri.getPath();
		try {
			return new CodeReader(filePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	
	/* -- ST-Origin --
	 * Source folder: org.eclipse.cdt.core/model
	 * Class: org.eclipse.cdt.internal.core.model.TranslationUnit
	 * Version: 1.120
	 */
	public IASTCompletionNode getCompletionNode(IIndex index, int style, int offset) throws CoreException {
		checkState();

		IScannerInfo scanInfo = getScannerInfo(true);
		if (scanInfo == null) {
			return null;
		}
		
		FileContent fileContent= FileContent.create(getPathForASTFileLocation(), getContents());
		
		ILanguage language= getLanguage();
		if (language != null) {
			IncludeFileContentProvider crf= getIncludeFileContentProvider(style, index, language.getLinkageID());
			IASTCompletionNode result = language.getCompletionNode(fileContent, scanInfo, crf, index,
					new DefaultLogService(), offset);
			if (result != null) {
				final IASTTranslationUnit ast = result.getTranslationUnit();
				if (ast != null) {
					ast.setIsHeaderUnit(false);
					((ASTTranslationUnit) ast).setOriginatingTranslationUnit(this);
				}
			}
			return result;
		}
		return null;
	}
	
	
	public String getContentTypeId() {
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getContents() {
		// TODO Auto-generated method stub
		return new char[0];
	}

	public ICElement getElement(String name) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICElement getElementAtLine(int line) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICElement getElementAtOffset(int offset) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICElement[] getElementsAtOffset(int offset) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IInclude getInclude(String name) {
		try {
			ICElement[] celements = getChildren();
			for (ICElement celement : celements) {
				if (celement.getElementType() == ICElement.C_INCLUDE) {
					if (name.equals(celement.getElementName())) {
						return (IInclude) celement;
					}
				}
			}
		} catch (CModelException e) {
		}
		return null;
	}

	public IInclude[] getIncludes() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList<ICElement> aList = new ArrayList<ICElement>();
		for (ICElement celement : celements) {
			if (celement.getElementType() == ICElement.C_INCLUDE) {
				aList.add(celement);
			}
		}
		return aList.toArray(new IInclude[0]);
	}

	public ILanguage getLanguage() throws CoreException {
		checkState();
		return fLanguage;
	}
	
	private void checkState() {
		if(fLanguage == null && fLanguageId != null) {
			if(RemoteUtil.isRemote()) { // if we are running in the miner
				DataStore dataStore = null;
				if(Thread.currentThread() instanceof CDTMiner) {
					dataStore = ((CDTMiner)Thread.currentThread())._dataStore;
				}
				
				fLanguage = RemoteLanguageMapper.getLanguageById(fLanguageId, fLanguageProperties, dataStore); 
				if(fLanguageProperties != null && fLanguage instanceof IConfigurableLanguage) {
					((IConfigurableLanguage)fLanguage).setProperties(fLanguageProperties);
				}
			}
			else {
				fLanguage = LanguageManager.getInstance().getLanguage(fLanguageId);
			}
		}
	}

	public IPath getLocation() {	
		URI uri = null;
		
		if(fManagedLocation != null)
			uri = fManagedLocation;
		else
			uri = fLocation;
		
		if(uri == null)
			return null;
		
		String filePath = fRemotePath != null ? fRemotePath : uri.getPath();
		return new Path(filePath);		
	}

	public INamespace getNamespace(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public INamespace[] getNamespaces() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IScannerInfo getScannerInfo(boolean force) {
		return fScannerInfo;
	}

	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,
			IBufferFactory factory) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,
			IBufferFactory factory, IProblemRequestor requestor)
			throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IUsing getUsing(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public IUsing[] getUsings() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IWorkingCopy getWorkingCopy() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor,
			IBufferFactory factory) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isASMLanguage() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCLanguage() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCXXLanguage() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHeaderUnit() {
		return isHeaderUnit;
	}

	public boolean isSourceUnit() {
		return !isHeaderUnit;
	}

	public boolean isWorkingCopy() {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("unchecked")
	public Map parse() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setIsStructureKnown(boolean wasSuccessful) {
		// TODO Auto-generated method stub
		
	}

	public void close() throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public IBuffer getBuffer() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasUnsavedChanges() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isConsistent() throws CModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	public void makeConsistent(IProgressMonitor progress)
			throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public void makeConsistent(IProgressMonitor progress, boolean forced)
			throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public void open(IProgressMonitor progress) throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public void save(IProgressMonitor progress, boolean force)
			throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public void bufferChanged(BufferChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	public String getSource() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRange getSourceRange() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ITranslationUnit getTranslationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	public void copy(ICElement container, ICElement sibling, String rename,
			boolean replace, IProgressMonitor monitor) throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public void delete(boolean force, IProgressMonitor monitor)
			throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public void move(ICElement container, ICElement sibling, String rename,
			boolean replace, IProgressMonitor monitor) throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public void rename(String name, boolean replace, IProgressMonitor monitor)
			throws CModelException {
		// TODO Auto-generated method stub
		
	}

	public IProblemRequestor getProblemRequestor() {
		// TODO Auto-generated method stub
		return null;
	}
	

	public IWorkingCopy findSharedWorkingCopy() {
		// TODO Auto-generated method stub
		return null;
	}

	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,
			IProblemRequestor requestor) throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor)
			throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isActive() {
		return true;
	}


	private String getPathForASTFileLocation() {
		String pathString = getRemotePath();
		if(pathString == null) {
			pathString = getLocation().toOSString();
		}
		
		return pathString;	
	}
}
