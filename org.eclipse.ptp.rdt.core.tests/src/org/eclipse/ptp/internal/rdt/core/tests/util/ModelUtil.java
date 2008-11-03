/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.tests.util;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.model.CModelBuilder2;
import org.eclipse.ptp.internal.rdt.core.model.TranslationUnit;

public class ModelUtil {
	public static IASTTranslationUnit buildAST(ILanguage language, String name, String code) throws CoreException {
		CodeReader reader = new CodeReader(name, code.toCharArray());
		IScannerInfo scanInfo = new IScannerInfo() {
			public Map<String, String> getDefinedSymbols() {
				return Collections.emptyMap();
			}

			public String[] getIncludePaths() {
				return new String[0];
			}
		};
		ICodeReaderFactory fileCreator = new ICodeReaderFactory() {
			public CodeReader createCodeReaderForInclusion(String path) {
				return null;
			}

			public CodeReader createCodeReaderForTranslationUnit(String path) {
				return null;
			}

			public ICodeReaderCache getCodeReaderCache() {
				return null;
			}

			public int getUniqueIdentifier() {
				return 0;
			}
		};
		IIndex index = null;
		IParserLogService log = new DefaultLogService();
		return language.getASTTranslationUnit(reader, scanInfo, fileCreator, index, log);
	}
	
	public static TranslationUnit buildModel(ILanguage language, String name, String code) throws CoreException, DOMException {
		TranslationUnit translationUnit = new TranslationUnit(null, name, null, null);
		CModelBuilder2 builder = new CModelBuilder2(translationUnit, new NullProgressMonitor());
		IASTTranslationUnit ast = buildAST(language, name, code);
		builder.parse(ast);
		return translationUnit;
	}

	@SuppressWarnings("unchecked")
	public static <T> T reconstitute(T object) throws IOException, ClassNotFoundException {
		String serialized = Serializer.serialize(object);
		Object reconstituted = Serializer.deserialize(serialized);
		return (T) reconstituted;
	}

	public static IASTCompletionNode findCompletionNode(ILanguage language, String name, String code, int offset) throws CoreException {
		CodeReader reader = new CodeReader(name, code.toCharArray());
		IScannerInfo scanInfo = new IScannerInfo() {
			public Map<String, String> getDefinedSymbols() {
				return Collections.emptyMap();
			}

			public String[] getIncludePaths() {
				return new String[0];
			}
		};
		ICodeReaderFactory fileCreator = new ICodeReaderFactory() {
			public CodeReader createCodeReaderForInclusion(String path) {
				return null;
			}

			public CodeReader createCodeReaderForTranslationUnit(String path) {
				return null;
			}

			public ICodeReaderCache getCodeReaderCache() {
				return null;
			}

			public int getUniqueIdentifier() {
				return 0;
			}
		};
		IIndex index = null;
		IParserLogService log = new DefaultLogService();
		return language.getCompletionNode(reader, scanInfo, fileCreator, index, log, offset);
	}
}
