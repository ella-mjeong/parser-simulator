package SP20_simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



/**
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * ����̽��� ���� ����� ��ġ���� �ǹ� ������ ���⼭�� ���Ϸ� ����̽��� ��ü�Ѵ�.<br>
	 * ��, 'F1'�̶�� ����̽��� 'F1'�̶�� �̸��� ������ �ǹ��Ѵ�. <br>
	 * deviceManager�� ����̽��� �̸��� �Է¹޾��� �� �ش� �̸��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	char[] memory = new char[65536]; // String���� �����ؼ� ����Ͽ��� ������.
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList = new SymbolTable();
	// �̿ܿ��� �ʿ��� ���� �����ؼ� ����� ��.
	List<String> progNameList = new ArrayList<>(); //���α׷� �̸� ����
	List<Integer> progLengthList = new ArrayList<>(); //���α׷� ���� ����
	List<Integer> progStartList = new ArrayList<>(); //���α׷� �����ּ� ����
	
	int currentSection = 0; //���� ����
	int readCheck = 0;
	
	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	public void initializeResource(){
		for(int i = 0; i < memory.length; i++) {
			memory[i] = '0';
		}
		for(int i = 0; i < register.length; i++) {
			register[i] = 0;
		}
		register_F = 0;
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 */
	public void closeDevice() {
		Iterator<String> keys = deviceManager.keySet().iterator();
		while(keys.hasNext()) {
			String key = keys.next();
			Object stream = deviceManager.get(key);
			
			if(stream instanceof FileReader) {
				try {
					((FileReader)stream).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if(stream instanceof FileWriter) {
				try {
					((FileWriter)stream).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 */
	public void testDevice(String devName) {
		try {
			File file = new File(devName);
			if(devName.equals("F1")) {
				FileReader fileReader = new FileReader(file);
				deviceManager.put(devName, fileReader);
				setRegister(9,1);
			}
			else if(devName.equals("05")) {
				FileWriter fileWriter = new FileWriter(file, true); //append ���� flie write
				deviceManager.put(devName, fileWriter);
				setRegister(9,1);
			}
		}catch(FileNotFoundException e) {
			setRegister(9,0);
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������
	 */
	public char[] readDevice(String devName, int num){
		FileReader fileReader = (FileReader)deviceManager.get(devName);
		char[] cs = new char[num];
		int index = 0;
		int charTmp = 0;
		try {
			while(index <= readCheck) {
				charTmp = fileReader.read();
				index++;
			}
			if(charTmp == -1) {
				cs[0] = 0;
			}
			else {
				cs[0] = (char)charTmp;
			}
			readCheck++;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cs;

	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 * @param num ������ ������ ����
	 */
	public void writeDevice(String devName, char[] data, int num){
		FileWriter fileWriter = (FileWriter)deviceManager.get(devName);
		try {
			for(int i = 0; i < num; i++) {
				fileWriter.write(data[i]);
				fileWriter.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
	 */
	public char[] getMemory(int location, int num){
		char[] data = new char[num];
		
		for(int i = 0; i < num; i++) {
			data[i] = memory[location + i];
		}
		
		return data;
		
	}

	/**
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
	 */
	public void setMemory(int locate, char[] data, int num){
		for(int i = 0; i < num; i++) {
			memory[locate + i] = data[i];
		}
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}
	
	/**
	 * �־��� ��Ʈ�Ѽ����� ���α׷� �̸��� �������� �Լ�
	 * @param currentSection ���α׷� �̸��� �˰���� ��Ʈ�� ����
	 */
	public String getProgName(int currentSection){
		return progNameList.get(currentSection);
	}
	
	/**
	 * �־��� ��Ʈ�Ѽ����� ���α׷� ���̸� �������� �Լ�
	 * @param currentSection ���α׷� ���̸� �˰���� ��Ʈ�� ����
	 */
	public int getProgLength(int currentSection){
		return progLengthList.get(currentSection);
	}
	
	/**
	 * �־��� ��Ʈ�Ѽ����� ���α׷� �����ּҸ� �������� �Լ�
	 * @param currentSection ���α׷� ���̸� �˰���� ��Ʈ�� ����
	 */
	public int getProgStart(int currentSection){
		return progStartList.get(currentSection);
	}
	
	/**
	 * ���� ��Ʈ�Ѽ����� �������� �Լ�
	 */
	public int getCurrentSection(){
		return currentSection;
	}
	
	/**
	 * ���α׷��� ��ü ���̸� �������� �Լ�
	 */
	public int getProgTotalLen() {
		int len = 0;
		for(int i = 0; i< progLengthList.size(); i++) {
			len += progLengthList.get(i);
		}
		return len;
	}


	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * ���α׷� �̸��� �����ϴ� �Լ�
	 * @param progName ���α׷� �̸�
	 * @param currentSection ���α׷� �̸��� �ش��ϴ� ��Ʈ�� ����
	 */
	public void setProgName(String progName, int currentSection){
		progNameList.add(currentSection, progName);
	}
	
	/**
	 * ���α׷� ���̸� �����ϴ� �Լ�
	 * @param progLength ���α׷� ����
	 * @param currentSection ���α׷� ���̿� �ش��ϴ� ��Ʈ�� ����
	 */
	public void setProgLength(int progLength, int currentSection){
		progLengthList.add(currentSection, progLength);
	}
	
	/**
	 * ���α׷� �����ּҸ� �����ϴ� �Լ�
	 * @param progStart ���α׷� �����ּ�
	 * @param currentSection ���α׷� ���̿� �ش��ϴ� ��Ʈ�� ����
	 */
	public void setProgStart(int progStart, int currentSection){
		int address = progStart;
		
		if(currentSection != 0) { //ù��° ������ �ƴ϶�� ���� ������ �ּҸ� ������
			address += progStartList.get(currentSection - 1) +  progLengthList.get(currentSection - 1);
		}
		progStartList.add(currentSection, address);
	}
	
	/**
	 * ���� ��Ʈ�Ѽ����� �����ϴ� �Լ�
	 * @param currentSection ������ ��Ʈ�Ѽ���
	 */
	public void setCurrentSection(int currentSection){
		this.currentSection = currentSection;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		String tmp = "";
		char[] charData = new char[3];
		
		tmp = String.format("%06X", data);
	
		for(int i = 0; i < 3; i++) {
			charData[i] = (char)(Integer.parseInt(tmp.substring(2*i,2*i+2),16));
		}
		
		return charData;
	}

	/**
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. char[]���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int byteToInt(char[] data){
		int intData = 0;
		
		String tmp = new String();
		for(int i = 0; i < data.length; i++){
			tmp += String.format("%02X", (int)data[i]);
		}
		intData = Integer.parseInt(tmp,16);
		return intData;
	}
}