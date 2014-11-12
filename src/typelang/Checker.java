package typelang;

import java.util.List;

import typelang.AST.*;
import typelang.Type.NumT;

public class Checker implements Visitor<Type,Env> {

    Type check(Program p) {
		return (Type) p.accept(this, null);
	}

	@Override
	public Type visit(AddExp e, Env env) {
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

		List<Exp> operands = e.all();
		for(Exp exp: operands) {
			Type intermediate = (Type) exp.accept(this, env); // Static type-checking
			if(!(intermediate instanceof Type.NumT))
				return Type.ErrorT.getInstance();
		}
		return NumT.getInstance();
	}

	@Override
	public Type visit(Unit e, Env env) {
		return Type.UnitT.getInstance();
	}

	@Override
	public Type visit(Const e, Env env) {
		// Let program = "1", AST is (Program (Const 1)).
		return NumT.getInstance();
	}

	@Override
	public Type visit(StrConst e, Env env) {
		// Let program = "hello", AST is (Program (StrConst "hello")).
		return Type.StringT.getInstance();
	}

	@Override
	public Type visit(BoolConst e, Env env) {
		// Let program = "#t", AST is (Program (BoolConst "#t")).
		// Let program = "#f", AST is (Program (BoolConst "#f")).
		return Type.BoolT.getInstance();
	}

	@Override
	public Type visit(DivExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ErrorExp e, Env env) {
		return Type.ErrorT.getInstance();
	}

	@Override
	public Type visit(MultExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Program p, Env env) {
		for(DefineDecl d: p.decls())
			d.accept(this, env); //TODO: check if define decls type-checked.
		return (Type) p.e().accept(this, env);
	}

	@Override
	public Type visit(SubExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(VarExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(LetExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(DefineDecl d, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ReadExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(EvalExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(LambdaExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(CallExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(LetrecExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(IfExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(LessExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(EqualExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(GreaterExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(CarExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(CdrExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ConsExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ListExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(NullExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(RefExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(DerefExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(AssignExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(FreeExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

}
