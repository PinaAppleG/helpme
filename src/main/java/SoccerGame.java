import burlap.domain.stochasticgames.gridgame.GridGame;
//import burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics;
import burlap.domain.stochasticgames.gridgame.state.GGAgent;
import burlap.domain.stochasticgames.gridgame.state.GGGoal;
import burlap.domain.stochasticgames.gridgame.state.GGWall;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.action.UniversalActionType;
import burlap.mdp.core.oo.OODomain;
import burlap.mdp.core.oo.propositional.GroundedProp;
import burlap.mdp.core.oo.propositional.PropositionalFunction;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.oo.state.generic.GenericOOState;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.oo.OOSGDomain;

import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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


    public static State getSoccerInitialState(){
        //1 = true = hasBall
        Random rand = new Random();
        boolean randomBall = false; //set to true if you want to randomize who starts with ball in each game
        int i = 1;
        int j = 0;
        if(randomBall) {
            i = rand.nextInt(1);
            j = 0;
            if (i == 0) {
                j = 1;
            }
        }

        GenericOOState s = new GenericOOState(
                //GGAgent(int x, int y, int player, java.lang.String name)
                new SoccerAgent(1, 1, 0, "agent0", i),
                new SoccerAgent(2, 1, 1, "agent1", j),
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
            int hasBall = (Integer) agent.get(BALL);

            //find all universal goals
            List<ObjectInstance> goals = s.objectsOfClass(CLASS_GOAL);
            for(ObjectInstance goal : goals){

                int gt = (Integer)goal.get(VAR_GT);
                if(gt == apn+1){

                    int gx = (Integer)goal.get(VAR_X);
                    int gy = (Integer)goal.get(VAR_Y);
                    if(gx == ax && gy == ay && hasBall == 1){
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

    /**
     * Specifies goal rewards and default rewards for agents. Defaults rewards to 0 reward everywhere except transition to unviersal or personal goals which return a reward 1.
     * @author James MacGlashan
     *
     */
    public static class GGJointRewardFunction implements JointRewardFunction {

        PropositionalFunction agentInPersonalGoal;
        PropositionalFunction agentInUniversalGoal;

        double stepCost = -100.;
        double pGoalReward = 100.;
        double uGoalReward = 0;
        boolean noopIncursCost = false;
        Map<Integer, Double> personalGoalRewards = null;

        /**
         * Initializes for a given domain. Defaults rewards to 0 reward everywhere except transition to unviersal or personal goals which return a reward 1.
         * @param ggDomain the domain
         */
        public GGJointRewardFunction(OODomain ggDomain){
            agentInPersonalGoal = ggDomain.propFunction(GridGame.PF_IN_P_GOAL);
            agentInUniversalGoal = ggDomain.propFunction(GridGame.PF_IN_U_GOAL);
        }

        /**
         * Initializes for a given domain, step cost reward and goal reward.
         * @param ggDomain the domain
         * @param stepCost the reward returned for all transitions except transtions to goal locations
         * @param goalReward the reward returned for transitioning to a personal or universal goal
         * @param noopIncursStepCost if true, then noop actions also incur the stepCost reward; if false, then noops always return 0 reward.
         */
        public GGJointRewardFunction(OODomain ggDomain, double stepCost, double goalReward, boolean noopIncursStepCost){
            agentInPersonalGoal = ggDomain.propFunction(GridGame.PF_IN_P_GOAL);
            agentInUniversalGoal = ggDomain.propFunction(GridGame.PF_IN_U_GOAL);
            this.stepCost = stepCost;
            this.pGoalReward = this.uGoalReward = goalReward;
            this.noopIncursCost = noopIncursStepCost;
        }


        /**
         * Initializes for a given domain, step cost reward, personal goal reward, and universal goal reward.
         * @param ggDomain the domain
         * @param stepCost the reward returned for all transitions except transtions to goal locations
         * @param personalGoalReward the reward returned for transitions to a personal goal
         * @param universalGoalReward the reward returned for transitions to a universal goal
         * @param noopIncursStepCost if true, then noop actions also incur the stepCost reward; if false, then noops always return 0 reward.
         */
        public GGJointRewardFunction(OODomain ggDomain, double stepCost, double personalGoalReward, double universalGoalReward, boolean noopIncursStepCost){
            agentInPersonalGoal = ggDomain.propFunction(GridGame.PF_IN_P_GOAL);
            agentInUniversalGoal = ggDomain.propFunction(GridGame.PF_IN_U_GOAL);
            this.stepCost = stepCost;
            this.pGoalReward = personalGoalReward;
            this.uGoalReward = universalGoalReward;
            this.noopIncursCost = noopIncursStepCost;
        }

        /**
         * Initializes for a given domain, step cost reward, universal goal reward, and unique personal goal reward for each player.
         * @param ggDomain the domain
         * @param stepCost the reward returned for all transitions except transtions to goal locations
         * @param universalGoalReward the reward returned for transitions to a universal goal
         * @param noopIncursStepCost if true, then noop actions also incur the stepCost reward; if false, then noops always return 0 reward.
         * @param personalGoalRewards a map from player numbers to their personal goal reward (the first player number is 0)
         */
        public GGJointRewardFunction(OODomain ggDomain, double stepCost, double universalGoalReward, boolean noopIncursStepCost, Map<Integer, Double> personalGoalRewards){

            agentInPersonalGoal = ggDomain.propFunction(GridGame.PF_IN_P_GOAL);
            agentInUniversalGoal = ggDomain.propFunction(GridGame.PF_IN_U_GOAL);
            this.stepCost = stepCost;
            this.uGoalReward = universalGoalReward;
            this.noopIncursCost = noopIncursStepCost;
            this.personalGoalRewards = personalGoalRewards;

        }

        public double[] reward(State s, JointAction ja, State sp) {
            OOState osp = (OOState)sp;
            double [] rewards = new double[ja.size()];

            //get all agents and initialize reward to default
            List <ObjectInstance> obs = osp.objectsOfClass(GridGame.CLASS_AGENT);
            for(ObjectInstance o : obs){
                int aid = ((GGAgent)o).player;
                rewards[aid] = this.defaultCost(aid, ja);
            }

            //check for any agents that reached a universal goal location and give them a goal reward if they did
            //List<GroundedProp> upgps = sp.getAllGroundedPropsFor(agentInUniversalGoal);
            List<GroundedProp> upgps = agentInUniversalGoal.allGroundings((OOState)sp);
            for(GroundedProp gp : upgps){
                String agentName = gp.params[0];
                if(gp.isTrue(osp)){
                    int aid = ((GGAgent)((OOState) sp).object(agentName)).player;
                    rewards[aid] = uGoalReward;
                }
            }

            //check for any agents that reached a personal goal location and give them a goal reward if they did
            //List<GroundedProp> ipgps = sp.getAllGroundedPropsFor(agentInPersonalGoal);
            List<GroundedProp> ipgps = agentInPersonalGoal.allGroundings((OOState)sp);
            for(GroundedProp gp : ipgps){
                String agentName = gp.params[0];
                if(gp.isTrue(osp)){
                    int aid = ((GGAgent)((OOState) sp).object(agentName)).player;
                    rewards[aid] = this.getPersonalGoalReward(osp, agentName);

                    if(aid==0) {
                        rewards[1] = -1 * this.getPersonalGoalReward(osp, "agent1");
                    }
                    else{
                        rewards[0] = -1 * this.getPersonalGoalReward(osp, "agent0");
                    }

                }
            }

            return rewards;
        }


        /**
         * Returns a default cost for an agent assuming the agent didn't transition to a goal state. If noops incur step cost,
         * then this is always the step cost. If noops do not incur step costs and the agent took a noop, then 0 is returned.
         * @param aid the agent of interest
         * @param ja the joint action set
         * @return the default reward; either step cost or 0.
         */
        protected double defaultCost(int aid, JointAction ja){
            if(this.noopIncursCost){
                return this.stepCost;
            }
            else if(ja.action(aid) == null || ja.action(aid).actionName().equals(GridGame.ACTION_NOOP)){
                return 0.;
            }
            return this.stepCost;
        }


        /**
         * Returns the personal goal rewards. If a single common personal goal reward was set then that is returned.
         * If different personal goal rewards were defined for each player number, then that is queried and returned instead.
         * @param s the state in which the agent player numbers are defined
         * @param agentName the agent name for which the person goal reward is to be returned
         * @return the personal goal reward for the specified agent.
         */
        protected double getPersonalGoalReward(OOState s, String agentName){
            if(this.personalGoalRewards == null){
                return this.pGoalReward;
            }

            //Only if individual personal rewards are used. So, no.
            int pn = (Integer)s.object(agentName).get(GridGame.VAR_PN);
            return this.personalGoalRewards.get(pn);

        }

    }


    /**
     * Causes termination when any agent reaches a personal or universal goal location.
     * @author James MacGlashan
     *
     */
    public static class GGTerminalFunction implements TerminalFunction {

        PropositionalFunction agentInPersonalGoal;
        PropositionalFunction agentInUniversalGoal;


        /**
         * Initializes for the given domain
         * @param ggDomain the specific grid world domain.
         */
        public GGTerminalFunction(OODomain ggDomain){
            agentInPersonalGoal = ggDomain.propFunction(GridGame.PF_IN_P_GOAL);
            agentInUniversalGoal = ggDomain.propFunction(GridGame.PF_IN_U_GOAL);
        }


        public boolean isTerminal(State s) {

            //check personal goals; if anyone reached their personal goal, it's game over
            //List<GroundedProp> ipgps = s.getAllGroundedPropsFor(agentInPersonalGoal);
            List<GroundedProp> ipgps = agentInPersonalGoal.allGroundings((OOState)s);
            for(GroundedProp gp : ipgps){
                if(gp.isTrue((OOState)s)){
                    return true;
                }
            }


            //check universal goals; if anyone reached a universal goal, it's game over
            //List<GroundedProp> upgps = s.getAllGroundedPropsFor(agentInUniversalGoal);
            List<GroundedProp> upgps = agentInUniversalGoal.allGroundings((OOState)s);
            for(GroundedProp gp : upgps){
                if(gp.isTrue((OOState)s)){
                    return true;
                }
            }

            return false;
        }

    }




}
