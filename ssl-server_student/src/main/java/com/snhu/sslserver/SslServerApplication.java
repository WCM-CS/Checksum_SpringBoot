package com.snhu.sslserver;

//Imports related to sping framework
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


//imports for hash function SHA-256
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//Link to the reference I used for the hash function implementation: https://www.geeksforgeeks.org/sha-256-hash-in-java/ 


// Starts the spring boot application allowing us to use related API's for request/controllers
@SpringBootApplication
public class SslServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SslServerApplication.class, args);
	}
	
}

@RestController
class ServerController{ 
	private AppData appData = new AppData();
	
	private class AppData{ //Data class for encapsulation
		private String data; // "Hello World Check Sum"; // stores the data we want to encrypt 
		private String hash; //"SHA-256"; //stores the name of the hash function we will use
		private String inputData;
		private String validatedHash;
		
		public AppData() {
			this.data = "Hello World Check Sum"; 
			this.hash = "SHA-256";
		}
		
		public String getData() {
			return data;
		}
		
		public String getHash() {
			return hash;
		}
		
		public void setInputData(String id) {
			this.inputData = id;
		}
		
		public void setValidatedHash(String vh) {
			this.validatedHash = vh;
		}
		
		public String getInputData() {
			return inputData;
		}
		
		public String getValidatedHash() {
			return validatedHash;
		}
	}

	
	@RequestMapping("/")
	public String home() {
		return "<html><body>"
	            + "<div style=\"display: flex; align-items: center;\">"
	            + "<div style=\"text-align: center;\">"
	            + "<h1 style=\"text-align: center;\"><strong>CS:305 </strong></h1>"
	            + "<img src=\"https://img.pixers.pics/pho_wat(s3:700/FO/42/22/32/95/700_FO42223295_0bf85350c145a33b27167d38090fa754.jpg,700,700,cms:2018/10/5bd1b6b8d04b8_220x50-watermark.png,over,480,650,jpg)/posters-cool-dragon.jpg.jpg\" alt=\"Cool Dragon\" style=\"max-width: 150px;\">"
	            + "<div>by Walker Martin</div>"
	            + "</div>"
	            + "</div>"
	            + "<hr>"
	            + "</body></html>";
	}

	
	
	 @GetMapping("/hash") // Add a separate method to handle GET request to display text to the user 
	    public String displayHashForm() throws NoSuchAlgorithmException {
	        String data = appData.getData();
	        String hash = appData.getHash();
	        String output = getSHA(data, hash);
	        
	        String context = createOutput(data, hash, output); 
	        
	        String htmlInput = "<div style=\"border: 1px solid black; padding: 20px;\">"
	                + "<h1>Validate Data</h1>"
	                + "<h4>Note: Blank spaces matter</h4>"
	                + "<form action=\"/hash\" method=\"POST\">"
	                + "<label for=\"inputData\">Input Data:</label>"
	                + "<input type=\"text\" id=\"inputData\" name=\"inputData\">"
	                + "<button type=\"submit\">Submit</button>"
	                + "<button type=\"reset\" style=\"background-color: red;\" onclick=\"clearInputData()\">Reset</button>"
	                + "</form>"
	                + "<br>"
	                + "<h2>Hash Output:</h2>"
	                + "<textarea rows=\"1\" cols=\"" + output.length() + "\" readonly id=\"outputData\">"
	                + "</textarea>"
	                + "<script>"
	                + "function clearInputData() {"
	                + "  document.getElementById('inputData').value = '';"
	                + "  document.getElementById('outputData').value = '';"
	                + "  var xhr = new XMLHttpRequest();"
	                + "  xhr.open('POST', '/resetInputData', true);"
	                + "  xhr.send();"
	                + "}"
	                + "</script>"
	                + "</div>";

	        return home() + context + htmlInput;
	    }

	    @PostMapping("/hash") //This method takes in user input through body of post making communications more secure 
	    public String myHash(@RequestParam("inputData") String inputData) throws NoSuchAlgorithmException {
	        String data = appData.getData();
	        String hash = appData.getHash();

	        appData.setInputData(inputData);
	        appData.setValidatedHash(getSHA(appData.getInputData(), hash));
	    
	        String output = getSHA(data, hash);
	    

	        String context = createOutput(data, hash, output);

	        String htmlInput = "<html><body><div style=\"border: 1px solid black; padding: 20px;\">"
	                + "<h1>Validate Data</h1>"
	                + "<form action=\"/hash\" method=\"POST\">"
	                + "<label for=\"inputData\">Input Data:</label>"
	                + "<input type=\"text\" id=\"inputData\" name=\"inputData\">"
	                + "<button type=\"submit\">Submit</button>"
	                + "<button type=\"reset\" style=\"background-color: red;\" onclick=\"clearInputData()\">Reset</button>"
	                + "</form>"
	                + "<br>"
	                + "<h2>Hash Output:</h2>"
	                + "<textarea rows=\"1\" cols=\"" + output.length() + "\" readonly id=\"outputData\">"
	                + appData.getValidatedHash()
	                + "</textarea>"
	                + "<script>"
	                + "function clearInputData() {"
	                + "  document.getElementById('inputData').value = '';"
	                + "  document.getElementById('outputData').value = '';"
	                + "  var xhr = new XMLHttpRequest();"
	                + "  xhr.open('POST', '/resetInputData', true);"
	                + "  xhr.send();"
	                + "}"
	                + "</script>"
	                + "</div></body></html>";
	    
	        return home() + context + htmlInput;
	    }
	    
	    @RequestMapping("/resetInputData") //function called when reset button is clicked 
	    public void resetInputData() {
	        appData.setInputData(""); // Set inputData to an empty string
	    }
	    
	    
	    private String getSHA(String data, String hash) throws NoSuchAlgorithmException{
	    	MessageDigest message = MessageDigest.getInstance(hash); //creates a hash encryption var message of type sha-256
	    	byte[] temp = message.digest(data.getBytes(StandardCharsets.UTF_8)); //uses hash var to encrypt the data stores it as bytes
	    	return bytesToHex(temp); //uses bytesToHex function to returns bytes encryption as a string instead
	    }
	
	    private String bytesToHex(byte[] encrypt) {
			BigInteger num = new BigInteger(1, encrypt); //converts bytes to big int
			StringBuilder hexStr = new StringBuilder(num.toString(16)); // makes a hex string with base 16 radix
			while (hexStr.length() < 64) { // uses length 64 since each hexadecimal reps 4 bits which equals 256
				hexStr.insert(0, '0');  //ensures proper length of the string by adding zeros when needed
			}
			return hexStr.toString(); //converts hex string to regular string and returns it
	    }
	
	    private String createOutput(String data, String hash, String out) { //This function creates a html formatting for the outputs 
	    	String encrypted = String.format("<span style=\"color: red;\">%s</span>", out); //turns the encrypted output red
	    	return String.format("<p>Data: %s<br>Algorithm: %s<br>Hash: %s<br></p>", data, hash, encrypted);
	    }
}

