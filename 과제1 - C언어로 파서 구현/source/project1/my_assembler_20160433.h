/* 
 * my_assembler �Լ��� ���� ���� ���� �� ��ũ�θ� ��� �ִ� ��� �����̴�. 
 * 
 */
#define MAX_INST 256
#define MAX_LINES 5000
#define MAX_OPERAND 3

#define N (1<<5)
#define I (1<<4)
#define X (1<<3)
#define B (1<<2)
#define P (1<<1)
#define E 1

/*
 * instruction ��� ���Ϸ� ���� ������ �޾ƿͼ� �����ϴ� ����ü �����̴�.
 * ������ ������ instruction set�� ��Ŀ� ���� ���� �����ϵ�
 * ���� ���� �ϳ��� instruction�� �����Ѵ�.
 */
struct inst_unit
{
	/* add your code here */
	char instruction[10]; //��ɾ�
	int opcode; //�� ��ɾ �ش��ϴ� opcode
	int format;//��ɾ��� ����
	int operand_num; //�� ����� �ǿ����� ����
};

// instruction�� ������ ���� ����ü�� �����ϴ� ���̺� ����
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
 * ����� �� �ҽ��ڵ带 �Է¹޴� ���̺��̴�. ���� ������ ������ �� �ִ�.
 */
char *input_data[MAX_LINES];
static int line_num;

/*TYPE�� ���������� ����*/
typedef enum {
	T_DIRECTIVE,
	T_INSTRUCTION,
	T_LITERAL,
	T_COMMENT
} TYPE;

/*
 * ����� �� �ҽ��ڵ带 ��ū������ �����ϱ� ���� ����ü �����̴�.
 * operator�� renaming�� ����Ѵ�.
 * nixbpe�� 8bit �� ���� 6���� bit�� �̿��Ͽ� n,i,x,b,p,e�� ǥ���Ѵ�.
 */
struct token_unit
{
	char *label;				//��ɾ� ���� �� label
	char *operator;				//��ɾ� ���� �� operator
	char *operand[MAX_OPERAND]; //��ɾ� ���� �� operand
	char *comment;				//��ɾ� ���� �� comment
	char nixbpe;				//���� 6bit ���: _ _ n i x b p e
	int op_index;				//���� �߰��� ������ �ҽ��ڵ忡 �����ϴ� ��ɾ ���� ������ �����ϰ� �ִ� inst_table�� index
	int plus_check;				//���� �߰��� ������operand�� +�� ���ԵǾ� �ִ����� ���� ������ ����
	char *directive;			//���� �߰��� ������ ��ɾ� ���� �� ���þ�
	int address;				//���� �߰��� ������ loc ����
	char section[6];			//���� �߰��� ������ �ش� ������ � ���α׷��� �����ִ��� ����
	TYPE type;					 //���� �߰��� ������ Ÿ���� ���
	int opnum;					//object code�� �� ���� 2���� 16���� ���� ����
};

typedef struct token_unit token;
token *token_table[MAX_LINES];
static int token_line;
char* now_area;

/*
 * �ɺ��� �����ϴ� ����ü�̴�.
 * �ɺ� ���̺��� �ɺ� �̸�, �ɺ��� ��ġ�� �����ȴ�.
 */
struct symbol_unit
{
	char symbol[6];
	int addr;
	char type; //���� �߰��� ������ symbol�� 'R'(relative expression)���� 'A'(absolute expression)���� ����
	char* area; //���� �߰��� ������ �ش� �ɺ��� � ���α׷��� ���ԵǾ� �ִ��� ����
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];
static int sym_num;

/*
* ���ͷ��� �����ϴ� ����ü�̴�.
* ���ͷ� ���̺��� ���ͷ��� �̸�, ���ͷ��� ��ġ�� �����ȴ�.
*/
struct literal_unit
{
	char literal[6];
	int addr;
	char type; //���� �߰��� ������ literal�� 'C'�� �����ϴ��� 'X'�� �����ϴ��� ���� 
	int alloc;//���� �߰��� ������ LTORG�� ������ �޸𸮰� �Ҵ�Ǿ������� ���� üũ
};

typedef struct literal_unit literal;
literal literal_table[MAX_LINES];
static int literal_num;

static int locctr;

struct CtlSection {//���α׷����� opcode�� �����ϱ� ���� �߰��� ����ü
	char obj_code[MAX_LINES][9]; //�ش� ������ ��ȯ�Ǵ� address ����
	int obj_line; //������ ����
	char section[10]; //� ���α׷��� �����ϴ��� Ȯ���ϱ� ���� �߰��� ����
	int extdef; //���þ� EXTDEF�� ������ �� symbol�� ������ ����
	int extref; //���þ� EXTREF�� ������ �� symbol�� ������ ����
	symbol def[MAX_LINES]; //���þ� EXTDEF�� ������ �� symbol�� ����
	symbol ref[MAX_LINES]; //���þ� EXTREF�� ������ �� symbol�� ����
	symbol mod[MAX_LINES]; //���κ��� �����鼭 operand�� ref�� ������ ��Ÿ���� �� ����
	int mod_num; //���κ��� �����鼭 operand�� ref�� ������ ��Ÿ���� �� ������ ����
	int ref_size[MAX_LINES]; //���κ��� �����鼭 operand�� ref�� ������ ��Ÿ���� �� ũ�⸦ ����
	int lastAddr; //���α׷��� ������ �ּҰ��� ����
	int startAddr; //���α׷��� ���� �ּҰ��� ����
};

typedef struct CtlSection cs;
cs control_section[MAX_LINES];
int cs_num;

//--------------
static char *input_file;
static char *output_file;
int init_my_assembler(void);
int init_inst_file(char *inst_file);
int init_input_file(char *input_file);
int token_parsing(char *str);
int search_opcode(char *str);
int search_directive(char *str);//���� �߰��� �Լ��� ���þ ���� ������ �Լ� �ȿ� ���Խ��� �Ķ���Ͱ� ���þ����� �ƴ����� �Ǻ����ִ� �Լ� 
static int assem_pass1(void);
//void make_opcode_output(char *file_name); //�̹� ������Ʈ������ ������� �ʴ� �Լ�
int make_symtab(char*str);//���� �߰��� �Լ��� symbol_table�� �������ֱ� ���� �Լ�
int search_symtab(char*str);//���� �߰��� �Լ��� symbol_table�� Ž���Ͽ� �ش��ϴ� symbol�� �ּҰ��� ��ȯ�ϴ� �Լ�
int make_literaltab(char*str);//���� �߰��� �Լ��� literal_table�� �����ϴ� �Լ�
void addr_literal_tab();//���� �߰��� �Լ��� make_literaltab()���� �־��� literal���� �ּҸ� �־��ִ� �Լ��̴�.
int search_literaltab(char*str);//���� �߰��� �Լ��� literal_table�� Ž���Ͽ� �ش��ϴ� literal�� �ּҰ��� ��ȯ�ϴ� �Լ�
void literal_return();//���� �߰��� �Լ��� literal table�� �ִ� literal�� �޸𸮿� �Ҵ����ֱ� ���� literal���� ��ȯ���ִ� �Լ�
void make_symtab_output(char *file_name);
void make_literaltab_output(char *file_name);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);
