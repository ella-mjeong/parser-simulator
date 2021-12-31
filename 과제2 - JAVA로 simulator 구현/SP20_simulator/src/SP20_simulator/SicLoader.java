package SP20_simulator;

import java.io.*;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		// 필요하다면 초기화
		setResourceManager(resourceManager);
	}

	
	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 */
	public void load(File objectCode){
		//pass1
		try {
			// 입력 스트림 생성
			FileReader fileReader = new FileReader(objectCode);
			// 입력 버퍼 생성
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			
			int currentSection = 0;
			int loc = 0;

			while((line = bufReader.readLine()) != null){ //파일을 끝까지 읽기 전까지 한줄씩 읽음
				if (line.length() == 0) {//읽은 문자열이 빈 라인이라면 다음 라인으로 넘기기
					continue;
				}
				
				//라인의 첫번째 문자로 그 라인을 판단
				switch(line.charAt(0)) {
				
				//Header Record
				//프로그램 이름, 시작주소, 프로그램 길이  저장
				case 'H':
					rMgr.setProgName(line.substring(1,7), currentSection);
					loc = Integer.parseInt(line.substring(7, 13),16);
					rMgr.setProgStart(loc, currentSection);
					rMgr.setProgLength(Integer.parseInt(line.substring(13,19),16), currentSection);
					if(currentSection == 0) {
						rMgr.symtabList.putSymbol(line.substring(1,7).trim(), loc);
					}
					else {
						rMgr.symtabList.putSymbol(line.substring(1,7).trim(), + rMgr.getProgStart(currentSection));
					}
					break;
				
				//Define Record
				//정의된 심볼들을 symbol table에 저장
				case 'D':
					for(int i = 0; i < ((line.length() - 1) / 12); i++){
						rMgr.symtabList.putSymbol(line.substring(12*i + 1, 12*i + 7).trim(), Integer.parseInt(line.substring(12*i + 7, 12*i + 13),16));
					}
					break;
					
				//Text Record
				case 'T':
					int currentAddr = Integer.parseInt(line.substring(1, 7), 16) + rMgr.getProgStart(currentSection);
					int lineLength = Integer.parseInt(line.substring(7, 9), 16);
					char[] pack = new char[lineLength];
					for(int i = 0; i < lineLength; i++){
						pack[i] = (char) (0 | Integer.parseInt(line.substring(2*i + 9, 2*i + 11),16));
					}
					rMgr.setMemory(currentAddr, pack, lineLength);
					break;
					
				//Modification Record
				//pass2에서 수정할 정보 저장
				case 'M':
					int modLocation = Integer.parseInt(line.substring(1, 7), 16) + rMgr.getProgStart(currentSection);
					int modSize = Integer.parseInt(line.substring(7, 9), 16);
					String modSymbol = line.substring(9, line.length());
					rMgr.symtabList.modSymbol(modSymbol, modLocation, modSize);
					break;
					
				//End Record
				case 'E':
					currentSection++;
					break;
				}
			}
			bufReader.close(); // 입력버퍼를 닫음
		}catch(FileNotFoundException e) {
			System.out.println("not found");
		}catch(IOException e) {
			System.out.println(e);
		}
		
		//pass2
		//M record의 정보를 이용하여 메모리에 저장된 값 수정
		for(int i = 0; i < rMgr.symtabList.modList.size(); i++) {
			String tmp = rMgr.symtabList.modList.get(i);
			int size = rMgr.symtabList.modSize.get(i);
			char mode = tmp.charAt(0);
			String symbol = tmp.substring(1);
			
			String modifyAddr = new String();
			char[] pack = new char[3];
			if(size == 5) {
				modifyAddr = String.format("%05X", rMgr.symtabList.search(symbol));
				char[] temp = rMgr.getMemory(rMgr.symtabList.modLocationList.get(i), 1);
				int temp2 = temp[0];
				temp2 = temp2 >>> 4;
				temp2 = temp2 << 4;
				pack[0] = (char)(temp2 | Integer.parseInt(modifyAddr.substring(0,1),16));
				
				for(int j = 1; j < 3; j++){
					pack[j] = (char) (0 | Integer.parseInt(modifyAddr.substring(2*j - 1, 2*j + 1),16));
				}	
			}
			else if(size == 6) {
				modifyAddr = String.format("%06X", rMgr.symtabList.search(symbol));
				if(mode == '-') {
					char[] temp = rMgr.getMemory(rMgr.symtabList.modLocationList.get(i), 3);
					int tmpChar = temp[0];
					String tmp2 = String.format("%02X", tmpChar);
					tmpChar = temp[1];
					tmp2 += String.format("%02X", tmpChar);
					tmpChar = temp[2];
					tmp2 += String.format("%02X", tmpChar);
					
					tmpChar = Integer.parseInt(tmp2,16);
					int tmpNew = Integer.parseInt(modifyAddr,16);
					modifyAddr = String.format("%06X", tmpChar - tmpNew);
				}
				
				for(int j = 0; j < 3; j++){
					pack[j] = (char) (0 | Integer.parseInt(modifyAddr.substring(2*j, 2*j + 2),16));
				}
			}
			rMgr.setMemory(rMgr.symtabList.modLocationList.get(i), pack, 3);
		}
		
	};

}
