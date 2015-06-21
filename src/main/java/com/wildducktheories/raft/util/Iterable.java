package com.wildducktheories.raft.util;

/**
 * An iterable type can return its next log entry.
 */
public interface Iterable<T> {
	T successor();
	T predecessor();
}
