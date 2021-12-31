/*
 * 화일명 : my_assembler_20160433.c 
 * 설  명 : 이 프로그램은 SIC/XE 머신을 위한 간단한 Assembler 프로그램의 메인루틴으로,
 * 입력된 파일의 코드 중, 명령어에 해당하는 OPCODE를 찾아 출력한다.
 * 파일 내에서 사용되는 문자열 "00000000"에는 자신의 학번을 기입한다.
 */

/*
 *
 * 프로그램의 헤더를 정의한다. 
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <ctype.h>

// 파일명의 "00000000"은 자신의 학번으로 변경할 것.
#include "my_assembler_20160433.h"
#pragma warning(disable:4996)
#pragma warning(disable:4018)
#pragma warning(disable:4047)
#pragma warning(disable:4024)

/* ----------------------------------------------------------------------------------
 * 설명 : 사용자로 부터 어셈블리 파일을 받아서 명령어의 OPCODE를 찾아 출력한다.
 * 매계 : 실행 파일, 어셈블리 파일 
 * 반환 : 성공 = 0, 실패 = < 0 
 * 주의 : 현재 어셈블리 프로그램의 리스트 파일을 생성하는 루틴은 만들지 않았다. 
 *		   또한 중간파일을 생성하지 않는다. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{

	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: 프로그램 초기화에 실패 했습니다.\n");
		return -1;
	}

	if (assem_pass1() < 0)
	{
		printf("assem_pass1: 패스1 과정에서 실패하였습니다.  \n");
		return -1;
	}

	//make_symtab_output(NULL); //symbol table을 화면에 출력
	make_symtab_output("symtab_20160433.txt"); // symbol table을 파일로 출력
	//make_literaltab_output(NULL); //literal table을 화면에 출력
	make_literaltab_output("literaltab_20160433.txt"); // literal table을 파일로 출력

	if (assem_pass2() < 0) //object code 생성
	{
		printf("assem_pass2: 패스2 과정에서 실패하였습니다.  \n");
		return -1;
	}

	//make_objectcode_output(NULL); //결과를 화면에 출력
	make_objectcode_output("output_20160433.txt"); // 결과를 파일로 출력

	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 프로그램 초기화를 위한 자료구조 생성 및 파일을 읽는 함수이다. 
 * 매계 : 없음
 * 반환 : 정상종료 = 0 , 에러 발생 = -1
 * 주의 : 각각의 명령어 테이블을 내부에 선언하지 않고 관리를 용이하게 하기 
 *		   위해서 파일 단위로 관리하여 프로그램 초기화를 통해 정보를 읽어 올 수 있도록
 *		   구현하였다. 
 * ----------------------------------------------------------------------------------
 */
int init_my_assembler(void)
{
	int result;

	if ((result = init_inst_file("inst.data")) < 0)
		return -1;
	if ((result = init_input_file("input.txt")) < 0)
		return -1;
	return result;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 머신을 위한 기계 코드목록 파일을 읽어 기계어 목록 테이블(inst_table)을 
 *        생성하는 함수이다. 
 * 매계 : 기계어 목록 파일
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : 기계어 목록파일 형식은 자유롭게 구현한다. 예시는 다음과 같다.
 *	
 *	===============================================================================
 *		 | 이름 | 오퍼랜드의 갯수 | 형식 | 기계어 코드 | NULL |
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
	FILE *file;
	int errno;

	/* add your code here */
	file = fopen(inst_file, "r"); //읽기모드로 파일을 연다

	if (file == NULL) { //파일 열기에 실패하면 에러
		errno = -1;
	}
	else {
		while (1) {//동적할당 후 기계어 목록파일의 형식에 따라 해당하는 값들을 기계아 목록 테이블에 저장.
			inst_table[inst_index] = (inst*)malloc(sizeof(inst));
			memset(inst_table[inst_index], 0, sizeof(inst));

			fscanf(file, "%s	%d	%d	%x", &inst_table[inst_index]->instruction, &inst_table[inst_index]->operand_num, &inst_table[inst_index]->format, &inst_table[inst_index]->opcode);
			inst_index++;

			if (inst_index == 59)
				break;
		}
		fclose(file);
	}

	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 어셈블리 할 소스코드를 읽어 소스코드 테이블(input_data)를 생성하는 함수이다. 
 * 매계 : 어셈블리할 소스파일명
 * 반환 : 정상종료 = 0 , 에러 < 0  
 * 주의 : 라인단위로 저장한다.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE *file;
	int errno;

	/* add your code here */
	int buf_size = 100;

	file = fopen(input_file, "r"); //읽기모드로 파일을 연다
	if (file == NULL) { //파일 열기에 실패하면 에러
		errno = -1;
	}
	else {
		while (1) {//동적할당 후 파일을 라인단위로 읽어 소스코드 테이블에 저장
			input_data[line_num] = (char*)malloc(sizeof(char)*buf_size);
			memset(input_data[line_num], 0, buf_size);

			if (fgets(input_data[line_num], buf_size, file) == NULL) {//파일의 끝에 도달하면 while문 탈출
				break;
			}

			if (input_data[line_num][strlen(input_data[line_num]) - 1] == '\n') {//라인의 끝에 존재하는 '\n'를 '\0'로 대체
				input_data[line_num][strlen(input_data[line_num]) - 1] = '\0';
			}

			line_num++;
		}
		fclose(file);
	}
	return errno;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 소스 코드를 읽어와 토큰단위로 분석하고 토큰 테이블을 작성하는 함수이다. 
 *        패스 1로 부터 호출된다. 
 * 매계 : 파싱을 원하는 문자열  
 * 반환 : 정상종료 = 0 , 에러 < 0 
 * 주의 : my_assembler 프로그램에서는 라인단위로 토큰 및 오브젝트 관리를 하고 있다. 
 * ----------------------------------------------------------------------------------
 */
int token_parsing(char *str)
{
	/* add your code here */
	char *tmp_str;
	char *tok;
	int inst_num;
	int tok_num;
	int dir_num;
	int inst_check = 0;

	tmp_str = (char*)malloc(sizeof(char));

	tmp_str = str;
	token_table[token_line] = (token*)malloc(sizeof(token));
	memset(token_table[token_line], 0, sizeof(token));

	token_table[token_line]->label = NULL;
	token_table[token_line]->operator =  NULL;
	token_table[token_line]->operand[0] = NULL;
	token_table[token_line]->operand[1] = NULL;
	token_table[token_line]->comment = NULL;
	token_table[token_line]->directive = NULL;

	if (tmp_str[0] == '.') {//입력문자열이 주석이라면 토큰테이블의 comment변수에 저장
		token_table[token_line]->comment = (char*)malloc(sizeof(char));
		token_table[token_line]->comment = tmp_str;
		token_table[token_line]->type = T_COMMENT;
		token_line++;
		return 0;
	}

	tok = strtok(tmp_str, " \t");//" \t"라는 구분자로 토큰분리

	if (tok == NULL)//분리할 문자열이 없으면 리턴
		return 0;

	if (tok[0] == '+') {//기계어에 +가 추가되어 있으면
		inst_check = 1;
		char inst_four[100];
		strcpy(inst_four, tok);
		int size = sizeof(tok);
		for (int i = 0; i < size; i++) {
			tok[i] = inst_four[i + 1];
		}
		tok[size] = '\0';
	}
	if ((inst_num = search_opcode(tok)) >= 0) {//잘린 문자열이 기계어라면
		token_table[token_line]->operator =  (char*)malloc(sizeof(char));
		token_table[token_line]->operator=tok;
		token_table[token_line]->op_index = inst_num;
		if (inst_table[token_table[token_line]->op_index]->format == 3) {
			if (inst_check) {
				//기계어에 +가 추가되어 있으면 토큰테이블에 따로 저장
				token_table[token_line]->plus_check = 1;
				//4형식명령어이므로 e를 표시
				token_table[token_line]->nixbpe |= E;
			}
			else {
				//기계어에 +가 추가되어 있지 않다면 pc relative를 사용할 것이므로 p를 표시
				token_table[token_line]->nixbpe |= P;
			}
		}
		if ((tok_num = inst_table[inst_num]->operand_num) != 0) {//operand가 있다면
			token_table[token_line]->operand[0] = (char*)malloc(sizeof(char));
			if (tok_num == 1) {//operand 개수가 1개일 때
				tok = strtok(NULL, " \t");
				token_table[token_line]->operand[0] = tok;
				if (strstr(tok, ",") != NULL) {//BUFFER,x 같이 배열을 쓰는 경우에 토큰분리
					token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
					memset(token_table[token_line]->operand[1], 0, 10);
					token_table[token_line]->operand[0] = strtok_s(tok, ",", &token_table[token_line]->operand[1]);
					if (!strcmp(token_table[token_line]->operand[1], "X")) {
						token_table[token_line]->nixbpe |= X;
					}
				}

			}
			else {//operand 개수가 2개일 때
				tok = strtok(NULL, ",");
				token_table[token_line]->operand[0] = tok;
				tok = strtok(NULL, " \t");
				token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
				token_table[token_line]->operand[1] = tok;
			}

			//addressing 방식이
			if (strstr(token_table[token_line]->operand[0], "@") != NULL) {//indirection addressing일 때
				token_table[token_line]->nixbpe |= N;
			}
			else if (strstr(token_table[token_line]->operand[0], "#") != NULL) {//immediate addressing일 때
				token_table[token_line]->nixbpe |= I;
				token_table[token_line]->nixbpe ^= P;
			}
			else {// 둘다 아니라면
				if (inst_table[inst_num]->format == 3) { //그런데 기계어의 형식이 3형식/4형식이라면
					token_table[token_line]->nixbpe |= N;
					token_table[token_line]->nixbpe |= I;
				}
			}

		}
		else {//operand가 없는 3형식 명령어라면
			if (inst_table[inst_num]->format == 3) { 
				token_table[token_line]->nixbpe |= N;
				token_table[token_line]->nixbpe |= I;
			}
		}

		if (tok != NULL) { //comment 분리
			token_table[token_line]->comment = (char*)malloc(sizeof(char));
			tok = strtok(NULL, "\0");
			token_table[token_line]->comment = tok;
		}
		token_table[token_line]->type = T_INSTRUCTION;
	}
	else if ((dir_num = search_directive(tok)) >= 0) {//잘린 문자열이 지시어라면
		token_table[token_line]->directive = (char*)malloc(sizeof(char));
		token_table[token_line]->directive = tok;
		if (dir_num == 1) {//지시어 뒤에 다른 정보가 포함되어 있을 때
			token_table[token_line]->operand[0] = (char*)malloc(sizeof(char));
			tok = strtok(NULL, " \t");
			token_table[token_line]->operand[0] = tok;
			if (strstr(tok, ",") != NULL) {
				token_table[token_line]->operand[0] = strtok(tok, ",");
				token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
				token_table[token_line]->operand[1] = strtok(NULL, ",");
				if (tok != NULL) {
					token_table[token_line]->operand[2] = (char*)malloc(sizeof(char));
					token_table[token_line]->operand[2] = strtok(NULL, ",");
				}
			}

		}

		if (tok != NULL) { //comment 분리
			token_table[token_line]->comment = (char*)malloc(sizeof(char));
			tok = strtok(NULL, "\0");
			token_table[token_line]->comment = tok;
		}
		token_table[token_line]->type = T_DIRECTIVE;
	}
	else {//잘린 문자열이 label이라면
		token_table[token_line]->label = (char*)malloc(sizeof(char));
		token_table[token_line]->label = tok;
		tok = strtok(NULL, " \t");

		if (tok[0] == '+') {//기계어코드에 +가 추가되어 있으면
			inst_check = 1;
			char inst_four[100];
			strcpy(inst_four, tok);
			int size = sizeof(tok);
			for (int i = 0; i < size; i++) {
				tok[i] = inst_four[i + 1];
			}
			tok[size] = '\0';
		}

		if ((inst_num = search_opcode(tok)) >= 0) {//잘린 문자열이 기계어코드라면
			token_table[token_line]->operator =  (char*)malloc(sizeof(char));
			token_table[token_line]->operator = tok;
			token_table[token_line]->op_index = inst_num;

			if (inst_table[token_table[token_line]->op_index]->format == 3) {
				if (inst_check) {
					//+가 붙은 명령어라면 토큰테이블에 따로 저장
					token_table[token_line]->plus_check = 1;
					//4형식명령어이므로 e를 표시
					token_table[token_line]->nixbpe |= E;
				}
				else {
					//+가 붙은 명령어가 아니라면 pc relative를 사용할 것이므로 p를 표시
					token_table[token_line]->nixbpe |= P;
				}
			}

			if ((tok_num = inst_table[inst_num]->operand_num) != 0) {//operand가 있다면
				token_table[token_line]->operand[0] = (char*)malloc(sizeof(char));
				if (tok_num == 1) { //operand의 개수가 1개일 때
					tok = strtok(NULL, " \t");
					token_table[token_line]->operand[0] = tok;
					if (strstr(tok, ",") != NULL) { //BUFFER,x 같이 배열을 쓰는 경우에 토큰분리
						token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
						memset(token_table[token_line]->operand[1], 0, 10);
						token_table[token_line]->operand[0] = strtok_s(tok, ",", &token_table[token_line]->operand[1]);

						if (!strcmp(token_table[token_line]->operand[1], "X")) {
							token_table[token_line]->nixbpe |= X;
						}
					}
				}
				else { //operand 개수가 2개일 때
					tok = strtok(NULL, ",");
					token_table[token_line]->operand[0] = tok;
					tok = strtok(NULL, " \t");
					token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
					token_table[token_line]->operand[1] = tok;
				}
				//addressing 방식이
				if (strstr(token_table[token_line]->operand[0], "@") != NULL) {//indirection addressing일 때
					token_table[token_line]->nixbpe |= N;
				}
				else if (strstr(token_table[token_line]->operand[0], "#") != NULL) {//immediate addressing일 때
					token_table[token_line]->nixbpe |= I;
					token_table[token_line]->nixbpe ^= P;
				}
				else {// 둘다 아니라면
					if (inst_table[inst_num]->format == 3) { //그런데 기계어의 형식이 3형식/4형식이라면
						token_table[token_line]->nixbpe |= N;
						token_table[token_line]->nixbpe |= I;
					}
				}
			}
			else {//operand가 없는 3형식 명령어라면
				if (inst_table[inst_num]->format == 3) {
					token_table[token_line]->nixbpe |= N;
					token_table[token_line]->nixbpe |= I;
				}
			}
			token_table[token_line]->type = T_INSTRUCTION;
		}
		else if ((dir_num = search_directive(tok)) >= 0) {//잘린 문자열이 지시어라면
			token_table[token_line]->directive = (char*)malloc(sizeof(char));
			token_table[token_line]->directive = tok;
			if (dir_num == 1) {//지시어 뒤에 다른 정보가 포함되어 있을 때
				token_table[token_line]->operand[0] = (char*)malloc(sizeof(char));
				tok = strtok(NULL, " \t");
				token_table[token_line]->operand[0] = tok;
				if (strstr(tok, ",") != NULL) {
					token_table[token_line]->operand[0] = strtok(tok, ",");
					token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
					token_table[token_line]->operand[1] = strtok(NULL, ",");
					if (tok != NULL) {
						token_table[token_line]->operand[2] = (char*)malloc(sizeof(char));
						token_table[token_line]->operand[2] = strtok(NULL, ",");
					}
				}
			}
			token_table[token_line]->type = T_DIRECTIVE;
		}
		if (tok != NULL) { //comment 분리
			token_table[token_line]->comment = (char*)malloc(sizeof(char));
			tok = strtok(NULL, "\0");
			token_table[token_line]->comment = tok;
		}
	}

	token_line++;
	return 0;
}

/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 기계어 코드인지를 검사하는 함수이다. 
 * 매계 : 토큰 단위로 구분된 문자열 
 * 반환 : 정상종료 = 기계어 테이블 인덱스, 에러 < 0 
 * 주의 : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str)
{
	/* add your code here */
	for (int i = 0; i < inst_index; i++) {
		if (!strcmp(inst_table[i]->instruction, str)) { //입력된 문자열이 inst_table의 명령어와 일치한다면 inst_table의 index를 반환
			return i;
		}
	}
	return -1;
}

//추가한 함수
/* ----------------------------------------------------------------------------------
 * 설명 : 입력 문자열이 지시어인지를 검사하는 함수이다.
 * 매계 : 토큰 단위로 구분된 문자열
 * 반환 : 정상종료 >= 0, 에러 < 0
 * 주의 :
 *
 * ----------------------------------------------------------------------------------
 */
int search_directive(char *str)
{
	char *directive_table[] = { "START","END","BYTE","WORD","RESB","RESW","CSECT","EXTDEF","EXTREF","EQU","ORG","LTORG" };
	int directive_num[] = { 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0 };

	for (int i = 0; i < sizeof(directive_num) / sizeof(int); i++) {
		if (!strcmp(directive_table[i], str)) { //입력된 문자열이 directive_table의 지시어와 일치한다면 뒤에 정보가 포함되어 있는 지시어라면 1, 아니라면 0 리턴
			return directive_num[i];
		}
	}
	return -1;
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 위한 패스1과정을 수행하는 함수이다.
*		   패스1에서는..
*		   1. 프로그램 소스를 스캔하여 해당하는 토큰단위로 분리하여 프로그램 라인별 토큰
*		   테이블을 생성한다.
*
* 매계 : 없음
* 반환 : 정상 종료 = 0 , 에러 = < 0
* 주의 : 현재 초기 버전에서는 에러에 대한 검사를 하지 않고 넘어간 상태이다.
*	  따라서 에러에 대한 검사 루틴을 추가해야 한다.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
	/* add your code here */

	for (int i = 0; i < line_num; i++) { //입력파일에서 라인별로 읽어들인 소스들을 각각 토큰분리시킨다.
		if (token_parsing(input_data[i]) < 0) {
			return -1;
		}
	}

	now_area = (char*)malloc(sizeof(char));

	//토큰 분리한 것들을 가지고 loc를 포함한 기타 필요한 정보들 저장
	for (int i = 0; i < token_line; i++) { 
		if (token_table[i]->type == T_DIRECTIVE) {
			if (!strcmp(token_table[i]->directive ,"START")) { //프로그램이 시작한다면
				locctr = atoi(token_table[i]->operand[0]);
				token_table[i]->address = locctr;
				now_area = token_table[i]->label;
				strcpy(token_table[i]->section, now_area);
				if (make_symtab(token_table[i]->label) < 0) {
					fprintf(stderr,"duplicate symbol error\n");
					return -1;
				}
				continue;
			}
			else if (!strcmp(token_table[i]->directive, "CSECT")) { //새로운 sub program이 시작된다면
				control_section[cs_num].lastAddr = locctr; // 이전 프로그램의 마지막 주소값을 저장
				cs_num++;
				locctr = 0;
				token_table[i]->address = locctr;
				now_area = token_table[i]->label;
				strcpy(token_table[i]->section, now_area);
				if (make_symtab(token_table[i]->label) < 0) {
					fprintf(stderr, "duplicate symbol error\n");
					return -1;
				}
				continue;
			}
		}

		if (token_table[i]->label != NULL) {
			if (make_symtab(token_table[i]->label) < 0) {
				fprintf(stderr, "duplicate symbol error\n");
				return -1;
			}
		}
		token_table[i]->address = locctr;
		strcpy(token_table[i]->section, now_area);

		if (token_table[i]->type == T_INSTRUCTION) {
			if (token_table[i]->plus_check) { //명령어의 형식에 맞춰 locctr값 증가
				locctr += 4;
			}
			else {
				locctr += inst_table[token_table[i]->op_index]->format;
			}
			if (token_table[i]->operand[0] != NULL) {
				if (strstr(token_table[i]->operand[0], "=") != NULL) {//indirection addressing일 때
					make_literaltab(token_table[i]->operand[0]);
				}
			}
		}
		else if (token_table[i]->type == T_DIRECTIVE){
			if (!strcmp(token_table[i]->directive, "WORD")) {//locctr을 3 증가시킴
				locctr += 3;
			}
			else if (!strcmp(token_table[i]->directive, "RESW")) {//locctr을 (3 * 피연산자값)만큼 증가시킴
				locctr += 3* atoi(token_table[i]->operand[0]);
			}
			else if (!strcmp(token_table[i]->directive, "RESB")) {//locctr을 피연산자값만큼 증가시킴
				locctr += atoi(token_table[i]->operand[0]);
			}
			else if (!strcmp(token_table[i]->directive, "BYTE")) { //locctr을 1 증가시킴
				locctr += 1;
			}
			else if (!strcmp(token_table[i]->directive, "EQU")){
				if (!strcmp(token_table[i]->operand[0], "*")) {// 피연산자가 *인 경우 
					sym_table[sym_num-1].addr = locctr; //현재 locctr의 값을 주소로 저장
				}
				else {  // 그렇지 않은 경우 피연산자를 통해 주소값을 계산하여 저장
					if (strstr(token_table[i]->operand[0], "-")) {
						char op_t[100];
						strcpy(op_t, token_table[i]->operand[0]);
						char* op1 = (char*)malloc(sizeof(char));
						char* op2 = (char*)malloc(sizeof(char));
						memset(op1, 0, 10);
						memset(op2, 0, 10);
						op1 = strtok_s(op_t, "-", &op2);
						sym_table[sym_num-1].addr = search_symtab(op1) - search_symtab(op2);
						token_table[i]->address = sym_table[sym_num - 1].addr;
						sym_table[sym_num - 1].type = 'A';
					}
				}
			}
			else if (!strcmp(token_table[i]->directive, "LTORG")) { //프로그램에 나왔던 literal을 저장
				addr_literal_tab();
			}
			else if (!strcmp(token_table[i]->directive, "END")) { //프로그램에 나왔던 literal을 저장
				addr_literal_tab();
			}
		}
	}
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : symbol table을 생성하는 함수이다.
* 매계 : symbol table에 추가할 symbol명
* 반환 : 정상종료 >= 0, 에러 < 0
* 주의 : 없음
* -----------------------------------------------------------------------------------
*/
int make_symtab(char*str) {
	for (int i = 0; i < sym_num; i++) {
		if (!strcmp(sym_table[i].area, now_area)) {//symtab에서 area도 같고 str도 같은 symbol이 존재하면 에러
			if (!strcmp(sym_table[i].symbol, str)) {
				return -1;
			}
		}
	}
	strcpy(sym_table[sym_num].symbol,str); //symtab에 존재하지 않는 symbol이라면 추가
	sym_table[sym_num].area = (char*)malloc(sizeof(char));
	sym_table[sym_num].area = now_area;
	sym_table[sym_num].addr = locctr;
	sym_table[sym_num].type = 'R';
	sym_num++;
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : symbol table을 탐색해서 해당하는 symbol의 주소값을 반환하는 함수이다.
* 매계 : 탐색할 symbol명
* 반환 : 정상종료 >= 0, 에러 < 0
* 주의 : 없음
* ---------------------------------------------------------------------------------
*/
int search_symtab(char*str) {
	for (int i = 0; i < sym_num; i++) {
		if (!strcmp(sym_table[i].area, now_area)) {//symtab에서 area도 같고 str도 같은 symbol이 존재하면 symbol의 주소값 반환
			if (!strcmp(sym_table[i].symbol, str)) {
				return sym_table[i].addr;
			}
		}
	}
	return -1;
}


/* ----------------------------------------------------------------------------------
* 설명 : literal table을 생성하는 함수이다.
* 매계 : literal table에 추가할 literal명
* 반환 : 정상종료 >= 0, 에러 < 0
* 주의 : 없음
* -----------------------------------------------------------------------------------
*/
int make_literaltab(char*str) {
	
	char *tmp_str = (char*)malloc(sizeof(char));
	strcpy(tmp_str, str);
	tmp_str++;

	for (int i = 0; i < literal_num; i++) { //이미 literal table에 존재하는 literal인지 확인
		if (!strcmp(literal_table[i].literal, tmp_str)) {
			return -1;
		}
	}
	strcpy(literal_table[literal_num].literal, tmp_str); //literal_table에 존재하지 않는 literal이라면 추가
	literal_num++;
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : literal table의 leteral들의 주소를 넣어주는 함수이다.
* 매계 : 없음
* 반환 : 없음
* 주의 : 없음
* -----------------------------------------------------------------------------------
*/
void addr_literal_tab() {
	char* tmp_literal = (char*)malloc(sizeof(char));
	int literalSize;
	
	for (int i = 0; i < literal_num; i++) {
		if (!literal_table[i].alloc) {
			literal_table[i].addr = locctr;
			if (literal_table[i].literal[0] == 'C' || literal_table[i].literal[0] == 'c') { //literal이 'C'인지 확인
				literal_table[i].type = 'C';
				strcpy(tmp_literal, literal_table[i].literal);
				tmp_literal += 2;
				literalSize = strlen(tmp_literal);
				tmp_literal[literalSize - 1] = '\0';
				strcpy(literal_table[i].literal, tmp_literal);
				for (int j = literalSize; j < literalSize + 2; j++) {
					literal_table[i].literal[j] = '\0'; 
				}
				locctr += literalSize - 1; //char개수만큼 locctr 증가
				literal_table[i].alloc = 1; //주소를 넣어줬다고 체크
			}
			else if (literal_table[i].literal[0] == 'X' || literal_table[i].literal[0] == 'x') { //literal이 'X"인지 확인
				literal_table[i].type = 'X';
				strcpy(tmp_literal, literal_table[i].literal);
				tmp_literal += 2;
				literalSize = strlen(tmp_literal);
				tmp_literal[literalSize - 1] = '\0';
				strcpy(literal_table[i].literal, tmp_literal);
				for (int j = literalSize; j < literalSize + 2; j++) {
					literal_table[i].literal[j] = '\0';
				}
				locctr += (literalSize - 1)/2; //byte 개수의 절반만큼 locctr증가
				literal_table[i].alloc = 1; //주소를 넣어줬다고 체크
			}
		}
	}
}

/* ----------------------------------------------------------------------------------
* 설명 : literal table을 탐색해서 해당하는 literal의 주소값을 반환하는 함수이다.
* 매계 : 탐색할 literal명
* 반환 : 정상종료 >= 0, 에러 < 0
* 주의 : 없음
* ---------------------------------------------------------------------------------
*/
int search_literaltab(char*str) {
	char* tmp_literal = (char*)malloc(sizeof(char));
	int literalSize;
	strcpy(tmp_literal, str);
	tmp_literal += 3;
	literalSize = strlen(tmp_literal);
	tmp_literal[literalSize - 1] = '\0';

	for (int i = 0; i < literal_num; i++) { //literal table에 찾고자 하는 literal이 존재하는지 확인
		if (!strcmp(literal_table[i].literal, tmp_literal)) {
			literal_table[i].alloc = 0; //메모리에 할당해주기 위해 체크
			return literal_table[i].addr;
		}
	}

	return -1;
}

/* ----------------------------------------------------------------------------------
* 설명 : literal table을 탐색해서 메모리에 할당해주는 함수이다 .
* 매계 : 없음
* 반환 : 없음
* 주의 : 없음
* ---------------------------------------------------------------------------------
*/
void literal_return() {
	for (int i = 0; i < literal_num; i++) {
		if (!literal_table[i].alloc) {
			if (literal_table[i].type == 'X') { //literal의 type이 'X'라면 literal값을 그대로 메모리에 할당
				sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%s", literal_table[i].literal);
			}
			else {
				for (int j = 0; j < strlen(literal_table[i].literal); j++) { //literal의 type이 'C'라면 각 char 값의 ASCII code를 메모리에 할당
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%s%X", control_section[cs_num].obj_code[control_section[cs_num].obj_line], literal_table[i].literal[j]);
				}
			}
			control_section[cs_num].obj_line++;
			literal_table[i].alloc = 1;
		}
	}
}


/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 명령어 옆에 OPCODE가 기록된 표(과제 5번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*        또한 과제 5번에서만 쓰이는 함수이므로 이후의 프로젝트에서는 사용되지 않는다.
* -----------------------------------------------------------------------------------
*/
// void make_opcode_output(char *file_name)
// {
// 	/* add your code here */
// }

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 SYMBOL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	/* add your code here */
FILE* fp;
if (file_name != NULL) { //인자로 NULL값이 들어오지 않는 경우 파일에 기록
	fp = fopen(file_name, "w+t");
}
else { //인자로 NULL값이 들어오는 경우 표준출력으로 화면에 출력
	fp = stdout;
}

char *tmp_area = (char*)malloc(sizeof(char));
strcpy(tmp_area, sym_table[0].area);
//SYMBOL별 주소값이 저장된 TABLE 내용을 출력
for (int i = 0; i < sym_num; i++) {
	if (strcmp(tmp_area, sym_table[i].area)) {
		fprintf(fp, "\n");
		strcpy(tmp_area, sym_table[i].area);
	}
	fprintf(fp, "%s\t\t\t%X\n", sym_table[i].symbol, sym_table[i].addr);
}

if (file_name != NULL) {//파일을 열은 경우 닫아줌.
	fclose(fp);
}
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 LITERAL별 주소값이 저장된 TABLE이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char *file_name)
{
	/* add your code here */
	FILE* fp;
	if (file_name != NULL) { //인자로 NULL값이 들어오지 않는 경우 파일에 기록
		fp = fopen(file_name, "w+t");
	}
	else { //인자로 NULL값이 들어오는 경우 표준출력으로 화면에 출력
		fp = stdout;
	}

	//LITERAL별 주소값이 저장된 TABLE 내용을 아래의 형식으로 출력
	for (int i = 0; i < literal_num; i++) {
		fprintf(fp, "%s\t\t\t%X\n", literal_table[i].literal, literal_table[i].addr);
	}

	if (file_name != NULL) {//파일을 열은 경우 닫아줌.
		fclose(fp);
	}
}

/* ----------------------------------------------------------------------------------
* 설명 : 어셈블리 코드를 기계어 코드로 바꾸기 위한 패스2 과정을 수행하는 함수이다.
*		   패스 2에서는 프로그램을 기계어로 바꾸는 작업은 라인 단위로 수행된다.
*		   다음과 같은 작업이 수행되어 진다.
*		   1. 실제로 해당 어셈블리 명령어를 기계어로 바꾸는 작업을 수행한다.
* 매계 : 없음
* 반환 : 정상종료 = 0, 에러발생 = < 0
* 주의 :
* -----------------------------------------------------------------------------------
*/
static int assem_pass2(void)
{
	/* add your code here */
	int pc;
	int target;
	int addr;
	char* reg[10] = { "A","X","L","B","S","T","F","","PC","SW" };
	cs_num = 0;

	//토큰 분리한 것들을 가지고 pass2 시작
	for (int i = 0; i < token_line; i++) {
		if (token_table[i]->type == T_DIRECTIVE) {
			if (!strcmp(token_table[i]->directive, "START")) {//프로그램이 시작하면
				control_section[cs_num].startAddr = atoi(token_table[i]->operand[0]);
				now_area = token_table[i]->section;
				strcpy(control_section[cs_num].section,now_area);
			}
			else if (!strcmp(token_table[i]->directive, "CSECT")) { //서브프로그램이 시작하면
				now_area = token_table[i]->section;
				cs_num++;
				control_section[cs_num].startAddr = 0;
				strcpy(control_section[cs_num].section, now_area);
			}
			else if (!strcmp(token_table[i]->directive, "EXTDEF")) { //EXTDEF 뒤에 나오는 값들 저장
				strcpy(control_section[cs_num].def[control_section[cs_num].extdef].symbol, token_table[i]->operand[0]);
				control_section[cs_num].def[control_section[cs_num].extdef].addr = search_symtab(token_table[i]->operand[0]);
				control_section[cs_num].extdef++;
				if (token_table[i]->operand[1] != NULL) {
					strcpy(control_section[cs_num].def[control_section[cs_num].extdef].symbol, token_table[i]->operand[1]);
					control_section[cs_num].def[control_section[cs_num].extdef].addr = search_symtab(token_table[i]->operand[1]);
					control_section[cs_num].extdef++;
					if (token_table[i]->operand[2] != NULL) {
						strcpy(control_section[cs_num].def[control_section[cs_num].extdef].symbol, token_table[i]->operand[2]);
						control_section[cs_num].def[control_section[cs_num].extdef].addr = search_symtab(token_table[i]->operand[2]);
						control_section[cs_num].extdef++;
					}
				}
			}
			else if (!strcmp(token_table[i]->directive, "EXTREF")) {//EXTREF 뒤에 나오는 값들 저장
				strcpy(control_section[cs_num].ref[control_section[cs_num].extref].symbol, token_table[i]->operand[0]);
				control_section[cs_num].extref++;
				if (token_table[i]->operand[1] != NULL) {
					strcpy(control_section[cs_num].ref[control_section[cs_num].extref].symbol, token_table[i]->operand[1]);
					control_section[cs_num].extref++;
					if (token_table[i]->operand[2] != NULL) {
						strcpy(control_section[cs_num].ref[control_section[cs_num].extref].symbol, token_table[i]->operand[2]);
						control_section[cs_num].extref++;
					}
				}
			}
			else if (!strcmp(token_table[i]->directive, "LTORG")) { //프로그램에 나왔던 literal값들을 메모리에 저장
				literal_return();
				continue;
			}
			else if (!strcmp(token_table[i]->directive, "END")) { //프로그램에 나왔던literal값들을 메모리에 저장
				literal_return();
				control_section[cs_num].lastAddr = locctr;
				cs_num++;
				continue;
			}
			else if (!strcmp(token_table[i]->directive, "BYTE")) {
				char * tmp_data = (char*)malloc(sizeof(char));
				char *tmp_token = NULL;
				strcpy(tmp_data, token_table[i]->operand[0]);
				tmp_token = strtok(tmp_data, "'");
				tmp_token = strtok(NULL, "'");
				if(token_table[i]->operand[0][0] == 'X' || token_table[i]->operand[0][0] == 'x') {
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%s", tmp_token);
				}
				else if(token_table[i]->operand[0][0] == 'C' || token_table[i]->operand[0][0] == 'c'){
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%X", tmp_token[0]);
					for (int j = 1; j < strlen(tmp_token); j++) {
						sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%s%X", control_section[cs_num].obj_code[control_section[cs_num].obj_line], tmp_token[j]);
					}
				}
			}
			else if (!strcmp(token_table[i]->directive, "WORD")) {

				if (isdigit(token_table[i]->operand[0][0])) { // 숫자라면
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%X", atoi(token_table[i]->operand[0][0]));
				}
				else { // 문자일때의 처리
					if (strstr(token_table[i]->operand[0], "-")) {
						char op_t[100];
						int ref_check = 0;
						strcpy(op_t, token_table[i]->operand[0]);
						char* op1 = (char*)malloc(sizeof(char));
						char* op2 = (char*)malloc(sizeof(char));
						memset(op1, 0, 10);
						memset(op2, 0, 10);
						op1 = strtok_s(op_t, "-", &op2);
						for (int j = 0; j < control_section[cs_num].extref; j++) {
							if (!strcmp(op1, control_section[cs_num].ref[j].symbol)) {
								ref_check = 1;
							}
							if (!strcmp(op2, control_section[cs_num].ref[j].symbol)) {
								ref_check = 2;
							}
						}
						if (ref_check != 0) {
							sprintf(control_section[cs_num].mod[control_section[cs_num].mod_num].symbol, "+%s", op1);
							control_section[cs_num].mod[control_section[cs_num].mod_num].addr = token_table[i]->address;
							control_section[cs_num].ref_size[control_section[cs_num].mod_num] = 6;
							control_section[cs_num].mod_num++;
							sprintf(control_section[cs_num].mod[control_section[cs_num].mod_num].symbol, "-%s", op2);
							control_section[cs_num].mod[control_section[cs_num].mod_num].addr = token_table[i]->address;
							control_section[cs_num].ref_size[control_section[cs_num].mod_num] = 6;
							control_section[cs_num].mod_num++;
							
							sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%06X", 0);
						}
					}
				}
			}
		}
		else if (token_table[i]->type == T_INSTRUCTION) {
			int op = 252;
			token_table[i]->opnum = op & inst_table[token_table[i]->op_index]->opcode;
			if ((token_table[i]->nixbpe & N) == N) {
				token_table[i]->opnum += 2;
			}
			if ((token_table[i]->nixbpe & I) == I) {
				token_table[i]->opnum += 1;
			}
			if (token_table[i]->plus_check == 1) { //명령어가 4형식일 때
				sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%01X%05X", token_table[i]->opnum, token_table[i]->nixbpe & 0b001111, 0);
				for (int j = 0; j < control_section[cs_num].extref; j++) {
					if (!strcmp(token_table[i]->operand[0], control_section[cs_num].ref[j].symbol)) {
						sprintf(control_section[cs_num].mod[control_section[cs_num].mod_num].symbol,"+%s", token_table[i]->operand[0]);
						control_section[cs_num].mod[control_section[cs_num].mod_num].addr = token_table[i]->address + 1;
						control_section[cs_num].ref_size[control_section[cs_num].mod_num] = 5;
						control_section[cs_num].mod_num++;
					}
				}
				control_section[cs_num].obj_line++;
				continue;
			}
			if (inst_table[token_table[i]->op_index]->format == 1) {//1형식일 때
				sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X", inst_table[token_table[i]->op_index]->opcode);
			}
			else if (inst_table[token_table[i]->op_index]->format == 2) { //2형식일 때
				if(inst_table[token_table[i]->op_index]->operand_num == 1){ //피연산자의 개수가 1개일 때
					int reg_num;
					for (int j = 0; j < 10; j++) {
						if (!strcmp(token_table[i]->operand[0], reg[j])){
							reg_num = j;
							break;
						}
					} 
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%01X%01X", token_table[i]->opnum,reg_num, 0);
				}
				else if (inst_table[token_table[i]->op_index]->operand_num == 2) { //피연산자의 개수가 2개일 때
					int reg_num1, reg_num2;
					for (int j = 0; j < 6; j++) {
						if (!strcmp(token_table[i]->operand[0], reg[j])) {
							reg_num1 = j;
						}
						if (!strcmp(token_table[i]->operand[1], reg[j])) {
							reg_num2 = j;
						}
					}
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%01X%01X", token_table[i]->opnum, reg_num1, reg_num2);
				}
			}
			else if (inst_table[token_table[i]->op_index]->format == 3) { //3형식일 때
				if (inst_table[token_table[i]->op_index]->operand_num == 0) { //피연산자가 존재하지 않을 때
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%04X", token_table[i]->opnum, 0);
					control_section[cs_num].obj_line++;
					continue;
				}
				else if (token_table[i]->operand[0] != NULL) { //피연산자가 존재할 때
					if (strstr(token_table[i]->operand[0], "#") != NULL) { //immediate addressing일 때
						token_table[i]->operand[0]++;
						sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%04X", token_table[i]->opnum, atoi(token_table[i]->operand[0]));
						control_section[cs_num].obj_line++;
						continue;
					}
					if (strstr(token_table[i]->operand[0], "@") != NULL){ //indirection addressing일 때
						token_table[i]->operand[0]++;
					}
					if (strstr(token_table[i]->operand[0], "=") != NULL) { //피연산자가 literal일 때 
						if ((target = search_literaltab(token_table[i]->operand[0])) < 0) {
							fprintf(stderr, "no literal");
						}
					}
					else {
						target = search_symtab(token_table[i]->operand[0]);
					}
					pc = token_table[i + 1]->address;
					addr = target - pc;
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%01X%03X", token_table[i]->opnum, token_table[i]->nixbpe & 0b001111, addr & 0xFFF);
				}
			}
		}
		control_section[cs_num].obj_line++;
	}
	return 0;
}

/* ----------------------------------------------------------------------------------
* 설명 : 입력된 문자열의 이름을 가진 파일에 프로그램의 결과를 저장하는 함수이다.
*        여기서 출력되는 내용은 object code (프로젝트 1번) 이다.
* 매계 : 생성할 오브젝트 파일명
* 반환 : 없음
* 주의 : 만약 인자로 NULL값이 들어온다면 프로그램의 결과를 표준출력으로 보내어
*        화면에 출력해준다.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	/* add your code here */
	FILE* fp;
	if (file_name != NULL) { //인자로 NULL값이 들어오지 않는 경우 파일에 기록
		fp = fopen(file_name, "w+t");
	}
	else { //인자로 NULL값이 들어오는 경우 표준출력으로 화면에 출력
		fp = stdout;
	}
	int line_len = 0;
	int check_line = 0;
	int byte_num = 0;
	int start = 0;
	int line_max = 0;
	//object code 내용을 아래의 형식으로 출력
	for (int i = 0; i < cs_num; i++) {
		//Header record 작성
		fprintf(fp, "H%-6s%06X%06X\n", control_section[i].section, control_section[i].startAddr, control_section[i].lastAddr);
		
		if (control_section[i].extdef != 0) { //Define record 작성
			fprintf(fp, "D");
			for (int j = 0; j < control_section[i].extdef; j++) {
				fprintf(fp, "%-6s%06X", control_section[i].def[j].symbol, control_section[i].def[j].addr);
			}
			fprintf(fp, "\n");
		}

		if (control_section[i].extref != 0) { //Refer record 작성
			fprintf(fp, "R");
			for (int j = 0; j < control_section[i].extref; j++) {
				fprintf(fp, "%-6s", control_section[i].ref[j].symbol);
			}
			fprintf(fp, "\n");
		}
		
		//Text record 작성
		line_len = 0;
		check_line = 0;
		byte_num = 0;
		start = 0;
		line_max = 0;
		
		for (int j = 0; j < control_section[i].obj_line; j++) {
			if (strlen(control_section[i].obj_code[j]) == 0) {
				if (!start) {
					check_line++;
					continue;
				}
			}
			else {
				start = 1;
			}

			if(strlen(control_section[i].obj_code[j]) == 0){
				break;
			}
			line_len += strlen(control_section[i].obj_code[j]);
			if (line_len > 60) { //현재까지의 byte의 개수가 한줄에 쓸수 있는 분량보다 많다면
				line_len -= strlen(control_section[i].obj_code[j]);
				line_max = 1;
			}
			if(line_max){
				fprintf(fp, "T%06X", control_section[i].startAddr + byte_num);
				byte_num += line_len / 2;
				fprintf(fp, "%02X", line_len / 2);
				for (int k = check_line; k < j; k++) {
					fprintf(fp, "%s", control_section[i].obj_code[k]);
					check_line++;
				}
				fprintf(fp, "\n");
				j--;
				line_len = 0;
				line_max = 0;
			}
		}
		if (line_len) { // 남은 byte의 개수가 60보다 적을 때
			fprintf(fp, "T%06X", control_section[i].startAddr + byte_num);
			fprintf(fp, "%02X", line_len / 2);
			for (int k = check_line; k < control_section[i].obj_line; k++) {
				fprintf(fp, "%s", control_section[i].obj_code[k]);
				check_line++;
				if (strlen(control_section[i].obj_code[k]) == 0) {
					break;
				}
			}
			fprintf(fp, "\n");
			while(control_section[i].obj_line - check_line) { //아직 다 끝나지 않았을 때
				if (strlen(control_section[i].obj_code[check_line]) == 0) {
					check_line++;
					continue;
				}
				else {
					fprintf(fp, "T%06X", token_table[check_line]->address);
					line_len = strlen(control_section[i].obj_code[check_line]);
					check_line++;
					if (strlen(control_section[i].obj_code[check_line]) == 0) {
						fprintf(fp, "%02X%s\n", line_len/2, control_section[i].obj_code[check_line - 1]);
					}
				}
			}
		}

		if (control_section[i].extref != 0) { //Modification record 작성
			for (int j = 0; j < control_section[i].mod_num; j++) {
				fprintf(fp, "M%06X%02X%s\n", control_section[i].mod[j].addr, control_section[i].ref_size[j], control_section[i].mod[j].symbol);
			}
		}

		if (i == 0) { //End record 작성
			fprintf(fp, "E%06X\n\n", control_section[i].startAddr);
		}
		else {
			fprintf(fp, "E\n\n");
		}
	}

	if (file_name != NULL) {//파일을 열은 경우 닫아줌.
		fclose(fp);
	}
}
