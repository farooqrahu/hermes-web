package es.jyago.hermes.person;


public class SessionStateMachine {

    enum Session_states {

        NORMAL(1), IN_SESSION(2), REST(3);

        public int value;

        Session_states(int v) {
            value = v;
        }
    };

    private Session_states currentState;

    public SessionStateMachine() {
        currentState = Session_states.NORMAL;
    }

    public boolean isInactive() {
        return currentState == Session_states.NORMAL;
    }

    public boolean isInSession() {
        return currentState == Session_states.IN_SESSION;
    }

    public boolean isResting() {
        return currentState == Session_states.REST;
    }
    
    public boolean changeToNormal() {
        // Se permite transicionar a NORMAL desde cualquier estado.
        currentState = Session_states.NORMAL;
        return true;
    }

    public boolean changeToInSession() {
        // Se permite transicionar a IN_SESSION desde cualquier estado.
        currentState = Session_states.IN_SESSION;
        return true;
    }

    public boolean changeToRest() {
        // Si estamos en estado NORMAL, no podemos cambiar a REST.
        if (currentState == Session_states.NORMAL) {
            return false;
        }
        
        currentState = Session_states.REST;
        return true;
    }
}
