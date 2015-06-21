package com.wildducktheories.raft.statemachine;

import java.io.IOException;

import com.wildducktheories.promise.Promise;

public interface StateMachine {
	Promise<Reply, IOException> send(Command m);
}
