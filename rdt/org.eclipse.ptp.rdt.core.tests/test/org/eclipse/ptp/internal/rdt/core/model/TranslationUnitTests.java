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

package org.eclipse.ptp.internal.rdt.core.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;

@SuppressWarnings("restriction")
public class TranslationUnitTests extends TestCase {
	
	IASTTranslationUnit parse(String contents) throws CoreException, URISyntaxException, IOException {
		File file = File.createTempFile("test" + System.currentTimeMillis(), ".cpp");  //$NON-NLS-1$//$NON-NLS-2$
		FileWriter writer = new FileWriter(file);
		try {
			writer.write(contents);
		} finally {
			writer.close();
		}
		file.deleteOnExit();
		
		TranslationUnit unit = new TranslationUnit(null, file.getName(), "test", new URI("file:///" + file.getAbsolutePath().replace('\\', '/'))); //$NON-NLS-1$
		IScannerInfo scannerInfo = new IScannerInfo() {
			public Map<String, String> getDefinedSymbols() {
				return Collections.emptyMap();
			}

			public String[] getIncludePaths() {
				return new String[0];
			}
		};
		unit.setLanguage(GPPLanguage.getDefault());
		unit.setASTContext(scannerInfo, Collections.<String,String>emptyMap());
		return unit.getAST();
	}
	
	public void testParseEmptyAST() throws Exception {
		IASTTranslationUnit ast = parse(""); //$NON-NLS-1$
		assertNotNull(ast);
	}

	
	public void testParseSimpleAST() throws Exception {
		IASTTranslationUnit ast = parse("int x;"); //$NON-NLS-1$
		assertNotNull(ast);
		List<IASTName> names = getNames(ast);
		assertEquals(1, names.size());
		assertEquals("x", names.get(0).toString()); //$NON-NLS-1$
	}
	
	public List<IASTName> getNames(IASTTranslationUnit unit) {
		final List<IASTName> names = new ArrayList<IASTName>();
		ASTVisitor visitor = new ASTVisitor() {
			@Override
			public int visit(IASTName name) {
				names.add(name);
				return ASTVisitor.PROCESS_CONTINUE;
			}
		};
		visitor.shouldVisitNames = true;
		unit.accept(visitor);
		return names;
	}
}
