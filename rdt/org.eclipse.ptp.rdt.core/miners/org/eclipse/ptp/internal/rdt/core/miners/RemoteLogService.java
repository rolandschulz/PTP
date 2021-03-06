/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.AbstractParserLogService;
import org.eclipse.cdt.internal.core.parser.ParserMessages;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

public class RemoteLogService extends AbstractParserLogService {
	
	public static final String LOG_TAG = "CDTMiner"; //$NON-NLS-1$
	private DataStore fDataStore;
	private DataElement fStatus;
	private List<String> fProblemBindingMessages = new ArrayList<String>();
	private List<String> fErrorMessages = new ArrayList<String>();

	public RemoteLogService(DataStore dataStore, DataElement status) {
		super();
		fDataStore = dataStore;
		fStatus = status;
		generateErrorMessages();
	}
	
	private void generateErrorMessages() {		
		fErrorMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.inclusionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fErrorMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.definitionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
		
		fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.error", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.warning", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.inclusionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.definitionNotFound", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.invalidMacroDefn", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.invalidMacroRedefn", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.unbalancedConditional", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.conditionalEval", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.macroUsage", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.circularInclusion", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.invalidDirective", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.macroPasting", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.missingRParen", "")); //$NON-NLS-1$ //$NON-NLS-2$       
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.preproc.invalidVaArgs", "")); //$NON-NLS-1$ //$NON-NLS-2$       
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.invalidEscapeChar", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.unboundedString", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.badFloatingPoint", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.badHexFormat", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.badOctalFormat", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.badDecimalFormat", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.assignmentNotAllowed", "")); //$NON-NLS-1$ //$NON-NLS-2$        
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.divideByZero", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.missingRParen", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.expressionSyntaxError", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.illegalIdentifier", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.badConditionalExpression", "")); //$NON-NLS-1$ //$NON-NLS-2$        
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.unexpectedEOF", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ScannerProblemFactory.error.scanner.badCharacter", "")); //$NON-NLS-1$ //$NON-NLS-2$
    	fProblemBindingMessages.add(ParserMessages.getFormattedString("ParserProblemFactory.error.syntax.syntaxError", "")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#errorLog(java.lang.String)
	 */
	@Override
	public void errorLog(String message) {
		UniversalServerUtilities.logError(LOG_TAG, getErrorMessage(message), null, fDataStore);
	}
	
	protected String getErrorMessage(String message){
		String returnMsg=message;
	
		if (message.indexOf("Indexer:") < 0) { //$NON-NLS-1$
			returnMsg = "Parser Error Trace: " + message; //$NON-NLS-1$
		} else {
			if(fStatus!=null){
				for (int i = 0; i < fErrorMessages.size(); i++) {
	    			if (message.indexOf(fErrorMessages.get(i)) > 0) {
	    				fStatus.setAttribute(DE.A_NAME, message);
	    				fStatus.getDataStore().createObject(fStatus, CDTMiner.T_INDEXING_ERROR, message);
	    				fStatus.getDataStore().refresh(fStatus);
	    			}
	    		}
			}
		}
		return returnMsg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#isTracing()
	 */
	@Override
	public boolean isTracing() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#isTracingExceptions()
	 */
	@Override
	public boolean isTracingExceptions() {
		return true;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.AbstractParserLogService#traceLog(java.lang.String)
	 */
	@Override
	public void traceLog(String message) {		
		String logMessage = getTraceMessage(message);
		if(logMessage!=null){
			UniversalServerUtilities.logDebugMessage(LOG_TAG, message, fDataStore);
		}
		
	}
	
	protected String getTraceMessage(String message){
		String returnMsg=message;
		if (message.indexOf("Indexer:") < 0) { //$NON-NLS-1$
			returnMsg = "Parser Trace: " + message; //$NON-NLS-1$
		}else {
			if (message.indexOf("Indexer: unresolved name") >= 0 ) { //$NON-NLS-1$
				//see PDOMWriter.reportProblem(IProblemBinding problem)
				errorLog(message);
				return null;
			}else {
				//determine if it is an indexer error
				//see PDOMWriter.reportProblem(IASTProblem problem)
				boolean found = false;				
				for (int i = 0; i < fProblemBindingMessages.size(); i++) {
					if (message.indexOf(fProblemBindingMessages.get(i)) >= 0) {
						found = true;
						break;
					}
				}
				
				if (found) {
					errorLog(message);
					return null;
				} 
			}
		}
		return returnMsg;
	}
}
