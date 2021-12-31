package SP20_simulator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
@SuppressWarnings("serial")
public class VisualSimulator extends JFrame {
	ResourceManager resourceManager;
	SicLoader sicLoader;
	SicSimulator sicSimulator;
	
	String filePath;
	
	JList<String> instList; //명령어 리스트
	JList<String> logList; //로그리스트
	JTextField txtProgramName = new JTextField(); //H 프로그램이름
	JTextField txtObjectProgram = new JTextField(); //H 프로그램 시작주소
	JTextField txtLengthOfProgram = new JTextField(); //H 프로그램 길이
	JTextField txtInObjectProgram = new JTextField(); //E 프로그램 시작주소
	JTextField txtADec = new JTextField();
	JTextField txtAHex = new JTextField();
	JTextField txtXDec = new JTextField();
	JTextField txtXHex = new JTextField();
	JTextField txtLDec = new JTextField();
	JTextField txtLHex = new JTextField();
	JTextField txtBDec = new JTextField();
	JTextField txtBHex = new JTextField();
	JTextField txtSDec = new JTextField();
	JTextField txtSHex = new JTextField();
	JTextField txtTDec = new JTextField();
	JTextField txtTHex = new JTextField();
	JTextField txtF = new JTextField();
	JTextField txtPCDec = new JTextField();
	JTextField txtPCHex = new JTextField();
	JTextField txtSW = new JTextField();
	JTextField txtStart = new JTextField(); //start Address in Memory
	JTextField txtTarget = new JTextField(); //Target Address
	JTextField txtDevice = new JTextField(); //사용중인 장치
	
	JScrollPane scrollPaneInst;
	JScrollPane scrollPane;
	
	public VisualSimulator() { //프레임 만들기 및 초기화
		super("SIC/XE Simulator");
				
		resourceManager = new ResourceManager();
		sicLoader = new SicLoader(resourceManager);
		sicSimulator = new SicSimulator(resourceManager);
		
		setBounds(100 , 100 , 500 , 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null); //절대 위치를 사용하기 위한 설정
		
		//파일오픈 부분
		JLabel fileName = new JLabel("FileName:");
		fileName.setBounds(10, 20, 100, 10);
		add(fileName);
		JTextField inputText = new JTextField();
		inputText.setBounds(75,16,120,20);
		add(inputText);
		
		JButton btnOpen = new JButton("open"); // 파일 오픈버튼 생성
		btnOpen.setBounds(200, 16, 70, 18);
		add(btnOpen);
		
		//H부분
		JPanel panelH = new JPanel();
		panelH.setBorder(new TitledBorder(null,"H (Header Record)"));
		panelH.setBounds(10,45,230,125);
		add(panelH);
		panelH.setLayout(null);
		
		JLabel lbProgramName = new JLabel("Program Name:");
		lbProgramName.setBounds(16,22,100,20);
		panelH.add(lbProgramName);
		
		txtProgramName.setBounds(110,22,100,23);
		txtProgramName.setEditable(false);
		panelH.add(txtProgramName);

		JLabel lbStartAddressOf = new JLabel("Start Address Of");
		lbStartAddressOf.setBounds(16,46,100,20);
		panelH.add(lbStartAddressOf);
		JLabel lbObjectProgram = new JLabel("Object Program:");
		lbObjectProgram.setBounds(33,63,100,20);
		panelH.add(lbObjectProgram);
		
		txtObjectProgram.setBounds(130,56,80,23);
		txtObjectProgram.setEditable(false);
		panelH.add(txtObjectProgram);
		
		JLabel lbLengthOfProgram = new JLabel("Length of Program:");
		lbLengthOfProgram.setBounds(16,87,120,20);
		panelH.add(lbLengthOfProgram);
		
		txtLengthOfProgram.setBounds(131,88,80,23);
		txtLengthOfProgram.setEditable(false);
		panelH.add(txtLengthOfProgram);
		
		//E부분
		JPanel panelE = new JPanel();
		panelE.setBorder(new TitledBorder(null,"E (End Record)"));
		panelE.setBounds(260,45,210,77);
		add(panelE);
		panelE.setLayout(null);
		
		JLabel lbAddressOfFirstInstruction = new JLabel("Address of First Instruction");
		lbAddressOfFirstInstruction.setBounds(16,22,180,20);
		panelE.add(lbAddressOfFirstInstruction);
		
		JLabel lbInObjectProgram = new JLabel("in Object Program:");
		lbInObjectProgram.setBounds(20,40,120,20);
		panelE.add(lbInObjectProgram);
		
		txtInObjectProgram.setBounds(130,42,60,23);
		txtInObjectProgram.setEditable(false);
		panelE.add(txtInObjectProgram);
		
		//Register부분
		JPanel panelReg = new JPanel();
		panelReg.setBorder(new TitledBorder(null,"Register"));
		panelReg.setBounds(10,180,230,275);
		add(panelReg);
		panelReg.setLayout(null);
		
		JLabel lbDec = new JLabel("Dec");
		lbDec.setBounds(85,20,30,20);
		panelReg.add(lbDec);
		JLabel lbHex = new JLabel("Hex");
		lbHex.setBounds(150,20,30,20);
		panelReg.add(lbHex);
		JLabel lbA = new JLabel("A(#0)");
		lbA.setBounds(30,45,50,20);
		panelReg.add(lbA);
		JLabel lbX = new JLabel("X(#1)");
		lbX.setBounds(30,70,50,20);
		panelReg.add(lbX);
		JLabel lbL = new JLabel("L(#2)");
		lbL.setBounds(30,95,50,20);
		panelReg.add(lbL);
		JLabel lbB = new JLabel("B(#3)");
		lbB.setBounds(30,120,40,20);
		panelReg.add(lbB);
		JLabel lbS = new JLabel("S(#4)");
		lbS.setBounds(30,145,40,20);
		panelReg.add(lbS);
		JLabel lbT = new JLabel("T(#5)");
		lbT.setBounds(30,170,40,20);
		panelReg.add(lbT);
		JLabel lbF = new JLabel("F(#6)");
		lbF.setBounds(30,195,40,20);
		panelReg.add(lbF);
		JLabel lbPC = new JLabel("PC(#8)");
		lbPC.setBounds(25,220,50,20);
		panelReg.add(lbPC);
		JLabel lbSW = new JLabel("SW(#9)");
		lbSW.setBounds(24,245,50,20);
		panelReg.add(lbSW);
		

		txtADec.setBounds(80,45,60,18);
		txtADec.setEditable(false);
		panelReg.add(txtADec);

		txtAHex.setBounds(145,45,60,18);
		txtAHex.setEditable(false);
		panelReg.add(txtAHex);

		txtXDec.setBounds(80,70,60,18);
		txtXDec.setEditable(false);
		panelReg.add(txtXDec);

		txtXHex.setBounds(145,70,60,18);
		txtXHex.setEditable(false);
		panelReg.add(txtXHex);

		txtLDec.setBounds(80,95,60,18);
		txtLDec.setEditable(false);
		panelReg.add(txtLDec);

		txtLHex.setBounds(145,95,60,18);
		txtLHex.setEditable(false);
		panelReg.add(txtLHex);

		txtBDec.setBounds(80,120,60,18);
		txtBDec.setEditable(false);
		panelReg.add(txtBDec);

		txtBHex.setBounds(145,120,60,18);
		txtBHex.setEditable(false);
		panelReg.add(txtBHex);

		txtSDec.setBounds(80,145,60,18);
		txtSDec.setEditable(false);
		panelReg.add(txtSDec);

		txtSHex.setBounds(145,145,60,18);
		txtSHex.setEditable(false);
		panelReg.add(txtSHex);

		txtTDec.setBounds(80,170,60,18);
		txtTDec.setEditable(false);
		panelReg.add(txtTDec);

		txtTHex.setBounds(145,170,60,18);
		txtTHex.setEditable(false);
		panelReg.add(txtTHex);

		txtF.setBounds(80,195,125,18);
		txtF.setEditable(false);
		panelReg.add(txtF);

		txtPCDec.setBounds(80,220,60,18);
		txtPCDec.setEditable(false);
		panelReg.add(txtPCDec);

		txtPCHex.setBounds(145,220,60,18);
		txtPCHex.setEditable(false);
		panelReg.add(txtPCHex);

		txtSW.setBounds(80,245,125,18);
		txtSW.setEditable(false);
		panelReg.add(txtSW);
		
		//Start Address in Memory
		JLabel lbStart = new JLabel("Start Address in Memory");
		lbStart.setBounds(270,135,170,20);
		add(lbStart);

		txtStart.setEditable(false);
		txtStart.setBounds(370,155,93,20);
		add(txtStart);
		JLabel lbTarget = new JLabel("Target Address:");
		lbTarget.setBounds(270,180,100,20);
		add(lbTarget);

		txtTarget.setBounds(370,180,93,20);
		txtTarget.setEditable(false);
		add(txtTarget);
		
		//instruction
		JLabel lbInstructions = new JLabel("Instructions:");
		lbInstructions.setBounds(270,210,100,20);
		add(lbInstructions);
		JTextArea txtInstructions = new JTextArea();
		scrollPaneInst = new JScrollPane(txtInstructions); //스크롤 추가
		scrollPaneInst.setBounds(260,230,106,225);
		add(scrollPaneInst);
		instList = new JList<>();
		scrollPaneInst.setViewportView(instList);
		
		JLabel lbDevice = new JLabel("사용중인 장치");
		lbDevice.setFont(new Font("고딕",Font.PLAIN,12));
		lbDevice.setBounds(385,230,100,20);
		add(lbDevice);

		txtDevice.setBounds(395,250,60,20);
		txtDevice.setEditable(false);
		add(txtDevice);
		
		//실행버튼
		JButton btn1Step = new JButton("실행(1 Step)");
		btn1Step.setFont(new Font("고딕",Font.PLAIN,10));
		btn1Step.setEnabled(false);
		btn1Step.setBounds(375, 365, 95, 20);
		btn1Step.addActionListener(new ActionListener() { //실행(1 Step)버튼을 눌렀을 때
			public void actionPerformed(ActionEvent arg0) {
				oneStep();
				update();
			}
		});
		add(btn1Step);
		
		JButton btnStep = new JButton("실행 (All)");
		btnStep.setFont(new Font("고딕",Font.PLAIN,10));
		btnStep.setEnabled(false);
		btnStep.setBounds(375, 395, 95, 20);
		btnStep.addActionListener(new ActionListener() { //실행(All Step)버튼을 눌렀을 때
			public void actionPerformed(ActionEvent arg0) {
				allStep();
				update();
			}
		});
		add(btnStep);
		
		btnOpen.addActionListener(new ActionListener() { //오픈버튼을 눌렀을 때
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(); // open할 파일을 선택할 때 JfileChooser를 쓰는 방식 예제
				chooser.setDialogTitle("열기");
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Object File (*.obj)", "txt", "obj");
				chooser.setFileFilter(filter);
				
				int ret = chooser.showOpenDialog(null);
				if( ret == JFileChooser.APPROVE_OPTION){
					
					filePath = chooser.getSelectedFile().getPath();
					String file = chooser.getSelectedFile().getName();
					inputText.setText(file);
					
					try {
						load(chooser.getSelectedFile());
						update();
						btn1Step.setEnabled(true);
						btnStep.setEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {//사용자가 창을 강제로 닫았거나 취소버튼을 누른 경우
					JOptionPane.showMessageDialog(null,"Not Choose File","WARNING",JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		});
		
		JButton btnExit = new JButton("종료");
		btnExit.setFont(new Font("고딕",Font.PLAIN,10));
		btnExit.setBounds(375, 425, 95, 20);
		btnExit.addActionListener(new ActionListener() { //종료버튼을 눌렀을 때
			public void actionPerformed(ActionEvent arg0) {
				resourceManager.closeDevice();
				System.exit(0);
			}
		});
		add(btnExit);
		
		//Log부분
		JLabel lbLog = new JLabel("Log(명령어 수행 관련):");
		lbLog.setBounds(20,457,150,30);
		add(lbLog);

		JTextArea txtLog = new JTextArea();
		scrollPane = new JScrollPane(txtLog); //스크롤 추가
		scrollPane.setBounds(10,483,460,165);
		add(scrollPane);
		logList = new JList<>();
		scrollPane.setViewportView(logList);
		
		setVisible(true);
	}
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 */
	public void load(File program){
		//...
		sicSimulator.load(program);
		sicLoader.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 */
	public void oneStep(){
		sicSimulator.oneStep();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep(){
		sicSimulator.allStep();
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(){
		
		//log 출력
		String[] logStringList = sicSimulator.getLogList().toArray(new String[sicSimulator.getLogList().size()]);
		logList.setListData(logStringList);
		
		//명령어 리스트 출력
		String[] instStringList = sicSimulator.getInstList().toArray(new String[sicSimulator.getInstList().size()]);
		instList.setListData(instStringList);
		
		//스크롤 위치 아래로 조정
		scrollPaneInst.getVerticalScrollBar().setValue(scrollPaneInst.getVerticalScrollBar().getMaximum());
		scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
		
		//현재 실행중인 프로그램 섹션 이름 출력
		txtProgramName.setText(resourceManager.getProgName(resourceManager.getCurrentSection()));
		
		//현재 실행중인 프로그램의 시작주소
		String start = "";
		if(resourceManager.getCurrentSection() == 0){
			start = String.format("%06X", resourceManager.getProgStart(resourceManager.getCurrentSection()));
			txtObjectProgram.setText(start);
			txtInObjectProgram.setText(start);
		}
		else {
			start = String.format("%06X", resourceManager.getProgStart(resourceManager.getCurrentSection()) - resourceManager.getProgLength(resourceManager.getCurrentSection()-1));
			txtObjectProgram.setText(start);
			txtInObjectProgram.setText(start);
		}

		//프로그램의 전체 길이
		txtLengthOfProgram.setText(String.format("%06X",resourceManager.getProgTotalLen()));
		
		//레지스터 정보
		txtADec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.A_REGISTER)));
		txtAHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.A_REGISTER)));
		
		txtXDec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.X_REGISTER)));
		txtXHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.X_REGISTER)));
		
		txtLDec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.L_REGISTER)));
		txtLHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.L_REGISTER)));
		
		txtBDec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.B_REGISTER)));
		txtBHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.B_REGISTER)));
		
		txtSDec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.S_REGISTER)));
		txtSHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.S_REGISTER)));
		
		txtSDec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.S_REGISTER)));
		txtSHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.S_REGISTER)));

		txtTDec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.T_REGISTER)));
		txtTHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.T_REGISTER)));

		txtF.setText(String.format("%f", resourceManager.register_F));
		
		txtPCDec.setText(String.format("%d", resourceManager.getRegister(SicSimulator.PC_REGISTER)));
		txtPCHex.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.PC_REGISTER)));
		
		txtSW.setText(String.format("%06X", resourceManager.getRegister(SicSimulator.SW_REGISTER)));

		//start Address in Memory
		txtStart.setText(String.format("%06X", resourceManager.getProgStart(resourceManager.getCurrentSection())));

		//Target Address
		txtTarget.setText(String.format("%06X", sicSimulator.getAddress()));

		//사용중인 장치
		txtDevice.setText(sicSimulator.getDevice());
			
	};
	
	public static void main(String[] args) {
		new VisualSimulator();
	}
}
