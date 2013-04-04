/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.miners;

import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class RemoteInactiveHighlightingHandler {

	public static String collectInactiveCodePositions(IASTTranslationUnit translationUnit) {

		if (translationUnit == null) {
			return ""; //$NON-NLS-1$
		}

		String fileName = translationUnit.getFilePath();
		if (fileName == null) {
			return ""; //$NON-NLS-1$
		}

		StringBuilder sb = new StringBuilder();

		int inactiveCodeStart = -1;
		boolean inInactiveCode = false;
		Stack<Boolean> inactiveCodeStack = new Stack<Boolean>();

		IASTPreprocessorStatement[] preprocStmts = translationUnit.getAllPreprocessorStatements();

		for (IASTPreprocessorStatement statement : preprocStmts) {
			IASTFileLocation floc = statement.getFileLocation();
			if (floc == null || !fileName.equals(floc.getFileName())) {
				// preprocessor directive is from a different file
				continue;
			}
			if (statement instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement) statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = floc.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement) statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifdefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = floc.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorIfndefStatement) {
				IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement) statement;
				inactiveCodeStack.push(Boolean.valueOf(inInactiveCode));
				if (!ifndefStmt.taken()) {
					if (!inInactiveCode) {
						inactiveCodeStart = floc.getNodeOffset();
						inInactiveCode = true;
					}
				}
			} else if (statement instanceof IASTPreprocessorElseStatement) {
				IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement) statement;
				if (!elseStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = floc.getNodeOffset();
					inInactiveCode = true;
				} else if (elseStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = floc.getNodeOffset();
					addHighlightPosition(sb, inactiveCodeStart, inactiveCodeEnd, false);
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement) statement;
				if (!elifStmt.taken() && !inInactiveCode) {
					inactiveCodeStart = floc.getNodeOffset();
					inInactiveCode = true;
				} else if (elifStmt.taken() && inInactiveCode) {
					int inactiveCodeEnd = floc.getNodeOffset();
					addHighlightPosition(sb, inactiveCodeStart, inactiveCodeEnd, false);
					inInactiveCode = false;
				}
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				try {
					boolean wasInInactiveCode = inactiveCodeStack.pop().booleanValue();
					if (inInactiveCode && !wasInInactiveCode) {
						int inactiveCodeEnd = floc.getNodeOffset() + floc.getNodeLength();
						addHighlightPosition(sb, inactiveCodeStart, inactiveCodeEnd, true);
					}
					inInactiveCode = wasInInactiveCode;
				} catch (EmptyStackException e) {
				}
			}
		}
		if (inInactiveCode) {
			// handle unterminated #if - http://bugs.eclipse.org/255018
			int inactiveCodeEnd = Integer.MAX_VALUE; // fDocument.getLength();
			addHighlightPosition(sb, inactiveCodeStart, inactiveCodeEnd, true);
		}

		String result = sb.toString();
		if (result.startsWith(",")) { //$NON-NLS-1$
			result = result.substring(1);
		}
		return result;
	}

	private static void addHighlightPosition(StringBuilder sb, int start, int end, boolean inclusive) {
		sb.append(String.format(",%d,%d,%b", start, end, inclusive)); //$NON-NLS-1$
	}
}
