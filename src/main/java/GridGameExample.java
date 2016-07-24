import burlap.behavior.learningrate.ExponentialDecayLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.stochasticgames.PolicyFromJointPolicy;
import burlap.behavior.stochasticgames.agents.interfacing.singleagent.LearningAgentToSGAgentInterface;
import burlap.behavior.stochasticgames.agents.madp.MultiAgentDPPlanningAgent;
import burlap.behavior.stochasticgames.agents.maql.MAQLFactory;
import burlap.behavior.stochasticgames.agents.maql.MultiAgentQLearning;
import burlap.behavior.stochasticgames.auxiliary.GameSequenceVisualizer;
import burlap.behavior.stochasticgames.madynamicprogramming.backupOperators.CoCoQ;
import burlap.behavior.stochasticgames.madynamicprogramming.backupOperators.CorrelatedQ;
import burlap.behavior.stochasticgames.madynamicprogramming.backupOperators.MinMaxQ;
import burlap.behavior.stochasticgames.madynamicprogramming.dpplanners.MAValueIteration;
import burlap.behavior.stochasticgames.madynamicprogramming.policies.ECorrelatedQJointPolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.policies.EGreedyMaxWellfare;
import burlap.behavior.stochasticgames.madynamicprogramming.policies.EMinMaxPolicy;
import burlap.behavior.stochasticgames.solvers.CorrelatedEquilibriumSolver;
import burlap.behavior.valuefunction.ConstantValueFunction;
import burlap.behavior.valuefunction.QFunction;
import burlap.debugtools.DPrint;
import burlap.domain.stochasticgames.gridgame.GGVisualizer;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;
import burlap.mdp.stochasticgames.oo.OOSGDomain;
import burlap.mdp.stochasticgames.world.World;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;
import burlap.visualizer.Visualizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example code showing the usage of CoCo-Q and Correlated-Q operators with planning (VI) and learning (Q-learning),
 * and using single agent learning algorithms on two-player Grid Games (a multi-agent stochastic game).
 * From main, comment/uncomment the example method you want to run.
 * @author James MacGlashan.
 */
public class GridGameExample {

    public static void FoeQ(){
        //Setting up GridGame World parameters
        SoccerGame gridGame = new SoccerGame();
        final OOSGDomain domain = gridGame.generateDomain();
        final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();
        final State s = SoccerGame.getSoccerInitialState();
        JointRewardFunction rf = new SoccerGame.GGJointRewardFunction(domain, -1, 100, false); //set reward parameters
        TerminalFunction tf = new SoccerGame.GGTerminalFunction(domain);

        // MultiAgentQLearning parameters
        double gamma = 0.9;
        double epsilon = 0.001; //expoloration?
        double initialLearningRate = 1.0;
        double decayRate = 0.99;
        double minimumLearningRate = 0.001;
        QFunction initQ = new ConstantValueFunction(1.0);
        LearningRate learningRate = new ExponentialDecayLR(initialLearningRate, decayRate, minimumLearningRate);
        SGAgentType at = GridGame.getStandardGridGameAgentType(domain);
        //Decay rate --> updates learning rate after every stage, NOT game. What is a stage?

        MultiAgentQLearning a0 = new MultiAgentQLearning(domain, gamma, learningRate, hashingFactory, initQ, new MinMaxQ(), true, "agent0", at);
        MultiAgentQLearning a1 = new MultiAgentQLearning(domain, gamma, learningRate, hashingFactory, initQ, new MinMaxQ(), true, "agent1", at);
        //Then for Friend-Q, I swap out "new MinMaxQ()" for "new MaxQ()".

        World w = new World(domain, rf, tf, s);

        EMinMaxPolicy ja0 = new EMinMaxPolicy(a0, epsilon, 0);
        EMinMaxPolicy ja1 = new EMinMaxPolicy(a1, epsilon, 1);

        ja0.setAgentsInJointPolicyFromWorld(w);
        ja1.setAgentsInJointPolicyFromWorld(w);

        a0.setLearningPolicy(new PolicyFromJointPolicy(0,ja0));
        a1.setLearningPolicy(new PolicyFromJointPolicy(1,ja1));


        w.join(a0);
        w.join(a1);
        //System.out.println(s.variableKeys());
        GameEpisode ga = null;
        List<GameEpisode> games = new ArrayList<GameEpisode>();

        for(int i = 0; i < 10; i++){
            ga = w.runGame();
            games.add(ga);
        }

        Visualizer v = GGVisualizer.getVisualizer(9, 9);
        new GameSequenceVisualizer(v, domain, games);



    }
    public static void VICorrelatedTest(){

        //SoccerGame soccer = new SoccerGame();
        SoccerGame gridGame = new SoccerGame();

        //GridGame gridGame = new GridGame();
        final OOSGDomain domain = gridGame.generateDomain();

        final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        final State s = SoccerGame.getSoccerInitialState();

        JointRewardFunction rf = new SoccerGame.GGJointRewardFunction(domain, -1, 100, false);
        TerminalFunction tf = new SoccerGame.GGTerminalFunction(domain);

        SGAgentType at = GridGame.getStandardGridGameAgentType(domain);
        MAValueIteration vi = new MAValueIteration(domain, rf, tf, 0.99, hashingFactory, 0., new CorrelatedQ(CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective.UTILITARIAN), 0.00015, 50);

        World w = new World(domain, rf, tf, s);


        //for correlated Q, use a correlated equilibrium policy joint policy
        ECorrelatedQJointPolicy jp0 = new ECorrelatedQJointPolicy(CorrelatedEquilibriumSolver.CorrelatedEquilibriumObjective.UTILITARIAN, 0.);


        MultiAgentDPPlanningAgent a0 = new MultiAgentDPPlanningAgent(domain, vi, new PolicyFromJointPolicy(0, jp0, true), "agent0", at);
        MultiAgentDPPlanningAgent a1 = new MultiAgentDPPlanningAgent(domain, vi, new PolicyFromJointPolicy(1, jp0, true), "agent1", at);

        w.join(a0);
        w.join(a1);
        //System.out.println(s.variableKeys());
        GameEpisode ga = null;
        List<GameEpisode> games = new ArrayList<GameEpisode>();

        for(int i = 0; i < 10; i++){
            ga = w.runGame();
            games.add(ga);
        }

        Visualizer v = GGVisualizer.getVisualizer(9, 9);
        new GameSequenceVisualizer(v, domain, games);

    }

    public static void QLCoCoTest(){

        GridGame gridGame = new GridGame();
        final OOSGDomain domain = gridGame.generateDomain();

        final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        final State s = GridGame.getPrisonersDilemmaInitialState();
        JointRewardFunction rf = new GridGame.GGJointRewardFunction(domain, -1, 100, false);
        TerminalFunction tf = new GridGame.GGTerminalFunction(domain);
        SGAgentType at = GridGame.getStandardGridGameAgentType(domain);

        World w = new World(domain, rf, tf, s);

        final double discount = 0.95;
        final double learningRate = 0.1;
        final double defaultQ = 100;

        MultiAgentQLearning a0 = new MultiAgentQLearning(domain, discount, learningRate, hashingFactory, defaultQ, new CoCoQ(), true, "agent0", at);
        MultiAgentQLearning a1 = new MultiAgentQLearning(domain, discount, learningRate, hashingFactory, defaultQ, new CoCoQ(), true, "agent1", at);

        w.join(a0);
        w.join(a1);


        //don't have the world print out debug info (comment out if you want to see it!)
        DPrint.toggleCode(w.getDebugId(), false);

        System.out.println("Starting training");
        int ngames = 1000;
        List<GameEpisode> games = new ArrayList<GameEpisode>();
        for(int i = 0; i < ngames; i++){
            GameEpisode ga = w.runGame();
            games.add(ga);
            if(i % 10 == 0){
                System.out.println("Game: " + i + ": " + ga.maxTimeStep());
            }
        }

        System.out.println("Finished training");


        Visualizer v = GGVisualizer.getVisualizer(9, 9);
        new GameSequenceVisualizer(v, domain, games);

    }


    public static void saInterface(){

        GridGame gridGame = new GridGame();
        final OOSGDomain domain = gridGame.generateDomain();

        final HashableStateFactory hashingFactory = new SimpleHashableStateFactory();

        final State s = GridGame.getSimpleGameInitialState();
        JointRewardFunction rf = new GridGame.GGJointRewardFunction(domain, -1, 100, false);
        TerminalFunction tf = new GridGame.GGTerminalFunction(domain);
        SGAgentType at = GridGame.getStandardGridGameAgentType(domain);

        World w = new World(domain, rf, tf, s);

        //single agent Q-learning algorithms which will operate in our stochastic game
        //don't need to specify the domain, because the single agent interface will provide it
        QLearning ql1 = new QLearning(null, 0.99, new SimpleHashableStateFactory(), 0, 0.1);
        QLearning ql2 = new QLearning(null, 0.99, new SimpleHashableStateFactory(), 0, 0.1);

        //create a single-agent interface for each of our learning algorithm instances
        LearningAgentToSGAgentInterface a1 = new LearningAgentToSGAgentInterface(domain, ql1, "agent0", at);
        LearningAgentToSGAgentInterface a2 = new LearningAgentToSGAgentInterface(domain, ql2, "agent1", at);

        w.join(a1);
        w.join(a2);

        //don't have the world print out debug info (comment out if you want to see it!)
        DPrint.toggleCode(w.getDebugId(), false);

        System.out.println("Starting training");
        int ngames = 1000;
        List<GameEpisode> gas = new ArrayList<GameEpisode>(ngames);
        for(int i = 0; i < ngames; i++){
            GameEpisode ga = w.runGame();
            gas.add(ga);
            if(i % 10 == 0){
                System.out.println("Game: " + i + ": " + ga.maxTimeStep());
            }
        }

        System.out.println("Finished training");


        Visualizer v = GGVisualizer.getVisualizer(9, 9);
        new GameSequenceVisualizer(v, domain, gas);
    }

    public static void main(String[] args) {
        FoeQ();

        //VICoCoTest();
        //VICorrelatedTest();
        //QLCoCoTest();
        //saInterface();
    }
}