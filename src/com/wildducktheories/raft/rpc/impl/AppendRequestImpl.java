package com.wildducktheories.raft.rpc.impl;

import java.util.ArrayList;
import java.util.List;

import com.wildducktheories.raft.log.Entry;
import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.log.Key;
import com.wildducktheories.raft.log.Term;
import com.wildducktheories.raft.rpc.AppendRequest;
import com.wildducktheories.raft.server.ServerID;

public class AppendRequestImpl implements AppendRequest {
	
	private final Term term;
	private final Key lastKey;
	private final ServerID leaderID;
	private final Index leaderCommit;
	private final List<Entry> entries = new ArrayList<Entry>();

	public AppendRequestImpl(
		ServerID leaderID, 
		Term term, 
		Key previous,
		Index leaderCommit,
		List<Entry> entries
	) {
		super();
		this.term = term;
		this.lastKey = previous;
		this.leaderID = leaderID;
		this.leaderCommit = leaderCommit;
		this.entries.addAll(entries);
	}

	@Override
	public Term getTerm() {
		return term;
	}

	@Override
	public ServerID getLeaderID() {
		return leaderID;
	}

	@Override
	public Key getLastKey() {
		return lastKey;
	}

	@Override
	public List<Entry> getEntries() {
		return entries;
	}

	@Override
	public Index getLeaderCommit() {
		return leaderCommit;
	}

}
