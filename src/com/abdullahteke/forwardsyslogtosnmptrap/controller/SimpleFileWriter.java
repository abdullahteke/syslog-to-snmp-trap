package com.abdullahteke.forwardsyslogtosnmptrap.controller;
import java.io.*;



public class SimpleFileWriter 
{

	public static SimpleFileWriter openFileForWriting(String filename)
	{
		try {
			return new SimpleFileWriter(new PrintWriter(new FileWriter(filename), true));
		} catch(IOException e) {	
			return null;
		}	
	}	

	public void print(String s)
	{
		writer.print(s);
	}


	public void println(String s)
	{
		writer.println(s);
	}
	
	

	public void close()
	{
		writer.close();
	}
	

	private SimpleFileWriter(PrintWriter writer) 
	{
		this.writer = writer;
	}
	
	private PrintWriter writer;

}
