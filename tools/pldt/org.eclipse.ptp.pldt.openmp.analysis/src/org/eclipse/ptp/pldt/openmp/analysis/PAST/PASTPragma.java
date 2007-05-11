/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTPragma;
import org.eclipse.ptp.pldt.openmp.analysis.Utility;

/**
 * Pseudo node for IAST pragma nodes
 * @author pazel
 *
 */
public class PASTPragma extends PASTNode implements IASTPreprocessorPragmaStatement
{
	protected ASTPragma pragma_ = null;
    protected String    content_ = "";
	
    /**
     * PASTPragma - constructor
     * @param pragma
     */
	public PASTPragma(ASTPragma pragma)
	{
		super(pragma);
		pragma_ = pragma;
	}
    
    /**
     * PASTPragma - Copy constructor
     * @param pPragma: PASTPragma
     */
    public PASTPragma(PASTPragma pPragma)
    {
        super(pPragma);
        pragma_  = pPragma.getASTPragma();
        content_ = pPragma.getContent();
    }
    
    protected ASTPragma getASTPragma()
    {
        return pragma_;
    }
    
    public String getType()
    {
        return "#pragma";
    }

	/**
	 * *NOTE* must get Don's CDT parser changes
	 * for this to compile successfully!
	 * 
	 * @return
	 */
	public String getContent()
	{
		//return pragma_.getContent();
		return content_;
	}
    
    /**
     * getOffset - relative to the AST, i.e. include files factored into offset
     * @return int
     */
    public int getOffset()
    {
        return pragma_.getOffset();
    }
    
    /**
     * getLength - get the length of the pragma 
     * @return int
     */
    public int getLength()
    {
        return pragma_.getLength();
    }
    
    /**
     * getLocalOffset - relative to containing file, i.e. include files factored out offset
     * @return int
     */
    public int getLocalOffset()
    {
        Utility.Location l = Utility.getLocation(pragma_);
        return l.low_;
    }
    
    public void setContent(String content) { content_ = content; }

	public char[] getMessage() {
		return pragma_.getMessage();
	}
	

}
