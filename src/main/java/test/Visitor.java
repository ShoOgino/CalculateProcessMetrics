package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

public class Visitor extends ASTVisitor {
	CompilationUnit file;
	ArrayList<Method> methods=new ArrayList<Method>();
	ArrayList<String> methodsCalled= new ArrayList<String>();
    ArrayList<String> source = new ArrayList<String>();

	public Visitor(CompilationUnit ast, String pathFile) {
		this.file=ast;
		try(FileReader fr = new FileReader(pathFile); BufferedReader br = new BufferedReader(fr);){
			String str = br.readLine();
			while(str!=null) {
				this.source.add(str);
				str = br.readLine();
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

    public class MethodNameGenerator {
        private final MethodDeclaration node;
        private final StringBuilder buffer = new StringBuilder();

        public MethodNameGenerator(final MethodDeclaration node) {
            this.node = node;
        }

        public String generate() {
            generateTypeParameters();
            // generateReturnType();
            generateName();
            generateParameters();
            return buffer.toString();
        }

        protected void generateTypeParameters() {
            @SuppressWarnings("unchecked")
            final List<Object> types = node.typeParameters();
            if (types != null && !types.isEmpty()) {
                final String typenames = types.stream()
                        .map(o -> escape(o.toString()))
                        .collect(Collectors.joining(","));
                buffer.append("[").append(typenames).append("]_");
            }
        }

        protected void generateReturnType() {
            final Type type = node.getReturnType2();
            if (type != null) {
                buffer.append(escape(type.toString())).append("_");
            }
        }

        protected void generateName() {
            buffer.append(node.getName().getIdentifier());
        }

        protected void generateParameters() {
            @SuppressWarnings("unchecked")
            final List<Object> params = node.parameters();
            final String names = params.stream()
                    .map(o -> getTypeName((SingleVariableDeclaration) o))
                    .collect(Collectors.joining(","));
            buffer.append("(").append(names).append(")");
        }

        protected String getTypeName(final SingleVariableDeclaration v) {
            final StringBuilder sb = new StringBuilder();
            sb.append(escape(v.getType().toString()));
            for (int i = 0; i < v.getExtraDimensions(); i++) {
                sb.append("[]");
            }
            if (v.isVarargs()) {
                sb.append("...");
            }
            return sb.toString();
        }

        protected String escape(final String s) {
            return s.replace(' ', '-')
                    .replace('?', '#')
                    .replace('<', '[')
                    .replace('>', ']');
        }
    }


	@Override
	public boolean visit(MethodDeclaration node) {
		String test=node.getName().getIdentifier();
		if(test.contains("."))
		    System.out.println(test);
		if(node.getBody()==null) {
			return false;
		}

		Method method = new Method();
        method.name = new MethodNameGenerator(node).generate();
        //if(method.name.contains("extends"))
            System.out.println(method.name);

		//	System.out.println(method.id);

		VisitorFanout visitorFanout = new VisitorFanout();
		node.accept(visitorFanout);
		method.fanOut = visitorFanout.fanout;

		method.parameters = node.parameters().size();

		VisitorLocalVar visitorLocalVar = new VisitorLocalVar();
		node.accept(visitorLocalVar);
		method.localVar = visitorLocalVar.NOVariables;

		method.commentRatio = calculateCommentRatio(node.getBody());

		VisitorCountPath visitorCountPath =new VisitorCountPath();
		node.accept(visitorCountPath);
		int path=1;
		for(int branch: visitorCountPath.branches) {
			path*=branch;
		}
		method.countPath=path;

		VisitorComplexity visitorComplexity =new VisitorComplexity();
		node.accept(visitorComplexity);
		method.complexity=visitorComplexity.complexity;

		VisitorExecStmt visitorStatement =new VisitorExecStmt();
		if(node.getBody().statements().size()>0) {
			((ASTNode) node.getBody().statements().get(0)).accept(visitorStatement);
			method.execStmt=visitorStatement.execStmt;
		}else {
			method.execStmt=0;
		}


		VisitorMaxNesting visitorMaxNesting =new VisitorMaxNesting();
		node.accept(visitorMaxNesting);
		method.maxNesting=visitorMaxNesting.maxNesting;

		methods.add(method);
		return super.visit(node);
	}


	public boolean visit(MethodInvocation node) {
		String id=calculateIDMethod(node);
		methodsCalled.add(id);

		return super.visit(node);
	}


	//purpose: skip anonymous class.
	public boolean visit(AnonymousClassDeclaration classDeclarationStatement) {
		return false;
	}

    public float calculateCommentRatio(Block node) {
    	int startLineNumber=file.getLineNumber(node.getStartPosition())-1;
    	int endLineNumber=file.getLineNumber(node.getStartPosition()+node.getLength())-1;
		String[] lines=new String[endLineNumber-startLineNumber+1];
    	for(int lineCount=startLineNumber;lineCount<=endLineNumber;lineCount++) {
    		lines[lineCount-startLineNumber]=source.get(lineCount);
    		//System.out.println(lines[lineCount-startLineNumber]);
    	}

		int CountLineCode=0;
		int CountLineComment=0;
		boolean inComment=false;
		for(String line:lines) {
		    if(line.matches(".*\\*/.*")) {
    			inComment=false;
    			CountLineComment++;
		    }
		    if(inComment) {
    		    CountLineComment++;
		    }else if(line.matches(".*//.*|.*/\\*.*")) {
				CountLineComment++;
				if(line.matches(".*/\\*.*")){
					inComment=true;
				}
			}
			if(!inComment & !line.matches("\\s*|\\s*//.*|\\s*/\\*.*|.*\\*/.*")) {
	    	    CountLineCode++;
			}
		}
   		return (float) CountLineComment/ (float)CountLineCode;
    }



	public String calculateIDMethod(MethodInvocation node) {
		if(node.resolveMethodBinding()!=null) {
			String namePackage = node.resolveMethodBinding().getDeclaringClass().getPackage().getName();
			String nameClass="";
			ITypeBinding classLast = node.resolveMethodBinding().getDeclaringClass();
			int count=0;
			while(classLast!=null){
				nameClass="/" + classLast.getName().toString() + nameClass;
				classLast=classLast.getDeclaringClass();
				count=count+1;
			}

			String nameMethod = node.getName().toString();
			String nameArgment = "";

			if(node.resolveMethodBinding().getMethodDeclaration().getParameterTypes().length!=0) {
				ITypeBinding[] argments = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
				nameArgment=argments[0].getName().toString();
				for(ITypeBinding argment: argments) {
					nameArgment = nameArgment+","+argment.getName().toString();
				}
			}

			return namePackage + nameClass + "/" + nameMethod + "(" + nameArgment + ")";
		}else {
			return "";
		}
	}


	public String calculateIDMethod(MethodDeclaration node){
		String namePackage = node.resolveBinding().getDeclaringClass().getPackage().getName();
		String nameClass="";
		ITypeBinding classLast = node.resolveBinding().getDeclaringClass();
		int count=0;
		while(classLast!=null){
			nameClass="/" + classLast.getName().toString() + nameClass;
			classLast=classLast.getDeclaringClass();
			count=count+1;
		}

		String nameMethod = node.getName().toString();
		String nameArgment = "";

		if(node.parameters().size()!=0) {
			List<SingleVariableDeclaration> argments=new ArrayList<>(node.parameters());
			nameArgment=argments.get(0).getType().toString();
			argments.remove(0);
			for(SingleVariableDeclaration argment: argments) {
				nameArgment = nameArgment+","+argment.getType().toString();
			}
		}
		return namePackage + nameClass + "/" + nameMethod + "(" + nameArgment + ")";
	}
}