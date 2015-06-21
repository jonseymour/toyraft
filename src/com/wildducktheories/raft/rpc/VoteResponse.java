package com.wildducktheories.raft.rpc;

import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.log.Term;

/**
 * A response sent by a server to a candidate to indicate
 * whether the server voted for the candidate.
 * @author jonseymour
 */
public interface VoteResponse {
	Term getTerm();
	boolean wasSuccessful();
	Index getLast();
}
