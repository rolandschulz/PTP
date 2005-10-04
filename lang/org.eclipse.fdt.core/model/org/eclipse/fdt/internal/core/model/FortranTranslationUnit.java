package org.eclipse.fdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.IBuffer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.BufferManager;
import org.eclipse.cdt.internal.core.model.CElement;
import org.eclipse.cdt.internal.core.model.CElementInfo;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.CreateIncludeOperation;
import org.eclipse.cdt.internal.core.model.CreateNamespaceOperation;
import org.eclipse.cdt.internal.core.model.CreateWorkingCopyOperation;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.model.Openable;
import org.eclipse.cdt.internal.core.model.OpenableInfo;
import org.eclipse.cdt.internal.core.model.SourceManipulationInfo;
import org.eclipse.cdt.internal.core.model.TranslationUnitInfo;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @see ITranslationUnit
 */
public class FortranTranslationUnit extends Openable implements ITranslationUnit {

	IPath location = null;
	String fContentTypeID;

	/**
	 * If set, this is the problem requestor which will be used to notify problems
	 * detected during reconciling.
	 */
	protected IProblemRequestor problemRequestor;


	SourceManipulationInfo sourceManipulationInfo = null;

	public FortranTranslationUnit(ICElement parent, IFile file, String idType) {
		super(parent, file, ICElement.C_UNIT);
		setContentTypeID(idType);
	}

	public FortranTranslationUnit(ICElement parent, IPath path, String idType) {
		super(parent, path, ICElement.C_UNIT);
		setContentTypeID(idType);
	}

	public FortranTranslationUnit(ICElement parent, IResource res, String name, String idType) {
		super(parent, res, name, ICElement.C_UNIT);
		setContentTypeID(idType);
	}

	public ITranslationUnit getTranslationUnit () {
		return this;
	}

	public IInclude createInclude(String includeName, boolean isStd, ICElement sibling, IProgressMonitor monitor)
		throws CModelException {
		CreateIncludeOperation op = new CreateIncludeOperation(includeName, isStd, this);
		if (sibling != null) {
			op.createBefore(sibling);
		}
		op.runOperation(monitor);
		return getInclude(includeName);
	}

	public IUsing createUsing(String usingName, boolean isDirective, ICElement sibling, IProgressMonitor monitor) throws CModelException {
		CreateIncludeOperation op = new CreateIncludeOperation(usingName, isDirective, this);
		if (sibling != null) {
			op.createBefore(sibling);
		}
		op.runOperation(monitor);
		return getUsing(usingName);
	}

	public INamespace createNamespace(String namespace, ICElement sibling, IProgressMonitor monitor) throws CModelException {
		CreateNamespaceOperation op = new CreateNamespaceOperation(namespace, this);
		if (sibling != null) {
			op.createBefore(sibling);
		}
		op.runOperation(monitor);
		return getNamespace(namespace);
	}

	public ICElement getElementAtLine(int line) throws CModelException {
		ICElement[] celements = getChildren();
		for (int i = 0; i < celements.length; i++) {
			ISourceRange range = ((ISourceReference)celements[i]).getSourceRange();
			int startLine = range.getStartLine();
			int endLine = range.getEndLine();
			if (line >= startLine && line <= endLine) {
				return celements[i];
			}
		}
		return null;
	}

	public ICElement getElementAtOffset(int pos) throws CModelException {
		ICElement e= getSourceElementAtOffset(pos);
		if (e == this) {
			return null;
		}
		return e;
	}

	public ICElement[] getElementsAtOffset(int pos) throws CModelException {
		ICElement[] e= getSourceElementsAtOffset(pos);
		if (e.length == 1 && e[0] == this) {
			return CElement.NO_ELEMENTS;
		}
		return e;		
	}

	public ICElement getElement(String name ) {
		if (name == null || name.length() == 0) {
			return null;
		}
		try {
			ICElement[] celements = getChildren();
			for (int i = 0; i < celements.length; i++) {
				if (name.equals(celements[i].getElementName())) {
					return celements[i];
				}
			}
		} catch (CModelException e) {
			//
		}

		String[] names = name.split("::"); //$NON-NLS-1$
		ICElement current = this;
		for (int j = 0; j < names.length; ++j) {
			if (current instanceof IParent) {
				try {
					ICElement[] celements = ((IParent)current).getChildren();
					current = null;
					for (int i = 0; i < celements.length; i++) {
						if (names[j].equals(celements[i].getElementName())) {
							current = celements[i];
							break;
						}
					}
				} catch (CModelException e) {		
					current = null;
				}
			} else {
				current = null;
			}
		}
		return current;
	}

	public IInclude getInclude(String name) {
		try {
			ICElement[] celements = getChildren();
			for (int i = 0; i < celements.length; i++) {
				if (celements[i].getElementType() == ICElement.C_INCLUDE) {
					if (name.equals(celements[i].getElementName())) {
						return (IInclude)celements[i];
					}
				}
			}
		} catch (CModelException e) {		
		}
		return null;
	}

	public IInclude[] getIncludes() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList aList = new ArrayList();
		for (int i = 0; i < celements.length; i++) {
			if (celements[i].getElementType() == ICElement.C_INCLUDE) {
				aList.add(celements[i]);
			}
		}
		return (IInclude[])aList.toArray(new IInclude[0]);
	}

	public IUsing getUsing(String name) {
		try {
			ICElement[] celements = getChildren();
			for (int i = 0; i < celements.length; i++) {
				if (celements[i].getElementType() == ICElement.C_USING) {
					if (name.equals(celements[i].getElementName())) {
						return (IUsing)celements[i];
					}
				}
			}
		} catch (CModelException e) {		
		}		
		return null;
	}

	public IUsing[] getUsings() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList aList = new ArrayList();
		for (int i = 0; i < celements.length; i++) {
			if (celements[i].getElementType() == ICElement.C_USING) {
				aList.add(celements[i]);
			}
		}
		return (IUsing[])aList.toArray(new IUsing[0]);
	}

	public INamespace getNamespace(String name) {
		try {
			String[] names = name.split("::"); //$NON-NLS-1$
			ICElement current = this;
			for (int j = 0; j < names.length; ++j) {
				if (current instanceof IParent) {
					ICElement[] celements = ((IParent)current).getChildren();
					current = null;
					for (int i = 0; i < celements.length; i++) {
						if (celements[i].getElementType() == ICElement.C_NAMESPACE) {
							if (name.equals(celements[i].getElementName())) {
								current = celements[i];
								break;
							}
						}
					}
				} else {
					current = null;
				}
			}
			if (current instanceof INamespace) {
				return (INamespace)current;
			}
		} catch (CModelException e) {		
		}		
		return null;
	}

	public INamespace[] getNamespaces() throws CModelException {
		ICElement[] celements = getChildren();
		ArrayList aList = new ArrayList();
		for (int i = 0; i < celements.length; i++) {
			if (celements[i].getElementType() == ICElement.C_NAMESPACE) {
				aList.add(celements[i]);
			}
		}
		return (INamespace[])aList.toArray(new INamespace[0]);
	}

	public void setLocation(IPath loc) {
		location = loc;
	}

	public IPath getLocation() {
		if (location == null) {
			IFile file = getFile();
			if (file != null) {
				location = file.getLocation();
			} else {
				return getPath();
			}
		}
		return location;
	}

	protected IFile getFile() {
		IResource res = getResource();
		if (res instanceof IFile) {
			return (IFile)res;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#copy(org.eclipse.cdt.core.model.ICElement, org.eclipse.cdt.core.model.ICElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void copy(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().copy(container, sibling, rename, force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#delete(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().delete(force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#move(org.eclipse.cdt.core.model.ICElement, org.eclipse.cdt.core.model.ICElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void move(ICElement container, ICElement sibling, String rename, boolean force,
		IProgressMonitor monitor) throws CModelException {
		getSourceManipulationInfo().move(container, sibling, rename, force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceManipulation#rename(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void rename(String name, boolean force, IProgressMonitor monitor)
		throws CModelException {
		getSourceManipulationInfo().rename(name, force, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getSource()
	 */
	public String getSource() throws CModelException {
		return getSourceManipulationInfo().getSource();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ISourceReference#getSourceRange()
	 */
	public ISourceRange getSourceRange() throws CModelException {
		return getSourceManipulationInfo().getSourceRange();
	}

	protected TranslationUnitInfo getTranslationUnitInfo() throws CModelException {
		return (TranslationUnitInfo)getElementInfo();
	}

	protected SourceManipulationInfo getSourceManipulationInfo() {
		if (sourceManipulationInfo == null) {
			sourceManipulationInfo = new SourceManipulationInfo(this);
		}
		return sourceManipulationInfo;
	}

	protected CElementInfo createElementInfo () {
		return new TranslationUnitInfo(this);
	}

	/**
	 * Returns true if this handle represents the same Java element
	 * as the given handle.
	 *
	 * <p>Compilation units must also check working copy state;
	 *
	 * @see Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof ITranslationUnit)) return false;
		return super.equals(o) && !((ITranslationUnit)o).isWorkingCopy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#findSharedWorkingCopy(org.eclipse.cdt.internal.core.model.IBufferFactory)
	 */
	public IWorkingCopy findSharedWorkingCopy(IBufferFactory factory) {

		// if factory is null, default factory must be used
		if (factory == null) factory = BufferManager.getDefaultBufferManager();

		// In order to be shared, working copies have to denote the same translation unit 
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map sharedWorkingCopies = CModelManager.getDefault().sharedWorkingCopies;
	
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null) return null;
		return (FortranWorkingCopy)perFactoryWorkingCopies.get(this);
	}

	/**
	 * To be removed with the new model builder in place
	 * @param newElements
	 * @param element
	 */
	private void getNewElements(Map mapping, CElement element){
		Object info = null;
		try {
			info = element.getElementInfo();
		} catch (CModelException e) {
		}
		if(info != null){
			if(element instanceof IParent){
				ICElement[] children = ((CElementInfo)info).getChildren();
				int size = children.length;
				for (int i = 0; i < size; ++i) {
					CElement child = (CElement) children[i];
					getNewElements(mapping, child);		
				}		
			}
		}
		mapping.put(element, info);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#buildStructure(org.eclipse.cdt.internal.core.model.OpenableInfo, org.eclipse.core.runtime.IProgressMonitor, java.util.Map, org.eclipse.core.resources.IResource)
	 */
	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws CModelException {
		TranslationUnitInfo unitInfo = (TranslationUnitInfo) info;

		// We reuse the general info cache in the CModelBuilder, We should not do this
		// and instead create the info explicitely(see JDT).
		// So to get by we need to remove in the LRU all the info of this handle
		CModelManager.getDefault().removeChildrenInfo(this);

		// generate structure
		this.parse(newElements); 
		
		///////////////////////////////////////////////////////////////
		
		if (isWorkingCopy()) {
			ITranslationUnit original =  ((IWorkingCopy)this).getOriginalElement();
			// might be IResource.NULL_STAMP if original does not exist
			IResource r = original.getResource();
			if (r != null && r instanceof  IFile) {
				unitInfo.fTimestamp = ((IFile) r).getModificationStamp();
			}
		}
		
		return unitInfo.isStructureKnown();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getContents()
	 */
	public char[] getContents() {
		try {
			IBuffer buffer = this.getBuffer();
			return buffer == null ? null : buffer.getCharacters();
		} catch (CModelException e) {
			return new char[0];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getSharedWorkingCopy(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.cdt.internal.core.model.IBufferFactory)
	 */
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,IBufferFactory factory)
		throws CModelException {
		return getSharedWorkingCopy(monitor, factory, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getSharedWorkingCopy(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.cdt.internal.core.model.IBufferFactory, org.eclipse.cdt.core.model.IProblemRequestor)
	 */
	public IWorkingCopy getSharedWorkingCopy(IProgressMonitor monitor,IBufferFactory factory, IProblemRequestor requestor)
		throws CModelException {
	
		// if factory is null, default factory must be used
		if (factory == null) factory = BufferManager.getDefaultBufferManager();

		CModelManager manager = CModelManager.getDefault();
	
		// In order to be shared, working copies have to denote the same translation unit 
		// AND use the same buffer factory.
		// Assuming there is a little set of buffer factories, then use a 2 level Map cache.
		Map sharedWorkingCopies = manager.sharedWorkingCopies;
	
		Map perFactoryWorkingCopies = (Map) sharedWorkingCopies.get(factory);
		if (perFactoryWorkingCopies == null){
			perFactoryWorkingCopies = new HashMap();
			sharedWorkingCopies.put(factory, perFactoryWorkingCopies);
		}
		FortranWorkingCopy workingCopy = (FortranWorkingCopy)perFactoryWorkingCopies.get(this);
		if (workingCopy != null) {
			workingCopy.useCount++;
			return workingCopy;
		}
		CreateWorkingCopyOperation op = new CreateWorkingCopyOperation(this, perFactoryWorkingCopies, factory, requestor);
		op.runOperation(monitor);
		return (IWorkingCopy)op.getResultElements()[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getWorkingCopy()
	 */
	public IWorkingCopy getWorkingCopy()throws CModelException{
		return this.getWorkingCopy(null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getWorkingCopy(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.cdt.internal.core.model.IBufferFactory)
	 */
	public IWorkingCopy getWorkingCopy(IProgressMonitor monitor, IBufferFactory factory)throws CModelException{
		FortranWorkingCopy workingCopy = new FortranWorkingCopy(getParent(), getFile(), getContentTypeId(), factory);
		// open the working copy now to ensure contents are that of the current state of this element
		workingCopy.open(monitor);
		return workingCopy;
	}

	/**
	 * Returns true if this element may have an associated source buffer.
	 */
	protected boolean hasBuffer() {
		return true;
	}

	/*
	 * @see Openable#openParent
	 */
	protected void openParent(Object childInfo, Map newElements, IProgressMonitor pm) throws CModelException {
		try {
			super.openParent(childInfo, newElements, pm);
		} catch(CModelException e){
			// allow parent to not exist for working copies defined outside
			if (!isWorkingCopy()){ 
				throw e;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IOpenable#isConsistent()
	 */
	public boolean isConsistent() throws CModelException {
		return CModelManager.getDefault().getElementsOutOfSynchWithBuffers().get(this) == null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#isSourceElement()
	 */
	protected boolean isSourceElement() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isWorkingCopy()
	 */
	public boolean isWorkingCopy() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#openBuffer(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IBuffer openBuffer(IProgressMonitor pm) throws CModelException {

		// create buffer -  translation units only use default buffer factory
		BufferManager bufManager = getBufferManager();		
		IBuffer buffer = getBufferFactory().createBuffer(this);
		if (buffer == null) 
			return null;
	
		// set the buffer source
		if (buffer.getCharacters() == null){
			IResource file = this.getResource();
			if (file != null && file.getType() == IResource.FILE) {
				buffer.setContents(Util.getResourceContentsAsCharArray((IFile)file));
			}
		}

		// add buffer to buffer cache
		bufManager.addBuffer(buffer);
			
		// listen to buffer changes
		buffer.addBufferChangedListener(this);
	
		return buffer;
	}

	public Map parse() {
		Map map = new HashMap();
		try {
			getNewElements(map, this);
		} catch (Exception e) {
		}
		return map;
	}

	/**
	 * Parse the buffer contents of this element.
	 */
	private void parse(Map newElements){
		try {
			FortranModelBuilder modelBuilder = new FortranModelBuilder(this, newElements);
			boolean quickParseMode = ! (CCorePlugin.getDefault().useStructuralParseMode());
			modelBuilder.parse(quickParseMode);
		} catch (Exception e) {
			// use the debug log for this exception.
			Util.debugLog( "Exception in CModelBuilder", IDebugLogConstants.MODEL);  //$NON-NLS-1$
		}							
	}
	
	public IProblemRequestor getProblemRequestor() {
		return problemRequestor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isHeaderUnit()
	 */
	public boolean isHeaderUnit() {
		return (
				CCorePlugin.CONTENT_TYPE_CHEADER.equals(fContentTypeID)
				|| CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(fContentTypeID)
				);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isSourceUnit()
	 */
	public boolean isSourceUnit() {
		return (
				CCorePlugin.CONTENT_TYPE_CSOURCE.equals(fContentTypeID)
				|| CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(fContentTypeID)
				|| CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(fContentTypeID)
				);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isCLanguage()
	 */
	public boolean isCLanguage() {
		return (
				CCorePlugin.CONTENT_TYPE_CSOURCE.equals(fContentTypeID)
				|| CCorePlugin.CONTENT_TYPE_CHEADER.equals(fContentTypeID)
				);
	}
	
	/**
	 * True if the code is Fortran (this is only temporary until AdditionalLanguages extension pt works (CER)
	 * @return
	 */
	public boolean isFortranLanguage() {
		return fContentTypeID.equals("org.eclipse.fdt.core.fortranSource");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isCXXLanguage()
	 */
	public boolean isCXXLanguage() {
		return (
				CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(fContentTypeID)
				|| CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(fContentTypeID)
				);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#isASMLanguage()
	 */
	public boolean isASMLanguage() {
		return CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(fContentTypeID);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	public boolean exists() {
		IResource res = getResource();
		if (res != null)
			return res.exists();
		return super.exists();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getASTTranslationUnit()
	 */
	public IASTTranslationUnit getASTTranslationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ITranslationUnit#getContentTypeId()
	 */
	public String getContentTypeId() {
		return fContentTypeID;
	}

	protected void setContentTypeID(String id) {
		fContentTypeID = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.Openable#closing(java.lang.Object)
	 */
	public void closing(Object info) throws CModelException {
		IContentType cType = CCorePlugin.getContentType(getCProject().getProject(), getElementName());
		if (cType != null) {
			setContentTypeID(cType.getId());
		}
		super.closing(info);
	}
}
