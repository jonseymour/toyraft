package com.wildducktheories.raft.log;


public final class Key 
	implements Comparable<Key>
{
	private final Term term;
	private final Index index;
	
	public Key(Term term, Index index) {
		super();
		this.term = term;
		this.index = index;
	}
	public final Term getTerm() {
		return term;
	}
	public final Index getIndex() {
		return index;
	}
	
	@Override
	public int compareTo(Key o) {
		int r = term.compareTo(o.term); 
		if (r != 0) {
			return r;
		} else {
			return index.compareTo(o.index);
		}
	}
	
	@Override
	public int hashCode() {
		return term.hashCode()^index.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Key) {
			Key tmp = (Key)obj;
			return term.equals(tmp.term) && index.equals(tmp.index);
		} else {
			return false;
		}
	}
	
	
}
