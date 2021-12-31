import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	
	public int lastAddr; //�� ���α׷��� ���� ������ ������ �ּҰ��� ����
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� literalTable�� instTable�� ��ũ��Ų��.
	 * @param symTab : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param literalTab : �ش� section�� ����Ǿ��ִ� literal table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, LiteralTable literalTab, InstTable instTab) {
		tokenList = new ArrayList<>();
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
	}

	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line,instTab));
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table literal table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index : object �ڵ带 �����ϰ����ϴ� ��ū �ν��Ͻ��� �ε���
	 */
	public void makeObjectCode(int index){
		String register[] = {"A","X","L","B","S","T","F","","PC","SW"};
		Token tmpToken = tokenList.get(index);
		
		if(tmpToken.type.equals("DIRECTIVE")) {
			if(tmpToken.directive.equals("LTORG")) { //���α׷��� ���Դ� literal������ �޸𸮿� ����
				for(int i = 0; i<literalTab.getSize(); i++) {
					if(literalTab.getLiteral(i).charAt(0) == 'X') {//literal�� 'X'�� �����ϸ� literal���� �״�� �޸𸮿� �Ҵ�
						String str = new String(literalTab.getLiteral(i));
						str = str.substring(2,str.length()-1);
						tokenList.get(index).setObjectCode(String.format("%s", str));
						tokenList.get(index).setByteSize(1);
					}
					else {  //literal�� 'C'�� �����ϸ� �� char ���� ASCII code�� �޸𸮿� �Ҵ�
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
			else if(tmpToken.directive.equals("END")) { //���α׷��� ���Դ� literal������ �޸𸮿� ����
				for(int i = 0; i<literalTab.getSize(); i++) {
					if(literalTab.getLiteral(i).charAt(0) == 'X') {//literal�� 'X'�� �����ϸ� literal���� �״�� �޸𸮿� �Ҵ�
						String str = new String(literalTab.getLiteral(i));
						str = str.substring(2,str.length()-1);
						tokenList.get(index).setObjectCode(String.format("%s", str));
						tokenList.get(index).setByteSize(1);
					}
					else {  //literal�� 'C'�� �����ϸ� �� char ���� ASCII code�� �޸𸮿� �Ҵ�
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
				if(Character.isDigit(tmpToken.operand[0].charAt(0))) { // ���ڶ��
					tokenList.get(index).setObjectCode(String.format("%X", Integer.parseInt(tmpToken.operand[0])));
					tokenList.get(index).setByteSize(1);
				}
				else {// �����϶��� ó��
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
			
			if(tmpToken.plus_check == 1) { //��ɾ 4������ ��
				tokenList.get(index).setObjectCode(String.format("%02X%01X%05X", opcode,tmpToken.getFlag(15),0));
				tokenList.get(index).setByteSize(4);
				if(symTab.searchrefSymbol(tmpToken.operand[0]) == 1){
					symTab.modSymbol(String.format("+%s",tmpToken.operand[0]), tmpToken.location + 1,5);
				}
				return;
			}
			if(tmpToken.instTable.getFormat(tmpToken.operator) == 1) { //1������ ��
				tokenList.get(index).setObjectCode(String.format("%02X", opcode));
				tokenList.get(index).setByteSize(1);
			}
			else if(tmpToken.instTable.getFormat(tmpToken.operator) == 2) { //2������ ��
				if(tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 1) { //�ǿ������� ������ 1���� ��
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
				else if(tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 2) { //�ǿ������� ������ 2���� ��
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
			else if(tmpToken.instTable.getFormat(tmpToken.operator) == 3) { //3������ ��
				if(tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 0) {  //�ǿ����ڰ� �������� ���� ��
					tokenList.get(index).setObjectCode(String.format("%02X%04X", opcode,0));
					tokenList.get(index).setByteSize(3);
					return;
				}
				else {//�ǿ����ڰ� ������ ��
					if(tmpToken.operand[0].contains("#")) {//immediate addressing�� ��
						tmpToken.operand[0] = tmpToken.operand[0].substring(1);
						tokenList.get(index).setObjectCode(String.format("%02X%04X", opcode,Integer.parseInt(tmpToken.operand[0])));
						tokenList.get(index).setByteSize(3);
						return;
					}
					if(tmpToken.operand[0].contains("@")) {//indirection addressing�� ��
						tmpToken.operand[0] = tmpToken.operand[0].substring(1);
					}
					int target;
					if(tmpToken.operand[0].contains("=")) { //�ǿ����ڰ� literal�� �� 
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
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;
	int plus_check; //operand�� +�� ���ԵǾ� �ִ����� ���� ������ ����
	String directive; // ��ɾ� ���� �� ���þ�
	String type; // �ش� ��ɾ� ������ � Ÿ������ ���
	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	InstTable instTable;
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line, InstTable instTable) {
		//��ū�� �Ľ��ϱ� ���� instruction table ��ũ
		this.instTable = instTable;
		//initialize �߰�
		parsing(line);
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
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
		if(tokens.hasMoreTokens() == false) { //�и��� ���ڿ��� ������ ����
			return;
		}
		
		tok = tokens.nextToken(" \t");
		
		if(tok.charAt(0) == '+') { //��� +�� �߰��Ǿ� ������
			plus_check = 1; // ��ū���̺� ���� ����
			tmpInst = new String(tok.substring(1));
			tok = tmpInst;
		}
		if(instTable.isInstruction(tok) == 1) { //�߸� ���ڿ��� ������
			operator = tok;
			if(instTable.getFormat(tok) == 3) {
				if(plus_check == 1) {
					setFlag(TokenTable.eFlag,1); //4���ĸ�ɾ��̹Ƿ� e�� ǥ��
				}
				else {
					setFlag(TokenTable.pFlag,1); //��� +�� �߰��Ǿ� ���� �ʴٸ� pc relative�� ����� ���̹Ƿ� p�� ǥ��
				}
			}
			if((numofOperand = instTable.getNumberOfOperand(tok)) != 0) { //operand�� �ִٸ�
				if(numofOperand == 1) { //operand ������ 1���� ��
					tok = tokens.nextToken();
					operand[0] = new String(tok);
					if(tok.contains(",")) { //BUFFER,x ���� �迭�� ���� ��쿡 ��ū�и�
						operand = tok.split(",");
						if(operand[1].contains("X")) {
							setFlag(TokenTable.xFlag,1);
						}
					}
				}
				else { // operand ������ 2���� ��
					tok = tokens.nextToken();
					operand[0] = new String(tok);
					operand = tok.split(",");
				}
				
				//addressing �����
				if(operand[0].charAt(0) == '@') {//indirection addressing�� ��
					setFlag(TokenTable.nFlag,1);
					setFlag(TokenTable.iFlag,0);
				}
				else if(operand[0].charAt(0) == '#') {////immediate addressing�� ��
					setFlag(TokenTable.nFlag,0);
					setFlag(TokenTable.iFlag,1);
					setFlag(TokenTable.pFlag,0);
				}
				else {// �Ѵ� �ƴ϶��
					if(instTable.getFormat(tok) == 3) {//�׷��� ������ ������ 3����/4�����̶��
						setFlag(TokenTable.nFlag,1);
						setFlag(TokenTable.iFlag,1);
					}
				}
			}
			else {//operand�� ���� 3���� ��ɾ���
				if(instTable.getFormat(tok) == 3) {
					setFlag(TokenTable.nFlag,1);
					setFlag(TokenTable.iFlag,1);
				}
			}
			if(tokens.hasMoreTokens() == true) { //comment �и�
				tok = tokens.nextToken("\0");
				comment = tok;
			}
			type = new String("INSTRUCTION");	
		}
		else if((numofOperand = instTable.isDirective(tok)) >= 0) { //�߸� ���ڿ��� ���þ���
			directive = new String(tok);
			if(numofOperand == 1) {//���þ� �ڿ� �ٸ� ������ ���ԵǾ� ���� ��
				tok = tokens.nextToken();
				operand[0] = new String(tok);
				if(tok.contains(",")) {
					operand = tok.split(",");
				}
			}
			if(tokens.hasMoreTokens() == true) { //comment �и�
				tok = tokens.nextToken("\0");
				comment = tok;
			}
			type = new String("DIRECTIVE");
		}
		else {//�߸� ���ڿ��� label�̶��
			label = new String(tok);
			tok = tokens.nextToken();
			if(tok.charAt(0) == '+') { //��� +�� �߰��Ǿ� ������
				plus_check = 1; // ��ū���̺� ���� ����
				tmpInst = new String(tok.substring(1));
				tok = tmpInst;
			}
			if(instTable.isInstruction(tok) == 1) { //�߸� ���ڿ��� ������
				operator = tok;
				if(instTable.getFormat(tok) == 3) {
					if(plus_check == 1) {
						setFlag(TokenTable.eFlag,1); //4���ĸ�ɾ��̹Ƿ� e�� ǥ��
					}
					else {
						setFlag(TokenTable.pFlag,1); //��� +�� �߰��Ǿ� ���� �ʴٸ� pc relative�� ����� ���̹Ƿ� p�� ǥ��
					}
				}
				
				if((numofOperand = instTable.getNumberOfOperand(tok)) != 0) { //operand�� �ִٸ�
					if(numofOperand == 1) { //operand ������ 1���� ��
						tok = tokens.nextToken();
						operand[0] = new String(tok);
						if(tok.contains(",")) { //BUFFER,x ���� �迭�� ���� ��쿡 ��ū�и�
							operand = tok.split(",");
							if(operand[1].contains("X")) {
								setFlag(TokenTable.xFlag,1);
							}
						}
					}
					else { // operand ������ 2���� ��
						tok = tokens.nextToken();
						operand[0] = new String(tok);
						operand = tok.split(",");
					}
					
					//addressing �����
					if(operand[0].charAt(0) == '@') {//indirection addressing�� ��
						setFlag(TokenTable.nFlag,1);
						setFlag(TokenTable.iFlag,0);
					}
					else if(operand[0].charAt(0) == '#') {////immediate addressing�� ��
						setFlag(TokenTable.nFlag,0);
						setFlag(TokenTable.iFlag,1);
						setFlag(TokenTable.pFlag,0);
					}
					else {// �Ѵ� �ƴ϶��
						if(instTable.getFormat(tok) == 3) {//�׷��� ������ ������ 3����/4�����̶��
							setFlag(TokenTable.nFlag,1);
							setFlag(TokenTable.iFlag,1);
						}
					}
				}
				else {//operand�� ���� 3���� ��ɾ���
					if(instTable.getFormat(tok) == 3) {
						setFlag(TokenTable.nFlag,1);
						setFlag(TokenTable.iFlag,1);
					}
				}
				if(tokens.hasMoreTokens() == true) { //comment �и�
					tok = tokens.nextToken("\0");
					comment = tok;
				}
				type = new String("INSTRUCTION");
			}
			else if((numofOperand = instTable.isDirective(tok)) >= 0) { //�߸� ���ڿ��� ���þ���
				directive = new String(tok);
				if(numofOperand == 1) {//���þ� �ڿ� �ٸ� ������ ���ԵǾ� ���� ��
					tok = tokens.nextToken();
					
					operand[0] = new String(tok);
					if(tok.contains(",")) {
						operand = tok.split(",");
					}
				}
				if(tokens.hasMoreTokens() == true) { //comment �и�
					tok = tokens.nextToken("\0");
					comment = tok;
				}
				type = new String("DIRECTIVE");
			}
		}
		return;
	}
	
	/** 
	 * n,i,x,b,p,e flag�� �����Ѵ�. 
	 * 
	 * ��� �� : setFlag(nFlag, 1); 
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
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
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� 
	 * 
	 * ��� �� : getFlag(nFlag)
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	
	/**
	 * loc���� ���ڷ� �޾ƿ� �����Ѵ�.
	 * 
	 * @param loc : �����ϰ����ϴ� location
	 * 
	 */
	public void setlocation(int loc) {
		location = loc;
	}
	
	/**
	 * object code�� ���ڷ� �޾ƿ� �����Ѵ�.
	 * 
	 * @param objcode : �����ϰ����ϴ� object code
	 * 
	 */
	public void setObjectCode(String objcode) {
		objectCode = new String(objcode);
	}
	
	/**
	 * byteSize�� ���ڷ� �޾ƿ� �����Ѵ�.
	 * 
	 * @param bsize : �����ϰ����ϴ� byte code
	 * 
	 */
	public void setByteSize(int bSize) {
		byteSize = bSize;
	}
	
}
