package com.wildducktheories.raft.rpc.impl;

import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.log.Term;
import com.wildducktheories.raft.rpc.AppendResponse;

public final class AppendResponseImpl implements AppendResponse {
	private final Term term;
	private final boolean successful;
	private final Index last;

	public AppendResponseImpl(Term term, boolean successful, Index lastWritten) {
		super();
		this.term = term;
		this.successful = successful;
		this.last = lastWritten;
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
