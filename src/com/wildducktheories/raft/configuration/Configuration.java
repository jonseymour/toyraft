package com.wildducktheories.raft.configuration;

import java.util.Map;
import java.util.Set;

import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.server.ServerID;

public interface Configuration {
	/**
	 * Determine if the set of votes represents a quorum in the current
	 * configuration.
	 * 
	 * @param votes
	 * @return True if the current set of votes represents a quorum.
	 */
	boolean quorumAchieved(Set<ServerID> votes);
	
	/**
	 * @return Answer the current set of followers for the current configuration.
	 */
	Set<ServerID> getFollowers();
	
	/**
	 * @return Answer the heart beat timeout for the current configuration.
	 */
	long getHeartBeatTimeout();
	
	/**
	 * @return Answer the election timeout for the current configuration.
	 */
	long getElectionTimeout();
	
	/**
	 * Given the current commit and indexes we have discovered
	 * from our followers, return the highest index that has achieved a quorum in
	 * the currently active configuration.
	 * @param commit
	 * @param confirmed
	 * @return
	 */
	Index updateCommit(Index commit, Map<ServerID, Index> confirmed);
}
