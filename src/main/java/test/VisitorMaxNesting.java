package test;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

public class VisitorMaxNesting extends ASTVisitor {
	int maxNesting=-1;

	public boolean visit(Block node) {
		VisitorMaxNesting visitor=new VisitorMaxNesting();
		List<Statement> statements=node.statements();
		for(Statement statement: statements) {
			statement.accept(visitor);
			if(maxNesting<=visitor.maxNesting) {
				maxNesting=visitor.maxNesting+1;
			}
		}
    	return super.visit(node);
	}
}
