/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Photran modifications
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.photran.internal.core.preprocessor.c;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.parser.CodeReader;

/**
 * Instructs the preprocessor on how to handle a file-inclusion.
 * @since 5.0
 */
public class IncludeFileContent {
	public enum InclusionKind {
		/**
		 * Instruct the preprocessor to skip this inclusion. 
		 */
		SKIP_FILE,
		/**
		 * The file and its dependents are indexed, required information is read
		 * from there.
		 */
		FOUND_IN_INDEX,
		/**
		 * The file has to be scanned, a code reader is provided.
		 */
		USE_CODE_READER
	}

	private final InclusionKind fKind;
	private final CodeReader fCodeReader;
	private final List<IIndexMacro> fMacroDefinitions;
	private final List<ICPPUsingDirective> fUsingDirectives;
	private final String fFileLocation;
	private List<IIndexFile> fFiles;
	
	/**
	 * For skipping include files.
	 * @param fileLocation the location of the file.
	 * @param kind must be {@link InclusionKind#SKIP_FILE}.
	 * @throws IllegalArgumentException if fileLocation is <code>null</code> or the kind value is illegal for
	 * this constructor.
	 */
	public IncludeFileContent(String fileLocation, InclusionKind kind) throws IllegalArgumentException {
		if (fileLocation == null || kind != InclusionKind.SKIP_FILE) {
			throw new IllegalArgumentException();
		}
		fKind= kind;
		fFileLocation= fileLocation;
		fMacroDefinitions= null;
		fUsingDirectives= null;
		fCodeReader= null;
	}

	/**
	 * For reading include files from disk.
	 * @param codeReader the code reader for the inclusion.
	 * @throws IllegalArgumentException in case the codeReader or its location is <code>null</code>.
	 */
	public IncludeFileContent(CodeReader codeReader) throws IllegalArgumentException {
		if (codeReader == null) {
			throw new IllegalArgumentException();
		}
		fKind= InclusionKind.USE_CODE_READER;
		fFileLocation= codeReader.getPath();
		fCodeReader= codeReader;
		fMacroDefinitions= null;
		fUsingDirectives= null;
		if (fFileLocation == null) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * For using information about an include file from the index.
	 * @param fileLocation the location of the file
	 * @param macroDefinitions a list of macro definitions
	 * @param files 
	 * @throws IllegalArgumentException in case the fileLocation or the macroDefinitions are <code>null</code>.
	 */
	public IncludeFileContent(String fileLocation, List<IIndexMacro> macroDefinitions, List<ICPPUsingDirective> usingDirectives,
			List<IIndexFile> files) {
		fKind= InclusionKind.FOUND_IN_INDEX;
		fFileLocation= fileLocation;
		fCodeReader= null;
		fUsingDirectives= usingDirectives;
		fMacroDefinitions= macroDefinitions;
		fFiles= files;
	}

	/**
	 * @return the kind
	 */
	public InclusionKind getKind() {
		return fKind;
	}

	/**
	 * Returns the location of the file to be included.
	 */
	public String getFileLocation() {
		return fFileLocation;
	}

	/**
	 * Valid with {@link InclusionKind#USE_CODE_READER}.
	 * @return the codeReader or <code>null</code> if kind is different to {@link InclusionKind#USE_CODE_READER}.
	 */
	public CodeReader getCodeReader() {
		return fCodeReader;
	}

	/**
	 * Valid with {@link InclusionKind#FOUND_IN_INDEX}.
	 * @return the macroDefinitions or <code>null</code> if kind is different to {@link InclusionKind#FOUND_IN_INDEX}.
	 */
	public List<IIndexMacro> getMacroDefinitions() {
		return fMacroDefinitions;
	}

	/**
	 * Valid with {@link InclusionKind#FOUND_IN_INDEX}.
	 * @return the usingDirectives or <code>null</code> if kind is different to {@link InclusionKind#FOUND_IN_INDEX}.
	 */
	public List<ICPPUsingDirective> getUsingDirectives() {
		return fUsingDirectives;
	}

	/**
	 * Valid with {@link InclusionKind#FOUND_IN_INDEX}.
	 * @return the files included or <code>null</code> if kind is different to {@link InclusionKind#FOUND_IN_INDEX}.
	 */
	public List<IIndexFile> getFilesIncluded() {
		return fFiles;
	}
}
