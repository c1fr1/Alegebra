

fun main() {

	val az = Expr("α.z * (αΔ.y * βΔ.x - αΔ.x * βΔ.y) * (βΔ.y * αΔ.x - βΔ.x * αΔ.y) + αΔ.z * ((α.x - β.x) * βΔ.y - (α.y - β.y) * βΔ.x) * (βΔ.y * αΔ.x - βΔ.x * αΔ.y)")!!
	val bz = Expr("β.z * (βΔ.y * αΔ.x - βΔ.x * αΔ.y) * (αΔ.y * βΔ.x - αΔ.x * βΔ.y) + βΔ.z * ((β.x - α.x) * αΔ.y - (β.y - α.y) * αΔ.x) * (αΔ.y * βΔ.x - αΔ.x * βΔ.y)")!!

	val mapF = {term : Term ->
		val preh = term.name.substringBefore('.')
		val compeh = term.name.substringAfter('.')
		val npreh = when (preh) {
			"α" -> "a"
			"αΔ" -> "A"
			"β" -> "b"
			"βΔ" -> "B"
			else -> "?"
		}
		Expr("$npreh.$compeh + ${npreh}Δ.$compeh * T")!!
	}
	//α = a + aΔ * T
	//αΔ = A + AΔ * T

	//β = b + bΔ * T
	//βΔ = B + BΔ * T

	val alf = az.mapTerms(mapF).expanded()
	val blf = bz.mapTerms(mapF).expanded()
	val total = SubExpr(alf, ParenExpr(blf)).expanded()

	val sumExpr = SumExpr(total)
	sumExpr.terms.sortBy {prodE ->
		prodE.terms.find { it.first.name == "T" }?.second?.toDouble() ?: 0.0
	}
	var totalStr = sumExpr.toString()
	totalStr = totalStr.replace("+ ", "+\n").replace("- ", "-\n")
	println(totalStr)
}