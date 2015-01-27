package de.ifgi.lod4wfs.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import it.cutruzzula.lwkt.WKTParser;

public class Utils {

	
	public static boolean isWKT(String wkt){
		
		boolean result = true;
		
		try {
			
			WKTParser.parseToGML2(wkt);
			
			}
		catch(Exception e) {
			result = false;
		}
				
		return result;
		
	}

	public static String convertWKTtoGML(String literal){

		String gml = new String();

		if(isWKT(literal)){

			try {

				if(literal.contains("<") && literal.contains(">")){
					String CRS = new String();


					/**
					 * Extracting Reference System
					 */
					if(literal.contains("<") && literal.contains(">")){

						CRS = literal.substring(literal.indexOf("<") + 1, literal.indexOf(">"));
						literal = literal.substring(literal.indexOf(">") + 1, literal.length());

					}

					/**
					 * Removing Literal Type
					 */
					if(literal.contains("^^")){

						literal = literal.substring(0, literal.indexOf("^^"));

					}

					gml = WKTParser.parseToGML2(literal,CRS);


				} else {

					gml = WKTParser.parseToGML2(literal,GlobalSettings.defautlCRS);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return gml;

	}


	public static String convertWKTtoGeoJSON(String wkt){



		if(wkt.contains("<") && wkt.contains(">")){
			String CRS = new String();


			/**
			 * Extracting Reference System
			 */
			if(wkt.contains("<") && wkt.contains(">")){

				CRS = wkt.substring(wkt.indexOf("<") + 1, wkt.indexOf(">"));
				wkt = wkt.substring(wkt.indexOf(">") + 1, wkt.length());

			}

			/**
			 * Removing Literal Type
			 */
			if(wkt.contains("^^")){

				wkt = wkt.substring(0, wkt.indexOf("^^"));

			}

		}

		StringBuilder geoJSONStringBuilder = new StringBuilder();
		geoJSONStringBuilder.append(wkt.replace("(", "[").replace(")", "]").replace(", ",","));
		String geoType = geoJSONStringBuilder.substring(0, geoJSONStringBuilder.indexOf("[")).trim();
		geoJSONStringBuilder.delete(geoJSONStringBuilder.indexOf("[")-1, geoJSONStringBuilder.indexOf("["));

		boolean flagNumber = false;
		StringBuilder geoJSONOutuput = new StringBuilder();


		for (int i = 0; i < geoJSONStringBuilder.length(); i++) {

			if(geoJSONStringBuilder.charAt(i)=='[' ||
					geoJSONStringBuilder.charAt(i)==']' ||	
					geoJSONStringBuilder.charAt(i)=='.' ||
					geoJSONStringBuilder.charAt(i)=='-'){


				geoJSONOutuput.append(geoJSONStringBuilder.charAt(i));

			} else {

				if(Character.isDigit(geoJSONStringBuilder.charAt(i)) && flagNumber==false){

					if (!geoType.toUpperCase().equals("POINT")){

						/**
						 * Checking if the coordinate has negative values.
						 */
						if(geoJSONStringBuilder.charAt(i-1)=='-'){
							/**
							 * Closes the pair of coordinates with a squared bracket '['
							 */
							geoJSONOutuput.insert(geoJSONOutuput.length()-1, '[');

						} else {

							geoJSONOutuput.append("[");

						}
					}

					flagNumber = true;

					geoJSONOutuput.append(geoJSONStringBuilder.charAt(i));

				} else

					if(Character.isDigit(geoJSONStringBuilder.charAt(i)) && flagNumber==true){

						geoJSONOutuput.append(geoJSONStringBuilder.charAt(i));

					}

				if(geoJSONStringBuilder.charAt(i)==' '){

					geoJSONOutuput.append(",");

				}

				if(geoJSONStringBuilder.charAt(i)==','){

					geoJSONOutuput.append("],[");

				}
			}
		}

		if (!geoType.toUpperCase().equals("POINT")){

			geoJSONOutuput.append("]");
		}

		if (geoType.toUpperCase().equals("POINT")){
			geoType = "Point";
		} else if (geoType.toUpperCase().equals("MULTIPOLYGON")){
			geoType = "MultiPolygon";
		} else if (geoType.toUpperCase().equals("POLYGON")){
			geoType = "Polygon";
		} else if (geoType.toUpperCase().equals("MULTIPOINT")){
			geoType = "MultiPoint";
		} else if (geoType.toUpperCase().equals("LINESTRING")){
			geoType = "LineString";
		} else if (geoType.toUpperCase().equals("MULTILINESTRING")){
			geoType = "MultiLineString";
		} else if (geoType.toUpperCase().equals("GEOMETRYCOLLECTION")){
			geoType = "GeometryCollection";
		}

		return "{\"type\":\""+ geoType + "\",\"coordinates\":" + geoJSONOutuput + "},";


	}

	//TODO: isGML() to be implemented
	public static boolean isGML(String literal){

		return true;

	}


	//TODO: isGeoJSON() to be implemented
	public static boolean isGeoJSON(String literal){

		return true;

	}

	
	public static String getCanonicalHostName(){

		String result = new String();

		try {

			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();

			String eth0 = "";
			String wlan0 = "";
			
			while (en.hasMoreElements()) {

				NetworkInterface intf = en.nextElement();
				Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();

				
				if(intf.getName().equals("wlan0")){
					
					while (enumIpAddr.hasMoreElements()) {
						
						InetAddress inetAddress = enumIpAddr.nextElement();						
						wlan0 = inetAddress.getCanonicalHostName();
						
					}
					
				}

				if(intf.getName().equals("eth0")){
					
					while (enumIpAddr.hasMoreElements()) {
						
						InetAddress inetAddress = enumIpAddr.nextElement();						
						eth0 = inetAddress.getCanonicalHostName();
						
					}
					
				}
				
			}
			
			
			if (!eth0.equals("")) {
				
				result = eth0;
				
			} else {
				
				result = wlan0;
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}

		return result.toLowerCase();
	}

	public static File compressFile(String fileContent, String fileName) throws IOException{

		Path tempFile;


		tempFile = Files.createTempFile(null, ".tmp");

		File file = new File(tempFile.toString());
		FileWriter fw = new FileWriter(file);

		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(fileContent);
		bw.close();

		byte[] buffer = new byte[1024];

		File zipfile = new File(Files.createTempFile(null, ".zip").toString());
		
		FileOutputStream fos = new FileOutputStream(zipfile);
		ZipOutputStream zos = new ZipOutputStream(fos);

		ZipEntry ze= new ZipEntry(fileName);
		zos.putNextEntry(ze);
		FileInputStream in = new FileInputStream(file);

		int len;

		while ((len = in.read(buffer)) > 0) {
			
			zos.write(buffer, 0, len);
			
		}

		in.close();
		zos.closeEntry();
		zos.close();

		return zipfile;


	}

	 /** 
	 * @param XML Document
	 * @return string containing the given XML Document contents.
	 */
	public static String printXMLDocument(Document document){
		
		String XMLString = new String();
		StringWriter stringWriter = new StringWriter();		
		DOMSource source = new DOMSource(document);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		
		try {
			
			transformer = transformerFactory.newTransformer();
			StreamResult result = new StreamResult(stringWriter);
			transformer.transform(source, result);
			StringBuffer stringBuffer = stringWriter.getBuffer();
			XMLString = stringBuffer.toString();
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	
		return XMLString;
	}
}
