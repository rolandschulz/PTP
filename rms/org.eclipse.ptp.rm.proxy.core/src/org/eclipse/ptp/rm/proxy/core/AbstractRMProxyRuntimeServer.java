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

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer;

/**
 * @since 2.0
 */
public abstract class AbstractRMProxyRuntimeServer extends AbstractProxyRuntimeServer {

	public AbstractRMProxyRuntimeServer(String host, int port, IProxyRuntimeEventFactory eventFactory) {
		super(host, port, eventFactory);
	}

	/*
	 * This routine is called during the DISCOVERY phase.
	 */
	protected abstract List<IAttributeDefinition<?, ?, ?>> detectAttributeDefinitions();

	@Override
	protected void doServerState(ServerState state, int transID, IProxyCommand command) throws IOException {
		switch (state) {
		case DISCOVERY:
			// detect Attributes
			List<IAttributeDefinition<?, ?, ?>> attrDefList = detectAttributeDefinitions();
			// iterate through attributes and send them to the client
			for (IAttributeDefinition<?, ?, ?> attrDef : attrDefList) {
				AttributeDefinitionSerializer ads = new AttributeDefinitionSerializer(attrDef);
				// System.err.println(ads.str());
				IProxyEvent event = fEventFactory.newProxyRuntimeAttributeDefEvent(transID, ads.strList());
				sendEvent(event);
			}
			break;
		default:
			/*
			 * No other processing needed.
			 */
			break;
		}
	}
}