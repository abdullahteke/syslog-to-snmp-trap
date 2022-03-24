package com.abdullahteke.forwardsyslogtosnmptrap.main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.abdullahteke.forwardsyslogtosnmptrap.controller.SimpleFileReader;



public class SendSnmpTrap {

	private String trapAddress="10.6.112.11/162";
	private String community="intra";
	
	private static Logger logger= LogManager.getLogger(SendSnmpTrap.class);
	
	private Vector<String> syslogMessageTypes;
	private Vector<String> nodeIPs;
	
	public SendSnmpTrap() {
		super();
		
		try {
			initiliaze();
			//sendTrap("test mesaji");
			//readFile("config/syslog2.log");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
				
	}
	

	private void readFile(String string) {
		SimpleFileReader hostReader= SimpleFileReader.openFileForReading(string);
		String tmp1= hostReader.readLine();
		
		while (tmp1!=null){
			parseLine(tmp1);
			tmp1=hostReader.readLine();
		}
		
		hostReader.close();
		
	}


	private void parseLine(String tmp1) {
		System.out.println(getIPAddress(tmp1));
		
		
	}


	private String getIPAddress(String tmp1) {
		String retVal=null;
		Pattern ipPattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
		
		String[] stParts= tmp1.split(" ");
		
		for (int i=0;i<stParts.length;i++){
			if(ipPattern.matcher(stParts[i]).matches()){
				retVal=stParts[i];
				break;
			}
		}
		
		return retVal;
		
	}


	private void initiliaze() {
		
		syslogMessageTypes= new Vector<String>();
		nodeIPs=new Vector<String>();

		SimpleFileReader hostReader= SimpleFileReader.openFileForReading("config/hostList.txt");
		SimpleFileReader snmpMsgReader= SimpleFileReader.openFileForReading("config/snmpMsgType.txt");
		
		String tmp1= hostReader.readLine();
		
		while (tmp1!=null){
			nodeIPs.add(tmp1);
			tmp1=hostReader.readLine();
		}
		
		String tmp2= snmpMsgReader.readLine();
		
		while (tmp2!=null){
			syslogMessageTypes.add(tmp2);
			tmp2=snmpMsgReader.readLine();
		}
		
		hostReader.close();
		snmpMsgReader.close();		
	}


	public static void main(String[] args) {
		
		SendSnmpTrap trap= new SendSnmpTrap();
		logger.info("SNMP Trap Sender Uygulamasi Baslatildi");
		trap.startHandleSyslog();
		//trap.print();
		//trap.sendTrap("a b c d 10.63.0.1 test mesaji");
		logger.error("SNMP Trap Sender Uygulamasi Sonlandi");	
	}

	
	private void startHandleSyslog() {
		
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String line=null;
		
		while (true){
			try {				
				line= stdin.readLine();
				if (line!=null){
					if (!checkHost(line) & !checkMsgType(line)){
						sendTrap(line);
					}					
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	
	private boolean checkHost(String line) {
		boolean retVal=false;
		
		for (int i=0;i<nodeIPs.size();i++){
			if (line.indexOf(nodeIPs.get(i))>-1){
				System.out.println("IP yeri:"+line.indexOf(nodeIPs.get(i)));
				retVal=true;
				break;
			}
		}
		
		return retVal;
	}
	
	private boolean checkMsgType(String line) {
		boolean retVal=false;
		
		for (int i=0;i<syslogMessageTypes.size();i++){
			if (line.indexOf(syslogMessageTypes.get(i)) != -1){
				System.out.println("msg yeri:"+line.indexOf(nodeIPs.get(i)));
				retVal=true;
				break;
			}
		}
		
		return retVal;
	}
	
	private void print(){
		for (int i=0;i<syslogMessageTypes.size();i++){
			System.out.println("Message"+syslogMessageTypes.get(i));
		}
	}


	private void sendTrap (String msg) {
	
		//PDU trap = new PDU();
		PDUv1 trap = new PDUv1();
		trap.setGenericTrap(PDUv1.genErr);
		//trap.setType(PDUv1.TRAP);
		trap.setAgentAddress(new IpAddress(getIPAddress(msg)));
		
		OID oid = new OID(".1.3.6.1.6.3.1.1.5.6.0.0");		   
		trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
		
		Variable msgVar = new OctetString(msg);
		VariableBinding syslogmsg= new VariableBinding(new OID(".1.3.6.1.4.1.9.2.2.1.1.20.10035"),msgVar);		
		trap.add(syslogmsg);
		
		Variable nodeIP = new OctetString(getIPAddress(msg));
		VariableBinding nodeIPBind= new VariableBinding(new OID(".1.3.6.1.4.1.9.2.2.1.1.20.10035"),nodeIP);		
		trap.add(nodeIPBind);
		
		//trap.setTimestamp(new Date().getTime());
		      
		// Specify receiver
		Address targetaddress = new UdpAddress(trapAddress);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(community));
		//target.setVersion(SnmpConstants.version2c);
		target.setVersion(SnmpConstants.version1);		
	    target.setAddress(targetaddress);
		   
		try {
			
			Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
			snmp.send(trap, target, null, null);
			
			
		} catch (IOException e) {
			logger.error("Hata:"+e.getMessage());
		}		      
	}
	

	public String getTrapAddress() {
		return trapAddress;
	}

	public void setTrapAddress(String trapAddress) {
		this.trapAddress = trapAddress;
	}

	public Vector<String> getSyslogMessageTypes() {
		return syslogMessageTypes;
	}

	public void setSyslogMessageTypes(Vector<String> syslogMessageTypes) {
		this.syslogMessageTypes = syslogMessageTypes;
	}

	public Vector<String> getHostIPAddresses() {
		return nodeIPs;
	}

	public void setHostIPAddresses(Vector<String> hostIPAddresses) {
		this.nodeIPs = hostIPAddresses;
	}
	

}
