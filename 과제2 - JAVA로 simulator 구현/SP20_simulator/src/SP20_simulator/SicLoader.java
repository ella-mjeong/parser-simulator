package SP20_simulator;

import java.io.*;

/**
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. 
 * <br><br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader {
	ResourceManager rMgr;
	
	public SicLoader(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ
		setResourceManager(resourceManager);
	}

	
	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
	}
	
	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
	 */
	public void load(File objectCode){
		//pass1
		try {
			// �Է� ��Ʈ�� ����
			FileReader fileReader = new FileReader(objectCode);
			// �Է� ���� ����
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			
			int currentSection = 0;
			int loc = 0;

			while((line = bufReader.readLine()) != null){ //������ ������ �б� ������ ���پ� ����
				if (line.length() == 0) {//���� ���ڿ��� �� �����̶�� ���� �������� �ѱ��
					continue;
				}
				
				//������ ù��° ���ڷ� �� ������ �Ǵ�
				switch(line.charAt(0)) {
				
				//Header Record
				//���α׷� �̸�, �����ּ�, ���α׷� ����  ����
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
				//���ǵ� �ɺ����� symbol table�� ����
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
				//pass2���� ������ ���� ����
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
			bufReader.close(); // �Է¹��۸� ����
		}catch(FileNotFoundException e) {
			System.out.println("not found");
		}catch(IOException e) {
			System.out.println(e);
		}
		
		//pass2
		//M record�� ������ �̿��Ͽ� �޸𸮿� ����� �� ����
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
