/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.miners;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.eclipse.ptp.internal.rdt.core.miners.RemoteLogService;



public class StandaloneLogService extends RemoteLogService {
	
	
	private PrintStream log_ps;
	private boolean fIsTracing=true;
	private boolean fIsTracingExceptions=true;
	private static final String TRACE_LINE_BREAK="---------------------------------------------------------------"; //$NON-NLS-1$
	private static final String ERROR_LINE_BREAK="==============================================================="; //$NON-NLS-1$
	
	private static final StandaloneLogService instance= new StandaloneLogService();
	
	protected StandaloneLogService() {
		super(null, null);
		// TODO Auto-generated constructor stub
	}
	
	public static StandaloneLogService getInstance(){
		
		return instance;
	}
	
		
	public void setLogFileName(String logFileName) {
		
		if(logFileName!=null && logFileName.length()>0){
			try {
				File logFile = new File(logFileName);
				if(!logFile.exists()){
					logFile.createNewFile();
				}
				FileOutputStream log_out=new FileOutputStream(logFileName);
				if(this.log_ps!=null){
					//close previous log 
					this.log_ps.close();
				}
				this.log_ps = new PrintStream(log_out);
				
			} catch (FileNotFoundException e) {
				
				
			} catch (IOException e) {
				
			}
			
		}
	}
	
	
	
	public void setTracing(boolean isTracing) {
		fIsTracing = isTracing;
	}

	public void setTracingExceptions(boolean isTracingExceptions) {
		fIsTracingExceptions = isTracingExceptions;
	}

	public void saveLog(){
		if(log_ps!=null){
			log_ps.flush();
			log_ps.close();
			this.log_ps=null;
		}
		
	}
	
	

	public void traceLog(String message) {
		if(isTracing()){
			PrintStream local_ps = System.out;
			if(log_ps!=null){
				local_ps=log_ps;
			}
			
			String logMessage = getTraceMessage(message);
			if(logMessage!=null&&logMessage.length()>0){
				Date date = new Date();
				local_ps.println(date.toString());
				local_ps.println(logMessage); 
				local_ps.println(TRACE_LINE_BREAK);
			}
		}
		
	}

	public void errorLog(String message) {
		if(isTracingExceptions()){
			PrintStream local_ps = System.out;
			if(log_ps!=null){
				local_ps=log_ps;
			}
			if(message!=null&&message.length()>0){
				Date date = new Date();
				local_ps.println(date.toString());
				local_ps.println(getErrorMessage(message));
				local_ps.println(ERROR_LINE_BREAK);
			}
		}
		
	}
	public static String getStackTrace(Throwable e)
    {
        StringWriter strW = new StringWriter();
        PrintWriter printW = new PrintWriter(strW, true);
        e.printStackTrace(printW);
        printW.flush();
        strW.flush();
        return strW.toString();
    }

	public void errorLog(String message, Throwable e) {
		if(isTracingExceptions()){
			PrintStream local_ps = System.out;
			if(log_ps!=null){
				local_ps=log_ps;
			}
			if(message!=null&&message.length()>0){
				Date date = new Date();
				local_ps.println(date.toString());
				local_ps.println(getErrorMessage(message));
				
			}
			if(e !=null){
				local_ps.println(getErrorMessage(getStackTrace(e)));
			}
			local_ps.println(ERROR_LINE_BREAK);
		}
		
	}

	public boolean isTracing(){
		return fIsTracing;
	}
	
	public boolean isTracingExceptions() {
		return fIsTracingExceptions;
	}
	
	

}
