/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Martin Oberhuber (Wind River Systems) - bug 155096
 *     Gerhard Schaber (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.internal.ptp.rdt.managedbuilder.gnu.ui.scannerdiscovery;

import java.util.ArrayList;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCPerFileBOPConsoleParserUtility;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.KVStringPair;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SCDOptionsEnum;
import org.eclipse.cdt.make.xlc.core.scannerconfig.util.XLCCommandDSC;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;



public class RemoteGCCPerFileBOPConsoleParserUtility extends
		GCCPerFileBOPConsoleParserUtility {

	private String fDefaultMacroDefinitionValue= "1"; //$NON-NLS-1$
	public RemoteGCCPerFileBOPConsoleParserUtility(IProject project,
			IPath workingDirectory, IMarkerGenerator markerGenerator) {
		super(project, workingDirectory, markerGenerator);
		// TODO Auto-generated constructor stub
	}
	
	/**
     * @return Returns the project.
     */
    public IProject getProject() {
        return super.getProject();
    }
	
	 /**
     * @return CCommandDSC compile command description 
     */
    public CCommandDSC getNewCCommandDSC(String[] tokens, final int idxOfCompilerCommand, boolean cppFileType) {
		ArrayList<KVStringPair> dirafter = new ArrayList<KVStringPair>();
		ArrayList<String> includes = new ArrayList<String>();
		CCommandDSC command = new XLCCommandDSC(cppFileType, getProject());
        command.addSCOption(new KVStringPair(SCDOptionsEnum.COMMAND.toString(), tokens[idxOfCompilerCommand]));
        for (int i = idxOfCompilerCommand+1; i < tokens.length; ++i) {
        	String token = tokens[i];
        	//Target specific options: see GccScannerInfoConsoleParser
			if (token.startsWith("-m") ||		//$NON-NLS-1$
				token.startsWith("--sysroot") || //$NON-NLS-1$
				token.equals("-ansi") ||		//$NON-NLS-1$
				token.equals("-posix") ||		//$NON-NLS-1$
				token.equals("-pthread") ||		//$NON-NLS-1$
				token.startsWith("-O") ||		//$NON-NLS-1$
				token.equals("-fno-inline") ||	//$NON-NLS-1$
				token.startsWith("-finline") ||	//$NON-NLS-1$
				token.equals("-fno-exceptions") ||	//$NON-NLS-1$
				token.equals("-fexceptions") ||		//$NON-NLS-1$
				token.equals("-fshort-wchar") ||	//$NON-NLS-1$
				token.equals("-fshort-double") ||	//$NON-NLS-1$
				token.equals("-fno-signed-char") ||	//$NON-NLS-1$
				token.equals("-fsigned-char") ||	//$NON-NLS-1$
				token.startsWith("-fabi-version=")	//$NON-NLS-1$
			) {		
		        command.addSCOption(new KVStringPair(SCDOptionsEnum.COMMAND.toString(), token));
				continue;
        	}
            for (int j = SCDOptionsEnum.MIN; j <= SCDOptionsEnum.MAX; ++j) {
                final SCDOptionsEnum optionKind = SCDOptionsEnum.getSCDOptionsEnum(j);
				if (token.startsWith(optionKind.toString())) {
                    String option = token.substring(
                            optionKind.toString().length()).trim();
                    if (option.length() > 0) {
                        // ex. -I/dir
                    }
                    else if (optionKind.equals(SCDOptionsEnum.IDASH)) {
                        for (String inc : includes) {
                            option = inc;
                            KVStringPair pair = new KVStringPair(SCDOptionsEnum.IQUOTE.toString(), option);
                            command.addSCOption(pair);
                        }
                        includes = new ArrayList<String>();
                        // -I- has no parameter
                    }
                    else {
                        // ex. -I /dir
                        // take a next token
                        if (i+1 < tokens.length && !tokens[i+1].startsWith("-")) { //$NON-NLS-1$
                            option = tokens[++i];
                        }
                        else break;
                    }
                    
                    if (option.length() > 0 && (
                            optionKind.equals(SCDOptionsEnum.INCLUDE) ||
                            optionKind.equals(SCDOptionsEnum.INCLUDE_FILE) ||
                            optionKind.equals(SCDOptionsEnum.IMACROS_FILE) ||
                            optionKind.equals(SCDOptionsEnum.IDIRAFTER) ||
                            optionKind.equals(SCDOptionsEnum.ISYSTEM) || 
                            optionKind.equals(SCDOptionsEnum.IQUOTE) )) {
                        option = (getAbsolutePath(option)).toString();
                    }
                    
                    if (optionKind.equals(SCDOptionsEnum.IDIRAFTER)) {
                        KVStringPair pair = new KVStringPair(SCDOptionsEnum.INCLUDE.toString(), option);
                    	dirafter.add(pair);
                    }
                    else if (optionKind.equals(SCDOptionsEnum.INCLUDE)) {
                    	includes.add(option);
                    }
                    else { // add the pair
                    	if (optionKind.equals(SCDOptionsEnum.DEFINE)) {
                        	if (option.indexOf('=') == -1) {
                        		option += '='+ fDefaultMacroDefinitionValue;
                        	}
                    	}
                        KVStringPair pair = new KVStringPair(optionKind.toString(), option);
                    	command.addSCOption(pair);
                    }
                    break;
                }
            }
        }
        for (String option : includes) {
            KVStringPair pair = new KVStringPair(SCDOptionsEnum.INCLUDE.toString(), option);
            command.addSCOption(pair);
        }
        for (KVStringPair kvStringPair : dirafter) {
            command.addSCOption(kvStringPair);
        }
        return command;
    }

}
