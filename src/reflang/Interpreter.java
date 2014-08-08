package reflang;
import java.io.IOException;

import reflang.AST.*;

/**
 * This main class implements the Read-Eval-Print-Loop of the interpreter with
 * the help of Reader, Evaluator, and Printer classes. 
 * 
 * @author hridesh
 *
 */
public class Interpreter {
	public static void main(String[] args) {
		System.out.println("Type a program to evaluate and press the enter key,\n" + 
							"e.g. (let ((class (ref 0))) (let ((res (set! class 342))) (deref class))) \n" + 
							"Press Ctrl + C to exit.");
		Reader reader = new Reader();
		Evaluator eval = new Evaluator();
		Printer printer = new Printer();
		try {
			while (true) { // Read-Eval-Print-Loop (also known as REPL)
				Program p = reader.read();
				try {
					Value val = eval.valueOf(p);
					printer.print(val);
				} catch (Env.LookupException e) {
					printer.print(e);
				} catch (Store.StoreException e) {
					printer.print(e);
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading input.");
		}
	}
}
