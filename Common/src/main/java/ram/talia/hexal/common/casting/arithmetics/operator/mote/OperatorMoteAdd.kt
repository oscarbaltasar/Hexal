package ram.talia.hexal.common.casting.arithmetics.operator.mote

import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBasic
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate.ofType
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import ram.talia.hexal.common.casting.arithmetics.operator.nextMote
import ram.talia.hexal.common.lib.hex.HexalIotaTypes.MOTE

object OperatorMoteAdd : OperatorBasic(2, IotaMultiPredicate.all(ofType(MOTE))) {
    override fun apply(iotas: Iterable<Iota>, env: CastingEnvironment): Iterable<Iota> {
        val it = iotas.iterator().withIndex()

        val absorber = it.nextMote(arity)
        val absorbee = it.nextMote(arity)

        if (absorber == null || absorbee == null) {
            // ensure always 1 iota returned to the stack.
            val toReturn = listOfNotNull(absorber?.copy(), absorbee?.copy())
            return toReturn.ifEmpty { null.asActionResult }
        }
        if (absorber.itemIndex == absorbee.itemIndex)
            return listOf(absorber.copy())

        if (!absorber.typeMatches(absorbee))
            throw MishapInvalidIota.of(absorbee, 0, "cant_combine_motes")

        absorber.absorb(absorbee)

        return listOfNotNull(absorber.copy())
    }

    /* Credits to @walksanatora on github for the fix.
     * To be more specific, this fix can be originally found on: 
     * https://github.com/walksanatora/Hexal/commit/c7d23c8d096e7192eea1163bdc494b7ccb30498b
     */
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        val stack = image.stack.toMutableList()
        val input = mutableListOf(stack.removeFirst(),stack.removeFirst())
        stack.addAll(apply(input,env))
        return OperationResult(
            image.copy(stack),
            listOf(),
            continuation,
            HexEvalSounds.NORMAL_EXECUTE
        )
    }
}