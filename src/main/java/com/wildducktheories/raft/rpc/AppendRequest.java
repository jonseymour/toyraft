package com.wildducktheories.raft.rpc;

import java.util.List;

import com.wildducktheories.raft.log.Entry;
import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.log.Key;
import com.wildducktheories.raft.log.Term;
import com.wildducktheories.raft.server.ServerID;

public interface AppendRequest {
	/**
	 * @return Answer the term of the current request.
	 */
	Term getTerm();
	
	/**
	 * @return Answer the identifier of the leader.
	 */
	ServerID getLeaderID();
	
	/**
	 * @return Answer key to which the log will be truncated or to 
	 * which the entries will be appended.
	 */
	Key getLastKey();
	
	/**
	 * @return Answer the entries to be appended to the receiver's 
	 * log.
	 */
	List<Entry> getEntries();
	
	/**
	 * @return The index that the leader considers committed.
	 */
	Index getLeaderCommit();
}
