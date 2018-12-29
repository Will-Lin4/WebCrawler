package me.wlin;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;

public class JTextAreaOutputStream extends OutputStream {

	private final JTextArea area;
	public JTextAreaOutputStream(JTextArea area){
		this.area = area;
	}
	
	@Override
	public void write(int i) throws IOException {
		area.append("" + (char)i);	
	}

}
