package com.wildducktheories.raft.rpc.impl;

import com.wildducktheories.raft.log.Key;
import com.wildducktheories.raft.log.Term;
import com.wildducktheories.raft.rpc.VoteRequest;
import com.wildducktheories.raft.server.ServerID;

public class VoteRequestImpl implements VoteRequest {
	private final ServerID candidate;
	private final Term term;
	private final Key lastKey;
	
	public VoteRequestImpl(ServerID candidate, Term term, Key last) {
		super();
		this.candidate = candidate;
		this.term = term;
		this.lastKey = last;
	}
	@Override
	public ServerID getCandidateID() {
		return candidate;
	}
	@Override
	public Term getTerm() {
		return term;
	}
	@Override
	public Key getLastKey() {
		return lastKey;
	}
	
}
