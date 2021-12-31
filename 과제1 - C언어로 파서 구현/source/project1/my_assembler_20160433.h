/* 
 * my_assembler 함수를 위한 변수 선언 및 매크로를 담고 있는 헤더 파일이다. 
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
 * instruction 목록 파일로 부터 정보를 받아와서 생성하는 구조체 변수이다.
 * 구조는 각자의 instruction set의 양식에 맞춰 직접 구현하되
 * 라인 별로 하나의 instruction을 저장한다.
 */
struct inst_unit
{
	/* add your code here */
	char instruction[10]; //명령어
	int opcode; //각 명령어에 해당하는 opcode
	int format;//명령어의 형식
	int operand_num; //각 명령의 피연산자 개수
};

// instruction의 정보를 가진 구조체를 관리하는 테이블 생성
typedef struct inst_unit inst;
inst *inst_table[MAX_INST];
int inst_index;

/*
 * 어셈블리 할 소스코드를 입력받는 테이블이다. 라인 단위로 관리할 수 있다.
 */
char *input_data[MAX_LINES];
static int line_num;

/*TYPE을 열거형으로 선언*/
typedef enum {
	T_DIRECTIVE,
	T_INSTRUCTION,
	T_LITERAL,
	T_COMMENT
} TYPE;

/*
 * 어셈블리 할 소스코드를 토큰단위로 관리하기 위한 구조체 변수이다.
 * operator는 renaming을 허용한다.
 * nixbpe는 8bit 중 하위 6개의 bit를 이용하여 n,i,x,b,p,e를 표시한다.
 */
struct token_unit
{
	char *label;				//명령어 라인 중 label
	char *operator;				//명령어 라인 중 operator
	char *operand[MAX_OPERAND]; //명령어 라인 중 operand
	char *comment;				//명령어 라인 중 comment
	char nixbpe;				//하위 6bit 사용: _ _ n i x b p e
	int op_index;				//새로 추가한 변수로 소스코드에 존재하는 명령어에 대한 정보를 포함하고 있는 inst_table의 index
	int plus_check;				//새로 추가한 변수로operand에 +가 포함되어 있는지에 대한 정보를 저장
	char *directive;			//새로 추가한 변수로 명령어 라인 중 지시어
	int address;				//새로 추가한 변수로 loc 저장
	char section[6];			//새로 추가한 변수로 해당 라인이 어떤 프로그램에 속해있는지 저장
	TYPE type;					 //새로 추가한 변수로 타입을 명시
	int opnum;					//object code에 들어갈 상위 2개의 16진수 값을 저장
};

typedef struct token_unit token;
token *token_table[MAX_LINES];
static int token_line;
char* now_area;

/*
 * 심볼을 관리하는 구조체이다.
 * 심볼 테이블은 심볼 이름, 심볼의 위치로 구성된다.
 */
struct symbol_unit
{
	char symbol[6];
	int addr;
	char type; //새로 추가한 변수로 symbol이 'R'(relative expression)인지 'A'(absolute expression)인지 구분
	char* area; //새로 추가한 변수로 해당 심볼이 어떤 프로그램에 포함되어 있는지 저장
};

typedef struct symbol_unit symbol;
symbol sym_table[MAX_LINES];
static int sym_num;

/*
* 리터럴을 관리하는 구조체이다.
* 리터럴 테이블은 리터럴의 이름, 리터럴의 위치로 구성된다.
*/
struct literal_unit
{
	char literal[6];
	int addr;
	char type; //새로 추가한 변수로 literal이 'C'로 시작하는지 'X'로 시작하는지 구분 
	int alloc;//새로 추가한 변수로 LTORG를 만나서 메모리가 할당되었는지의 여부 체크
};

typedef struct literal_unit literal;
literal literal_table[MAX_LINES];
static int literal_num;

static int locctr;

struct CtlSection {//프로그램별로 opcode를 저장하기 위해 추가한 구조체
	char obj_code[MAX_LINES][9]; //해당 라인이 변환되는 address 저장
	int obj_line; //라인의 개수
	char section[10]; //어떤 프로그램을 관리하는지 확인하기 위해 추가한 변수
	int extdef; //지시어 EXTDEF가 나왔을 때 symbol의 개수를 저장
	int extref; //지시어 EXTREF가 나왔을 때 symbol의 개수를 저장
	symbol def[MAX_LINES]; //지시어 EXTDEF가 나왔을 때 symbol을 저장
	symbol ref[MAX_LINES]; //지시어 EXTREF가 나왔을 때 symbol을 저장
	symbol mod[MAX_LINES]; //라인별로 읽으면서 operand에 ref인 변수가 나타났을 때 저장
	int mod_num; //라인별로 읽으면서 operand에 ref인 변수가 나타나면 그 개수를 저장
	int ref_size[MAX_LINES]; //라인별로 읽으면서 operand에 ref인 변수가 나타나면 그 크기를 저장
	int lastAddr; //프로그램의 마지막 주소값을 저장
	int startAddr; //프로그램의 시작 주소값을 저장
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
int search_directive(char *str);//새로 추가한 함수로 지시어에 대한 정보를 함수 안에 포함시켜 파라미터가 지시어인지 아닌지를 판별해주는 함수 
static int assem_pass1(void);
//void make_opcode_output(char *file_name); //이번 프로젝트에서는 사용하지 않는 함수
int make_symtab(char*str);//새로 추가한 함수로 symbol_table을 생성해주기 위한 함수
int search_symtab(char*str);//새로 추가한 함수로 symbol_table을 탐색하여 해당하는 symbol의 주소값을 반환하는 함수
int make_literaltab(char*str);//새로 추가한 함수로 literal_table을 생성하는 함수
void addr_literal_tab();//새로 추가한 함수로 make_literaltab()에서 넣어준 literal들의 주소를 넣어주는 함수이다.
int search_literaltab(char*str);//새로 추가한 함수로 literal_table을 탐색하여 해당하는 literal의 주소값을 반환하는 함수
void literal_return();//새로 추가한 함수로 literal table에 있는 literal을 메모리에 할당해주기 위해 literal값을 반환해주는 함수
void make_symtab_output(char *file_name);
void make_literaltab_output(char *file_name);
static int assem_pass2(void);
void make_objectcode_output(char *file_name);
