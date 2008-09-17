/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
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
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.miners.StandaloneSavedCodeReaderFactory;

public class TranslationUnit extends Parent implements ITranslationUnit {
	private static final long serialVersionUID = 1L;

	Class<? extends ILanguage> fLanguageClass;
	transient protected ILanguage fLanguage;

	private IScannerInfo fScannerInfo;
	
	public TranslationUnit(ICElement parent, String name, String projectName) {
		super(parent, ICElement.C_UNIT, name);
		if (projectName != null) {
			setCProject(new CProject(projectName));
		}
		
		else {
			throw new IllegalArgumentException();
		}
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

	public void setASTContext(IScannerInfo scannerInfo) {
		fScannerInfo = scannerInfo;
	}
	
	public void setLanguage(ILanguage language) {
		fLanguage = language;
		if (language == null) {
			fLanguageClass = null;
		} else {
			fLanguageClass = language.getClass();
		}
	}

	public IASTTranslationUnit getAST(IIndex index, int style) throws CoreException {
		checkState();
		if (fLanguage != null && fScannerInfo != null && fLocation != null) {
			IParserLogService log = new DefaultLogService();
			ICodeReaderFactory fileCreator = StandaloneSavedCodeReaderFactory.getInstance();
			String filePath = fLocation.getPath();
			int options = style | getParserOptions(filePath);
			CodeReader reader = getCodeReader();
			return fLanguage.getASTTranslationUnit(reader, fScannerInfo, fileCreator, index, options, log);
		}
		return null;
	}

	public CodeReader getCodeReader() {
		if (fLocation != null) {
			String filePath = fLocation.getPath();
			try {
				return new CodeReader(filePath);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return null;
	}

	public IASTCompletionNode getCompletionNode(IIndex index, int style, int offset) throws CoreException {
		checkState();
		if (fLanguage != null && fScannerInfo != null && fLocation != null) {
			IParserLogService log = new DefaultLogService();
			ICodeReaderFactory fileCreator = StandaloneSavedCodeReaderFactory.getInstance();
			CodeReader reader = getCodeReader();
			return fLanguage.getCompletionNode(reader, fScannerInfo, fileCreator, index, log, offset);
		}
		return null;
	}

	public String getContentTypeId() {
		// TODO Auto-generated method stub
		return null;
	}

	public char[] getContents() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	public IInclude[] getIncludes() throws CModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ILanguage getLanguage() throws CoreException {
		checkState();
		return fLanguage;
	}

	public IPath getLocation() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSourceUnit() {
		// TODO Auto-generated method stub
		return false;
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
	
	private void checkState() {
		if (fLanguage == null && fLanguageClass != null) {
			try {
				fLanguage = fLanguageClass.newInstance();
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
	
	public int getParserOptions(String filePath) {
		if (filePath.endsWith(".cpp") || filePath.endsWith(".C") || filePath.endsWith(".c")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return ILanguage.OPTION_IS_SOURCE_UNIT;
		}
		return 0;
	}
}
