/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Photran modifications
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.preprocessor.c;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ParserFactory;

/**
 * @author jcamelon
 */
public class InternalParserUtil extends ParserFactory {

	public static CodeReader createFileReader(String finalPath) {
		File includeFile = new File(finalPath);
		if (includeFile.isFile()) {
			try {
			    //use the canonical path so that in case of non-case-sensitive OSs
			    //the CodeReader always has the same name as the file on disk with
			    //no differences in case.
				return new CodeReader(includeFile.getCanonicalPath());
			} catch (IOException e) {
			}
		}
		return null;
	}
}
