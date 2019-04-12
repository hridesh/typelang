(let 
	((f : (num -> (bool -> num))
			(lambda (x : num)
					(lambda (y : num)
							(+ x y)
					)
			)
	))
f)