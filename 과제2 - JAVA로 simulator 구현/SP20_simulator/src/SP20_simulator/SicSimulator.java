package SP20_simulator;

import java.io.File;
import java.util.*;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
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
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
		inst = new InstLuncher(rMgr);
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ ��*/
		rMgr.initializeResource();
	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
	 */
	public void oneStep() {
		addr = rMgr.getRegister(PC_REGISTER);
		if(pEnd) {//��� instruction�� ������ �������� �ǹ�
			return;
		}
		char [] bytes = rMgr.getMemory(addr,2);
		int temp = bytes[0];
		int opcode = temp;
		int addressing = 0;
		boolean form_4 = false;
		
		// indirect addressing���� immediate addressing���� �Ǵ�
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
			
		//�ι�° ����Ʈ�� �̿��Ͽ� Ȯ��� 4���� ��ɾ����� üũ
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
	 * ���� ��� instruction�� ����� ����� ���δ�.
	 */
	public void allStep() {
		while(!pEnd){
			oneStep();
		}
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
		logList.add(log);
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ������ ��ɾ� code�� ���⵵�� �Ѵ�.
	 */
	public void addInstruction(String instruction) {
		instructionsList.add(instruction);
	}
	
	/**
	 * ���ݱ��� ������ �αױ�ϵ��� ����Ʈ�� ��ȯ�ϴ� �޼ҵ��̴�.
	 * @return ���ݱ��� ������ �α� ����� ����� ����Ʈ
	 */
	public List<String> getLogList(){
		return logList;
	}
	
	/**
	 * ���ݱ��� ������ code��ϵ��� ����Ʈ�� ��ȯ�ϴ� �޼ҵ��̴�.
	 * @return ���ݱ��� ������ code ����� ����� ����Ʈ
	 */
	public List<String> getInstList(){
		return instructionsList;
	}
	
	/**
	 * ���� ������� ��ġ(����)�̸��� ��ȯ�ϴ� �޼ҵ��̴�.
	 * @return ���� ������� ��ġ(����)�̸�
	 */
	public String getDevice() {
		return currentDev;
	}
	
	/**
	 * ���� �������� ��ɾ��� �ּҸ� ��ȯ�ϴ� �޼ҵ��̴�.
	 * @return ���� �������� ��ɾ��� �ּ�
	 */
	public int getAddress() {
		return addr;
	}
	
	/**
	 * ���� �������� code�� ������� ����� ���� char[]�� String���� �ٲ��ִ� �޼ҵ��̴�.
	 * @param instruction String���� �ٲٷ��� �ϴ� code
	 * @param format instruction�� ����
	 * @return String���� �ٲ� code
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
