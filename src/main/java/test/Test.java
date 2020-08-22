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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	private static final String idCommitLatest="81f7d184765b4b66d1483305014427da78ff487c";
	private static final int NOReleases=3;
	private static int idRelease;
	private static final String pathProject = "C:/Users/login/work/cassandra";
	private static final String pathRepositoryMethod = pathProject+"/method";
	private static String pathRepositoryFile;
	private static final String pathInfoBug = pathProject+"/infoBug.json";
	private static final String pathRevision2Date = pathProject + "/revision2Date.json";
	private static final String pathRelease2Date = pathProject + "/release2Date.json";
	private static final String pathHistoriesFile = pathProject + "/history.json";
	private static final String pathPlugins="C:\\Users\\login\\work\\pleiades\\eclipse\\plugins";
	private static String pathDataset;
	private static HashMap<String, HistoryFile> historiesFile;
	private static HashMap<String, Method> dataset;

	public static void main(String[] args) {
		for(idRelease=3;idRelease<=NOReleases;idRelease++) {
			pathRepositoryFile = pathProject+"/file"+idRelease;
			pathDataset = pathProject+"/"+idRelease+".csv";
            calculateDataset();
		}
	}


	private static void calculateDataset() {
		try {
			dataset = getDataset();
			File csv = new File(pathDataset);
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
	}


	private static HashMap<String, Method> getDataset() throws IOException {
		HashMap<String, Method> dataset = new HashMap<String, Method>();
		getCodeMetrics(dataset);
		getProcessMetrics(dataset);
		//getIsBuggy(dataset);
		return dataset;
	}


    private static void getIsBuggy(HashMap<String, Method> dataset) {
		String strHistory=readAll(pathHistoriesFile);
		String strRelease2Date=readAll(pathRelease2Date);
		String strRevision2Date=readAll(pathRevision2Date);
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, HistoryFile> history = null;
		HashMap<String, Integer> release2Date=null;
		HashMap<String, Integer> revision2Date=null;
		try {
			history = mapper.readValue(strHistory, new TypeReference<HashMap<String, HistoryFile>>() {});
			release2Date=mapper.readValue(strRelease2Date, new TypeReference<HashMap<String, Integer>>() {});
			revision2Date=mapper.readValue(strRevision2Date, new TypeReference<HashMap<String, Integer>>() {});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count=0;
		int allFiles=dataset.size();
		int dateRelease=release2Date.get(Integer.toString(idRelease));

		HistoryFile tmp=null;
		List<Commit> commits=new ArrayList<Commit>();
		for(String key: dataset.keySet()) {
			count++;
			System.out.println(count+"/"+allFiles);
			tmp=history.get(key);
			if(tmp==null)continue;
			int i=0;
			do{
				if(tmp.commits.get(i).date  < release2Date.get(Integer.toString(idRelease-1))
						| release2Date.get(Integer.toString(idRelease))<tmp.commits.get(i).date
						| commits.get(0).date < tmp.commits.get(i).date) {
					i++;
				}
				else {
			        if(tmp.commits.get(i).type==4 & tmp.commits.get(i).pathNew!=null) {
		    		    tmp=history.get(tmp.commits.get(i).pathNew);
    		    		i=0;
		    	    }
		    	    commits.add(tmp.commits.get(i));
		    	    i++;
				}
		    }while(i<tmp.commits.size());
			for(Commit commit: commits) {
				if(commit.bugFix!=null) {
				for(String idRevision: commit.bugFix) {
					if(revision2Date.get(idRevision) <release2Date.get(Integer.toString(idRelease))
							& release2Date.get(Integer.toString(idRelease))<commit.date) {
						dataset.get(key).isBuggy=1;
					}
				}
				}
			}
		}
    }


	private static void getCodeMetrics(HashMap<String, Method> dataset) {
		final String[] sourcePathDirs = {};
		final String[] libs = getLibraries(pathRepositoryFile);
		final String[] sources = getSources(pathRepositoryFile);

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
				Paths.get(pathPlugins),
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


	private static void getProcessMetrics(HashMap<String, Method> dataset) {
		String strHistory=readAll(pathHistoriesFile);
		String strRelease2Date=readAll(pathRelease2Date);
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, HistoryFile> history = null;
		HashMap<String, Integer> release2Date=null;
		try {
			history = mapper.readValue(strHistory, new TypeReference<HashMap<String, HistoryFile>>() {});
			release2Date=mapper.readValue(strRelease2Date, new TypeReference<HashMap<String, Integer>>() {});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count=0;
		int allFiles=dataset.size();
		int dateFrom=release2Date.get(Integer.toString(idRelease-1));
		int dateUntil=release2Date.get(Integer.toString(idRelease));


		HistoryFile tmp=null;
		List<Commit> commits=null;
		for(String key: dataset.keySet()) {
			commits=new ArrayList<Commit>();
			//if(!key.equals("src/java/org/apache/cassandra/db/commitlog/CommitLog.LogRecordAdder#run().mjava"))continue;
			count++;
			System.out.println(count+"/"+allFiles);
			System.out.println(key);
			tmp=history.get(key);
			if(tmp==null)continue;
			tmp.commits.sort((a, b)->a.date-b.date);
			int i=tmp.commits.size()-1;
			do{
				if(tmp.commits.get(i).date  < release2Date.get(Integer.toString(idRelease-1))
					| release2Date.get(Integer.toString(idRelease))<tmp.commits.get(i).date
					| (0<commits.size() && commits.get(0).date < tmp.commits.get(i).date)) {
					i--;
				}else {
					if(tmp.commits.get(i).type==0) {
						commits.add(0,tmp.commits.get(i));
				        commits.get(0).isMerge=false;
						break;
					}
			        if(tmp.commits.get(i).type==1) {
				        commits.add(0,tmp.commits.get(i));
    		    		tmp=history.get(tmp.commits.get(i).pathOld);
    					tmp.commits.sort((a, b)->a.date-b.date);
		    		    i=tmp.commits.size()-1;
		    	    }else {
			            commits.add(0, tmp.commits.get(i));
		    	        i--;
		    	    }
				}
		    }while(0<=i);
			getA(dataset.get(key), commits);
		}
	}


	private static void getA(Method method, List<Commit> commits){
		Set<String> authors = new HashSet<String>();

		try {
			int methodHistories=0;
			ArrayList<Integer> stmtAddeds = new ArrayList<Integer>();
			ArrayList<Integer> stmtDeleteds = new ArrayList<Integer>();
			ArrayList<Integer> churns = new ArrayList<Integer>();
			ArrayList<Integer> decls = new ArrayList<Integer>();
			ArrayList<Integer> conds = new ArrayList<Integer>();
			ArrayList<Integer> elseAddeds = new ArrayList<Integer>();
			ArrayList<Integer> elseDeleteds = new ArrayList<Integer>();

			List<String> statements = Arrays.asList("AssertStatement","BreakStatement","ConstructorInvocation","ContinueStatement","DoStatement","EnhancedForStatement", "ExpressionStatement","ForStatement","IfStatement","ReturnStatement","SuperConstructorInvocation","SwitchStatement","ThrowStatement","TryStatement","WhileStatement");
			List<String> operatorsCondition = Arrays.asList("<", ">", "<=", ">=", "==", "!=", "^", "&", "|", "&&", "||");

			JdtTreeGenerator jdtTreeGenerator = new JdtTreeGenerator();
			String sourcePrev =  null;
			String sourceCurrent =null;
			ITree iTreePrev = null;
			ITree iTreeCurrent=null;

			for(int i=0;i<commits.size();i++) {
				if(commits.get(i).type==4|commits.get(i).isMerge)continue;
				sourcePrev= "public class Test{"+commits.get(i).sourceOld+"}";
				sourceCurrent ="public class Test{"+commits.get(i).sourceNew+"}";
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
				if(listAction.size()==0)continue;
				authors.add(commits.get(i).author);
				int stmtAdded=0;
				int stmtDeleted=0;
				int churn=0;
				int decl=0;
				int cond=0;
				int elseAdded=0;
				int elseDeleted=0;
				for(Action action: listAction) {
					switch(action.getName()){
					case "insert-node":{
						if(rangeInserted[0]<=action.getNode().getPos() & action.getNode().getEndPos()<=rangeInserted[1]) {
							break;
						}
						Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Insert) action).getNode());
						while(childs.hasNext()) {
							ITree child=childs.next();
							if(statements.contains(child.getType().toString())) {
								stmtAdded++;
							}
							if(child.getType().toString().equals("IfStatement")) {
								if(child.getChildren().size()==3) {
								    elseAdded++;
								}
							    if(child.getParent().getType().toString().equals("IfStatement")) {
							        elseAdded++;
						        }
							}
						}
						List<ITree> parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("Block")) {
								break;
							}else if(parent.getType().toString().equals("MethodDeclaration")) {
								decl++;
							}
						}
						parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("InfixExpression")) {
								List<ITree> childsInfixExpression=parent.getChildren();
								for(ITree child : childsInfixExpression) {
									if(child.hasLabel()) {
										if(operatorsCondition.contains(child.getLabel().toString())) {
											cond++;
										}
									}
								}
							}
						}
						if(action.getNode().getType().toString().equals("Block")) {
							if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
								elseAdded++;
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
						Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeInsert) action).getNode());
						while(childs.hasNext()) {
							ITree child=childs.next();
							if(statements.contains(child.getType().toString())) {
								stmtAdded++;
							}
							if(child.getType().toString().equals("IfStatement")) {
								if(child.getChildren().size()==3) {
								    elseAdded++;
								}
							    if(child.getParent().getType().toString().equals("IfStatement")) {
							        elseAdded++;
						        }
							}
						}
						List<ITree> parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("Block")) {
								break;
							}else if(parent.getType().toString().equals("MethodDeclaration")) {
								decl++;
							}
						}
						parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("InfixExpression")) {
								List<ITree> childsInfixExpression=parent.getChildren();
								for(ITree child : childsInfixExpression) {
									if(child.hasLabel()) {
										if(operatorsCondition.contains(child.getLabel().toString())) {
											cond++;
										}
									}
								}
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
						Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Delete) action).getNode());
						while(childs.hasNext()) {
							ITree child=childs.next();
							if(statements.contains(child.getType().toString())) {
								stmtDeleted++;
							}
							if(child.getType().toString().equals("IfStatement")) {
								if(child.getChildren().size()==3) {
								    elseDeleted++;
								}
							    if(child.getParent().getType().toString().equals("IfStatement")) {
							        elseDeleted++;
						        }
							}
						}
						List<ITree> parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("Block")) {
								break;
							}else if(parent.getType().toString()=="MethodDeclaration") {
								decl++;
							}
						}
						parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("InfixExpression")) {
								List<ITree> childsInfixExpression=parent.getChildren();
								for(ITree child : childsInfixExpression) {
									if(child.hasLabel()) {
										if(operatorsCondition.contains(child.getLabel().toString())) {
											cond++;
										}
									}
								}
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
						Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeDelete) action).getNode());
						while(childs.hasNext()) {
							ITree child=childs.next();
							if(statements.contains(child.getType().toString())) {
								stmtDeleted++;
							}
							if(child.getType().toString().equals("IfStatement")) {
								if(child.getChildren().size()==3) {
								    elseDeleted++;
								}
							    if(child.getParent().getType().toString().equals("IfStatement")) {
							        elseDeleted++;
						        }
							}
						}
						List<ITree> parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("Block")) {
								break;
							}else if(parent.getType().toString().equals("MethodDeclaration")) {
								decl++;
							}
						}
						parents=action.getNode().getParents();
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("InfixExpression")) {
								List<ITree> childsInfixExpression=parent.getChildren();
								for(ITree child : childsInfixExpression) {
									if(child.hasLabel()) {
										if(operatorsCondition.contains(child.getLabel().toString())) {
											cond++;
										}
									}
								}
							}
						}
						if(action.getNode().getType().toString().equals("Block")) {
							if(action.getNode().getParent().getType().toString().equals("IfStatement")) {
								elseDeleted++;
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
								decl++;
							}
						}
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("InfixExpression")) {
								List<ITree> childsInfixExpression=parent.getChildren();
								for(ITree child : childsInfixExpression) {
									if(child.hasLabel()) {
										if(operatorsCondition.contains(child.getLabel().toString())) {
											cond++;
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
								decl++;
							}
						}
						for(ITree parent : parents) {
							if(parent.getType().toString().equals("InfixExpression")) {
								List<ITree> childsInfixExpression=parent.getChildren();
								for(ITree child : childsInfixExpression) {
									if(child.hasLabel()) {
										if(operatorsCondition.contains(child.getLabel().toString())) {
											cond++;
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
				churn=stmtAdded-stmtDeleted;
				methodHistories++;
				stmtAddeds.add(stmtAdded);
				stmtDeleteds.add(stmtDeleted);
				churns.add(churn);
				decls.add(decl);
				conds.add(cond);
				elseAddeds.add(elseAdded);
				elseDeleteds.add(elseDeleted);
			}
			if(methodHistories==0)return;
			method.methodHistories=methodHistories;
			method.authors=authors.size();
			method.stmtAdded=stmtAddeds.stream().mapToInt(e->e).sum();
			method.maxStmtAdded=stmtAddeds.stream().mapToInt(e->e).max().getAsInt();
			method.avgStmtAdded=method.stmtAdded/(float)methodHistories;
			method.stmtDeleted=stmtDeleteds.stream().mapToInt(e->e).sum();
			method.maxStmtDeleted=stmtDeleteds.stream().mapToInt(e->e).max().getAsInt();
			method.avgStmtDeleted=method.stmtDeleted/(float)methodHistories;
			method.churn=churns.stream().mapToInt(e->e).sum();
			method.maxChurn=churns.stream().mapToInt(e->e).max().getAsInt();
			method.avgChurn=method.churn/(float)methodHistories;
			method.decl=decls.stream().mapToInt(e->e).sum();
			method.cond=conds.stream().mapToInt(e->e).sum();
			method.elseAdded=elseAddeds.stream().mapToInt(e->e).sum();
			method.elseDeleted=elseDeleteds.stream().mapToInt(e->e).sum();
			//System.out.println("tset");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static String readAll(final String path){
		String value=null;
	    try {
	    	value = Files.lines(Paths.get(path), Charset.forName("UTF-8")).collect(Collectors.joining(System.getProperty("line.separator")));
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			return value;
		}
	}
}