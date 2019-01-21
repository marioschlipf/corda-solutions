package net.corda.businessnetworks.ledgersync

import net.corda.core.contracts.Contract
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.transactions.LedgerTransaction

const val BOGUS_SIDEKICK_CONTRACT_ID = "net.corda.businessnetworks.ledgersync.BogusSidekickContract"

class BogusSidekickContract : Contract {
    override fun verify(tx: LedgerTransaction) {
        // accept everything. this is a simple test fixture only.
    }

    sealed class Commands : TypeOnlyCommandData() {
        class Bogus : Commands()
        class TransformBogus : Commands()
        class ConsumeBogus : Commands()
    }
}
