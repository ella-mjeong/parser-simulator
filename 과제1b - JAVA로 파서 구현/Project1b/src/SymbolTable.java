import java.util.ArrayList;

/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	ArrayList<String> modList;
	ArrayList<Integer> modLocationList;
	ArrayList<Integer> modSize;
	ArrayList<String> defList;
	ArrayList<String> refList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	
	public SymbolTable() {//초기화
		symbolList = new ArrayList<>();
		locationList = new ArrayList<>();
		modList = new ArrayList<>();
		modLocationList = new ArrayList<>();
		modSize = new ArrayList<>();
		defList = new ArrayList<>();
		refList = new ArrayList<>();
	}
	
	
	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param location : 해당 symbol이 가지는 주소값
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, int location) {
		if(symbolList.contains(symbol) == false) {
				symbolList.add(symbol);
				locationList.add(location);
		}
	}
	
	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation) {
		int index;
		index = symbolList.indexOf(symbol);
		locationList.set(index, newLocation);
	}
	
	/**
	 * modification record에 작성할 symbol들을 table에 추가한다.
	 * @param symbol : modify될 symbol의 label
	 * @param location : 해당 symbol의 location
	 * @param size : modify될 바이트의 크기
	 */
	public void modSymbol(String symbol, int location, int size) {
		modList.add(symbol);
		modLocationList.add(location);
		modSize.add(size);
	}
	
	/**
	 * 지시어 EXTDEF가 나왔을 때 symbol을 저장
	 * @param symbol : 새로 추가되는 symbol의 label
	 * 
	 */
	public void setdefSymbol(String symbol) {
		defList.add(symbol);
	}
	
	/**
	 * 지시어 EXTREF가 나왔을 때 symbol을 저장
	 * @param symbol : 새로 추가되는 symbol의 label
	 * 
	 */
	public void setrefSymbol(String symbol) {
		refList.add(symbol);
	}
	
	/**
	 * 주어진 symbol이 definition된 symbol인지 확인
	 * @param symbol : 확인하고자 하는 symbol
	 * @return : definition된 symbol이면 1, 아니라면 -1 반환
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
	 * 주어진 symbol이 reference된 symbol인지 확인
	 * @param symbol : 확인하고자 하는 symbol
	 * @return : reference된 symbol이면 1, 아니라면 -1 반환
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
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
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
	 * index에 해당하는 symbol을 리턴한다.
	 * @param index : 리턴할 symbol의 인덱스
	 * @return : symbol
	 * 
	 */
	public String getSymbol(int index) {
		return symbolList.get(index);
	}
	
	/**
	 * index를 이용하여 symbol을 찾은 후 그 symbol에 해당하는 address을 리턴한다.
	 * @param index : 리턴할 address의 인덱스
	 * @return : address
	 * 
	 */
	public int getaddress(int index) {
		return locationList.get(index);
	}
	
	/**
	 * symboltable의 크기을 리턴한다.
	 * @return : symboltable의 크기
	 * 
	 */
	public int getSize() {
		return symbolList.size();
	}
	
}
