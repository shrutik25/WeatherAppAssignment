import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WeatherReport {
		public static  String base_URL = "http://www.metoffice.gov.uk/climate/uk/summaries/datasets#Yearorder";
	public static void main(String args[]) throws ClientProtocolException, IOException, JSONException {
		HttpClient client  = new DefaultHttpClient();
		HttpGet request = new HttpGet(base_URL);
		HttpResponse response = client.execute(request);
//		HttpResponse response = client.execute(request);
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		 String line = "";
		 StringBuffer res = new StringBuffer();
		 	  while ((line = reader.readLine()) != null) {
		 	    System.out.println(line);
		 	    res.append(line);
		 	  }
		 	  
		 	  JSONArray responseJson = parseHtmlToJSON(res);
		 	  downloadFiles(responseJson);
		 

		 	  
	}
	private static void downloadFiles(JSONArray responseJson) throws IOException, JSONException {
		String[] regions = {"UK","England","Wales","Scotland"};
		String[] params = {"Tmax","Tmin","Tmean","Sunshine","Rainfall"};
		File[] inputFiles;
		ArrayList<String> targetList = new ArrayList<>();
		for(int index= 0; index <responseJson.length(); index++){
			JSONObject json =  responseJson.getJSONObject(index);
			JSONArray regionArray = json.getJSONArray(regions[index]);
			for(int i=0;i< regionArray.length();i++){
				JSONObject paramObject =  regionArray.getJSONObject(i);
				String url = paramObject.getString(params[i]);
				URL website = new URL(url);
				try (InputStream in = website.openStream()) {
					String target = "E:/downloadWeatherReport"+index+""+i;
					targetList.add(target);
					Path targetPath = Paths.get(target);
					Files.copy(in, targetPath,StandardCopyOption.REPLACE_EXISTING);
//				    Files.copy(in, targetPath);
				    
				    
				}		
			}
			
		}
		 File dir = new File("E:/downloadWeatherReport");
			File inputFolder = new File("E:/downloadWeatherReport");
			Set<String> alreadyMetNames = new HashSet<>();
		    File output = File.createTempFile("outputWeather", ".csv",dir);//File.createTempFile("outputWeather", ".csv");
		    try (FileWriter fw = new FileWriter(output); BufferedWriter bw = new BufferedWriter(fw)) {
//		        bw.write("region_code,weather_param,year, key, value");
		        bw.newLine();
		        for(int x=0; x<targetList.size();x++){
		        	File file = new File(targetList.get(x));
		            try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
		                String line;

		                // Search the header line
		                while ((line = br.readLine()) != null) {
//		                    if (line.startsWith(" ")||(line.startsWith("UK")|| line.startsWith("England") || line.startsWith("Wales")|| line.startsWith("Scotland"))){
		                    	
		                 
//		                    	String[] split = line.split(" ");
//		                    	for(int i=0; i<split.length;i++){
		                    		bw.write(line);
			                        bw.newLine();
//		                    	}
//		                    }
		                   
		                }

		                // Start to parse the file at the first row containing data
		               
		            }
		        }
		    }
		
		
	    
	    System.out.println("Your file is here : " + output.getAbsolutePath());
		
	}
	private static JSONArray parseHtmlToJSON(StringBuffer res) throws JSONException {
		// TODO Auto-generated method stub
		Document document = Jsoup.parse(res.toString());
	 	Element table = document.select("table").first();
	 	JSONObject jsonObj = new JSONObject();
	 	
	 	JSONArray regionArray = new JSONArray();
	 	Element yearOrderTable = table.getElementsByAttributeValue("summary", "Year ordered statistics").first();
	 	Elements rows = yearOrderTable.getElementsByTag("tr");
//	 	JSONObject regionObject = new JSONObject();
	 	for (int i = 1, l =5 ; i < l; i++) {
	 		Elements aTags = rows.get(i).getElementsByTag("a");
	 		Element header = rows.get(i).getElementsByTag("td").first();
	 		Element regionElement = header.getElementsByTag("strong").first();
	 		String region =  regionElement.text().toString();
	 		JSONObject regionObject = new JSONObject();
	 		JSONArray jsonArr = new JSONArray();
	 		for(int j = 0; j< 5; j++){
	 			Element aTag = aTags.get(j);
	 			String title = aTag.attr("title");
	 			title = title.substring(title.lastIndexOf(" ")+1);
	 			String url = aTag.attr("href");
	 			JSONObject jo = new JSONObject();
	 			 jo.put(title, url);
	 			jsonArr.put(jo);
	 		}
//	 		jsonArr.put(jo);
	 		regionObject.put(region,jsonArr);
	 		regionArray.put(regionObject);
	 		
	 	}
	 	
		return regionArray;
	}
}
