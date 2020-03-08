/*
  Copyright 2019~2020 Usbong Social Systems, Inc.

  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You ' may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, ' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing ' permissions and limitations under the License.

  @author: Michael Syson
  @date created: 20190807
  @date updated: 20200308

  Given:
  1) Lists with the details of the transactions for the day from the Physical and Occupational Therapists workbook at our partner hospital, St. Vincent General Hospital (SVGH)

  Output:
  1) Automatically connect to the database (DB) and send the details of the transactions to the computer server to store them in the DB
  
  Notes:
  1) The details of the transactions to be sent are in the JSON (JavaScript Object Notation) format.
    
  2) To compile on Windows' Command Prompt the add-on software with the libraries, e.g. JSON .jar file, use the following command:
   javac -cp .;org.json.jar;org.apache.httpclient.jar;org.apache.httpcore.jar;org.apache.commons-logging.jar UsbongHTTPConnect.java

  3) To execute on Windows' Command Prompt the add-on software with the JSON .jar file, i.e. json, use the following command:
   java -cp .;org.json.jar;org.apache.httpclient.jar;org.apache.httpcore.jar;org.apache.commons-logging.jar UsbongHTTPConnect

  4) The JSON .jar file can be downloaded here:
   https://github.com/stleary/JSON-java; last accessed: 20190808
   
  5) The two (2) Apache HttpComponents, i.e. 1) HttpClient and 2) HttpCore .jar files (not beta) can be downloaded here:
   http://hc.apache.org/downloads.cgi; last accessed: 20190810

  6) The Apache commons-logging .jar is also necessary to execute the add-on software. The .jar file is present in the set of .jar files inside the "lib", i.e. library, folder of the zipped httpcomponents-client-<version>-bin folder. It is in this same library folder that you can find the Apache HttpComponent, HttpClient, .jar file.
     
  References:
  1) Introducing JSON. https://www.json.org/; last accessed: 20190807
  --> ECMA-404 The JSON Data Interchange Standard
  
  2) https://stackoverflow.com/questions/7181534/http-post-using-json-in-java; last accessed: 20190807
  --> answer by: Cigano Morrison Mendez on 20131111; edited on 20140819
  
  3) The Apache Software Foundation. (2019). The Official Apache HttpComponents Homepage. https://hc.apache.org/index.html; last accessed: 20190810
*/

import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;

import java.util.Scanner;
import java.nio.charset.StandardCharsets;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import java.util.Date; //added by Mike, 20200213
import java.text.SimpleDateFormat; //added by Mike, 20200213

//location: St. Vincent General Hospital: Orthopedic and Physical Rehabilitation Unit
public class UsbongHTTPConnect { 
	//added by Mike, 20190811
	private static boolean isInDebugMode = false; //true;

	//added by Mike, 20190814; edited by Mike, 20200229
	private static boolean isForUpload = false; //default //true;

	//edited by Mike, 20190918
	//note: to verify use this
	//http://localhost/usbong_kms/server/storeTransactionsListsForTheDayFromOTAndPTAtSVGH.php
	private static String serverIpAddress = "http://localhost/"; //"";
	
	//edited by Mike, 20200227
/*	private static final String STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD = "usbong_kms/server/storetransactionslistfortheday.php";
*/
	private static final String STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD = "usbong_kms/server/storeTransactionsListsForTheDayFromPTAndOTAtSVGH.php";

	//edited by Mike, 20200228
/*	
	private static final String GET_TRANSACTIONS_LIST_FOR_THE_DAY_DOWNLOAD = "usbong_kms/server/gettransactionslistfortheday.php";
*/
	private static final String GET_TRANSACTIONS_LIST_FOR_THE_DAY_DOWNLOAD = "usbong_kms/server/getTransactionsListsForTheDayFromPTAndOTAtSVGH.php";

	//added by Mike, 20190812
	private static String inputFilename;
	private static int rowCount;

	//TO-DO: -update: this
	//added by Mike, 20190811; edited by Mike, 20200308
	private static final int INPUT_OR_NUMBER_COLUMN = 0; //Official Receipt Number
	private static final int INPUT_PATIENT_NAME_COLUMN = 1;
	private static final int INPUT_CLASSIFICATION_COLUMN = 2;
	private static final int INPUT_AMOUNT_PAID_COLUMN = 3;
	private static final int INPUT_NET_PF_COLUMN = 4;

	private static final int INPUT_TRANSACTION_DATE_COLUMN = 0;
//	private static final int INPUT_PATIENT_NAME_COLUMN = 1;
	private static final int INPUT_TRANSACTION_FEE_COLUMN = 17; //column R
	private static final int INPUT_TRANSACTION_FEE_DISCOUNT_COLUMN = 18; //column S
	private static final int INPUT_TRANSACTION_HMO_NAME_COLUMN = 18; //column S
	
	private static final int INPUT_TRANSACTION_FEE_COLUMN_IN_PT = 18; //column S
	private static final int INPUT_TRANSACTION_FEE_DISCOUNTCOLUMN_IN_PT = 17; //column Q
	
	
/*	
	private static String TAG = "usbong.HTTPConnect.storeTransactionsListForTheDay";	
	private static String TAG = "usbong.HTTPConnect.getTransactionsListForTheDay";	
*/

/*
	private String filePath = "";
	private String columnName = "";
	private String action = "";
*/	
	private URL url;
	private HttpURLConnection conn;

	public static void main(String[] args) throws Exception {
//		JSONObject json = new JSONObject();
//		json.put("myKey", "myValue");    

		UsbongHTTPConnect main = new UsbongHTTPConnect();

		//added by Mike, 20190918
		serverIpAddress = args[1];
		
		isForUpload = false;

		if (args[0].contains("upload")) {
			isForUpload = true;
		}

		//edited by Mike, 20190918		
		if (isForUpload) {
			//main.processUpload(new String[]{args[1]});
//			main.processPTAndOTReportInputForUpload(new String[]{args[1]});
			
			//start at 2, due to 0 being for the action, i.e. download or upload, and 1 being for the server IP address
			for(int iCount=2; iCount<args.length; iCount++) {
				//edited by Mike, 20200227
/*				main.processPTAndOTReportInputForUpload(new String[]{args[iCount]});				
*/
				main.processUpload(new String[]{args[iCount]});
			}
			
		}
		else {
			main.processDownload(new String[]{args[1]});
		}
	}
	
	//TO-DO: -update: this to store each patient name as a transaction;
	//note: computer server executes this action after receiving the transactions list which adheres to the JSON format
	//sending each transaction via the computer network fails, i.e. computer server does not receive the transactions
	private void processUpload(String[] args) throws Exception {
		//edited by Mike, 20200227
/*		JSONObject json = processPayslipInputForUpload(args);	
*/
		JSONObject json = processPTAndOTReportInputForUpload(args);	
				
		int totalTransactionCount = json.getInt("iTotal");
		System.out.println("totalTransactionCount: "+totalTransactionCount);

//		System.out.println(json.getInt("report_id"));				
		System.out.println(json.getInt("report_type_id"));
		System.out.println(json.getString("report_filename"));
		
		for(int transactionCount=0; transactionCount<totalTransactionCount; transactionCount++) {
			JSONObject transactionInJSONFormat = json.getJSONObject("i"+transactionCount);

//			System.out.println("json: "+transactionInJSONFormat.getString(""+INPUT_PATIENT_NAME_COLUMN));

/*
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();

			try {
				HttpPost request = new HttpPost(serverIpAddress+STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD);
				//edited by Mike, 20200306
//				StringEntity params = new StringEntity(json.toString(), "UTF-8");
				StringEntity params = new StringEntity(transactionInJSONFormat.getString(""+INPUT_PATIENT_NAME_COLUMN), "UTF-8");

				request.addHeader("content-type", "application/json; charset=utf-8'"); //edited by Mike, 20191012
				request.setEntity(params);
				httpClient.execute(request);
			} catch (Exception ex) {
				
			} finally {
				httpClient.close();
			}
*/
		}


		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(serverIpAddress+STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD);
			
			//edited by Mike, 20200306
			StringEntity params = new StringEntity(json.toString(), "UTF-8");

			request.addHeader("content-type", "application/json; charset=utf-8'"); //edited by Mike, 20191012
			request.setEntity(params);
			httpClient.execute(request);
		} catch (Exception ex) {
			
		} finally {
			httpClient.close();
		}
		
	}

	private void processUploadPrev(String[] args) throws Exception {
		//edited by Mike, 20200227
/*		JSONObject json = processPayslipInputForUpload(args);	
*/
		JSONObject json = processPTAndOTReportInputForUpload(args);	
				
//		System.out.println("json: "+json.toString());

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		try {
			HttpPost request = new HttpPost(serverIpAddress+STORE_TRANSACTIONS_LIST_FOR_THE_DAY_UPLOAD);
			StringEntity params = new StringEntity(json.toString(), "UTF-8"); //edited by Mike, 20191012
			request.addHeader("content-type", "application/json; charset=utf-8'"); //edited by Mike, 20191012
			request.setEntity(params);
			httpClient.execute(request);
		} catch (Exception ex) {
			
		} finally {
			httpClient.close();
		}
	}
	
	//added by Mike, 20190814; edited by Mike, 20190815
	//Reference: https://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientWithResponseHandler.java; last accessed: 20190814
	private void processDownload(String[] args) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		 try {
            HttpGet httpget = new HttpGet(serverIpAddress+GET_TRANSACTIONS_LIST_FOR_THE_DAY_DOWNLOAD);

            System.out.println("Executing request " + httpget.getRequestLine());

            //Create a custom response handler
            ResponseHandler<String> responseHandler = new MyResponseHandler();
			
            String responseBody = httpClient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody); 
			
			//edited by Mike, 20190820
			if (!responseBody.contains("No reports")) {
				System.out.println("JSON Array----------------------------------------");			
				//edited by Mike, 20200228
/*				processPayslipInputAfterDownload(responseBody);
*/

				processPTAndOTReportInputAfterDownload(responseBody);

			}			
        } finally {
            httpClient.close();
        }
	}
		
	//added by Mike, 20200213; edited by Mike, 20200304
	//location: St. Vincent General Hospital (SVGH): Orthopedic and Physical Rehabilitation Unit
	//Note: Physical and Occupational Therapy Treatment Report inputs
	private JSONObject processPTAndOTReportInputForUpload(String[] args) throws Exception {
		JSONObject json = new JSONObject();
//		json.put("myKey", "myValue");    

		//added by Mike, 20190812
		int transactionCount = 0; //start from zero
		
		//added by Mike, 20200213
		int iLastColumnCount = 0;

		for (int i=0; i<args.length; i++) {									
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			System.out.println("inputFilename: " + inputFilename);

			//added by Mike, 20200228
//			json.put("report_filename", new String(args[i].getBytes(), StandardCharsets.UTF_8));
			json.put("report_filename", autoEscapeToJSONFormat(args[i])); //TO-DO: -add: escape slash character


/*
			//added by Mike, 20190917
			//note that the default payslip_type_id is 2, i.e. "PT Treatment"
			if (inputFilename.contains("CONSULT")) {
				json.put("payslip_type_id", 1);    				
			}			
			else {
				json.put("payslip_type_id", 2);    				
			}
*/			

			//added by Mike, 20200227
			//note that the default report_type_id is 2, i.e. "OT and PT Treatment"
			json.put("report_type_id", 2);    				


			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
/*			
			//edited by Mike, 20191012
			//s=sc.nextLine(); 			
			s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

			//edited by Mike, 20190917
			json.put("dateTimeStamp", s.trim());

			//edited by Mike, 20191012
			//s=sc.nextLine(); 			
			s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);
			
			//edited by Mike, 20190917
			json.put("cashierPerson", s.trim().replace("\"",""));    
*/



			//TO-DO: -upload: only transactions for the day
			//output: 02/01/2020
			String dateTimeStamp = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
			System.out.println(">>" + dateTimeStamp);

			json.put("dateTimeStamp", dateTimeStamp);
//			json.put("inputFilename", inputFilename);    

/*	
			if (isInDebugMode) {
				rowCount=0;
			}
*/

/*			JSONObject transactionInJSONFormat = new JSONObject();
*/
			rowCount=1; //start at 1 given worksheet from MS EXCEL
						
			//count/compute the number-based values of inputColumns 
			while (sc.hasNextLine()) {			
			    //edited by Mike, 20191012
				//s=sc.nextLine();
				s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);
				
//				System.out.println("s: " + s);

				String[] inputColumns = s.split("\t");

				if (rowCount==1) { //do not include table header
/*				
					//identify last column
					int iColumnCount = 1; //start at 1 given worksheet from MS EXCEL
					while (iColumnCount <= inputColumns.length) {
						iColumnCount++;
					}
					
					iLastColumnCount = iColumnCount-1;
				
*/					
/*
					//edited by Mike, 20190813
					json.put("i"+transactionCount, transactionInJSONFormat);    				
					transactionCount++;
*/
					rowCount++;

					continue;
				}

				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				

/*				transactionInJSONFormat = new JSONObject();
				

				//edited by Mike, 20200229
				s = autoEscapeToJSONFormat(s);

				transactionInJSONFormat.put("i"+transactionCount, s); 		
*/
				//added by Mike, 20200306
				//patient name
				JSONObject transactionInJSONFormat = new JSONObject();

				transactionInJSONFormat.put(""+INPUT_PATIENT_NAME_COLUMN, autoEscapeToJSONFormat(inputColumns[INPUT_PATIENT_NAME_COLUMN])); //.replace("\"",""));

				//added by Mike, 20200308
				transactionInJSONFormat.put(""+INPUT_TRANSACTION_DATE_COLUMN, autoEscapeToJSONFormat(inputColumns[INPUT_TRANSACTION_DATE_COLUMN]));
				
				if (inputFilename.contains("IN-PT")) {
					//TO-DO: -update: this
/*					
					private static final int INPUT_TRANSACTION_FEE_COLUMN_IN_PT = 19; //column S
					private static final int INPUT_TRANSACTION_FEE_DISCOUNTCOLUMN_IN_PT = 18; //column Q
*/				
				}
				else {
					transactionInJSONFormat.put(""+INPUT_TRANSACTION_FEE_COLUMN, autoEscapeToJSONFormat(inputColumns[INPUT_TRANSACTION_FEE_COLUMN]));

//					if (isNumeric(inputColumns[INPUT_TRANSACTION_HMO_NAME_COLUMN])) {
						if ((inputFilename.contains("LASER CASH")) || (inputFilename.contains("SWT CASH"))) {
							transactionInJSONFormat.put(""+INPUT_TRANSACTION_FEE_DISCOUNT_COLUMN, -1);
						}
						else {
							if (inputColumns.length!=INPUT_TRANSACTION_FEE_DISCOUNT_COLUMN) {
								transactionInJSONFormat.put(""+INPUT_TRANSACTION_FEE_DISCOUNT_COLUMN, autoEscapeToJSONFormat(inputColumns[INPUT_TRANSACTION_FEE_DISCOUNT_COLUMN]));
							}

							//hmo name
							if (inputColumns.length!=INPUT_TRANSACTION_HMO_NAME_COLUMN) {
								if (isNumeric(inputColumns[INPUT_TRANSACTION_HMO_NAME_COLUMN])) {
									transactionInJSONFormat.put(""+INPUT_TRANSACTION_HMO_NAME_COLUMN, autoEscapeToJSONFormat(inputColumns[INPUT_TRANSACTION_HMO_NAME_COLUMN]));
								}
							}
						}
/*					}
					else { //hmo name
						transactionInJSONFormat.put(""+INPUT_TRANSACTION_HMO_NAME_COLUMN, autoEscapeToJSONFormat(inputColumns[INPUT_TRANSACTION_HMO_NAME_COLUMN]));
					}
*/
				}
				
				json.put("i"+transactionCount, transactionInJSONFormat);    				
				transactionCount++;

				rowCount++;

				if (isInDebugMode) {
					System.out.println("rowCount: "+rowCount);
				}
			}				
		}
				
		//added by Mike, 20190812; edited by Mike, 20190815
		json.put("iTotal", transactionCount);    				
								
		System.out.println("json: "+json.toString());
		
		return json;
	}
	

	private String autoEscapeToJSONFormat(String s) {
		s = s.replace("\t","\\t");
		s = s.replace("\\","\\\\");

		s = s.replace("'","u2018"); //left single quote
		s = s.replace("'","u2019"); //right single quote

		s = s.replace("\"","");
//		s = s.replace("\"","\\\"");
//		s = s.replace("\'","\\'");		
		s = s.replace("\n","\\n");
		
		s = s.replace("Ñ", "u00d1");
		s = s.replace("ñ", "u00f1");
		s = s.replace("®", "u00ae");
		
		return s;
	}

	private String autoEscapeFromJSONFormat(String s) {
		s = s.replace("\\\\t","\\t");

//		s = s.replace("\\\\","\\");

		s = s.replace("u2018", "'"); //left single quote
		s = s.replace("u2019", "'"); //right single quote


//		s = s.replace("\"","");

//		s = s.replace("\"","\\\"");
//		s = s.replace("\'","\\'");		
	
		s = s.replace("\\n","\n");

		s = s.replace("u00d1", "Ñ");
		s = s.replace("u00f1", "ñ");
		s = s.replace("u00ae", "®");
				
		return s;
	}

	//location: Sta. Lucia Health Care Centre (SLHCC): Orthopedic and Physical Therapy Unit
	//added by Mike, 20190811; edited by Mike, 20190812
	//Note: Consultation and PT Treatment payslip inputs are processed separately
	private JSONObject processPayslipInputForUpload(String[] args) throws Exception {
		JSONObject json = new JSONObject();
//		json.put("myKey", "myValue");    

		//added by Mike, 20190812
		int transactionCount = 0; //start from zero

		for (int i=0; i<args.length; i++) {									
			inputFilename = args[i].replaceAll(".txt","");			
			File f = new File(inputFilename+".txt");

			//added by Mike, 20190917
			//note that the default payslip_type_id is 2, i.e. "PT Treatment"
			if (inputFilename.contains("CONSULT")) {
				json.put("payslip_type_id", 1);    				
			}			
			else {
				json.put("payslip_type_id", 2);    				
			}
			
			Scanner sc = new Scanner(new FileInputStream(f));				
		
			String s;		
			
			//edited by Mike, 20191012
			//s=sc.nextLine(); 			
			s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

			//edited by Mike, 20190917
			json.put("dateTimeStamp", s.trim());

			//edited by Mike, 20191012
			//s=sc.nextLine(); 			
			s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);
			
			//edited by Mike, 20190917
			json.put("cashierPerson", s.trim().replace("\"",""));    
	
			if (isInDebugMode) {
				rowCount=0;
			}
						
			//count/compute the number-based values of inputColumns 
			while (sc.hasNextLine()) {				
			    //edited by Mike, 20191012
				//s=sc.nextLine();
				s = new String(sc.nextLine().getBytes(), StandardCharsets.UTF_8);

				//if the row is blank
				if (s.trim().equals("")) {
					continue;
				}
				
				String[] inputColumns = s.split("\t");					

				//System.out.println(s);
				//json.put("myKey", "myValue");    

				//added by Mike, 20190812; edited by Mike, 20190816
				JSONObject transactionInJSONFormat = new JSONObject();
				transactionInJSONFormat.put(""+INPUT_OR_NUMBER_COLUMN, Integer.parseInt(inputColumns[INPUT_OR_NUMBER_COLUMN]));
				transactionInJSONFormat.put(""+INPUT_PATIENT_NAME_COLUMN, inputColumns[INPUT_PATIENT_NAME_COLUMN].replace("\"",""));
				transactionInJSONFormat.put(""+INPUT_CLASSIFICATION_COLUMN, inputColumns[INPUT_CLASSIFICATION_COLUMN]);
				transactionInJSONFormat.put(""+INPUT_AMOUNT_PAID_COLUMN, inputColumns[INPUT_AMOUNT_PAID_COLUMN]);
				transactionInJSONFormat.put(""+INPUT_NET_PF_COLUMN, inputColumns[INPUT_NET_PF_COLUMN]);

				//edited by Mike, 20190813
				json.put("i"+transactionCount, transactionInJSONFormat);    				
				transactionCount++;

				if (isInDebugMode) {
					rowCount++;
					System.out.println("rowCount: "+rowCount);
				}
			}				
		}
		
		//added by Mike, 20190812; edited by Mike, 20190815
		json.put("iTotal", transactionCount);    				
								
		System.out.println("json: "+json.toString());
		
		return json;
	}	

	//added by Mike, 20200228; edited by Mike, 20200229
	//location: St. Vincent General Hospital (SVGH): Orthopedic and Physical Rehabilitation Unit
	//Note: Physical and Occupational Therapy Treatment Report inputs
	//TO-DO: -update: this for the computer to write the values from the database in .txt files
	private void processPTAndOTReportInputAfterDownload(String s) throws Exception {		

System.out.println("downloaded string: " + s +"\n");

		JSONArray nestedJsonArray = new JSONArray(s);

		if (nestedJsonArray != null) {

		   for(int j=0;j<nestedJsonArray.length();j++) {
				JSONObject jo_inside = nestedJsonArray.getJSONObject(j);

				System.out.println(jo_inside.getInt("report_id"));				
				System.out.println(jo_inside.getInt("report_type_id"));
				
//				System.out.println("filename: " + autoEscapeFromJSONFormat(jo_inside.getString("report_filename")));

				String filename = autoEscapeFromJSONFormat(jo_inside.getString("report_filename"));
				String updatedFilename = filename.split("client")[1].replace("\\","");
				
//				System.out.println("updated filename: " + autoEscapeFromJSONFormat(jo_inside.getString("report_filename").split("client")[1].replace("\\","")));
								
//				PrintWriter writer = new PrintWriter("output/SVGH/server/halimbawa.txt", "UTF-8");	

				PrintWriter writer = new PrintWriter("output/SVGH/server/" + updatedFilename, "UTF-8");	
				
				String reportDescriptionArray = autoEscapeFromJSONFormat(jo_inside.getString("report_description"));

//				System.out.println(">> " +reportDescriptionArray);

				writer.write(reportDescriptionArray);
				
				JSONObject reportInJSONFormat = new JSONObject(reportDescriptionArray);//.replace("\t",","));

				int totalTransactionCount = reportInJSONFormat.getInt("iTotal");
				System.out.println("totalTransactionCount: "+totalTransactionCount);

				writer.close();

		   }   
		}
	}
	
	//added by Mike, 20190812; edited by Mike, 20191026
	//Note: Consultation and PT Treatment payslip inputs are automatically identified
	private void processPayslipInputAfterDownload(String s) throws Exception {		
		JSONArray nestedJsonArray = new JSONArray(s);
		
		//edited by Mike, 20190917
		PrintWriter writer = new PrintWriter("output/payslipPTFromCashier.txt", "UTF-8");	
		//PrintWriter writer = new PrintWriter("");
		
		//added by Mike, 20191026
		PrintWriter consultationWriter = new PrintWriter("output/payslipConsultationFromCashier.txt", "UTF-8");	
		
		if (nestedJsonArray != null) {
		   for(int j=0;j<nestedJsonArray.length();j++) {
				JSONObject jo_inside = nestedJsonArray.getJSONObject(j);

/*				//removed by Mike, 20191026				
				//added by Mike, 20190917
				if (jo_inside.getInt("payslip_type_id") == 1) {
					writer = new PrintWriter("output/payslipConsultationFromCashier.txt", "UTF-8");	
				}
*/				
/*				else {
					writer = new PrintWriter("output/payslipPTFromCashier.txt", "UTF-8");	
				}
*/				
				System.out.println(""+jo_inside.getString("payslip_description"));				
				
				JSONObject payslipInJSONFormat = new JSONObject(jo_inside.getString("payslip_description"));

				int totalTransactionCount = payslipInJSONFormat.getInt("iTotal");
				System.out.println("totalTransactionCount: "+totalTransactionCount);
				
				//added by Mike, 20190821
				int count;
				
				for (int i=0; i<totalTransactionCount; i++) {
					JSONArray transactionInJSONArray = payslipInJSONFormat.getJSONArray("i"+i);
					
//					System.out.println(""+transactionInJSONArray.getInt(0)); //Official Receipt Number
//					System.out.println(""+transactionInJSONArray.getString(1)); //Patient Name

					//edited by Mike, 20190821
					count = i+1;
					
					String outputString = 	this.getDate(payslipInJSONFormat.getString("dateTimeStamp")) + "\t" +
							   count + "\t" +
							   transactionInJSONArray.getInt(INPUT_OR_NUMBER_COLUMN) + "\t" +
							   transactionInJSONArray.getString(INPUT_PATIENT_NAME_COLUMN) + "\t" +
							   "\t" + //FEE COLUMN
							   transactionInJSONArray.getString(INPUT_CLASSIFICATION_COLUMN) + "\t" +
							   transactionInJSONArray.getString(INPUT_AMOUNT_PAID_COLUMN) + "\t" +
							   //edited by Mike, 20191010
							   transactionInJSONArray.getString(INPUT_NET_PF_COLUMN) + "\t"; //"\n";

					//added by Mike, 20191010
					outputString = outputString + jo_inside.getString("added_datetime_stamp") + "\t" +
												  payslipInJSONFormat.getString("cashierPerson") + "\n";
			
					//added by Mike, 20191012
//					outputString = outputString.replace("u00d1", "Ñ");

					//edited by Mike, 20191026
					//write in Tab-delimited .txt file
/*					writer.write(outputString);
*/
					if (jo_inside.getInt("payslip_type_id") == 1) {
						consultationWriter.write(outputString);
					}
					else {
						writer.write(outputString);
					}
				}
		   }
		   
		   //added by Mike, 20190817; edited by Mike, 20191026
		   writer.close();
		   consultationWriter.close();
		}
	}
	
	//added by Mike, 20190820
	//input: 2019-08-11T14:12:16
	//output: 08/16/2019
	//note: when the date is imported to MS EXCEL the format becomes the intended 16/08/2019
	private String getDate(String dateTimeStamp) {
		String[] dateStringPart1 = dateTimeStamp.split("T");		
		String[] dateStringPart2 = dateStringPart1[0].split("-");		
		
		return dateStringPart2[1] + "/" + dateStringPart2[2] + "/" + dateStringPart2[0];
	}	

	//added by Mike, 20200308
	//Reference:
	//https://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java;
	//answer by: CraigTP on 20090709; edited by: Javad Besharati on 20190302
	public static boolean isNumeric(String str) { 
	  try {  
		Double.parseDouble(str);  
		return true;
	  } catch(NumberFormatException e){  
		return false;  
	  }  
	}

}

//added by Mike, 20190814; edited by Mike, 20190815
//Create a custom response handler
//Reference: https://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientWithResponseHandler.java; last accessed: 20190814
class MyResponseHandler implements ResponseHandler<String> {
	@Override
	public String handleResponse(
			final HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
		if (status >= 200 && status < 300) {
			HttpEntity entity = response.getEntity();
			return entity != null ? EntityUtils.toString(entity) : null;
		} else {
			throw new ClientProtocolException("Unexpected response status: " + status);
		}
	}		
}