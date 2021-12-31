package SP20_simulator;

// instruction에 따라 동작을 수행하는 메소드를 정의하는 클래스

public class InstLuncher {
    ResourceManager rMgr;
    int targetAddr = 0;

    public InstLuncher(ResourceManager resourceManager) {
        this.rMgr = resourceManager;
    }

    // instruction 별로 동작을 수행하는 메소드를 정의
    // ex) public void add(){...}
    
    /**
     * L 레지스터의 값을 메모리에 저장하는 메소드이다.
     * m..m+2 <- (L)
     * @param start instruction이 위치한 시작주소
     * @param format instruction의 형식
     * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
     */
    public void stl(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
    		
    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
    		char[] data =  rMgr.intToChar(rMgr.getRegister(SicSimulator.L_REGISTER));
    		rMgr.setMemory(address, data, 3);
    	}
    	
    }
    /**
    * PC에 저장되어 있는 값을 L레지스터에 저장후 메모리의 값을 PC레지스터에 저장하는 메소드이다.
    * L <- (PC); PC <- (m)
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    */
    public void jsub(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	
    	if(format == 4) {
    		rMgr.setRegister(SicSimulator.L_REGISTER, rMgr.getRegister(SicSimulator.PC_REGISTER));
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2];
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[3];
    		rMgr.setRegister(SicSimulator.PC_REGISTER, tmp);
    	}
    	
    }
    /**
    * A <- (m..m+2)
    * 메모리의 값을 읽어 A레지스터에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    */
    public void lda(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		if(addressing == 1) { // immediate addressing일 때
        		int tmp = instruction[1];
        		tmp = tmp << 8;
        		tmp = tmp | (int)instruction[2];
        		rMgr.setRegister(SicSimulator.A_REGISTER, tmp);
        
    		}
    		else if(addressing == 3) {
	    		int tmp = instruction[1] & 15;
	    		tmp = tmp << 8;
	    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
	    		
	    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
	    		char[] data =  rMgr.getMemory(address, 3);
	    		rMgr.setRegister(SicSimulator.A_REGISTER, rMgr.byteToInt(data));
    		}
    	}
    }
    
    /**
    * (A):(m..m+2)
    * A레지스터의 값과 메모리에 저장되어 있는 값을 비교하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    */
    public void comp(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		if(addressing == 1) { // immediate addressing일 때
        		int tmp = instruction[1];
        		tmp = tmp << 8;
        		tmp = tmp | (int)instruction[2];
        		
        		int dif = rMgr.getRegister(SicSimulator.A_REGISTER) - tmp;
        		if(dif == 0) {
            		rMgr.setRegister(SicSimulator.SW_REGISTER, 0);
            	}
            	else if(dif > 0) {
            		rMgr.setRegister(SicSimulator.SW_REGISTER, 1);
        		}
            	else {
            		rMgr.setRegister(SicSimulator.SW_REGISTER, -1);
            	}
    		}
    		
    	}
    }
    
    /**
    *  PC <-m if CC set to =(0)
    * sw레지스터가 0이라면 메모리에 저장된 값으로 이동하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    */
    public void jeq(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		if(rMgr.getRegister(SicSimulator.SW_REGISTER) == 0) {
	    		int tmp = instruction[1] & 15;
	    		tmp = tmp << 8;
	    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
	    		if((instruction[1] & 15) == 15) {//F로 시작하는 주소의 경우 앞부분을 다 F로 채워넣기..
	    			tmp = tmp |((0xFFFFF)<<12);
	    		}	
	    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
	    		rMgr.setRegister(SicSimulator.PC_REGISTER, address);
    		}
    	}
    }
    
    /***
    * PC <- m
    * 메모리에 저장된 값을 pc레지스터에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    */
    public void j(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
    		if((instruction[1] & 15) == 15) {//F로 시작하는 주소의 경우 앞부분을 다 F로 채워넣기..
    			tmp = tmp |((0xFFFFF)<<12);
    		}	
    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
    		if(addressing == 2){ //indirect addressing이라면
    			address = rMgr.byteToInt(rMgr.getMemory(address, 3));
			}
    		rMgr.setRegister(SicSimulator.PC_REGISTER, address);
    	}
    }
    
    /**
    * m..m+2 <- (A)
    * A레지스터의 값을 메모리에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    */
    public void sta(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
    		
    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
    		char[] data =  rMgr.intToChar(rMgr.getRegister(SicSimulator.A_REGISTER));
    		rMgr.setMemory(address, data, 3);
    	}
    }
    /**
    * r1 <- 0
    * 레지스터의 값을 0으로 초기화하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    */
    public void clear(int start) {
    	char [] instruction = rMgr.getMemory(start, 2);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + 2;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	int regNum = instruction[1];
    	regNum = regNum >>> 4;
    	rMgr.setRegister(regNum, 0);
    }
    
    /**
    * T <- (m..m+2)
    * 메모리의 값을 읽어와 T레지스터에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    */
    public void ldt(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 4) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2];
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[3];
    		char[] data = rMgr.getMemory(tmp, 3);
    		rMgr.setRegister(SicSimulator.T_REGISTER, rMgr.byteToInt(data));
    	
    	}
    	else {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
    		
    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
    		char[] data =  rMgr.getMemory(address, 3);
    		rMgr.setRegister(SicSimulator.T_REGISTER, rMgr.byteToInt(data));
    	}
    }
    
    /**
    //Test device specified by (m)
    //장치(파일)를 확인하는 메소드로 sw레지스터가 0이면 준비안된 것. 0이 아니라면 준비된 것.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    * @return 확인하고 있는 장치의 이름 
    */
    public String td(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
    		
    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
    		char[] deviceAddr = rMgr.getMemory(address, 1);
    		String deviceName = String.format("%X%X", deviceAddr[0]>>4,deviceAddr[0]&15);
    		rMgr.testDevice(deviceName);
    		return deviceName;
    	}
    	return null;
    }
    
    /**
    * A[rightmost byte] <- data from device specified by (m)
    * 기기(파일)에서 한바이트 읽어와 A레지스터의 제일 오른쪽 1바이트에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    **/
    public void rd(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
    		
    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
    		char[] deviceAddr = rMgr.getMemory(address, 1);
    		String deviceName = String.format("%X%X", deviceAddr[0]>>4,deviceAddr[0]&15);
    		char[] data = rMgr.readDevice(deviceName, rMgr.getRegister(SicSimulator.T_REGISTER));
    		char[] tmp2 = new char[1];
    		tmp2[0] = data[0];
    		rMgr.setRegister(SicSimulator.A_REGISTER, rMgr.byteToInt(tmp2));
    	}
    }
    
    /**
    * (r1) : (r2)
    * r1레지스터에 저장된 값과 r2 레지스터에 저장된 값을 비교하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    **/
    public void compr(int start) {
    	char [] instruction = rMgr.getMemory(start, 2);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + 2;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	
    	int regNum1 = instruction[1] >>> 4;
    	int regNum2 = instruction[1] & 15;
    	int dif = rMgr.getRegister(regNum1) - rMgr.getRegister(regNum2);
    	if(dif == 0) {
    		rMgr.setRegister(SicSimulator.SW_REGISTER, 0);
    	}
    	else if(dif > 0) {
    		rMgr.setRegister(SicSimulator.SW_REGISTER, 1);
		}
    	else {
    		rMgr.setRegister(SicSimulator.SW_REGISTER, -1);
    	}
    }
    
    /**
    * m <- (A)[rightmost byte]
    * A레지스터의 제일 오른쪽 한바이트를 메모리에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    **/
    public void stch(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 4) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2];
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[3];
    		
    		int tmpData = rMgr.getRegister(SicSimulator.A_REGISTER) & 255;
    		char[] data = rMgr.intToChar(tmpData);
    		char[] tmp2 = new char[1];
    		tmp2[0]= data[2];
    		rMgr.setMemory(tmp + rMgr.getRegister(SicSimulator.X_REGISTER), tmp2, 1);
    	}
    }
    
    /**
    * X <- (X) + 1; (X):(r1)
    * X레지스터의 값을 1 증가시키고 r1레지스터와 비교하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    **/
    public void tixr(int start) {
    	char [] instruction = rMgr.getMemory(start, 2);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + 2;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	
    	rMgr.setRegister(SicSimulator.X_REGISTER, rMgr.getRegister(SicSimulator.X_REGISTER)+1);
    	
    	int regNum = instruction[1];
    	regNum = regNum >>> 4;
    	int dif = rMgr.getRegister(SicSimulator.X_REGISTER) - rMgr.getRegister(regNum);
    	if(dif == 0) {
    		rMgr.setRegister(SicSimulator.SW_REGISTER, 0);
    	}
    	else if(dif > 0) {
    		rMgr.setRegister(SicSimulator.SW_REGISTER, 1);
		}
    	else {
    		rMgr.setRegister(SicSimulator.SW_REGISTER, -1);
    	}
    	
    }
    
    /**
    * PC <- m if CC set to <(-1)
    * SW 레지스터의 값이 -1이라면 메모리에 저장된 값을 PC레지스터에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    **/
    public void jlt(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {
    		if(rMgr.getRegister(SicSimulator.SW_REGISTER) == -1) {
	    		int tmp = instruction[1] & 15;
	    		tmp = tmp << 8;
	    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
	    		if((instruction[1] & 15) == 15) {//F로 시작하는 주소의 경우 앞부분을 다 F로 채워넣기..
	    			tmp = tmp |((0xFFFFF)<<12);
	    		}	
	    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
	    		rMgr.setRegister(SicSimulator.PC_REGISTER, address);
    		}
    	}
    }
    
    /**
    //m .. m+2 <- (X)
    //x레지스터의 값을 메모리에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    **/
    public void stx(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 4) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2];
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[3];
    		
    		char[] data = rMgr.intToChar(rMgr.getRegister(SicSimulator.X_REGISTER));
    		rMgr.setMemory(tmp, data, 3);
    	
    	}

    }
    /**
    //PC <- (L)
    //L레지스터에 저장된 값을 pc레지스터에 저장하는 메소드이다.
    **/
    public void rsub() {
    	rMgr.setRegister(SicSimulator.PC_REGISTER, rMgr.getRegister(SicSimulator.L_REGISTER));
    }
    
    /**
    * (A)[rightmost byte] <- (m) 
    * 메모리의 값을 A의 제일 오른쪽 한 바이트에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    **/
    public void ldch(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 4) {
    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2];
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[3];
    		
    		char[] data = rMgr.getMemory(tmp + rMgr.getRegister(SicSimulator.X_REGISTER), 1);
    		rMgr.setRegister(SicSimulator.A_REGISTER, rMgr.byteToInt(data));
    	}

    }
    
    /**
    * Device specified by (m) <- (A)[rightmost byte] 
    * A레지스터에 저장된 값 중 제일 오른쪽 한바이트를 장치(파일)에 저장하는 메소드이다.
    * 메모리의 값을 A의 제일 오른쪽 한 바이트에 저장하는 메소드이다.
    * @param start instruction이 위치한 시작주소
    * @param format instruction의 형식
    * @param addressing instruction의 addressing 방식 (1 : immediate, 2 : indirect, 3: direct)
    **/
    public void wd(int start, int format, int addressing) {
    	char [] instruction = rMgr.getMemory(start, format);
    	targetAddr = rMgr.getRegister(SicSimulator.PC_REGISTER) + format;
    	rMgr.setRegister(SicSimulator.PC_REGISTER, targetAddr);
    	if(format == 3) {

    		int tmp = instruction[1] & 15;
    		tmp = tmp << 8;
    		tmp = tmp | (int)instruction[2]; //뒤의 3자리 주소값 찾기 
    		
    		int address = tmp + rMgr.getRegister(SicSimulator.PC_REGISTER);
    		char[] deviceAddr = rMgr.getMemory(address, 1);
    		String deviceName = String.format("%X%X", deviceAddr[0]>>4,deviceAddr[0]&15);
    		int regData = rMgr.getRegister(SicSimulator.A_REGISTER);
    		char[] data = rMgr.intToChar(regData);
    		char[] tmp2 = new char[1];
    		tmp2[0] = data[2];
    		rMgr.writeDevice(deviceName,tmp2,1);
    		}
    }

}