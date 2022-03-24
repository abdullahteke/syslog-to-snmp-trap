package com.abdullahteke.forwardsyslogtosnmptrap.controller;

import java.io.*;


public class SimpleFileReader
{

	public static SimpleFileReader openFileForReading(String filename)
	{
		try {
			return new SimpleFileReader(new BufferedReader(new FileReader(filename)));
		} catch(IOException e) {	
			return null;
		}	
	}
	

	public String readLine()
	{
		try {
			return reader.readLine();
		} catch (IOException e) {
			return null;
		}
	}


	public void close()
	{
		try {
			reader.close();
		} catch (IOException e) {}
	}
	

	private SimpleFileReader(BufferedReader reader) 
	{
		this.reader = reader;
	}
	
	private BufferedReader reader;

}