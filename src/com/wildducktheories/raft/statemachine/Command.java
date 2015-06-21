package com.wildducktheories.raft.statemachine;


public interface Command {
	/**
	 * A unique identifier for a message that enables replay detection.
	 * @return
	 */
	CommandID getID();
}
