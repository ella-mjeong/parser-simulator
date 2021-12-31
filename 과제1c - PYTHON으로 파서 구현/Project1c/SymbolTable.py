class SymbolTable:
    def __init__(self):
        '''
        클래스를 초기화한다
        '''
        self.symbolList = []
        self.locationList = []
        self.modList = []
        self.modLocationList = []
        self.modSize = []
        self.defList = []
        self.refList = []

    def putSymbol(self, symbol, location):
        '''
        새로운 symbol을 table에 추가하는 함수이다. 중복된 symbol은 putSymbol로 입력될 수 없다. symbol의 주소를 변경하려면 modifySymbol()를 이용한다.

        symbol : 새로 추가되는 symbol의 label\n
        location : 해당 symbol의 주소값
        '''
        if not symbol in self.symbolList:
            self.symbolList.append(symbol)
            self.locationList.append(location)

    def modifySymbol(self, symbol, newLocation):
        '''
        기존에 존재하는 symbol값에 대하여 가리키는 주소값을 변경한다.

        symbol : 변경을 원하는 symbol의 label\n
        newLocation : 새로 바꾸고자 하는 주소값
        '''
        if symbol in self.symbolList:
            index = self.symbolList.index(symbol)
            self.locationList[index] = newLocation

    def modSymbol(self, symbol, location, size):
        '''
        modification record에 작성할 symbol들을 table에 추가한다.
	    
        symbol : modify될 symbol의 label\n
	    location : 해당 symbol의 location\n
	    size : modify될 바이트의 크기
        '''
        self.modList.append(symbol)
        self.modLocationList.append(location)
        self.modSize.append(size)

    def setDefSymbol(self, symbol):
        '''
        지시어 EXTDEF가 나왔을 때 symbol을 저장
	    
        symbol : 새로 추가되는 symbol의 label
        '''
        self.defList.append(symbol)

    def setRefSymbol(self, symbol):
        '''
        지시어 EXTREF가 나왔을 때 symbol을 저장
	    
        symbol : 새로 추가되는 symbol의 label
        '''
        self.refList.append(symbol)

    def searchRefSymbol(self, symbol):
        '''
        주어진 symbol이 reference된 symbol인지 확인

	    symbol : 확인하고자 하는 symbol\n
	    return : reference symbol이면 1, 아니라면 -1 반환
        '''
        if symbol in self.refList:
            return 1
        else:
            return -1

    def search(self, symbol):
        '''
        인자로 전달된 symbol이 어떤 주소를 가리키는지 알려준다. 

	    symbol : 검색을 원하는 symbol의 label\n
	    symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
        '''
        if symbol in self.symbolList:
            index = self.symbolList.index(symbol)
            return self.locationList[index]
        else:
            return -1

    def getSymbol(self, index):
        '''
        주어진 index에 해당하는 symbol을 리턴한다.
        
        index : symbol을 검색할 인덱스\n
        return : index에 해당하는 symbol
        '''
        return self.symbolList[index]

    def getAddress(self, index):
        '''
        index를 이용하여 symbol을 찾은 후 그 symbol에 해당하는 address을 리턴한다.

	    index : address을 검색할 인덱스\n
	    return : address
        '''
        return self.locationList[index]

    def getSize(self):
        '''
        symboltable의 크기을 리턴한다.

	    return : symboltable의 크기
        '''
        return len(self.symbolList)