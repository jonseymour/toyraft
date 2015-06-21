package com.wildducktheories.raft.rpc;

import com.wildducktheories.raft.log.Key;
import com.wildducktheories.raft.log.Term;
import com.wildducktheories.raft.server.ServerID;


/**
 * A request sent by a candidate to a server requesting the vote
 * of the recipient in favour of the candidate.
 * @author jonseymour
 */
public interface VoteRequest {
	/**
	 * @return The candidate requesting the vote.
	 */
	public ServerID getCandidateID();
	
	/**
	 * @return The term for which election is requested.
	 */
	public Term getTerm();
	
	/**
	 * @return The last log key seen by the server.
	 */
	public Key getLastKey();
}
