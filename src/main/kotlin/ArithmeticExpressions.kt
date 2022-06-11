
class AddExpr(override val left : Expr, override val right : MExpr) : Expr, BinOpExpr {
	override fun toString() = "$left + $right"

	override fun expanded() : Expr {
		val l = left.expanded()
		val r = right.expanded()
		if (l is ParenExpr) {
			return AddExpr(l.internalExpr, r).expanded()
		}
		if (r is ParenExpr) when (r.internalExpr) {
			is MExpr -> return AddExpr(l, r.internalExpr)
			is AddExpr -> return AddExpr(AddExpr(l, r.internalExpr.right), ParenExpr(r.internalExpr.left)).expanded()
			is SubExpr -> return AddExpr(SubExpr(l, r.internalExpr.right), ParenExpr(r.internalExpr.left)).expanded()
		}
		return AddExpr(l, r)
	}

	override fun mapTerms(f : (Term) -> Expr) : AddExpr {
		return AddExpr(left.mapTerms(f), right.mapTerms(f))
	}
}

class SubExpr(override val left : Expr, override val right : MExpr) : Expr, BinOpExpr {
	override fun toString() = "$left - $right"
	override fun expanded() : Expr {
		val l = left.expanded()
		val r = right.expanded()
		if (l is ParenExpr) {
			return SubExpr(l.internalExpr, r).expanded()
		}
		if (r is ParenExpr) when (r.internalExpr) {
			is MExpr -> return SubExpr(l, r.internalExpr)
			is AddExpr -> return SubExpr(SubExpr(l, r.internalExpr.right), ParenExpr(r.internalExpr.left)).expanded()
			is SubExpr -> return SubExpr(AddExpr(l, r.internalExpr.right), ParenExpr(r.internalExpr.left)).expanded()
		}
		return SubExpr(l, r)
	}

	override fun mapTerms(f : (Term) -> Expr) : SubExpr {
		return SubExpr(left.mapTerms(f), right.mapTerms(f))
	}
}

class MulExpr(override val left : MExpr, override val right : MExpr) : MExpr, BinOpExpr {
	override fun toString() = "$left * $right"

	override fun expanded() : MExpr {
		val l = left.expanded()
		val r = right.expanded()
		val actualLeft = when (l) {
			is ParenExpr -> l.internalExpr
			else -> l
		}
		when (actualLeft) {
			is AddExpr -> return ParenExpr(AddExpr(MulExpr(ParenExpr(actualLeft.left), r),
				MulExpr(actualLeft.right, r)).expanded())
			is SubExpr -> return ParenExpr(SubExpr(MulExpr(ParenExpr(actualLeft.left), r),
				MulExpr(actualLeft.right, r)).expanded())
			else -> {}
		}
		val actualRight = when (r) {
			is ParenExpr -> r.internalExpr
			else -> r
		}
		return when (actualRight) {
			is AddExpr -> ParenExpr(AddExpr(MulExpr(l, ParenExpr(actualRight.left)),
				MulExpr(l, actualRight.right)).expanded())
			is SubExpr -> ParenExpr(SubExpr(MulExpr(l, ParenExpr(actualRight.left)),
				MulExpr(l, actualRight.right)).expanded())
			is MExpr -> when (actualLeft) {
				is MExpr -> MulExpr(actualLeft, actualRight)
				else -> MulExpr(l, actualRight)
			}
			else -> when (actualLeft) {
				is MExpr -> MulExpr(actualLeft, r)
				else -> MulExpr(l, r)
			}
		}
	}

	override fun mapTerms(f : (Term) -> Expr) : MulExpr {
		return MulExpr(left.mapTerms(f), right.mapTerms(f))
	}

	companion object {
		fun parse(tokens : ArrayList<String>) : MExpr? {
			var left : MExpr = Term.parse(tokens) ?: return null
			while (tokens.size > 0) left = when (tokens[0]) {
				"*" -> {
					tokens.removeFirst()
					MulExpr(left, Term.parse(tokens) ?: return null)
				}
				"/" -> {
					tokens.removeFirst()
					DivExpr(left, Term.parse(tokens) ?: return null)
				}
				else -> break
			}
			return left
		}
	}
}

class DivExpr(override val left : MExpr, override val right : MExpr) : MExpr, BinOpExpr {
	override fun toString() = "$left / $right"

	override fun expanded() : MExpr {
		val l = left.expanded()
		val r = right.expanded()
		val actualLeft = when (l) {
			is ParenExpr -> l.internalExpr
			else -> l
		}
		return when (actualLeft) {
			is AddExpr -> ParenExpr(AddExpr(DivExpr(ParenExpr(actualLeft.left), r),
				DivExpr(actualLeft.right, r)).expanded())
			is SubExpr -> ParenExpr(SubExpr(DivExpr(ParenExpr(actualLeft.left), r),
				DivExpr(actualLeft.right, r)).expanded())
			is MExpr -> DivExpr(actualLeft, r)
			else -> DivExpr(l, r)
		}
	}

	override fun mapTerms(f : (Term) -> Expr) : DivExpr {
		return DivExpr(left.mapTerms(f), right.mapTerms(f))
	}
}

class ParenExpr(val internalExpr : Expr, override val isNeg : Boolean = false) : Trm  {
	override fun toString() = if (isNeg) "-($internalExpr)" else "($internalExpr)"
	override fun mapTerms(f : (Term) -> Expr) : ParenExpr {
		return ParenExpr(internalExpr.mapTerms(f))
	}

	override fun expanded() : Trm {
		val internal = internalExpr.expanded()
		if (internal is ParenExpr) return ParenExpr(internal.internalExpr)
		return ParenExpr(internal.expanded())
	}
}

class Term(val name : String, override val isNeg : Boolean = false) : Trm  {
	override fun toString() = name

	override fun expanded() : Trm {
		return Term(name)
	}

	override fun mapTerms(f : (Term) -> Expr) : Trm {
		val ret = f(this)
		if (ret is Trm) return ret
		return ParenExpr(ret)
	}

	companion object {
		fun parse(tokens : ArrayList<String>, isNeg : Boolean = false) : Trm? {
			if (tokens[0] == "-") {
				tokens.removeFirst()
				val ret = parse(tokens, !isNeg) ?: return null
				return ret
			} else if (tokens[0] == "(") {
				tokens.removeFirst()
				val ret = ParenExpr(Expr(tokens) ?: return null, isNeg)
				if (tokens.removeFirst() != ")") return null
				return ret
			} else {
				if (tokens[0].contains(Regex("[()-]|\\+"))) return null
				return Term(tokens.removeFirst(), isNeg)
			}
		}
	}
}