import java.util.ArrayList;

/**
 * literal�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class LiteralTable {
	ArrayList<String> literalList;
	ArrayList<Integer> locationList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	public LiteralTable() {
		literalList = new ArrayList<>();
		locationList = new ArrayList<>();
	}
	
	/**
	 * ���ο� Literal�� table�� �߰��Ѵ�.
	 * @param literal : ���� �߰��Ǵ� literal�� label
	 * @param location : �ش� literal�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� literal�� putLiteral�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifyLiteral()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putLiteral(String literal, int location) {
		String tmpInst = new String(literal.substring(1));
		literal = tmpInst;
		if(literalList.contains(literal) == false) {
			literalList.add(literal);
			locationList.add(location);
		}
	}
	
	/**
	 * ������ �����ϴ� literal ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param literal : ������ ���ϴ� literal�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifyLiteral(String literal, int newLocation) {
		int index;
		index = literalList.indexOf(literal);
		locationList.set(index, newLocation);
	}
	
	/**
	 * ���ڷ� ���޵� literal�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param literal : �˻��� ���ϴ� literal�� label
	 * @return literal�� ������ �ִ� �ּҰ�. �ش� literal�� ���� ��� -1 ����
	 */
	public int search(String literal) {
		int address = 0;
		
		if(literalList.contains(literal)) {
			for (int i = 0; i < literalList.size(); i++) {
				if (literal.equals(literalList.get(i))){
					address = locationList.get(i);
					break;
				}
			}
		}
		else {
			address = -1;
		}
		return address;
	}
	
	/**
	 * literal���� �ּҸ� �־��ִ� �Լ��̴�
	 * @param locctr : ���� ���α׷��� �ּ�
	 * @return locctr : literal���� �ּҸ� �־��ְ� ������ �ּ� ��ȯ
	 */
	public int addrLiteralTab(int locctr) {
		String tmpstr;
		int strsize;
		for (int i = 0; i < literalList.size(); i++) {
			if(search(literalList.get(i)) == -2) {
				modifyLiteral(literalList.get(i),locctr);
				if(literalList.get(i).charAt(0) == 'C' || literalList.get(i).charAt(0) == 'c') { //literal�� 'C'���� Ȯ��
					tmpstr = new String(literalList.get(i));
					strsize = tmpstr.length();
					tmpstr = tmpstr.substring(2,strsize-1); 
					locctr += tmpstr.length(); //char������ŭ locctr ����
				}
				else if(literalList.get(i).charAt(0) == 'X' || literalList.get(i).charAt(0) == 'x') { //literal�� 'X"���� Ȯ��
					tmpstr = new String(literalList.get(i));
					strsize = tmpstr.length();
					tmpstr = tmpstr.substring(2,strsize-1); 
					locctr += tmpstr.length()/2; //char������ŭ locctr ����
				}
			}
		}
		return locctr;
	}
	
	/**
	 * index�� �ش��ϴ� literal�� �����Ѵ�.
	 * @param index : ������ literal�� �ε���
	 * @return : literal
	 * 
	 */
	public String getLiteral(int index) {
		return literalList.get(index);
	}
	
	/**
	 * index�� �̿��Ͽ� literal�� ã�� �� �ش��ϴ� literald�� address�� �����Ѵ�.
	 * @param index : ������ address�� �ε���
	 * @return : address
	 * 
	 */
	public int getaddress(int index) {
		return locationList.get(index);
	}
	
	/**
	 * literaltable�� ũ���� �����Ѵ�.
	 * @return : literalable�� ũ��
	 * 
	 */
	public int getSize() {
		return literalList.size();
	}
	
}
