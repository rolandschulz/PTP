/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * David McKnight   (IBM)  [251619] [dstore] shell output readers not cleaned up on disconnect
 * David McKnight   (IBM)  [244070] [dstore] DStoreHostShell#exit() does not terminate child processes
 *******************************************************************************/

package org.eclipse.ptp.internal.remote.rse.core;

import java.io.File;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.rse.services.dstore.util.DStoreStatusMonitor;
import org.eclipse.rse.services.shells.AbstractHostShell;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IHostShellOutputReader;


public class DStoreHostShell extends AbstractHostShell implements IHostShell
{
	private IHostShellOutputReader _stdoutHandler;
	private IHostShellOutputReader _stderrHandler;
	private DataElement _status;
	private DStoreStatusMonitor _statusMonitor;
	protected DataElement _sendInputDescriptor;
	private boolean _sentCharConversionCommand = false;
	private DataStore _dataStore;
	
	@SuppressWarnings("restriction")
	public DStoreHostShell(DStoreStatusMonitor statusMonitor, DataElement status, DataStore dataStore, String initialWorkingDirectory, String invocation, String encoding, String[] environment, boolean redirectStderr)
	{
		_dataStore = dataStore;
		
		// make this subsystem a communications listener
		DataElement contextDir = dataStore.createObject(null, "directory", (new File(initialWorkingDirectory)).getName(), initialWorkingDirectory); //$NON-NLS-1$
		dataStore.setObject(contextDir);
		
		_status = status;
		
		if(redirectStderr) {
			// we redirect stderr to stdout using our own hacked up copy of DStoreShellOutputReader
			_stdoutHandler = new DStoreShellOutputReader(this, _status, false);
			_stderrHandler = null;
		}
		
		else {
			// use normal RSE stuff
			_stdoutHandler = new org.eclipse.rse.internal.services.dstore.shells.DStoreShellOutputReader(this, _status, false);
			_stderrHandler = new org.eclipse.rse.internal.services.dstore.shells.DStoreShellOutputReader(this, _status, true);
		}
		
		_statusMonitor = statusMonitor;
		
		
	}
	
	public boolean isActive()
	{
		return !_statusMonitor.determineStatusDone(_status);
	}

	public void writeToShell(String command)
	{
		DataElement commandElement = _status.getParent();
		DataStore dataStore = commandElement.getDataStore();

		if (command.equals("") || command.equals("#break")) //$NON-NLS-1$ //$NON-NLS-2$
		{
		    String cmd = command;
		    if (cmd.equals("")) //$NON-NLS-1$
		        cmd = "#enter"; //$NON-NLS-1$
			DataElement commandDescriptor = getSendInputDescriptor(commandElement);
			if (commandDescriptor != null)
			{
				DataElement in = dataStore.createObject(null, "input", cmd); //$NON-NLS-1$
				dataStore.command(commandDescriptor, in, commandElement);
			}
		}
		else
		{
		    String[] tokens = command.split("\n\r"); //$NON-NLS-1$
			for (int i = 0; i <tokens.length; i++)
			{
			    String cmd = tokens[i];

				if (cmd != null)
				{	
					// first, find out if the server support conversion
					DataElement fsD= dataStore.findObjectDescriptor(DataStoreResources.model_directory);
					DataElement convDes = dataStore.localDescriptorQuery(fsD, "C_CHAR_CONVERSION", 1); //$NON-NLS-1$
					if (convDes != null){						
						if (!_sentCharConversionCommand){
							dataStore.command(convDes, _status);
							_sentCharConversionCommand = true;
						}
						
						cmd = convertSpecialCharacters(cmd);
					}
					
				    DataElement commandDescriptor = getSendInputDescriptor(commandElement);
					if (commandDescriptor != null)
					{
						DataElement in = dataStore.createObject(null, "input", cmd); //$NON-NLS-1$
						dataStore.command(commandDescriptor, in, commandElement);
					}
				}
			}
		}
	}
	
	private DataElement getSendInputDescriptor(DataElement remoteObject)
	{
	    if (_sendInputDescriptor == null || _dataStore != remoteObject.getDataStore())
	    {
	        _sendInputDescriptor = _dataStore.findCommandDescriptor(DataStoreSchema.C_SEND_INPUT);
	    }
	
	    return _sendInputDescriptor;
	    }
	
	private String convertSpecialCharacters(String input){
	   // needed to ensure xml characters aren't converted in xml layer	
	
		StringBuffer output = new StringBuffer();

		for (int idx = 0; idx < input.length(); idx++)
		{
			char currChar = input.charAt(idx);
			switch (currChar)
			{
			case '&' :
				output.append("&#38;"); //$NON-NLS-1$
				break;
			case ';' :
				output.append("&#59;"); //$NON-NLS-1$
				break;
			default :
				output.append(currChar);
				break;
			}
		}
		return output.toString();
	}

	public IHostShellOutputReader getStandardOutputReader()
	{
		return _stdoutHandler;
	}

	public IHostShellOutputReader getStandardErrorReader()
	{
		return _stderrHandler;
	}
	
	public DataElement getStatus()
	{
		return _status;
	}

	

	public void exit()
	{
		// send cancel command
		DataElement command = _status.getParent();
		DataStore dataStore = command.getDataStore();
		DataElement cmdDescriptor = command.getDescriptor();
		DataElement cancelDescriptor = dataStore.localDescriptorQuery(cmdDescriptor, "C_CANCEL"); //$NON-NLS-1$

		if (cancelDescriptor != null)
		{
			dataStore.command(cancelDescriptor, command);
		}
		
		_status.setAttribute(DE.A_VALUE, "done"); //$NON-NLS-1$
		
		_stdoutHandler.finish();
		_stderrHandler.finish();
		
		
	}

	
}
