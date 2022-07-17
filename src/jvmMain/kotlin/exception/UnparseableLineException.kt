package exception

class UnparseableLineException(line: String, e: Throwable): Exception(line, e)
