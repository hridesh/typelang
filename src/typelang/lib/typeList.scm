(list : int  1 2 3)
(list : int  1 2 #t)  // Type error

(car (list : int  1 2 8))
(null? (list : int  1 2 8))

(null? (cons 1 2))   // Type error

(car (cons 1 2))   // Type error

(cons (cons 1 2) (list : int 2 4 6))

(cons (cons 1 2) (list : boolean #t #f))

(null? (cons (cons 1 2) (list : boolean #t #f))) // type error

(list : List<int> (list : int 2))

(list : List<int> (list : boolean 2))    // type error

(list : List<int> (list : boolean #t))    // type error
