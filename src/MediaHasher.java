

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

import org.spongycastle.util.encoders.Hex;

import de.matthiasmann.jpegdecoder.JPEGDecoder;


public class MediaHasher 
{
	private final static int BYTE_READ_SIZE = 8192;

	public static void main (String[] args) throws Exception
	{
		File inputFile = new File(args[0]);
		//String hash = MediaHasher.getBitmapHash(new FileInputStream(inputFile));
		String hash = MediaHasher.getJpegHash(new FileInputStream(inputFile));
		
		System.out.println("got inputfile: " + args[0]);
		System.out.println("pixel hash=" + hash);
	}
	
	public static String hash (File file, String hashFunction)  throws IOException, NoSuchAlgorithmException
	{
		return hash (new FileInputStream(file), hashFunction);
	}
	
	public static String hash (byte[] bytes, String hashFunction) throws NoSuchAlgorithmException, IOException
	{
		return hash (new ByteArrayInputStream(bytes), hashFunction);
	}
	
	public static String hash (InputStream is, String hashFunction) throws IOException, NoSuchAlgorithmException
	{
		MessageDigest digester;
		
		digester = MessageDigest.getInstance(hashFunction); //MD5 or SHA-1
	
		  byte[] bytes = new byte[BYTE_READ_SIZE];
		  int byteCount;
		  while ((byteCount = is.read(bytes)) > 0) {
		    digester.update(bytes, 0, byteCount);
		  }
		  
		  byte[] messageDigest = digester.digest();
		  
		// Create Hex String WTF?!
		  	/*
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i<messageDigest.length; i++)
	            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
	        */
	        
	        return new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
	
	}
	
	public static String getJpegHash(InputStream is) throws NoSuchAlgorithmException, IOException {
		
		
		JPEGDecoder decoder = new JPEGDecoder(is);
	    decoder.decodeHeader();
	    int width = decoder.getImageWidth();
	    int height = decoder.getImageHeight();
	    decoder.startDecode();
	    
	    int stride = width*4; //4 bytes per pixel RGBA
	
	    MessageDigest digester = MessageDigest.getInstance("SHA-1");
		
	 //   System.out.println("Stride: " + stride);
	    
		for(int h=0; h<decoder.getNumMCURows(); h++) {
			
		    ByteBuffer bb = ByteBuffer.allocate(stride * decoder.getMCURowHeight());

		//	System.out.println("handling row: " + h);
			
		    decoder.decodeRGB(bb, stride, 1);
			
			digester.update(bb.array());
			
		}
		
		byte[] messageDigest = digester.digest();
		return new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
	}
	
	public static String getBitmapHash(InputStream is) throws NoSuchAlgorithmException, IOException {
		
		BufferedImage bitmap = ImageIO.read(is);
		MessageDigest digester = MessageDigest.getInstance("SHA-1");
		
		for(int h=0; h<bitmap.getHeight(); h++) {
			
			
			
			byte[] rowBytes = new byte[bitmap.getWidth()];
			
			for(int b=0; b<rowBytes.length; b++) {
				int p =  bitmap.getRGB(b, h);;
				rowBytes[b] = (byte)p;
				
				int R = (p >> 16) & 0xff;
				int G = (p >> 8) & 0xff;
				int B = p & 0xff;
				
				if (b == 0)
				System.out.println("row " + h + ": " + R +"," + G + "," + B);
			}
			
			digester.update(rowBytes);
			//byte[] messageDigest = digester.digest();
			//String lineHash = new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
			
			rowBytes = null;
			
		}
		
		byte[] messageDigest = digester.digest();
		return new String(Hex.encode(messageDigest), Charset.forName("UTF-8"));
	}
	
	/*
	public static String getBitmapHash(java.io.File file) throws NoSuchAlgorithmException, IOException {
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		String hash = "";
		ByteBuffer buf;
		
		buf = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
		
		bitmap.copyPixelsToBuffer(buf);
		hash = MediaHasher.hash(buf.array(), "MD5");
		buf.clear();
		buf = null;
		return hash;
	}
	
	public static String getBitmapHash(info.guardianproject.iocipher.FileInputStream fis) throws NoSuchAlgorithmException, IOException {
		Bitmap bitmap = BitmapFactory.decodeStream(fis);
		String hash = "";
		ByteBuffer buf;
		
		buf = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
		
		bitmap.copyPixelsToBuffer(buf);
		hash = MediaHasher.hash(buf.array(), "MD5");
		buf.clear();
		buf = null;
		return hash;
	}
	
	public static String getBitmapHash(java.io.FileInputStream fis) throws NoSuchAlgorithmException, IOException {
		Bitmap bitmap = BitmapFactory.decodeStream(fis);
		String hash = "";
		ByteBuffer buf;
		
		buf = ByteBuffer.allocate(bitmap.getRowBytes() * bitmap.getHeight());
		
		bitmap.copyPixelsToBuffer(buf);
		hash = MediaHasher.hash(buf.array(), "MD5");
		buf.clear();
		buf = null;
		return hash;
	}
	*/
	
}
