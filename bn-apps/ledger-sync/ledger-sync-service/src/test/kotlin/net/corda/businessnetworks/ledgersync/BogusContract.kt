package net.corda.businessnetworks.ledgersync

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

const val BOGUS_CONTRACT_ID = "net.corda.businessnetworks.ledgersync.BogusContract"

class BogusContract : Contract {
    override fun verify(tx: LedgerTransaction) {
        // accept everything. this is a simple test fixture only.
    }

    sealed class Commands : TypeOnlyCommandData() {
        class Bogus : Commands()
        class ConsumeBogus : Commands()
    }
}
