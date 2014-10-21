/*
 * Copyright: Almende B.V. (2014), Rotterdam, The Netherlands
 * License: The Apache Software License, Version 2.0
 */
package com.almende.eve.agent;

import java.util.EventListener;

/**
 * The listener interface for receiving agentEvent events.
 * The class that is interested in processing a agentEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addAgentEventListener<code> method. When
 * the agentEvent event occurs, that object's appropriate
 * method is invoked.
 */
interface AgentEventListener extends EventListener, Runnable {
	
}
