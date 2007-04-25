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

#define PROXY_EV_OK				0
#define PROXY_EV_ERROR			1
#define PROXY_EV_CONNECTED		2	/* LOCAL EVENT */
#define PROXY_EV_DISCONNECTED	3	/* LOCAL EVENT */
#define PROXY_EV_TIMEOUT		4	/* LOCAL EVENT */

/*
 * Codes must EXACTLY match org.eclipse.ptp.rtsystem.proxy.event.IProxyRuntimeEvent
 */
#define PROXY_EV_RT_OFFSET				200
#define PROXY_EV_RT_ERROR				PROXY_EV_RT_OFFSET + 1	/* LOCAL EVENT */
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
#endif /* !_PROXY_EVENT_H_ */
