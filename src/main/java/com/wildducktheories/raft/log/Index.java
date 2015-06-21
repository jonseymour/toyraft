package com.wildducktheories.raft.log;

import com.wildducktheories.raft.util.Iterable;

/**
 * An index is a strictly-monotonically increasing index into a log.
 * @author jonseymour
 */
public interface Index 
	extends Comparable<Index>, Iterable<Index>
{

}
