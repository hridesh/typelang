(define append : (List<num> List<num> -> List<num>)
	(lambda (lst1: List<num> lst2: List<num>)
		(if (null? lst1) lst2
			(if (null? lst2) lst1
				(cons (car lst1) (append (cdr lst1) lst2))
			)
		)		
	)
)