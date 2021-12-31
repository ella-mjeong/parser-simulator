package SP20_simulator;

import java.io.File;
import java.util.*;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;
	InstLuncher inst;
	
	public static final int A_REGISTER = 0;
	public static final int X_REGISTER = 1;
	public static final int L_REGISTER = 2;
	public static final int B_REGISTER = 3;
	public static final int S_REGISTER = 4;
	public static final int T_REGISTER = 5;
	public static final int F_REGISTER = 6;
	public static final int PC_REGISTER = 8;
	public static final int SW_REGISTER = 9;
	
	private List<String> instructionsList = new ArrayList<>();
	private List<String> logList = new ArrayList<>();
	
	String currentDev = "";
	int addr = 0;
	boolean pEnd = false;
	
	public SicSimulator(ResourceManager resourceManager) {
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
		inst = new InstLuncher(rMgr);
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 */
	public void load(File program) {
		/* 메모리 초기화, 레지스터 초기화 등*/
		rMgr.initializeResource();
	}

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 */
	public void oneStep() {
		addr = rMgr.getRegister(PC_REGISTER);
		if(pEnd) {//모든 instruction이 수행을 마쳤음을 의미
			return;
		}
		char [] bytes = rMgr.getMemory(addr,2);
		int temp = bytes[0];
		int opcode = temp;
		int addressing = 0;
		boolean form_4 = false;
		
		// indirect addressing인지 immediate addressing인지 판단
		if((temp & 3) == 3) {
			opcode -= 3;
			addressing = 3;
		}
		else if((temp & 2) == 2){
			opcode -= 2;
			addressing = 2;
		}
		else if((temp & 1) == 1){
			opcode -= 1;
			addressing = 1;
		}
			
		//두번째 바이트를 이용하여 확장된 4형식 명령어인지 체크
		temp = (bytes[1] >>> 4);
		if((temp & 1) == 1) {
			form_4 = true;
		}
		
		String code = new String();	
		char [] tmpInst = new char[2];
		
		switch(opcode) {
		case 0x14://3,4
			addLog("STL");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.stl(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.stl(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
		
		case 0x48: //3,4
			addLog("JSUB");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.jsub(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.jsub(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}

			break;
			
		case 0x00://3,4
			addLog("LDA");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.lda(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.lda(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0x28://3,4
			addLog("COMP");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.comp(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.comp(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0x30://3,4
			addLog("JEQ");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.jeq(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.jeq(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0x3C://3,4
			addLog("J");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.j(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.j(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			if(rMgr.getRegister(PC_REGISTER) == 0){
				pEnd = true;
			}
			break;
			
		case 0x0C://3,4
			addLog("STA");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.sta(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.sta(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0xB4://2
			addLog("CLEAR");
			tmpInst = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 2);
			code = makingCode(tmpInst, 2);
			addInstruction(code);
			inst.clear(rMgr.getRegister(PC_REGISTER));
			break;
			
		case 0x74://3,4
			addLog("LDT");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.ldt(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.ldt(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0xE0://3,4
			addLog("TD");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				currentDev = inst.td(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				currentDev = inst.td(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0xD8://3,4
			addLog("RD");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.rd(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.rd(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0xA0://2
			addLog("COMPR");
			tmpInst = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 2);
			code = makingCode(tmpInst, 2);
			addInstruction(code);
			inst.compr(rMgr.getRegister(PC_REGISTER));
			break;
			
		case 0x54://3,4
			addLog("STCH");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.stch(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.stch(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0xB8://2
			addLog("TIXR");
			tmpInst = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 2);
			code = makingCode(tmpInst, 2);
			addInstruction(code);
			inst.tixr(rMgr.getRegister(PC_REGISTER));
			break;
			
		case 0x38://3,4
			addLog("JLT");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.jlt(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.jlt(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0x10://3,4
			addLog("STX");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.stx(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.stx(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0x4C://3,4
			addLog("RSUB");
			tmpInst = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
			code = makingCode(tmpInst, 3);
			addInstruction(code);
			inst.rsub();
			currentDev = "";
			break;
			
		case 0x50://3,4
			addLog("LDCH");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.ldch(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.ldch(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;
			
		case 0xDC://3,4
			addLog("WD");
			if(form_4) {
				logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
				code = makingCode(instruction, 4);
				addInstruction(code);
				inst.wd(rMgr.getRegister(PC_REGISTER), 4, addressing);
			}
			else {
				char [] instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
				code = makingCode(instruction, 3);
				addInstruction(code);
				inst.wd(rMgr.getRegister(PC_REGISTER), 3, addressing);
			}
			break;	
		}
	}
	
	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 */
	public void allStep() {
		while(!pEnd){
			oneStep();
		}
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
		logList.add(log);
	}
	
	/**
	 * 각 단계를 수행할 때 마다 수행한 명령어 code를 남기도록 한다.
	 */
	public void addInstruction(String instruction) {
		instructionsList.add(instruction);
	}
	
	/**
	 * 지금까지 수행한 로그기록들의 리스트를 반환하는 메소드이다.
	 * @return 지금까지 수행한 로그 기록이 저장된 리스트
	 */
	public List<String> getLogList(){
		return logList;
	}
	
	/**
	 * 지금까지 수행한 code기록들의 리스트를 반환하는 메소드이다.
	 * @return 지금까지 수행한 code 기록이 저장된 리스트
	 */
	public List<String> getInstList(){
		return instructionsList;
	}
	
	/**
	 * 현재 사용중인 장치(파일)이름을 반환하는 메소드이다.
	 * @return 현재 사용중인 장치(파일)이름
	 */
	public String getDevice() {
		return currentDev;
	}
	
	/**
	 * 현재 진행중인 명령어의 주소를 반환하는 메소드이다.
	 * @return 현재 진행중인 명령어의 주소
	 */
	public int getAddress() {
		return addr;
	}
	
	/**
	 * 현재 수행중인 code를 기록으로 남기기 위해 char[]를 String으로 바꿔주는 메소드이다.
	 * @param instruction String으로 바꾸려고 하는 code
	 * @param format instruction의 형식
	 * @return String으로 바꾼 code
	 */
	public String makingCode(char[] instruction, int format) {
		int tmp = instruction[0];
		String code = String.format("%02X", tmp);
		tmp = instruction[1];
		code += String.format("%02X", tmp);
		if(format == 4) {
			tmp = instruction[2];
			code += String.format("%02X", tmp);
			tmp = instruction[3];
			code += String.format("%02X", tmp);
		}
		else if(format == 3) {
			tmp = instruction[2];
			code += String.format("%02X", tmp);
		}
		return code;
	}
}
