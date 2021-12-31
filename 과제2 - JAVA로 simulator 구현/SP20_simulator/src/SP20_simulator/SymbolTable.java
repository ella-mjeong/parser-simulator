package SP20_simulator;
import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	ArrayList<String> modList;
	ArrayList<Integer> modLocationList;
	ArrayList<Integer> modSize;

	public SymbolTable() {
		symbolList = new ArrayList<>();
		addressList = new ArrayList<>();
		modList = new ArrayList<>();
		modLocationList = new ArrayList<>();
		modSize = new ArrayList<>();
	}
	
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param address : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int address) {
		if(symbolList.contains(symbol) == false) {
			symbolList.add(symbol);
			addressList.add(address);
		}
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newaddress : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newaddress) {
		int index;
		index = symbolList.indexOf(symbol);
		addressList.set(index, newaddress);
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
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol) {
		int address = 0;
		
		if(symbolList.contains(symbol)) {
			for (int i = 0; i < symbolList.size(); i++) {
				if (symbol.equals(symbolList.get(i))){
					address = addressList.get(i);
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
		return addressList.get(index);
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
