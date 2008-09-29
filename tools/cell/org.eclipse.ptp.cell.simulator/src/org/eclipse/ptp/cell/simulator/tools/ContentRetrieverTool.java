/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ContentRetrieverTool {

	public static void copyStreamContent(OutputStream outputStream, InputStream inputStream) throws IOException {
		byte buffer[] = new byte[1000];
		int numRead = 0;
		while (true) {
			numRead = inputStream.read(buffer);
			if (numRead == -1) {
				break;
			}
			outputStream.write(buffer, 0, numRead);
		}
	}

	public static String readStreamContent(InputStreamReader inputReader) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(inputReader);
		String result = ""; //$NON-NLS-1$
		while (true) {
			String line = bufferedReader.readLine();
			if (line == null) {
				break;
			} else {
				result += line + '\n';
			}
		}
		return result;
	}

}
