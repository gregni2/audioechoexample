package com.example.audiocapture;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;

public class AudioWriterThread implements Runnable{
	
		public static AudioRecord recorder;
		public static boolean isRecording;
		public String outputFile = null;
		
		int bufferSizeInBytes = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		byte Data[] = new byte[bufferSizeInBytes];
		
		
		 public void run() {
			 
			 	if (outputFile == null) {
			 		Log.e("AudioWriterThread", "Error. NO OUTPUT DEFINED.");
			 	}
			 	
		 	   // Write the output audio in byte
			   FileOutputStream os = null;
			   try {
				   os = new FileOutputStream(outputFile);
			   } catch (FileNotFoundException e) {
				   e.printStackTrace();
			   }

			   while (isRecording) {
				    // gets the voice output from microphone to byte format
				    int bytesRead = recorder.read(Data, 0, Data.length);
				    Log.d("AudioWriterThread", "Short wirting to file" + Data.toString());
				    try {
					     // writes the data to file from buffer stores the voice buffer
					  os.write(Data, 0, bytesRead);
				    } catch (IOException e) {
				     e.printStackTrace();
				    }
			   }
			   
			   try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		 }
}
