package com.wildducktheories.raft.server.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wildducktheories.promise.Deferred;
import com.wildducktheories.promise.DoneCallback;
import com.wildducktheories.promise.FailCallback;
import com.wildducktheories.promise.Promise;
import com.wildducktheories.promise.PromiseAPI;
import com.wildducktheories.raft.configuration.Configuration;
import com.wildducktheories.raft.log.Entry;
import com.wildducktheories.raft.log.Index;
import com.wildducktheories.raft.log.Key;
import com.wildducktheories.raft.log.Log;
import com.wildducktheories.raft.log.Term;
import com.wildducktheories.raft.rpc.AppendRequest;
import com.wildducktheories.raft.rpc.AppendResponse;
import com.wildducktheories.raft.rpc.ConsensusService;
import com.wildducktheories.raft.rpc.VoteRequest;
import com.wildducktheories.raft.rpc.VoteResponse;
import com.wildducktheories.raft.rpc.impl.AppendRequestImpl;
import com.wildducktheories.raft.rpc.impl.AppendResponseImpl;
import com.wildducktheories.raft.rpc.impl.VoteRequestImpl;
import com.wildducktheories.raft.rpc.impl.VoteResponseImpl;
import com.wildducktheories.raft.server.ServerID;
import com.wildducktheories.raft.statemachine.Reply;
import com.wildducktheories.raft.statemachine.StateMachine;
import com.wildducktheories.raft.time.TimingService;
import com.wildducktheories.tasklet.Directive;
import com.wildducktheories.tasklet.Scheduler;
import com.wildducktheories.tasklet.SchedulerAPI;
import com.wildducktheories.tasklet.Tasklet;
import com.wildducktheories.tasklet.lib.TaskletLib;

/**
 * This class implements a server that participates in the Raft consensus protocol.
 * <p>
 * The underlying promise framework guarantees that all messages are delivered
 * on a single thread which avoids the need for synchronization within the implementation
 * of the service itself.
 * </p>
 * @author jonseymour
 */
public class ServerImpl 
	implements Runnable
{

	/**
	 * The behaviour interface of server states.
	 */
	private interface State {
		StateKey getKey();
	}

	/**
	 * The token that represents each state.
	 */
	private enum StateKey implements State {
		FOLLOWER,
		CANDIDATE,
		LEADER;
		
		public StateKey getKey() {
			return this;
		}
	}
	
	/**
	 * The id of this server.
	 */
	private final ServerID id;
	
	/**
	 * The current state of this server.
	 */
	private State state = getState(StateKey.FOLLOWER);
	
	/**
	 * The configuration of the cluster.
	 */
	private Configuration configuration;
	
	/**
	 * The current leadership term.
	 */
	private Term currentTerm;
	
	/**
	 * The server for which this server voted, in this term.
	 */
	private ServerID votedFor;
	
	/**
	 * The command log.
	 */
	private Log log;
	
	/**
	 * Set of votes received for this server.
	 */
	private Set<ServerID> votesReceived = new HashSet<ServerID>();
	
	/**
	 * The service used for obtaining timers.
	 */
	private TimingService timingService;
	
	/**
	 * The last index we think we have sent to each follower. This value is not necessarily correct
	 * until we have received a message from the follower in the current term. The value
	 * does NOT imply the receiver and the follower match upto an including the matching index.
	 * 
	 * This map is used to optimize performance.
	 */
	private Map<ServerID, Index> lastIndex = new HashMap<ServerID, Index>();	
	
	/**
	 * The last index for which we know that both leader and follower match.
	 */
	private Map<ServerID, Index> confirmedIndex = new HashMap<ServerID, Index>();
	
	/**
	 * The local state machine.
	 */
	private StateMachine stateMachine;
	
	/**
	 * Whether we are waiting for the completion of a command by the local state machine.
	 */
	private boolean commandPending = false;
	
	/**
	 * The scheduler of the main thread.
	 */
	private Scheduler scheduler;
	
	/**
	 * @param key The state key.
	 * @return A Directive interface for the receiver for the specified key. Currently is just the key. In future, may 
	 * also contain receiver specific behaviour and state.
	 */
	private State getState(final StateKey key) {
		return key;
	}
	
	/**
	 * Ensure that the receiver's term matches the specified term. If we are demoted from
	 * leader then restart the election timeouts. If there is a change of term, demote
	 * ourselves to follower.
	 * <p>
	 * @param t The new term.
	 */
	private void updateTerm(Term t) {
		if (t.compareTo(currentTerm) > 0) {
			currentTerm = t;
			votedFor = null;
			state = getState(StateKey.FOLLOWER);
		}
		if (state.getKey() != StateKey.LEADER) {
			scheduleElection();
		}
	}
	

	/**
	 * Schedule an election.
	 */
	private void scheduleElection() {
		timingService.after(configuration.getElectionTimeout()).done(new DoneCallback<Void>() {
			@Override
			public void onDone(Void p) {
				callElection();
			}			
		});
	}


	public ServerImpl(ServerID id) {
		super();
		this.id = id;
	}


	/**
	 * Method that handles append requests from other servers.
	 */
	private AppendResponse append(AppendRequest request)  {
		updateTerm(request.getTerm());
		if (request.getTerm().compareTo(currentTerm) < 0) {
			return new AppendResponseImpl(currentTerm, false, request.getLastKey().getIndex());
		}
		
		if (state.getKey() == StateKey.CANDIDATE) {
			state = getState(StateKey.FOLLOWER);
		}
		
		if (state.getKey() == StateKey.FOLLOWER) {
			if (request.getLastKey().getIndex().compareTo(log.getLast()) < 0) {
				log.rollback(request.getLastKey().getIndex());
			}
			if (!log.getEntry(log.getLast()).getKey().equals(request.getLastKey())) {
				return new AppendResponseImpl(currentTerm, false, log.getLast());
			} else {
				for (Entry e : request.getEntries()) {
					final Key k = e.getKey();
					log.write(e.getCommand(), k.getTerm());
					confirmedIndex.put(id, k.getIndex());
				}
				if (log.getCommit() != request.getLeaderCommit()) {
					log.commit(request.getLeaderCommit());
					updateStateMachine(); // update the local state machine
				}
				return new AppendResponseImpl(currentTerm, true, log.getLast());
			}
		} else {
			return new AppendResponseImpl(currentTerm, false, request.getLastKey().getIndex());
		}

	}

	private VoteResponse vote(VoteRequest request) {
		updateTerm(request.getTerm());
		final int termCompare = request.getTerm().compareTo(currentTerm);
		if (termCompare < 0) {
			return new VoteResponseImpl(currentTerm, log.getLast(), false);
		} else {
			int lastIndexCmp = request.getLastKey().getIndex().compareTo(log.getLast());
			if ((votedFor == null || votedFor.equals(request.getCandidateID())) 
				&& lastIndexCmp >= 0)  {	
				votedFor = request.getCandidateID();
				state = getState(StateKey.FOLLOWER);
				return new VoteResponseImpl(currentTerm, log.getLast(), true);
			} else {
				if (lastIndexCmp > 0 || request.getLastKey().equals(log.getEntry(log.getLast()).getKey())) {
					// if the receiver's log is shorter or the last terms match
					return new VoteResponseImpl(currentTerm, log.getLast(), false);
				} else {
					// if the receiver's log is the same length and the last terms don't match, then step back one
					return new VoteResponseImpl(currentTerm, log.getLast().predecessor(), false);
				}
			}
		}
	}
	
	private void onVoteResponse(ServerID id, VoteResponse response) {
		updateTerm(response.getTerm());
		if (!response.getTerm().equals(currentTerm)) {
			// dropped response.
			return;
		}

		if (response.wasSuccessful()) {
			votesReceived.add(id);
		}
		lastIndex.put(id, response.getLast());
		
		switch (state.getKey()) {
		case CANDIDATE:
			if (configuration.quorumAchieved(votesReceived)) {
				state = getState(StateKey.LEADER);
				// write leader changed noop into log so leader can 
				// guarantee previous commits are committed.
				onLeaderHeartbeat();
			}
			break;
		case FOLLOWER:
		case LEADER:
		default:
			// no action required
		}
	}
	
	private ConsensusService getConsensusService(ServerID id) {
		throw new UnsupportedOperationException("");
	}
	
	/**
	 * Call an election, except if we are leader.
	 */
	private void callElection() {
		if (state.getKey() == StateKey.LEADER) {
			return;
		}
		updateTerm(currentTerm.successor());
		state=getState(StateKey.CANDIDATE);
		votedFor = id;
		votesReceived.clear();
		votesReceived.add(id);
		resetFollowers();
		
		// notify all followers
				
		for (final ServerID followerID : configuration.getFollowers()) {
			getConsensusService(followerID)
				.vote(new VoteRequestImpl(id, currentTerm, log.getEntry(log.getLast()).getKey()))
				.done(new DoneCallback<VoteResponse>(){
					@Override
					public void onDone(VoteResponse p) {
						onVoteResponse(followerID, p);
					}					
				});
		}
	}
	
	/**
	 * Update followers on each heartbeat, then schedule another heartbeat.
	 */
	private void onLeaderHeartbeat() {
		if (state.getKey() == StateKey.LEADER) {
			updateFollowers();
			timingService.after(configuration.getHeartBeatTimeout()).done(
				new DoneCallback<Void>() {
					public void onDone(Void p) {
						onLeaderHeartbeat();
					};
				}
			);
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param response
	 */
	private void onAppendResponse(ServerID id, AppendResponse response) {
		updateTerm(response.getTerm());
		if (response.getTerm().compareTo(currentTerm) < 0) {
			conditionallyUpdateFollower(id);
			return;
		}
		if (state.getKey() == StateKey.LEADER) {
			
			if (response.wasSuccessful()) {
				// need to check that both these always advance to cope with out of order response.
				if (!confirmedIndex.containsKey(id) || confirmedIndex.get(id).compareTo(response.getLast()) <= 0) {
					// confirmedIndex always advances on successful appends
					confirmedIndex.put(id, response.getLast());					
					lastIndex.put(id,  response.getLast());
				} 
			} else {
				// if the follower is further behind that we think, then trust the follower's view of log size
				final Index last = lastIndex.get(id);
				if (last == null || response.getLast().compareTo(last) < 0) {
					lastIndex.put(id, response.getLast());
				} else {
					// otherwise, the follower's log needs to be truncated by at least one.
					lastIndex.put(id, last.predecessor());
				}
			}
			
			// check whether we can advance the commit index
			
			final Index next = configuration.updateCommit(log.getCommit(), confirmedIndex);
			
			if (!log.getCommit().equals(next)) {
				// we have advanced the commit
				log.commit(next);
				// Tell all followers now so that their state machines can catch up.
				updateFollowers();
				// Update the local state machine.
				updateStateMachine();
			} else {
				// Conditionally update this follower if it is still not up to date.			
				conditionallyUpdateFollower(id);
			}
		}
	}
	
	/**
	 * Update the local state machine by sending it a command and waiting for a reply.
	 */
	private void updateStateMachine() {
		if (!commandPending && log.getNext().compareTo(log.getCommit()) <= 0) {
			final Entry entry = log.getEntry(log.getNext());
			commandPending = true;
			stateMachine
				.send(entry.getCommand())
				.done(new DoneCallback<Reply>() {
					@Override
					public void onDone(Reply p) {
						log.apply(entry.getKey().getIndex());
						commandPending = false;
						updateStateMachine();
					}
				})
				.fail(new FailCallback<IOException>() {

					@Override
					public void onFail(IOException f) {
						commandPending = false;
					}
					
				});
		}
	}
	
	/**
	 * Optimistically re-initialize lastIndex to be the current server's state. Clear the confirmedIndex so that
	 * we only make commit decisions based on follower replies we receive in this term.
	 */
	private void resetFollowers() {
		lastIndex.clear();
		confirmedIndex.clear();
		confirmedIndex.put(id, log.getLast());
		for (ServerID follower : configuration.getFollowers()) {
			lastIndex.put(follower, log.getLast());
		}
	}
	
	/**
	 * Unconditionally send append log entries messages to all followers.
	 */
	private void updateFollowers() {
		for (final ServerID followerID : configuration.getFollowers()) {
			updateFollower(followerID);
		}
	}
	
	/** 
	 * Unconditionally update the specified follower.
	 * @param followerID
	 */
	private void updateFollower(final ServerID followerID) {
		Index next = lastIndex.get(followerID);
		if (next == null) {
			throw new IllegalStateException("index == null: id == "+id);
		}
		final List<Entry> entries = log.getFrom(next.successor());
		final AppendRequest request = new AppendRequestImpl(id, currentTerm, log.getEntry(next).getKey(), log.getCommit(), entries);
		getConsensusService(followerID)
			.append(request)
			.done(new DoneCallback<AppendResponse>(){
				@Override
				public void onDone(AppendResponse p) {
					onAppendResponse(followerID, p);
				}					
			});
		
	}
	
	/**
	 * Update the specified follower iff the follower's log is behind the leader's log.
	 * @param followerID The follower id.
	 */
	private void conditionallyUpdateFollower(ServerID followerID)
	{
		final Index index = lastIndex.get(followerID);
		if (index == null) {
			throw new IllegalStateException("index == null: id == "+followerID);
		}
		if (index.compareTo(log.getLast()) < 0) {
			updateFollower(followerID);
		}
	}
	
	public ConsensusService getConsensusService() {
		return new ConsensusService() {

			@Override
			public Promise<AppendResponse, IOException> append(final AppendRequest request) {
				final Deferred<AppendResponse, IOException> deferred = PromiseAPI.get().deferred();
				scheduler.schedule(new Tasklet() {

					@Override
					public Directive task() {
						try {
							deferred.resolve(ServerImpl.this.append(request));
						} catch (RuntimeException e) {
							deferred.reject(new IOException(e));
						}
						return Directive.DONE;
					}
					
				}, Directive.SYNC);
				return deferred.promise();
			}

			@Override
			public Promise<VoteResponse, IOException> vote(final VoteRequest request) {
				final Deferred<VoteResponse, IOException> deferred = PromiseAPI.get().deferred();
				scheduler.schedule(new Tasklet() {

					@Override
					public Directive task() {
						try {
							deferred.resolve(ServerImpl.this.vote(request));
						} catch (RuntimeException e) {
							deferred.reject(new IOException(e));
						}
						return Directive.DONE;
					}
					
				}, Directive.SYNC);
				return deferred.promise();
			}
			
		};
	}
	
	/**
	 * Run the server.
	 */
	public void run() {
		SchedulerAPI
			.get()
			.with(scheduler, TaskletLib.WAIT)
			.run();
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setTimingService(TimingService timingService) {
		this.timingService = timingService;
	}

	public void setStateMachine(StateMachine stateMachine) {
		this.stateMachine = stateMachine;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}
