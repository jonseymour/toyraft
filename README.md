#NAME
toyraft - a toy [Raft](http://raftconsensus.github.io) implementation.

#DESCRIPTION
This project provides a toy-implementation of the Raft consensus protocol. The intention is to provide a Java implementation of the Raft consensus protocol without committing too early to a particular physical implementation of any of the core components. The main aim of this project to act as a framework for exploring the Raft consensus protocol in more detail by those interested in doing so.

While the primary intent of this project is not to implement an actual production implementation of the protocol, the abstractions are chosen in a way that should not preclude a production implementation should one choose to do so simply by choosing implementation of the abstractions that mapped each of the abstractions onto physical implementations. However, if you actually need a working Java Raft implementation, you might instead use one of the many existing Java Raft implementations for this purpose.

#STATUS
This implementation is currently incomplete.

