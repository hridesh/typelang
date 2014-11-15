package typelang;

import java.util.ArrayList;
import java.util.List;

import typelang.AST.*;
import typelang.Env.ExtendEnv;
import typelang.Type.*;

public class Checker implements Visitor<Type,Env<Type>> {

    Type check(Program p) {
		return (Type) p.accept(this, null);
	}

	public Type visit(AddExp e, Env<Type> env) {
		// Logical assertion: precondition => implications.
		// Let program = "(+ 300 42)", 
		// AST is (Program (AddExp (Const 300) (Const 42))).
		
		// This program's type is NumT.
		// This is because the contained subexpression's type is NumT.
		// This is because the contained subexpression is an addition.
		// An addition expression has type NumT, if and only if,
		// all of its operands have type NumT,
		// and Const expressions have type NumT.
		
		// Let program = "(+ 300 "42")", 
		// AST is (Program (AddExp (Const 300) (StrConst "42"))).

		// This program's type is ErrorT.
		// This is because the contained subexpression's type is ErrorT.
		// This is because the contained subexpression is an addition.
		// An addition expression has type NumT, if and only if,
		// all of its operands have type NumT,
		// First Const expressions has type NumT, but second StrConst
		// expression has type StringT.
		
		// Let program = "(+ 300 x)", 
		// AST is (Program (AddExp (Const 300) (VarExp x))).

		return visitCompoundArithExp(e, env);
	}

	public Type visit(Unit e, Env<Type> env) {
		return Type.UnitT.getInstance();
	}

	public Type visit(Const e, Env<Type> env) {
		// Let program = "1", AST is (Program (Const 1)).
		return NumT.getInstance();
	}

	public Type visit(StrConst e, Env<Type> env) {
		// Let program = "hello", AST is (Program (StrConst "hello")).
		return Type.StringT.getInstance();
	}

	public Type visit(BoolConst e, Env<Type> env) {
		// Let program = "#t", AST is (Program (BoolConst "#t")).
		// Let program = "#f", AST is (Program (BoolConst "#f")).
		return Type.BoolT.getInstance();
	}

	public Type visit(DivExp e, Env<Type> env) {
		return visitCompoundArithExp(e, env);
	}

	public Type visit(ErrorExp e, Env<Type> env) {
		return Type.ErrorT.getInstance();
	}

	public Type visit(MultExp e, Env<Type> env) {
		return visitCompoundArithExp(e, env);
	}

	public Type visit(Program p, Env<Type> env) {
		Env<Type> new_env = env;
		for (DefineDecl d: p.decls()) {
			Type type = (Type)d.accept(this, new_env);
			Type dType = d.type();

			if (!type.typeEqual(dType)) {
				return ErrorT.getInstance();
			}

			new_env = new ExtendEnv<Type>(new_env, d.name(), dType);
		}
		return (Type) p.e().accept(this, new_env);
	}

	public Type visit(SubExp e, Env<Type> env) {
		return visitCompoundArithExp(e, env);
	}

	public Type visit(VarExp e, Env<Type> env) {
		return env.get(e.name());
	}

	public Type visit(LetExp e, Env<Type> env) {
		List<String> names = e.names();
		List<Exp> value_exps = e.value_exps();
		List<Type> values = new ArrayList<Type>(value_exps.size());

		for (Exp exp : value_exps) 
			values.add((Type)exp.accept(this, env));

		Env<Type> new_env = env;
		for (int index = 0; index < names.size(); index++)
			new_env = new ExtendEnv<Type>(new_env, names.get(index),
					values.get(index));

		return (Type) e.body().accept(this, new_env);		
	}

	public Type visit(DefineDecl d, Env<Type> env) {
		return (Type) d._value_exp.accept(this, env);
	}

	public Type visit(ReadExp e, Env<Type> env) {
		// FIXME
		return UnitT.getInstance();
	}

	public Type visit(EvalExp e, Env<Type> env) {
		// FIXME
		return UnitT.getInstance();
	}

	public Type visit(LambdaExp e, Env<Type> env) {
		List<String> names = e.formals();
		Type type = e.type();

		if (type instanceof FuncT) {
			FuncT ft = (FuncT)type;

			List<Type> types = ft.argTypes();
			if (types.size() == names.size()) {
				Env<Type> new_env = env;
				int index = 0;
				for (Type argType : types) { 
					new_env = new ExtendEnv<Type>(new_env, names.get(index),
							argType);
					index++;
				}
				
				Type bodyType = (Type) e.body().accept(this, new_env);
				if (bodyType.typeEqual(ft.returnType())) {
					return ft;
				}
			}
		}

		return ErrorT.getInstance();
	}

	public Type visit(CallExp e, Env<Type> env) {
		Exp operator = e.operator();
		List<Exp> operands = e.operands();

		Type type = (Type)operator.accept(this, env);
		if (type instanceof FuncT) {
			FuncT ft = (FuncT)type;

			List<Type> argTypes = ft.argTypes();
			int size_actuals = operands.size();
			int size_formals = argTypes.size();
			if (size_actuals == size_formals) {
				for (int i = 0; i < size_actuals; i++) {
					Exp operand = operands.get(i);
					Type operand_type = (Type)operand.accept(this, env);

					if (!assignable(argTypes.get(i), operand_type)) {
						return ErrorT.getInstance();
					}
				}
				return ft.returnType();
			}
		}
		return ErrorT.getInstance();
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
			Type type = (Type)fun_exps.get(index).accept(this, new_env);

			if (!assignable(types.get(index), type)) {
				return ErrorT.getInstance();
			}
		}

		return (Type) e.body().accept(this, new_env);
	}

	public Type visit(IfExp e, Env<Type> env) {
		Exp cond = e.conditional();
		Type condType = (Type)cond.accept(this, env);
		if (!(condType instanceof BoolT)) { return ErrorT.getInstance(); }

		Type thentype = (Type)e.then_exp().accept(this, env);
		Type elsetype = (Type)e.else_exp().accept(this, env);

		return unionType(thentype, elsetype);
	}

	public Type visit(LessExp e, Env<Type> env) {
		return visitBinaryComparator(e, env);
	}

	public Type visit(EqualExp e, Env<Type> env) {
		return visitBinaryComparator(e, env);
	}

	public Type visit(GreaterExp e, Env<Type> env) {
		return visitBinaryComparator(e, env);
	}

	public Type visit(CarExp e, Env<Type> env) {
		Exp exp = e.arg();
		Type type = (Type)exp.accept(this, env);
		if (type instanceof PairT) {
			PairT pt = (PairT)type;
			return pt.fst();
		}

		return ErrorT.getInstance();
	}

	public Type visit(CdrExp e, Env<Type> env) {
		Exp exp = e.arg();
		Type type = (Type)exp.accept(this, env);
		if (type instanceof PairT) {
			PairT pt = (PairT)type;
			return pt.snd();
		}

		return ErrorT.getInstance();
	}

	public Type visit(ConsExp e, Env<Type> env) {
		Exp fst = e.fst(); 
		Exp snd = e.snd();

		Type t1 = (Type)fst.accept(this, env);
		if (t1 instanceof ErrorT) { return ErrorT.getInstance(); }

		Type t2 = (Type)snd.accept(this, env);
		if (t2 instanceof ErrorT) { return ErrorT.getInstance(); }

		return new PairT(t1, t2);
	}

	public Type visit(ListExp e, Env<Type> env) {
		List<Exp> elems = e.elems();
		Type type = e.type();

		for (Exp elem : elems) {
			Type elemType = (Type)elem.accept(this, env);
			if (!assignable(type, elemType)) {
				return ErrorT.getInstance();
			}
		}
		return new ListT(type);
	}

	public Type visit(NullExp e, Env<Type> env) {
		Exp arg = e.arg();
		Type type = (Type)arg.accept(this, env);
		if (type instanceof ListT) { return BoolT.getInstance(); }

		return ErrorT.getInstance();
	}

	public Type visit(RefExp e, Env<Type> env) {
		Exp value = e.value_exp();
		Type type = e.type();
		Type expType = (Type)value.accept(this, env);
		if (expType.typeEqual(type)) {
			return new RefT(type);
		}

		return ErrorT.getInstance();
	}

	public Type visit(DerefExp e, Env<Type> env) {
		Exp exp = e.loc_exp();
		Type type = (Type)exp.accept(this, env);

		if (type instanceof RefT) {
			RefT rt = (RefT)type;
			return rt.nestType();
		}

		return ErrorT.getInstance();
	}

	public Type visit(AssignExp e, Env<Type> env) {
		Exp lhs_exp = e.lhs_exp();
		Type lhsType = (Type)lhs_exp.accept(this, env);

        Exp rhs_exp = e.rhs_exp();
        Type rhsType = (Type)rhs_exp.accept(this, env);

        if (lhsType instanceof RefT) {
        	RefT rt = (RefT)lhsType;
        	Type nested = rt.nestType();

        	if (rhsType.typeEqual(nested)) { return rhsType; }
        }

		return ErrorT.getInstance();
	}

	public Type visit(FreeExp e, Env<Type> env) {
		Exp exp = e.value_exp();
		Type type = (Type)exp.accept(this, env);

		if (type instanceof RefT) {
			return UnitT.getInstance();
		}

		return ErrorT.getInstance();
	}

	private boolean isNumType(Exp exp, Env<Type> env) {
		Type first_type = (Type)exp.accept(this, env);
		return first_type instanceof NumT;
	}

	private Type visitBinaryComparator(BinaryComparator e, Env<Type> env) {
		Exp first_exp = e.first_exp();
		Exp second_exp = e.second_exp();

		if (!(isNumType(first_exp, env) && isNumType(second_exp, env))){
			return ErrorT.getInstance();
		}

		return BoolT.getInstance();
	}

	private Type visitCompoundArithExp(CompoundArithExp e, Env<Type> env) {
		List<Exp> operands = e.all();
		for (Exp exp: operands) {
			Type intermediate = (Type) exp.accept(this, env); // Static type-checking
			if(!(intermediate instanceof Type.NumT))
				return Type.ErrorT.getInstance();
		}
		return NumT.getInstance();
	}

	private static boolean assignable(Type t1, Type t2) {
		if (t2 instanceof UnitT) { return true; }

		return t1.typeEqual(t2);
	}

	private static Type unionType(Type t1, Type t2) {
		if (t1.typeEqual(t2)) { return t1; }

		return ErrorT.getInstance();
	}
}
