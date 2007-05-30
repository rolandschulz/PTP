/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly  
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 ******************************************************************************/
 
#ifndef _PROXY_EVENT_H_
#define _PROXY_EVENT_H_

/*
 * Event code definitions.
 * 
 * These are the events used by the proxy runtime system.
 * 
 * Event codes that are marked 'LOCAL EVENT' are *not* sent across the wire. They
 * are used for internal communication only (mainly on the Java side, but can
 * be used by a C client if desired). All other events are sent across
 * the wire.
 */

#define PROXY_EV_OK				0
#define PROXY_EV_MESSAGE		1
#define PROXY_EV_CONNECTED		2	/* LOCAL EVENT */
#define PROXY_EV_DISCONNECTED	3	/* LOCAL EVENT */
#define PROXY_EV_TIMEOUT		4	/* LOCAL EVENT */
#define PROXY_EV_ERROR			5

/*
 * Codes must EXACTLY match org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent
 */
#define PROXY_EV_RT_OFFSET				200
#define PROXY_EV_RT_MESSAGE				PROXY_EV_RT_OFFSET + 1	/* LOCAL EVENT */
#define PROXY_EV_RT_ATTR_DEF			PROXY_EV_RT_OFFSET + 2
#define PROXY_EV_RT_NEW_JOB				PROXY_EV_RT_OFFSET + 3
#define PROXY_EV_RT_NEW_MACHINE			PROXY_EV_RT_OFFSET + 4
#define PROXY_EV_RT_NEW_NODE			PROXY_EV_RT_OFFSET + 5
#define PROXY_EV_RT_NEW_PROCESS			PROXY_EV_RT_OFFSET + 6
#define PROXY_EV_RT_NEW_QUEUE			PROXY_EV_RT_OFFSET + 7
#define PROXY_EV_RT_JOB_CHANGE			PROXY_EV_RT_OFFSET + 8
#define PROXY_EV_RT_MACHINE_CHANGE		PROXY_EV_RT_OFFSET + 9
#define PROXY_EV_RT_NODE_CHANGE			PROXY_EV_RT_OFFSET + 10
#define PROXY_EV_RT_PROCESS_CHANGE		PROXY_EV_RT_OFFSET + 11
#define PROXY_EV_RT_QUEUE_CHANGE		PROXY_EV_RT_OFFSET + 12
#define PROXY_EV_RT_STARTUP				PROXY_EV_RT_OFFSET + 13	/*LOCAL EVENT */
#define PROXY_EV_RT_RUNNING				PROXY_EV_RT_OFFSET + 14	/*LOCAL EVENT */
#define PROXY_EV_RT_SHUTDOWN			PROXY_EV_RT_OFFSET + 15	/*LOCAL EVENT */
#define PROXY_EV_RT_REMOVE_ALL			PROXY_EV_RT_OFFSET + 16
#define PROXY_EV_RT_REMOVE_JOB			PROXY_EV_RT_OFFSET + 17
#define PROXY_EV_RT_REMOVE_MACHINE		PROXY_EV_RT_OFFSET + 18
#define PROXY_EV_RT_REMOVE_NODE			PROXY_EV_RT_OFFSET + 19
#define PROXY_EV_RT_REMOVE_PROCESS		PROXY_EV_RT_OFFSET + 20
#define PROXY_EV_RT_REMOVE_QUEUE		PROXY_EV_RT_OFFSET + 21
#define PROXY_EV_RT_STARTUP_ERROR		PROXY_EV_RT_OFFSET + 22	/*LOCAL EVENT */
#define PROXY_EV_RT_SUBMITJOB_ERROR		PROXY_EV_RT_OFFSET + 23	/*LOCAL EVENT */
#define PROXY_EV_RT_TERMINATEJOB_ERROR	PROXY_EV_RT_OFFSET + 24	/*LOCAL EVENT */
#endif /* !_PROXY_EVENT_H_ */
