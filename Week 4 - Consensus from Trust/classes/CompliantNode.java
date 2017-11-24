import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    
    Set<Transaction> myPendingTransactions = new HashSet();
    boolean[] myFollowees = {};
    
    double myP_graph;
    double myP_malicious;
    double myP_txDistribution;
    int myNumRounds;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        
        myP_graph = p_graph;
        myP_malicious = p_malicious;
        myP_txDistribution = p_txDistribution;
        myNumRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        myFollowees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        
        myPendingTransactions = pendingTransactions;
    }

    @Override
    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return myPendingTransactions;
    }

    @Override
    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        
        for (Candidate candidate : candidates)
        {
            if (myFollowees[candidate.sender]) //if sender is it's followee
            {
                myPendingTransactions.add(candidate.tx);
            }
        }
    }
}
