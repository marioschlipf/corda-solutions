package net.corda.businessnetworks.ledgersync

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.SchedulableState
import net.corda.core.contracts.ScheduledActivity
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowLogicRefFactory
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

/**
 * A trivial flow that is merely used to illustrate synchronisation by persisting meaningless transactions in
 * participant's vaults
 */
@InitiatingFlow
@StartableByRPC
class BogusFlow(
        private val them: Party
) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val cmd = Command(BogusContract.Commands.Bogus(), listOf(ourIdentity.owningKey))

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(BogusState(ourIdentity, them), BOGUS_CONTRACT_ID)
                .addCommand(cmd).apply {
                    verify(serviceHub)
                }

        val partiallySigned = serviceHub.signInitialTransaction(txBuilder)

        val x = subFlow(FinalityFlow(partiallySigned))


        val cmd2 = Command(BogusContract.Commands.Bogus(), listOf(ourIdentity.owningKey))

        val txBuilder2 = TransactionBuilder(notary)
                .addOutputState(BogusCompanion(ourIdentity, them), BOGUS_CONTRACT_ID)
                .addCommand(cmd2).apply {
                    verify(serviceHub)
                }

        val partiallySigned2 = serviceHub.signInitialTransaction(txBuilder2)

        subFlow(FinalityFlow(partiallySigned2))

        return x
    }
}

@InitiatingFlow
@StartableByRPC
class TransformBogusFlow(
        private val bogusState: StateAndRef<BogusState>,
        private val bogusCompanion: StateAndRef<BogusCompanion>
) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        val cmd = Command(BogusContract.Commands.Bogus(), listOf(ourIdentity.owningKey))

        val txBuilder = TransactionBuilder(notary)
                .addInputState(bogusCompanion)
                .addInputState(bogusState)
                .addOutputState(bogusState.state.data.copy(counter = bogusState.state.data.counter + 1), BOGUS_CONTRACT_ID)
                .addCommand(cmd).apply {
                    verify(serviceHub)
                }

        val partiallySigned = serviceHub.signInitialTransaction(txBuilder)

        return subFlow(FinalityFlow(partiallySigned))
    }
}

data class BogusState(
        private val us: Party,
        private val them: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        val counter: Int = 1
) : LinearState, SchedulableState {
    override fun nextScheduledActivity(thisStateRef: StateRef, flowLogicRefFactory: FlowLogicRefFactory): ScheduledActivity? {
        return null
    }

    override val participants: List<AbstractParty> = listOf(us, them)
}

data class BogusCompanion(
        private val us: Party,
        private val them: Party,
        override val linearId: UniqueIdentifier = UniqueIdentifier(),
        val counter: Int = 1
) : LinearState {
    override val participants: List<AbstractParty> = listOf(them, us)
}
