package com.wildducktheories.raft.rpc.impl;

import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.log.Term;
import com.wildducktheories.raft.rpc.VoteResponse;

public final class VoteResponseImpl implements VoteResponse {
	private final Term term;
	private final Index last;
	private final boolean successful;

	public VoteResponseImpl(Term term, Index last, boolean successful) {
		super();
		this.term = term;
		this.last = last;
		this.successful = successful;
	}

	@Override
	public Term getTerm() {
		return term;
	}

	@Override
	public boolean wasSuccessful() {
		return successful;
	}
	
	@Override
	public Index getLast() {
		return last;
	}

}
