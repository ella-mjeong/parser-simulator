class InstTable:
    def __init__(self, instFile):
        '''
        클래스를 초기화하는 함수로 명령어에 대한 정보를 파싱하는 과정을 동시에 처리한다.
        명령어에 대한 정보는 변수 instMap에 저장한다.

        instFile : 명령어에 대한 정보를 저장하고 있는 파일의 이름
        '''
        self.instMap = dict() #명령어에 대한 정보를 저장
        self.directive = {"START" : 1,"END" : 1,"BYTE" : 1,"WORD" : 1,"RESB" : 1,"RESW" : 1,"CSECT" : 0,"EXTDEF" : 1,"EXTREF" : 1,"EQU" : 1,"ORG" : 1,"LTORG" : 0} #지시어에 대한 정보를 저장
        self.openFile(instFile)


    def openFile(self,fileName):
        '''
        전달받은 이름의 파일을 열고 파일 안에 저장되어 있던 내용을 파싱하여 instMap에 저장한다.

        instFile : 명령어에 대한 정보를 저장하고 있는 파일의 이름

        '''
        file = open(fileName,'r') #파일 열기
        while True:
            line = file.readline() #파일을 한줄씩 읽어들임
            if not line: #더 이상 읽어들일 라인이 존재하지 않으면 while문 탈출
                break
            token = Instruction(line) #읽어들인 라인의 정보를 토큰분리한 값을 저장
            self.instMap[token.instruction] = token #instruction 이름을 key로 저장하고 해당하는 instruction에 대한 정보를 value로 저장
        file.close() #파일 닫기

    def getOpcode(self,instruction):
        '''
        인자로 받은 명령어의 opcode를 찾아서 리턴하는 함수이다.

        instruction : opcode를 알고 싶은 명령어
        return : 해당 명령어의 opcode
        '''
        info = self.instMap.get(instruction) #주어진 문자열이 key값으로 존재한다면 해당하는 키값의 value를 리턴
        if info == 'None': # 주어진 문자열이 key값으로 존재하지 않을 때 -1리턴
            return -1
        else : # 주어진 문자열이 key값으로 존재할 때 해당하는 키값의 value의 opcode를 리턴
            return info.opcode

    def getNumberOfOperand(self,instruction):
        '''
        인자로 받은 명령어의 피연산자 개수를 찾아서 리턴하는 함수이다.

        instruction : 피연산자 개수를 알고 싶은 명령어
        return : 해당 명령어의 피연산자 개수
        '''
        info = self.instMap.get(instruction) #주어진 문자열이 key값으로 존재한다면 해당하는 키값의 value를 리턴
        if info == 'None': # 주어진 문자열이 key값으로 존재하지 않을 때 -1리턴
            return -1
        else : # 주어진 문자열이 key값으로 존재할 때 해당하는 키값의 value의 opcode를 리턴
            return info.numberOfOperand

    def getFormat(self,instruction):
        '''
        인자로 받은 명령어의 형식를 찾아서 리턴하는 함수이다.

        instruction : 형식를 알고 싶은 명령어
        return : 해당 명령어의 형식
        '''
        info = self.instMap.get(instruction) #주어진 문자열이 key값으로 존재한다면 해당하는 키값의 value를 리턴
        if info == 'None': # 주어진 문자열이 key값으로 존재하지 않을 때 -1리턴
            return -1
        else : # 주어진 문자열이 key값으로 존재할 때 해당하는 키값의 value의 opcode를 리턴
            return info.format

    def isInstruction(self,str):
        '''
        입력문자열이 명령어인지 검사하는 함수이다.

        str : 명령어인지 검사하고 싶은 문자열
        return : 명령어가 맞다면 1, 아니라면 -1을 리턴
        '''
        if str in self.instMap: #주어진 문자열이 key값으로 존재한다면 1 리턴
            return 1
        else : #주어진 문자열이 key값으로 존재하지 않는다면 -1 리턴
            return -1 

    def isDirective(self,str):
        '''
        입력문자열이 지시어인지 검사하는 함수이다.

        str : 지시어인지 검사하고 싶은 문자열
        return : 지시어가 맞다면 지시어 뒤에 몇 개의 정보가 포함되어있는지(0,1)를 리턴하고 아니라면 -1을 리턴
        '''
        if str in self.directive: #주어진 문자열이 key값으로 존재한다면 1 리턴
            return self.directive[str]
        else : #주어진 문자열이 key값으로 존재하지 않는다면 -1 리턴
            return -1
        
class Instruction:
    def __init__(self, line):
        '''
        클래스를 선언 및 초기화하면서 동시에 명령어의 구조에 맞게 파싱하여 정보를 저장한다.

        line : 명령어의 정보가 포함된 파일로부터 한 줄씩 읽어들인 문자열
        '''
        self.instruction = "" #명령어의 이름
        self.opcode = 0 #명령어의 opcode 값
        self.numberOfOperand = 0 # 명령어가 몇개의 피연산자를 가지고 있는지
        self.format = 0 #명령어의 형식
        self.parsing(line) 

    def parsing(self,line):
        '''
        전달된 문자열을 파싱하여 instruction에 대한 정보를 저장한다.

        line : 명령어의 정보가 포함된 파일로부터 한 줄씩 읽어들인 문자열
        '''
        token = line.split()
        self.instruction = token[0]
        self.numberOfOperand = int(token[1])
        self.format = int(token[2])
        self.opcode = int(token[3],16)