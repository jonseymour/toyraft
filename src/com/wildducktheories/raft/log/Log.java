package com.wildducktheories.raft.log;

import java.util.List;

import com.wildducktheories.raft.statemachine.Command;

public interface Log {
	/** 	 
	 * Write a message into the log with the specified term 
	 * @param Command m The message to write into the log.
	 * @param Term t The current term.
	 */
	Key write(Command m, Term t);
	
	/**
	 * Commit all the entries in the log up to the specified log index.
	 * @param commitTo The log index of the last entry that is committed.
	 * @throws IllegalStateException if commitTo does not exist in the log.
	 */
	void commit(Index commitTo);
	
	/**
	 * Rollback all the entries in the log up to the specified log index.
	 * @param rollbackTo
	 * @throws
	 */ 
	void rollback(Index rollbackTo);
	
	/**
	 * Mark all the messages upto and including applyTo as applied.
	 * @param applyTo
	 */
	void apply(Index applyTo);
	
	/**
	 * @param index The index of the message to receive.
	 * @return The entry at the specified log index.
	 */
	Entry getEntry(Index index);
	
	/**
	 * @param index The index of the message next message to be applied.
	 * @return The index of the next message to be applied.
	 */
	Index getNext();
	
	/**
	 * @param index The index of the last message written to the log.
	 */
	Index getLast();
	
	/**
	 * @param index The commit index.
	 */
	Index getCommit();
	
	/**
	 * Get all the log entries after the specified index.
	 */
	List<Entry> getFrom(Index fromIndex);
}
