package typelang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import typelang.AST.*;
import typelang.Env.ExtendEnv;
import typelang.Type.ErrorT;
import typelang.Type.*;

public class Checker implements Visitor<Type, Env<Type>> {
    Printer.Formatter ts = new Printer.Formatter();

    Type check(Program p) {
	return (Type) p.accept(this, null);
    }

    public Type visit(Program p, Env<Type> env) {
	Env<Type> new_env = env;

	for (DefineDecl d : p.decls()) {
	    Type type = (Type) d.accept(this, new_env);

	    if (type instanceof ErrorT) {
		return type;
	    }

	    Type dType = d.type();

	    if (!type.typeEqual(dType)) {
		return new ErrorT("Expected " + dType.tostring() + " found " + type.tostring()
			+ " in " + ts.visit(d, null));
	    }

	    new_env = new ExtendEnv<Type>(new_env, d.name(), dType);
	}
	return (Type) p.e().accept(this, new_env);
    }

    public Type visit(VarExp e, Env<Type> env) {
	try {
	    return env.get(e.name());
	} catch (Exception ex) {
	    return new ErrorT("Variable " + e.name()
	    + " has not been declared in " + ts.visit(e, null));
	}
    }

    public Type visit(LetExp e, Env<Type> env) {
	List<String> names = e.names();
	List<Exp> value_exps = e.value_exps();
	List<Type> types = e.varTypes();
	List<Type> values = new ArrayList<Type>(value_exps.size());

	int i = 0;
	for (Exp exp : value_exps) {
	    Type type = (Type) exp.accept(this, env);
	    if (type instanceof ErrorT) {
		return type;
	    }

	    Type argType = types.get(i);
	    if (!type.typeEqual(argType)) {
		return new ErrorT("The declared type of the " + i
			+ " let variable and the actual type mismatch, expect "
			+ argType.tostring() + " found " + type.tostring()
			+ " in " + ts.visit(e, null));
	    }

	    values.add(type);
	    i++;
	}

	Env<Type> new_env = env;
	for (int index = 0; index < names.size(); index++)
	    new_env = new ExtendEnv<Type>(new_env, names.get(index),
		    values.get(index));

	return (Type) e.body().accept(this, new_env);
    }

    public Type visit(DefineDecl d, Env<Type> env) {
	return (Type) d._value_exp.accept(this, env);
    }

    public Type visit(LambdaExp e, Env<Type> env) {
	List<String> names = e.formals();
	List<Type> types = e.formal_types();

	String message = "The declared type of a lambda expression should be "
		+ "of function type in ";

	//FuncT ft = (FuncT) type;
	message = "The number of formal parameters and the number of "
		+ "arguments in the function type do not match in ";
	if (types.size() == names.size()) {
	    Env<Type> new_env = env;
	    int index = 0;
	    for (Type argType : types) {
		new_env = new ExtendEnv<Type>(new_env, names.get(index),
			argType);
		index++;
	    }

	    Type bodyType = (Type) e.body().accept(this, new_env);

	    if (bodyType instanceof ErrorT) {
		return bodyType;
	    }

	    //create a new function type with arguments, and the type of 
	    //the body as the return type. Notice, that the body type isn't 
	    //given in any type annotation, but being computed here.
	    return new FuncT(types,bodyType);	    
	}

	return new ErrorT(message + ts.visit(e, null));
    }

    public Type visit(CallExp e, Env<Type> env) {
	Exp operator = e.operator();
	List<Exp> operands = e.operands();

	Type type = (Type) operator.accept(this, env);
	if (type instanceof ErrorT) {
	    return type;
	}

	String message = "Expect a function type in the call expression, found "
		+ type.tostring() + " in ";
	if (type instanceof FuncT) {
	    FuncT ft = (FuncT) type;

	    List<Type> argTypes = ft.argTypes();
	    int size_actuals = operands.size();
	    int size_formals = argTypes.size();

	    message = "The number of arguments expected is " + size_formals
		    + " found " + size_actuals + " in ";
	    if (size_actuals == size_formals) {
		for (int i = 0; i < size_actuals; i++) {
		    Exp operand = operands.get(i);
		    Type operand_type = (Type) operand.accept(this, env);

		    if (operand_type instanceof ErrorT) {
			return operand_type;
		    }

		    if (!assignable(argTypes.get(i), operand_type)) {
			return new ErrorT("The expected type of the " + i
				+ " argument is " + argTypes.get(i).tostring()
				+ " found " + operand_type.tostring() + " in "
				+ ts.visit(e, null));
		    }
		}
		return ft.returnType();
	    }
	}
	return new ErrorT(message + ts.visit(e, null));
    }

    public Type visit(LetrecExp e, Env<Type> env) {
	List<String> names = e.names();
	List<Type> types = e.types();
	List<Exp> fun_exps = e.fun_exps();

	// collect the environment
	Env<Type> new_env = env;
	for (int index = 0; index < names.size(); index++) {
	    new_env = new ExtendEnv<Type>(new_env, names.get(index),
		    types.get(index));
	}

	// verify the types of the variables
	for (int index = 0; index < names.size(); index++) {
	    Type type = (Type) fun_exps.get(index).accept(this, new_env);

	    if (type instanceof ErrorT) {
		return type;
	    }

	    if (!assignable(types.get(index), type)) {
		return new ErrorT(
			"The expected type of the " + index + " variable is "
				+ types.get(index).tostring() + " found "
				+ type.tostring() + " in " + ts.visit(e, null));
	    }
	}

	return (Type) e.body().accept(this, new_env);
    }

    public Type visit(IfExp e, Env<Type> env) {
	Exp cond = e.conditional();
	Type condType = (Type) cond.accept(this, env);
	if (condType instanceof ErrorT) {
	    return condType;
	}

	if (!(condType instanceof BoolT)) {
	    return new ErrorT("The condition should have boolean type, found "
		    + condType.tostring() + " in " + ts.visit(e, null));
	}

	Type thentype = (Type) e.then_exp().accept(this, env);
	if (thentype instanceof ErrorT) {
	    return thentype;
	}

	Type elsetype = (Type) e.else_exp().accept(this, env);
	if (elsetype instanceof ErrorT) {
	    return elsetype;
	}

	if (thentype.typeEqual(elsetype)) {
	    return thentype;
	}

	return new ErrorT("The then and else expressions should have the same "
		+ "type, then has type " + thentype.tostring()
		+ " else has type " + elsetype.tostring() + " in "
		+ ts.visit(e, null));
    }

    public Type visit(CarExp e, Env<Type> env) {
	Exp exp = e.arg();
	Type type = (Type) exp.accept(this, env);
	if (type instanceof ErrorT) {
	    return type;
	}

	if (type instanceof PairT) {
	    PairT pt = (PairT) type;
	    return pt.fst();
	}

	return new ErrorT("The car expect an expression of type Pair, found "
		+ type.tostring() + " in " + ts.visit(e, null));
    }

    public Type visit(CdrExp e, Env<Type> env) {
	Exp exp = e.arg();
	Type type = (Type) exp.accept(this, env);
	if (type instanceof ErrorT) {
	    return type;
	}

	if (type instanceof PairT) {
	    PairT pt = (PairT) type;
	    return pt.snd();
	}

	return new ErrorT("The cdr expect an expression of type Pair, found "
		+ type.tostring() + " in " + ts.visit(e, null));
    }

    public Type visit(ConsExp e, Env<Type> env) {
	Exp fst = e.fst();
	Exp snd = e.snd();

	Type t1 = (Type) fst.accept(this, env);
	if (t1 instanceof ErrorT) {
	    return t1;
	}

	Type t2 = (Type) snd.accept(this, env);
	if (t2 instanceof ErrorT) {
	    return t2;
	}

	return new PairT(t1, t2);
    }

    public Type visit(ListExp e, Env<Type> env) {
	List<Exp> elems = e.elems();
	Type type = e.type();

	int index = 0;
	for (Exp elem : elems) {
	    Type elemType = (Type) elem.accept(this, env);
	    if (elemType instanceof ErrorT) {
		return elemType;
	    }

	    if (!assignable(type, elemType)) {
		return new ErrorT("The " + index
			+ " expression should have type " + type.tostring()
			+ " found " + elemType.tostring() + " in "
			+ ts.visit(e, null));
	    }
	    index++;
	}
	return new ListT(type);
    }

    public Type visit(NullExp e, Env<Type> env) {
	Exp arg = e.arg();
	Type type = (Type) arg.accept(this, env);
	if (type instanceof ErrorT) {
	    return type;
	}

	if (type instanceof ListT) {
	    return BoolT.getInstance();
	}

	return new ErrorT("The null? expects an expression of type List, found "
		+ type.tostring() + " in " + ts.visit(e, null));
    }

    public Type visit(RefExp e, Env<Type> env) {
	Exp value = e.value_exp();
	Type type = e.type();
	Type expType = (Type) value.accept(this, env);
	if (type instanceof ErrorT) {
	    return type;
	}

	if (expType.typeEqual(type)) {
	    return new RefT(type);
	}

	return new ErrorT("The Ref expression expects type " + type.tostring()
	+ " found " + expType.tostring() + " in " + ts.visit(e, null));
    }

    public Type visit(DerefExp e, Env<Type> env) {
	Exp exp = e.loc_exp();
	Type type = (Type) exp.accept(this, env);
	if (type instanceof ErrorT) {
	    return type;
	}

	if (type instanceof RefT) {
	    RefT rt = (RefT) type;
	    return rt.nestType();
	}

	return new ErrorT("The dereference expression expects a reference type "
		+ "found " + type.tostring() + " in " + ts.visit(e, null));
    }

    public Type visit(AssignExp e, Env<Type> env) {
	Exp lhs_exp = e.lhs_exp();
	Type lhsType = (Type) lhs_exp.accept(this, env);
	if (lhsType instanceof ErrorT) {
	    return lhsType;
	}

	if (lhsType instanceof RefT) {
	    Exp rhs_exp = e.rhs_exp();
	    Type rhsType = (Type) rhs_exp.accept(this, env);
	    if (rhsType instanceof ErrorT) {
		return rhsType;
	    }

	    RefT rt = (RefT) lhsType;
	    Type nested = rt.nestType();

	    if (rhsType.typeEqual(nested)) {
		return rhsType;
	    }

	    return new ErrorT("The inner type of the reference type is "
		    + nested.tostring() + " the rhs type is "
		    + rhsType.tostring() + " in " + ts.visit(e, null));
	}

	return new ErrorT("The lhs of the assignment expression expects a "
		+ "reference type found " + lhsType.tostring() + " in "
		+ ts.visit(e, null));
    }

    public Type visit(FreeExp e, Env<Type> env) {
	Exp exp = e.value_exp();
	Type type = (Type) exp.accept(this, env);

	if (type instanceof ErrorT) {
	    return type;
	}

	if (type instanceof RefT) {
	    return UnitT.getInstance();
	}

	return new ErrorT("The free expression expects a reference type "
		+ "found " + type.tostring() + " in " + ts.visit(e, null));
    }

    public Type visit(UnitExp e, Env<Type> env) {
	return Type.UnitT.getInstance();
    }

    public Type visit(NumExp e, Env<Type> env) {
	return Type.NumT.getInstance();
    }

    public Type visit(StrExp e, Env<Type> env) {
	return Type.StringT.getInstance();
    }

    public Type visit(BoolExp e, Env<Type> env) {
	return Type.BoolT.getInstance();
    }

    public Type visit(LessExp e, Env<Type> env) {
	return visitBinaryComparator(e, env, ts.visit(e, null));
    }

    public Type visit(EqualExp e, Env<Type> env) {
	return visitBinaryComparator(e, env, ts.visit(e, null));
    }

    public Type visit(GreaterExp e, Env<Type> env) {
	return visitBinaryComparator(e, env, ts.visit(e, null));
    }

    private Type visitBinaryComparator(BinaryComparator e, Env<Type> env,
	    String printNode) {
	Exp first_exp = e.first_exp();
	Exp second_exp = e.second_exp();

	Type first_type = (Type) first_exp.accept(this, env);
	if (first_type instanceof ErrorT) {
	    return first_type;
	}

	Type second_type = (Type) second_exp.accept(this, env);
	if (second_type instanceof ErrorT) {
	    return second_type;
	}

	if (!(first_type instanceof NumT)) {
	    return new ErrorT("The first argument of a binary expression "
		    + "should be num Type, found " + first_type.tostring()
		    + " in " + printNode);
	}

	if (!(second_type instanceof NumT)) {
	    return new ErrorT("The second argument of a binary expression "
		    + "should be num Type, found " + second_type.tostring()
		    + " in " + printNode);
	}

	return BoolT.getInstance();
    }

    public Type visit(AddExp e, Env<Type> env) {
	return visitCompoundArithExp(e, env, ts.visit(e, null));
    }

    public Type visit(DivExp e, Env<Type> env) {
	return visitCompoundArithExp(e, env, ts.visit(e, null));
    }

    public Type visit(MultExp e, Env<Type> env) {
	return visitCompoundArithExp(e, env, ts.visit(e, null));
    }

    public Type visit(SubExp e, Env<Type> env) {
	return visitCompoundArithExp(e, env, ts.visit(e, null));
    }

    public Type visit(ErrorExp e, Env<Type> env) {
	return new ErrorT("Encountered an error type " + ts.visit(e, null));
    }

    private Type visitCompoundArithExp(CompoundArithExp e, Env<Type> env,
	    String printNode) {
	List<Exp> operands = e.all();

	for (Exp exp : operands) {
	    Type intermediate = (Type) exp.accept(this, env); // Static
	    // type-checking
	    if (intermediate instanceof ErrorT) {
		return intermediate;
	    }

	    if (!(intermediate instanceof Type.NumT)) {
		return new ErrorT("expected num found "
			+ intermediate.tostring() + " in " + printNode);
	    }
	}

	return NumT.getInstance();
    }

    private static boolean assignable(Type t1, Type t2) {
	if (t2 instanceof UnitT) {
	    return true;
	}

	return t1.typeEqual(t2);
    }

    public Type visit(ReadExp e, Env<Type> env) {
	return UnitT.getInstance();
    }

    public Type visit(EvalExp e, Env<Type> env) {
	return UnitT.getInstance();
    }

    @Override
    public Type visit(IsNullExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }

    @Override
    public Type visit(IsProcedureExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }

    @Override
    public Type visit(IsListExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }

    @Override
    public Type visit(IsPairExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }

    @Override
    public Type visit(IsUnitExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }

    @Override
    public Type visit(IsNumberExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }

    @Override
    public Type visit(IsStringExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }

    @Override
    public Type visit(IsBooleanExp e, Env<Type> env) {
	Type exp_type = (Type) e.exp().accept(this, env);
	if (exp_type instanceof ErrorT) {
	    return exp_type;
	}
	return BoolT.getInstance();
    }
    
    public static void main(String[] args) {
	System.out.println(
		"TypeLang: Type a program to check and press the enter key,\n"
			+ "e.g. ((lambda (x: num y: num z : num) (+ x (+ y z))) 1 2 3) \n"
			+ "or try (let ((x : num 2)) x) \n"
			+ "or try (car (list : num  1 2 8)) \n"
			+ "or try (ref : num 2) \n"
			+ "or try  (let ((a : Ref num (ref : num 2))) (set! a (deref a))) \n"
			+ "Press Ctrl + C to exit.");
	Reader reader = new Reader();
	Printer printer = new Printer();
	Checker checker = new Checker(); // Type checker
	REPL: while (true) { // Read-Eval-Print-Loop (also known as REPL)
		Program p = null;
		try {
			p = reader.read();
			if(p._e == null) continue REPL;
			Type t = checker.check(p); /*** Type checking the program ***/
 			printer.print(t);
		} catch (Env.LookupException e) {
			printer.print(e);
		} catch (IOException e) {
			System.out.println("Error reading input:" + e.getMessage());
		} catch (NullPointerException e) {
			System.out.println("Error:" + e.getMessage());
		}
	}
	
    }
}
