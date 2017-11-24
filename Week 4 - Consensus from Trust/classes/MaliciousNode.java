import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class MaliciousNode implements Node {

    Set<Transaction> myPendingTransactions = new HashSet();
    boolean[] myFollowees = {};
    
    double myP_graph;
    double myP_malicious;
    double myP_txDistribution;
    int myNumRounds;
   
    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    }

    @Override
    public void setFollowees(boolean[] followees) {
        
         myFollowees = followees;
    }

    @Override
    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        
         myPendingTransactions = pendingTransactions;
    }

    @Override
    public Set<Transaction> sendToFollowers() {//random behaviour of malicious
        
        if (Math.random() < 0.8)
            return myPendingTransactions;
        else
            return new HashSet<>();
    }

    @Override
    public void receiveFromFollowees(Set<Candidate> candidates) {
//        return;
    }
}
