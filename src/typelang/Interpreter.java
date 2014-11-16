package typelang;
import java.io.IOException;

import typelang.AST.*;

/**
 * This main class implements the Read-Eval-Print-Loop of the interpreter with
 * the help of Reader, Evaluator, and Printer classes. 
 * 
 * @author hridesh
 *
 */
public class Interpreter {
	public static void main(String[] args) {
		System.out.println("TypeLang: Type a program to evaluate and press the enter key,\n" + 
				"e.g. (ref 342) \n" + 
				"or try (deref (ref 342)) \n" +
				"or try (let ((class (ref 342))) (deref class)) \n" +
				"or try (let ((class (ref 342))) (set! class 541)) \n" + 
				"or try  (let ((r (ref 342))) (let ((d (free r))) (deref r))) \n" +
				"Press Ctrl + C to exit.");
		Reader reader = new Reader();
		Evaluator eval = new Evaluator(reader);
		Printer printer = new Printer();
		Checker checker = new Checker(); // Type checker
		try {
			while (true) { // Read-Eval-Print-Loop (also known as REPL)
				Program p = reader.read();
				Type t = checker.check(p); // Type checking the program
				if(t instanceof Type.ErrorT)
{
System.out.println("type error");
					printer.print(t);
}
				else {
					try {
						Value val = eval.valueOf(p);
						printer.print(val);
					} catch (Env.LookupException e) {
						printer.print(e);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading input.");
		}
	}
}
