
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    
    public static final int CUT_OFF_AGE = 10;
    
//    Block genesisBlock;
//    Block maxHeightBlock;
    
    TransactionPool transactionPool = new TransactionPool();
    
    Stack< HashMap <Block, UTXOPool> > blockUTXOPoolStack = new Stack(); //ds is d blockchain
    Stack< HashMap <Block, Integer> > blockHeightStack = new Stack(); 
    
    int maxHeight;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     * @param genesisBlock
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        
        HashMap <Block, UTXOPool> blockPoolMap = new HashMap();
        blockPoolMap.put(genesisBlock, getUpdatedUTXOPool(genesisBlock));
        
        HashMap <Block, Integer> blockHeightMap = new HashMap();
        blockHeightMap.put(genesisBlock, 1);
        
        //update TX pool
        updateTxPool(genesisBlock);
        
        blockHeightStack.push(blockHeightMap);
        blockUTXOPoolStack.push(blockPoolMap);
    }

    /** Get the maximum height block
     * @return  */
    public Block getMaxHeightBlock() {
        
        // IMPLEMENT THIS
        return blockUTXOPoolStack.peek().keySet().iterator().next();
    }

    /** Get the UTXOPool for mining a new block on top of max height block
     * @return  */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        Block b = blockUTXOPoolStack.peek().keySet().iterator().next();
        return blockUTXOPoolStack.peek().get(b);
    }

    /** Get the transaction pool to mine a new block
     * @return  */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @param block
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        boolean isPrevHashValid = validatePrevHash(block);
        boolean isBlockHeightValid = validateBlockHeight(block);
        boolean isDoubleSpendDetected = validateDoubleSpends(block);
        boolean isUTXOClaimNotInParent = validateUTXOClaimUsedInParent(block);
        
        if (isPrevHashValid && isBlockHeightValid && isDoubleSpendDetected) // && isUTXOClaimNotInParent 
        {
            int blockHeight = maxHeight + 1; //4
            
            HashMap<Block, Integer> heightMap = new HashMap<>();
            heightMap.put(block, blockHeight);
            
            HashMap<Block, UTXOPool> utxoPoolMap = new HashMap<>();
            utxoPoolMap.put(block, getUpdatedUTXOPool(block));
            
            blockUTXOPoolStack.push(utxoPoolMap);
            blockHeightStack.push(heightMap);
            
            //update tx pool
            updateTxPool(block);
                    
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    private UTXOPool getUpdatedUTXOPool(Block block)
    {
        UTXOPool maxPool = new UTXOPool();
        
        if (null != block.getPrevBlockHash())
        {
            maxPool = getMaxHeightUTXOPool();
        }
        
        ArrayList<UTXO> allUTXOs = maxPool.getAllUTXO();

        for (Transaction blockTx: block.getTransactions())
        {
           for (Transaction.Input blockInput : blockTx.getInputs())
           {
                UTXO blockUTXO = new UTXO(blockInput.prevTxHash, blockInput.outputIndex);

                for (UTXO allUtxo: allUTXOs)
                {
                    if (blockUTXO.compareTo(allUtxo) == 0)
                        maxPool.removeUTXO(allUtxo);
                }
           }

           ArrayList<Transaction.Output> blockOutputs = blockTx.getOutputs();

           for (int i=0; i < blockOutputs.size(); i++)
           {
                UTXO newUTXO = new UTXO(blockTx.getHash(), i);
                maxPool.addUTXO(newUTXO, blockOutputs.get(i));
           }
        }
        
        return maxPool;
    }
    
    private void updateTxPool(Block block)
    {
        TransactionPool txPool = getTransactionPool();
        
        for (Transaction tx: block.getTransactions())
        {
            txPool.removeTransaction(tx.getHash());
        }
    }
            
    private boolean validateUTXOClaimUsedInParent(Block block)
    {
        byte [] parentHash = block.getPrevBlockHash();
        ArrayList<Transaction> blockTxs = block.getTransactions();
        ArrayList<Transaction> parentTxs = new ArrayList();
        
        Enumeration <HashMap <Block, UTXOPool>> enumeration = blockUTXOPoolStack.elements();
        
        while (enumeration.hasMoreElements())
        {
            HashMap <Block, UTXOPool> utxoPoolMap = enumeration.nextElement();
            Block blk = utxoPoolMap.keySet().iterator().next();
            
            if (parentHash == blk.getHash())
            {
//                parentTxs = blk.getTransactions();
                
                TxHandler handler = new TxHandler(utxoPoolMap.get(blk));
                Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);
                Transaction[] validTxs = handler.handleTxs(txs);
                if (validTxs.length != txs.length) {
                    return false;
                }
                else
                    return true;
//                break;
            }
        }
            
//        for (Transaction blkTx: blockTxs)
//        {
//            for (Transaction.Input blkInput:blkTx.getInputs())
//            {
//                UTXO blkUTXO = new UTXO(blkInput.prevTxHash, blkInput.outputIndex);
//                        
//                for (Transaction parentTx: parentTxs)
//                {
//                    for (Transaction.Input parentInput: parentTx.getInputs())
//                    {
//                        UTXO parentUTXO = new UTXO(parentInput.prevTxHash, parentInput.outputIndex);
//                        
//                         if (blkUTXO.compareTo(parentUTXO) == 0)
//                           return false;
//                    }
//                    
//                }
//            }
//        }
        
        return false;
    }
    
    private boolean validateDoubleSpends(Block block)
    {
        ArrayList<Transaction> txList = block.getTransactions();
        
        for (int i=0; i<txList.size(); i++)
        {
            ArrayList<Transaction.Input> iInputs = txList.get(i).getInputs();
            
            for (int j=0; j<txList.size(); j++)
            {
                if (j == i)
                    continue;
                
                ArrayList<Transaction.Input> jInputs = txList.get(j).getInputs();
                
                for (Transaction.Input iInput: iInputs)
                {
                    UTXO iUTXO = new UTXO(iInput.prevTxHash, iInput.outputIndex);
                    
                    for (Transaction.Input jInput: jInputs)
                    {
                        UTXO jUTXO = new UTXO(jInput.prevTxHash, jInput.outputIndex);
                        
                         if (iUTXO.compareTo(jUTXO) == 0)
                           return false;
                    }
                     
                }
            }
        }
        return true;
    }
    
    private boolean validateBlockHeight(Block block)
    {
        //get prev hash block and get heigt of d block
        Enumeration <HashMap <Block, Integer>> enumeration = blockHeightStack.elements();
        
        while (enumeration.hasMoreElements())
        {
            HashMap <Block, Integer> blkHeightMap = enumeration.nextElement();
            Block blk = blkHeightMap.keySet().iterator().next();
            
            if (block.getPrevBlockHash() == blk.getHash())
            {
                maxHeight = blkHeightMap.get(blk); //3 
                break;
            }
        }
        
        
//        maxHeight = blockHeightStack.peek().values().iterator().next(); //3  
        int blockHeight = maxHeight + 1;//4
        
        return (blockHeight <= CUT_OFF_AGE + maxHeight);
//        return (blockHeight >= maxHeight - CUT_OFF_AGE);
    }
     
    
    private boolean validatePrevHash(Block block){
        // IMPLEMENT THIS
        byte[] prevHash = block.getPrevBlockHash();
        
        if (prevHash == null )
            return false;
        
        Enumeration <HashMap <Block, UTXOPool>> enumeration = blockUTXOPoolStack.elements();
        
        while (enumeration.hasMoreElements())
        {
            Block blk = enumeration.nextElement().keySet().iterator().next();
            
            if (blk.getHash() == prevHash && blockUTXOPoolStack.peek().keySet().iterator().next().getHash() == prevHash)
                return true;
        }
        
        return false;
    }

    /** Add a transaction to the transaction pool
     * @param tx */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transactionPool.addTransaction(tx);
    }
}