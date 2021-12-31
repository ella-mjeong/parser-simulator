import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	public int lastAddr; //각 프로그램이 끝날 때마다 마지막 주소값을 저장
	
	/**
	 * 초기화하면서 symTable과 literalTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param literalTab : 해당 section과 연결되어있는 literal table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab) {
		tokenList = new ArrayList<>();
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line,instTab));
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index : object 코드를 생성하고자하는 토큰 인스턴스의 인덱스
	 */
	public void makeObjectCode(int index){
		String register[] = {"A","X","L","B","S","T","F","","PC","SW"};
		Token tmpToken = tokenList.get(index);
		
		if(tmpToken.type.equals("DIRECTIVE")) {
			if(tmpToken.directive.equals("LTORG")) { //프로그램에 나왔던 literal값들을 메모리에 저장
				for(int i = 0; i<literalTab.getSize(); i++) {
					if(literalTab.getLiteral(i).charAt(0) == 'X') {//literal이 'X'로 시작하면 literal값을 그대로 메모리에 할당
						String str = new String(literalTab.getLiteral(i));
						str = str.substring(2,str.length()-1);
						tokenList.get(index).setObjectCode(String.format("%s", str));
						tokenList.get(index).setByteSize(1);
					}
					else {  //literal이 'C'로 시작하면 각 char 값의 ASCII code를 메모리에 할당
						String str = new String(literalTab.getLiteral(i));
						str = str.substring(2,str.length()-1);
						String tmpStr = "";
						for(int j = 0; j < str.length(); j++) {
							tmpStr += String.format("%02X", (int)str.charAt(j));
						}
						tokenList.get(index).setObjectCode(tmpStr);
						tokenList.get(index).setByteSize(str.length());
					}
				}
			}
			else if(tmpToken.directive.equals("END")) { //프로그램에 나왔던 literal값들을 메모리에 저장
				for(int i = 0; i<literalTab.getSize(); i++) {
					if(literalTab.getLiteral(i).charAt(0) == 'X') {//literal이 'X'로 시작하면 literal값을 그대로 메모리에 할당
						String str = new String(literalTab.getLiteral(i));
						str = str.substring(2,str.length()-1);
						tokenList.get(index).setObjectCode(String.format("%s", str));
						tokenList.get(index).setByteSize(1);
					}
					else {  //literal이 'C'로 시작하면 각 char 값의 ASCII code를 메모리에 할당
						String str = new String(literalTab.getLiteral(i));
						str = str.substring(2,str.length()-1);
						String tmpStr = "";
						for(int j = 0; j < str.length(); j++) {
							tmpStr += String.format("%02X", (int)str.charAt(j));
						}
						tokenList.get(index).setObjectCode(tmpStr);
						tokenList.get(index).setByteSize(str.length());
					}
				}
			}
			else if(tmpToken.directive.equals("BYTE")) {
				String str = new String(tmpToken.operand[0]);
				str = str.substring(2,str.length()-1);
				if(tmpToken.operand[0].charAt(0) == 'X' || tmpToken.operand[0].charAt(0) == 'x') { 
					tokenList.get(index).setObjectCode(String.format("%s", str));
					tokenList.get(index).setByteSize(1);
				}
				else if(tmpToken.operand[0].charAt(0) == 'C' || tmpToken.operand[0].charAt(0) == 'c') { 
					String tmpStr = "";
					for(int j = 0; j < str.length(); j++) {
						tmpStr += String.format("%02X", (int)str.charAt(j));
					}
					tokenList.get(index).setObjectCode(tmpStr);
					tokenList.get(index).setByteSize(str.length());
				}
			}
			else if(tmpToken.directive.equals("WORD")) {
				if(Character.isDigit(tmpToken.operand[0].charAt(0))) { // 숫자라면
					tokenList.get(index).setObjectCode(String.format("%X", Integer.parseInt(tmpToken.operand[0])));
					tokenList.get(index).setByteSize(1);
				}
				else {// 문자일때의 처리
					tmpToken.operand = tmpToken.operand[0].split("-");
					String str1 = new String(tmpToken.operand[0]);
					String str2 = new String(tmpToken.operand[1]);
					if(symTab.searchrefSymbol(str1) == 1) {
						symTab.modSymbol(String.format("+%s",str1), tmpToken.location,6);
					}
					if(symTab.searchrefSymbol(str2) == 1) {
						symTab.modSymbol(String.format("-%s",str2), tmpToken.location,6);
					}
					tokenList.get(index).setObjectCode(String.format("%06X", 0));
					tokenList.get(index).setByteSize(3);
				}
			}
		}
		else if(tmpToken.type.equals("INSTRUCTION")) {
			int op = 252;
			int opcode = op & tmpToken.instTable.getOpcode(tmpToken.operator);
			if(tmpToken.instTable.getFormat(tmpToken.operator) == 3) {
				opcode += tmpToken.getFlag(nFlag | iFlag)/16;
			}
			
			if(tmpToken.plus_check == 1) { //명령어가 4형식일 때
				tokenList.get(index).setObjectCode(String.format("%02X%01X%05X", opcode,tmpToken.getFlag(15),0));
				tokenList.get(index).setByteSize(4);
				if(symTab.searchrefSymbol(tmpToken.operand[0]) == 1){
					symTab.modSymbol(String.format("+%s",tmpToken.operand[0]), tmpToken.location + 1,5);
				}
				return;
			}
			if(tmpToken.instTable.getFormat(tmpToken.operator) == 1) { //1형식일 때
				tokenList.get(index).setObjectCode(String.format("%02X", opcode));
				tokenList.get(index).setByteSize(1);
			}
			else if(tmpToken.instTable.getFormat(tmpToken.operator) == 2) { //2형식일 때
				if(tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 1) { //피연산자의 개수가 1개일 때
					int reg_num = 0;
					for(int i = 0; i< 10; i++) {
						if(tmpToken.operand[0].equals(register[i])) {
							reg_num = i;
							break;
						}
					}
					tokenList.get(index).setObjectCode(String.format("%02X%01X%01X", opcode,reg_num,0));
					tokenList.get(index).setByteSize(2);
				}
				else if(tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 2) { //피연산자의 개수가 2개일 때
					int reg_num1 = 0, reg_num2 = 0;
					for(int i = 0; i<10; i++) {
						if(tmpToken.operand[0].equals(register[i])) {
							reg_num1 = i;
						}
						if(tmpToken.operand[1].equals(register[i])) {
							reg_num2 = i;
						}
					}
					tokenList.get(index).setObjectCode(String.format("%02X%01X%01X", opcode,reg_num1,reg_num2));
					tokenList.get(index).setByteSize(2);
				}
			}
			else if(tmpToken.instTable.getFormat(tmpToken.operator) == 3) { //3형식일 때
				if(tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 0) {  //피연산자가 존재하지 않을 때
					tokenList.get(index).setObjectCode(String.format("%02X%04X", opcode,0));
					tokenList.get(index).setByteSize(3);
					return;
				}
				else {//피연산자가 존재할 때
					if(tmpToken.operand[0].contains("#")) {//immediate addressing일 때
						tmpToken.operand[0] = tmpToken.operand[0].substring(1);
						tokenList.get(index).setObjectCode(String.format("%02X%04X", opcode,Integer.parseInt(tmpToken.operand[0])));
						tokenList.get(index).setByteSize(3);
						return;
					}
					if(tmpToken.operand[0].contains("@")) {//indirection addressing일 때
						tmpToken.operand[0] = tmpToken.operand[0].substring(1);
					}
					int target;
					if(tmpToken.operand[0].contains("=")) { //피연산자가 literal일 때 
						tmpToken.operand[0] = tmpToken.operand[0].substring(1);
						target = literalTab.search(tmpToken.operand[0]);
					}
					else {
						target = symTab.search(tmpToken.operand[0]);
					}
					int pc = tokenList.get(index+1).location;
					int addr = target - pc;
					tokenList.get(index).setObjectCode(String.format("%02X%01X%03X", opcode,tmpToken.getFlag(15),addr&0xFFF));
					tokenList.get(index).setByteSize(3);
				}
			}
		}
		return;
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;
	int plus_check; //operand에 +가 포함되어 있는지에 대한 정보를 저장
	String directive; // 명령어 라인 중 지시어
	String type; // 해당 명령어 라인이 어떤 타입인지 명시
	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	InstTable instTable;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line, InstTable instTable) {
		//토큰을 파싱하기 위해 instruction table 링크
		this.instTable = instTable;
		//initialize 추가
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		String tmpStr;
		String tok;
		String tmpInst;
		int numofOperand;
		nixbpe = '0';
		
		tmpStr = new String(line);
		operand = new String[3];
		
		if(tmpStr.charAt(0) == '.') {
			comment = new String(tmpStr);
			type = new String("COMMENT");
			return;
		}
		
		StringTokenizer tokens = new StringTokenizer(tmpStr);
		if(tokens.hasMoreTokens() == false) { //분리할 문자열이 없으면 리턴
			return;
		}
		
		tok = tokens.nextToken(" \t");
		
		if(tok.charAt(0) == '+') { //기계어에 +가 추가되어 있으면
			plus_check = 1; // 토큰테이블에 따로 저장
			tmpInst = new String(tok.substring(1));
			tok = tmpInst;
		}
		if(instTable.isInstruction(tok) == 1) { //잘린 문자열이 기계어라면
			operator = tok;
			if(instTable.getFormat(tok) == 3) {
				if(plus_check == 1) {
					setFlag(TokenTable.eFlag,1); //4형식명령어이므로 e를 표시
				}
				else {
					setFlag(TokenTable.pFlag,1); //기계어에 +가 추가되어 있지 않다면 pc relative를 사용할 것이므로 p를 표시
				}
			}
			if((numofOperand = instTable.getNumberOfOperand(tok)) != 0) { //operand가 있다면
				if(numofOperand == 1) { //operand 개수가 1개일 때
					tok = tokens.nextToken();
					operand[0] = new String(tok);
					if(tok.contains(",")) { //BUFFER,x 같이 배열을 쓰는 경우에 토큰분리
						operand = tok.split(",");
						if(operand[1].contains("X")) {
							setFlag(TokenTable.xFlag,1);
						}
					}
				}
				else { // operand 개수가 2개일 때
					tok = tokens.nextToken();
					operand[0] = new String(tok);
					operand = tok.split(",");
				}
				
				//addressing 방식이
				if(operand[0].charAt(0) == '@') {//indirection addressing일 때
					setFlag(TokenTable.nFlag,1);
					setFlag(TokenTable.iFlag,0);
				}
				else if(operand[0].charAt(0) == '#') {////immediate addressing일 때
					setFlag(TokenTable.nFlag,0);
					setFlag(TokenTable.iFlag,1);
					setFlag(TokenTable.pFlag,0);
				}
				else {// 둘다 아니라면
					if(instTable.getFormat(tok) == 3) {//그런데 기계어의 형식이 3형식/4형식이라면
						setFlag(TokenTable.nFlag,1);
						setFlag(TokenTable.iFlag,1);
					}
				}
			}
			else {//operand가 없는 3형식 명령어라면
				if(instTable.getFormat(tok) == 3) {
					setFlag(TokenTable.nFlag,1);
					setFlag(TokenTable.iFlag,1);
				}
			}
			if(tokens.hasMoreTokens() == true) { //comment 분리
				tok = tokens.nextToken("\0");
				comment = tok;
			}
			type = new String("INSTRUCTION");	
		}
		else if((numofOperand = instTable.isDirective(tok)) >= 0) { //잘린 문자열이 지시어라면
			directive = new String(tok);
			if(numofOperand == 1) {//지시어 뒤에 다른 정보가 포함되어 있을 때
				tok = tokens.nextToken();
				operand[0] = new String(tok);
				if(tok.contains(",")) {
					operand = tok.split(",");
				}
			}
			if(tokens.hasMoreTokens() == true) { //comment 분리
				tok = tokens.nextToken("\0");
				comment = tok;
			}
			type = new String("DIRECTIVE");
		}
		else {//잘린 문자열이 label이라면
			label = new String(tok);
			tok = tokens.nextToken();
			if(tok.charAt(0) == '+') { //기계어에 +가 추가되어 있으면
				plus_check = 1; // 토큰테이블에 따로 저장
				tmpInst = new String(tok.substring(1));
				tok = tmpInst;
			}
			if(instTable.isInstruction(tok) == 1) { //잘린 문자열이 기계어라면
				operator = tok;
				if(instTable.getFormat(tok) == 3) {
					if(plus_check == 1) {
						setFlag(TokenTable.eFlag,1); //4형식명령어이므로 e를 표시
					}
					else {
						setFlag(TokenTable.pFlag,1); //기계어에 +가 추가되어 있지 않다면 pc relative를 사용할 것이므로 p를 표시
					}
				}
				
				if((numofOperand = instTable.getNumberOfOperand(tok)) != 0) { //operand가 있다면
					if(numofOperand == 1) { //operand 개수가 1개일 때
						tok = tokens.nextToken();
						operand[0] = new String(tok);
						if(tok.contains(",")) { //BUFFER,x 같이 배열을 쓰는 경우에 토큰분리
							operand = tok.split(",");
							if(operand[1].contains("X")) {
								setFlag(TokenTable.xFlag,1);
							}
						}
					}
					else { // operand 개수가 2개일 때
						tok = tokens.nextToken();
						operand[0] = new String(tok);
						operand = tok.split(",");
					}
					
					//addressing 방식이
					if(operand[0].charAt(0) == '@') {//indirection addressing일 때
						setFlag(TokenTable.nFlag,1);
						setFlag(TokenTable.iFlag,0);
					}
					else if(operand[0].charAt(0) == '#') {////immediate addressing일 때
						setFlag(TokenTable.nFlag,0);
						setFlag(TokenTable.iFlag,1);
						setFlag(TokenTable.pFlag,0);
					}
					else {// 둘다 아니라면
						if(instTable.getFormat(tok) == 3) {//그런데 기계어의 형식이 3형식/4형식이라면
							setFlag(TokenTable.nFlag,1);
							setFlag(TokenTable.iFlag,1);
						}
					}
				}
				else {//operand가 없는 3형식 명령어라면
					if(instTable.getFormat(tok) == 3) {
						setFlag(TokenTable.nFlag,1);
						setFlag(TokenTable.iFlag,1);
					}
				}
				if(tokens.hasMoreTokens() == true) { //comment 분리
					tok = tokens.nextToken("\0");
					comment = tok;
				}
				type = new String("INSTRUCTION");
			}
			else if((numofOperand = instTable.isDirective(tok)) >= 0) { //잘린 문자열이 지시어라면
				directive = new String(tok);
				if(numofOperand == 1) {//지시어 뒤에 다른 정보가 포함되어 있을 때
					tok = tokens.nextToken();
					
					operand[0] = new String(tok);
					if(tok.contains(",")) {
						operand = tok.split(",");
					}
				}
				if(tokens.hasMoreTokens() == true) { //comment 분리
					tok = tokens.nextToken("\0");
					comment = tok;
				}
				type = new String("DIRECTIVE");
			}
		}
		return;
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if(value == 1) {
			nixbpe |= flag;
		}
		else {
			nixbpe ^= flag;
		}	
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	
	/**
	 * loc값을 인자로 받아와 저장한다.
	 * 
	 * @param loc : 저장하고자하는 location
	 * 
	 */
	public void setlocation(int loc) {
		location = loc;
	}
	
	/**
	 * object code을 인자로 받아와 저장한다.
	 * 
	 * @param objcode : 저장하고자하는 object code
	 * 
	 */
	public void setObjectCode(String objcode) {
		objectCode = new String(objcode);
	}
	
	/**
	 * byteSize을 인자로 받아와 저장한다.
	 * 
	 * @param bsize : 저장하고자하는 byte code
	 * 
	 */
	public void setByteSize(int bSize) {
		byteSize = bSize;
	}
	
}
