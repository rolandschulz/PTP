 /*******************************************************************************
  * Copyright (c) 2010 The University of Tennessee,
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Benjamin Lindner (ben@benlabs.net) - initial implementation (bug 316671)

  *******************************************************************************/

package org.eclipse.ptp.rm.proxy.core;


import java.io.IOException;
import java.util.List;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.command.IProxyQuitCommand;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeInitCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeModelDefCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStartEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSubmitJobCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeTerminateJobCommand;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer;
import org.eclipse.ptp.proxy.runtime.server.ElementIDGenerator;

import org.eclipse.ptp.core.attributes.*;


/**
 * @since 2.0
 */
abstract public class ProxyRuntimeServer extends AbstractProxyRuntimeServer {

	public ProxyRuntimeServer(String host, int port,
			IProxyRuntimeEventFactory eventFactory) {
		super(host, port, eventFactory);
		// TODO Auto-generated constructor stub
	}

	// this routine has to be implemented and is called during the DISCOVERY phase:
	//abstract protected Map<String, IAttributeDefinition<?, ?, ?>> detectAttributes();
	abstract protected List<IAttributeDefinition<?, ?, ?>> detectAttributeDefinitions();
	
	// this routine is overwritten to allow for the checkEnvironment and 
	// detectdAttributes functionality
	@Override
	protected void runStateMachine() throws InterruptedException, IOException {
		while (state != ServerState.SHUTDOWN) {
			IProxyCommand command;
			IProxyEvent event;
			int transID;
			System.out.println("runStateMachine: state: " + state); //$NON-NLS-1$
			switch (state) {
			case INIT:
				command = fCommands.take();

				// instead of getting the base_ID the hard way, rather implement
				// a getBase_ID method in IProxyRuntimeInitCommand.
				int base_ID = Integer.parseInt(command.getArguments()[1].split("=")[1]); //$NON-NLS-1$
				ElementIDGenerator.getInstance().setBaseID(base_ID);
				transID = command.getTransactionID();
				System.out.println("runStateMachine: command: " + command.getCommandID() + " (" + transID + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (command instanceof IProxyRuntimeInitCommand) {
					try {
						initServer();
						event = fEventFactory.newOKEvent(transID);
						sendEvent(event);
						state = ServerState.DISCOVERY;
					} catch (Exception e) {
						event = fEventFactory.newErrorEvent(transID, 0, e.getMessage());
						e.printStackTrace();
						sendEvent(event);
						state = ServerState.SHUTDOWN;
					}
				} else {
					System.err.println("unexpected command (INIT): " + command); //$NON-NLS-1$
				}
				
				break;
			case DISCOVERY:
				command = fCommands.take();
				transID = command.getTransactionID();
				System.out.println("runStateMachine: command: " + command.getCommandID() + " (" + transID + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$			

				// detect Attributes
				List<IAttributeDefinition<?,?,?>> attrDefList = detectAttributeDefinitions();
				//iterate through attributes and send them to the client
				for( IAttributeDefinition<?,?,?> attrDef : attrDefList ) {
					AttributeDefinitionSerializer ads = new AttributeDefinitionSerializer(attrDef);
					//System.err.println(ads.str()); 
					event = fEventFactory.newProxyRuntimeAttributeDefEvent(transID, ads.strList());
					sendEvent(event);
				}
				
				if (command instanceof IProxyRuntimeModelDefCommand) {
					event = fEventFactory.newOKEvent(transID);
					sendEvent(event);
					state = ServerState.NORMAL;
				} else {
					System.err.println("unexpected command (DISC): " + command); //$NON-NLS-1$
				}

				break;
			case NORMAL:
				command = fCommands.take();
				transID = command.getTransactionID();
				System.out.println("runStateMachine: command: " + command.getCommandID() + " (" + transID + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (command instanceof IProxyRuntimeStartEventsCommand) {
					// TODO start event loop
					// event = eventFactory.newOKEvent(transID); //TODO: send OK
					// here?
					// sendEvent(event);

					if (fEventThread == null) {
						fEventLoopTransID = transID;
						this.startEventThread(transID);
					}
				} else if (command instanceof IProxyQuitCommand) {
					event = fEventFactory.newShutdownEvent(transID);
					sendEvent(event);
					state = ServerState.SHUTDOWN;
				} else if (command instanceof IProxyRuntimeTerminateJobCommand) {
					terminateJob(transID, command.getArguments());
				} else if (command instanceof IProxyRuntimeSubmitJobCommand) {
					submitJob(transID, command.getArguments());
				} else {
					System.err.println("unexpected command (NORM): " + command); //$NON-NLS-1$
				}
				// state = ServerState.NORMAL;
				break;
			}
		}
	}

}