package com.example.audiocapture;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	// File names.
	public static String sBackgroundAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/background-8k16bitMono.pcm";
	public static String sRegularAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/regular-8k16bitMono.pcm";
	
	// Recorders.
	private AudioRecord mbackgroundRecorder;
	private AudioRecord mForgroundRecorder;
	
   
	// Thread.
	private Thread writeRecordingThread = null;
   
	// Buttons to record and play.
	private Button mStartRecordBackground,stop,play;
  
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      // Setup UI.
      setContentView(R.layout.activity_main);
      mStartRecordBackground = (Button)findViewById(R.id.recordBackground);
      stop = (Button)findViewById(R.id.stopRecording);
      play = (Button)findViewById(R.id.play);
      
      
      // Button state.
      mStartRecordBackground.setEnabled(true);
      stop.setEnabled(false);
      play.setEnabled(false);
      
      
      // Setup the recoding thread.
   
	  
      /*
       *     // Setup the Recorder.
	   //   myAudioRecorder = new MediaRecorder();
	      myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	      myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
	      myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	      myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
	      myAudioRecorder.setOutputFile(outputFile);
      */
   }
   
  
   public void recordBackground(View view){
      try {
    	  
    	  int minBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		  mbackgroundRecorder = new AudioRecord( MediaRecorder.AudioSource.VOICE_COMMUNICATION, 
								  			 8000, 
								  			 AudioFormat.CHANNEL_IN_MONO,
								  			 AudioFormat.ENCODING_PCM_16BIT, 
								  			 minBufferSize);
    	  mbackgroundRecorder.startRecording();
    	 
    	  // Setup the write to file part.
    	  AudioWriterThread writerRunnable = new AudioWriterThread();
    	  writerRunnable.outputFile = sBackgroundAudio;
    	  writerRunnable.isRecording = true;
    	  AudioWriterThread.recorder = mbackgroundRecorder;
    	  
    	  // Writer thread. 
    	  writeRecordingThread = new Thread( writerRunnable, "AudioRecorder Thread" );
    	  writeRecordingThread.start();
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      // Start the thread.
      mStartRecordBackground.setEnabled(false);
      stop.setEnabled(true);
      Toast.makeText(getApplicationContext(), "Recording started background to " + sBackgroundAudio, Toast.LENGTH_LONG).show();

   }
   
   @SuppressLint("NewApi")
public void recordBackgroundWithAudio(View view){
    
	   // Start recorder.
	   try {
		   int minBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		   mForgroundRecorder= new AudioRecord( MediaRecorder.AudioSource.VOICE_COMMUNICATION, 
										  		8000, 
										  		AudioFormat.CHANNEL_IN_MONO,
										  		AudioFormat.ENCODING_PCM_16BIT, 
										  		minBufferSize);
		   
		   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { 
				boolean isNSPAvailable = NoiseSuppressor.isAvailable();
				if (isNSPAvailable) {
					NoiseSuppressor nsp = NoiseSuppressor.create(mForgroundRecorder.getAudioSessionId());
					nsp.setEnabled(true);
				}
				
				boolean isAvailable = AcousticEchoCanceler.isAvailable(); 
				if (isAvailable) { 
					AcousticEchoCanceler aec = AcousticEchoCanceler.create(mForgroundRecorder.getAudioSessionId());
				    if(!aec.getEnabled())
				         aec.setEnabled(true);
				    Log.d("recordBackgroundWithAudio", " applying AcousticEchoCanceler"); 
				}
				else {
					Log.d("recordBackgroundWithAudio", " could not apply AcousticEchoCanceler");
				}
			}
		   
		   
		   
    	  mForgroundRecorder.startRecording();
    	  
    	  // Setup the write to file part.
    	  AudioWriterThread writerRunnable = new AudioWriterThread();
    	  writerRunnable.isRecording = true;
    	  AudioWriterThread.recorder = mForgroundRecorder;
    	  writerRunnable.outputFile = sRegularAudio;
    	  
    	  // Writer thread.
    	  writeRecordingThread = new Thread( writerRunnable, "AudioRecorder Thread" );
    	  writeRecordingThread.start();
      } catch (Exception e) {
         e.printStackTrace();
      }
	  
	  Thread player = new Thread( new PlayAudio(view), "Audio Player thread");
	  player.start();
      	   
      mStartRecordBackground.setEnabled(false);
      stop.setEnabled(true);
      Toast.makeText(getApplicationContext(), "Recording Background Audio", Toast.LENGTH_LONG).show();
   }
   
   
   public class PlayAudio implements Runnable{
	private View mView;

		public PlayAudio(View view) {
			mView = view;
		}

		@Override
		public void run() {
			 
			   // Play background.
			   try {
				play(mView, sBackgroundAudio);
			   } catch( Exception e) {
					e.printStackTrace();
			   }
		}

   }
   
   
   
   public void stop(View view){
	   
	   // Stop recording.
	   AudioWriterThread.isRecording = false;
	   if ( mbackgroundRecorder != null ) {
		   mbackgroundRecorder.stop();
		   mbackgroundRecorder.release();
		   mbackgroundRecorder  = null;
	   }
	   
	   if ( mForgroundRecorder != null ) {
		   mForgroundRecorder.stop();
		   mForgroundRecorder.release();
		   mForgroundRecorder  = null;
	   }
	   
	   // Enable buttons.
	   stop.setEnabled(false);
	   play.setEnabled(true);
	   Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
    //  getMenuInflater().inflate(R.menu.main, menu);
      return true;
   }
   
   public void play(View view) throws IllegalArgumentException,SecurityException, IllegalStateException, IOException   {
   		play(view, sRegularAudio);
   }

   private void play(View view, String inputName) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException{
		// Byte read.
	 // setup a
	  int minBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
	  int bufferSize = 512;
	
	  AudioTrack audioTrack =  new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000, 
			  									AudioFormat.CHANNEL_OUT_MONO, 
			   									AudioFormat.ENCODING_PCM_16BIT, minBufferSize, 
			   									AudioTrack.MODE_STREAM);
	  int count = 0;
	  byte[] data = new byte[bufferSize];
	  try {
	      FileInputStream fileInputStream = new FileInputStream(inputName);
	      DataInputStream dataInputStream = new DataInputStream(fileInputStream);
	      audioTrack.play();
	      
	      while((count = dataInputStream.read(data, 0, bufferSize)) > -1){
	          audioTrack.write(data, 0, count); 
	      }
	      audioTrack.stop();
	      audioTrack.release();
	      dataInputStream.close();
	      fileInputStream.close();
	  
	  } catch (FileNotFoundException e) { 
	      e.printStackTrace();
	  } catch (IOException e) { 
	      e.printStackTrace();
	  }
   }
}
