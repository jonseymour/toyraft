package com.wildducktheories.raft.rpc;

import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.log.Term;

public interface AppendResponse {
	public Term getTerm();
	public boolean wasSuccessful();
	
	/**
	 * @return The last index written. Valid iff wasSuccessful() was true.
	 */
	Index getLast();
}
