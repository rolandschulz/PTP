package org.eclipse.ptp.rm.jaxb.core.data;

public class ArglistImpl {

	private final Arglist args;

	public ArglistImpl(Arglist args) {
		this.args = args;
	}

	/*
	 * Iterate over all dynamic attributes, appending the sequence of args for
	 * each attribute. By convention, '${@name}' and '${@value}' will refer to a
	 * given dynamic attribute name and value (of undefined position i in the
	 * list).
	 */
	public void toString(StringBuffer buffer) {

	}
}
