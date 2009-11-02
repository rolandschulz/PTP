/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.cdtinterface.dom;

import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.runtime.CoreException;

/**
 * This interface must be implemented by any Fortran DOM parser.
 * <p>
 * See the org.eclipse.photran.cdtinterface.domparser extension point.
 * <p>
 * A DOM parser would parse a Fortran file and return an abstract syntax tree that is compatible
 * with CDT's DOM.  Photran does not, by default, provide a DOM parser; it uses its own abstract
 * syntax tree (see the VPG plug-ins).  This interface and extension point are provided in case
 * a CDT-compliant DOM becomes available in the future.  (A Fortran DOM parser was prototyped at IBM
 * Research but, at the time of writing, is incomplete and not publicly available.)
 * 
 * @author Jeff Overbey
 */
public interface IFortranDOMParser
{
    /** @see ILanguage#getASTTranslationUnit(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, IParserLogService) */
    IASTTranslationUnit getASTTranslationUnit(CodeReader reader,
        IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
        IIndex index, IParserLogService log) throws CoreException;

    /** @see ILanguage#getCompletionNode(CodeReader, IScannerInfo, ICodeReaderFactory, IIndex, IParserLogService, int) */
    IASTCompletionNode getCompletionNode(CodeReader reader,
        IScannerInfo scanInfo, ICodeReaderFactory fileCreator,
        IIndex index, IParserLogService log, int offset)
        throws CoreException;

    /** @see ILanguage#getSelectedNames(IASTTranslationUnit, int, int) */
    IASTName[] getSelectedNames(IASTTranslationUnit ast, int start, int length);
}
