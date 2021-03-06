package SP20_simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



/**
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다.
 * 크게 네가지의 가상 자원 공간을 선언하고, 이를 관리할 수 있는 함수들을 제공한다.<br><br>
 * 
 * 1) 입출력을 위한 외부 장치 또는 device<br>
 * 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.<br>
 * 3) 연산을 수행하는데 사용하는 레지스터 공간.<br>
 * 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들. 
 * <br><br>
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면,
 * 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서 차이가 있다.
 */
public class ResourceManager{
	/**
	 * 디바이스는 원래 입출력 장치들을 의미 하지만 여기서는 파일로 디바이스를 대체한다.<br>
	 * 즉, 'F1'이라는 디바이스는 'F1'이라는 이름의 파일을 의미한다. <br>
	 * deviceManager는 디바이스의 이름을 입력받았을 때 해당 이름의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
	 * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을 넣음으로서 이를 관리할 수 있다.
	 * <br><br>
	 * 변형된 형태로 사용하는 것 역시 허용한다.<br>
	 * 예를 들면 key값으로 String대신 Integer를 사용할 수 있다.
	 * 파일 입출력을 위해 사용하는 stream 역시 자유로이 선택, 구현한다.
	 * <br><br>
	 * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
	 */
	HashMap<String,Object> deviceManager = new HashMap<String,Object>();
	char[] memory = new char[65536]; // String으로 수정해서 사용하여도 무방함.
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList = new SymbolTable();
	// 이외에도 필요한 변수 선언해서 사용할 것.
	List<String> progNameList = new ArrayList<>(); //프로그램 이름 저장
	List<Integer> progLengthList = new ArrayList<>(); //프로그램 길이 저장
	List<Integer> progStartList = new ArrayList<>(); //프로그램 시작주소 저장
	
	int currentSection = 0; //현재 섹션
	int readCheck = 0;
	
	/**
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
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
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
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
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
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
				FileWriter fileWriter = new FileWriter(file, true); //append 모드로 flie write
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
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터
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
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param data 보내는 데이터
	 * @param num 보내는 글자의 개수
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
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	public char[] getMemory(int location, int num){
		char[] data = new char[num];
		
		for(int i = 0; i < num; i++) {
			data[i] = memory[location + i];
		}
		
		return data;
		
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	public void setMemory(int locate, char[] data, int num){
		for(int i = 0; i < num; i++) {
			memory[locate + i] = data[i];
		}
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return register[regNum];
		
	}
	
	/**
	 * 주어진 컨트롤섹션의 프로그램 이름을 가져오는 함수
	 * @param currentSection 프로그램 이름을 알고싶은 컨트롤 섹션
	 */
	public String getProgName(int currentSection){
		return progNameList.get(currentSection);
	}
	
	/**
	 * 주어진 컨트롤섹션의 프로그램 길이를 가져오는 함수
	 * @param currentSection 프로그램 길이를 알고싶은 컨트롤 섹션
	 */
	public int getProgLength(int currentSection){
		return progLengthList.get(currentSection);
	}
	
	/**
	 * 주어진 컨트롤섹션의 프로그램 시작주소를 가져오는 함수
	 * @param currentSection 프로그램 길이를 알고싶은 컨트롤 섹션
	 */
	public int getProgStart(int currentSection){
		return progStartList.get(currentSection);
	}
	
	/**
	 * 현재 컨트롤섹션을 가져오는 함수
	 */
	public int getCurrentSection(){
		return currentSection;
	}
	
	/**
	 * 프로그램의 전체 길이를 가져오는 함수
	 */
	public int getProgTotalLen() {
		int len = 0;
		for(int i = 0; i< progLengthList.size(); i++) {
			len += progLengthList.get(i);
		}
		return len;
	}


	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		register[regNum] = value;
	}

	/**
	 * 프로그램 이름을 저장하는 함수
	 * @param progName 프로그램 이름
	 * @param currentSection 프로그램 이름에 해당하는 컨트롤 섹션
	 */
	public void setProgName(String progName, int currentSection){
		progNameList.add(currentSection, progName);
	}
	
	/**
	 * 프로그램 길이를 저장하는 함수
	 * @param progLength 프로그램 길이
	 * @param currentSection 프로그램 길이에 해당하는 컨트롤 섹션
	 */
	public void setProgLength(int progLength, int currentSection){
		progLengthList.add(currentSection, progLength);
	}
	
	/**
	 * 프로그램 시작주소를 저장하는 함수
	 * @param progStart 프로그램 시작주소
	 * @param currentSection 프로그램 길이에 해당하는 컨트롤 섹션
	 */
	public void setProgStart(int progStart, int currentSection){
		int address = progStart;
		
		if(currentSection != 0) { //첫번째 섹션이 아니라면 이전 섹션의 주소를 더해줌
			address += progStartList.get(currentSection - 1) +  progLengthList.get(currentSection - 1);
		}
		progStartList.add(currentSection, address);
	}
	
	/**
	 * 현재 컨트롤섹션을 저장하는 함수
	 * @param currentSection 저장할 컨트롤섹션
	 */
	public void setCurrentSection(int currentSection){
		this.currentSection = currentSection;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
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
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. char[]값을 int형태로 변경한다.
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