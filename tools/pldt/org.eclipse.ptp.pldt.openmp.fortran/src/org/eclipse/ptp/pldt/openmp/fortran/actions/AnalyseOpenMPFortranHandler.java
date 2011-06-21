/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.fortran.actions;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.photran.internal.cdtinterface.core.FortranLanguage;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.openmp.fortran.analysis.OpenMPFortranASTVisitor;

/**
 * @since 4.0
 */
public class AnalyseOpenMPFortranHandler {
	public void run(String languageID, ITranslationUnit tu, String fileName, ScanReturn msr) {
		if (languageID.equals(FortranLanguage.LANGUAGE_ID)) {
			IResource res = tu.getUnderlyingResource();
			if (!(res instanceof IFile))
				throw new IllegalStateException();
			IFile file = (IFile) res;

			try {
				ASTExecutableProgramNode ast = new Parser().parse(new ASTLexerFactory().createLexer(file));
				ast.accept(new OpenMPFortranASTVisitor(fileName, msr));
			} catch (Exception e) {
				e.printStackTrace(); // TODO
			}
		}
	}
}
