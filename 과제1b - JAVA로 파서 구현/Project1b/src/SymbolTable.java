import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	ArrayList<String> modList;
	ArrayList<Integer> modLocationList;
	ArrayList<Integer> modSize;
	ArrayList<String> defList;
	ArrayList<String> refList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	public SymbolTable() {//�ʱ�ȭ
		symbolList = new ArrayList<>();
		locationList = new ArrayList<>();
		modList = new ArrayList<>();
		modLocationList = new ArrayList<>();
		modSize = new ArrayList<>();
		defList = new ArrayList<>();
		refList = new ArrayList<>();
	}
	
	
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param location : �ش� symbol�� ������ �ּҰ�
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int location) {
		if(symbolList.contains(symbol) == false) {
				symbolList.add(symbol);
				locationList.add(location);
		}
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newLocation) {
		int index;
		index = symbolList.indexOf(symbol);
		locationList.set(index, newLocation);
	}
	
	/**
	 * modification record�� �ۼ��� symbol���� table�� �߰��Ѵ�.
	 * @param symbol : modify�� symbol�� label
	 * @param location : �ش� symbol�� location
	 * @param size : modify�� ����Ʈ�� ũ��
	 */
	public void modSymbol(String symbol, int location, int size) {
		modList.add(symbol);
		modLocationList.add(location);
		modSize.add(size);
	}
	
	/**
	 * ���þ� EXTDEF�� ������ �� symbol�� ����
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * 
	 */
	public void setdefSymbol(String symbol) {
		defList.add(symbol);
	}
	
	/**
	 * ���þ� EXTREF�� ������ �� symbol�� ����
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * 
	 */
	public void setrefSymbol(String symbol) {
		refList.add(symbol);
	}
	
	/**
	 * �־��� symbol�� definition�� symbol���� Ȯ��
	 * @param symbol : Ȯ���ϰ��� �ϴ� symbol
	 * @return : definition�� symbol�̸� 1, �ƴ϶�� -1 ��ȯ
	 * 
	 */
	public int searchdefSymbol(String symbol) {
		if(defList.contains(symbol) == true) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	/**
	 * �־��� symbol�� reference�� symbol���� Ȯ��
	 * @param symbol : Ȯ���ϰ��� �ϴ� symbol
	 * @return : reference�� symbol�̸� 1, �ƴ϶�� -1 ��ȯ
	 * 
	 */
	public int searchrefSymbol(String symbol) {
		if(refList.contains(symbol) == true) {
			return 1;
		}
		else {
			return -1;
		}
	}
	
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = 0;
		
		if(symbolList.contains(symbol)) {
			for (int i = 0; i < symbolList.size(); i++) {
				if (symbol.equals(symbolList.get(i))){
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
	 * index�� �ش��ϴ� symbol�� �����Ѵ�.
	 * @param index : ������ symbol�� �ε���
	 * @return : symbol
	 * 
	 */
	public String getSymbol(int index) {
		return symbolList.get(index);
	}
	
	/**
	 * index�� �̿��Ͽ� symbol�� ã�� �� �� symbol�� �ش��ϴ� address�� �����Ѵ�.
	 * @param index : ������ address�� �ε���
	 * @return : address
	 * 
	 */
	public int getaddress(int index) {
		return locationList.get(index);
	}
	
	/**
	 * symboltable�� ũ���� �����Ѵ�.
	 * @return : symboltable�� ũ��
	 * 
	 */
	public int getSize() {
		return symbolList.size();
	}
	
}
