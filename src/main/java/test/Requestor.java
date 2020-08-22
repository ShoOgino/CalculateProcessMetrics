package test;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class Requestor extends FileASTRequestor  {
	ArrayList<Method> methods=new ArrayList<Method>();
	ArrayList<String> methodsCalled = new ArrayList<String>();

	public void acceptAST(String pathFile, CompilationUnit ast) {
		System.out.println("---------------"+pathFile+"-----------------------------------------");

		if(!pathFile.equals("C:\\Users\\login\\work\\cassandra\\file1\\src\\java\\org\\apache\\cassandra\\service\\StorageService.java")
		   & !pathFile.equals("C:\\Users\\login\\work\\cassandra\\file2\\src\\java\\org\\apache\\cassandra\\hadoop\\ColumnFamilyInputFormat.java")
		   & !pathFile.equals("C:\\Users\\login\\work\\cassandra\\file2\\src\\java\\org\\apache\\cassandra\\hadoop\\cql3\\CqlPagingInputFormat.java")
		   & !pathFile.equals("C:\\Users\\login\\work\\cassandra\\file3\\src\\java\\org\\apache\\cassandra\\hadoop\\cql3\\CqlInputFormat.java")) {
		    Visitor visitor = new Visitor(ast, pathFile);
		    ast.accept(visitor);
		    methods.addAll(visitor.methods);
		    methodsCalled.addAll(visitor.methodsCalled);
		}
	}
}