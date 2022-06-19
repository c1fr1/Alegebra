

fun main() {
	print("starting Expr: ")
	cli(readLine()!!)
}

fun cli(workingExpr : String) {
	var wExpr = Expr(workingExpr)!!
	do {
		println(wExpr)
		print("> ")
		val cmd = readLine() ?: "exit"
		wExpr = when {
			cmd.startsWith("expand") -> wExpr.expanded()
			cmd.startsWith("mul ") -> MulExpr(ParenExpr(wExpr), ParenExpr(Expr(cmd.removePrefix("mul "))!!))
			cmd.startsWith("div ") -> DivExpr(ParenExpr(wExpr), ParenExpr(Expr(cmd.removePrefix("div "))!!))
			cmd.startsWith("add ") -> AddExpr(wExpr, ParenExpr(Expr(cmd.removePrefix("add "))!!))
			cmd.startsWith("sub ") -> SubExpr(wExpr, ParenExpr(Expr(cmd.removePrefix("sub "))!!))
			cmd.startsWith("set ") -> Expr(cmd.removePrefix("set "))!!
			cmd.startsWith("repl ") -> {
				val varNam = cmd.removePrefix("repl ").substringBefore(" ")
				val expr = Expr(cmd.removePrefix("repl ").substringAfter(" "))!!
				wExpr.mapTerms { term : Term -> if (term.name == varNam) expr else term }
			}
			cmd.startsWith("printPoly") -> {
				println(Polynomial(SumExpr(wExpr.expanded()), "T"))
				wExpr
			}
			else -> wExpr
		}
	} while (!cmd.startsWith("exit"))
}