//package classes;

import java.util.ArrayList;
import java.util.Collections;

public class MaxFeeTxHandler {
	
	private UTXOPool utxoPool;
	ArrayList <UTXO> utxoList;
	
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
    	
    	this.utxoPool = utxoPool;
    }
    
    
	public Transaction[] handleTxs(Transaction[] txs)
	{
		ArrayList <Transaction> maxTransactionsList = new ArrayList<>();
		Transaction [] transactionsArray = {};
		
		utxoList = utxoPool.getAllUTXO();
		
		for (Transaction tx: txs)
		{
			boolean isValid = isValidTx(tx);
			
			if (!isValid)
				continue;
			
			double outputSum = 0.0;
			double inputSum = 0.0;
			
			ArrayList<Transaction.Output> txOutputs  = tx.getOutputs();
			ArrayList<Transaction.Input> txInputs  = tx.getInputs();
			
			for (Transaction.Output txOutput:txOutputs)
			{
				outputSum += txOutput.value ;
				
			} 
			
			for (Transaction.Input txInput:txInputs)
			{
				for (UTXO utxo : utxoList) {
    			
	    			if (utxo.compareTo(new UTXO(txInput.prevTxHash, txInput.outputIndex)) == 0)
	    			{
	    				Transaction.Output op = utxoPool.getTxOutput(utxo);
	    				inputSum += op.value;
	    			}
				} 
				
			}
			
			if (inputSum > outputSum)
				maxTransactionsList.add(tx);
			
			maxTransactionsList.trimToSize();
	    	transactionsArray = new Transaction [maxTransactionsList.size()];
	    	
	    	for (int i=0; i<maxTransactionsList.size(); i++)
	    	{
	    		transactionsArray[i] = maxTransactionsList.get(i);
	    	}
			
		}
		
		return transactionsArray;
		
	}
	
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
    	
    	return inUTXOList && areSignaturesValid && areContainedInputUTXOsUnique && areOutputValuesPositive && areInputOutputValuesSumAdherent;
//    	 
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

}
