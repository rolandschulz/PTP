/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.tools.vprof.core.vmon;

import java.io.IOException;

import org.eclipse.cdt.utils.elf.parser.ElfParser;
import org.eclipse.cdt.utils.elf.parser.GNUElfParser;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class test {

	public static ISymbol findNearestSym(IAddress addr, ISymbol[] syms) {
		for (int i = 0; i < syms.length; i++) {
			if (syms[i].getAddress().compareTo(addr) > 0) {
				if (i == 0)
					return syms[0];
				else
					return syms[i];
			}
		}
		
		return syms[syms.length-1];
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IBinaryFile bf;
		IBinaryObject bo;
		IBinaryParser parser = null;
		
		System.out.println(Platform.getExtensionRegistry());

		try {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, "BinaryParser");
			IExtension extension = extensionPoint.getExtension("org.eclipse.cdt.core.ELF");
			if (extension != null) {
				IConfigurationElement element[] = extension.getConfigurationElements();
				for (int i = 0; i < element.length; i++) {
					if (element[i].getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
						parser = (IBinaryParser) element[i].createExecutableExtension("run"); //$NON-NLS-1$
						break;
					}
				}
			} else {
				IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1, CCorePlugin.getResourceString("CCorePlugin.exception.noBinaryFormat"), null); //$NON-NLS-1$
				throw new CoreException(s);
			}
		} catch (CoreException e) {
			System.out.println("could not find parser");
			return;
		}

		Path p = new Path("../test2");

		try {
			bf = parser.getBinary(p);
		} catch (IOException e) {
			System.out.println("could not open binary: " + e.getMessage());
			return;
		}
		
		if (bf.getType() == IBinaryFile.EXECUTABLE) {
			System.out.println("is an executable");
			bo = (IBinaryObject)bf;
			System.out.println("name=" + bo.getName() + " cpu=" + bo.getCPU());
		} else {
			System.out.println("not an executable");
			return;
		}

		VMonFile vf = new VMonFile();
		try {
			vf.Read("../vmon.out");
		} catch (IOException e) {
			System.out.println("could not open file: " + e.getMessage());
			return;
		}
		
		for (int i = 0; i < vf.getData().length; i++) {
			VMonData vd = vf.getData()[i];
			
			System.out.println("event " + vd.getEvent().getEventName() + ": " + vd.getEvent().getEventDescription());

			VMonData.VMonInfo vi[] = vd.getData();
			
			for (int j = 0; j < vi.length; j++) {
				System.out.println(" addr = " + vi[j].address);
				System.out.println(" count = " + vi[j].count);
				
				ISymbol sym = findNearestSym(vi[j].address, bo.getSymbols());
				
				if (sym != null)
					System.out.println(" sym = " + sym.getName());
			}
		}

	}

}
