package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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

		HashMap<String, Method> dataset=getDataset(new Project("test", "C:/Users/login/work/cassandra_method"));

		//long end = System.currentTimeMillis();
		//System.out.println((end - start)  + "ms");
	}

	private static HashMap<String, Method> getDataset(Project project) throws IOException {
		HashMap<String, Method> dataset = new HashMap<String, Method>();
	    getCodeMetrics(dataset, project);

	    for(Entry<String, Method> method : dataset.entrySet()) {
			long start = System.currentTimeMillis();
	    	getProcessMetrics(method.getValue(), project);
			long end = System.currentTimeMillis();
			System.out.println((end - start)  + "ms");
	    }
	    return dataset;
	}

	private static void getCodeMetrics(HashMap<String, Method> dataset, Project project) {
		final String[] sourcePathDirs = {};
		final String[] libs = getLibraries();
		final String[] sources = getSources(); //{"C:\\Users\\login\\work\\ant\\src\\main\\org\\apache\\tools\\zip\\ZipShort.java"};
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


	private static String[] getLibraries() {
		Path[] dirs = new Path[]{
				Paths.get("C:\\Users\\login\\work\\pleiades\\eclipse\\plugins"),
				Paths.get("C:\\Users\\login\\work\\cassandra_file")
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


	private static String[] getSources() {
		Path rootDir = Paths.get("C:\\Users\\login\\work\\cassandra_file\\src");
		String[] sources = null;
		try {
			List<String> test =Files.walk(rootDir)
				.map(Path::toString)
				.filter(p -> p.endsWith(".java"))
				.filter(p -> !p.contains("test"))
		        .collect(Collectors.toList());
			sources=test.toArray(new String[test.size()]);
        } catch (IOException e) {
			e.printStackTrace();
		}
        return sources;
	}

	private static void getProcessMetrics(Method method, Project project) throws IOException {
		List<String> statements = Arrays.asList("AssertStatement","Block","BreakStatement","ConstructorInvocation","ContinueStatement","DoStatement","EmptyStatement","EnhancedForStatement", "ExpressionStatement","ForStatement","IfStatement","LabeledStatement","ReturnStatement","SuperConstructorInvocation","SwitchCase","SwitchStatement","SynchronizedStatement","ThrowStatement","TryStatement","TypeDeclarationStatement","VariableDeclarationStatement","WhileStatement");
		List<String> operatorsCondition = Arrays.asList("<", ">", "<=", ">=", "==", "!=", "^", "&", "|", "&&", "||");
		getHistory(method.history, project.path, method.path.replace("\\", "/"));

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


}