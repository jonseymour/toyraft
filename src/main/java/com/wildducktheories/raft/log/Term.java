package com.wildducktheories.raft.log;

import com.wildducktheories.raft.util.Iterable;

/**
 * A monotonically increasing term identifier. A term represents
 * a contiguous sequence of log indexes which are authored by the
 * consensus leader. It increases each time a leader election
 * is called.
 */
public interface Term 
	extends Comparable<Term>, Iterable<Term>
{

}
