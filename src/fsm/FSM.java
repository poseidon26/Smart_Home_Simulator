package fsm;

import java.util.*;

/**
 * General purpose Finite State Machine (FSM) class.
 * This class manages state transitions and events.
 *
 * @param <S> State type
 * @param <E> Event type
 */
public class FSM<S, E> {
    private S currentState;
    private S initialState;
    private Set<S> finalStates = new HashSet<>();
    private Map<S, Map<E, S>> transitions = new HashMap<>();
    private List<FSMStateChangeListener<S>> stateChangeListeners = new ArrayList<>();

    /**
     * Creates an FSM with the specified initial state.
     *
     * @param initialState Initial state
     */
    public FSM(S initialState) {
        this.initialState = initialState;
        this.currentState = initialState;
    }

    /**
     * Adds a transition for a state and event.
     *
     * @param fromState Source state
     * @param event Triggering event
     * @param toState Target state
     * @return This FSM instance (for Builder pattern)
     */
    public FSM<S, E> addTransition(S fromState, E event, S toState) {
        transitions.computeIfAbsent(fromState, k -> new HashMap<>()).put(event, toState);
        return this;
    }

    /**
     * Adds a final state.
     *
     * @param state State to be added as a final state
     * @return This FSM instance
     */
    public FSM<S, E> addFinalState(S state) {
        finalStates.add(state);
        return this;
    }

    /**
     * Processes an event and makes a state change if necessary.
     *
     * @param event Event to process
     * @return Whether a state change occurred
     */
    public boolean processEvent(E event) {
        Map<E, S> stateTransitions = transitions.get(currentState);
        
        if (stateTransitions != null && stateTransitions.containsKey(event)) {
            S oldState = currentState;
            currentState = stateTransitions.get(event);
            
            // Notify state change listeners
            for (FSMStateChangeListener<S> listener : stateChangeListeners) {
                listener.onStateChanged(oldState, currentState, event);
            }
            
            return true;
        }
        
        return false;
    }

    /**
     * Resets the FSM to its initial state.
     */
    public void reset() {
        currentState = initialState;
    }

    /**
     * Checks if the current state is a final state.
     *
     * @return True if the current state is a final state
     */
    public boolean isInFinalState() {
        return finalStates.contains(currentState);
    }

    /**
     * Returns the current state.
     *
     * @return Current state
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * Adds a state change listener.
     *
     * @param listener Listener to add
     */
    public void addStateChangeListener(FSMStateChangeListener<S> listener) {
        stateChangeListeners.add(listener);
    }

    /**
     * Returns the current state and transitions of the FSM as a String.
     *
     * @return String representation of the FSM
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FSM{");
        sb.append("currentState=").append(currentState);
        sb.append(", initialState=").append(initialState);
        sb.append(", finalStates=").append(finalStates);
        sb.append(", transitions=").append(transitions);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Interface for listening to state changes.
     *
     * @param <S> State type
     */
    public interface FSMStateChangeListener<S> {
        /**
         * Called when a state changes.
         *
         * @param oldState Old state
         * @param newState New state
         * @param event Event that caused the change
         */
        void onStateChanged(S oldState, S newState, Object event);
    }
}