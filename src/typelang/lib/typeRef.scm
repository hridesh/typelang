(ref : int 2)

(ref : Ref int (ref : int 2))

(let ((a : Ref int (ref : int 2))) (deref a))

(let ((a : Ref int (ref : int 2))) (set! a (deref a)))
