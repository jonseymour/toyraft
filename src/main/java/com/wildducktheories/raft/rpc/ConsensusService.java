package com.wildducktheories.raft.rpc;

import java.io.IOException;

import com.wildducktheories.promise.Promise;

public interface ConsensusService {
	public Promise<AppendResponse, IOException> append(AppendRequest request);
	
	public Promise<VoteResponse, IOException> vote(VoteRequest request);
}
