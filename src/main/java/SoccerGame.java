import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics;
import burlap.domain.stochasticgames.gridgame.state.GGAgent;
import burlap.domain.stochasticgames.gridgame.state.GGGoal;
import burlap.domain.stochasticgames.gridgame.state.GGWall;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.generic.GenericOOState;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.oo.OOSGDomain;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hwkim on 7/22/16.
 */
public class SoccerGame extends GridGame{

    public static final String BALL = "hasBall";

    List<PropositionalFunction> generatePFs(){
        return Arrays.asList(new AgentInUGoal(PF_IN_U_GOAL), new AgentInPGoal(PF_IN_P_GOAL));
    }

    @Override
    public OOSGDomain generateDomain() {

        OOSGDomain domain = new OOSGDomain();

        domain.addStateClass(CLASS_AGENT, GGAgent.class)
                .addStateClass(CLASS_GOAL, GGGoal.class)
                .addStateClass(CLASS_DIM_H_WALL, GGWall.GGHorizontalWall.class)
                .addStateClass(CLASS_DIM_V_WALL, GGWall.GGVerticalWall.class);

        domain.addActionType(new UniversalActionType(ACTION_NORTH))
                .addActionType(new UniversalActionType(ACTION_SOUTH))
                .addActionType(new UniversalActionType(ACTION_EAST))
                .addActionType(new UniversalActionType(ACTION_WEST))
                .addActionType(new UniversalActionType(ACTION_NOOP));

        OODomain.Helper.addPfsToDomain(domain, this.generatePFs());

        domain.setJointActionModel(new GridGameStandardMechanics(domain, this.semiWallProb));

        return domain;
    }

    /**
     * Returns the initial state for a classic prisoner's dilemma formulated in a Grid Game.
     * @return the grid game prisoner's dilemma initial state
     */
    public static State getSoccerInitialState(){

        GenericOOState s = new GenericOOState(
                //GGAgent(int x, int y, int player, java.lang.String name)
                new SoccerAgent(1, 1, 0, "agent0", true),
                new SoccerAgent(2, 1, 1, "agent1", false),
                //GGGoal(int x, int y, int type, java.lang.String name)
                new GGGoal(0, 0, 2, "g0"),
                new GGGoal(0, 1, 2, "g1"),
                new GGGoal(3, 0, 1, "g2"),
                new GGGoal(3, 1, 1, "g3")
        );

        setBoundaryWalls(s, 4, 2);

        return s;
    }

    static class AgentInPGoal extends PropositionalFunction{

        public AgentInPGoal(String name) {
            super(name, new String[]{CLASS_AGENT});
        }
        @Override
        public boolean isTrue(OOState s, String... params) {

            ObjectInstance agent = s.object(params[0]);
            int ax = (Integer)agent.get(VAR_X);
            int ay = (Integer)agent.get(VAR_Y);
            int apn = (Integer)agent.get(VAR_PN);
            boolean hasBall = (Boolean)agent.get(BALL);

            //find all universal goals
            List<ObjectInstance> goals = s.objectsOfClass(CLASS_GOAL);
            for(ObjectInstance goal : goals){

                int gt = (Integer)goal.get(VAR_GT);
                if(gt == apn+1){

                    int gx = (Integer)goal.get(VAR_X);
                    int gy = (Integer)goal.get(VAR_Y);
                    if(gx == ax && gy == ay && hasBall){
                        return true;
                    }
                }
            }
            return false;
        }
    }

    static class AgentInUGoal extends PropositionalFunction {
        public AgentInUGoal(String name) {
            super(name, new String[]{CLASS_AGENT});
        }
        @Override
        public boolean isTrue(OOState s, String... params) {

            ObjectInstance agent = s.object(params[0]);
            int ax = (Integer)agent.get(VAR_X);
            int ay = (Integer)agent.get(VAR_Y);

            //find all universal goals
            List <ObjectInstance> goals = s.objectsOfClass(CLASS_GOAL);
            for(ObjectInstance goal : goals){

                int gt = (Integer)goal.get(VAR_GT);
                if(gt == 0){

                    int gx = (Integer)goal.get(VAR_X);
                    int gy = (Integer)goal.get(VAR_Y);
                    if(gx == ax && gy == ay){
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
