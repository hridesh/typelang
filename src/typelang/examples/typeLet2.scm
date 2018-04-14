(let 
	((x : num 2)) 
	(let 
		((z : (num -> num) (lambda (y : num) (+ x y)))) 
		(z 5)
	)
)
