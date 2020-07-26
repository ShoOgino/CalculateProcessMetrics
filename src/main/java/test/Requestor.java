package test;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class Requestor extends FileASTRequestor  {
	ArrayList<Method> methods=new ArrayList<Method>();
	ArrayList<String> methodsCalled = new ArrayList<String>();

	public void acceptAST(String pathFile, CompilationUnit ast) {
		System.out.println("---------------"+pathFile+"-----------------------------------------");



		Visitor visitor = new Visitor(ast, pathFile);
	    ast.accept(visitor);

	    methods.addAll(visitor.methods);
	    methodsCalled.addAll(visitor.methodsCalled);
	}
}