/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.preprocessor.c;

/**
 * A Token class used for include directives. This class keeps track
 * of the file path that is included.
 * 
 * @author Matthew Michelotti
 */
public class TokenForInclude extends TokenWithImage {
	private String fIncludeFilePath = null;

	public TokenForInclude(int kind, Object source, int offset, int endOffset, char[] image) {
		super(kind, source, offset, endOffset, image);
	}

    public TokenForInclude(int kind, Object source, int offset, int endOffset, char[] image, char[] preWhiteSpace) {
        super(kind, source, offset, endOffset, image, preWhiteSpace);
    }
    
    @Override
    public String getIncludeFile() {
    	return fIncludeFilePath;
    }
    
    public void setIncludeFile(String includeFilePath) {
    	fIncludeFilePath = includeFilePath;
    }
}
