(letrec 
	(
		(isEven : (num -> bool) 
			(lambda (n : (num -> bool)) 
				(if (= 0 n) #t  (isOdd  (- n 1)))
			)
		)
		(isOdd  : (int -> boolean) 
			(lambda (n : (int -> boolean)) 
				(if (= 0 n) #f  (isEven (- n 1)))
			)
		)
	)
	(isOdd 11)
)