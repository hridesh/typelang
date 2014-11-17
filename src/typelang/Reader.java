package typelang;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;

import typelang.AST.*;
import typelang.Type.*;

public class Reader {

	private static String GRAMMAR_FILE = "build/typelang/RefLang.g";
	// Following are ANTLR constants - Change them if you change the Grammar.
	// Convention: New rules are always added at the end of the file. 
	private static final String startRule = "program";
	private static final int 
	program = 0, definedecl = 1, exp = 2, varexp = 3, numexp = 4, strconst = 5,
	boolconst = 6, addexp = 7, subexp = 8, multexp = 9, divexp = 10,
	letexp = 11, // New expression for the varlang language.
	lambdaexp = 12, callexp = 13, // New expressions for this language.
	ifexp = 14, lessexp = 15, equalexp = 16, greaterexp = 17, // Other expressions for convenience.
	carexp = 18, cdrexp = 19, consexp = 20, listexp = 21, nullexp = 22,
	letrecexp = 23, // New expression for the letrec language.
	refexp = 24, derefexp = 25, assignexp = 26, freeexp = 27 // New expression for the typelang language.
	, type = 28, booltype = 29, funtype = 30, inttype = 31, listtype = 32,
	pairtype = 33, reftype = 34, stringtype = 35, voidtype = 36
	;

	private static final boolean DEBUG = false;

	Program read() throws IOException {
		String programText = readNextProgram();
		Program program = parse(programText);
		return program;
	}

	Program parse(String programText) {
		final LexerInterpreter lexEngine = lg.createLexerInterpreter(
				new ANTLRInputStream(programText));
		final CommonTokenStream tokens = new CommonTokenStream(lexEngine);
		final ParserInterpreter parser = g.createParserInterpreter(tokens);
		final ParseTree t = parser.parse(g.rules.get(startRule).index);
		if(DEBUG) 
			System.out.println("parse tree: " + t.toStringTree(parser));
		Program program = convertParseTreeToAST(parser, t);
		return program;
	}

	private Program convertParseTreeToAST(ParserInterpreter parser, ParseTree parseTree) {
		// We know that top-level parse tree node is a program, and for this 
		// language it contains an list of zero or more declarations followed by 
		// a single expression, so we first extract the declarations and then convert the 
		// enclosing expression's parse tree to the AST used by this interpreter.
		int numDecls = parseTree.getChildCount() - 1;
		List<DefineDecl> definedecls = new ArrayList<DefineDecl>();
		TreeToExpConverter convertor = new TreeToExpConverter(parser);
		for(int i=0; i < numDecls ; i++) 
			definedecls.add((DefineDecl) parseTree.getChild(i).accept(convertor));
		Exp exp = parseTree.getChild(numDecls).accept(new TreeToExpConverter(parser));
		if(exp instanceof DefineDecl) {
			definedecls.add((DefineDecl) exp);
			return new Program(definedecls, new Unit());
		} 
		return new Program(definedecls, exp);
	}

	private static final LexerGrammar lg = createLexicalGrammar();
	private static LexerGrammar createLexicalGrammar() {
		LexerGrammar lg = null;
		try {
			lg = new LexerGrammar(readFile(GRAMMAR_FILE));
		} catch (RecognitionException e) {
			System.out.println("ErrorExp in Lexical Specification\n" + e);
			System.exit(-1); // These are fatal errors
		}
		return lg;
	}

	private static final Grammar g = createGrammar();
	private static Grammar createGrammar() {
		Grammar g = null;
		try {
			g = new Grammar(readFile(GRAMMAR_FILE), Reader.lg);
		} catch (RecognitionException e) {
			System.out.println("Error in Grammar Specification\n" + e);
			System.exit(-1); // These are fatal errors
		}
		return g;
	}

	static String readFile(String fileName) {
		try {
			try (BufferedReader br = new BufferedReader(
					new FileReader(fileName))) {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				return sb.toString();
			}
		} catch (IOException e) {
			System.out.println("Could not open file " + fileName);
			System.exit(-1); // These are fatal errors
		}
		return "";
	}

	public class ConversionException extends RuntimeException {
		private static final long serialVersionUID = -5663970743340723405L;
		public ConversionException(String message){
			super(message);
		}
	}
	/**
	 * This data adapter takes the parse tree provided by ANTLR and converts it
	 * to the abstract syntax tree representation used by the rest of this
	 * interpreter. This class needs to be adapted for each new abstract syntax
	 * tree node.
	 * 
	 * @author hridesh
	 * 
	 */
	class TreeToExpConverter implements ParseTreeVisitor<AST.Exp> {
		TreeToTypeConverter tttc;

		ParserInterpreter parser;

		TreeToExpConverter(ParserInterpreter parser) {
			this.parser = parser;

			tttc = new TreeToTypeConverter(parser);
		}

		public AST.Exp visit(ParseTree tree) {
			System.out.println("visit: " + tree.toStringTree(parser));
			return null;
		}

		public AST.Exp visitChildren(RuleNode node) {
			switch(node.getRuleContext().getRuleIndex()){
			case varexp: return convertVarExp(node);
			case numexp: return convertConst(node);
			case strconst: return convertStrConst(node);
			case boolconst: return convertBoolConst(node);
			case addexp: return convertAddExp(node); 
			case subexp: return convertSubExp(node); 
			case multexp: return convertMultExp(node);
			case divexp: return convertDivExp(node);
			case letexp: return convertLetExp(node);
			case definedecl: return convertDefineDecl(node);
			case lambdaexp: return convertLambdaExp(node);
			case callexp: return convertCallExp(node);
			case ifexp: return convertIfExp(node);
			case lessexp: return convertLessExp(node);
			case equalexp: return convertEqualExp(node);
			case greaterexp: return convertGreaterExp(node);
			case carexp: return convertCarExp(node);
			case cdrexp: return convertCdrExp(node);
			case consexp: return convertConsExp(node);
			case listexp: return convertListExp(node);
			case nullexp: return convertNullExp(node);
			case letrecexp: return convertLetrecExp(node);
			case refexp: return convertRefExp(node);
			case derefexp: return convertDerefExp(node);
			case assignexp: return convertAssignExp(node);
			case freeexp: return convertFreeExp(node);
			case exp: return visitChildrenHelper(node).get(0);
			case program: 
			default: 
				System.out.println("Conversion error (from parse tree to AST): found unknown/unhandled case " + parser.getRuleNames()[node.getRuleContext().getRuleIndex()]);
			}
			return null;
		}

		/**
		 *  Syntax: Identifier
		 */  
		private AST.VarExp convertVarExp(RuleNode node){
			if(node.getChildCount() > 1)
				throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
						"expected only Identifier, found " + node.getChildCount() +  " nodes.");

			String s = node.getChild(0).getText();
			return new AST.VarExp(s);
		}

		/**
		 *  Syntax: Number
		 */  
		private AST.Const convertConst(RuleNode node){
			try {
				String s = node.getChild(0).toStringTree(parser);
				if(node.getChildCount() > 2) {
					s+=node.getChild(1).toStringTree(parser);
					s+=node.getChild(2).toStringTree(parser);
				}
				double v = Double.parseDouble(s);
				return new AST.Const(v);
			} catch (NumberFormatException e) {
				throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
						"expected Number, found " + node.getChild(0).toStringTree(parser));
			}
		}

		/**
		 *  Syntax: "a string"
		 */  
		private AST.StrConst convertStrConst(RuleNode node){
			String s = node.getChild(0).toStringTree(parser);
			s = s.substring(1, s.length()-1); //Trim to remove quotes.
			return new AST.StrConst(s);
		}

		/**
		 *  Syntax: #t or #f
		 */  
		private AST.BoolConst convertBoolConst(RuleNode node){
			String s = node.getChild(0).toStringTree(parser);
			if(s.equals("#t"))
				return new AST.BoolConst(true);
			return new AST.BoolConst(false);
		}

		/**
		 *  Syntax: (+ exp* )
		 */  
		private AST.Exp convertAddExp(RuleNode node){
			int index = expect(node,0,"(", "+");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.AddExp(operands);
		}

		/**
		 *  Syntax: (- exp* )
		 */  
		private AST.Exp convertSubExp(RuleNode node){
			int index = expect(node,0,"(", "-");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.SubExp(operands);
		}

		/**
		 *  Syntax: (* exp* )
		 */  
		private AST.Exp convertMultExp(RuleNode node){
			int index = expect(node,0,"(", "*");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.MultExp(operands);
		}

		/**
		 *  Syntax: (/ exp* )
		 */  
		private AST.Exp convertDivExp(RuleNode node){
			int index = expect(node,0,"(", "/");
			List<AST.Exp> operands = expectOperands(node, index);
			return new AST.DivExp(operands);
		}

		List<AST.Exp> expectOperands(RuleNode node, int startChildIndex) {
			int index = startChildIndex; 
			List<AST.Exp> operands = new ArrayList<AST.Exp>();	
			while (!match(node,index,")")) {
				AST.Exp operand = node.getChild(index++).accept(this);
				operands.add(operand);
			}
			expect(node,index++, ")");
			return operands;
		}

		/**
		 *  Syntax: (let ((name value_exp)* ) body_exp)
		 */  
		private AST.Exp convertLetExp(RuleNode node){
			int index = expect(node,0,"(", "let", "(");
			List<String> names = new ArrayList<String>();	
			List<Type> types = new ArrayList<>();
			List<AST.Exp> value_exps = new ArrayList<AST.Exp>();	
			while (match(node,index,"(")) {
				index++;
				String name = expectString(node, index++, 1)[0];
				names.add(name);

				expect(node, index++, ":");

				Type type = node.getChild(index++).accept(tttc);
				types.add(type);

				AST.Exp value_exp = expectExp(node, index++, 1)[0];
				value_exps.add(value_exp);
				index = expect(node,index, ")");
			}
			expect(node,index++, ")");
			AST.Exp body = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.LetExp(names, types, value_exps, body);
		}

		/**
		 *  Syntax: (define name value_exp)
		 */  
		private AST.Exp convertDefineDecl(RuleNode node){
			int index = expect(node,0,"(", "define");
			String name = expectString(node, index++, 1)[0];

			expect(node, index++, ":");

			Type type = node.getChild(index++).accept(tttc);

			AST.Exp value_exp = expectExp(node, index++, 1)[0];
			expect(node,index++, ")");
			return new AST.DefineDecl(name, type, value_exp);
		}

		/**
		 *  Syntax: ( lambda ( Identifier+ ) body_exp )
		 */
		private AST.Exp convertLambdaExp(RuleNode node){
			int index = expect(node,0,"(", "lambda", "(");
			List<String> formals = new ArrayList<String>();	
			while (!match(node,index,":")) {
				String formal = expectString(node, index++, 1)[0];
				formals.add(formal);
			}

			expect(node, index++, ":");

			Type type = node.getChild(index++).accept(tttc);

			expect(node,index++, ")");
			AST.Exp body_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.LambdaExp(formals, type, body_exp);
		}

		/**
		 *  Syntax: ( operator_exp operand_exp* )
		 */
		private AST.Exp convertCallExp(RuleNode node){
			int index = expect(node,0,"(");
			AST.Exp operator = node.getChild(index++).accept(this);
			List<AST.Exp> operands = new ArrayList<AST.Exp>();	
			while (!match(node,index,")")) {
				AST.Exp operand = node.getChild(index++).accept(this);
				operands.add(operand);
			}
			expect(node,index++, ")");
			return new AST.CallExp(operator, operands);
		}

		/**
		 *  Syntax: ( if conditional_exp then_exp else_exp )
		 */
		private AST.Exp convertIfExp(RuleNode node){
			int index = expect(node,0,"(", "if");
			AST.Exp conditional = node.getChild(index++).accept(this);
			AST.Exp then_exp = node.getChild(index++).accept(this);
			AST.Exp else_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.IfExp(conditional, then_exp, else_exp);
		}

		/**
		 *  Syntax: ( < first_exp second_exp )
		 */
		private AST.Exp convertLessExp(RuleNode node){
			int index = expect(node,0,"(","<");
			AST.Exp first_exp = node.getChild(index++).accept(this);
			AST.Exp second_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.LessExp(first_exp, second_exp);
		}

		/**
		 *  Syntax: ( == first_exp second_exp )
		 */
		private AST.Exp convertEqualExp(RuleNode node){
			int index = expect(node,0,"(","=");
			AST.Exp first_exp = node.getChild(index++).accept(this);
			AST.Exp second_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.EqualExp(first_exp, second_exp);
		}

		/**
		 *  Syntax: ( > first_exp second_exp )
		 */
		private AST.Exp convertGreaterExp(RuleNode node){
			int index = expect(node,0,"(",">");
			AST.Exp first_exp = node.getChild(index++).accept(this);
			AST.Exp second_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.GreaterExp(first_exp, second_exp);
		}

		/**
		 *  Syntax: ( car exp )
		 */
		private AST.Exp convertCarExp(RuleNode node){
			int index = expect(node,0,"(","car");
			AST.Exp _exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.CarExp(_exp);
		}

		/**
		 *  Syntax: ( cdr exp )
		 */
		private AST.Exp convertCdrExp(RuleNode node){
			int index = expect(node,0,"(","cdr");
			AST.Exp _exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.CdrExp(_exp);
		}

		/**
		 *  Syntax: ( operator_exp operand_exp* )
		 */
		private AST.Exp convertListExp(RuleNode node){
			int index = expect(node,0,"(", "list");
			List<AST.Exp> operands = new ArrayList<AST.Exp>();

			expect(node, index++, ":");

			Type type = node.getChild(index++).accept(tttc);

			while (!match(node,index,")")) {
				AST.Exp operand = node.getChild(index++).accept(this);
				operands.add(operand);
			}
			expect(node,index++, ")");
			return new AST.ListExp(type, operands);
		}

		/**
		 *  Syntax: ( cons first_exp second_exp )
		 */
		private AST.Exp convertConsExp(RuleNode node){
			int index = expect(node,0,"(","cons");
			AST.Exp first_exp = node.getChild(index++).accept(this);
			AST.Exp second_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.ConsExp(first_exp, second_exp);
		}

		/**
		 *  Syntax: ( car exp )
		 */
		private AST.Exp convertNullExp(RuleNode node){
			int index = expect(node,0,"(","null?");
			AST.Exp _exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.NullExp(_exp);
		}

		/**
		 *  Syntax: (letrec ((name value_exp)* ) body_exp)
		 */  
		private AST.Exp convertLetrecExp(RuleNode node){
			int index = expect(node,0,"(", "letrec", "(");
			List<String> names = new ArrayList<String>();	
			List<Type> types = new ArrayList<>();
			List<AST.Exp> value_exps = new ArrayList<AST.Exp>();	
			while (match(node,index,"(")) {
				index++;
				String name = expectString(node, index++, 1)[0];
				names.add(name);

				expect(node, index++, ":");

				Type type = node.getChild(index++).accept(tttc);
				types.add(type);

				AST.Exp value_exp = expectExp(node, index++, 1)[0];
				value_exps.add(value_exp);
				index = expect(node,index, ")");
			}
			expect(node,index++, ")");
			AST.Exp body = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.LetrecExp(names, types, value_exps, body);
		}

		/**
		 *  Syntax: ( ref value_exp )
		 */
		private AST.Exp convertRefExp(RuleNode node){
			int index = expect(node,0,"(", "ref");

			expect(node, index++, ":");

			Type type = node.getChild(index++).accept(tttc);

			AST.Exp value_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.RefExp(value_exp, type);
		}

		/**
		 *  Syntax: ( deref loc_exp )
		 */
		private AST.Exp convertDerefExp(RuleNode node){
			int index = expect(node,0,"(", "deref");
			AST.Exp loc_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.DerefExp(loc_exp);
		}

		/**
		 *  Syntax: ( set! lhs_exp rhs_exp )
		 */
		private AST.Exp convertAssignExp(RuleNode node){
			int index = expect(node,0,"(", "set!");
			AST.Exp lhs_exp = node.getChild(index++).accept(this);
			AST.Exp rhs_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.AssignExp(lhs_exp, rhs_exp);
		}

		/**
		 *  Syntax: ( ref value_exp )
		 */
		private AST.Exp convertFreeExp(RuleNode node){
			int index = expect(node,0,"(", "free");
			AST.Exp value_exp = node.getChild(index++).accept(this);
			expect(node,index++, ")");
			return new AST.FreeExp(value_exp);
		}

		public AST.Exp visitTerminal(TerminalNode node) {
			String s = node.toStringTree(parser);
			if (isConcreteSyntaxToken(s))
				return null;

			try {
				int v = Integer.parseInt(s);
				return new AST.Const(v);
			} catch (NumberFormatException e) {
			}
			// Error case - generally means a new Token is added in the grammar
			// and not handled in
			// the filterTokens method above, or a new value type is added.
			System.out.println("visitTerminal: Illegal terminal " + s);
			return new AST.ErrorExp();
		}

		public AST.Exp visitErrorNode(ErrorNode node) {
			System.out.println("visitErrorNode: " + node.toStringTree(parser));
			return new AST.ErrorExp();
		}

		private List<AST.Exp> visitChildrenHelper(RuleNode node) {
			int childCount = node.getChildCount();
			List<AST.Exp> results = new ArrayList<AST.Exp>(); 
			if(DEBUG) System.out.println("visitChildren(RuleNode node), node = "
					+ node.toStringTree(parser) + ", #children = "
					+ childCount);
			for (int i = 0; i < childCount; i++) {
				AST.Exp result = node.getChild(i).accept(this);
				if(result!=null) results.add(result);
			}
			return results;
		}

		/**
		 * This method filters out those Tokens that are part of the concrete
		 * syntax and thus are not represented in the abstract syntax.
		 * 
		 * @param s - string representation of the token
		 * @return true if the token is part of concrete syntax.
		 */
		private boolean isConcreteSyntaxToken(String s) {
			if (s.equals("(") || s.equals(")") || s.equals("+")
					|| s.equals("-") || s.equals("*") || s.equals("/"))
				return true;
			return false;
		}

		/**
		 * Expect nth, n+1th, ..., n+mth children of node to be expressions
		 * @param node - node to be examined.
		 * @param n - index of first child.
		 * @param numTokens - expected number of expressions.
		 * @return an array of n + numTokens expressions, if expectations is met. Otherwise, throws ConversionException.
		 */
		protected AST.Exp[] expectExp(RuleNode node, int n, int numTokens){
			AST.Exp results[] = new AST.Exp[numTokens];
			for(int i = 0; i< numTokens; i++) {
				AST.Exp value = node.getChild(n+i).accept(this);
				if(value == null || value instanceof AST.ErrorExp)
					throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
							"expected Exp, found " + node.getChild(n+i).toStringTree(parser));
				results[i] = value;
			}
			return results;
		}

		/**
		 * Expect nth, n+1th, ..., n+mth children of node to be strings
		 * @param node - node to be examined.
		 * @param n - index of first child.
		 * @param numTokens - expected number of strings.
		 * @return an array of n + numTokens strings, if expectations is met. Otherwise, throws ConversionException.
		 */
		protected String[] expectString(RuleNode node, int n, int numTokens){
			String results[] = new String[numTokens];
			for(int i = 0; i< numTokens; i++, n++) {
				String value = node.getChild(n).toString();
				if(value == null || node.getChild(n).getChildCount()!=0)
					throw new ConversionException("Conversion error: " + node.toStringTree(parser) + ", " + 
							"expected Exp, found " + node.getChild(n).toStringTree(parser));
				results[i] = value;
			}
			return results;
		}

		/**
		 * Expect nth, n+1th, ... children of node to match the token 
		 * @param node - node to be examined.
		 * @param n - index of child.
		 * @param tokens - expected strings.
		 * @return n + tokens.length, if expectations is met. Otherwise, throws ConversionException.
		 */
		protected int expect(RuleNode node, int n, String ... tokens) {
			return staticexpect(parser, node, n, tokens);
		}

		protected boolean match(RuleNode node, int n, String ... tokens) {
			return staticmatch(parser, node, n, tokens);
		}
	}

	/**
	 * Expect nth, n+1th, ... children of node to match the token 
	 * @param node - node to be examined.
	 * @param n - index of child.
	 * @param tokens - expected strings.
	 * @return n + tokens.length, if expectations is met. Otherwise, throws ConversionException.
	 */
	protected int staticexpect(ParserInterpreter parser, RuleNode node, int n,
			String ... tokens) {
		int numTokens = tokens.length;
		for(int i = 0; i< numTokens; i++) {
			if (!node.getChild(n+i).toStringTree(parser).equals(tokens[i])) 
				throw new ConversionException("Conversion error: " +
						node.toStringTree(parser) + ", " + "expected " +
						tokens[i] + ", found " +
						node.getChild(n+i).toStringTree(parser));
		}
		return n+numTokens;
	}

	/**
	 * Test if nth, n+1th, ... children of node match the token 
	 * @param node - node to be examined.
	 * @param n - index of child.
	 * @param tokens - expected strings.
	 * @return true, if test is met. False, otherwise.
	 */
	public static boolean staticmatch(ParserInterpreter parser, RuleNode node,
			int n, String ... tokens) {
		int numTokens = tokens.length;
		for(int i = 0; i < numTokens; i++) {
			if (!node.getChild(n+i).toStringTree(parser).equals(tokens[i])) 
				return false;
		}
		return true;
	}

	private String readNextProgram() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("$ ");
		String programText = br.readLine();
		return runFile(programText);
	}

	private String runFile(String programText) throws IOException {
		if (programText.startsWith("run ")) {
			programText = readFile("build/typelang/" + programText.substring(4));
		}
		return programText; 
	}

	public class TreeToTypeConverter implements ParseTreeVisitor<Type> {
		public ParserInterpreter parser;

		public TreeToTypeConverter(ParserInterpreter parser) {
			this.parser = parser;
		}

		public Type visit(ParseTree tree) {
			System.out.println("TreeToTypeConverter visit: " +
					tree.toStringTree(parser));
			return null;
		}

		public Type visitChildren(RuleNode node) { 
			switch(node.getRuleContext().getRuleIndex()){
			case booltype: return BoolT.getInstance();
			case funtype: return getFunType(node);
			case inttype: return NumT.getInstance();
			case listtype: return getListType(node);
			case pairtype: return getPairType(node);
			case reftype: return getRefType(node);
			case stringtype: return StringT.getInstance();
			case voidtype: return UnitT.getInstance();
			case type: return visitChildrenHelper(node);
			default:
				System.out.println("Conversion error (from parse tree to AST):"
						+ " found unknown/unhandled case " +
						parser.getRuleNames()[node.getRuleContext().getRuleIndex()]);
				// throw new Error();
			}
			return null;
		}

		public Type visitErrorNode(ErrorNode node) {
			System.out.println("visitErrorNode: " +
					node.toStringTree(parser));
			return new ErrorT("visitErrorNode: " + node.toStringTree(parser));
		}

		public Type visitTerminal(TerminalNode node) {
			String s = node.toStringTree(parser);
			if (s.compareTo("int") == 0) { return NumT.getInstance(); }
			if (s.compareTo("boolean") == 0) { return BoolT.getInstance(); }
			if (s.compareTo("String") == 0) { return StringT.getInstance(); }
			if (s.compareTo("void") == 0) { return UnitT.getInstance(); }

			System.out.println("visitTerminal: Illegal terminal " + s);
			return UnitT.getInstance();
		}

		private FuncT getFunType(RuleNode node){
			int index = 0;

			staticexpect(parser, node, index++, "(");

			List<Type> argTypes = new ArrayList<>();

			while (!staticmatch(parser, node, index, "->")) {
				argTypes.add(node.getChild(index++).accept(this));
			}

			staticexpect(parser, node, index++, "->");

			Type returnType = node.getChild(index++).accept(this);

			staticexpect(parser, node, index++, ")");

			return new FuncT(argTypes, returnType);
		}

		private ListT getListType(RuleNode node){
			int index = 0;

			staticexpect(parser, node, index++, "List");
			staticexpect(parser, node, index++, "<");

			Type type = node.getChild(index++).accept(this);

			staticexpect(parser, node, index++, ">");

			return new ListT(type);
		}

		private PairT getPairType(RuleNode node){
			int index = 0;

			staticexpect(parser, node, index++, "(");

			Type t1 = node.getChild(index++).accept(this);

			staticexpect(parser, node, index++, ",");

			Type t2 = node.getChild(index++).accept(this);

			staticexpect(parser, node, index++, ")");

			return new PairT(t1, t2);
		}

		private RefT getRefType(RuleNode node){
			int index = 0;

			staticexpect(parser, node, index++, "Ref");

			return new RefT(node.getChild(index++).accept(this));
		}

		private Type visitChildrenHelper(RuleNode node) {
			int childCount = node.getChildCount();
			if (childCount == 1) {
				Type type = node.getChild(0).accept(this);
				if (type != null) return type;
			}
			throw new Error();
		}
	}
}
