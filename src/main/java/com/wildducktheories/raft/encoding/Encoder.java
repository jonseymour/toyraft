package com.wildducktheories.raft.encoding;


/**
 * Encode an instance of type T as an instance of type E.
 * @param <T>
 */
public interface Encoder<T, E> {
		E encode(T t);
}
