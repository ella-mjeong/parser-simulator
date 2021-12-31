import java.util.HashMap;
import java.io.*;
import java.util.StringTokenizer;

/**
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 */
	public void openFile(String fileName) {
		try {
			// ���� ��ü ����
			File file = new File(fileName);
			// �Է� ��Ʈ�� ����
			FileReader fileReader = new FileReader(file);
			// �Է� ���� ����
			BufferedReader bufReader = new BufferedReader(fileReader);

			String line = "";
			String inst; 
			StringTokenizer tokens;
		
			while((line = bufReader.readLine()) != null){ //������ ������ �б� ������ ���پ� ����
				tokens = new StringTokenizer(line);// �о� ���� ������ ���� �������� �� �� �и��Ͽ� instruction�� ����
				inst = tokens.nextToken(" \t");
				instMap.put(inst, new Instruction(line)); //instruction �̸��� key�� ����
			}
			bufReader.close(); // �Է¹��۸� ����
		}catch(FileNotFoundException e) {
			System.out.println("not found");
		}catch(IOException e) {
			System.out.println(e);
		}
	}
	
	//get, set, search ���� �Լ��� ���� ����
	/**
	 * ���ڷ� ���� ��ɾ��� opcode�� ã�� ������
	 * @param instruction: opcode�� �˰���� ��ɾ�
	 * @return: �ش� ��ɾ��� opcode
	**/
	public int getOpcode(String instruction) {
		if(instMap.containsKey(instruction)) //�־��� ��ɾ key������ �����Ѵٸ� �ش��ϴ� ��ü�� opcode�� ã�Ƽ� ����
			return instMap.get(instruction).opcode;
		else //�־��� ��ɾ key������ �������� ���� �� -1 ����
			return -1;
	}

	/**
	 * ���ڷ� ���� ��ɾ��� �ǿ����� ������ ã�� ������
	 * @param instruction: �ǿ����� ������ �˰���� ��ɾ�
	 * @return: �ش� ��ɾ��� �ǿ����� ����
	 */
	public int getNumberOfOperand(String instruction) {
		if(instMap.containsKey(instruction)) //�־��� ��ɾ key������ �����Ѵٸ� �ش��ϴ� ��ü�� �ǿ����ڰ����� ã�Ƽ� ����
			return instMap.get(instruction).numberOfOperand;
		else  //�־��� ��ɾ key������ �������� ���� �� -1 ����
			return -1;
	}
	
	/**
	 * ���ڷ� ���� ��ɾ��� ������ ã�� ������
	 * @param instruction: ������ �˰���� ��ɾ�
	 * @return: �ش� ��ɾ��� ����
	 */
	public int getFormat(String instruction) {
		if(instMap.containsKey(instruction)) //�־��� ��ɾ key������ �����Ѵٸ� �ش��ϴ� ��ü�� ������ ã�Ƽ� ����
			return instMap.get(instruction).format;
		else //�־��� ��ɾ key������ �������� ���� �� -1 ����
			return -1;
	}
	
	/**
	 * �Է¹��ڿ��� ��ɾ����� �˻�.
	 * 
	 * @param str: �˻��ϰ���� ���ڿ�
	 * @return: ��ɾ �´ٸ� 1, �ƴ϶�� -1 ����
	 */
	public int isInstruction(String str) {
		if(instMap.containsKey(str)) { // �Էµ� ���ڿ��� ��ɾ���(key������ �����Ѵٸ�)
			return 1; 
		}
		else {//�־��� ��ɾ key������ �������� ���� �� -1 ����
			return -1;
		}
	}
	
	/**
	 * �Է¹��ڿ��� ���þ����� �˻�.
	 * 
	 * @param str: �˻��ϰ���� ���ڿ�
	 * @return: ���þ �´ٸ� >=0, �ƴ϶�� -1 ����
	 */
	public int isDirective(String str) {
		String directiveTable[] = {"START","END","BYTE","WORD","RESB","RESW","CSECT","EXTDEF","EXTREF","EQU","ORG","LTORG"};
		int directive_num[] = {1,1,1,1,1,1,0,1,1,1,1,0};
		for(int i = 0; i < directiveTable.length; i++) {
			if(str.equals(directiveTable[i])) { // �Էµ� ���ڿ��� directive_table�� ���þ�� ��ġ�Ѵٸ�
				return directive_num[i]; // �ڿ� ������ ���ԵǾ� �ִ� ���þ��� 1, �ڿ� ������ ���ԵǾ� ���� ���� ���þ��� 0 ����
			}
		}
		return -1; //�Էµ� ���ڿ��� ���þ �ƴ϶�� -1 ����
	}
	
}
/**
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	/* 
	 * ������ inst.data ���Ͽ� �°� �����ϴ� ������ �����Ѵ�.
	 * 
	 */
	
	String instruction; //��ɾ�
	int opcode; //�� ��ɾ �ش��ϴ� opcode
	int numberOfOperand; //�� ��ɾ��� �ǿ������� ����
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		// TODO Auto-generated method stub
		StringTokenizer tokens = new StringTokenizer(line);
		instruction = tokens.nextToken(" \t");
		numberOfOperand = Integer.parseInt(tokens.nextToken(" \t"));
		format = Integer.parseInt(tokens.nextToken(" \t"));
		opcode = Integer.parseInt(tokens.nextToken(" \t"), 16);
	}
	//�� �� �Լ� ���� ����
	
}
