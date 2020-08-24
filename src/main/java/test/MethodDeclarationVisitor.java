package test;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclarationVisitor extends ASTVisitor {
    String method="";
    @Override
    public boolean visit(MethodDeclaration node) {
        return super.visit(node);
    }
}
