package test;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

public class Requestor extends FileASTRequestor  {
	ArrayList<Method> methods=new ArrayList<Method>();
	ArrayList<String> methodsCalled = new ArrayList<String>();

	public void acceptAST(String pathFile, CompilationUnit ast) {
		System.out.println("---------------"+pathFile+"-----------------------------------------");

		if(//!pathFile.equals("C:\\Users\\ShoOgino\\work\\cassandra\\file1.0\\src\\java\\org\\apache\\cassandra\\service\\StorageService.java")
				// !pathFile.equals("C:\\Users\\ShoOgino\\work\\cassandra\\file2.0\\src\\java\\org\\apache\\cassandra\\hadoop\\ColumnFamilyInputFormat.java")
					//	 & !pathFile.equals("C:\\Users\\ShoOgino\\work\\cassandra\\file2.0\\src\\java\\org\\apache\\cassandra\\hadoop\\cql3\\CqlPagingInputFormat.java")
						 !pathFile.equals("C:\\Users\\ShoOgino\\work\\cassandra\\file3.0\\src\\java\\org\\apache\\cassandra\\hadoop\\cql3\\CqlInputFormat.java")) {
		    Visitor visitor = new Visitor(ast, pathFile);
		    ast.accept(visitor);
		    methods.addAll(visitor.methods);
		    methodsCalled.addAll(visitor.methodsCalled);
		}
	}
}