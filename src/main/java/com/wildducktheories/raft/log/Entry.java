package com.wildducktheories.raft.log;

import com.wildducktheories.raft.statemachine.Command;

public interface Entry {
	/**
	 * @return The key associated with the entry.
	 */
	public Key getKey();
	/**
	 * @return The next command associated with entry.
	 */
	public Command getCommand();
}
