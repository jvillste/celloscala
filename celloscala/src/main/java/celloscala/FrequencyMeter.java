package celloscala;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class FrequencyMeter {
	TargetDataLine targetDataLine;
	
    float sampleRate = 44100;
    int sampleSizeInBits = 16;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = false;

	public FrequencyMeter(){
		try {
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
            targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(format, (int)sampleRate);
			targetDataLine.start();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double getFrequency(){
		
		double minimumFrequency = 30.0;
		byte[] buffer = new byte[2*(int)Math.round(sampleRate/minimumFrequency)];
        int[] a = new int[buffer.length/2];
        
        int n = -1;
        
        
        n = targetDataLine.read(buffer, 0, buffer.length);
        targetDataLine.stop();
        targetDataLine.flush();
        targetDataLine.start();
        
        for ( int i = 0; i < n; i+=2 ) {
        	// convert two bytes into single value
        	//int value = (short)((buffer[i]&0xFF) | ((buffer[i+1]&0xFF) << 8));
        	int value = (short)((buffer[i+1] << 8) + (buffer[i] & 0x00ff));
        	a[i >> 1] = value;
        }

        return getPitch(a,sampleRate);
	}

	public static double getPitch(int[] a, float sampleRate) {
		double prevDiff = 0;
        double prevDx = 0;
        double maxDiff = 0;
        double maximumFrequency = 1000.0;
        int len = a.length/2;

        for ( int i = (int)Math.round((sampleRate/maximumFrequency)); i < len; i++ ) {
        	double diff = 0;
        	for ( int j = 0; j < len; j++ ) {
        		diff += Math.abs(a[j]-a[i+j]);
        	}

        	double dx = prevDiff-diff;

        	// change of sign in dx
        	if ( dx < 0 && prevDx > 0 ) {
        		// only look for troughs that drop to less than 10% of peak
        		
        		if ( diff < (0.1*maxDiff) && i > 0) {
        			//System.out.println(sampleRate/(i-1));
        			return sampleRate/(i-1) +1;
        		}
        	}

        	prevDx = dx;
        	prevDiff=diff;
        	maxDiff=Math.max(diff,maxDiff);
        	
        }

        return 0;
	}
}
