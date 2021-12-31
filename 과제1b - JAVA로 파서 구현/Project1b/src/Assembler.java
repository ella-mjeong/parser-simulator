import java.util.ArrayList;
import java.io.*;


/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. 
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. 
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. 
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) 
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) 
 * 
 * 
 * �ۼ����� ���ǻ��� : 
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)
 * 
 *     
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� literal table�� �����ϴ� ����*/
	ArrayList<LiteralTable> literaltabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����.   
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;
	
	static int locctr;
	static int sectionNum;
	
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * ������� ���� ��ƾ
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");	
		assembler.pass1();

		assembler.printSymbolTable("symtab_20160433.txt");
		assembler.printLiteralTable("literaltab_20160433.txt");
		assembler.pass2();
		assembler.printObjectCode("output_20160433.txt");
		
	}

	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.
	 * @param inputFile : input ���� �̸�.
	 */
	private void loadInputFile(String inputFile) {
		try {
			// ���� ��ü ����
			File file = new File(inputFile);
			// �Է� ��Ʈ�� ����
			FileReader fileReader = new FileReader(file);
			// �Է� ���� ����
			BufferedReader bufReader = new BufferedReader(fileReader);			
			String line = "";

			while ((line = bufReader.readLine()) != null) { //������ ������ �б� ������ ���پ� ����
				lineList.add(line); // �о���� ���ε��� lineList�� ����
			}
			bufReader.close(); // �Է¹��۸� ����
		} catch (FileNotFoundException e) {
			System.out.println("not found");
		} catch (IOException e){
			System.out.println(e);
		}
	}

	/** 
	 * pass1 ������ �����Ѵ�.
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����
	 *   2) label�� symbolTable�� ����
	 *   
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		String line;
		Token tmpToken;
		for(int i = 0; i < lineList.size(); i++ ) {
			line = lineList.get(i);
			if(line.contains("START")) {//���α׷��� ���۵� �� tokentable, symtab, literaltab ����
				locctr = 0;
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(sectionNum), literaltabList.get(sectionNum), instTable));
			}
			else if(line.contains("CSECT")) {//sub ���α׷��� ���۵� �� tokentable, symtab, literaltab ����
				sectionNum++;
				locctr = 0;
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(sectionNum), literaltabList.get(sectionNum), instTable));	
				
			}
			TokenList.get(sectionNum).putToken(line);
		}
		
		//��ū �и��� �͵��� ������ loc�� ������ ��Ÿ �ʿ��� ������ ����
		for(int i = 0; i<sectionNum + 1; i++) {
			for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				tmpToken = TokenList.get(i).getToken(j);
				if(tmpToken.type.equals("DIRECTIVE")) {
					if(tmpToken.directive.equals("START")) {//���α׷��� ���۵� �� 
						locctr = Integer.parseInt(tmpToken.operand[0]);
						TokenList.get(i).getToken(j).setlocation(locctr);
						symtabList.get(i).putSymbol(tmpToken.label, locctr);
						continue;
					}
					else if(tmpToken.directive.equals("CSECT")) {//sub ���α׷��� ���۵� ��
						TokenList.get(i-1).lastAddr = locctr;
						locctr = 0;
						TokenList.get(i).getToken(j).setlocation(locctr);
						symtabList.get(i).putSymbol(tmpToken.label, locctr);
						continue;
					}
					else if(tmpToken.directive.contentEquals("EXTDEF")) { //EXTDEF �ڿ� ������ ���� ����
						for(int k = 0; k<tmpToken.operand.length; k++) {
							symtabList.get(i).setdefSymbol(tmpToken.operand[k]);
						}
					}
					else if(tmpToken.directive.contentEquals("EXTREF")) { //EXTREF �ڿ� ������ ���� ����
						for(int k = 0; k<tmpToken.operand.length; k++) {
							symtabList.get(i).setrefSymbol(tmpToken.operand[k]);
						}
					}
				}
				if(tmpToken.label != null) {
					symtabList.get(i).putSymbol(tmpToken.label, locctr);
				}
				
				TokenList.get(i).getToken(j).setlocation(locctr);
				
				if(tmpToken.type.equals("INSTRUCTION")) {
					if(tmpToken.plus_check == 1) { //��ɾ��� ���Ŀ� ���� locctr�� ����
						locctr += 4;
					}
					else {
						locctr += tmpToken.instTable.getFormat(tmpToken.operator);
					}
					if(tmpToken.operand[0] != null) {
						if(tmpToken.operand[0].contains("=")) { //indirection addressing�� ��
							literaltabList.get(i).putLiteral(tmpToken.operand[0], -2);
						}
					}
				}
				else if(tmpToken.type.equals("DIRECTIVE")) {
					if(tmpToken.directive.equals("WORD")) { //locctr�� 3 ������Ŵ
						locctr += 3;
					}
					else if(tmpToken.directive.equals("RESW")) { //locctr�� (3 * �ǿ����ڰ�)��ŭ ������Ŵ
						locctr += 3 * Integer.parseInt(tmpToken.operand[0]);
					}
					else if(tmpToken.directive.equals("RESB")) { //locctr�� �ǿ����ڰ���ŭ ������Ŵ
						locctr += Integer.parseInt(tmpToken.operand[0]);
					}
					else if(tmpToken.directive.equals("BYTE")) { //locctr�� 1 ������Ŵ
						locctr += 1;
					}
					else if(tmpToken.directive.equals("EQU")) {
						if(tmpToken.operand[0].equals("*")) { // �ǿ����ڰ� *�� ��� 
							symtabList.get(i).modifySymbol(tmpToken.label, locctr);  //���� locctr�� ���� �ּҷ� ����
						}
						else { // �׷��� ���� ��� �ǿ����ڸ� ���� �ּҰ��� ����Ͽ� ����
							if(tmpToken.operand[0].contains("-")) {
								tmpToken.operand = tmpToken.operand[0].split("-");
								String str1 = new String(tmpToken.operand[0]);
								String str2 = new String(tmpToken.operand[1]);
								symtabList.get(i).modifySymbol(tmpToken.label, symtabList.get(i).search(str1) - symtabList.get(i).search(str2));
								TokenList.get(i).getToken(j).setlocation(symtabList.get(i).search(str1) - symtabList.get(i).search(str2));
								
							}
						}
					}
					else if(tmpToken.directive.equals("LTORG")) { //���α׷��� ���Դ� literal�� ����
						locctr = literaltabList.get(i).addrLiteralTab(locctr);
					}
					else if(tmpToken.directive.equals("END")) { //���α׷��� ���Դ� literal�� ����
						locctr = literaltabList.get(i).addrLiteralTab(locctr);
						TokenList.get(i).lastAddr = locctr;
					}
				}
			}
		}
		
		return;
	}
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printSymbolTable(String fileName) {
		try {
			// ���� ��ü ����
			File file = new File(fileName);
			// ��� ��Ʈ�� ����
			FileWriter fileWriter = new FileWriter(file);
			// ��� ���� ����
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for(int i = 0; i< symtabList.size(); i++) {
				for(int j = 0; j<symtabList.get(i).getSize(); j++) {
					bufferedWriter.write(String.format("%s\t\t\t%X", symtabList.get(i).getSymbol(j),symtabList.get(i).getaddress(j)));
					bufferedWriter.newLine();
				}
				bufferedWriter.newLine();
			}
			bufferedWriter.close(); // ��¹��۸� ����
		}catch(IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * �ۼ��� LiteralTable���� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printLiteralTable(String fileName) {
		try {
			// ���� ��ü ����
			File file = new File(fileName);
			// ��� ��Ʈ�� ����
			FileWriter fileWriter = new FileWriter(file);
			// ��� ���� ����
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			String str;
			for(int i = 0; i< literaltabList.size(); i++) {
				for(int j = 0; j<literaltabList.get(i).getSize(); j++) {
					str = literaltabList.get(i).getLiteral(j);
					str = str.substring(2,str.length()-1);
					bufferedWriter.write(String.format("%s\t\t\t%X", str,literaltabList.get(i).getaddress(j)));
				}
				if(literaltabList.get(i).getSize() == 0) {
					continue;
				}
				bufferedWriter.newLine();
			}
			bufferedWriter.close(); // ��¹��۸� ����
		}catch(IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * pass2 ������ �����Ѵ�.
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		int num = 0;
		String textTmp = null;
		String code = null;
		//��ū�и��� �͵��� ������ �� ���κ� object code �ۼ�
		for(int i = 0; i<TokenList.size(); i++) {
			for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				TokenList.get(i).makeObjectCode(j);
			}
		}
		
		Token tmpToken;
		String str;
		int check = 1;
		int line_max = 0;
		int line_check = 0;
		int line = 0;
		//���α׷��� object code�� �ۼ��Ͽ� codeList�� ����
		for(int i = 0; i<TokenList.size(); i++) {
			int notFinish = -1;
			for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				tmpToken = TokenList.get(i).getToken(j);
				//Header record �ۼ�
				if(tmpToken.type.equals("DIRECTIVE")) {
					if(tmpToken.directive.equals("START") || tmpToken.directive.equals("CSECT")) {
						int startAddr = TokenList.get(i).getToken(0).location;
						String name = new String(TokenList.get(i).getToken(0).label);
						int lastAddr = TokenList.get(i).lastAddr;
						codeList.add(String.format("H%-6s%06X%06X",name,startAddr,lastAddr));
						continue;
					}
					
					if(tmpToken.directive.equals("EXTDEF")){ //Define record �ۼ�
						str = new String("D");
						for(int k = 0; k < symtabList.get(i).defList.size(); k++) {
							str += String.format("%-6s%06X", symtabList.get(i).defList.get(k), symtabList.get(i).search(symtabList.get(i).defList.get(k)));
						}
						codeList.add(str);
						continue;
					}
					
					if(tmpToken.directive.equals("EXTREF")){ //Refer record �ۼ�
						str = new String("R");
						for(int k = 0; k < symtabList.get(i).refList.size(); k++) {
							str += String.format("%-6s", symtabList.get(i).refList.get(k));
						}
						codeList.add(str);
						continue;
					}
				}
				
				//Text record �ۼ�
				if(tmpToken.objectCode != null) {
					line_check = 0;
					if(check == 1) {
						textTmp = String.format("T%06X",TokenList.get(i).getToken(0).location+line);
						check = 0;
						notFinish = 2;
					}
					num += tmpToken.byteSize;
					if(num > 30) { //��������� byte�� ������ ���ٿ� ���� �ִ� �з����� ���ٸ�
						num -= tmpToken.byteSize;
						check = 1;
						textTmp = String.format("%s%02X%s", textTmp, num, code);
						codeList.add(textTmp);
						line += num;
						textTmp = null;
						code = null;
						line_max = 1;
						num = 0;
						j--;
						continue;
					}
					else {
						if(notFinish == 1) {
							textTmp = String.format("T%06X",TokenList.get(i).getToken(j).location);
						}
						if(line_max == 1) {
							textTmp = String.format("T%06X",TokenList.get(i).getToken(0).location+line);
							line_max = 0;
						}
						if(code != null) {
							code += String.format("%s",TokenList.get(i).getToken(j).objectCode);
						}
						else {
							code = String.format("%s",TokenList.get(i).getToken(j).objectCode);
						}
						line_check = 1;
					}
					continue;
				}
				else {
					if(line_check == 1) {
						if(notFinish == 2) {
							if(textTmp == null) {
								continue;
							}
							notFinish = 1;
							textTmp = String.format("%s%02X%s", textTmp,num,code);
							codeList.add(textTmp);
							line += num;
							textTmp = null;
							code = null;
							num = 0;
						}
					}
				}
			}
			if(line_check == 1) { 
				textTmp = String.format("%s%02X%s", textTmp,num,code);
				codeList.add(textTmp);
				textTmp = null;
				code = null;
				num = 0;
				line = 0;
				check = 1;
			}
			
			if (symtabList.get(i).refList.size() != 0) { //Modification record �ۼ�
				for (int k = 0; k < symtabList.get(i).modList.size(); k++) {
					codeList.add(String.format("M%06X%02X%s",symtabList.get(i).modLocationList.get(k),symtabList.get(i).modSize.get(k),symtabList.get(i).modList.get(k)));
				}
			}
			
			if (i == 0) { //End record �ۼ�
				codeList.add(String.format("E%06X", TokenList.get(i).getToken(0).location));
			}
			else {
				codeList.add("E");
			}
		}
		
		
	}
	
	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.
	 * @param fileName : ����Ǵ� ���� �̸�
	 */
	private void printObjectCode(String fileName) {
		try {
			// ���� ��ü ����
			File file = new File(fileName);
			// ��� ��Ʈ�� ����
			FileWriter fileWriter = new FileWriter(file);
			// ��� ���� ����
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			
			for(int i = 0; i< codeList.size(); i++) {
				bufferedWriter.write(String.format("%s", codeList.get(i)));
				bufferedWriter.newLine();
				
				if(codeList.get(i).charAt(0) == 'E') {
					bufferedWriter.newLine();
				}
			}
			bufferedWriter.close(); // ��¹��۸� ����
		}catch(IOException e) {
			System.out.println(e);
		}
	}
	
}
