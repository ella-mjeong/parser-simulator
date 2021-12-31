/*
 * ȭ�ϸ� : my_assembler_20160433.c 
 * ��  �� : �� ���α׷��� SIC/XE �ӽ��� ���� ������ Assembler ���α׷��� ���η�ƾ����,
 * �Էµ� ������ �ڵ� ��, ��ɾ �ش��ϴ� OPCODE�� ã�� ����Ѵ�.
 * ���� ������ ���Ǵ� ���ڿ� "00000000"���� �ڽ��� �й��� �����Ѵ�.
 */

/*
 *
 * ���α׷��� ����� �����Ѵ�. 
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <ctype.h>

// ���ϸ��� "00000000"�� �ڽ��� �й����� ������ ��.
#include "my_assembler_20160433.h"
#pragma warning(disable:4996)
#pragma warning(disable:4018)
#pragma warning(disable:4047)
#pragma warning(disable:4024)

/* ----------------------------------------------------------------------------------
 * ���� : ����ڷ� ���� ����� ������ �޾Ƽ� ��ɾ��� OPCODE�� ã�� ����Ѵ�.
 * �Ű� : ���� ����, ����� ���� 
 * ��ȯ : ���� = 0, ���� = < 0 
 * ���� : ���� ����� ���α׷��� ����Ʈ ������ �����ϴ� ��ƾ�� ������ �ʾҴ�. 
 *		   ���� �߰������� �������� �ʴ´�. 
 * ----------------------------------------------------------------------------------
 */
int main(int args, char *arg[])
{

	if (init_my_assembler() < 0)
	{
		printf("init_my_assembler: ���α׷� �ʱ�ȭ�� ���� �߽��ϴ�.\n");
		return -1;
	}

	if (assem_pass1() < 0)
	{
		printf("assem_pass1: �н�1 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}

	//make_symtab_output(NULL); //symbol table�� ȭ�鿡 ���
	make_symtab_output("symtab_20160433.txt"); // symbol table�� ���Ϸ� ���
	//make_literaltab_output(NULL); //literal table�� ȭ�鿡 ���
	make_literaltab_output("literaltab_20160433.txt"); // literal table�� ���Ϸ� ���

	if (assem_pass2() < 0) //object code ����
	{
		printf("assem_pass2: �н�2 �������� �����Ͽ����ϴ�.  \n");
		return -1;
	}

	//make_objectcode_output(NULL); //����� ȭ�鿡 ���
	make_objectcode_output("output_20160433.txt"); // ����� ���Ϸ� ���

	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : ���α׷� �ʱ�ȭ�� ���� �ڷᱸ�� ���� �� ������ �д� �Լ��̴�. 
 * �Ű� : ����
 * ��ȯ : �������� = 0 , ���� �߻� = -1
 * ���� : ������ ��ɾ� ���̺��� ���ο� �������� �ʰ� ������ �����ϰ� �ϱ� 
 *		   ���ؼ� ���� ������ �����Ͽ� ���α׷� �ʱ�ȭ�� ���� ������ �о� �� �� �ֵ���
 *		   �����Ͽ���. 
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
 * ���� : �ӽ��� ���� ��� �ڵ��� ������ �о� ���� ��� ���̺�(inst_table)�� 
 *        �����ϴ� �Լ��̴�. 
 * �Ű� : ���� ��� ����
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : ���� ������� ������ �����Ӱ� �����Ѵ�. ���ô� ������ ����.
 *	
 *	===============================================================================
 *		 | �̸� | ���۷����� ���� | ���� | ���� �ڵ� | NULL |
 *	===============================================================================	   
 *		
 * ----------------------------------------------------------------------------------
 */
int init_inst_file(char *inst_file)
{
	FILE *file;
	int errno;

	/* add your code here */
	file = fopen(inst_file, "r"); //�б���� ������ ����

	if (file == NULL) { //���� ���⿡ �����ϸ� ����
		errno = -1;
	}
	else {
		while (1) {//�����Ҵ� �� ���� ��������� ���Ŀ� ���� �ش��ϴ� ������ ���� ��� ���̺� ����.
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
 * ���� : ����� �� �ҽ��ڵ带 �о� �ҽ��ڵ� ���̺�(input_data)�� �����ϴ� �Լ��̴�. 
 * �Ű� : ������� �ҽ����ϸ�
 * ��ȯ : �������� = 0 , ���� < 0  
 * ���� : ���δ����� �����Ѵ�.
 *		
 * ----------------------------------------------------------------------------------
 */
int init_input_file(char *input_file)
{
	FILE *file;
	int errno;

	/* add your code here */
	int buf_size = 100;

	file = fopen(input_file, "r"); //�б���� ������ ����
	if (file == NULL) { //���� ���⿡ �����ϸ� ����
		errno = -1;
	}
	else {
		while (1) {//�����Ҵ� �� ������ ���δ����� �о� �ҽ��ڵ� ���̺� ����
			input_data[line_num] = (char*)malloc(sizeof(char)*buf_size);
			memset(input_data[line_num], 0, buf_size);

			if (fgets(input_data[line_num], buf_size, file) == NULL) {//������ ���� �����ϸ� while�� Ż��
				break;
			}

			if (input_data[line_num][strlen(input_data[line_num]) - 1] == '\n') {//������ ���� �����ϴ� '\n'�� '\0'�� ��ü
				input_data[line_num][strlen(input_data[line_num]) - 1] = '\0';
			}

			line_num++;
		}
		fclose(file);
	}
	return errno;
}

/* ----------------------------------------------------------------------------------
 * ���� : �ҽ� �ڵ带 �о�� ��ū������ �м��ϰ� ��ū ���̺��� �ۼ��ϴ� �Լ��̴�. 
 *        �н� 1�� ���� ȣ��ȴ�. 
 * �Ű� : �Ľ��� ���ϴ� ���ڿ�  
 * ��ȯ : �������� = 0 , ���� < 0 
 * ���� : my_assembler ���α׷������� ���δ����� ��ū �� ������Ʈ ������ �ϰ� �ִ�. 
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

	if (tmp_str[0] == '.') {//�Է¹��ڿ��� �ּ��̶�� ��ū���̺��� comment������ ����
		token_table[token_line]->comment = (char*)malloc(sizeof(char));
		token_table[token_line]->comment = tmp_str;
		token_table[token_line]->type = T_COMMENT;
		token_line++;
		return 0;
	}

	tok = strtok(tmp_str, " \t");//" \t"��� �����ڷ� ��ū�и�

	if (tok == NULL)//�и��� ���ڿ��� ������ ����
		return 0;

	if (tok[0] == '+') {//��� +�� �߰��Ǿ� ������
		inst_check = 1;
		char inst_four[100];
		strcpy(inst_four, tok);
		int size = sizeof(tok);
		for (int i = 0; i < size; i++) {
			tok[i] = inst_four[i + 1];
		}
		tok[size] = '\0';
	}
	if ((inst_num = search_opcode(tok)) >= 0) {//�߸� ���ڿ��� ������
		token_table[token_line]->operator =  (char*)malloc(sizeof(char));
		token_table[token_line]->operator=tok;
		token_table[token_line]->op_index = inst_num;
		if (inst_table[token_table[token_line]->op_index]->format == 3) {
			if (inst_check) {
				//��� +�� �߰��Ǿ� ������ ��ū���̺� ���� ����
				token_table[token_line]->plus_check = 1;
				//4���ĸ�ɾ��̹Ƿ� e�� ǥ��
				token_table[token_line]->nixbpe |= E;
			}
			else {
				//��� +�� �߰��Ǿ� ���� �ʴٸ� pc relative�� ����� ���̹Ƿ� p�� ǥ��
				token_table[token_line]->nixbpe |= P;
			}
		}
		if ((tok_num = inst_table[inst_num]->operand_num) != 0) {//operand�� �ִٸ�
			token_table[token_line]->operand[0] = (char*)malloc(sizeof(char));
			if (tok_num == 1) {//operand ������ 1���� ��
				tok = strtok(NULL, " \t");
				token_table[token_line]->operand[0] = tok;
				if (strstr(tok, ",") != NULL) {//BUFFER,x ���� �迭�� ���� ��쿡 ��ū�и�
					token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
					memset(token_table[token_line]->operand[1], 0, 10);
					token_table[token_line]->operand[0] = strtok_s(tok, ",", &token_table[token_line]->operand[1]);
					if (!strcmp(token_table[token_line]->operand[1], "X")) {
						token_table[token_line]->nixbpe |= X;
					}
				}

			}
			else {//operand ������ 2���� ��
				tok = strtok(NULL, ",");
				token_table[token_line]->operand[0] = tok;
				tok = strtok(NULL, " \t");
				token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
				token_table[token_line]->operand[1] = tok;
			}

			//addressing �����
			if (strstr(token_table[token_line]->operand[0], "@") != NULL) {//indirection addressing�� ��
				token_table[token_line]->nixbpe |= N;
			}
			else if (strstr(token_table[token_line]->operand[0], "#") != NULL) {//immediate addressing�� ��
				token_table[token_line]->nixbpe |= I;
				token_table[token_line]->nixbpe ^= P;
			}
			else {// �Ѵ� �ƴ϶��
				if (inst_table[inst_num]->format == 3) { //�׷��� ������ ������ 3����/4�����̶��
					token_table[token_line]->nixbpe |= N;
					token_table[token_line]->nixbpe |= I;
				}
			}

		}
		else {//operand�� ���� 3���� ��ɾ���
			if (inst_table[inst_num]->format == 3) { 
				token_table[token_line]->nixbpe |= N;
				token_table[token_line]->nixbpe |= I;
			}
		}

		if (tok != NULL) { //comment �и�
			token_table[token_line]->comment = (char*)malloc(sizeof(char));
			tok = strtok(NULL, "\0");
			token_table[token_line]->comment = tok;
		}
		token_table[token_line]->type = T_INSTRUCTION;
	}
	else if ((dir_num = search_directive(tok)) >= 0) {//�߸� ���ڿ��� ���þ���
		token_table[token_line]->directive = (char*)malloc(sizeof(char));
		token_table[token_line]->directive = tok;
		if (dir_num == 1) {//���þ� �ڿ� �ٸ� ������ ���ԵǾ� ���� ��
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

		if (tok != NULL) { //comment �и�
			token_table[token_line]->comment = (char*)malloc(sizeof(char));
			tok = strtok(NULL, "\0");
			token_table[token_line]->comment = tok;
		}
		token_table[token_line]->type = T_DIRECTIVE;
	}
	else {//�߸� ���ڿ��� label�̶��
		token_table[token_line]->label = (char*)malloc(sizeof(char));
		token_table[token_line]->label = tok;
		tok = strtok(NULL, " \t");

		if (tok[0] == '+') {//�����ڵ忡 +�� �߰��Ǿ� ������
			inst_check = 1;
			char inst_four[100];
			strcpy(inst_four, tok);
			int size = sizeof(tok);
			for (int i = 0; i < size; i++) {
				tok[i] = inst_four[i + 1];
			}
			tok[size] = '\0';
		}

		if ((inst_num = search_opcode(tok)) >= 0) {//�߸� ���ڿ��� �����ڵ���
			token_table[token_line]->operator =  (char*)malloc(sizeof(char));
			token_table[token_line]->operator = tok;
			token_table[token_line]->op_index = inst_num;

			if (inst_table[token_table[token_line]->op_index]->format == 3) {
				if (inst_check) {
					//+�� ���� ��ɾ��� ��ū���̺� ���� ����
					token_table[token_line]->plus_check = 1;
					//4���ĸ�ɾ��̹Ƿ� e�� ǥ��
					token_table[token_line]->nixbpe |= E;
				}
				else {
					//+�� ���� ��ɾ �ƴ϶�� pc relative�� ����� ���̹Ƿ� p�� ǥ��
					token_table[token_line]->nixbpe |= P;
				}
			}

			if ((tok_num = inst_table[inst_num]->operand_num) != 0) {//operand�� �ִٸ�
				token_table[token_line]->operand[0] = (char*)malloc(sizeof(char));
				if (tok_num == 1) { //operand�� ������ 1���� ��
					tok = strtok(NULL, " \t");
					token_table[token_line]->operand[0] = tok;
					if (strstr(tok, ",") != NULL) { //BUFFER,x ���� �迭�� ���� ��쿡 ��ū�и�
						token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
						memset(token_table[token_line]->operand[1], 0, 10);
						token_table[token_line]->operand[0] = strtok_s(tok, ",", &token_table[token_line]->operand[1]);

						if (!strcmp(token_table[token_line]->operand[1], "X")) {
							token_table[token_line]->nixbpe |= X;
						}
					}
				}
				else { //operand ������ 2���� ��
					tok = strtok(NULL, ",");
					token_table[token_line]->operand[0] = tok;
					tok = strtok(NULL, " \t");
					token_table[token_line]->operand[1] = (char*)malloc(sizeof(char));
					token_table[token_line]->operand[1] = tok;
				}
				//addressing �����
				if (strstr(token_table[token_line]->operand[0], "@") != NULL) {//indirection addressing�� ��
					token_table[token_line]->nixbpe |= N;
				}
				else if (strstr(token_table[token_line]->operand[0], "#") != NULL) {//immediate addressing�� ��
					token_table[token_line]->nixbpe |= I;
					token_table[token_line]->nixbpe ^= P;
				}
				else {// �Ѵ� �ƴ϶��
					if (inst_table[inst_num]->format == 3) { //�׷��� ������ ������ 3����/4�����̶��
						token_table[token_line]->nixbpe |= N;
						token_table[token_line]->nixbpe |= I;
					}
				}
			}
			else {//operand�� ���� 3���� ��ɾ���
				if (inst_table[inst_num]->format == 3) {
					token_table[token_line]->nixbpe |= N;
					token_table[token_line]->nixbpe |= I;
				}
			}
			token_table[token_line]->type = T_INSTRUCTION;
		}
		else if ((dir_num = search_directive(tok)) >= 0) {//�߸� ���ڿ��� ���þ���
			token_table[token_line]->directive = (char*)malloc(sizeof(char));
			token_table[token_line]->directive = tok;
			if (dir_num == 1) {//���þ� �ڿ� �ٸ� ������ ���ԵǾ� ���� ��
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
		if (tok != NULL) { //comment �и�
			token_table[token_line]->comment = (char*)malloc(sizeof(char));
			tok = strtok(NULL, "\0");
			token_table[token_line]->comment = tok;
		}
	}

	token_line++;
	return 0;
}

/* ----------------------------------------------------------------------------------
 * ���� : �Է� ���ڿ��� ���� �ڵ������� �˻��ϴ� �Լ��̴�. 
 * �Ű� : ��ū ������ ���е� ���ڿ� 
 * ��ȯ : �������� = ���� ���̺� �ε���, ���� < 0 
 * ���� : 
 *		
 * ----------------------------------------------------------------------------------
 */
int search_opcode(char *str)
{
	/* add your code here */
	for (int i = 0; i < inst_index; i++) {
		if (!strcmp(inst_table[i]->instruction, str)) { //�Էµ� ���ڿ��� inst_table�� ��ɾ�� ��ġ�Ѵٸ� inst_table�� index�� ��ȯ
			return i;
		}
	}
	return -1;
}

//�߰��� �Լ�
/* ----------------------------------------------------------------------------------
 * ���� : �Է� ���ڿ��� ���þ������� �˻��ϴ� �Լ��̴�.
 * �Ű� : ��ū ������ ���е� ���ڿ�
 * ��ȯ : �������� >= 0, ���� < 0
 * ���� :
 *
 * ----------------------------------------------------------------------------------
 */
int search_directive(char *str)
{
	char *directive_table[] = { "START","END","BYTE","WORD","RESB","RESW","CSECT","EXTDEF","EXTREF","EQU","ORG","LTORG" };
	int directive_num[] = { 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0 };

	for (int i = 0; i < sizeof(directive_num) / sizeof(int); i++) {
		if (!strcmp(directive_table[i], str)) { //�Էµ� ���ڿ��� directive_table�� ���þ�� ��ġ�Ѵٸ� �ڿ� ������ ���ԵǾ� �ִ� ���þ��� 1, �ƴ϶�� 0 ����
			return directive_num[i];
		}
	}
	return -1;
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �н�1������ �����ϴ� �Լ��̴�.
*		   �н�1������..
*		   1. ���α׷� �ҽ��� ��ĵ�Ͽ� �ش��ϴ� ��ū������ �и��Ͽ� ���α׷� ���κ� ��ū
*		   ���̺��� �����Ѵ�.
*
* �Ű� : ����
* ��ȯ : ���� ���� = 0 , ���� = < 0
* ���� : ���� �ʱ� ���������� ������ ���� �˻縦 ���� �ʰ� �Ѿ �����̴�.
*	  ���� ������ ���� �˻� ��ƾ�� �߰��ؾ� �Ѵ�.
*
* -----------------------------------------------------------------------------------
*/
static int assem_pass1(void)
{
	/* add your code here */

	for (int i = 0; i < line_num; i++) { //�Է����Ͽ��� ���κ��� �о���� �ҽ����� ���� ��ū�и���Ų��.
		if (token_parsing(input_data[i]) < 0) {
			return -1;
		}
	}

	now_area = (char*)malloc(sizeof(char));

	//��ū �и��� �͵��� ������ loc�� ������ ��Ÿ �ʿ��� ������ ����
	for (int i = 0; i < token_line; i++) { 
		if (token_table[i]->type == T_DIRECTIVE) {
			if (!strcmp(token_table[i]->directive ,"START")) { //���α׷��� �����Ѵٸ�
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
			else if (!strcmp(token_table[i]->directive, "CSECT")) { //���ο� sub program�� ���۵ȴٸ�
				control_section[cs_num].lastAddr = locctr; // ���� ���α׷��� ������ �ּҰ��� ����
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
			if (token_table[i]->plus_check) { //��ɾ��� ���Ŀ� ���� locctr�� ����
				locctr += 4;
			}
			else {
				locctr += inst_table[token_table[i]->op_index]->format;
			}
			if (token_table[i]->operand[0] != NULL) {
				if (strstr(token_table[i]->operand[0], "=") != NULL) {//indirection addressing�� ��
					make_literaltab(token_table[i]->operand[0]);
				}
			}
		}
		else if (token_table[i]->type == T_DIRECTIVE){
			if (!strcmp(token_table[i]->directive, "WORD")) {//locctr�� 3 ������Ŵ
				locctr += 3;
			}
			else if (!strcmp(token_table[i]->directive, "RESW")) {//locctr�� (3 * �ǿ����ڰ�)��ŭ ������Ŵ
				locctr += 3* atoi(token_table[i]->operand[0]);
			}
			else if (!strcmp(token_table[i]->directive, "RESB")) {//locctr�� �ǿ����ڰ���ŭ ������Ŵ
				locctr += atoi(token_table[i]->operand[0]);
			}
			else if (!strcmp(token_table[i]->directive, "BYTE")) { //locctr�� 1 ������Ŵ
				locctr += 1;
			}
			else if (!strcmp(token_table[i]->directive, "EQU")){
				if (!strcmp(token_table[i]->operand[0], "*")) {// �ǿ����ڰ� *�� ��� 
					sym_table[sym_num-1].addr = locctr; //���� locctr�� ���� �ּҷ� ����
				}
				else {  // �׷��� ���� ��� �ǿ����ڸ� ���� �ּҰ��� ����Ͽ� ����
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
			else if (!strcmp(token_table[i]->directive, "LTORG")) { //���α׷��� ���Դ� literal�� ����
				addr_literal_tab();
			}
			else if (!strcmp(token_table[i]->directive, "END")) { //���α׷��� ���Դ� literal�� ����
				addr_literal_tab();
			}
		}
	}
	return 0;
}

/* ----------------------------------------------------------------------------------
* ���� : symbol table�� �����ϴ� �Լ��̴�.
* �Ű� : symbol table�� �߰��� symbol��
* ��ȯ : �������� >= 0, ���� < 0
* ���� : ����
* -----------------------------------------------------------------------------------
*/
int make_symtab(char*str) {
	for (int i = 0; i < sym_num; i++) {
		if (!strcmp(sym_table[i].area, now_area)) {//symtab���� area�� ���� str�� ���� symbol�� �����ϸ� ����
			if (!strcmp(sym_table[i].symbol, str)) {
				return -1;
			}
		}
	}
	strcpy(sym_table[sym_num].symbol,str); //symtab�� �������� �ʴ� symbol�̶�� �߰�
	sym_table[sym_num].area = (char*)malloc(sizeof(char));
	sym_table[sym_num].area = now_area;
	sym_table[sym_num].addr = locctr;
	sym_table[sym_num].type = 'R';
	sym_num++;
	return 0;
}

/* ----------------------------------------------------------------------------------
* ���� : symbol table�� Ž���ؼ� �ش��ϴ� symbol�� �ּҰ��� ��ȯ�ϴ� �Լ��̴�.
* �Ű� : Ž���� symbol��
* ��ȯ : �������� >= 0, ���� < 0
* ���� : ����
* ---------------------------------------------------------------------------------
*/
int search_symtab(char*str) {
	for (int i = 0; i < sym_num; i++) {
		if (!strcmp(sym_table[i].area, now_area)) {//symtab���� area�� ���� str�� ���� symbol�� �����ϸ� symbol�� �ּҰ� ��ȯ
			if (!strcmp(sym_table[i].symbol, str)) {
				return sym_table[i].addr;
			}
		}
	}
	return -1;
}


/* ----------------------------------------------------------------------------------
* ���� : literal table�� �����ϴ� �Լ��̴�.
* �Ű� : literal table�� �߰��� literal��
* ��ȯ : �������� >= 0, ���� < 0
* ���� : ����
* -----------------------------------------------------------------------------------
*/
int make_literaltab(char*str) {
	
	char *tmp_str = (char*)malloc(sizeof(char));
	strcpy(tmp_str, str);
	tmp_str++;

	for (int i = 0; i < literal_num; i++) { //�̹� literal table�� �����ϴ� literal���� Ȯ��
		if (!strcmp(literal_table[i].literal, tmp_str)) {
			return -1;
		}
	}
	strcpy(literal_table[literal_num].literal, tmp_str); //literal_table�� �������� �ʴ� literal�̶�� �߰�
	literal_num++;
	return 0;
}

/* ----------------------------------------------------------------------------------
* ���� : literal table�� leteral���� �ּҸ� �־��ִ� �Լ��̴�.
* �Ű� : ����
* ��ȯ : ����
* ���� : ����
* -----------------------------------------------------------------------------------
*/
void addr_literal_tab() {
	char* tmp_literal = (char*)malloc(sizeof(char));
	int literalSize;
	
	for (int i = 0; i < literal_num; i++) {
		if (!literal_table[i].alloc) {
			literal_table[i].addr = locctr;
			if (literal_table[i].literal[0] == 'C' || literal_table[i].literal[0] == 'c') { //literal�� 'C'���� Ȯ��
				literal_table[i].type = 'C';
				strcpy(tmp_literal, literal_table[i].literal);
				tmp_literal += 2;
				literalSize = strlen(tmp_literal);
				tmp_literal[literalSize - 1] = '\0';
				strcpy(literal_table[i].literal, tmp_literal);
				for (int j = literalSize; j < literalSize + 2; j++) {
					literal_table[i].literal[j] = '\0'; 
				}
				locctr += literalSize - 1; //char������ŭ locctr ����
				literal_table[i].alloc = 1; //�ּҸ� �־���ٰ� üũ
			}
			else if (literal_table[i].literal[0] == 'X' || literal_table[i].literal[0] == 'x') { //literal�� 'X"���� Ȯ��
				literal_table[i].type = 'X';
				strcpy(tmp_literal, literal_table[i].literal);
				tmp_literal += 2;
				literalSize = strlen(tmp_literal);
				tmp_literal[literalSize - 1] = '\0';
				strcpy(literal_table[i].literal, tmp_literal);
				for (int j = literalSize; j < literalSize + 2; j++) {
					literal_table[i].literal[j] = '\0';
				}
				locctr += (literalSize - 1)/2; //byte ������ ���ݸ�ŭ locctr����
				literal_table[i].alloc = 1; //�ּҸ� �־���ٰ� üũ
			}
		}
	}
}

/* ----------------------------------------------------------------------------------
* ���� : literal table�� Ž���ؼ� �ش��ϴ� literal�� �ּҰ��� ��ȯ�ϴ� �Լ��̴�.
* �Ű� : Ž���� literal��
* ��ȯ : �������� >= 0, ���� < 0
* ���� : ����
* ---------------------------------------------------------------------------------
*/
int search_literaltab(char*str) {
	char* tmp_literal = (char*)malloc(sizeof(char));
	int literalSize;
	strcpy(tmp_literal, str);
	tmp_literal += 3;
	literalSize = strlen(tmp_literal);
	tmp_literal[literalSize - 1] = '\0';

	for (int i = 0; i < literal_num; i++) { //literal table�� ã���� �ϴ� literal�� �����ϴ��� Ȯ��
		if (!strcmp(literal_table[i].literal, tmp_literal)) {
			literal_table[i].alloc = 0; //�޸𸮿� �Ҵ����ֱ� ���� üũ
			return literal_table[i].addr;
		}
	}

	return -1;
}

/* ----------------------------------------------------------------------------------
* ���� : literal table�� Ž���ؼ� �޸𸮿� �Ҵ����ִ� �Լ��̴� .
* �Ű� : ����
* ��ȯ : ����
* ���� : ����
* ---------------------------------------------------------------------------------
*/
void literal_return() {
	for (int i = 0; i < literal_num; i++) {
		if (!literal_table[i].alloc) {
			if (literal_table[i].type == 'X') { //literal�� type�� 'X'��� literal���� �״�� �޸𸮿� �Ҵ�
				sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%s", literal_table[i].literal);
			}
			else {
				for (int j = 0; j < strlen(literal_table[i].literal); j++) { //literal�� type�� 'C'��� �� char ���� ASCII code�� �޸𸮿� �Ҵ�
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%s%X", control_section[cs_num].obj_code[control_section[cs_num].obj_line], literal_table[i].literal[j]);
				}
			}
			control_section[cs_num].obj_line++;
			literal_table[i].alloc = 1;
		}
	}
}


/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ ��ɾ� ���� OPCODE�� ��ϵ� ǥ(���� 5��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*        ���� ���� 5�������� ���̴� �Լ��̹Ƿ� ������ ������Ʈ������ ������ �ʴ´�.
* -----------------------------------------------------------------------------------
*/
// void make_opcode_output(char *file_name)
// {
// 	/* add your code here */
// }

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ SYMBOL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_symtab_output(char *file_name)
{
	/* add your code here */
FILE* fp;
if (file_name != NULL) { //���ڷ� NULL���� ������ �ʴ� ��� ���Ͽ� ���
	fp = fopen(file_name, "w+t");
}
else { //���ڷ� NULL���� ������ ��� ǥ��������� ȭ�鿡 ���
	fp = stdout;
}

char *tmp_area = (char*)malloc(sizeof(char));
strcpy(tmp_area, sym_table[0].area);
//SYMBOL�� �ּҰ��� ����� TABLE ������ ���
for (int i = 0; i < sym_num; i++) {
	if (strcmp(tmp_area, sym_table[i].area)) {
		fprintf(fp, "\n");
		strcpy(tmp_area, sym_table[i].area);
	}
	fprintf(fp, "%s\t\t\t%X\n", sym_table[i].symbol, sym_table[i].addr);
}

if (file_name != NULL) {//������ ���� ��� �ݾ���.
	fclose(fp);
}
}

/* ----------------------------------------------------------------------------------
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ LITERAL�� �ּҰ��� ����� TABLE�̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_literaltab_output(char *file_name)
{
	/* add your code here */
	FILE* fp;
	if (file_name != NULL) { //���ڷ� NULL���� ������ �ʴ� ��� ���Ͽ� ���
		fp = fopen(file_name, "w+t");
	}
	else { //���ڷ� NULL���� ������ ��� ǥ��������� ȭ�鿡 ���
		fp = stdout;
	}

	//LITERAL�� �ּҰ��� ����� TABLE ������ �Ʒ��� �������� ���
	for (int i = 0; i < literal_num; i++) {
		fprintf(fp, "%s\t\t\t%X\n", literal_table[i].literal, literal_table[i].addr);
	}

	if (file_name != NULL) {//������ ���� ��� �ݾ���.
		fclose(fp);
	}
}

/* ----------------------------------------------------------------------------------
* ���� : ����� �ڵ带 ���� �ڵ�� �ٲٱ� ���� �н�2 ������ �����ϴ� �Լ��̴�.
*		   �н� 2������ ���α׷��� ����� �ٲٴ� �۾��� ���� ������ ����ȴ�.
*		   ������ ���� �۾��� ����Ǿ� ����.
*		   1. ������ �ش� ����� ��ɾ ����� �ٲٴ� �۾��� �����Ѵ�.
* �Ű� : ����
* ��ȯ : �������� = 0, �����߻� = < 0
* ���� :
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

	//��ū �и��� �͵��� ������ pass2 ����
	for (int i = 0; i < token_line; i++) {
		if (token_table[i]->type == T_DIRECTIVE) {
			if (!strcmp(token_table[i]->directive, "START")) {//���α׷��� �����ϸ�
				control_section[cs_num].startAddr = atoi(token_table[i]->operand[0]);
				now_area = token_table[i]->section;
				strcpy(control_section[cs_num].section,now_area);
			}
			else if (!strcmp(token_table[i]->directive, "CSECT")) { //�������α׷��� �����ϸ�
				now_area = token_table[i]->section;
				cs_num++;
				control_section[cs_num].startAddr = 0;
				strcpy(control_section[cs_num].section, now_area);
			}
			else if (!strcmp(token_table[i]->directive, "EXTDEF")) { //EXTDEF �ڿ� ������ ���� ����
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
			else if (!strcmp(token_table[i]->directive, "EXTREF")) {//EXTREF �ڿ� ������ ���� ����
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
			else if (!strcmp(token_table[i]->directive, "LTORG")) { //���α׷��� ���Դ� literal������ �޸𸮿� ����
				literal_return();
				continue;
			}
			else if (!strcmp(token_table[i]->directive, "END")) { //���α׷��� ���Դ�literal������ �޸𸮿� ����
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

				if (isdigit(token_table[i]->operand[0][0])) { // ���ڶ��
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%X", atoi(token_table[i]->operand[0][0]));
				}
				else { // �����϶��� ó��
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
			if (token_table[i]->plus_check == 1) { //��ɾ 4������ ��
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
			if (inst_table[token_table[i]->op_index]->format == 1) {//1������ ��
				sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X", inst_table[token_table[i]->op_index]->opcode);
			}
			else if (inst_table[token_table[i]->op_index]->format == 2) { //2������ ��
				if(inst_table[token_table[i]->op_index]->operand_num == 1){ //�ǿ������� ������ 1���� ��
					int reg_num;
					for (int j = 0; j < 10; j++) {
						if (!strcmp(token_table[i]->operand[0], reg[j])){
							reg_num = j;
							break;
						}
					} 
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%01X%01X", token_table[i]->opnum,reg_num, 0);
				}
				else if (inst_table[token_table[i]->op_index]->operand_num == 2) { //�ǿ������� ������ 2���� ��
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
			else if (inst_table[token_table[i]->op_index]->format == 3) { //3������ ��
				if (inst_table[token_table[i]->op_index]->operand_num == 0) { //�ǿ����ڰ� �������� ���� ��
					sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%04X", token_table[i]->opnum, 0);
					control_section[cs_num].obj_line++;
					continue;
				}
				else if (token_table[i]->operand[0] != NULL) { //�ǿ����ڰ� ������ ��
					if (strstr(token_table[i]->operand[0], "#") != NULL) { //immediate addressing�� ��
						token_table[i]->operand[0]++;
						sprintf(control_section[cs_num].obj_code[control_section[cs_num].obj_line], "%02X%04X", token_table[i]->opnum, atoi(token_table[i]->operand[0]));
						control_section[cs_num].obj_line++;
						continue;
					}
					if (strstr(token_table[i]->operand[0], "@") != NULL){ //indirection addressing�� ��
						token_table[i]->operand[0]++;
					}
					if (strstr(token_table[i]->operand[0], "=") != NULL) { //�ǿ����ڰ� literal�� �� 
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
* ���� : �Էµ� ���ڿ��� �̸��� ���� ���Ͽ� ���α׷��� ����� �����ϴ� �Լ��̴�.
*        ���⼭ ��µǴ� ������ object code (������Ʈ 1��) �̴�.
* �Ű� : ������ ������Ʈ ���ϸ�
* ��ȯ : ����
* ���� : ���� ���ڷ� NULL���� ���´ٸ� ���α׷��� ����� ǥ��������� ������
*        ȭ�鿡 ������ش�.
*
* -----------------------------------------------------------------------------------
*/
void make_objectcode_output(char *file_name)
{
	/* add your code here */
	FILE* fp;
	if (file_name != NULL) { //���ڷ� NULL���� ������ �ʴ� ��� ���Ͽ� ���
		fp = fopen(file_name, "w+t");
	}
	else { //���ڷ� NULL���� ������ ��� ǥ��������� ȭ�鿡 ���
		fp = stdout;
	}
	int line_len = 0;
	int check_line = 0;
	int byte_num = 0;
	int start = 0;
	int line_max = 0;
	//object code ������ �Ʒ��� �������� ���
	for (int i = 0; i < cs_num; i++) {
		//Header record �ۼ�
		fprintf(fp, "H%-6s%06X%06X\n", control_section[i].section, control_section[i].startAddr, control_section[i].lastAddr);
		
		if (control_section[i].extdef != 0) { //Define record �ۼ�
			fprintf(fp, "D");
			for (int j = 0; j < control_section[i].extdef; j++) {
				fprintf(fp, "%-6s%06X", control_section[i].def[j].symbol, control_section[i].def[j].addr);
			}
			fprintf(fp, "\n");
		}

		if (control_section[i].extref != 0) { //Refer record �ۼ�
			fprintf(fp, "R");
			for (int j = 0; j < control_section[i].extref; j++) {
				fprintf(fp, "%-6s", control_section[i].ref[j].symbol);
			}
			fprintf(fp, "\n");
		}
		
		//Text record �ۼ�
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
			if (line_len > 60) { //��������� byte�� ������ ���ٿ� ���� �ִ� �з����� ���ٸ�
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
		if (line_len) { // ���� byte�� ������ 60���� ���� ��
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
			while(control_section[i].obj_line - check_line) { //���� �� ������ �ʾ��� ��
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

		if (control_section[i].extref != 0) { //Modification record �ۼ�
			for (int j = 0; j < control_section[i].mod_num; j++) {
				fprintf(fp, "M%06X%02X%s\n", control_section[i].mod[j].addr, control_section[i].ref_size[j], control_section[i].mod[j].symbol);
			}
		}

		if (i == 0) { //End record �ۼ�
			fprintf(fp, "E%06X\n\n", control_section[i].startAddr);
		}
		else {
			fprintf(fp, "E\n\n");
		}
	}

	if (file_name != NULL) {//������ ���� ��� �ݾ���.
		fclose(fp);
	}
}
