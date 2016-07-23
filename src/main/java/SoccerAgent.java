import burlap.domain.stochasticgames.gridgame.state.GGAgent;
import burlap.mdp.core.state.UnknownKeyException;

import java.util.Arrays;
import java.util.List;

//import static burlap.domain.stochasticgames.gridgame.GridGame.*;

import static burlap.domain.stochasticgames.gridgame.GridGame.VAR_PN;
import static burlap.domain.stochasticgames.gridgame.GridGame.VAR_X;
import static burlap.domain.stochasticgames.gridgame.GridGame.VAR_Y;

/**
 * Created by hwkim on 7/22/16.
 */
public class SoccerAgent extends GGAgent{
    public boolean hasBall;
    private static final List<Object> keys = Arrays.<Object>asList(VAR_X, VAR_Y, VAR_PN, SoccerGame.BALL);

    public SoccerAgent(int x, int y, int player, java.lang.String name, boolean hasBall) {
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
            System.out.println(hasBall);
            return hasBall;
        }
        else{
            throw new UnknownKeyException(variableKey);
        }
    }
}
