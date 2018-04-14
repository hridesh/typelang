(let 
	((f : (num -> (num -> num))
			(lambda (x : num)
					(lambda (y : num)
							(+ x y)
					)
			)
	))
f)