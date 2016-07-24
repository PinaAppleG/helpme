import burlap.domain.stochasticgames.gridgame.state.GGAgent;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;

import java.util.Arrays;
import java.util.List;

import static burlap.domain.stochasticgames.gridgame.GridGame.*;

/**
 * Created by hwkim on 7/22/16.
 */
public class SoccerAgent extends GGAgent{
    public int hasBall;
    private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_PN, SoccerGame.BALL);

    public SoccerAgent(int x, int y, int player, java.lang.String name, int hasBall) {
        super(x, y, player, name);
        this.hasBall = hasBall;

    }

    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(VAR_X)){
            return x;
        }
        else if(variableKey.equals(VAR_Y)){
            return y;
        }
        else if(variableKey.equals(VAR_PN)){
            return player;
        }
        else if(variableKey.equals(SoccerGame.BALL)) {
            //we hit this
            return hasBall;
        }
        else{
            throw new UnknownKeyException(variableKey);
        }
    }

    @Override
    public MutableState set(Object variableKey, Object value) {

        int i = StateUtilities.stringOrNumber(value).intValue();
        if(variableKey.equals(VAR_X)){
//            System.out.print("VAR_X: ");
//            System.out.println(i);
            this.x = i;
        }
        else if(variableKey.equals(VAR_Y)){
//            System.out.print("VAR_Y: ");
//            System.out.println(i);
            this.y = i;
        }
        else if(variableKey.equals(VAR_PN)){
//            System.out.print("VAR_PN: ");
//            System.out.println(i);
            this.player = i;
        }
        else if(variableKey.equals(SoccerGame.BALL)){
            System.out.print("BALL: ");
            System.out.println(i);
            this.hasBall = i;
        }
        else{
            throw new UnknownKeyException(variableKey);
        }

        return this;
    }

    @Override
    public ObjectInstance copyWithName(String objectName) {
        return new SoccerAgent(x, y, player, objectName, hasBall);
    }

    @Override
    public State copy() {
        return new SoccerAgent(x, y, player, name, hasBall);
    }

    @Override
    public List<Object> variableKeys() {
        return keys;
    }
}
