package com.chemscanner.omniscient.marrow.services

import com.chemscanner.omniscient.marrow.data.dao.BlockchainDao
import com.chemscanner.omniscient.marrow.data.models.BlockchainEntry
import com.chemscanner.omniscient.marrow.repository.GlobalKnowledgeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockchainNotaryService @Inject constructor(
    private val globalKnowledge: GlobalKnowledgeRepository,
    private val blockchainDao: BlockchainDao
) {
    private var lastBlockHash: String = "GENESIS_BLOCK_0000000000000000"
    private var merkleRoot: String = ""
    private val currentBlockTransactions = Collections.synchronizedList(mutableListOf<String>())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            blockchainDao.getLastBlockHash()?.let { lastBlockHash = it }
        }
    }

    fun notarizeDiscovery(module: String, data: String): String {
        val timestamp = System.currentTimeMillis()
        val payload = "$lastBlockHash|$module|$data|$timestamp"
        val newHash = hashString(payload)
        val previousHash = lastBlockHash
        lastBlockHash = newHash
        
        // OPTIMIZATION: Prevent infinite list growth and memory pressure
        synchronized(currentBlockTransactions) {
            currentBlockTransactions.add(newHash)
            if (currentBlockTransactions.size > 100) {
                currentBlockTransactions.removeAt(0)
            }
            updateMerkleRoot()
        }
        
        scope.launch {
            try {
                blockchainDao.insertBlock(BlockchainEntry(newHash, previousHash, module, data, timestamp, merkleRoot))
            } catch (e: Exception) {
                globalKnowledge.logEvent("NOTARY_ERROR", "Failed to insert block: ${e.message}", 2)
            }
        }
        globalKnowledge.logEvent("NOTARY", "Marrow discovery sealed in Akasha.", 5)
        return newHash
    }

    private fun updateMerkleRoot() {
        if (currentBlockTransactions.isEmpty()) return
        var tree = currentBlockTransactions.toMutableList()
        while (tree.size > 1) {
            val nextLevel = mutableListOf<String>()
            for (i in 0 until tree.size step 2) {
                val left = tree[i]
                val right = if (i + 1 < tree.size) tree[i + 1] else left
                nextLevel.add(hashString(left + right))
            }
            tree = nextLevel
        }
        merkleRoot = tree[0]
    }

    private fun hashString(input: String): String {
        // OPTIMIZATION: joinToString is much faster than string concatenation in a fold
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
