/**********************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.parser.ISignificantMacros;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author pazel
 * 
 */
@SuppressWarnings("restriction")
public class PASTInclusionStatement extends PASTNode implements IASTPreprocessorIncludeStatement {
	protected IASTPreprocessorIncludeStatement incl_ = null;

	/**
	 * PASTInclusionStatement - constructor
	 * 
	 * @param incl
	 *            : ASTInclusionStatement
	 */
	public PASTInclusionStatement(IASTPreprocessorIncludeStatement incl) {
		super((ASTNode) incl);
		incl_ = incl;
	}

	/**
	 * @since 4.1
	 */
	@Override
	public IASTNode copy(CopyStyle style) {
		return incl_.copy(style);
	}

	@Override
	public String getType() {
		return "#include"; //$NON-NLS-1$
	}

	@Override
	public String getPath() {
		return incl_.getPath();
	}

	// cdt40
	@Override
	public IASTName getName() {
		return incl_.getName();
	}

	// cdt40
	@Override
	public boolean isSystemInclude() {
		return incl_.isSystemInclude();
	}

	// cdt40
	@Override
	public boolean isActive() {
		return incl_.isActive();
	}

	// cdt40
	@Override
	public boolean isResolved() {
		return incl_.isResolved();
	}

	/**
	 * CDT 6.0 implement isPartOfTranslationUnitFile()
	 * 
	 * @return
	 */
	@Override
	public boolean isPartOfTranslationUnitFile() {
		return incl_.isPartOfTranslationUnitFile();
	}

	/**
	 * CDT 6.0 implement IASTPreprocessorIncludeStatement.isResolvedByHeuristics()
	 */
	@Override
	public boolean isResolvedByHeuristics() {
		return false;

	}

	/**
	 * CDT 8.1 implement
	 */
	@Override
	public ISignificantMacros getSignificantMacros() throws CoreException {
		return incl_.getSignificantMacros();
	}

	@Override
	public boolean hasPragmaOnceSemantics() throws CoreException {
		return incl_.hasPragmaOnceSemantics();
	}

	@Override
	public ISignificantMacros[] getLoadedVersions() {
		return incl_.getLoadedVersions();
	}

	@Override
	public boolean createsAST() {
		return incl_.createsAST();
	}

	@Override
	public IIndexFile getImportedIndexFile() {
		return incl_.getImportedIndexFile();
	}

	@Override
	public IASTNode getOriginalNode() {
		return incl_.getOriginalNode();
	}

	@Override
	public long getIncludedFileTimestamp() {
		return incl_.getIncludedFileTimestamp();
	}

	@Override
	public long getIncludedFileSize() {
		return incl_.getIncludedFileSize();
	}

	@Override
	public long getIncludedFileContentsHash() {
		return incl_.getIncludedFileContentsHash();
	}

	@Override
	public boolean isErrorInIncludedFile() {
		return incl_.isErrorInIncludedFile();
	}

	@Override
	public long getIncludedFileReadTime() {
		return incl_.getIncludedFileReadTime();
	}

	@Override
	public boolean isIncludedFileExported() {
		// TODO Auto-generated method stub
		return false;
	}

}
