
interface Expr {
	fun expanded() : Expr

	fun mapTerms(f : (Term) -> Expr) : Expr

	companion object {
		operator fun invoke(expr : String) : Expr? {
			val tokens = ArrayList<String>()
			var temp = ""
			for (c in expr) when (c) {
				' ' -> if (temp.isNotEmpty()) {
					tokens.add(temp)
					temp = ""
				}
				'(', ')', '+', '-', '*' -> {
					if (temp.isNotEmpty()) {
						tokens.add(temp)
						temp = ""
					}
					tokens.add(c.toString())
				}
				else -> {
					temp += c.toString()
				}
			}
			if (temp.isNotEmpty()) {
				tokens.add(temp)
			}
			return invoke(tokens)
		}

		operator fun invoke(tokens : ArrayList<String>) : Expr? {
			var left : Expr = MulExpr.parse(tokens) ?: return null
			while (tokens.size > 0) {
				left = when (tokens[0]) {
					"+" -> {
						tokens.removeFirst()
						val right = MulExpr.parse(tokens) ?: return null
						AddExpr(left, right)
					}
					"-" -> {
						tokens.removeFirst()
						val right = MulExpr.parse(tokens) ?: return null
						SubExpr(left, right)
					}
					else -> break
				}
			}
			return left
		}
	}
}

interface MExpr : Expr {
	override fun expanded() : MExpr
	override fun mapTerms(f : (Term) -> Expr) : MExpr
}

interface Trm : MExpr {
	val isNeg : Boolean

	override fun expanded() : Trm
	override fun mapTerms(f : (Term) -> Expr) : Trm
}

interface BinOpExpr {
	val left : Expr
	val right : Expr
}