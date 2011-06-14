/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.jardesc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.SAXException;

/**
 * Returns all the top level classes declared in a file.  This class
 * sloppily returns extra bits as top-level classes like inner classes,
 * comments and string literals that look like class declarations
 * (e.g. the literal, "this string almost has a class inside {" would
 * yield a class called "inside").
 */
public class SloppyElementHandler extends DocumentBackedElementHandler {

	public SloppyElementHandler(String basePath) throws SAXException {
		super(basePath);
	}

	@Override
	protected Set<String> collectTopLevelClasses(String path, String fileName) {
		Set<String> names = new HashSet<String>();
		names.add(fileName.replaceAll("\\.java$", ""));  //$NON-NLS-1$//$NON-NLS-2$
		
		try {
			File sourceFile = new File(path + "/" + fileName); //$NON-NLS-1$
			FileReader reader = new FileReader(sourceFile);
	
			CharBuffer buffer = CharBuffer.allocate((int) sourceFile.length());
			reader.read(buffer);
			buffer.rewind();
			String code = buffer.toString();
			
			// Scan for class declarations using regular expressions.
			// We won't have any context information so we may end up
			// picking up inner classes, and class-declaration-like constructs
			// in comments or literals.
			// In the worst case, we'll end up extracting more classes than we
			// actually want.  Most of the time, those "extra" classes won't
			// actually exist so the impact is minimal.
			
			// This pattern filters out most non-class declarations (i.e. phrases
			// in code/comments that contain the word "class").
			Pattern pattern1 = Pattern.compile(
					".*?" +                      // leading junk //$NON-NLS-1$
					"class\\s+" +                // class declaration //$NON-NLS-1$
					"([A-Za-z_][A-Za-z0-9_]*)" + // identifier //$NON-NLS-1$
					"\\s+?[^{s]*?(?!clas)" +     // ensure we don't gobble up what might be a real class declaration //$NON-NLS-1$
					"\\{(.*?)"                   // everything else //$NON-NLS-1$
					, Pattern.DOTALL);
			
			// This pattern picks up most class declarations but can be obstructed by
			// non-class declarations above.
			Pattern pattern2 = Pattern.compile(
					".*?" +                      // leading junk //$NON-NLS-1$
					"class\\s+" +                // class declaration //$NON-NLS-1$
					"([A-Za-z_][A-Za-z0-9_]*)" + // identifier //$NON-NLS-1$
					"\\s+?[^{]*?" +              // implements/extends clauses //$NON-NLS-1$
					"\\{(.*?)"                   // everything else //$NON-NLS-1$
					, Pattern.DOTALL);
	
			for (Pattern pattern : new Pattern[] { pattern1, pattern2 }) {
				Matcher matcher = pattern.matcher(code);
				while (matcher.matches()) {
					names.add(matcher.group(1));
					String tail = matcher.group(2);
					matcher = pattern.matcher(tail);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return names;
	}
}
