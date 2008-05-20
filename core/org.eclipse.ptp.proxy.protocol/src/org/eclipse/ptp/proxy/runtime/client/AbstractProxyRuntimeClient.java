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

package org.eclipse.ptp.proxy.runtime.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.ptp.proxy.client.AbstractProxyClient;
import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyConnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyDisconnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyErrorEvent;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.IProxyEventListener;
import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent;
import org.eclipse.ptp.proxy.event.IProxyOKEvent;
import org.eclipse.ptp.proxy.event.IProxyTimeoutEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent.Level;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStartEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStopEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSubmitJobCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeTerminateJobCommand;
import org.eclipse.ptp.proxy.runtime.command.ProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeErrorStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeMessageEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewJobEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewMachineEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewNodeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewProcessEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNewQueueEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeProcessChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeQueueChangeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveAllEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveJobEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveMachineEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveNodeEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveProcessEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRemoveQueueEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeShutdownStateEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeStartupErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeTerminateJobErrorEvent;
import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.util.DebugOptions;

public abstract class AbstractProxyRuntimeClient extends AbstractProxyClient
		implements IProxyRuntimeClient, IProxyEventListener {

	private class StateMachineThread implements Runnable {
		private static final String name = "State Machine Thread";

		public void run() {
			try {
				runStateMachine();
			} catch (IllegalStateException e) {
				System.out.println("Illegal state detected: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (logEvents) {
				System.out.println("state machine thread exited");
			}
		}
	}

	/*
	 * Proxy state
	 */
	enum ProxyState {
		IDLE, STARTUP, INIT, MODEL_DEF, RUNNING, SHUTDOWN, END, ERROR
	}

	/*
	 * Flag to enable/disable logging messages
	 */
	private boolean logEvents = false;
	/*
	 * Actual name of the proxy - used for debugging messages
	 */
	private String proxyName = "";
	/*
	 * Main state machine variable
	 */
	private volatile ProxyState state;
	/*
	 * Master list of commands that have been sent to the server
	 */
	private List<IProxyCommand> commands = new ArrayList<IProxyCommand>();
	/*
	 * Event queue for incoming events.
	 */
	private LinkedBlockingQueue<IProxyEvent> events = new LinkedBlockingQueue<IProxyEvent>();
	/*
	 * Listener list for events generated by us
	 */
	private Collection<IProxyRuntimeEventListener> listeners = Collections
			.synchronizedList(new ArrayList<IProxyRuntimeEventListener>());
	/*
	 * Based ID for model ID's generated by the proxy. This is used to ensure
	 * model ID's are unique.
	 */
	protected final int baseModelId;
	/*
	 * Factory for creating new events
	 */
	protected final IProxyRuntimeEventFactory eventFactory;
	/*
	 * Factory for creating new commands
	 */
	protected final IProxyRuntimeCommandFactory cmdFactory;

	public AbstractProxyRuntimeClient(String name, int baseModelId,
			IProxyRuntimeCommandFactory cmdFactory,
			IProxyRuntimeEventFactory eventFactory) {
		this.cmdFactory = cmdFactory;
		this.eventFactory = eventFactory;
		this.proxyName = name;
		this.baseModelId = baseModelId;
		this.state = ProxyState.IDLE;
		super.setEventFactory(eventFactory);
		super.addProxyEventListener(this);

		if (DebugOptions.CLIENT_TRACING) {
			this.logEvents = true;
		}
	}

	public AbstractProxyRuntimeClient(String name, int baseModelId) {
		this(name, baseModelId, new ProxyRuntimeCommandFactory(), new ProxyRuntimeEventFactory());
	}

	/**
	 * Add a command to the list of commands that have been sent to the proxy
	 * 
	 * @param command
	 */
	private void addCommand(IProxyCommand command) {
		synchronized (commands) {
			commands.add(command);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#addProxyRuntimeEventListener(org.eclipse.ptp.proxy.runtime.client.event.IProxyRuntimeEventListener)
	 */
	public void addProxyRuntimeEventListener(IProxyRuntimeEventListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.proxy.event.IProxyConnectedEvent)
	 */
	public void handleEvent(IProxyConnectedEvent event) {
		try {
			// this will wake up the state machine to process the event
			events.add(event);
		} catch (IllegalStateException except) {
			// events list should never be full
			except.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.proxy.event.IProxyDisconnectedEvent)
	 */
	public void handleEvent(IProxyDisconnectedEvent event) {
		try {
			// this will wake up the state machine to process the event
			events.add(event);
		} catch (IllegalStateException except) {
			// events list should never be full
			except.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.proxy.event.IProxyErrorEvent)
	 */
	public void handleEvent(IProxyErrorEvent event) {
		try {
			// this will wake up the state machine to process the event
			events.add(event);
		} catch (IllegalStateException except) {
			// events list should never be full
			except.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.proxy.event.IProxyExtendedEvent)
	 */
	public void handleEvent(IProxyExtendedEvent event) {
		try {
			// this will wake up the state machine to process the event
			events.add(event);
		} catch (IllegalStateException except) {
			// events list should never be full
			except.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.proxy.event.IProxyMessageEvent)
	 */
	public void handleEvent(IProxyMessageEvent event) {
		try {
			// this will wake up the state machine to process the event
			events.add(event);
		} catch (IllegalStateException except) {
			// events list should never be full
			except.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.proxy.event.IProxyOKEvent)
	 */
	public void handleEvent(IProxyOKEvent event) {
		try {
			// this will wake up the state machine to process the event
			events.add(event);
		} catch (IllegalStateException except) {
			// events list should never be full
			except.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.event.IProxyEventListener#handleEvent(org.eclipse.ptp.proxy.event.IProxyTimeoutEvent)
	 */
	public void handleEvent(IProxyTimeoutEvent event) {
		try {
			// this will wake up the state machine to process the event
			events.add(event);
		} catch (IllegalStateException except) {
			// events list should never be full
			except.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#removeProxyRuntimeEventListener(org.eclipse.ptp.proxy.runtime.client.event.IProxyRuntimeEventListener)
	 */
	public void removeProxyRuntimeEventListener(
			IProxyRuntimeEventListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#shutdown()
	 */
	public void shutdown() throws IOException {
		if (state != ProxyState.SHUTDOWN) {
			if (logEvents) {
				System.out.println(toString() + ": shutting down server...");
			}
			state = ProxyState.SHUTDOWN;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#startEvents()
	 */
	public void startEvents() throws IOException {
		if (state != ProxyState.RUNNING) {
			throw new IOException("Not accepting commands");
		}
		IProxyCommand command = cmdFactory.newProxyRuntimeStartEventsCommand();
		addCommand(command);
		sendCommand(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#startup()
	 */
	public void startup() throws IOException {
		if (state == ProxyState.IDLE) {
			state = ProxyState.STARTUP;

			Thread smt = new Thread(new StateMachineThread(), proxyName
					+ StateMachineThread.name);
			smt.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#stopEvents()
	 */
	public void stopEvents() throws IOException {
		if (state != ProxyState.RUNNING) {
			throw new IOException("Not accepting commands");
		}
		IProxyCommand command = cmdFactory.newProxyRuntimeStopEventsCommand();
		addCommand(command);
		sendCommand(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#submitJob(java.lang.String[])
	 */
	public void submitJob(String[] args) throws IOException {
		if (state != ProxyState.RUNNING) {
			throw new IOException("Not accepting commands");
		}
		IProxyCommand command = cmdFactory
				.newProxyRuntimeSubmitJobCommand(args);
		addCommand(command);
		sendCommand(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.runtime.client.IProxyRuntimeClient#terminateJob(java.lang.String)
	 */
	public void terminateJob(String jobId) throws IOException {
		if (state != ProxyState.RUNNING) {
			throw new IOException("Not accepting commands");
		}
		IProxyCommand command = cmdFactory
				.newProxyRuntimeTerminateJobCommand(jobId);
		addCommand(command);
		sendCommand(command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return proxyName + "ProxyRuntimeClient";
	}

	/**
	 * Locate the command associated with a particular event. Uses the
	 * transaction ID o match commands/events.
	 * 
	 * @param event
	 * @return
	 */
	private IProxyCommand getCommandForEvent(IProxyEvent event) {
		IProxyCommand[] ca;
		IProxyCommand command = null;
		synchronized (commands) {
			ca = commands.toArray(new IProxyCommand[0]);
		}
		for (IProxyCommand cmd : ca) {
			if (cmd.getTransactionID() == event.getTransactionID()) {
				command = cmd;
				break;
			}
		}
		return command;
	}

	/**
	 * Process events while in the RUNNING state.
	 */
	private void processRunningEvent(IProxyCommand command, IProxyEvent event) {
		if (logEvents) {
			System.out.println(toString() + " received event " + event);
		}
		if (event instanceof IProxyMessageEvent) {
			fireProxyRuntimeMessageEvent(eventFactory
					.newProxyRuntimeMessageEvent((IProxyMessageEvent) event));
		} else if (command instanceof IProxyRuntimeStartEventsCommand) {
			if (event instanceof IProxyRuntimeNewJobEvent) {
				fireProxyRuntimeNewJobEvent((IProxyRuntimeNewJobEvent) event);
			} else if (event instanceof IProxyRuntimeNewMachineEvent) {
				fireProxyRuntimeNewMachineEvent((IProxyRuntimeNewMachineEvent) event);
			} else if (event instanceof IProxyRuntimeNewNodeEvent) {
				fireProxyRuntimeNewNodeEvent((IProxyRuntimeNewNodeEvent) event);
			} else if (event instanceof IProxyRuntimeNewProcessEvent) {
				fireProxyRuntimeNewProcessEvent((IProxyRuntimeNewProcessEvent) event);
			} else if (event instanceof IProxyRuntimeNewQueueEvent) {
				fireProxyRuntimeNewQueueEvent((IProxyRuntimeNewQueueEvent) event);
			} else if (event instanceof IProxyRuntimeJobChangeEvent) {
				fireProxyRuntimeJobChangeEvent((IProxyRuntimeJobChangeEvent) event);
			} else if (event instanceof IProxyRuntimeMachineChangeEvent) {
				fireProxyRuntimeMachineChangeEvent((IProxyRuntimeMachineChangeEvent) event);
			} else if (event instanceof IProxyRuntimeNodeChangeEvent) {
				fireProxyRuntimeNodeChangeEvent((IProxyRuntimeNodeChangeEvent) event);
			} else if (event instanceof IProxyRuntimeProcessChangeEvent) {
				fireProxyRuntimeProcessChangeEvent((IProxyRuntimeProcessChangeEvent) event);
			} else if (event instanceof IProxyRuntimeQueueChangeEvent) {
				fireProxyRuntimeQueueChangeEvent((IProxyRuntimeQueueChangeEvent) event);
			} else if (event instanceof IProxyRuntimeRemoveAllEvent) {
				fireProxyRuntimeRemoveAllEvent((IProxyRuntimeRemoveAllEvent) event);
			} else if (event instanceof IProxyRuntimeRemoveJobEvent) {
				fireProxyRuntimeRemoveJobEvent((IProxyRuntimeRemoveJobEvent) event);
			} else if (event instanceof IProxyRuntimeRemoveMachineEvent) {
				fireProxyRuntimeRemoveMachineEvent((IProxyRuntimeRemoveMachineEvent) event);
			} else if (event instanceof IProxyRuntimeRemoveNodeEvent) {
				fireProxyRuntimeRemoveNodeEvent((IProxyRuntimeRemoveNodeEvent) event);
			} else if (event instanceof IProxyRuntimeRemoveProcessEvent) {
				fireProxyRuntimeRemoveProcessEvent((IProxyRuntimeRemoveProcessEvent) event);
			} else if (event instanceof IProxyRuntimeRemoveQueueEvent) {
				fireProxyRuntimeRemoveQueueEvent((IProxyRuntimeRemoveQueueEvent) event);
			} else if (event instanceof IProxyErrorEvent) {
				fireProxyRuntimeErrorStateEvent(eventFactory
						.newProxyRuntimeErrorStateEvent());
			} else if (event instanceof IProxyOKEvent) {
				removeCommand(command);
			}
		} else if (command instanceof IProxyRuntimeStopEventsCommand) {
			if (event instanceof IProxyOKEvent) {
				removeCommand(command);
			}
		} else if (command instanceof IProxyRuntimeSubmitJobCommand) {
			if (event instanceof IProxyRuntimeSubmitJobErrorEvent) {
				fireProxyRuntimeSubmitJobErrorEvent(eventFactory
						.newProxyRuntimeSubmitJobErrorEvent(event
								.getTransactionID(), event.getAttributes()));
			} else if (event instanceof IProxyErrorEvent) {
				fireProxyRuntimeErrorStateEvent(eventFactory
						.newProxyRuntimeErrorStateEvent());
			}
			removeCommand(command);
		} else if (command instanceof IProxyRuntimeTerminateJobCommand) {
			if (event instanceof IProxyRuntimeTerminateJobErrorEvent) {
				fireProxyRuntimeTerminateJobErrorEvent(eventFactory
						.newProxyRuntimeTerminateJobErrorEvent(event
								.getTransactionID(), event.getAttributes()));
			} else if (event instanceof IProxyErrorEvent) {
				fireProxyRuntimeErrorStateEvent(eventFactory
						.newProxyRuntimeErrorStateEvent());
			}
			removeCommand(command);
		}

	}

	/**
	 * Remove command from list of sent commands.
	 * 
	 * @param command
	 */
	private void removeCommand(IProxyCommand command) {
		synchronized (commands) {
			command.completed();
			commands.remove(command);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeAttributeDefEvent(
			IProxyRuntimeAttributeDefEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeConnectedStateEvent(
			IProxyRuntimeConnectedStateEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeErrorStateEvent(
			IProxyRuntimeErrorStateEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeJobChangeEvent(
			IProxyRuntimeJobChangeEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeMachineChangeEvent(
			IProxyRuntimeMachineChangeEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeMessageEvent(IProxyRuntimeMessageEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeNewJobEvent(IProxyRuntimeNewJobEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeNewMachineEvent(
			IProxyRuntimeNewMachineEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeNewNodeEvent(IProxyRuntimeNewNodeEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeNewProcessEvent(
			IProxyRuntimeNewProcessEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeNewQueueEvent(
			IProxyRuntimeNewQueueEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeNodeChangeEvent(
			IProxyRuntimeNodeChangeEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeProcessChangeEvent(
			IProxyRuntimeProcessChangeEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeQueueChangeEvent(
			IProxyRuntimeQueueChangeEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeRemoveAllEvent(
			IProxyRuntimeRemoveAllEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeRemoveJobEvent(
			IProxyRuntimeRemoveJobEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeRemoveMachineEvent(
			IProxyRuntimeRemoveMachineEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeRemoveNodeEvent(
			IProxyRuntimeRemoveNodeEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeRemoveProcessEvent(
			IProxyRuntimeRemoveProcessEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeRemoveQueueEvent(
			IProxyRuntimeRemoveQueueEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeRunningStateEvent(
			IProxyRuntimeRunningStateEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeShutdownStateEvent(
			IProxyRuntimeShutdownStateEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeStartupErrorEvent(
			IProxyRuntimeStartupErrorEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeSubmitJobErrorEvent(
			IProxyRuntimeSubmitJobErrorEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward event to listeners
	 * 
	 * @param event
	 */
	protected void fireProxyRuntimeTerminateJobErrorEvent(
			IProxyRuntimeTerminateJobErrorEvent event) {
		for (Object listener : listeners) {
			((IProxyRuntimeEventListener) listener).handleEvent(event);
		}
	}

	/**
	 * Main proxy state machine. Use to manage communication with a proxy
	 * client.
	 * 
	 * This should only be called from the state machine thread (so synchronized
	 * not needed)
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws IllegalStateException
	 */
	protected void runStateMachine() throws IOException, InterruptedException,
			IllegalStateException {
		while (state != ProxyState.IDLE && state != ProxyState.ERROR) {
			IProxyCommand command;
			IProxyEvent event;

			switch (state) {
			case STARTUP:
				/*
				 * This state is used to wait for a connected event from the
				 * proxy after it was started. A connected event indicates that
				 * the proxy has been launched successfully. Send an INIT
				 * command to the proxy and enter the INIT state.
				 */
				event = events.take();

				if (event instanceof IProxyConnectedEvent) {
					try {
						sessionHandleEvents();
						command = cmdFactory
								.newProxyRuntimeInitCommand(baseModelId);
						addCommand(command);
						sendCommand(command);
						state = ProxyState.INIT;
						fireProxyRuntimeConnectedStateEvent(eventFactory
								.newProxyRuntimeConnectedStateEvent());
					} catch (IOException e) {
						state = ProxyState.IDLE;
						fireProxyRuntimeStartupErrorEvent(eventFactory
								.newProxyRuntimeStartupErrorEvent(e
										.getMessage()));
					}
				} else if (event instanceof IProxyTimeoutEvent) {
					state = ProxyState.IDLE;
					fireProxyRuntimeStartupErrorEvent(eventFactory
							.newProxyRuntimeStartupErrorEvent("Proxy connection timeout out"));
				} else if (event instanceof IProxyDisconnectedEvent) {
					state = ProxyState.IDLE;
					fireProxyRuntimeStartupErrorEvent(eventFactory
							.newProxyRuntimeStartupErrorEvent("Proxy disconnected"));
				} else {
					state = ProxyState.ERROR;
					fireProxyRuntimeErrorStateEvent(eventFactory
							.newProxyRuntimeErrorStateEvent());
					throw new IllegalStateException("Received "
							+ event.toString() + " in STARTUP");
				}
				break;

			case INIT:
				/*
				 * This state is set to wait from a response from the INIT
				 * command. If we receive an OK event, we next send a MODEL_DEF
				 * command and enter the MODEL_DEF state.
				 */
				event = events.take();

				if (event instanceof IProxyDisconnectedEvent) {
					state = ProxyState.IDLE;
					fireProxyRuntimeStartupErrorEvent(eventFactory
							.newProxyRuntimeStartupErrorEvent("Proxy disconnected"));
				} else if (event instanceof IProxyMessageEvent) {
					fireProxyRuntimeMessageEvent(eventFactory
							.newProxyRuntimeMessageEvent((IProxyMessageEvent) event));
				} else {
					command = getCommandForEvent(event);
					if (command != null) {
						removeCommand(command);
						if (event instanceof IProxyOKEvent) {
							command = cmdFactory
									.newProxyRuntimeModelDefCommand();
							addCommand(command);
							sendCommand(command);
							state = ProxyState.MODEL_DEF;
						} else if (event instanceof IProxyErrorEvent) {
							state = ProxyState.IDLE;
							fireProxyRuntimeStartupErrorEvent(eventFactory
									.newProxyRuntimeStartupErrorEvent(event
											.getAttributes()));
						} else {
							state = ProxyState.ERROR;
							fireProxyRuntimeErrorStateEvent(eventFactory
									.newProxyRuntimeErrorStateEvent());
							throw new IllegalStateException(
									"Could not find command for event [" 
										+ event.toString() + "] in INIT");
						}
					} else {
						state = ProxyState.ERROR;
						fireProxyRuntimeErrorStateEvent(eventFactory
								.newProxyRuntimeErrorStateEvent());
						throw new IllegalStateException("Received "
								+ event.toString() + " in INIT");
					}
				}
				break;

			case MODEL_DEF:
				/*
				 * This state is used to process attribute definition events
				 * that are used by the proxy to define any attributes that it
				 * will use. This state is terminated when an OK event is
				 * received. At this point enter the RUNNING state.
				 * 
				 */
				event = events.take();

				if (event instanceof IProxyDisconnectedEvent) {
					state = ProxyState.IDLE;
					fireProxyRuntimeStartupErrorEvent(eventFactory
							.newProxyRuntimeStartupErrorEvent("Proxy disconnected"));
				} else if (event instanceof IProxyMessageEvent) {
					fireProxyRuntimeMessageEvent(eventFactory
							.newProxyRuntimeMessageEvent((IProxyMessageEvent) event));
				} else {
					command = getCommandForEvent(event);
					if (command != null) {
						if (event instanceof IProxyOKEvent) {
							removeCommand(command);
							state = ProxyState.RUNNING;
							fireProxyRuntimeRunningStateEvent(eventFactory
									.newProxyRuntimeRunningStateEvent());
						} else if (event instanceof IProxyRuntimeAttributeDefEvent) {
							fireProxyRuntimeAttributeDefEvent((IProxyRuntimeAttributeDefEvent) event);
						} else if (event instanceof IProxyErrorEvent) {
							removeCommand(command);
							state = ProxyState.IDLE;
							fireProxyRuntimeStartupErrorEvent(eventFactory
									.newProxyRuntimeStartupErrorEvent(event
											.getAttributes()));
						} else {
							removeCommand(command);
							state = ProxyState.ERROR;
							fireProxyRuntimeErrorStateEvent(eventFactory
									.newProxyRuntimeErrorStateEvent());
							throw new IllegalStateException(
									"Could not find command for event [" 
										+ event.toString() + "] in MODEL_DEF");
						}
					} else {
						state = ProxyState.ERROR;
						fireProxyRuntimeErrorStateEvent(eventFactory
								.newProxyRuntimeErrorStateEvent());
						throw new IllegalStateException("Received "
								+ event.toString() + " in MODEL_DEF");
					}
				}
				break;

			case RUNNING:
				/*
				 * This is the main event processing state. Any events we
				 * receive in this state are forwarded to event listeners. This
				 * state continues until the shutdown() method is called, at
				 * which time we enter the shutdown state.
				 */
				event = events.take();

				if (event instanceof IProxyDisconnectedEvent) {
					state = ProxyState.SHUTDOWN;
					fireProxyRuntimeMessageEvent(eventFactory
							.newProxyRuntimeMessageEvent(Level.FATAL,
									"Proxy disconnected"));
				} else {
					command = getCommandForEvent(event);
					if (command != null) {
						processRunningEvent(command, event);
					} else {
						state = ProxyState.ERROR;
						throw new IllegalStateException(
								"Could not find command for event [" 
									+ event.toString() + "] in RUNNING");
					}
				}
				break;

			case SHUTDOWN:
				/*
				 * This state is entered when the proxy has been shut down. We
				 * stay in this state until the session is shut down. This
				 * happens when an OK event is received in response to a QUIT
				 * command, or if the shutdown timeout has expired.
				 */
				if (isShutdown()) {
					fireProxyRuntimeShutdownStateEvent(eventFactory
							.newProxyRuntimeShutdownStateEvent());
					state = ProxyState.IDLE;
					commands.clear();
					events.clear();
				} else {
					/*
					 * Wait for any event. We will eventually get an
					 * IProxyDisconnectedEvent.
					 */
					events.take();
				}
				break;

			default:
				throw new IllegalStateException("Unknown state: "
						+ state.toString());
			}
		}
	}
}
