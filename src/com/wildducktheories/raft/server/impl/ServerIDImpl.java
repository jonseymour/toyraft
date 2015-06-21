package com.wildducktheories.raft.server.impl;

public class ServerIDImpl {
	
	private final int id;
	
	public ServerIDImpl(int id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ServerIDImpl) {
			return id == ((ServerIDImpl)o).id;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return id;
	}
}
