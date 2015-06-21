package com.wildducktheories.raft.encoding;


public interface Decoder<T, E> {
	/**
	 * Decode an instance of type E into an instance of type T.
	 * @param The encoded instance.
	 * @return The the decoded instance.
	 */
	T decode(E e);
}
