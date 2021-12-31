import InstTable
import LiteralTable
import SymbolTable
import TokenTable

class Assembler:
    def __init__(self,instFile):
        '''
        클래스를 초기화하는 함수로 instruction table을 동시에 세팅한다.

        instFile : instruiction에 대한 정보를 가지고 있는 파일 이름
        '''
        self.instTable = InstTable.InstTable(instFile) #명령어에 대한 정보를 저장
        self.lineList = list() #읽어들인 input 파일의 내용을 한줄씩 저장
        self.symtabList = list() #프로그램의 section별로 symboltable을 저장
        self.literaltabList = list() #프로그램의 section별로 literaltable을 저장
        self.TokenList = list() #프로그램이 section별로 프로그램을 저장
        self.codeList = list() #object code를 object program형식에 맞춰 작성한 후 저장
        self.locctr = 0 #주소
        self.sectionNum = 0 #프로그램이 총 몇개의 section으로 구성되어 있는지 저장

    def loadInputFile(self,inputFile):
        '''
        input파일을 읽어들여 lineList에 저장한다.

        inputFile : input파일의 이름
        '''
        file = open(inputFile,'r') #파일 열기
        while True:
            line = file.readline() #파일을 한줄씩 읽어들임
            if not line: #더 이상 읽어들일 라인이 존재하지 않으면 while문 탈출
                break
            self.lineList.append(line) #읽어들인 라인을 lineList에 저장
        file.close() #파일 닫기

    def pass1(self):
        '''
        pass1 과정을 수행하는 함수이다.\n
            1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
            2) label을 symbolTable에 정리\n
        SymbolTable과 TokenTable은 section별로 하나씩 선언하였다.
        '''
        for i in range(0,len(self.lineList)):
            line = self.lineList[i]
            if "START" in line: #프로그램이 시작될 때 tokenTable, symtab, literaltab을 하나 만들어준다.
                self.symtabList.append(SymbolTable.SymbolTable())
                self.literaltabList.append(LiteralTable.LiteralTable())
                self.TokenList.append(TokenTable.TokenTable(self.symtabList[self.sectionNum], self.literaltabList[self.sectionNum], self.instTable))
            if "CSECT" in line:
                self.sectionNum += 1
                self.symtabList.append(SymbolTable.SymbolTable())
                self.literaltabList.append(LiteralTable.LiteralTable())
                self.TokenList.append(TokenTable.TokenTable(self.symtabList[self.sectionNum], self.literaltabList[self.sectionNum], self.instTable))

            self.TokenList[self.sectionNum].putToken(line)

        #토큰분리한 것들을 가지고 loc를 포함한 기타 필요한 정보를 저장
        for i in range(0,self.sectionNum + 1):
            for j in range(0,len(self.TokenList[i].tokenList)):
                tmpToken = self.TokenList[i].getToken(j)
                if tmpToken.type == "DIRECTIVE":
                    if tmpToken.directive == "START": #프로그램이 시작될 때
                        self.locctr = int(tmpToken.operand[0])
                        self.TokenList[i].tokenList[j].setLocation(self.locctr)
                        self.symtabList[i].putSymbol(tmpToken.label, self.locctr)
                        continue
                    elif tmpToken.directive == "CSECT": #sub프로그램이 시작할 때
                        self.TokenList[i-1].lastAddr = self.locctr
                        self.locctr = 0
                        self.TokenList[i].tokenList[j].setLocation(self.locctr)
                        self.symtabList[i].putSymbol(tmpToken.label, self.locctr)
                        continue
                    elif tmpToken.directive == "EXTDEF": #EXTDEF 뒤에 나오는 값들 저장
                        for k in range(0, len(tmpToken.operand)):
                            self.symtabList[i].setDefSymbol(tmpToken.operand[k])
                    elif tmpToken.directive == "EXTREF": #EXTREF 뒤에 나오는 값들 저장
                        for k in range(0, len(tmpToken.operand)):
                            self.symtabList[i].setRefSymbol(tmpToken.operand[k])
                
                if tmpToken.label != "":
                    self.symtabList[i].putSymbol(tmpToken.label, self.locctr)

                self.TokenList[i].tokenList[j].setLocation(self.locctr)

                if tmpToken.type == "INSTRUCTION":
                    if tmpToken.plus_check == 1 : #명령어의 형식에 맞춰 locctr값 증가
                        self.locctr += 4
                    else :
                        self.locctr += tmpToken.instTable.getFormat(tmpToken.operator)

                    if len(tmpToken.operand) != 0 :
                        if '=' in tmpToken.operand[0] : #indirection addressing일 때
                            self.literaltabList[i].putLiteral(tmpToken.operand[0], -2)

                elif tmpToken.type == "DIRECTIVE":
                    if tmpToken.directive == "WORD": #locctr을 3 증가시킴
                        self.locctr += 3
                    elif tmpToken.directive == "RESW": #locctr을 (3 * 피연산자값)만큼 증가시킴
                        self.locctr += 3 * int(tmpToken.operand[0])
                    elif tmpToken.directive == "RESB": #locctr을 피연산자값만큼 증가시킴
                        self.locctr += int(tmpToken.operand[0])
                    elif tmpToken.directive == "BYTE": #locctr을 1 증가시킴
                        self.locctr += 1
                    elif tmpToken.directive == "EQU":
                        if tmpToken.operand[0] == "*": #피연산자가 *인 경우
                            self.symtabList[i].modifySymbol(tmpToken.label, self.locctr) #현재 locctr의 값을 주소로 저장
                        else :# 그렇지 않은 경우 피연산자를 통해 주소값을 계산하여 저장
                            if '-' in tmpToken.operand[0]:
                                tmpToken.operand = tmpToken.operand[0].split('-')
                                self.symtabList[i].modifySymbol(tmpToken.label, self.symtabList[i].search(tmpToken.operand[0]) -  self.symtabList[i].search(tmpToken.operand[1]))
                                self.TokenList[i].tokenList[j].setLocation(self.symtabList[i].search(tmpToken.operand[0]) -  self.symtabList[i].search(tmpToken.operand[1]))
                    elif tmpToken.directive == "LTORG": #프로그램에 나왔던 literal을 저장
                        self.locctr = self.literaltabList[i].addrLiteralTab(self.locctr)
                    elif tmpToken.directive == "END" : #프로그램에 나왔던 literal을 저장하고 마지막 주소값을 넣어줌
                        self.locctr = self.literaltabList[i].addrLiteralTab(self.locctr)
                        self.TokenList[i].lastAddr = self.locctr
        pass


    def printSymbolTable(self,fileName):
        '''
        작성된 SymbolTable들을 출력형태에 맞게 출력한다.

	    fileName : 저장되는 파일 이름
        '''
        file = open(fileName, 'w') #파일 열기
        for i in range(0, len(self.symtabList)):
            for j in range(0, self.symtabList[i].getSize()):
                data = self.symtabList[i].getSymbol(j) + '\t\t\t' + format(self.symtabList[i].getAddress(j),"X") + '\n'
                file.write(data)
            file.write('\n')

        file.close() #파일 닫기

    def printLiteralTable(self,fileName):
        '''
        작성된 LiteralTable들을 출력형태에 맞게 출력한다.

	    fileName : 저장되는 파일 이름
        '''
        file = open(fileName, 'w') #파일 열기
        for i in range(0, len(self.literaltabList)):
            for j in range(0, self.literaltabList[i].getSize()):
                if self.literaltabList[i].getSize() == 0 :
                    continue

                tmpStr = self.literaltabList[i].getLiteral(j)
                tmpStr = tmpStr[2:len(tmpStr)-1]
                data = tmpStr + '\t\t\t' + format(self.literaltabList[i].getAddress(j),"X") + '\n'
                file.write(data)
        file.close() #파일 닫기

    def pass2(self):
        '''
        pass2 과정을 수행한다.\n
            1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
        '''
        #토큰분리한 것들을 가지고 각 라인별 object code 작성
        for i in range(0, len(self.TokenList)):
            for j in range(0,len(self.TokenList[i].tokenList)):
                self.TokenList[i].makeObjectCode(j)
    
        check = 1
        line_max = 0
        line_check = 0
        line = 0
        textTmp = ""
        code = ""
        num = 0
        #프로그램의 object program을 작성하여 codeList에 저장
        for i in range(0,len(self.TokenList)):
            notFinish = -1
            for j in range(0,len(self.TokenList[i].tokenList)):
                tmpToken = self.TokenList[i].getToken(j)

                #Header record 작성
                if tmpToken.type == "DIRECTIVE":
                    if tmpToken.directive == "START" or tmpToken.directive == "CSECT":
                        startAddr = self.TokenList[i].getToken(0).location
                        name = self.TokenList[i].getToken(0).label
                        lastAddr = self.TokenList[i].lastAddr
                        self.codeList.append( "H"+ ("%-6s" %name) + format(startAddr,"06X") + format(lastAddr, "06X"))
                        continue

                    #Define record 작성
                    if tmpToken.directive == "EXTDEF":
                        tmp = "D"
                        for k in range(0,len(self.symtabList[i].defList)):
                            tmp += ("%-6s" %self.symtabList[i].defList[k]) + format(self.symtabList[i].search(self.symtabList[i].defList[k]),"06X")
                        self.codeList.append(tmp)
                        continue

                    #Refer record 작성
                    if tmpToken.directive == "EXTREF":
                        tmp = "R"
                        for k in range(0,len(self.symtabList[i].refList)):
                            tmp += ("%-6s" %self.symtabList[i].refList[k])
                        self.codeList.append(tmp)
                        continue
                
                #Text record 작성
                if tmpToken.objectCode != '' :
                    line_check = 0
                    if check == 1:
                        textTmp = "T" + format(self.TokenList[i].getToken(0).location + line ,"06X")
                        check = 0
                        notFinish = 2
                    
                    num += tmpToken.byteSize
                    if num > 30: #현재까지의 byte의 개수가 한줄에 쓸수 있는 분량보다 많다면
                        num -= tmpToken.byteSize
                        check = 1
                        textTmp += format(num, "02X") + code
                        self.codeList.append(textTmp)
                        line += num
                        textTmp = ""
                        code = ""
                        line_max = 1
                        num = 0
                        if line_max == 1:
                            code = self.TokenList[i].getToken(j).objectCode.format("s")
                            num += tmpToken.byteSize
                        continue

                    else :
                        if notFinish == 1:
                            textTmp = "T" + format(self.TokenList[i].getToken(j).location, "06X")
                        
                        if line_max == 1:
                            textTmp = "T" + format(self.TokenList[i].getToken(0).location + line, "06X")
                            line_max = 0
                        
                        if code != '':
                            code += self.TokenList[i].getToken(j).objectCode.format("s")
                        else :
                            code = self.TokenList[i].getToken(j).objectCode.format("s")
                        line_check = 1
                    continue
                else :
                    if line_check == 1:
                        if notFinish == 2:
                            if textTmp == '':
                                continue
                            notFinish = 1
                            textTmp += format(num,"02X") + code
                            self.codeList.append(textTmp)
                            line += num
                            textTmp = ""
                            code = ""
                            num = 0

            if line_check == 1:
                textTmp += format(num, "02X") + code
                self.codeList.append(textTmp)
                textTmp = ""
                code = ""
                num = 0
                line = 0
                check = 1
            
            #Modification record 작성
            if len(self.symtabList[i].refList) != 0:
                for k in range(0,len(self.symtabList[i].modList)):
                    tmp = "M" + format(self.symtabList[i].modLocationList[k],"06X") + format(self.symtabList[i].modSize[k], "02X") + format(self.symtabList[i].modList[k])
                    self.codeList.append(tmp)

            #End record 작성
            if i == 0 :
                tmp = "E" + format(self.TokenList[i].getToken(0).location, "06X")
                self.codeList.append(tmp)
            else :
                self.codeList.append("E")        
        pass

    def printObjectCode(self,fileName):
        '''
        작성된 codeList를 출력형태에 맞게 출력한다.
	
        fileName : 저장되는 파일 이름
        '''
        file = open(fileName, 'w') #파일 열기
        for i in range(0, len(self.codeList)):
            file.write(self.codeList[i]+"\n")

            if self.codeList[i][0] == 'E':
                file.write("\n")

        file.close() #파일 닫기

#이 프로그램의 메인 루틴이다.
if __name__ == '__main__': 
    assembler = Assembler("inst.data")
    assembler.loadInputFile("input.txt")
    assembler.pass1()
    assembler.printSymbolTable("symtab_20160433.txt")
    assembler.printLiteralTable("literaltab_20160433.txt")
    assembler.pass2()
    assembler.printObjectCode("output_20160433.txt")
