package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.TreeDelete;
import com.github.gumtreediff.actions.model.TreeInsert;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;

public class Test {
	public static void main(String[] args) throws IOException {
		//long start = System.currentTimeMillis();
		Method test= new Method();
		//test.path="C:/Users/login/work/cassandra_method/src/java/org/apache/cassandra/utils/XMLUtils#XMLUtils(String).mjava";
		//getProcessMetrics(test, new Project("test", "C:/Users/login/work/cassandra_method"));

		String release = "3.0";
		Project project=new Project(release, "C:\\Users\\login\\work\\cassandra\\file"+release, "C:\\Users\\login\\work\\cassandra\\method");
		HashMap<String, Method> dataset=getDataset(project);

		try {
			File csv = new File(project.id+".csv");
			BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
			for(String key: dataset.keySet()) {
				Method method=dataset.get(key);
				bw.write(
						"\""+method.path + "\"" + ","+
						method.isBuggy  + "," +

						method.fanIN + ","+
						method.fanOut +","+
						method.parameters + "," +
						method.localVar + "," +
						method.commentRatio +","+
						method.countPath + ","+
						method.complexity + "," +
						method.execStmt + "," +
						method.maxNesting + "," +

                        method.methodHistories + "," +
                        method.authors + "," +
                        method.stmtAdded + "," +
                        method.maxStmtAdded + "," +
                        method.avgStmtAdded + "," +
                        method.stmtDeleted + "," +
                        method.maxStmtDeleted + "," +
                        method.avgStmtDeleted + "," +
                        method.churn + "," +
                        method.maxChurn + "," +
                        method.avgChurn + "," +
                        method.decl + "," +
                        method.cond + "," +
                        method.elseAdded + "," +
                        method.elseDeleted
				);
				bw.newLine();
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//long end = System.currentTimeMillis();
		//System.out.println((end - start)  + "ms");
	}


	public static String readAll(final String path) throws IOException {
	    return Files.lines(Paths.get(path), Charset.forName("UTF-8"))
	        .collect(Collectors.joining(System.getProperty("line.separator")));
	}


	private static HashMap<String, Method> getDataset(Project project) throws IOException {
		HashMap<String, Method> dataset = new HashMap<String, Method>();
		getCodeMetrics(dataset, project);
		String strHistory=readAll("C:\\Users\\login\\work\\cassandra\\historyWithBugOneline.json");
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, HistoryFile> history = mapper.readValue(strHistory, new TypeReference<HashMap<String, HistoryFile>>() {});
		int count=0;
		int allFiles=dataset.size();
		int dateFrom=1378219089;
		int dateUntil=1446838714;
		for(String key: dataset.keySet()) {
			count++;
			System.out.println(count+"/"+allFiles);
			ArrayList<String> sources=new ArrayList<String>();
			Set<String> authors = new HashSet<String>();
			int NOSources=0;
			HistoryFile tmp = history.get(key);
			if(tmp==null) {
				System.out.println(key);
				continue;
			}
			for(int i=tmp.commits.size()-1;0<=i;i--) {
				Commit commit=tmp.commits.get(i);
				if(StringUtils.equals(commit.sourceOld,commit.sourceNew)) {
					continue;
				}
				if(dateFrom<commit.date & commit.date<dateUntil){
					sources.add(0, commit.sourceNew);
					authors.add(commit.author);
				}
			}
			while(!tmp.pathOld.equals("/dev/null")) {
				tmp=history.get(tmp.pathOld);
				for(int i=tmp.commits.size()-1;0<=i;i--) {
					Commit commit=tmp.commits.get(i);
					if(StringUtils.equals(commit.sourceOld, commit.sourceNew)) {
						continue;
					}
					if(dateFrom<commit.date & commit.date<dateUntil) {
						sources.add(0, commit.sourceNew);
						authors.add(commit.author);
					}
				}
			}
			tmp = history.get(key);

			getProcessMetrics(dataset.get(key),sources.toArray(new String[sources.size()]));
			dataset.get(key).authors=authors.size();
		}


		//	    for(Entry<String, Method> method : dataset.entrySet()) {
		//			long start = System.currentTimeMillis();
		//	    	getProcessMetrics(method.getValue(), project);
		//			long end = System.currentTimeMillis();
		//			System.out.println((end - start)  + "ms");
		//	    }
		return dataset;
	}

	private static void getCodeMetrics(HashMap<String, Method> dataset, Project project) {
		final String[] sourcePathDirs = {};
		final String[] libs = getLibraries(project.pathFile);
		final String[] sources = getSources(project.pathFile); //{"C:\\Users\\login\\work\\ant\\src\\main\\org\\apache\\tools\\zip\\ZipShort.java"};
		ArrayList<Method> methods;



		ASTParser parser = ASTParser.newParser(AST.JLS11);
		final Map<String,String> options = JavaCore.getOptions();
		//caution. to calculateIDMethod http://www.nextdesign.co.jp/tips/tips_eclipse_jdt.html
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		parser.setCompilerOptions(options);
		parser.setResolveBindings(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setEnvironment(libs, sourcePathDirs, null, true);


		String[] keys = new String[] {""};
		Requestor requestor = new Requestor();
		parser.createASTs(sources, null, keys, requestor, new NullProgressMonitor());
		methods=requestor.methods;


		for(String id: requestor.methodsCalled) {
			for(Method test: methods) {
				if(test.id.equals(id)) {
					test.fanIN++;
				}
			}
		}
		for(Method method: methods) {
			dataset.put(method.path, method);
		}
	}


	private static String[] getLibraries(String pathFile) {
		Path[] dirs = new Path[]{
				Paths.get("C:\\Users\\login\\work\\pleiades\\eclipse\\plugins"),
				Paths.get(pathFile)
		};
		List<String> classes=new ArrayList<String>();
		try {
			for(int i=0;i<dirs.length;i++) {
				classes.addAll(Files.walk(dirs[i])
						.map(Path::toString)
						.filter(p -> p.endsWith(".jar"))
						.collect(Collectors.toList()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classes.toArray(new String[classes.size()]);
	}


	private static String[] getSources(String pathFile) {
		Path rootDir = Paths.get(pathFile+"/src");
		String[] sources = null;
		try {
			List<String> test =Files.walk(rootDir)
					.map(Path::toString)
					.filter(p -> p.endsWith(".java"))
					//.filter(p -> !p.contains("test"))
					.collect(Collectors.toList());
			sources=test.toArray(new String[test.size()]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sources;
	}

	private static void getProcessMetrics(Method method, String[] sources) throws IOException {
		if(sources.length==0) {
			return;
		}
		List<String> statements = Arrays.asList("AssertStatement","Block","BreakStatement","ConstructorInvocation","ContinueStatement","DoStatement","EmptyStatement","EnhancedForStatement", "ExpressionStatement","ForStatement","IfStatement","LabeledStatement","ReturnStatement","SuperConstructorInvocation","SwitchCase","SwitchStatement","SynchronizedStatement","ThrowStatement","TryStatement","TypeDeclarationStatement","VariableDeclarationStatement","WhileStatement");
		List<String> operatorsCondition = Arrays.asList("<", ">", "<=", ">=", "==", "!=", "^", "&", "|", "&&", "||");

		JdtTreeGenerator jdtTreeGenerator = new JdtTreeGenerator();
		String sourcePrev =  "public class Test{}";
		ITree iTreePrev = jdtTreeGenerator.generateFrom().string(sourcePrev).getRoot();
		String sourceCurrent =null;
		ITree iTreeCurrent=null;
        int methodHistories=sources.length;
    	int[] stmtAdded= new int[methodHistories];
        Arrays.fill(stmtAdded, 0);
    	int[] stmtDeleted= new int[methodHistories];
        Arrays.fill(stmtDeleted, 0);
    	int[] churn= new int[methodHistories];
        Arrays.fill(churn, 0);
    	int[] decl= new int[methodHistories];
        Arrays.fill(decl, 0);
    	int[] cond= new int[methodHistories];
        Arrays.fill(cond, 0);
    	int[] elseAdded= new int[methodHistories];
        Arrays.fill(elseAdded, 0);
    	int[] elseDeleted= new int[methodHistories];
        Arrays.fill(elseDeleted, 0);

		for(int i=0;i<sources.length;i++) {
			sourceCurrent="public class Test {"+sources[i]+"}";
			iTreePrev = jdtTreeGenerator.generateFrom().string(sourcePrev).getRoot();
			iTreeCurrent = jdtTreeGenerator.generateFrom().string(sourceCurrent).getRoot();
			Matcher defaultMatcher = Matchers.getInstance().getMatcher();
			MappingStore mappings = defaultMatcher.match(iTreePrev, iTreeCurrent);
			EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
			EditScript actions = editScriptGenerator.computeActions(mappings);
			List<Action> listAction = new ArrayList<>();
			actions.iterator().forEachRemaining(listAction::add);
			listAction.sort((a, b)->a.getNode().getPos()-b.getNode().getPos());
			int[] rangeInserted=new int[2];
			int[] rangeDeleted=new int[2];
			for(Action action: listAction) {
				//System.out.println(action);
				switch(action.getName()){
				case "insert-node":{
					if(rangeInserted[0]<=action.getNode().getPos() & action.getNode().getEndPos()<=rangeInserted[1]) {
						break;
					}
					//if(statements.contains(action.getNode().getType().toString())) {
					//	stmtAdded[i]++;
					//}
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Insert) action).getNode());
					while(childs.hasNext()) {
						String target=childs.next().getType().toString();
						if(statements.contains(target)) {
							stmtAdded[i]++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							decl[i]++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										cond[i]++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							elseAdded[i]++;
						}
					}
					rangeInserted[0]=action.getNode().getPos();
					rangeInserted[1]=action.getNode().getEndPos();
					break;
				}
				case "insert-tree":{
					if(rangeInserted[0]<=action.getNode().getPos()&action.getNode().getEndPos()<=rangeInserted[1]) {
						break;
					}
					//if(statements.contains(action.getNode().getType().toString())) {
					//	stmtAdded[i]++;
					//}
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeInsert) action).getNode());
					while(childs.hasNext()) {
						String target=childs.next().getType().toString();
						if(statements.contains(target)) {
							stmtAdded[i]++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							decl[i]++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										cond[i]++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							elseAdded[i]++;
						}
					}
					rangeInserted[0]=action.getNode().getPos();
					rangeInserted[1]=action.getNode().getEndPos();
					break;
				}
				case "delete-node":{
					if(rangeDeleted[0]<=action.getNode().getPos()&action.getNode().getEndPos()<=rangeDeleted[1]) {
						break;
					}
					//if(statements.contains(action.getNode().getType().toString())) {
					//	stmtDeleted[i]++;
					//}
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Delete) action).getNode());
					while(childs.hasNext()) {
						String target=childs.next().getType().toString();
						if(statements.contains(target)) {
							stmtDeleted[i]++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString()=="MethodDeclaration") {
							decl[i]++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										cond[i]++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							elseDeleted[i]++;
						}
					}
					rangeDeleted[0]=action.getNode().getPos();
					rangeDeleted[1]=action.getNode().getEndPos();
					break;
				}
				case "delete-tree":{
					if(rangeDeleted[0]<=action.getNode().getPos()&action.getNode().getEndPos()<=rangeDeleted[1]) {
						break;
					}
					//if(statements.contains(action.getNode().getType().toString())) {
					//	stmtDeleted[i]++;
					//}
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeDelete) action).getNode());
					while(childs.hasNext()) {
						String target=childs.next().getType().toString();
						if(statements.contains(target)) {
							stmtDeleted[i]++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							decl[i]++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										cond[i]++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							elseDeleted[i]++;
						}
					}
					rangeDeleted[0]=action.getNode().getPos();
					rangeDeleted[1]=action.getNode().getEndPos();
					break;
				}
				case "move-tree":{
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							decl[i]++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							decl[i]++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										cond[i]++;
									}
								}
							}
						}
					}
					break;
				}
				case "update-node":{
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							decl[i]++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										cond[i]++;
									}
								}
							}
						}
					}
					break;
				}
				default:{
					break;
				}
				}
			}

			sourcePrev=sourceCurrent;
			churn[i]=stmtAdded[i]-stmtDeleted[i];
		}
		method.methodHistories=methodHistories;
		method.stmtAdded=Arrays.stream(stmtAdded).sum();
		method.maxStmtAdded=Arrays.stream(stmtAdded).max().getAsInt();
		method.avgStmtAdded=method.stmtAdded/(float)methodHistories;
		method.stmtDeleted=Arrays.stream(stmtDeleted).sum();
		method.maxStmtDeleted=Arrays.stream(stmtDeleted).max().getAsInt();
		method.avgStmtDeleted=method.stmtDeleted/(float)methodHistories;
		method.churn=Arrays.stream(churn).sum();
		method.maxChurn=Arrays.stream(churn).max().getAsInt();
		method.avgChurn=method.churn/(float)methodHistories;
		method.decl=Arrays.stream(decl).sum();
		method.cond=Arrays.stream(cond).sum();
		method.elseAdded=Arrays.stream(elseAdded).sum();
		method.elseDeleted=Arrays.stream(elseDeleted).sum();
		//System.out.println("tset");
	}
	/*
	private static void getProcessMetrics_(Method method, Project project) throws IOException {
		List<String> statements = Arrays.asList("AssertStatement","Block","BreakStatement","ConstructorInvocation","ContinueStatement","DoStatement","EmptyStatement","EnhancedForStatement", "ExpressionStatement","ForStatement","IfStatement","LabeledStatement","ReturnStatement","SuperConstructorInvocation","SwitchCase","SwitchStatement","SynchronizedStatement","ThrowStatement","TryStatement","TypeDeclarationStatement","VariableDeclarationStatement","WhileStatement");
		List<String> operatorsCondition = Arrays.asList("<", ">", "<=", ">=", "==", "!=", "^", "&", "|", "&&", "||");
		getHistory(method.history, project.pathMethod, method.path.replace("\\", "/"));

		String sourcePrev =  "public class Test{public int test(){if(2>1){}else {}} }";
		String sourceCurrent =null;

		for(Commit commit: method.history.commits) {
			sourceCurrent="public class Test {" + "public int test(){if(2>1){} }"; //+commit.snapshot+"}";
			JdtTreeGenerator jdtTreeGenerator = new JdtTreeGenerator();
			ITree iTreePrev = jdtTreeGenerator.generateFrom().string(sourcePrev).getRoot();
			ITree iTreeCurrent = jdtTreeGenerator.generateFrom().string(sourceCurrent).getRoot();
			Matcher defaultMatcher = Matchers.getInstance().getMatcher();
			MappingStore mappings = defaultMatcher.match(iTreePrev, iTreeCurrent);
			EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
			EditScript actions = editScriptGenerator.computeActions(mappings);
			//System.out.println(commit.snapshot);
			for(Action action: actions) {
				//System.out.println(action);
				switch(action.getName()){
				case "insert-node":{
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Insert) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							commit.stmtAdded++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							commit.decl++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										commit.cond++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							commit.elseAdded++;
						}
					}
					break;
				}
				case "insert-tree":{
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeInsert) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							commit.stmtAdded++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							commit.decl++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										commit.cond++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							commit.elseAdded++;
						}
					}
					break;
				}
				case "delete-node":{
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Delete) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							commit.stmtDeleted++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString()=="MethodDeclaration") {
							commit.decl++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										commit.cond++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							commit.elseDeleted++;
						}
					}
					break;
				}
				case "delete-tree":{
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeDelete) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							commit.stmtDeleted++;
						}
					}
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							commit.decl++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										commit.cond++;
									}
								}
							}
						}
					}
					if(action.getNode().getType().toString().equals("Block")) {
						if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
							commit.elseDeleted++;
						}
					}
					break;
				}
				case "move-tree":{
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							commit.decl++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							commit.decl++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										commit.cond++;
									}
								}
							}
						}
					}
					break;
				}
				case "update-node":{
					List<ITree> parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("Block")) {
							break;
						}else if(parent.getType().toString().equals("MethodDeclaration")) {
							commit.decl++;
						}
					}
					parents=action.getNode().getParents();
					for(ITree parent : parents) {
						if(parent.getType().toString().equals("InfixExpression")) {
							List<ITree> childsInfixExpression=parent.getChildren();
							for(ITree child : childsInfixExpression) {
								if(child.hasLabel()) {
									if(operatorsCondition.contains(child.getLabel().toString())) {
										commit.cond++;
									}
								}
							}
						}
					}
					break;
				}
				default:{
					break;
				}
				}
			}
			sourcePrev="public class Test {"+commit.snapshot+"}";
		}
		method.methodHistories=method.history.snapshots.size();
		method.authors=(int) method.history.commits.stream().map(e->e.author).count();
		method.stmtAdded=method.history.commits.stream().mapToInt(e->e.stmtAdded).sum();
		method.maxStmtAdded=method.history.commits.stream().mapToInt(e->e.stmtAdded).max().getAsInt();
		method.avgStmtAdded=method.history.commits.stream().mapToInt(e->e.stmtAdded).average().getAsDouble();
		method.stmtDeleted=method.history.commits.stream().mapToInt(e->e.stmtDeleted).max().getAsInt();
		method.maxStmtDeleted=method.history.commits.stream().mapToInt(e->e.stmtDeleted).max().getAsInt();
		method.avgStmtDeleted=method.history.commits.stream().mapToInt(e->e.stmtDeleted).average().getAsDouble();
		method.churn=method.history.commits.stream().mapToInt(e->e.churn).sum();
		method.maxChurn=method.history.commits.stream().mapToInt(e->e.churn).max().getAsInt();
		method.avgChurn=method.history.commits.stream().mapToInt(e->e.churn).average().getAsDouble();
		//System.out.println("test");
	}
	*/
	/*
	private static void getHistory(History history, String pathRepository, String pathFile) {
		pathFile=pathFile.replaceAll(pathRepository+"/", "");
		try {
			Repository repository = new FileRepositoryBuilder().setGitDir(new File(pathRepository+"/.git")).build();
			final Git git = new Git(repository);

			ObjectId head = repository.resolve("HEAD");
			//List<RevCommit> commitsAll = StreamSupport.stream(git.log().add(head).call().spliterator(), false).collect(Collectors.toList());
			List<RevCommit> commitsAll = StreamSupport.stream(git.log().call().spliterator(), false).collect(Collectors.toList());
			List<RevCommit> commitsFile = StreamSupport.stream(git.log().addPath(pathFile).call().spliterator(), false).collect(Collectors.toList());
			List<RevCommit[]> commitsSet = new ArrayList<RevCommit[]>();
			for(RevCommit commitFile: commitsFile) {
				int index=commitsAll.indexOf(commitFile);
				commitsSet.add(new RevCommit[]{commitsAll.get(index+1), commitsAll.get(index)});
			}

			DiffFormatter diffFormatter = new DiffFormatter(System.out);
			diffFormatter.setRepository(repository);

			for (RevCommit[] commitSet : commitsSet) {
				List<DiffEntry> list = diffFormatter.scan(commitSet[0].getTree(), commitSet[1].getTree());
				for (DiffEntry diffEntry : list) {
					if (diffEntry.getNewPath().equals(pathFile)) {
						Commit commit=new Commit();
						if (diffEntry.getChangeType() != DiffEntry.ChangeType.DELETE) {
							commit.id=commitSet[1].toObjectId().getName();
							commit.author=commitSet[0].getAuthorIdent().getName();
							String str;
							ObjectLoader loader = repository.open(diffEntry.getNewId().toObjectId());
							str = new String(loader.getBytes());
							commit.snapshot=str;
							//System.out.println(str);
						}
						if (diffEntry.getChangeType() == DiffEntry.ChangeType.RENAME) {
							pathFile = diffEntry.getOldPath();
						}
						history.commits.add(commit);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
	*/


}