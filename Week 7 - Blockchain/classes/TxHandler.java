
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	
	private UTXOPool utxoPool;
	ArrayList <UTXO> utxoList;
	
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	
    	this.utxoPool = utxoPool;
    }
    
    public UTXOPool getUTXOPool()
    {
        return utxoPool;
    }
    

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) { 
        // IMPLEMENT THIS
    	ArrayList <Transaction.Output> txOutputs = tx.getOutputs();
    	ArrayList <Transaction.Input> txInputs = tx.getInputs();
    	
    	utxoList = utxoPool.getAllUTXO();

    	boolean inUTXOList = areClaimedTransactionOutputsInUTXOPool(txInputs);   
    	boolean areSignaturesValid = areInputSignaturesValid(tx); 
    	boolean areContainedInputUTXOsUnique = areContainedInputUTXOsUnique(txInputs); 
    	boolean areOutputValuesPositive = areOutputValuesPositive(txOutputs);
    	boolean areInputOutputValuesSumAdherent = areInputOutputValuesSumAdherent(txOutputs, txInputs); 
    	
    	return inUTXOList && areOutputValuesPositive && areContainedInputUTXOsUnique && areInputOutputValuesSumAdherent && areSignaturesValid;
    }
    
    public boolean areInputSignaturesValid (Transaction tx)
    {
    	ArrayList <Transaction.Input> txInputs = tx.getInputs();
    	
    	for (int i=0; i< txInputs.size(); i++)
    	{
    		for (UTXO utxo : utxoList) {
    			
    			if (utxo.compareTo(new UTXO(txInputs.get(i).prevTxHash, txInputs.get(i).outputIndex)) == 0)
    			{
    				Transaction.Output op = utxoPool.getTxOutput(utxo);
    				
    				boolean isValidSignature = Crypto.verifySignature(op.address, tx.getRawDataToSign(i), txInputs.get(i).signature);
    	    		
    	        	if (!isValidSignature)
    	        		return false;
    			}
				
			} 
    	}
    	
    	return true;
    	
    }
    
    public boolean areInputOutputValuesSumAdherent (ArrayList<Transaction.Output> txOutputs, ArrayList<Transaction.Input> txInputs)
    {
    	double outputSum = 0.0;
    	double inputSum = 0.0;
    	
    	for (Transaction.Output txOutput:txOutputs)
    	{
    		outputSum += txOutput.value ;
    		
    	} 
    
    	for (int i=0; i< txInputs.size(); i++)
		{
    		
    		for (UTXO utxo : utxoList) {
    			
    			if (utxo.compareTo(new UTXO(txInputs.get(i).prevTxHash, txInputs.get(i).outputIndex)) == 0)
    			{
    				Transaction.Output op = utxoPool.getTxOutput(utxo);
    				inputSum += op.value;
    			}
				
			} 
    		
		}
    	
    	return (inputSum >= outputSum);
    	
    }
    
    public boolean areOutputValuesPositive (ArrayList<Transaction.Output> txOutputs)
    {
    	for (Transaction.Output txOutput:txOutputs)
    	{
    		boolean nonNegativeValue = txOutput.value >= 0;
    		
    		if (!nonNegativeValue)
    			return false;
    	}
    
    	return true;
    }
    
    public boolean areClaimedTransactionOutputsInUTXOPool(ArrayList <Transaction.Input> txInputs)
    {	
    	UTXO inputUTXO = null;
    	
    	for (int i=0; i< txInputs.size(); i++)
		{
    		inputUTXO = new UTXO(txInputs.get(i).prevTxHash, txInputs.get(i).outputIndex);
    		
    		if (!utxoList.contains(inputUTXO))
    		{
    			return false;
    		}
    		
		}

    	return true;
    }
    
    public boolean areContainedInputUTXOsUnique(ArrayList<Transaction.Input> txInputs)
    {
    	boolean multipleClaim = false;
    	ArrayList <UTXO> containedUTXOs = new ArrayList<>();
    	
    	for (Transaction.Input txInput : txInputs) {
			
    		containedUTXOs.add(new UTXO(txInput.prevTxHash, txInput.outputIndex));
		}
    	
    	for (UTXO containedUTXO: containedUTXOs)
    	{
    		multipleClaim = Collections.frequency(containedUTXOs, containedUTXO) > 1 ;
    				
    		if (multipleClaim)
    			return !multipleClaim;

    	}
    	
    	return !multipleClaim;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] proposedTxs) {
        // IMPLEMENT THIS
    	ArrayList<Transaction> transactionsList = new ArrayList<>();
    	Transaction [] transactionsArray = {};
    	
    	for (int i=0; i<proposedTxs.length; i++)
    	{
    		boolean isValid = isValidTx(proposedTxs[i]);
    		
    		if(isValid)
    		{
    			transactionsList.add(proposedTxs[i]);
    			
    			for (Transaction.Input transactionInput : proposedTxs[i].getInputs()) {
    				
					utxoPool.removeUTXO(new UTXO(transactionInput.prevTxHash, transactionInput.outputIndex));
				}
    			
    			ArrayList<Transaction.Output> transactionOuputs = proposedTxs[i].getOutputs();
    			
    			for (int x = 0; x < transactionOuputs.size(); x++) {
    				
    				utxoPool.addUTXO(new UTXO(proposedTxs[i].getHash(), x), transactionOuputs.get(x));
				}

    		}
    	}
    	
    	
    	transactionsList.trimToSize();
    	transactionsArray = new Transaction [transactionsList.size()];
    	
    	for (int i=0; i<transactionsList.size(); i++)
    	{
    		transactionsArray[i] = transactionsList.get(i);
    	}

    	return transactionsArray;
    }
    
}