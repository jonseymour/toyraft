package com.wildducktheories.raft.time;

import com.wildducktheories.promise.Promise;

public interface TimingService {
	Promise<Void, Void> after(long delay);
}
