/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.fdt.internal.core.parser.scanner2;

import org.eclipse.fdt.core.dom.ast.IASTProblem;
import org.eclipse.fdt.core.parser.CodeReader;

/**
 * @author jcamelon
 */
public interface IScannerPreprocessorLog {

    public void startTranslationUnit(CodeReader tu_reader);

    public void endTranslationUnit(int offset);

    public void startInclusion(CodeReader reader, int offset, int endOffset);

    public void endInclusion(int offset);

    public void enterObjectStyleMacroExpansion(char[] name, char[] expansion,
            int offset);

    public void exitObjectStyleMacroExpansion(char[] name, int offset);

    public void enterFunctionStyleExpansion(char[] name, char[][] parameters,
            char[] expansion, int offset);

    public void exitFunctionStyleExpansion(char[] name, int offset);

    public void defineObjectStyleMacro(ObjectStyleMacro m, int startOffset,
            int nameOffset, int nameEndOffset, int endOffset);

    public void defineFunctionStyleMacro(FunctionStyleMacro m, int startOffset,
            int nameOffset, int nameEndOffset, int endOffset);

    public void encounterPoundIf(int startOffset, int endOffset, boolean taken);

    public void encounterPoundIfdef(int startOffset, int endOffset,
            boolean taken);

    public void encounterPoundIfndef(int startOffset, int endOffset,
            boolean taken);

    public void encounterPoundElse(int startOffset, int endOffset, boolean taken);

    public void encounterPoundElif(int startOffset, int endOffset, boolean taken);

    public void encounterPoundEndIf(int startOffset, int endOffset);

    public void encounterPoundPragma(int startOffset, int endOffset);

    public void encounterPoundError(int startOffset, int endOffset);

    public void encounterPoundUndef(int startOffset, int endOffset);

    public void encounterProblem(IASTProblem problem);
}