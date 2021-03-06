/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import static javax.measure.unit.SI.KILOGRAM;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.measure.quantity.Mass;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.model.RelativisticModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@SpringBootApplication
public class Main 
{	
	@RequestMapping("/hello")
	String hello(Map<String, Object> model)
	{
	    RelativisticModel.select();
	    String energy = System.getenv().get("ENERGY");
		if (energy == null) {
	       energy = "12 GeV";
	    }
	    Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
		model.put("science", "Yo Ho Ho!"/* "E=mc^2: " + energy + " = " + m.toString() */);
	    return "hello";
	}

	@Value("${spring.datasource.url}")
	private String dbUrl;

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) throws Exception 
	{
		SpringApplication.run(Main.class, args);
	}

	@RequestMapping("/")
	String index() 
	{
		return "index";
	}

	@RequestMapping("/db")
	String db(Map<String, Object> model) 
	{
		try (Connection connection = dataSource.getConnection()) 
		{
			Statement stmt = connection.createStatement();
			// stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
			// stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
			// ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");
			//
			// ArrayList<String> output = new ArrayList<String>();
			// while (rs.next())
			// {
			// output.add("Read from DB: " + rs.getTimestamp("tick"));
			// }
			//
			// model.put("records", output);

			 stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
			 + "userID SERIAL PRIMARY KEY,"
			 + "name text NOT NULL,"
			 + "gender text NOT NULL,"
			 + "dateOfBirth date NOT NULL,"
			 + "CNIC BIGINT NOT NULL,"
			 + "Address text NOT NULL,"
			 + "contactNo text NOT NULL,"
			 + "username text NOT NULL UNIQUE,"
			 + "password text NOT NULL,"
			 + "dpURL text,"
			 + "role text NOT NULL,"
			 + "rating REAL"
			 + ")");
			 stmt.executeUpdate("CREATE TABLE IF NOT EXISTS comments ("
			 + "reviewID SERIAL PRIMARY KEY,"
			 + "userID INT REFERENCES users(userID) NOT NULL,"
			 + "review text NOT NULL"
			 + ")");
			 stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rooms ("
			 + "roomID SERIAL PRIMARY KEY,"
			 + "price REAL NOT NULL,"
			 + "availability boolean NOT NULL,"
			 + "noOfBeds INT NOT NULL,"
			 + "atCorner boolean NOT NULL,"
			 + "picURL text"
			 + ")");
			 stmt.executeUpdate(
					"INSERT INTO users (name, gender, dateOfBirth, CNIC, Address, contactNo, username, password, role, rating) values ('Ahsan', 'male', '1996-05-22', 123456789, 'Amity Park, London', '090078601', 'CodeSage', 'tempest', 'admin', NULL)");
			 model.put("records", "admin inserted successfully");
			return "db";
		} 
		catch (Exception e)
		{
			model.put("message", e.getMessage());
			return "error";
		}
	}

	@RequestMapping("/testUpload")
	String testUpload(Map<String, Object> model) 
	{
		try 
		{
			Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
			"cloud_name", "code-sage-cloud",
			"api_key", "623496281366913",
			"api_secret", "G6KiWcPb8twOAH2RMP-y9MCCB-A"));
			// Map uploadResult = cloudinary.uploader().upload(
			// "https://images.unsplash.com/photo-1432256851563-20155d0b7a39?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=7744813a62e994e17044d8ecb1516265&auto=format&fit=crop&w=1500&q=80",
			// ObjectUtils.emptyMap());
//			cloudinary.uploader().rename("noovhls0mjo6bzlmayal", "scene", ObjectUtils.emptyMap());
//			model.put("records", "test upload to Cloudinary successful!");
			Map uploadResult = cloudinary.uploader().upload("https://images.unsplash.com/photo-1500089386287-fa913ee39a49?ixlib=rb-0.3.5&ixid=eyJhcHBfaWQiOjEyMDd9&s=ff97e262f8a3b65af1b744c157801729&auto=format&fit=crop&w=1506&q=80", ObjectUtils.asMap(
					"public_id", "forest_cropped",
					"transformation", new Transformation().crop("limit").width(100).height(100)));
			return "db";
		}
		catch (Exception e)
		{
			model.put("message", e.getMessage());
			return "error";
		}
	}
	
	@GetMapping("/signup")
	String signupForm(Model model)
	{
		try 
		{
			model.addAttribute("user", new User());
			return "signup";
		}
		catch (Exception e)
		{
			model.addAttribute("message", e.getMessage());
			return "error";
		}
	}
	
	@PostMapping("/signup")
	String signupSubmit(@ModelAttribute @Valid User signUpForm, BindingResult bindingResult, Model model)
	{
		try 
		{
			if (bindingResult.hasErrors())
			{
				model.addAttribute("message", bindingResult.getAllErrors());
				return "error";
			}
			else
			{
				try (Connection connection = dataSource.getConnection())
				{
					Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", "code-sage-cloud", "api_key",
							"623496281366913", "api_secret", "G6KiWcPb8twOAH2RMP-y9MCCB-A"));

					File out = new File("temp.jpg");
					Map uploadResult = cloudinary.uploader().upload(
							out,
							ObjectUtils.asMap("public_id", signUpForm.getUsername() + "-dp", "transformation",
									new Transformation().crop("limit").width(200).height(200)));


					Statement stmt = connection.createStatement();
					stmt.executeUpdate(
							"INSERT INTO users (name, gender, dateOfBirth, CNIC, Address, contactNo, username, password, dpURL, role, rating) values ('"
									+ signUpForm.getName() + "', '" + signUpForm.getGender() + "', '"
									+ signUpForm.getDOB().toString()
									+ "', "
									+ signUpForm.getCNIC() + ", '" + signUpForm.getAddress() + "', '"
									+ signUpForm.getContactNo() + "', '" + signUpForm.getUsername() + "', '"
									+ signUpForm.getPassword() + "', '" + uploadResult.get("url") + "', 'user', NULL)");
					return "registered";
				}
				catch (Exception e)
				{
					model.addAttribute("message", e.getMessage());
					return "error";
				}
			}
		}
		catch (Exception e)
		{
			model.addAttribute("message", e.getMessage());
			return "error";
		}
	}
	
	@Bean
	public DataSource dataSource() throws SQLException 
	{
		if (dbUrl == null || dbUrl.isEmpty()) {
			return new HikariDataSource();
		} 
		else 
		{
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(dbUrl);
			return new HikariDataSource(config);
		}
	}
}
