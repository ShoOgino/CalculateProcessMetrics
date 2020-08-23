package test;

import net.sf.jsefa.csv.annotation.CsvDataType;
import net.sf.jsefa.csv.annotation.CsvField;

@CsvDataType()
public class Method {
	String id="";
    @CsvField(pos = 1)
	String path="";
	HistoryFile history=new HistoryFile();
    @CsvField(pos = 2)
    int isBuggy=0;

    @CsvField(pos = 3)
    int fanIN=0;
    @CsvField(pos = 4)
	int fanOut=0;
    @CsvField(pos = 5)
    int parameters=0;
    @CsvField(pos = 6)
    int localVar=0;
    @CsvField(pos = 7, converterType = DoubleConverter.class)
    double commentRatio=0;
    @CsvField(pos = 8)
    int countPath=0;
    @CsvField(pos = 9)
    int complexity=0;
    @CsvField(pos = 10)
    int execStmt=0;
    @CsvField(pos = 11)
    int maxNesting=0;


    @CsvField(pos = 12)
    int methodHistories=0;
    @CsvField(pos = 13)
    int authors=0;
    @CsvField(pos = 14)
    int stmtAdded=0;
    @CsvField(pos = 15)
    int maxStmtAdded=0;
    @CsvField(pos = 16, converterType = DoubleConverter.class)
    double avgStmtAdded=0;
    @CsvField(pos = 17)
    int stmtDeleted=0;
    @CsvField(pos = 18)
    int maxStmtDeleted=0;
    @CsvField(pos = 19, converterType = DoubleConverter.class)
    double avgStmtDeleted=0;
    @CsvField(pos = 20)
    int churn=0;
    @CsvField(pos = 21)
    int maxChurn=0;
    @CsvField(pos = 22, converterType = DoubleConverter.class)
    double avgChurn=0;
    @CsvField(pos = 23)
    int decl=0;
    @CsvField(pos = 24)
    int cond=0;
    @CsvField(pos = 25)
    int elseAdded=0;
    @CsvField(pos = 26)
    int elseDeleted=0;


    public Method() {
    }
    public Method(String path) {
    	this.path=path;
    }
    public Method(String id, String path) {
    	this.id=id;
    	this.path=path;
    }
}