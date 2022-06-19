
class SumExpr(var terms : ArrayList<ProdExpr> = ArrayList()) {
	constructor(addExpr : Expr) : this() {
		var expr : Expr = addExpr
		while (expr is AddExpr || expr is SubExpr) {
			if ((expr as BinOpExpr).right !is MulExpr) throw RuntimeException("invalid expression")
			val prodExpr = ProdExpr(expr.right as MulExpr)
			if (expr is SubExpr) prodExpr.coefficient *= -1
			addExpr(prodExpr)
			expr = (expr as BinOpExpr).left
		}
		if (expr is MulExpr) {
			addExpr(ProdExpr(expr))
		} else {
			throw RuntimeException("invalid expression")
		}
	}

	fun addExpr(mulExpr : ProdExpr) {
		val addable = terms.find { it.addable(mulExpr) }
		if (addable != null) {
			addable.coefficient += mulExpr.coefficient
			return
		}
		terms.add(mulExpr)
	}

	override fun toString() : String {
		return terms.map { it.toString() }.reduce {acc, s ->
			if (s[0] == '-') {
				"$acc - ${s.substring(1)}"
			} else {
				"$acc + $s"
			}
		}
	}
}

class Polynomial(var terms : ArrayList<SumExpr> = ArrayList(), val termName : String) {

	override fun toString() = terms.indices.map { "$termName^$it * (${terms[it]})" }.reduce {acc, next ->
		"$acc + $next"
	}
	
	companion object {
		operator fun invoke(expr : SumExpr, term : String) : Polynomial {
			val degree = expr.terms.maxOf { it.terms.find { t -> t.first.name == term }?.second ?: 0 }
			val ret = Array(degree + 1) {SumExpr()}
			for (ex in expr.terms) {
				val i = ex.terms.find { it.first.name == term }?.second ?: 0
				ret[i].addExpr(ProdExpr(ex.coefficient, ex.copy().terms.filter { it.first.name != term } as ArrayList))
			}
			return Polynomial(arrayListOf(*ret), term)
		}
	}
}

class ProdExpr(var coefficient : Float = 1f, val terms : ArrayList<Pair<Term, Int>> = ArrayList()) {

	constructor(mulExpr : MulExpr, isNeg : Boolean = false) : this() {
		coefficient = if (isNeg) -1f else 1f
		when (mulExpr.left) {
			is MulExpr -> mulBy(ProdExpr(mulExpr.left))
			is Term -> mulBy(mulExpr.left)
			else -> {throw RuntimeException("involid term")}
		}
		when (mulExpr.right) {
			is MulExpr -> mulBy(ProdExpr(mulExpr.right))
			is Term -> mulBy(mulExpr.right)
			else -> {throw RuntimeException("involid term")}
		}
	}

	fun mulBy(o : Term) {
		val asFloat = o.name.toFloatOrNull()
		if (asFloat != null) {
			coefficient *= asFloat

		}
		when (val i = terms.indices.find { terms[it].first.name == o.name }) {
			null -> terms.add(Pair(o, 1))
			else -> terms[i] = Pair(o, terms[i].second + 1)
		}
	}

	fun mulBy(o : ProdExpr) {
		coefficient *= o.coefficient
		for ((t, count) in o.terms) {
			when (val i = terms.indices.find { terms[it].first.name == t.name }) {
				null -> terms.add(Pair(t, count))
				else -> terms[i] = Pair(t, count + terms[i].second)
			}
		}
	}

	fun addable(o : ProdExpr) : Boolean {
		if (o.terms.size != terms.size) return false
		return o.terms.all { oterm -> terms.any { it.first.name == oterm.first.name && it.second == oterm.second } }
	}

	fun copy() : ProdExpr {
		return ProdExpr(coefficient, arrayListOf(*Array(terms.size) {terms[it]}))
	}

	override fun toString() : String {
		val mainTerms = terms.map { if (it.second > 1) "${it.first}^${it.second}" else "${it.first}" }.reduce {acc, it -> "$acc * $it" }
		return if (coefficient == 1f) {
			mainTerms
		} else if (coefficient == -1f) {
			"-$mainTerms"
		} else {
			"$coefficient * $mainTerms"
		}
	}
}