(define length : (List<num> -> num)
	(lambda (l : List<num>)
		(if (null? l) 0
			(+ 1 (length (cdr l)))
		)
	)
)