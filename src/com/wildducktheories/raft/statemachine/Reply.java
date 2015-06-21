package com.wildducktheories.raft.statemachine;

import com.wildducktheories.raft.server.ServerID;

/**
 * A reply from a 
 * @author jonseymour
 */
public interface Reply {
	/**
	 * @return True if the command was accepted by the server.
	 */
	public boolean wasAccepted();
	
	/**
	 * @return True if the server redirected the reply to another server.
	 */
	public boolean wasRedirected();
	
	/**
	 * @return The identity of the server to which the command should be
	 * directed.
	 */
	public ServerID getRedirection();
}
