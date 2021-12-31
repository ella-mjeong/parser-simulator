import java.util.HashMap;
import java.io.*;
import java.util.StringTokenizer;

/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {
		try {
			// 파일 객체 생성
			File file = new File(fileName);
			// 입력 스트림 생성
			FileReader fileReader = new FileReader(file);
			// 입력 버퍼 생성
			BufferedReader bufReader = new BufferedReader(fileReader);

			String line = "";
			String inst; 
			StringTokenizer tokens;
		
			while((line = bufReader.readLine()) != null){ //파일을 끝까지 읽기 전까지 한줄씩 읽음
				tokens = new StringTokenizer(line);// 읽어 들인 라인을 공백 기준으로 한 번 분리하여 instruction을 저장
				inst = tokens.nextToken(" \t");
				instMap.put(inst, new Instruction(line)); //instruction 이름을 key로 저장
			}
			bufReader.close(); // 입력버퍼를 닫음
		}catch(FileNotFoundException e) {
			System.out.println("not found");
		}catch(IOException e) {
			System.out.println(e);
		}
	}
	
	//get, set, search 등의 함수는 자유 구현
	/**
	 * 인자로 받은 명령어의 opcode를 찾아 리턴함
	 * @param instruction: opcode를 알고싶은 명령어
	 * @return: 해당 명령어의 opcode
	**/
	public int getOpcode(String instruction) {
		if(instMap.containsKey(instruction)) //주어진 명령어가 key값으로 존재한다면 해당하는 객체의 opcode를 찾아서 리턴
			return instMap.get(instruction).opcode;
		else //주어진 명령어가 key값으로 존재하지 않을 때 -1 리턴
			return -1;
	}

	/**
	 * 인자로 받은 명령어의 피연산자 개수를 찾아 리턴함
	 * @param instruction: 피연산자 개수를 알고싶은 명령어
	 * @return: 해당 명령어의 피연산자 개수
	 */
	public int getNumberOfOperand(String instruction) {
		if(instMap.containsKey(instruction)) //주어진 명령어가 key값으로 존재한다면 해당하는 객체의 피연산자개수를 찾아서 리턴
			return instMap.get(instruction).numberOfOperand;
		else  //주어진 명령어가 key값으로 존재하지 않을 때 -1 리턴
			return -1;
	}
	
	/**
	 * 인자로 받은 명령어의 형식을 찾아 리턴함
	 * @param instruction: 형식을 알고싶은 명령어
	 * @return: 해당 명령어의 형식
	 */
	public int getFormat(String instruction) {
		if(instMap.containsKey(instruction)) //주어진 명령어가 key값으로 존재한다면 해당하는 객체의 형식을 찾아서 리턴
			return instMap.get(instruction).format;
		else //주어진 명령어가 key값으로 존재하지 않을 때 -1 리턴
			return -1;
	}
	
	/**
	 * 입력문자열이 명령어인지 검사.
	 * 
	 * @param str: 검사하고싶은 문자열
	 * @return: 명령어가 맞다면 1, 아니라면 -1 리턴
	 */
	public int isInstruction(String str) {
		if(instMap.containsKey(str)) { // 입력된 문자열이 명령어라면(key값으로 존재한다면)
			return 1; 
		}
		else {//주어진 명령어가 key값으로 존재하지 않을 때 -1 리턴
			return -1;
		}
	}
	
	/**
	 * 입력문자열이 지시어인지 검사.
	 * 
	 * @param str: 검사하고싶은 문자열
	 * @return: 지시어가 맞다면 >=0, 아니라면 -1 리턴
	 */
	public int isDirective(String str) {
		String directiveTable[] = {"START","END","BYTE","WORD","RESB","RESW","CSECT","EXTDEF","EXTREF","EQU","ORG","LTORG"};
		int directive_num[] = {1,1,1,1,1,1,0,1,1,1,1,0};
		for(int i = 0; i < directiveTable.length; i++) {
			if(str.equals(directiveTable[i])) { // 입력된 문자열이 directive_table의 지시어와 일치한다면
				return directive_num[i]; // 뒤에 정보가 포함되어 있는 지시어라면 1, 뒤에 정보가 포함되어 있지 않은 지시어라면 0 리턴
			}
		}
		return -1; //입력된 문자열이 지시어가 아니라면 -1 리턴
	}
	
}
/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	/* 
	 * 각자의 inst.data 파일에 맞게 저장하는 변수를 선언한다.
	 * 
	 */
	
	String instruction; //명령어
	int opcode; //각 명령어에 해당하는 opcode
	int numberOfOperand; //각 명령어의 피연산자의 개수
	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	int format;
	
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		StringTokenizer tokens = new StringTokenizer(line);
		instruction = tokens.nextToken(" \t");
		numberOfOperand = Integer.parseInt(tokens.nextToken(" \t"));
		format = Integer.parseInt(tokens.nextToken(" \t"));
		opcode = Integer.parseInt(tokens.nextToken(" \t"), 16);
	}
	//그 외 함수 자유 구현
	
}
