(let ((x : int 2)) x)

(let ((x : int 2)) (let ((y : int 5)) (+ x y)))

(let ((x : int 2)) (let ((z : (int -> int) (lambda (y : (int -> int)) (+ x y)))) (z 5)))