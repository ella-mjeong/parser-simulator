import java.util.ArrayList;
import java.io.*;


/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	static int locctr;
	static int sectionNum;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
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
	 * 어셈블러의 메인 루틴
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
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		try {
			// 파일 객체 생성
			File file = new File(inputFile);
			// 입력 스트림 생성
			FileReader fileReader = new FileReader(file);
			// 입력 버퍼 생성
			BufferedReader bufReader = new BufferedReader(fileReader);			
			String line = "";

			while ((line = bufReader.readLine()) != null) { //파일을 끝까지 읽기 전까지 한줄씩 읽음
				lineList.add(line); // 읽어들인 라인들은 lineList에 저장
			}
			bufReader.close(); // 입력버퍼를 닫음
		} catch (FileNotFoundException e) {
			System.out.println("not found");
		} catch (IOException e){
			System.out.println(e);
		}
	}

	/** 
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// TODO Auto-generated method stub
		String line;
		Token tmpToken;
		for(int i = 0; i < lineList.size(); i++ ) {
			line = lineList.get(i);
			if(line.contains("START")) {//프로그램이 시작될 때 tokentable, symtab, literaltab 생성
				locctr = 0;
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(sectionNum), literaltabList.get(sectionNum), instTable));
			}
			else if(line.contains("CSECT")) {//sub 프로그램이 시작될 때 tokentable, symtab, literaltab 생성
				sectionNum++;
				locctr = 0;
				symtabList.add(new SymbolTable());
				literaltabList.add(new LiteralTable());
				TokenList.add(new TokenTable(symtabList.get(sectionNum), literaltabList.get(sectionNum), instTable));	
				
			}
			TokenList.get(sectionNum).putToken(line);
		}
		
		//토큰 분리한 것들을 가지고 loc를 포함한 기타 필요한 정보들 저장
		for(int i = 0; i<sectionNum + 1; i++) {
			for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				tmpToken = TokenList.get(i).getToken(j);
				if(tmpToken.type.equals("DIRECTIVE")) {
					if(tmpToken.directive.equals("START")) {//프로그램이 시작될 때 
						locctr = Integer.parseInt(tmpToken.operand[0]);
						TokenList.get(i).getToken(j).setlocation(locctr);
						symtabList.get(i).putSymbol(tmpToken.label, locctr);
						continue;
					}
					else if(tmpToken.directive.equals("CSECT")) {//sub 프로그램이 시작될 때
						TokenList.get(i-1).lastAddr = locctr;
						locctr = 0;
						TokenList.get(i).getToken(j).setlocation(locctr);
						symtabList.get(i).putSymbol(tmpToken.label, locctr);
						continue;
					}
					else if(tmpToken.directive.contentEquals("EXTDEF")) { //EXTDEF 뒤에 나오는 값들 저장
						for(int k = 0; k<tmpToken.operand.length; k++) {
							symtabList.get(i).setdefSymbol(tmpToken.operand[k]);
						}
					}
					else if(tmpToken.directive.contentEquals("EXTREF")) { //EXTREF 뒤에 나오는 값들 저장
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
					if(tmpToken.plus_check == 1) { //명령어의 형식에 맞춰 locctr값 증가
						locctr += 4;
					}
					else {
						locctr += tmpToken.instTable.getFormat(tmpToken.operator);
					}
					if(tmpToken.operand[0] != null) {
						if(tmpToken.operand[0].contains("=")) { //indirection addressing일 때
							literaltabList.get(i).putLiteral(tmpToken.operand[0], -2);
						}
					}
				}
				else if(tmpToken.type.equals("DIRECTIVE")) {
					if(tmpToken.directive.equals("WORD")) { //locctr을 3 증가시킴
						locctr += 3;
					}
					else if(tmpToken.directive.equals("RESW")) { //locctr을 (3 * 피연산자값)만큼 증가시킴
						locctr += 3 * Integer.parseInt(tmpToken.operand[0]);
					}
					else if(tmpToken.directive.equals("RESB")) { //locctr을 피연산자값만큼 증가시킴
						locctr += Integer.parseInt(tmpToken.operand[0]);
					}
					else if(tmpToken.directive.equals("BYTE")) { //locctr을 1 증가시킴
						locctr += 1;
					}
					else if(tmpToken.directive.equals("EQU")) {
						if(tmpToken.operand[0].equals("*")) { // 피연산자가 *인 경우 
							symtabList.get(i).modifySymbol(tmpToken.label, locctr);  //현재 locctr의 값을 주소로 저장
						}
						else { // 그렇지 않은 경우 피연산자를 통해 주소값을 계산하여 저장
							if(tmpToken.operand[0].contains("-")) {
								tmpToken.operand = tmpToken.operand[0].split("-");
								String str1 = new String(tmpToken.operand[0]);
								String str2 = new String(tmpToken.operand[1]);
								symtabList.get(i).modifySymbol(tmpToken.label, symtabList.get(i).search(str1) - symtabList.get(i).search(str2));
								TokenList.get(i).getToken(j).setlocation(symtabList.get(i).search(str1) - symtabList.get(i).search(str2));
								
							}
						}
					}
					else if(tmpToken.directive.equals("LTORG")) { //프로그램에 나왔던 literal을 저장
						locctr = literaltabList.get(i).addrLiteralTab(locctr);
					}
					else if(tmpToken.directive.equals("END")) { //프로그램에 나왔던 literal을 저장
						locctr = literaltabList.get(i).addrLiteralTab(locctr);
						TokenList.get(i).lastAddr = locctr;
					}
				}
			}
		}
		
		return;
	}
	
	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		try {
			// 파일 객체 생성
			File file = new File(fileName);
			// 출력 스트림 생성
			FileWriter fileWriter = new FileWriter(file);
			// 출력 버퍼 생성
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for(int i = 0; i< symtabList.size(); i++) {
				for(int j = 0; j<symtabList.get(i).getSize(); j++) {
					bufferedWriter.write(String.format("%s\t\t\t%X", symtabList.get(i).getSymbol(j),symtabList.get(i).getaddress(j)));
					bufferedWriter.newLine();
				}
				bufferedWriter.newLine();
			}
			bufferedWriter.close(); // 출력버퍼를 닫음
		}catch(IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		try {
			// 파일 객체 생성
			File file = new File(fileName);
			// 출력 스트림 생성
			FileWriter fileWriter = new FileWriter(file);
			// 출력 버퍼 생성
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
			bufferedWriter.close(); // 출력버퍼를 닫음
		}catch(IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		int num = 0;
		String textTmp = null;
		String code = null;
		//토큰분리한 것들을 가지고 각 라인별 object code 작성
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
		//프로그램의 object code를 작성하여 codeList에 저장
		for(int i = 0; i<TokenList.size(); i++) {
			int notFinish = -1;
			for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
				tmpToken = TokenList.get(i).getToken(j);
				//Header record 작성
				if(tmpToken.type.equals("DIRECTIVE")) {
					if(tmpToken.directive.equals("START") || tmpToken.directive.equals("CSECT")) {
						int startAddr = TokenList.get(i).getToken(0).location;
						String name = new String(TokenList.get(i).getToken(0).label);
						int lastAddr = TokenList.get(i).lastAddr;
						codeList.add(String.format("H%-6s%06X%06X",name,startAddr,lastAddr));
						continue;
					}
					
					if(tmpToken.directive.equals("EXTDEF")){ //Define record 작성
						str = new String("D");
						for(int k = 0; k < symtabList.get(i).defList.size(); k++) {
							str += String.format("%-6s%06X", symtabList.get(i).defList.get(k), symtabList.get(i).search(symtabList.get(i).defList.get(k)));
						}
						codeList.add(str);
						continue;
					}
					
					if(tmpToken.directive.equals("EXTREF")){ //Refer record 작성
						str = new String("R");
						for(int k = 0; k < symtabList.get(i).refList.size(); k++) {
							str += String.format("%-6s", symtabList.get(i).refList.get(k));
						}
						codeList.add(str);
						continue;
					}
				}
				
				//Text record 작성
				if(tmpToken.objectCode != null) {
					line_check = 0;
					if(check == 1) {
						textTmp = String.format("T%06X",TokenList.get(i).getToken(0).location+line);
						check = 0;
						notFinish = 2;
					}
					num += tmpToken.byteSize;
					if(num > 30) { //현재까지의 byte의 개수가 한줄에 쓸수 있는 분량보다 많다면
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
			
			if (symtabList.get(i).refList.size() != 0) { //Modification record 작성
				for (int k = 0; k < symtabList.get(i).modList.size(); k++) {
					codeList.add(String.format("M%06X%02X%s",symtabList.get(i).modLocationList.get(k),symtabList.get(i).modSize.get(k),symtabList.get(i).modList.get(k)));
				}
			}
			
			if (i == 0) { //End record 작성
				codeList.add(String.format("E%06X", TokenList.get(i).getToken(0).location));
			}
			else {
				codeList.add("E");
			}
		}
		
		
	}
	
	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		try {
			// 파일 객체 생성
			File file = new File(fileName);
			// 출력 스트림 생성
			FileWriter fileWriter = new FileWriter(file);
			// 출력 버퍼 생성
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			
			for(int i = 0; i< codeList.size(); i++) {
				bufferedWriter.write(String.format("%s", codeList.get(i)));
				bufferedWriter.newLine();
				
				if(codeList.get(i).charAt(0) == 'E') {
					bufferedWriter.newLine();
				}
			}
			bufferedWriter.close(); // 출력버퍼를 닫음
		}catch(IOException e) {
			System.out.println(e);
		}
	}
	
}
