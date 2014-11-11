package typelang;

import typelang.AST.AddExp;
import typelang.AST.AssignExp;
import typelang.AST.BoolConst;
import typelang.AST.CallExp;
import typelang.AST.CarExp;
import typelang.AST.CdrExp;
import typelang.AST.ConsExp;
import typelang.AST.Const;
import typelang.AST.DefineDecl;
import typelang.AST.DerefExp;
import typelang.AST.DivExp;
import typelang.AST.EqualExp;
import typelang.AST.ErrorExp;
import typelang.AST.EvalExp;
import typelang.AST.FreeExp;
import typelang.AST.GreaterExp;
import typelang.AST.IfExp;
import typelang.AST.LambdaExp;
import typelang.AST.LessExp;
import typelang.AST.LetExp;
import typelang.AST.LetrecExp;
import typelang.AST.ListExp;
import typelang.AST.MultExp;
import typelang.AST.NullExp;
import typelang.AST.Program;
import typelang.AST.ReadExp;
import typelang.AST.RefExp;
import typelang.AST.StrConst;
import typelang.AST.SubExp;
import typelang.AST.Unit;
import typelang.AST.VarExp;
import typelang.AST.Visitor;

public class Checker implements Visitor<Type> {

	@Override
	public Type visit(AddExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Unit e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Const e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(StrConst e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(BoolConst e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(DivExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(ErrorExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(MultExp e, Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type visit(Program p, Env env) {
		// TODO Auto-generated method stub
		return null;
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
