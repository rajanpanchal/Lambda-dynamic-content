package net.rajanpanchal.handlers.templates;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class WelcomeLetterGeneratorHandler implements RequestHandler<Map<String, String>, String> {
	Regions clientRegion = Regions.US_EAST_1;
	String fileObjKeyName = "welcome_letter.txt";
	String bucketName = "welcomelettersbucket";
	static VelocityContext vContext;
	static Template t;
	AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion).build();
	static {
		try {
			// Create a new Velocity Engine
			VelocityEngine velocityEngine = new VelocityEngine();
			// Set properties that allow reading vm file from classpath.
			Properties p = new Properties();
			velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file,class");
			velocityEngine.setProperty("class.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			velocityEngine.init(p);
			t = velocityEngine.getTemplate("welcomeLetter.vm");
			vContext = new VelocityContext();

		} catch (Exception e) {
			throw new RuntimeException(e);

		}
	}

	public String handleRequest(Map<String, String> event, Context context) {
		String response = null;
		try {
			// Add data to velocity context
			vContext.put("name", "Rajan");
			vContext.put("creditCardNumber", "1234 5678 9012 3456");
			File f = new File(fileObjKeyName);

			FileWriter writer = new FileWriter(f);
			// merge template and Data
			t.merge(vContext, writer);
			writer.flush();
			writer.close();

			// Upload a file as a new object with ContentType and title specified.
			PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, f);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType("plain/text");
			metadata.addUserMetadata("title", "Welcome Letter");
			request.setMetadata(metadata);
			s3Client.putObject(request);
			response  = new String("Success");
		} catch (Exception ex) {
			response =  new String("Error:"+ex.getCause());
		}

		return response;
	}
}
