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

import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.fdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.fdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.fdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.fdt.core.dom.ast.IASTProblem;

/**
 * @author jcamelon
 */
public interface ILocationResolver {
    
	public IASTPreprocessorMacroDefinition [] getMacroDefinitions();
	public IASTPreprocessorIncludeStatement [] getIncludeDirectives();
	public IASTPreprocessorStatement [] getAllPreprocessorStatements();

	public IASTNodeLocation [] getLocations( int offset, int length );
    
    public char [] getUnpreprocessedSignature( IASTNodeLocation [] locations );
    
    public IASTProblem[] getScannerProblems();

    public String getTranslationUnitPath();
    
    public void cleanup();

    public IASTNode getPreprocessorNode( String path, int offset, int length ) throws InvalidPreprocessorNodeException;
    
}
