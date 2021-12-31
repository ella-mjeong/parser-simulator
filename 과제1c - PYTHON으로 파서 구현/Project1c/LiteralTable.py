class LiteralTable:
    def __init__(self):
        '''
        클래스를 초기화한다.
        '''
        self.literalList = []
        self.locationList = []

    def putLiteral(self,literal,location):
        '''
        새로운 Literal을 table에 추가하는 함수이다. 중복된 literal은 putLiteral로 입력될 수 없다. literal의 주소를 변경하려면 modifyLital()를 이용한다.

        literal : 새로 추가되는 literal의 label\n
        location : 해당 literal의 주소값
        '''
        tmpliteral = literal[1:]
        if not tmpliteral in self.literalList:
            self.literalList.append(tmpliteral)
            self.locationList.append(location)
    
    def modifyLiteral(self, literal, newLocation):
        '''
        기존에 존재하는 literal 값에 대해서 해당 literal의 주소값을 변경한다.

        literal : 주소값의 변경을 원하는 literal의 label\n
        newLocation : 새로 바꾸고자 하는 주소값
        '''
        if literal in self.literalList:
            index = self.literalList.index(literal)
            self.locationList[index] = newLocation

    def search(self, literal):
        '''
        인자로 전달된 literal이 어떤 주소를 지칭하는지 알려준다.

        literal : 검색을 원하는 literal의 label\n
        return : literal이 가지고 있는 주소값. 해당 literal이 없을 경우 -1 리턴
        '''
        if literal in self.literalList:
            index = self.literalList.index(literal)
            return self.locationList[index]
        else:
            return -1

    def getAddress(self, index):
        '''
        주어진 index에 해당하는 address을 리턴한다.
        
        index : address을 검색할 인덱스\n
        return : index에 해당하는 address
        '''
        return self.locationList[index]

    def getLiteral(self, index):
        '''
        주어진 index에 해당하는 literal을 리턴한다.
        
        index : literal을 검색할 인덱스\n
        return : index에 해당하는 literal
        '''
        return self.literalList[index]


    def addrLiteralTab(self, locctr):
        '''
        literal들의 주소를 넣어주는 함수이다.

        locctr : 현재 프로그램의 주소\n
        return : literal들의 주소를 넣어주고 증가한 주소를 리턴
        '''
        for i in range(0,len(self.literalList)):
            if -2 in self.locationList:
                self.modifyLiteral(self.literalList[i],locctr)
                if self.literalList[i][0] == 'C' or self.literalList[i][0] == 'c': # literal이 'C'인지 확인
                    tmpStr = self.literalList[i]
                    tmpStr = tmpStr[2:len(tmpStr)-1]
                    locctr += len(tmpStr) #char개수만큼 locctr 증가
                elif self.literalList[i][0] == 'X' or self.literalList[i][0] == 'x': #literal이 'X'인지 확인
                    tmpStr = self.literalList[i]
                    tmpStr = tmpStr[2:len(tmpStr)-1]
                    locctr += int(len(tmpStr)/2) #char개수/2만큼 locctr 증가
        return locctr

    def getSize(self):
        '''
        literaltable의 크기를 알려주는 함수이다.

        return : literal table의 크기
        '''
        return len(self.literalList)
