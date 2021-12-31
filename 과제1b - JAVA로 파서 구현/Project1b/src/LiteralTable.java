import java.util.ArrayList;

/**
 * literal과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class LiteralTable {
	ArrayList<String> literalList;
	ArrayList<Integer> locationList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	
	public LiteralTable() {
		literalList = new ArrayList<>();
		locationList = new ArrayList<>();
	}
	
	/**
	 * 새로운 Literal을 table에 추가한다.
	 * @param literal : 새로 추가되는 literal의 label
	 * @param location : 해당 literal이 가지는 주소값
	 * 주의 : 만약 중복된 literal이 putLiteral을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifyLiteral()을 통해서 이루어져야 한다.
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
	 * 기존에 존재하는 literal 값에 대해서 가리키는 주소값을 변경한다.
	 * @param literal : 변경을 원하는 literal의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifyLiteral(String literal, int newLocation) {
		int index;
		index = literalList.indexOf(literal);
		locationList.set(index, newLocation);
	}
	
	/**
	 * 인자로 전달된 literal이 어떤 주소를 지칭하는지 알려준다. 
	 * @param literal : 검색을 원하는 literal의 label
	 * @return literal이 가지고 있는 주소값. 해당 literal이 없을 경우 -1 리턴
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
	 * literal들의 주소를 넣어주는 함수이다
	 * @param locctr : 현재 프로그램의 주소
	 * @return locctr : literal들의 주소를 넣어주고 증가한 주소 반환
	 */
	public int addrLiteralTab(int locctr) {
		String tmpstr;
		int strsize;
		for (int i = 0; i < literalList.size(); i++) {
			if(search(literalList.get(i)) == -2) {
				modifyLiteral(literalList.get(i),locctr);
				if(literalList.get(i).charAt(0) == 'C' || literalList.get(i).charAt(0) == 'c') { //literal이 'C'인지 확인
					tmpstr = new String(literalList.get(i));
					strsize = tmpstr.length();
					tmpstr = tmpstr.substring(2,strsize-1); 
					locctr += tmpstr.length(); //char개수만큼 locctr 증가
				}
				else if(literalList.get(i).charAt(0) == 'X' || literalList.get(i).charAt(0) == 'x') { //literal이 'X"인지 확인
					tmpstr = new String(literalList.get(i));
					strsize = tmpstr.length();
					tmpstr = tmpstr.substring(2,strsize-1); 
					locctr += tmpstr.length()/2; //char개수만큼 locctr 증가
				}
			}
		}
		return locctr;
	}
	
	/**
	 * index에 해당하는 literal을 리턴한다.
	 * @param index : 리턴할 literal의 인덱스
	 * @return : literal
	 * 
	 */
	public String getLiteral(int index) {
		return literalList.get(index);
	}
	
	/**
	 * index를 이용하여 literal을 찾은 후 해당하는 literald의 address을 리턴한다.
	 * @param index : 리턴할 address의 인덱스
	 * @return : address
	 * 
	 */
	public int getaddress(int index) {
		return locationList.get(index);
	}
	
	/**
	 * literaltable의 크기을 리턴한다.
	 * @return : literalable의 크기
	 * 
	 */
	public int getSize() {
		return literalList.size();
	}
	
}
