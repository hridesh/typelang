/* Binary Tree Procedures */

(define leaf
	(lambda (val)
		(ref (list 0 val))
	)
)

(define bintree
	(lambda (root left right)
		(ref (list 1 root left right))
	)
)

(define rootnode 
	(lambda (tree)
		(car (cdr (deref tree)))
	)
)

(define lefttree 
	(lambda (tree)
		(car (cdr (cdr (deref tree))))
	)
)

(define righttree 
	(lambda (tree)
		(car (cdr (cdr (cdr (deref tree)))))
	)
)

(define traverse
	(lambda (tree op combine)
		(if (= (car (deref tree)) 0)  (op (car (cdr (deref tree))))
			(combine
				(op (rootnode tree))
				(traverse (lefttree tree) op combine)
				(traverse (righttree tree) op combine)
			)
		)
	)
)

(define combine 
	(lambda (root left right) 
		(+ root left right)
	)
)

/* An example usage
$ (require "build/typelang/lib/tree.scm")
$ (define atree (bintree 3 (leaf 4) (leaf 2)))
$ (define op (lambda (x) x))
$ (let ((atree (bintree 3 (leaf 4) (leaf 2)))) (traverse atree op combine))
*/ 