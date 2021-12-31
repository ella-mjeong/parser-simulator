import re

class TokenTable:
    def __init__(self, symTab, literalTab, instTab):
        '''
        클래스를 초기화시키는 함수로 pass2 과정에서 object code로 변환하기 위해 symbolTable과 instTable과 instTable을 링크시킨다.

        symTab : 해당 section과 연결되어있는 symbol table\n
	    literalTab : 해당 section과 연결되어있는 literal table\n
	    instTab : instruction 명세가 정의된 instTable\n
        '''
        #bit 조작의 가독성을 위해 static 변수 선언
        TokenTable.nFlag = 32
        TokenTable.iFlag = 16
        TokenTable.xFlag = 8
        TokenTable.bFlag = 4
        TokenTable.pFlag = 2
        TokenTable.eFlag = 1

        #토큰을 다룰 때 필요한 테이블들을 링크시킨다.
        self.symTab = symTab
        self.literalTab = literalTab
        self.instTab = instTab

        #각 line을 의미별로 분할하고 분석하는 공간
        self.tokenList = list()

        #각 프로그램이 끝날때마다 마지막 주소값을 저장
        self.lastAddr = 0

    def putToken(self, line):
        '''
        일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가하는 함수이다.

	    line : 분리되지 않은 일반 문자열
        '''
        self.tokenList.append(Token(line, self.instTab))

    def getToken(self, index):
        '''
        tokenList에서 index에 해당하는 Token을 리턴한다.

	    index : token을 검색할 index\n
	    return : index번호에 해당하는 코드를 분석한 Token 클래스
        '''
        return self.tokenList[index]

    def makeObjectCode(self, index):
        '''
        Pass2 과정에서 사용하는 함수로 instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	    
        index : object 코드를 생성하고자하는 토큰 인스턴스의 인덱스
        '''
        register = ["A","X","L","B","S","T","F","","PC","SW"]
        tmpToken = self.tokenList[index]

        if tmpToken.type == "DIRECTIVE":
            if tmpToken.directive == "LTORG" : #프로그램에 나왔던 literal값들을 메모리에 저장
                for i in range(0,self.literalTab.getSize()):
                    if self.literalTab.getLiteral(i)[0] == 'X': #literal이 'X'로 시작하면 literal값을 그대로 메모리에 할당
                        tmp = self.literalTab.getLiteral(i)
                        tmp = tmp[2:len(tmp)-1]
                        self.tokenList[index].setObjectCode(tmp)
                        self.tokenList[index].setByteSize(1)
                    else : #literal이 'C'로 시작하면 각 char 값의 ASCII code를 메모리에 할당
                        tmp = self.literalTab.getLiteral(i)
                        tmp = tmp[2:len(tmp)-1]
                        tmpStr = ""
                        for j in range(0,len(tmp)):
                            tmpStr += format(ord(tmp[j]),"02X")
                        self.tokenList[index].setObjectCode(tmpStr)
                        self.tokenList[index].setByteSize(len(tmp))
            
            elif tmpToken.directive == "END": #프로그램에 나왔던 literal값들을 메모리에 저장
                for i in range(0,self.literalTab.getSize()):
                    if self.literalTab.getLiteral(i)[0] == 'X': #literal이 'X'로 시작하면 literal값을 그대로 메모리에 할당
                        tmp = self.literalTab.getLiteral(i)
                        tmp = tmp[2:len(tmp)-1]
                        self.tokenList[index].setObjectCode(tmp)
                        self.tokenList[index].setByteSize(1)
                    else : #literal이 'C'로 시작하면 각 char 값의 ASCII code를 메모리에 할당
                        tmp = self.literalTab.getLiteral(i)
                        tmp = tmp[2:len(tmp)-1]
                        tmpStr = ""
                        for j in range(0,len(tmp)):
                            tmpStr += format(ord(tmp[j]),"02X")
                        self.tokenList[index].setObjectCode(tmpStr)
                        self.tokenList[index].setByteSize(len(tmp))
            
            elif tmpToken.directive == "BYTE":
                tmp = tmpToken.operand[0]
                tmp = tmp[2:len(tmp)-1]
                if tmpToken.operand[0][0] == 'X' or tmpToken.operand[0][0] == 'x':
                    self.tokenList[index].setObjectCode(tmp)
                    self.tokenList[index].setByteSize(1)
                elif tmpToken.operand[0][0] == 'C' or tmpToken.operand[0][0] == 'c':
                    tmpStr = ""
                    for j in range(0,len(tmp)):
                        tmpStr += format(ord(tmp[j]),"02X")
                    self.tokenList[index].setObjectCode(tmp)
                    self.tokenList[index].setByteSize(len(tmp))
            
            elif tmpToken.directive == "WORD":
                if tmpToken.operand[0][0].isdigit(): #숫자라면
                    self.tokenList[index].setObjectCode(format(int(tmpToken.operand[0],16),"s"))
                    self.tokenList[index].setByteSize(1)
                else : #문자열일때의 처리
                    tmpToken.operand = tmpToken.operand[0].split('-')
                    tmp1 = tmpToken.operand[0]
                    tmp2 = tmpToken.operand[1]
                    if self.symTab.searchRefSymbol(tmp1) == 1:
                        self.symTab.modSymbol("+"+ tmp1, tmpToken.location, 6)
                    if self.symTab.searchRefSymbol(tmp2) == 1:
                        self.symTab.modSymbol("-"+tmp2, tmpToken.location, 6)
                    self.tokenList[index].setObjectCode(format(0,"06X"))
                    self.tokenList[index].setByteSize(3)
            
        elif tmpToken.type == "INSTRUCTION":
            op = 252
            opcode = op & tmpToken.instTable.getOpcode(tmpToken.operator)
            opcode += int(tmpToken.getFlag(self.nFlag | self.iFlag)/16)

            if tmpToken.plus_check == 1: #명령어가 4형식일 때
                self.tokenList[index].setObjectCode(format(opcode,"02X")+format(tmpToken.getFlag(15),"01X")+format(0, "05X"))
                self.tokenList[index].setByteSize(4)
                if self.symTab.searchRefSymbol(tmpToken.operand[0]) == 1 :
                    self.symTab.modSymbol("+" + tmpToken.operand[0], tmpToken.location + 1, 5)
                return
            
            if tmpToken.instTable.getFormat(tmpToken.operator) == 1 : #1형식일 때
                self.tokenList[index].setObjectCode(format(opcode, "02X"))
                self.tokenList[index].setByteSize(1)
            elif tmpToken.instTable.getFormat(tmpToken.operator) == 2 : #2형식일 때
                if tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 1: #피연산자의 개수가 1개일 때
                    reg_num = 0
                    for i in range(0,10):
                        if tmpToken.operand[0] == register[i]:
                            reg_num = i
                            break;
                    self.tokenList[index].setObjectCode(format(opcode,"02X")+format(reg_num,"01X")+format(0,"01X"))
                    self.tokenList[index].setByteSize(2)

                elif tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 2: #피연산자의 개수가 2개일 때
                    reg_num1 = 0
                    reg_num2 = 0
                    for i in range(0,10):
                        if tmpToken.operand[0] == register[i]:
                            reg_num1 = i
                        if tmpToken.operand[1] == register[i]:
                            reg_num2 = i
                    self.tokenList[index].setObjectCode(format(opcode,"02X")+format(reg_num1,"01X")+format(reg_num2,"01X"))
                    self.tokenList[index].setByteSize(2)
            elif tmpToken.instTable.getFormat(tmpToken.operator) == 3 : #3형식일 때
                if tmpToken.instTable.getNumberOfOperand(tmpToken.operator) == 0 : #피연산자가 존재하지 않을 때
                    self.tokenList[index].setObjectCode(format(opcode,"02X")+format(0,"04X"))
                    self.tokenList[index].setByteSize(3)
                    return
                else : #피연산자가 존재할 때
                    if tmpToken.operand[0][0] =='#' : #immediate addressing일 때
                        tmpToken.operand[0] = tmpToken.operand[0][1:]
                        self.tokenList[index].setObjectCode(format(opcode, "02X")+format(int(tmpToken.operand[0]),"04X"))
                        self.tokenList[index].setByteSize(3)
                        return
                    if tmpToken.operand[0][0] =='@' : #indirection addressing일 때
                        tmpToken.operand[0] = tmpToken.operand[0][1:]
                    target = 0
                    if tmpToken.operand[0][0] =='=' : #피연산자가 literal일 때 
                        tmpToken.operand[0] = tmpToken.operand[0][1:]
                        target = self.literalTab.search(tmpToken.operand[0])
                    else :
                        target = self.symTab.search(tmpToken.operand[0])
                    
                    pc = self.tokenList[index+1].location
                    addr = target - pc
                    self.tokenList[index].setObjectCode(format(opcode, "02X") + format(tmpToken.getFlag(15),"01X") + format(addr & 0xFFF, "03X"))
                    self.tokenList[index].setByteSize(3)
                    
    def getObjectCode(self, index):
        '''
        index번호에 해당하는 object code를 리턴한다.
	    
        index : objectCode를 검색할 index\n
	    return : object code
        '''
        return self.tokenList[index].objectCode

class Token:
    def __init__(self, line, instTable):
        '''
        클래스를 초기화하면서 바로 line의 의미분석을 수행한다. 

        line = line 문장단위로 저장된 프로그램 코드\n
        instTable = 토큰을 파싱하기 위해 instruction table 링크
        '''
        #의미 분석 단계에서 사용되는 변수들
        self.location = 0
        self.label = ""
        self.operator = ""
        self.operand = []
        self.comment = ""
        self.nixbpe = 0
        self.plus_check = 0 #operand에 +가 포함되어 있는지에 대한 정보를 저장
        self.directive = "" #명령어 라인 중 지시어
        self.type = "" #해당 명령어 라인이 어떤 타입인지 명시
        
        #object code 생성 단계에서 사용되는 변수들 
        self.objectCode = ""
        self.byteSize = 0
        
        #instruction에 대한 정보를 참고하기 위해 링크시킨 instruction table
        self.instTable = instTable
        self.parsing(line)

    def parsing(self, line):
        '''
        line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.

	    line :  문장단위로 저장된 프로그램 코드.
        '''
        token = re.split('\t|\n',line.strip())
        
        if token[0] == '.':
            self.comment = line
            self.type = "COMMENT"
            return
        
        #명령어에 +가 추가되어 있으면 +를 제거한 후 다시 저장
        if '+' in token[0]:
            self.plus_check = 1
            tmpInst = token[0][1:]
            token[0] = tmpInst
        elif len(token) >= 2:
            if '+' in token[1]:
                self.plus_check = 1
                tmpInst = token[1][1:]
                token[1] = tmpInst

        if self.instTable.isInstruction(token[0]) == 1: #첫번째로 잘린 문자열이 명령어라면
            self.operator = token[0]
            if self.instTable.getFormat(self.operator) == 3:
                if self.plus_check == 1:
                    self.setFlag(TokenTable.eFlag, 1) #4형식 명령어이므로 e를 표시
                else : #명령어에 +가 추가되어 있지 않다면 pc relative를 사용할 것이므로 p를 표시
                    self.setFlag(TokenTable.pFlag,1)
            
            numOfOperand = self.instTable.getNumberOfOperand(self.operator) #operand가 있는지 확인
            if numOfOperand != 0: #operand가 있다면
                if numOfOperand == 1: #operand의 개수가 1개라면
                    self.operand.append(token[1])
                    
                    if ',' in token[1]: #BUFFER,x 같이 배열을 쓰는 경우에 토큰분리
                        self.operand = token[1].split(',')
                        if self.operand[1] == "X":
                            self.setFlag(TokenTable.xFlag, 1)
                
                elif numOfOperand == 2: #operand의 개수가 2개라면
                    self.operand = token[1].split(',')

                #addressing방식이
                if self.operand[0][0] == '@': #indirection addressing일 때
                    self.setFlag(TokenTable.nFlag,1)

                elif self.operand[0][0] == '#': #immediate addressing일 때
                    self.setFlag(TokenTable.iFlag,1)
                
                else: #둘 다 아니라면
                    if self.instTable.getFormat(token[0]) == 3: #그런데 명령어의 형식이 3형식/4형식이라면
                        self.setFlag(TokenTable.nFlag,1)
                        self.setFlag(TokenTable.iFlag,1)
            
            else: #operand가 없다면
                if self.instTable.getFormat(token[0]) == 3: #그런데 명령어의 형식이 3형식이라면
                    self.setFlag(TokenTable.nFlag,1)
                    self.setFlag(TokenTable.iFlag,1)

                if len(token) == 2: #코멘트가 존재한다면 저장
                    self.comment = token[1]
            
            if len(token) == 3: #코멘트가 존재한다면 저장
                self.comment = token[2]
            
            self.type = "INSTRUCTION"

        elif self.instTable.isDirective(token[0]) >= 0: #첫번째로 잘린 문자열이 지시어라면
            self.directive = token[0]
            numOfOperand = self.instTable.isDirective(token[0])
            if numOfOperand == 1:
                if ',' in token[1]:
                    self.operand = token[1].split(',')
                else:
                    self.operand.append(token[1])

                if len(token) == 3:
                    self.comment = token[2]
            else :
                if len(token) == 2:
                    self.comment = token[1]
            self.type = "DIRECTIVE"

        else : #첫번째로 잘린 문자열이 label이라면
            self.label = token[0]
            if self.instTable.isInstruction(token[1]) == 1: #두번째로 잘린 문자열이 명령어라면
                self.operator = token[1]
                if self.instTable.getFormat(self.operator) == 3:
                    if self.plus_check == 1:
                        self.setFlag(TokenTable.eFlag, 1) #4형식 명령어이므로 e를 표시
                    else : #명령어에 +가 추가되어 있지 않다면 pc relative를 사용할 것이므로 p를 표시
                        self.setFlag(TokenTable.pFlag,1)
                
                numOfOperand = self.instTable.getNumberOfOperand(self.operator) #operand가 있는지 확인
                if numOfOperand != 0: #operand가 있다면
                    if numOfOperand == 1: #operand의 개수가 1개라면
                        self.operand.append(token[2])
                        
                        if ',' in token[2]: #BUFFER,x 같이 배열을 쓰는 경우에 토큰분리
                            self.operand = token[2].split(',')
                            if self.operand[1] == "X":
                                self.setFlag(TokenTable.xFlag, 1)
                    
                    elif numOfOperand == 2: #operand의 개수가 2개라면
                        self.operand = token[2].split(',')

                    #addressing방식이
                    if self.operand[0][0] == '@': #indirection addressing일 때
                        self.setFlag(TokenTable.nFlag,1)

                    elif self.operand[0][0] == '#': #immediate addressing일 때
                        self.setFlag(TokenTable.iFlag,1)
                    
                    else: #둘 다 아니라면
                        if self.instTable.getFormat(token[1]) == 3: #그런데 명령어의 형식이 3형식/4형식이라면
                            self.setFlag(TokenTable.nFlag,1)
                            self.setFlag(TokenTable.iFlag,1)
                
                else: #operand가 없다면
                    if self.instTable.getFormat(token[1]) == 3: #그런데 명령어의 형식이 3형식이라면
                        self.setFlag(TokenTable.nFlag,1)
                        self.setFlag(TokenTable.iFlag,1)
                    
                    if len(token) == 3: #코멘트가 존재한다면 저장
                        self.commnet = token[2]
                
                if len(token) == 4: #코멘트가 존재한다면 저장
                    self.comment = token[3]
                
                self.type = "INSTRUCTION"

            elif self.instTable.isDirective(token[1]) >= 0: #첫번째로 잘린 문자열이 지시어라면
                self.directive = token[1]
                numOfOperand = self.instTable.isDirective(token[1])
                if numOfOperand == 1:
                    if ',' in token[2]:
                        self.operand = token[1].split(',')
                    else:
                        self.operand.append(token[2])

                    if len(token) == 4:
                        self.comment = token[3]
                else :
                    if len(token) == 3:
                        self.comment = token[2]
                self.type = "DIRECTIVE"

    def setFlag(self, flag, value):
        '''
        n,i,x,b,p,e flag를 설정한다. 
	 
	    flag : 원하는 비트 위치
	    value : 집어넣고자 하는 값. 1을 넣으면 추가, 0을 넣으면 삭제
        '''
        if value == 1 :
            self.nixbpe |= flag
        else :
            self.nixbpe ^= flag


    def getFlag(self, flags):
        '''
        원하는 flag들의 값을 얻어오고자 하는 함수이다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 
	    flags : 값을 확인하고자 하는 비트 위치\n
	    return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴한다
        '''
        return self.nixbpe & flags

    def setLocation(self, loc):
        '''
        loc 값을 인자로 받아와 저장하는 함수이다.

        loc : 저장하고자 하는 location
        '''
        self.location = loc

    def setObjectCode(self, objectCode):
        '''
        object code을 인자로 받아와 저장한다.

        objcode : 저장하고자하는 object code
        '''
        self.objectCode = objectCode

    def setByteSize(self, byteSize):
        '''
        의미분석이 끝난 후 pass2에서 object code로 변형시켰을 때의 ByteSize를 인자로 받아와 저장시키기 위한 함수이다.

        byteSize : 저장하고자하는 byte size
        '''
        self.byteSize = byteSize